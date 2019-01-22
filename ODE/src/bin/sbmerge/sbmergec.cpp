#define _ODE_BIN_SBMERGE_SBMERGE_CPP


#include "bin/sbmerge/sbmergec.hpp"
#include "lib/io/ui.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/portable/runcmd.hpp"
#include "lib/portable/env.hpp"
#include "lib/string/strcon.hpp"


/******************************************************************************
 *
 */
int SbMerge::classMain( const char **argv, const char **envp )
{
  SbMerge sb( argv, envp );
  return( sb.run() );
}


/******************************************************************************
 *
 */
int SbMerge::run()
{
  boolean rc = true;

  processCommandLine();
  separateFilesAndMergeArgs();
  getBackingChain();

  // Attempt to merge each file listed on command line.
  for (int i = merge_files.firstIndex(); i <= merge_files.lastIndex(); i++)
  {
    if (!processMerge( merge_files[i] ))
      rc = false;
  }

  return (rc ? 0 : 1);
}


/******************************************************************************
 *
 */
void SbMerge::getBackingChain()
{
  Path::separatePaths( Path::normalize(
      SandboxConstants::getBACKED_SANDBOXDIR() ), &backingChain );

  if (backingChain.isEmpty())
  {
    Interface::printError( "sbmerge: Must be in a sandbox environment.");
    Interface::quit( 1 );
  }
}


/******************************************************************************
 *
 */
boolean SbMerge::processMerge( const String &mergeFile )
{
  boolean foundOtherMergeFile = false;
  boolean alreadyBackedUp     = false;
  boolean argFix              = false;
  boolean rc = true;
  String  fpMergeFile;   //full path merge file.
  String  mergeToolDefault = "merge";
  const String *mergeTool = Env::getenv( "ODEMERGE" );
  const String *strArgFix = Env::getenv( "SBMERGE_ARGFIX" );
  StringArray newcmd( 4 );
  String tempFile = StringConstants::EMPTY_STRING;
  // If no merge tool is specified, default to "merge".
  if (!mergeTool)
    mergeTool = &mergeToolDefault;
  Interface::printDebug( "mergeTool: " + *mergeTool );

  // If SBMERGE_ARGFIX is defined, remember it.
  if (strArgFix)
    argFix = true;

  // We allow absolute paths, only if it is in current sandbox.
  if (Path::absolute( mergeFile ))
    fpMergeFile = mergeFile;
  else
    fpMergeFile = Path::getCwdFromEnviron() + Path::DIR_SEPARATOR + mergeFile;

  Path::canonicalizeThis( fpMergeFile, false );

  // Ensure user supplied merge file exists.
  if (!Path::isFile( fpMergeFile ))
  {
    Interface::printError( "sbmerge: " + mergeFile + ": No such file." );
    Interface::quit( 1 );
  }

  String sbpath = getSbPath( fpMergeFile );

  // Must be in sandbox.
  if (sbpath == StringConstants::EMPTY_STRING)
  {
    Interface::printError( "sbmerge: " + mergeFile +
                           " file must be in current sandbox.");
    Interface::quit( 1 );
  }

  if (Interface::isDebug())
  {
    Interface::printDebug( "Input: " + mergeFile );
    Interface::printDebug( "Merge File: " + fpMergeFile );
  }

  String originalFile = getOriginalPath( sbpath );
  if (Interface::isDebug())
    Interface::printDebug( "sbmerge: original file = " + originalFile );

  // Ensure original file exists.
  if (!Path::isFile( originalFile ))
  {
    Interface::printError( "sbmerge: " + originalFile + ": No such file." );
    Interface::quit( 1 );
  }

  // This variable controls how far we look in the backing chain for a file
  // to merge with the given file.  We only look in the final backing build
  // when -orig is used on the command line.
  int factor = 1;
  if (origProvided)
    factor = 0;  // Look in backing build for other updated file.


  // Iterate backing chain, looking for file which we can merge with.
  for (int i = backingChain.firstIndex() + 1;
       i <= backingChain.lastIndex() - factor; i++)
  {
    tempFile = backingChain[i] + sbpath; // No DIR_SEPARATOR needed

    if (Path::isFile( tempFile ))
    {
      foundOtherMergeFile = true;

      if (Interface::isDebug())
        Interface::printDebug( "Other merge file found: " + tempFile );

      if (userRequestMerge( fpMergeFile, tempFile, originalFile ))
      {
        // Only back up merge file one time.
        if (!alreadyBackedUp)
        {
          Path::copyFile( fpMergeFile, fpMergeFile + ".bak", true, 0, true);
          alreadyBackedUp = true;
        }

        // Truncate first argument for AIX if possible.
        if (argFix && fpMergeFile.startsWith( Path::getCwdFromEnviron() ))
        {
          fpMergeFile.substringThis( Path::getCwdFromEnviron().length() +
                                     STRING_FIRST_INDEX + 1 );
        }

        // Build command string and run it.
        String cmdstr = *mergeTool + " " + merge_args + fpMergeFile + " " +
                        originalFile +  " " + tempFile;

        if ( infoOnly )
        {
          Interface::printAlways( cmdstr );
        }
        else
        {
          RunSystemCommand *running_cmd;

          if (Interface::isDebug())
            Interface::printDebug( cmdstr );

          RunSystemCommand::buildShellCmdArray( cmdstr,
                                                StringConstants::EMPTY_STRING,
                                                &newcmd );

          running_cmd = new RunSystemCommand( newcmd, true,
                                              true, false, false );
          running_cmd->start();
        }

        // If user hasn't requested -all, then get out of loop.
        if (!mergeAll)
          i = backingChain.lastIndex();
      }
    }
  }


  if (!foundOtherMergeFile)
  {
    Interface::printError( "sbmerge: No matching merge file found in backing chain." );
    Interface::quit( 1 );
  }

  return (rc);
}


