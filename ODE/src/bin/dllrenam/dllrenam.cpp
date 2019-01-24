using namespace std;
#define _ODE_BIN_DLLRENAM_DLLRENAM_CPP_

#include <ctype.h>

#include "bin/dllrenam/dllrenam.hpp"
#include "lib/exceptn/ioexcept.hpp"


String DLLRename::program_name = "dllrenam";


/**
 * Main entry point.
**/
int DLLRename::main( const char **argv, const char **envp )
{
  DLLRename mp( argv, envp );
  if (mp.run())
    return (0);
  else
    return (1);
}


/**
 *
**/
boolean DLLRename::run()
{
  checkCommandLine();

  StringArray files;
  cmdline->getUnqualifiedVariables( &files );

  if (files.size() < 2 || files.size() > 4)
  {
    Interface::printError( program_name + ": Wrong number of files specified" );
    printUsage();
    Interface::quit( 1 );
  }
  else if (files.size() == 2)
    return (changeNames( files[ARRAY_FIRST_INDEX],
        files[ARRAY_FIRST_INDEX + 1] ));
  else if (files.size() == 3)
    return (changeNames( files[ARRAY_FIRST_INDEX],
        files[ARRAY_FIRST_INDEX + 1],
        files[ARRAY_FIRST_INDEX + 2] ));
  else
    return (changeNames( files[ARRAY_FIRST_INDEX],
        files[ARRAY_FIRST_INDEX + 1],
        files[ARRAY_FIRST_INDEX + 2],
        files[ARRAY_FIRST_INDEX + 3] ));

  return (false); // shut the compiler up
}


/**
 * Evaluate the command line arguments.
**/
void DLLRename::checkCommandLine()
{
  StringArray arguments( args );
  const char *uv[] = { 0 };
  StringArray unqual_vars( uv );
  const char *qv[] = { 0 };
  StringArray qual_vars( qv );

  cmdline = new CommandLine( &unqual_vars, &qual_vars, true, arguments,
      true, *this );
  cmdline->process();
}


/**
 *
**/
boolean DLLRename::verifyInFile( const String &file )
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
  else
    return (true);

  return (false);
}


/**
 *
**/
boolean DLLRename::verifyOutDir( const String &dir )
{
  struct ODEstat dirstat;

  if (ODEstat( Path::canonicalize( dir, false ).toCharPtr(),
      &dirstat, OFFILE_ODEMODE, 1 ) != 0)
    Interface::printError( program_name +
        ": " + dir + " doesn't exist" );
  else if (!dirstat.is_dir)
    Interface::printError( program_name +
        ": " + dir + " isn't a dir" );
  else if (!dirstat.is_writable)
    Interface::printError( program_name +
        ": Don't have write access to " + dir );
  else
    return (true);

  return (false);
}


/**
 *
**/
boolean DLLRename::changeNames( const String &oldname, const String &newname,
    const String &olddir, const String &newdir )
{
  String oldlibpath, newlibpath, olddllpath, newdllpath;

  if (oldname.toUpperCase() == newname.toUpperCase())
  {
    Interface::printError( program_name +
        ": New name cannot be the same as old name" );
    return (false);
  }

/*
  if (oldname.length() != newname.length())
  {
    Interface::printError( program_name +
        ": Length of old and new names must be the same" );
    return (false);
  }
*/

  oldlibpath = olddir + "/" + oldname + ".LIB";
  newlibpath = newdir + "/" + newname + ".LIB";
  olddllpath = olddir + "/" + oldname + ".DLL";
  newdllpath = newdir + "/" + newname + ".DLL";

  if (!verifyInFile( oldlibpath ) || !verifyInFile( olddllpath ) ||
      !verifyOutDir( newdir ))
    return (false);

  if (cmdline->isState( "-info" ))
  {
    Interface::print( "INFO: Would convert " + oldname + " to " + newname );
    return (true);
  }

  return (changeName( oldname, newname, oldlibpath, newlibpath ) &&
      changeName( oldname, newname, olddllpath, newdllpath ));
}


/**
 *
**/
boolean DLLRename::changeName( const String &oldname, const String &newname,
    const String &oldpath, const String &newpath )
{
  ifstream *ifptr = 0;
  fstream *ofptr = 0;
  Array< int > readbuf, checkbuf;
  int ch, chkidx = STRING_FIRST_INDEX;
  boolean got_match = false;

  try
  {
    ifptr = Path::openFileReader( oldpath, true );
    ofptr = Path::openFileWriter( newpath, false, true, true );
  }
  catch (IOException &e)
  {
    Interface::printError( program_name + ": " + e.getMessage() );
    if (ifptr != 0)
      Path::closeFileReader( ifptr );
    return (false);
  }

  while ((ch = nextChar( ifptr, readbuf )) != EOF)
  {
    if (ch == oldname[chkidx])
    {
      chkidx++;
      checkbuf += ch;
      if (chkidx > oldname.lastIndex()) // MATCH!
      {
        (*ofptr) << newname; // replace oldname with newname
        chkidx = STRING_FIRST_INDEX;
        checkbuf.clear();
        got_match = true;
      }
    }
    else
    {
      if (checkbuf.length() > 0) // found a partial match
      {
        // rebuild readbuf properly (everything in checkbuf, plus the
        // current char, plus whatever's left in readbuf)
        checkbuf += ch;
        checkbuf += readbuf;
        readbuf = checkbuf;
        checkbuf.clear();
        chkidx = STRING_FIRST_INDEX;
        ch = nextChar( 0, readbuf ); // remove char that started the match
      }
      ofptr->put( (unsigned char)ch );
    }
  }

  if (checkbuf.length() > 0)
    for (int i = ARRAY_FIRST_INDEX; i <= checkbuf.lastIndex(); ++i)
      ofptr->put( (unsigned char)checkbuf[i] );

  Path::closeFileReader( ifptr );
  Path::closeFileWriter( ofptr );

  if (!got_match)
    Interface::printWarning( program_name +
        ": No occurrences of " + oldname + " were found" );

  return (true);
}


int DLLRename::nextChar( ifstream *ifptr, Array< int > &buf )
{
  int rc;

  if (buf.length() > 0)
  {
    rc = buf[ARRAY_FIRST_INDEX];
    buf.removeAtPosition( ARRAY_FIRST_INDEX );
  }
  else if (ifptr != 0)
    rc = ifptr->get();
  else
    rc = EOF;

  return (rc);
}


/**
 *
**/
void DLLRename::printUsage() const
{
  Interface::printAlways( "Usage: " + program_name +
      " [ODE options] oldname newname [old_dir] [new_dir]" );
  Interface::printAlways( "" );
  Interface::printAlways( "   ODE options:" );
  Interface::printAlways( "       -quiet -normal -verbose -debug -usage "
      "-version -rev -info -auto" );
  Interface::printAlways( "" );
  Interface::printAlways( "       oldname: old LIB/DLL name" );
  Interface::printAlways( "       newname: new LIB/DLL name" );
  Interface::printAlways( "       old_dir: location of old LIB/DLL" );
  Interface::printAlways( "       new_dir: where to create new LIB/DLL" );
}
