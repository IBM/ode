using namespace std;
#define _ODE_BIN_CURRSB_CURRSB_CPP

#include <base/odebase.hpp>
#include "bin/currsb/currsbc.hpp"
#include "lib/io/path.hpp"

#include "lib/exceptn/sboxexc.hpp"


/******************************************************************************
 *
 */
int CurrentSb::classMain( const char **argv, const char **envp )
{
  CurrentSb sb( argv, envp );
  return( sb.run() );
}


/******************************************************************************
 *
 */
int CurrentSb::run()
{
  checkCommandLine();

  if (!getSandbox())
    Interface::quit( 1 );

  StringArray info;
  getInfo( &info );
  printInfo( info );

  return (0);
}


/******************************************************************************
 *
 */
boolean CurrentSb::getSandbox()
{
  try
  {
    sb = new Sandbox( false, cmdLine->getRCFile(), getSbName() );
  }
  catch ( SandboxException &se )
  {
    Interface::printError( String( "currentsb: " ) + se.getMessage() );
    return false;
  }

  return true;
}


/******************************************************************************
 *
 */
StringArray *CurrentSb::getInfo( StringArray *buffer ) const
{
  StringArray *temp = (buffer == 0) ? new StringArray() : buffer ;
  temp->clear();

  // if no info is requested this variable will be false, in which case we just
  // have to print out the sb name
  boolean info = cmdLine->isState( "-sb" ) ||
                 cmdLine->isState( "-dir" ) ||
                 cmdLine->isState( "-back" ) ||
                 cmdLine->isState( "-chain" ) ;

  String tempstr;
  String firstLine;
  if (cmdLine->isState( "-sb" ) || cmdLine->isState( "-all" ) || !info)
  {
    firstLine = sb->getSandboxName();
    Path::normalizeThis( firstLine );
  }

  if (cmdLine->isState( "-dir" ) || cmdLine->isState( "-all" ) )
    firstLine += " " + Path::normalize( sb->getSandboxBaseDir() );

  if (cmdLine->isState( "-back" ) || cmdLine->isState( "-all" ) )
  {
    const String *backdir = sb->getBackingDir();
    if (backdir != 0)
      firstLine += " " + Path::normalize( *backdir );
  }

  if (firstLine.length() != 0)
    temp->add( firstLine );

  if (cmdLine->isState( "-chain" ))
  {
    StringArray chain;

    sb->getBackingChainArray( &chain );

    for (int i = chain.firstIndex(); i <= chain.lastIndex(); i++)
      temp->add( Path::normalize( chain[i] ) );
  }

  return temp;
}


/******************************************************************************
 *
 */
String CurrentSb::getSbName() const
{
  StringArray unQual_SBName;  // Array for unqualified variables

  cmdLine->getUnqualifiedVariables( &unQual_SBName );

  // if more than one (qualified or unqualified) sandbox on the command line
  if (unQual_SBName.lastIndex() > 1)
  {
     Interface::printError( CommandLine::getProgramName() +
              ": only one sandbox name can be given.");
     printUsage();
     Interface::quit(1);
  }

  if (unQual_SBName.length() != 0)
    return unQual_SBName[unQual_SBName.firstIndex()];
  else
    return (StringConstants::EMPTY_STRING);
}


/******************************************************************************
 *
 */
void CurrentSb::checkCommandLine()
{
  const char *sts[] = { "-sb", "-dir", "-back", "-chain", "-all", 0};
  StringArray states( sts );
  const char *qv[] = { "-rc", 0 };
  StringArray qVars( qv );
  StringArray arguments( args );

  cmdLine = new CommandLine( &states, &qVars, true, arguments, false, *this );
  cmdLine->process();
}


/******************************************************************************
 *
 */
void CurrentSb::printUsage() const
{
  Interface::printAlways( "Usage: currentsb [-sb -dir -back -chain -all]" );
  Interface::printAlways( "                 [ODE_opts] [sb_opts] [sandbox]" );
  Interface::printAlways( "" );
  Interface::printAlways( "   ODE_opts:" );
  Interface::printAlways( "       -quiet -normal -verbose -debug -usage "
      "-version -rev -info -auto" );
  Interface::printAlways( "" );
  Interface::printAlways( "   sb_opts:" );
  Interface::printAlways( "       -rc <rcfile>" );
}