/******************************************************************************
 * If in sandbox, this returns the path relative to the sandbox, else ""
 */
String SbMerge::getSbPath( const String &path )
{
  SmartCaseString smartpath( path );
  if (backingChain.length() == 0 ||
      !smartpath.startsWith( backingChain[backingChain.firstIndex()] ))
    return "";
  else
    return smartpath.substring(
                  backingChain[backingChain.firstIndex()].length() + 1);
}


/******************************************************************************
 *
 */
void SbMerge::processCommandLine()
{

  const char *statesp[] = { "-auto", "-all", "-info", 0 };
  StringArray states( statesp );
  const char *qvarsp[] = { "-sb", "-rc", "-orig", 0 };
  StringArray qvars( qvarsp );

  boolean unqvars = true;
  boolean needarguments = true; // have to have some options on commandline
  String tmpvar;

  // Initialize commandline class.
  StringArray arguments( args );
  cmdLine = new CommandLine( &states, &qvars, unqvars,
                             arguments, needarguments, *this );
  cmdLine->process( true );

  // Get all option's states to simplify programming.
  autoMerge    = cmdLine->isState( "-auto" );
  mergeAll     = cmdLine->isState( "-all"  );
  origProvided = cmdLine->isState( "-orig" );
  infoOnly     = cmdLine->isState( "-info" );


  // Get original root directory if entered on command line.
  if ((tmpvar = cmdLine->getQualifiedVariable( "-orig" )) !=
      StringConstants::EMPTY_STRING)
  {
    origEntry =  tmpvar;
    Path::normalizeThis( origEntry );
    if (Path::isDirectory( origEntry ))
      Path::canonicalizeThis( origEntry );
    else
    {
      Interface::printError( "sbmerge: -orig " + origEntry +
                             ": No such directory." );
      Interface::quit( 1 );
    }
  }


// Maybe we can use these in the future if we decide that the user does not
// currently need to be in a workon session of a sandbox.
//  if ((tmpvar = cmdLine->getQualifiedVariable( "-rc" )) !=
//      StringConstants::EMPTY_STRING)
//    rcFileName = Path::normalizeThis( tmpvar );

//  if ((tmpvar = cmdLine->getQualifiedVariable( "-sb" )) !=
//      StringConstants::EMPTY_STRING)
//    sboxName = Path::normalizeThis( tmpvar );

}


/******************************************************************************
 *
 */
boolean SbMerge::userRequestMerge( String fileA, String fileB, String origFile )
{

  String question = "Merge " + fileA + " with " + fileB + " based on " +
                    origFile + "? ([y]/n)";

  if (autoMerge)
  {
    Interface::print( question + " yes" );
    return (true);
  }

  // Ask user if user wants this particular merge.  Insist thay they
  // respond with a "y"es or "n"o.
  return (Interface::getConfirmation( question, true ));
}


/******************************************************************************
 *
 */
String SbMerge::getOriginalPath( String filename )
{

  String   response;

  //if  -orig flag is used,  then use this root directory
  //otherwise, assume backing build is original.
  if (origProvided)
  {
    response = origEntry + filename;   //Dir separator?
  }
  else
    response = backingChain[backingChain.lastIndex()] + filename; // No DIR_SEPARATOR needed

  // Make sure file exists before returning.
  if (!Path::isFile( response ))
  {
    Interface::printError( "sbmerge: original file: " + response +
                           ": No such file." );
    Interface::quit( 1 );
  }

  return (response);
}


/******************************************************************************
 *
 */
void SbMerge::separateFilesAndMergeArgs()
{
  StringArray unqualVars;
  cmdLine->getUnqualifiedVariables( &unqualVars );

  // Separate unqualified variables to those that are files to be merged
  // and those that are options to be passed to the merge tool.
  for (int count = unqualVars.firstIndex();
       count <= unqualVars.lastIndex(); count++)
  {
    if (unqualVars[count].startsWith( "-" ))
      // merge tool option
      merge_args += unqualVars[count] + StringConstants::SPACE;
    else
      // must be merge file,  what if more than 1?
      merge_files.add( unqualVars[count] );
  }

  // File argument must exist.
  if (merge_files.length() == 0)
  {
    Interface::printError( "sbmerge: Must provide file to merge.");
    Interface::quit( 1 );
  }
}


/******************************************************************************
 *
 */
void SbMerge::printUsage() const
{
  Interface::printAlways( "Usage: sbmerge [-all] [-auto] [-orig <dir>] [ODE_opts] [tool_opts] file" );
  Interface::printAlways( "" );
  Interface::printAlways( "       -all : don't stop, merge with entire backing chain" );
  Interface::printAlways( "       -auto: don't prompt before merging" );
  Interface::printAlways( "       -orig <dir>: root directory of build tree "
      "where original file is located" );

  Interface::printAlways( "" );
  Interface::printAlways( "   ODE_opts:" );
  Interface::printAlways( "       -quiet -normal -verbose -debug "
      "-usage -version -rev -info" );
  Interface::printAlways( "   tool_opts:" );
  Interface::printAlways( "       -*  : any flag not recognized by sbmerge "
      "is passed to the merge tool" );
  Interface::printAlways( "" );
  Interface::printAlways( "       file: local sandbox file of which to merge" );
}

