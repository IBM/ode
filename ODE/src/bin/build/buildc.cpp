#define _ODE_BIN_BUILD_BUILD_CPP_

#include <base/binbase.hpp>
#include "bin/build/buildc.hpp"
#include "lib/io/cmdline.hpp"
#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/runcmd.hpp"
#include "lib/portable/native/sleep.h"
#include "lib/string/strarray.hpp"
#include "lib/string/strcon.hpp"
#include "lib/string/string.hpp"
#include "lib/util/signal.hpp"

#include "lib/exceptn/sboxexc.hpp"

volatile int        Build::interrupt_cnt = 0;

/******************************************************************************
 *
 */
int Build::classMain( const char **argv, const char **envp )
{
  Build b( argv, envp );
  Signal::registerInterruptHandler( &b );
  return( b.run() );
}


/******************************************************************************
 *
 */
int Build::run()
{
  // to catch the stop sig to stop remote builds
  checkCommandLine();

  checkOperationMode();

  return 0;
}


/******************************************************************************
 *
 */
void Build::checkOperationMode()
{
  // if -rmthost flag is specified it means that a remote build has been
  // requested, else build on the local machine.
  if (!cmdLine->isState( "-rmthost" ))
  {
    if (!getSandbox())
      Interface::quit( 1 );

    separateTargsAndMkArgs();

    // get targets from command line
    targets = new Target( initialTargets, sb, cmdLine );

    if (!targets->check())  // check to see if ALL targets are OK
      Interface::quit( 1 );

    startBuildProcess();
  }
  else
    doRemoteOps();
}


/******************************************************************************
 * Does a lil checking before the actual mk command is called.
 */
void Build::startBuildProcess() const
{
  StringArray targs;
  targets->getTargets( &targs );

  StringArray actDirAndTarg;
  for (int i = ARRAY_FIRST_INDEX; i <= targs.lastIndex(); i++)
  {
    actDirAndTarg.clear();
    targs[i].split( " ", 2, &actDirAndTarg );
    if (actDirAndTarg.size() < 1)
    {
      actDirAndTarg.add( StringConstants::EMPTY_STRING );
      actDirAndTarg.add( StringConstants::EMPTY_STRING );
    }
    else if (actDirAndTarg.size() < 2)
      actDirAndTarg.add( StringConstants::EMPTY_STRING );

    if (!targets->processTarget( actDirAndTarg[ARRAY_FIRST_INDEX] ))
      continue;

    if (cmdLine->isState( "-clean" ))
    {
      if (actDirAndTarg[ARRAY_FIRST_INDEX + 1].equals( "build_all" ))
        buildTarget( actDirAndTarg[ARRAY_FIRST_INDEX], "clean_all" );
      else
        buildTarget( actDirAndTarg[ARRAY_FIRST_INDEX],
            String( "clean_" ) + actDirAndTarg[ARRAY_FIRST_INDEX + 1] );
    }

    buildTarget( actDirAndTarg[ARRAY_FIRST_INDEX],
        actDirAndTarg[ARRAY_FIRST_INDEX + 1] );
  }
}


/**
 * CD to the specified dir.
**/
void Build::buildTarget( const String &cdTo, const String &target ) const
{
  if (!changeDir( cdTo ))
    Interface::quitWithErrMsg( "build: Unable to change directories...", 1 );

  if (Interface::isDebug())
    Interface::printDebug( "About to mk target: " + target );
  StringArray mkCommand;
  constructMakeCommand( target, mkCommand );

  int exitcode = runMake( mkCommand );

  // if user specifies ignore then ignore errors and build next target
  if (exitcode != 0)
  {
    Interface::printError( String( "build: command \"" ) +
        mkCommand.join( " " ) + "\" failed with exit code " +
        String( exitcode ) );
    if (cmdLine->isState( "-ignore" ))
      Interface::printError( String( "build: Make failed for target - " ) +
          target + ". Continuing... " );
    else
      Interface::quitWithErrMsg( String( "build: Make failed for target - " ) +
          target, 1 );
  }
}


/**
 *
**/
int Build::runMake( const StringArray &arguments ) const
{
  RunSystemCommand runsyscmd( arguments, true, true, false, false );

  if (cmdLine->isState( "-info" ))
  {
    Interface::printAlways( "Would run the command:" );
    Interface::printAlways( arguments.join( StringConstants::SPACE ) );
    return 0;
  }

  if (Interface::isVerbose())
  {
    Interface::printVerbose( "Running: " );
    Interface::printVerbose( arguments.join( StringConstants::SPACE ) );
  }

  runsyscmd.run();
  if( Signal::isInterrupted() )
  {
    ODEsleep( 300 );
    runsyscmd.stopChild();
    ODEsleep( 500 );
  }
  return (runsyscmd.getExitCode());
}


