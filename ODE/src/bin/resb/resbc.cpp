using namespace std;
#define _ODE_BIN_RESB_RESB_CPP_

#include "bin/resb/resbc.hpp"
#include "lib/io/path.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/variable.hpp"
#include "lib/exceptn/sboxexc.hpp"
#include "lib/exceptn/parseexc.hpp"


/******************************************************************************
 *
 */
int Resb::classMain( const char **argv, const char **envp )
{
  Resb rb( argv, envp );
  return( rb.run() );
}


/******************************************************************************
 *
 */
int Resb::run()
{
  checkCommandLine();

  String rcfile = cmdLine->getQualifiedVariable("-rc", 1);
  String sbName = cmdLine->getQualifiedVariable("-sb", 1);

  try
  {
    sb = new Sandbox( true, rcfile, sbName, true, false, true, true );
  }
  catch (SandboxException &se)
  {
    Interface::quitWithErrMsg( String( "resb: " ) + se.getMessage(), 1 );
  }

  if (!sb->isBacked())
  {
    if (!Interface::getConfirmation( "About to retarget backing build '" +
                                  sb->getSandboxName() +
                                  "', do you want to proceed?([y]|n)", true ))
    {
      Interface::printWarning( "'" + sb->getSandboxName() +
                               "' was not retargetted." );
      return 0;
    }
  }

  if (!retarget( getBackingBuild() ))
    Interface::quit( 1 );
  else if (Env::getenv( SandboxConstants::WORKON_VAR ) != 0)
    Interface::printWarning( "You may need to exit and re-enter workon to "
        "acquire changes." );

  return 0;
}


/******************************************************************************
 *
 */
void Resb::checkCommandLine()
{
  StringArray arguments( args );
  const char *qv[] = { "-sb", "-rc", 0 };
  StringArray qvars( qv );

  cmdLine = new CommandLine( 0, &qvars, true, arguments, false, *this );
  cmdLine->process();
}


/******************************************************************************
 *
 */
String Resb::getBackingBuild()
{
  StringArray newBackingBuild( cmdLine->getUnqualifiedVariables() );
  StringArray buildList;
  String dir;

  if (Interface::isVerbose())
    Interface::printVerbose("About to check new backing build...");

  if ((sb->getBuildList() != 0) && sb->getBuildList()->exists())
    sb->getBuildList()->getBuildList( &buildList );

  if (newBackingBuild.length() > 0) // if backing build specified on cmdline
    dir = newBackingBuild[newBackingBuild.firstIndex()];

  while ((dir = getProperBackingBuild( dir )) == StringConstants::EMPTY_STRING)
  {
    if (buildList.length() == 0) // if no build list found
    {
      dir = Interface::getResponse( "Enter full path to backing build: ",
          true ).trim();

      // if the auto option is used, dir will be null.
      if (dir == StringConstants::EMPTY_STRING)
        Interface::quitWithErrMsg( "resb: Cannot use the <-auto> "
            "option without a valid backing build in build list.", 1 );
    }
    else
    {
      Interface::printArrayWithDefault(buildList, " : ", 1);
      dir = Interface::getResponse( String( "Select one of the above " ) +
          "builds or enter full path to backing build: ", false ).trim();
      if (dir == StringConstants::EMPTY_STRING)
        dir = buildList[buildList.firstIndex()];
    }
  }

  return (dir);
}


/**
 * If the dir specified is absolute, exists, and is a directory,
 * then just return it.  Otherwise, check to see if the dir
 * is one of the build list names...if so, return its path.
 *
**/
String Resb::getProperBackingBuild( const String &dir )
{

  String backDir, result;
  String dir_test;

  try
  {
    dir_test = Variable::envVarEval( dir );
  }
  catch (ParseException &e)
  {
    return (StringConstants::EMPTY_STRING);
  }

  File fdir( dir_test, true );

  if (Path::absolute( dir_test ) && fdir.doesExist() && fdir.isDir())
  {
    result = dir;        // unevaluated version
    backDir = dir_test;  // evaluated version
  }
  else if (sb->getBuildList() != 0 && sb->getBuildList()->exists() &&
      (backDir = 
       Variable::envVarEval( result = 
                             sb->getBuildList()->getBuildDir( dir_test ) )) !=
      StringConstants::EMPTY_STRING)
  {
    result += Path::DIR_SEPARATOR;  // unevaluated version
    result += dir;
    backDir += Path::DIR_SEPARATOR; // evaluated version
    backDir += dir_test;
  }
  else
  {
    if ( dir.length() == 0 )
      Interface::printWarning( "New backing build is not specified." );
    else
      Interface::printWarning( "New backing build '" + dir +
          "' is relative, doesn't exist, or isn't a directory." );
    return (StringConstants::EMPTY_STRING);
  }

  if (!sb->isNonRecursiveBackingDir( backDir ))
  {
    Interface::printWarning( "Using '" + backDir +
                             "' would result in a recursive chain." );
    result = StringConstants::EMPTY_STRING;
  }

  return (Path::unixizeThis( result ));
}


/******************************************************************************
 *
 */
boolean Resb::retarget( const String &newBackingBuild )
{
  if (cmdLine->isState( "-info" ))
  {
    Interface::printAlways( String( "Would have retargetted '" ) +
                            sb->getSandboxName() + "' to be backed by '" +
                            newBackingBuild + "'" );
    return true;
  }
  else
  {
    Interface::printAlways( String( "Retargetting '" ) +
                            sb->getSandboxName() + "' to be backed by '" +
                            newBackingBuild + "'" );
  }

  if (Interface::isVerbose())
    Interface::printVerbose("About to reset the backing build...");

  if (newBackingBuild == StringConstants::EMPTY_STRING)
  {
    Interface::printError("resb: Proper backing build not entered/found.");
    return false;
  }
  else
  {
    try
    {
      if (SbconfReader::checkInChain( newBackingBuild, sb->getSandboxBase() ))
      {
        Interface::printError( "resb: retargeting to " + newBackingBuild +
                  " would introduce an infinite cycle in the backing chain." );
        return false;
      }
    }
    catch (SandboxException &se)
    {
      Interface::printError( String( "resb: " ) + se.getMessage() );
      return false;
    }
    if (!sb->setBackingDir( newBackingBuild ))
    {
      Interface::printError( "resb: Unable to set backing dir..." );
      return false;
    }
  }

  return true;
}


/******************************************************************************
 *
 */
void Resb::printUsage() const
{
  Interface::printAlways( "Usage: resb [ODE_opts] [sb_opts] "
      "path" );
  Interface::printAlways( StringConstants::EMPTY_STRING );
  Interface::printAlways( "   ODE_opts:" );
  Interface::printAlways( "       -quiet -normal -verbose -debug "
      "-usage -version -rev -info -auto" );
  Interface::printAlways( StringConstants::EMPTY_STRING );
  Interface::printAlways( "   sb_opts:" );
  Interface::printAlways( "       -sb <sandbox>, -rc <rcfile>" );
  Interface::printAlways( StringConstants::EMPTY_STRING );
  Interface::printAlways( "       path: the full path to the new "
      "backing build" );
}



