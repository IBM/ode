#define _ODE_BIN_BUILD_TARGET_CPP_


#include <base/binbase.hpp>
#include "bin/build/target.hpp"
#include "lib/io/ui.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/smartstr.hpp"


/******************************************************************************
 *
 */
boolean Target::check()
{
  String dir;

  for (int i = targets->firstIndex(); i <= targets->lastIndex(); i++)
  {
    // if not in sandbox and target is not an absolute path - quit
    if (!inSandbox() && !((*targets)[i].startsWith("/") ||
                          (*targets)[i].startsWith("\\")))
    {
      Interface::printError( "build: Not in sandbox and target <" +
                             (*targets)[i] + "> is not absolute." );
      return false;
    }
    else            // if it is an OK target
    {
      // if absolute
      if ((*targets)[i].startsWith("/") || (*targets)[i].startsWith("\\"))
        dir = sb->getSandboxBase() + Path::DIR_SEPARATOR +
              SandboxConstants::getSRCNAME() + Path::DIR_SEPARATOR +
              (*targets)[i];

      else                                    // else get path after /src
        dir = Path::getcwd() + Path::DIR_SEPARATOR + (*targets)[i];

      if (checkBackingChain( dir ))  // check the whole path
      {
        targets->elementAtPosition( i ) = dir + " " + getMkTarget( dir );
      }
      else if (!cmd_line->isState( "-here" ) &&
          checkBackingChain( getDir( dir ) ))
      {
        targets->elementAtPosition( i ) = getDir( dir ) + " " +
            getMkTarget( dir );
      }
      else
      {
        Interface::printError( "build: Invalid target: " + (*targets)[i] );
        return false;
      }
    }
  }

  return true;
}


/******************************************************************************
 *
 */
boolean Target::processTarget( const String &actDir ) const
{
  if (!Path::exists( actDir )) // if it doesn't exist in Sandbox
  {
    String userize_actDir = actDir;
    Path::userizeThis( userize_actDir );
    if (Interface::getConfirmation( "Dir <" + userize_actDir +
                                    "> does not exist. Create? {y/[n]}",
                                    false ))
    {
      if (cmd_line->isState( "-info" ))
      {
        Interface::printAlways( "Would create dir " +
            userize_actDir );
      }
      else
      {
        if (!Path::createPath( actDir ))  // create dirs
        {
          Interface::printError( "build: Unable to create the directory..." );
          return false;
        }
      }
    }
    else
      return false;
  }

  return true;
}


/******************************************************************************
 * Check if target is present in backing chain.
 */
boolean Target::checkBackingChain( const String &dir ) const
{
  StringArray backingChain;
  sb->getBackingChainArray( &backingChain );

  if (Interface::isDebug())
  {
    Interface::printDebug( "Checking dir < " + dir + "> in backing chain." );
    Interface::printDebug( "Backing chain: " +
        backingChain.join( " " ) );
    Interface::printDebug( "Is < " + dir + " > a dir?: " +
        (Path::isDirectory( dir ) ? "yes" : "no") );
  }
  if (!Path::isDirectory( dir ) ||
      Path::findFileInChain( getSrcPath( dir ), backingChain ) ==
      StringConstants::EMPTY_STRING)
    return false;

  return true;
}


/**
 * Get path after the sb name (returns an empty
 * string if dir isn't a subdir of sb base).
**/
String Target::getSrcPath( const String &dir ) const
{
  String result;
  String sbSrc = sb->getSandboxBase();
  Path::fullyCanonicalize( sbSrc );
  SmartCaseString dir_canon( dir );
  Path::fullyCanonicalize( dir_canon );
  if (sbSrc.length() > 0 && dir_canon.length() > 0)
  {
    // ensure dir ends on a directory boundary
    sbSrc += Path::DIR_SEPARATOR;
    dir_canon += Path::DIR_SEPARATOR;

    if (dir_canon.startsWith( sbSrc ))
      result = dir_canon.substring( sbSrc.lastIndex() + 1,
          dir_canon.lastIndex() );
  }

  return (result);
}


/**
 * Being in a Sandbox means that you have to be in a src subtree.
**/
boolean Target::inSandbox() const
{
  String cwd( Path::getcwd() );
  cwd += Path::DIR_SEPARATOR;
  String sb_src( sb->getSandboxBase() );
  sb_src += Path::DIR_SEPARATOR;
  sb_src += SandboxConstants::getSRCNAME();
  sb_src += Path::DIR_SEPARATOR;

  if (Path::isPrefix( Path::getcwd(), sb_src ))
    return (true);

  return (false);
}
