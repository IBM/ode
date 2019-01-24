using namespace std ;
#define _ODE_LIB_IO_GENPATH_CPP_
#include "lib/intcmds/genpath.hpp"
#include "lib/io/path.hpp"
#include "lib/io/sandbox.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/string/strcon.hpp"
#include "lib/exceptn/sboxexc.hpp"


/**
 *
**/
int Genpath::run()
// throws Exception
{
  checkCommandLine();
  getSandbox();
  getDirs();
  setOutputParameters();
  createOutputDirs();

  return (0);
}

/**
 *
**/
boolean Genpath::run( const StringArray &args, String &buf,
    const SetVars *vars )
// throws Exception
{
  static Genpath genpath;
  genpath.reset();
  genpath.args = &args;
  buf = StringConstants::EMPTY_STRING;
  genpath.output_string = &buf;
  if (vars == 0)
    genpath.env_vars = Env::getSetVars();
  else
    genpath.env_vars = vars;
  return (genpath.run() == 0);
}

void Genpath::reset()
{
  args = 0;
  output_string = 0;
  env_vars = 0;
  delete cmdLine;
  cmdLine = 0;
  backedDirs.clear();
  vpathFormat = shouldExist = objDirabs = onlySrc = onlyObj = false;
}

/**
 * Creates a sandbox object only if the user specifies so on the command line
 * or if we aren't in a sandbox environment.
**/
void Genpath::getSandbox()
{
  String sb_name = cmdLine->getSandbox();
  String rc_file = cmdLine->getRCFile();

  if (inLibraryMode() && !inSbEnv())
    throw SandboxException( "not in a sandbox environment" );

  if (sb_name != StringConstants::EMPTY_STRING ||
      rc_file != StringConstants::EMPTY_STRING || !inSbEnv())
  {
    Sandbox sb( true, rc_file, sb_name, true );

    if (!inSbEnv())
      throw SandboxException( "Could not get env from sandbox" );
  }
}

/************************************************
  * --- void Genpath::createOutputDirs ---
  *
  * Creates an array of output directories for the 4 possible
  * kind of inputs.
  ************************************************/
void Genpath::createOutputDirs()
{
  StringArray quals( 4 ), qFlags, allPaths;

  if (!inLibraryMode() && Interface::isDebug())
    Interface::printDebug( "Generating output directories..." );

  quals.add( "-I" );
  quals.add( "-L" );
  quals.add( "-R" );
  quals.add( "-E" );

  cmdLine->getOrderedQualifiedVariables( quals, &qFlags, &allPaths );

  generatePaths( qFlags, allPaths );
}


/******************************************************************************
 * Concatenates the various "parts" of each path and adds the paths to the
 * output paths array
 * For three types of paths ILR, we need to include the OBJECTDIR too. Only
 * E paths are free from this restriction.
 */
void Genpath::generatePaths( const StringArray &qflags,
    const StringArray &dirs )
{
  int j, k;
  String fullExt, printstr;
  const String *exportDirPtr;

  for (int i = dirs.firstIndex(); i <= dirs.lastIndex(); i++)
  {
    fullExt = StringConstants::EMPTY_STRING;
    if (Path::absolute( dirs[i] ))    // if it is absolute don't do anything
      printPath( dirs[i] );           // just print it as it is
#ifdef DEFAULT_SHELL_IS_CMD
    else if (dirs[i].length() > 1 && isalpha( dirs[i][STRING_FIRST_INDEX] ) &&
        dirs[i][STRING_FIRST_INDEX + 1] == ':') // if drive letter
      printPath( dirs[i] );           // just print it as it is
#endif
    else
      switch (qflags[i].charAt( STRING_FIRST_INDEX + 1 ))
      {
        case 'I':
        case 'L':
          fullExt = getILpathExtensions();

        case 'R':
          fullExt += Path::DIR_SEPARATOR;
          fullExt += dirs[i];

          if (!onlySrc && objDir.length() > 0 && objDirabs)
            printPath( objDir + fullExt );  // print absolute obj dir just once

          for (j = backedDirs.firstIndex(); j <= backedDirs.lastIndex(); j++)
          {
            // if we did find OBJECTDIR env var
            if (!onlySrc && objDir.length() > 0 && !objDirabs)
            {
              printstr = backedDirs[j];
              printstr.append( Path::DIR_SEPARATOR ).append( objDir );
              printstr.append( fullExt );
              printPath( printstr );
            }

            if (!onlyObj)
              printPath( backedDirs[j]  + fullExt ); // print src path
          }
          break;

        case 'E':
          if (onlySrc || onlyObj)
            break;

          fullExt = Path::DIR_SEPARATOR;
          fullExt += dirs[i];

          exportDirPtr = Env::getenv( "EXPORTDIR" );
          if (exportDirPtr == 0)
            exportDirPtr = &StringConstants::EMPTY_STRING;
          for (k = backedDirs.firstIndex(); k <= backedDirs.lastIndex(); k++)
          {
            printstr = backedDirs[k];
            printstr.append( Path::DIR_SEPARATOR ).append( *exportDirPtr ) ;
            printstr.append( fullExt );
            printPath( printstr );
          }
          break;
      }
  }

  if (!inLibraryMode() && vpathFormat)
    Interface::printAlways( StringConstants::EMPTY_STRING ); // print newline
}

