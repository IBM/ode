using namespace std;
using namespace std;
#include <stdlib.h> // for the atoi function

#define _ODE_BIN_WORKON_WORKON_CPP_
#include "bin/workon/workonc.hpp"
#include "lib/io/path.hpp"
#include "lib/io/sandbox.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/runcmd.hpp"
#include "lib/portable/env.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/string.hpp"
#include "lib/exceptn/sboxexc.hpp"





/******************************************************************************
 *
 */
int Workon::classMain( const char **argv, const char **envp )
{
  Workon w( argv, envp );
  return( w.run() );
}


/******************************************************************************
 *
 */
int Workon::run()
{
  checkCommandLine();

  if (!getSandbox())
    Interface::quit(1);

  setEnvVariables();
  openNewShell();

  return 0;
}


/******************************************************************************
 *
 */
boolean Workon::getSandbox()
{
  try
  {
    sb = new Sandbox( false, cmdLine->getRCFile(), getSbName(), true );

    if (!sb->verifyMachine())
    {
      const String *context = Env::getenv( SandboxConstants::CONTEXT_VAR );
      throw SandboxException( String( "Invalid machine (" ) +
                              ((context == 0) ? String() : *context) + ")" );
    }
  }
  catch (SandboxException &se)
  {
    Interface::printError( String( "workon: " ) + se.getMessage() );
    return false;
  }

  return true;
}


/******************************************************************************
 *
 */
String Workon::getSbName() const
{
  StringArray sboxName;    // Array for qualified variables
  StringArray sbName;      // Array for unqualified variables

  cmdLine->getQualifiedVariables( "-sb", &sboxName );
  cmdLine->getUnqualifiedVariables( &sbName );

  // if more than one (qualified or unqualified) sandbox on the command line
  if( (sboxName.lastIndex() > 1) ||
      (sbName.lastIndex() > 1)  )
  {
     Interface::printError( CommandLine::getProgramName() +
              ": one sandbox name can be given.");
     printUsage();
     Interface::quit(1);
  }

  // if a (qualified and unqualified) sandbox specified on the command line
  if ( (sboxName.length() != 0) && (sbName.length() != 0) )
  {
     Interface::printError( CommandLine::getProgramName() +
              ": one sandbox name can be given.");
     printUsage();
     Interface::quit(1);
  }
  else if (sboxName.length() != 0)
      return sboxName[sboxName.firstIndex()];
  else if (sbName.length() != 0)
     return sbName[sbName.firstIndex()];

  return (StringConstants::EMPTY_STRING); // to appease the compiler
}


/******************************************************************************
 *
 */
void Workon::setEnvVariables()
{
  // check if WORKON is set. If it is increment it by one. Else set it to 1.
  const String *workon = Env::getenv( SandboxConstants::WORKON_VAR );

  if (workon == 0)
    Env::setenv( SandboxConstants::WORKON_VAR, "1", true );
  else
  {
    int value = atoi( workon->toCharPtr() );

    if (value == 0)
      Interface::printWarning( String( "workon: " ) +
        SandboxConstants::WORKON_VAR +
        " env variable was set to an invalid value. Resetting it to 1.");

    Env::setenv( SandboxConstants::WORKON_VAR, String( ++value ), true );
  }

}


/******************************************************************************
 *
 */
void Workon::openNewShell()
{
  String fs = Path::DIR_SEPARATOR;

  String can_dir = String( sb->getSandboxBase() + fs +
      SandboxConstants::getSRCNAME() );
  Path::canonicalizeThis( can_dir );
  if (cmdLine->isState( "-info" ))
    Interface::printAlways( "Would change to directory " +
                            can_dir );
  else
  {
    Interface::print( "Changing directory to: " + can_dir );
    if (!Path::setcwd( can_dir ))
      Interface::quitWithErrMsg( "workon: Unable to change to directory: " +
                                 can_dir, 1 );
  }

  int rc = runCommand();
#ifdef OS2
// KLUDGE!  OS/2's CMD.EXE may return a non-zero exit code when
// you simply start the shell and exit.  So, we need to check
// the EXACT return code that occurs when SHELL contains a command that
// can't be found or executed.  We don't need to be this specific for
// other platforms.
  if (rc == -1)
#else
  if (rc != 0)
#endif
    Interface::quitWithErrMsg( String( "workon: shell returned exit code " ) +
        String( rc ), 1 );
}


