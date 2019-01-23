using namespace std;
/**
 * SbInfo
**/

#define _ODE_BIN_SBINFO_SBINFOC_CPP_
#include "bin/sbinfo/sbinfoc.hpp"
#include "bin/sbinfo/sbinfoex.hpp"
#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/io/sandbox.hpp"
#include "lib/exceptn/sboxexc.hpp"
#include "lib/portable/platcon.hpp"

int SbInfo::classMain( const char **argv, const char **envp )
{
  SbInfo sbinfo( argv, envp );
  return (sbinfo.run());
}

int SbInfo::run()
{
  try
  {
    parseCmdLine();
    readPrintInfo();
  }
  catch (SbInfoException &e)
  {
    Interface::printError( e.getMessage() );
    Interface::quit( 1 );
  }
  return (0);
}

void SbInfo::parseCmdLine()
//    throws SbInfoException
{
  const char *cpstates[] = { 0 };
  const char *cpqvars[] = { "-sb", "-rc", 0 };
  StringArray states( cpstates );
  StringArray qvars( cpqvars );
  String tmpvar;

  // initialize commandline class
  StringArray arguments( args );
  cmdline = new CommandLine( &states, &qvars, true, arguments, false, *this );
  cmdline->process();

  // get all option's value
  if ((tmpvar = cmdline->getQualifiedVariable( "-sb" )) !=
      StringConstants::EMPTY_STRING)
  {
    sboxname = tmpvar;
    Path::normalizeThis( sboxname );
  }

  if ((tmpvar = cmdline->getQualifiedVariable( "-rc" )) !=
      StringConstants::EMPTY_STRING)
  {
    rcfilename = tmpvar;
    Path::normalizeThis( rcfilename );
  }

  cmdline->getUnqualifiedVariables( &entry_variables );
}

/**
*
**/
void SbInfo::readPrintInfo()
//    throws SbInfoException
{
  int i; // used for several for loops

  try
  {
    Sandbox sbox( false, rcfilename, sboxname );
    const SetVars &setenv_vars = sbox.getEnvs();
    const SetVars &directive_vars = sbox.getBuildconfLocals();
    if (entry_variables.length() > 0)
    {
      const String *tmpvar;

      for (i = entry_variables.firstIndex();
          i <= entry_variables.lastIndex(); i++)
      {
        if ((tmpvar = setenv_vars.get( entry_variables[i] )) != 0)
          Interface::printAlways( Path::normalize( *tmpvar ) );
        else if ((tmpvar = directive_vars.get( entry_variables[i] )) != 0)
          Interface::printAlways( Path::normalize( *tmpvar ) );
      }
    }
    else
    {
      StringArray list;

      Interface::printAlways( "" );
      Interface::printAlways( "The following environment variables were set:" );
      setenv_vars.get( !PlatformConstants::onCaseSensitiveOS(), &list );
      for (i = list.firstIndex(); i <= list.lastIndex(); i++)
        Interface::printAlways( Path::normalize( list[i] ) );

      Interface::printAlways( "" );
      Interface::printAlways(
          "The following directives are defined in the rc file:" );
      list.clear();
      directive_vars.get( !PlatformConstants::onCaseSensitiveOS(), &list );
      for (i = list.firstIndex(); i <= list.lastIndex(); i++)
        Interface::printAlways( Path::normalize( list[i] ) );
    }
  }
  catch (SandboxException &e)
  {
    throw SbInfoException( e.getMessage() );
  }
}

String SbInfo::toString() const
{
  return (PROGRAM_NAME);
}

/**
 *
 */
void SbInfo::printUsage() const
{
  Interface::printAlways( "Usage: sbinfo [ODE_opts] [sb_opts] [variables]" );
  Interface::printAlways( "" );
  Interface::printAlways( "   ODE_opts:" );
  Interface::printAlways( "       -quiet -normal -verbose -debug "
      "-usage -version -rev -info -auto" );
  Interface::printAlways( "" );
  Interface::printAlways( "   sb_opts:" );
  Interface::printAlways( "       -sb <sandbox_name> -rc <rcfile>" );
  Interface::printAlways( "" );
  Interface::printAlways( "       variables: variable names to show" );
}
