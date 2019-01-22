#define _ODE_BIN_MKPATH_MKPATH_CPP_

#include <base/binbase.hpp>
#include "bin/mkpath/mkpathc.hpp"
#include "lib/io/path.hpp"
#include "lib/string/string.hpp"

/******************************************************************************
 *
 */
int Mkpath::classMain( const char **argv, const char **envp )
{
  Mkpath mp( argv, envp );
  return( mp.run() );
}


/******************************************************************************
 *
 */
int Mkpath::run()
{
  checkCommandLine();

  StringArray paths;
  cmdLine->getUnqualifiedVariables( &paths );

  if (paths.length() == 0) // if no paths entered
  {
    printUsage();
    Interface::quit( 1 );
  }

  createPaths( paths );

  return 0;
}


/******************************************************************************
 *
 */
void Mkpath::checkCommandLine()
{
  StringArray arguments( args );

  cmdLine = new CommandLine( 0, 0, true, arguments, true, *this );
  cmdLine->process();
}


/******************************************************************************
 *
 */
void  Mkpath::createPaths( const StringArray &path )
{
  for (int index = path.firstIndex(); index <= path.lastIndex(); index++)
  {
    String pathStrng = Path::filePath( path[index] );

    if (pathStrng == StringConstants::EMPTY_STRING ||
        pathStrng.equals( "." )) // if invalid path
    {
      Interface::printError( "mkpath: Invalid path entered - " + path[index] );
      continue;
    }

    if (Path::exists( pathStrng )) // if path exists
    {
      Interface::print( "Path already exists - " + pathStrng );
      continue;
    }
    else //else create it
    {
      if (cmdLine->isState( "-info" ))
        Interface::print( "Would create path - " + pathStrng );
      else
      {
        Interface::print( "Creating path - " + pathStrng );
        if (!Path::createPath( pathStrng ))
          Interface::quitWithErrMsg( "mkpath: Unable to create path - " +
            pathStrng, 1 );
      }
    }
  }
}


/******************************************************************************
 *
 */
void Mkpath::printUsage() const
{
  Interface::printAlways( "Usage: mkpath [ODE options] path..." );
  Interface::printAlways( "" );
  Interface::printAlways( "   ODE options:" );
  Interface::printAlways( "       -quiet -normal -verbose -debug -usage "
      "-version -rev -info -auto" );
  Interface::printAlways( "" );
  Interface::printAlways( "       path: path to create" );
}