/**
 *
**/
int Workon::runCommand()
{
  if (cmdLine->isState( "-info" ))
  {
    Interface::printAlways( "Would spawn new shell" );
    return (0);
  }

  const String *shell = Env::getenv( StringConstants::SHELL_VAR );
  StringArray cmdstr;
  StringArray c_cmds, k_cmds;    // Arrays for commands
  boolean prepend_shell;
  boolean stayInShell = false;
  cmdLine->getQualifiedVariables( "-c", &c_cmds );
  cmdLine->getQualifiedVariables( "-k", &k_cmds );
  if (c_cmds.lastIndex() > 0 || k_cmds.lastIndex() > 0 )
  {
    // if more than one -c or -k command on the command line
    if( (c_cmds.lastIndex() + k_cmds.lastIndex()) > 1)
    {
       Interface::printError( CommandLine::getProgramName() +
                ": only one -c <command> or -k <command> can be given.");
       printUsage();
       Interface::quit(1);
    }
    prepend_shell = true;
    if (c_cmds.lastIndex() > 0)
      cmdstr.add( c_cmds[c_cmds.firstIndex()] );
    else
    {
      cmdstr.add( k_cmds[k_cmds.firstIndex()] );
      stayInShell = true;
    }
    if (cmdLine->isState( "-debug" ))
      Interface::printAlways( "Running command (" +
                              cmdstr[cmdstr.firstIndex()] + ")" );
  }
  else
  {
    prepend_shell = false;
    cmdstr.add( (shell == 0) ? String() : *shell );
    Interface::print( "Spawning new shell (" + cmdstr[cmdstr.firstIndex()] +
                      ")..." );
  }
  RunSystemCommand cmd( cmdstr, true, true, false, prepend_shell, stayInShell );

#if defined(IGNORE_SIGS_BEFORE_FORK) && !defined(NO_IGNORE_SIGS_AT_FORK)
  Signal::ignoreInterrupts();
#endif
  cmd.run();
#if !defined(IGNORE_SIGS_BEFORE_FORK) && !defined(NO_IGNORE_SIGS_AT_FORK)
  Signal::ignoreInterrupts();
#endif
  cmd.waitFor();
  Signal::restoreInterrupts();
  return (cmd.getExitCode());
}


/******************************************************************************
 *
 */
void Workon::checkCommandLine()
{
  StringArray arguments( args );

  const char  *sts[] = { 0 };
  StringArray states( sts );

  const char  *qv[] = {"-sb", "-rc", "-m", "-c", "-k", 0 };
  StringArray qVars( qv );

  cmdLine = new CommandLine(&states, &qVars, true, arguments, false,
                            0, 0, *this);

  cmdLine->process();
}


/******************************************************************************
 *
 */
void Workon::printUsage() const
{
  Interface::printAlways( "Usage: workon [-m <machine>] [command_opts] [ODE_opts] [sb_opts]");
  Interface::printAlways( "" );
  Interface::printAlways( "       -m <machine>: machine to build for" );
  Interface::printAlways( "" );
  Interface::printAlways( "   command_opts:" );
  Interface::printAlways( "       -c <command>: command to run in workon "
                          "environment, then exit" );
  Interface::printAlways( "       -k <command>: command to run in workon "
                          "environment, then continue" );
  Interface::printAlways( "                     in workon environment" );
  Interface::printAlways( "" );
  Interface::printAlways( "   ODE_opts:" );
  Interface::printAlways( "       -quiet -normal -verbose -debug "
      "-usage -version -rev -info -auto" );
  Interface::printAlways( "" );
  Interface::printAlways( "   sb_opts:" );
  Interface::printAlways( "       <sandbox>, -sb <sandbox>, -rc <rcfile>" );
  Interface::printAlways( "" );
  Interface::printAlways( "       workon [<sandbox>] | -sb <sandbox>" );
}
