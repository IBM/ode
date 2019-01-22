#ifndef _ODE_BIN_BUILD_TARGET_HPP_
#define _ODE_BIN_BUILD_TARGET_HPP_


#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/io/path.hpp"
#include "lib/io/sandbox.hpp"
#include "lib/io/cmdline.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/string.hpp"


class Target
{
  public:

    Target( const StringArray &targs, const Sandbox *sb,
        CommandLine *cmd_line ) :
        sb( sb ), cmd_line( cmd_line )
    {
      targets = new StringArray( targs );
    };

    ~Target()
    {
      delete targets;
    };

    boolean check();
    boolean processTarget( const String &actDir ) const;
    inline StringArray *getTargets( StringArray *buffer = 0 ) const;


  private:

    StringArray   *targets;
    const Sandbox *sb;
    CommandLine   *cmd_line;

    boolean        checkBackingChain( const String &dir ) const;
    inline String  getMkTarget( const String &dir ) const;
    inline String  getDir( const String &dir ) const;
    String         getSrcPath( const String &dir ) const;
    boolean        inSandbox() const;
};


/******************************************************************************
 *
 */
StringArray *Target::getTargets( StringArray *buffer ) const
{
  StringArray *temp = (buffer == 0) ? new StringArray() : buffer;
  *temp = *targets;
  return temp;
}


/******************************************************************************
 * Returns the penultimate dir
 * Ex: For input = "a/b/c/d" the output is "a/b/c"
 */
String Target::getDir( const String &dir ) const
{
  return (dir.substring( dir.firstIndex(),
                         Path::unixize( dir ).lastIndexOf( "/" ) ) );
}


/******************************************************************************
 *
 */
String Target::getMkTarget( const String &dir ) const
{
  if (cmd_line->isState( "-here" ))
    return( "build_all" );
  else
    return (dir.substring( Path::unixize( dir ).lastIndexOf( "/" ) + 1 ));
}


#endif //_ODE_BIN_BUILD_TARGET_HPP_
