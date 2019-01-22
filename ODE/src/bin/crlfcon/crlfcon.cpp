#define _ODE_BIN_CRLFCON_CRLFCON_CPP_

#include <ctype.h>

#include "bin/crlfcon/crlfcon.hpp"
#include "lib/string/string.hpp"
#include "lib/io/path.hpp"
#include "lib/exceptn/ioexcept.hpp"


String CRLFCon::program_name = "crlfcon";


/**
 * Main entry point.
**/
int CRLFCon::classMain( const char **argv, const char **envp )
{
  CRLFCon mp( argv, envp );
  if (!mp.run())
    Interface::quit( 1 );
  return (0);
}


/**
 *
**/
boolean CRLFCon::run()
{
  checkCommandLine();

  StringArray files;
  cmdline->getUnqualifiedVariables( &files );

  if (files.size() < 1 || files.size() > 2)
  {
    Interface::printError( program_name + ": Wrong number of files specified" );
    printUsage();
    Interface::quit( 1 );
  }

  // if infile and outfile are the same, pretend outfile wasn't given
  if (files.size() == 2 && Path::isSamePath( files[ARRAY_FIRST_INDEX],
      files[ARRAY_FIRST_INDEX + 1] ))
    files.setNumElements( 1 ); // quick way to ignore the second element

  return (convertFile( files ));
}


/**
 * Evaluate the command line arguments.
**/
void CRLFCon::checkCommandLine()
{
  StringArray arguments( args );
  const char *uv[] = { "-eof", 0 };
  StringArray unqual_vars( uv );
  const char *qv[] = { "-format", 0 };
  StringArray qual_vars( qv );

  cmdline = new CommandLine( &unqual_vars, &qual_vars, true, arguments,
      true, *this );
  cmdline->process();

  use_eof = cmdline->isState( "-eof" );

  if (cmdline->isState( "-format" ))
  {
    String fmt = cmdline->getQualifiedVariable( "-format" );
    format = tolower( fmt[STRING_FIRST_INDEX] );
    if (fmt.length() > 1 || (format != 'u' && format != 'd'))
    {
      Interface::printError( program_name + ": Invalid format: " + fmt );
      printUsage();
      Interface::quit( 1 );
    }
  }
  else
#ifdef DEFAULT_SHELL_IS_CMD
    format = 'd';
#else
    format = 'u';
#endif

  Interface::printVerbose( "Target format: " + String( format ) );
}


/**
 *
**/
boolean CRLFCon::verifyFile( const String &file, boolean writable )
{
  struct ODEstat filestat;

  if (ODEstat( Path::canonicalize( file, false ).toCharPtr(),
      &filestat, OFFILE_ODEMODE, 1 ) != 0)
    Interface::printError( program_name +
        ": " + file + " doesn't exist" );
  else if (!filestat.is_file)
    Interface::printError( program_name +
        ": " + file + " isn't a file" );
  else if (!filestat.is_readable)
    Interface::printError( program_name +
        ": Don't have read access to " + file );
  else if (writable && !filestat.is_writable)
    Interface::printError( program_name +
        ": Don't have write access to " + file );
  else
    return (true);

  return (false);
}


/**
 *
**/
boolean CRLFCon::convertFile( const StringArray &files )
{
  ifstream *ifptr = 0;
  fstream *ofptr = 0;
  String linebuf;
  String outfile;
#ifdef EBCDIC_CHARSET
  char cr = (char)13;
  char lf = (char)21;
  char eof = (char)63;
#else /* ASCII */
  char cr = (char)13;
  char lf = (char)10;
  char eof = (char)26;
#endif

  if (!verifyFile( files[ARRAY_FIRST_INDEX], files.size() == 1 ))
    return (false);

  if (cmdline->isState( "-info" ))
  {
    Interface::print( "INFO: Would convert " + files[ARRAY_FIRST_INDEX] );
    return (true);
  }

  if (files.size() == 2)
    outfile = files[ARRAY_FIRST_INDEX + 1];
  else
    outfile = Path::tempFilename();

  try
  {
    ifptr = Path::openFileReader( files[ARRAY_FIRST_INDEX], true );
    ofptr = Path::openFileWriter( outfile, false, true, true );
  }
  catch (IOException &e)
  {
    Interface::printError( program_name + ": " + e.getMessage() );
    if (ifptr != 0)
      Path::closeFileReader( ifptr );
    return (false);
  }

  if (files.size() == 1)
    Interface::printDebug( "Temp file: " + outfile );

  while (Path::readLine( *ifptr, &linebuf ))
  {
    // always trim off the EOF...it will be re-added later if needed
    if (linebuf.charAt( linebuf.lastIndex() ) == eof)
    {
      linebuf.remove( linebuf.lastIndex(), 1 );
      *ofptr << linebuf;
      break;
    }
    *ofptr << linebuf;
    if (format == 'd')
      *ofptr << cr;
    *ofptr << lf;
  }

  if (format == 'd' && use_eof)
    *ofptr << eof;

  Path::closeFileReader( ifptr );
  Path::closeFileWriter( ofptr );

  boolean rc = true;

  if (files.size() == 1)
  {
    rc = Path::copyFile( outfile, files[ARRAY_FIRST_INDEX], true, 0, true );
    Path::deletePath( outfile );
  }

  return (rc);
}


/**
 *
**/
void CRLFCon::printUsage() const
{
  Interface::printAlways( "Usage: " + program_name +
      " [-format <u|d>] [-eof] [ODE options] infile [outfile]" );
  Interface::printAlways( "" );
  Interface::printAlways( "       -format <u|d>: output format "
      "(unix|dos)" );
  Interface::printAlways( "       -eof: add Ctrl-Z at EOF (only for 'd' "
      "format)" );
  Interface::printAlways( "" );
  Interface::printAlways( "   ODE options:" );
  Interface::printAlways( "       -quiet -normal -verbose -debug -usage "
      "-version -rev -info -auto" );
  Interface::printAlways( "" );
  Interface::printAlways( "       infile: input text file to convert" );
  Interface::printAlways( "       outfile: file in which results are stored" );
}
