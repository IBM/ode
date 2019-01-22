#define _ODE_BIN_MKBB_MKBB_CPP_

#include "bin/mkbb/mkbbc.hpp"
#include "lib/io/path.hpp"
#include "lib/portable/env.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/string.hpp"


/******************************************************************************
 *
 */
int Mkbb::classMain( const char **argv, const char **envp )
{
  Mkbb bb( argv, envp );
  return( bb.run() );
}


/******************************************************************************
 *
 */
int Mkbb::run()
{
  checkCommandLine();

  // Here we set the BUILDLIST environment variable so that Sandbox uses this
  // file instead of the default backing build's.
  String build_list;
  if ((build_list = cmdLine->getQualifiedVariable( "-blist", 1 )) !=
      StringConstants::EMPTY_STRING)
    Env::setenv( SandboxConstants::BUILDLIST_PATH_VAR, build_list, true );
  Mksb::checkOperationMode( true );

  return (0);
}


/******************************************************************************
 *
 */
void Mkbb::checkCommandLine()
{
  StringArray           arguments( args );
  const char            *sts[] = { "-def", "-list", "-upgrade", "-undo",
                                   "-nobld", 0 };
  StringArray           states( sts );
  const char            *qv[]  = { "-dir", "-m", "-blist", "-rc", "-sb", 0 };
  StringArray           qVars( qv );
  const char            *ma1[] = { "-list", "-undo", "-upgrade", 0 };
  StringArray           mutexArray1( ma1 );
  Vector< StringArray > mutex;
  mutex.addElement( mutexArray1 );

  cmdLine = new CommandLine( &states, &qVars, true, arguments, true,
                             &mutex, 0, *this );
  cmdLine->process();
}


/******************************************************************************
 *
 */
void Mkbb::printUsage() const
{
  Interface::printAlways( "Usage: mkbb [-dir <sandbox_dir>] [-m <machines>]" );
  Interface::printAlways( "            [-def] [-nobld] [-blist <build_list>]" );
  Interface::printAlways( "            [sb_opts] [ODE_opts] <sandbox>" );
  Interface::printAlways( "       -dir <sandbox_dir>: directory to make sandbox in" );
  Interface::printAlways( "       -m <machines>: colon-separated list of machines to set up" );
  Interface::printAlways( "       -def: make new sandbox the default" );
  Interface::printAlways( "       -nobld: sandbox will not use build environment" );
  Interface::printAlways( "       -blist <build_list>: use the specified build_list file" );
  Interface::printAlways( "   sb_opts:" );
  Interface::printAlways( "       -rc <user rc>, -sb <sandbox>" );
  Interface::printAlways( "   ODE_opts:" );
  Interface::printAlways( "       -auto -info -quiet -normal -verbose "
      "-debug -usage -version -rev" );
  Interface::printAlways( "   sandbox: name of sandbox to create" );
  Interface::printAlways( "" );
  Interface::printAlways( "       mkbb -list" );
  Interface::printAlways( "       mkbb -undo [<sandbox> | -sb <sandbox>]" );
  Interface::printAlways( "       mkbb -upgrade <sandbox>" );
}