/**
 *
**/
StringArray &Build::constructMakeCommand( const String &target,
    StringArray &mkCommand ) const
{
  String cmdstr( make_command_name );

  // add all mk args from build command line
  cmdstr += StringConstants::SPACE + mk_args;
  // finally, add the target
  cmdstr += target;

  RunSystemCommand::buildShellCmdArray( cmdstr, "", &mkCommand );

  return (mkCommand);
}


/******************************************************************************
 *
 */
boolean Build::changeDir( const String &cdTo ) const
{
  if (cmdLine->isState( "-info" ))
  {
    Interface::printAlways( "Would change to dir " +
        Path::userize( cdTo ) );
  }
  else
  {
    if (Interface::isVerbose())
      Interface::printVerbose( "Changing directory to " +
          Path::userize( cdTo ) );
    if (!Path::setcwd( cdTo ))
      return false;
  }

  return true;
}


/******************************************************************************
 *
 */
boolean Build::getSandbox()
{
  try
  {
    sb = new Sandbox( true, cmdLine->getRCFile(), cmdLine->getSandbox(), true );
  }
  catch (SandboxException &se)
  {
    Interface::printError( String( "build: " ) + se.getMessage() );
    return false;
  }

  if (!sb->verifyMachine())
  {
    Interface::printError( "build: Invalid machine name..." );
    Interface::print( "Valid machine names: " + sb->getMachineList() );
    return false;
  }

  return true;
}


/******************************************************************************
 *
 */
void Build::doRemoteOps()
{
  String machine;
  String host = cmdLine->getQualifiedVariable( "-rmthost", 1 );
  String newCommandLine;

  if ((machine = cmdLine->getQualifiedVariable( "-m", 1 )) ==
      StringConstants::EMPTY_STRING)
    machine = *Env::getenv( "CONTEXT" );

  if (cmdLine->isState( "-info" ))
  {
    Interface::printAlways( "Would send command via remote host: " +
                            cmdLine->getQualifiedVariable( "-rmthost", 1 ));
    return;
  }

  Interface::printAlways( "Starting remote build..." );

  StringArray buildCommand;
  String client_cmd_quote, remote_cmd_quote, user_name, remote_cmd, dce_suffix;
  String dce_suffix_quote;

  remote_cmd_quote = " \" ";

  if (StringConstants::getODEMAKE_RUSER() != StringConstants::EMPTY_STRING)
    user_name = " -l " + StringConstants::getODEMAKE_RUSER();

  if (StringConstants::getODEMAKE_RDCELOGIN() == StringConstants::EMPTY_STRING)
    dce_suffix = StringConstants::EMPTY_STRING;
  else
    dce_suffix = " -e ";

  if (!StringConstants::getODEMAKE_RDCECMD_QUOTED().isEmpty())
    dce_suffix_quote = " \\\" ";

  remote_cmd = StringConstants::getODEMAKE_RCMDPREPEND().dequote()
      + StringConstants::getODEMAKE_RDCELOGIN().dequote() + dce_suffix
      + dce_suffix_quote + getNewCommandLine() + " "
      + StringConstants::getODEMAKE_RCMDAPPEND().dequote() +
      dce_suffix_quote;

  newCommandLine = client_cmd_quote + StringConstants::getODEMAKE_RSHELL() +
      " " + host + user_name + remote_cmd_quote + remote_cmd +
      remote_cmd_quote + client_cmd_quote;

  RunSystemCommand::buildShellCmdArray( newCommandLine, "", &buildCommand );

  if (Interface::isVerbose())
  {
    Interface::printVerbose( "Remote host   : " + host );
    Interface::printVerbose( "       machine: " + machine );
    Interface::printVerbose( "       command: " + newCommandLine );
  }

  runMake( buildCommand );
}


/******************************************************************************
 *
 */
String Build::getNewCommandLine()
{
  StringArray arguments( args );
  String command;

  for (int i = arguments.firstIndex(); i <= arguments.lastIndex(); i++)
  {
    if (arguments[i].equals( "-rmthost" ))
      ++i;
    else
      command += arguments[i] + " ";
  }

  return command;
}


/******************************************************************************
 *
 */