/******************************************************************************
 *
 */
void Genpath::getDirs()
{
  objDir = SandboxConstants::getOBJECTDIR();
  objDirabs = Path::absolute( objDir );

  const String &bdir = SandboxConstants::getBACKED_SANDBOXDIR();
  if (!bdir.isEmpty())
  {
    bdir.split( Path::PATH_SEPARATOR, UINT_MAX, &backedDirs );

    // Append "/src" to each element of the array.
    //
    for (int i = backedDirs.firstIndex(); i <= backedDirs.lastIndex(); i++)
    {
      backedDirs[i] += Path::DIR_SEPARATOR;
      backedDirs[i] += SandboxConstants::getSRCNAME();
    }
  }
}


/******************************************************************************
 * Prints the output directories in different formats based on the given
 * states
 */
void Genpath::printPath( const String &path ) const
{
  if (shouldExist && !Path::exists( path ))
      return;

  String unix_can_path = path;
  Path::canonicalizeThis( unix_can_path, false );
  Path::unixizeThis( unix_can_path );
#ifdef FILENAME_BLANKS
  unix_can_path.trimThis();
  unix_can_path.doubleQuoteThis();
#endif
  if (inLibraryMode())
  {
    *output_string += flag + unix_can_path;
    if (!vpathFormat)
      *output_string += StringConstants::SPACE;
  }
  else
  {
    Interface::printnlnAlways( flag + unix_can_path );
    if (!vpathFormat)
      Interface::printAlways( StringConstants::EMPTY_STRING ); // print newline
  }
}


/**
 * Note that hasOnlyGenpathFlags() might need to be modified
 * when flags are added or deleted.
**/
void Genpath::checkCommandLine()
// throws Exception
{
  const char *s[]  = {"-a", "-i", "-l", "-O", "-S", "-V", "-z", 0};
  const char *qV[] = {"-I", "-L", "-R", "-E", 0};
  StringArray states( s );
  StringArray qualVars( qV );
  if (!inLibraryMode())
  {
    qualVars.add( "-sb" );
    qualVars.add( "-rc" );
  }

  cmdLine = new CommandLine( &states, &qualVars, false, *args, false,
      *this, inLibraryMode() );
  cmdLine->process();

  // if both -S and -O specified output dirs that corresponding to the one
  // specified last
  StringArray OSstates;
  OSstates.add( "-S" );
  OSstates.add( "-O" );
  const String lastState = cmdLine->lastState( OSstates );
  if (lastState.equals( "-S" ))
    onlySrc = true;
  else if (lastState.equals( "-O" ))
    onlyObj = true;

}


/**
 * Parameter to control the output
**/
void Genpath::setOutputParameters()
{
  static StringArray states;
  if (states.size() < 1)
  {
    states.add( "-V" );
    states.add( "-i" );
    states.add( "-l" );
    states.add( "-z" );
  }

  const String lastState = cmdLine->lastState( states );

  shouldExist = cmdLine->isState( "-a" );

  if (lastState.equals( "-V" ))
  { 
    flag = Path::PATH_SEPARATOR; 
    vpathFormat = true; 
  }
  else if (lastState.equals( "-i" ))
    flag = "-I" ;  
  else if (lastState.equals( "-l" ))
    flag = "-L";  
  else if (lastState.equals( "-z" ))
    flag = StringConstants::EMPTY_STRING;
  else 
    flag = "-I"; //Default
}


/**
 *
**/
void Genpath::printUsage() const
{
  Interface::printAlways( "Usage: genpath [ODE opts] [sb opts] "
      "[genpath_flags...]" );
  Interface::printAlways( StringConstants::EMPTY_STRING );
  Interface::printAlways( "  genpath_flags:" );
  Interface::printAlways( "     -a      List a directory only if it exists" );
  Interface::printAlways( "     -i      Force -I output (default)" );
  Interface::printAlways( "     -l      Force -L output" );
  Interface::printAlways( "     -Idir   Add directory to include list" );
  Interface::printAlways( "     -Ldir   Add directory to library list" );
  Interface::printAlways( "     -Rdir   Add a relative directory to a list" );
  Interface::printAlways( "     -Edir   Add an export directory to a list" );
  Interface::printAlways( "     -O      Generate only object directories" );
  Interface::printAlways( "     -S      Generate only src directories" );
  Interface::printAlways( "     -V      Generate VPATH format output" );
  Interface::printAlways( "     -z      Do not generate a -[IL] flag "
      "during output" );
  Interface::printAlways( "  ODE opts:");
  Interface::printAlways( "     -quiet -normal -verbose -debug -usage "
      "-version -rev -info -auto" );
  Interface::printAlways( "  sb opts:" );
  Interface::printAlways( "     -rc <rc_file>, -sb <sandbox_name>" );
}
