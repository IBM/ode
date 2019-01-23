/**
 * Sufffix
 *
**/
using namespace std;
#define _ODE_BIN_MAKE_SUFFIX_CPP_

#include <base/binbase.hpp>
#include "bin/make/dir.hpp"
#include "bin/make/constant.hpp"
#include "bin/make/suffix.hpp"



/**
 * Zero out the Suffix path.
**/
void Suffix::clearPath()
{
  if (path.getPath() != 0)
    path.setPath( (StringArray *)0 );
}

/**
 * Set .PATH variable into SetVar vars by path array in Dir path
**/
void Suffix::setDotPathVars( SetVars &vars )
{
  if (path.getPath() !=0)
    vars.set( Constants::DOT_PATH + suff, path.getPath()->join(
        StringConstants::SPACE ), true );
  else
    vars.set( Constants::DOT_PATH + suff, StringConstants::EMPTY_STRING, true );
}

/**
 *  Append path to current Suffix path( not set, we don't change function
 *  name so that it is compatible with old version)
**/
void Suffix::setPath( const Dir &path )
{
  this->path.append( path );
}

/**
 *  Append path to current Suffix path( not set, we don't change function
 *  name so that it is compatible with old version)
**/
void Suffix::setPath( const StringArray *path )
{
  if (path == 0) return;
  this->path.append( *path );
}

/**
 *  Clear completely the Suffix corepath
**/
void Suffix::clearCorePath()
{
  if (corepath.getPath() != 0)
    corepath.setPath( (StringArray *)0 );
}

/**
 *  Append path to current Suffix corepath
**/
void Suffix::setCorePath( const Dir &path )
{
  this->corepath.append( path );
}

/**
 *  Append path to current Suffix corepath
**/
void Suffix::setCorePath( const StringArray *path )
{
  if (path == 0) return;
  this->corepath.append( *path );
}