void Build::separateTargsAndMkArgs()
{
  unsigned long eq_i, sp_i; // indices for an equals sign and a space
  StringArray unqualVars;
  cmdLine->getUnqualifiedVariables( &unqualVars );

  for (int count = unqualVars.firstIndex();
       count <= unqualVars.lastIndex(); count++)
  {
    if (unqualVars[count].startsWith( "-" )) // mk flag
#ifdef DEFAULT_SHELL_IS_VMS
      mk_args += "\"" + unqualVars[count] + "\"" + StringConstants::SPACE;
#else
      mk_args += unqualVars[count] + StringConstants::SPACE;
#endif
    else if ((eq_i = unqualVars[count].indexOf(
        StringConstants::EQUAL_SIGN )) != ELEMENT_NOTFOUND) // mk variable
    {
#ifdef DEFAULT_SHELL_IS_VMS
      mk_args += "\"" + unqualVars[count] + "\"" + StringConstants::SPACE;
#else
      // does the value contain whitespace but no quotes?  if so, quote it!
      if (((sp_i = unqualVars[count].indexOf( StringConstants::SPACE )) !=
          ELEMENT_NOTFOUND) &&
          (unqualVars[count].indexOf( StringConstants::DOUBLE_QUOTE ) ==
          ELEMENT_NOTFOUND))
      {
        if (sp_i < eq_i) // var name has a space?!  quote entire string
        {
          mk_args += StringConstants::DOUBLE_QUOTE;
          mk_args += unqualVars[count];
        }
        else // just quote the value
        {
          mk_args += unqualVars[count].substring(
              STRING_FIRST_INDEX, eq_i + 1 );
          mk_args += StringConstants::DOUBLE_QUOTE;
          mk_args += unqualVars[count].substring( eq_i + 1 );
        }
        mk_args += StringConstants::DOUBLE_QUOTE;
        mk_args += StringConstants::SPACE;
      }
      else
        mk_args += unqualVars[count] + StringConstants::SPACE;
#endif
    }
    else // it must be a target
#ifdef DEFAULT_SHELL_IS_VMS
      initialTargets.add( "\"" + unqualVars[count] + "\"" );
#else
      initialTargets.add( unqualVars[count] );
#endif
  }

  if (initialTargets.length() == 0)  // use default target
  {
    if (cmdLine->isState( "-here" ))
      initialTargets.add( "." );
    else
      initialTargets.add( "build_all" );
  }
}



/******************************************************************************
 *
 */
void Build::checkCommandLine()
{
  StringArray arguments( args );
  const char *sts[] = { "-here", "-clean", "-rmthost", "-ignore", 0 };
  StringArray states( sts );
  const char *qv[]  = { "-sb", "-rc", "-m", "-rmthost", 0 };
  StringArray qVars( qv );

  cmdLine = new CommandLine( &states, &qVars, true, arguments, false, *this );

  cmdLine->process( true );

  if (cmdLine->isState( "-rmthost" ))
  {
    if (cmdLine->getQualifiedVariable( "-rmthost" ) ==
        StringConstants::EMPTY_STRING)
    {
      Interface::printError( "build: Remote host name not specified." );
      printUsage();
      Interface::quit( 1 );
    }
  }

  const String *var = Env::getenv( "MAKE" );
  make_command_name = (var == 0) ? String( "mk" ) : *var;
}

/************************************************
  *   --- void Build::handleInterrupt ---
  *
  ************************************************/
void Build::handleInterrupt()
{
  if (interrupt_cnt == 0)
    Interface::printError( "build: Interrupt received.  Cleaning up..." );
  else if (interrupt_cnt > 2)
  {
    Interface::printError( "build: Interrupt received, again.  Giving up..." );
    Interface::quitWithErrMsg( String( "Aborting" ), 1 );
  }
  else
    Interface::printError( "build: Interrupt received, again.  Please wait..." );
  interrupt_cnt++;
}

/******************************************************************************
 *
 */
void Build::printUsage() const
{
  Interface::printAlways( "Usage: build [-here] [-clean] [-m <machine>] "
      "[-ignore] [ODE opts]" );
  Interface::printAlways( "             [sb opts] [make_opts] [remote_opts] "
      "[target...]" );
  Interface::printAlways( "       -here  : targets are directories to "
      "work from" );
  Interface::printAlways( "       -clean : old format to remove and "
      "rebuild target" );
  Interface::printAlways( "       -m <machine>: machine type to use" );
  Interface::printAlways( "       -ignore: ignore errors when building "
      "multiple targets" );
  Interface::printAlways( "       ODE opts:" );
  Interface::printAlways( "         -quiet -normal -verbose -debug -usage "
      "-version -rev -info -auto" );
  Interface::printAlways( "       sb opts:" );
  Interface::printAlways( "         -sb <sandbox>, -rc <rcfile>, ");
  Interface::printAlways( "       make opts:" );
  Interface::printAlways( "         -*  : any flag not recognized by build "
      "is passed to make" );
  Interface::printAlways( "         *=* : any equation of this form is "
      "passed to make" );
  Interface::printAlways( "       remote opts:" );
  Interface::printAlways( "         -rmthost remote_hostname" );
  Interface::printAlways( "       target...: item(s) to build" );
}
