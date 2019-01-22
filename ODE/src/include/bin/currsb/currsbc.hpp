#ifndef _ODE_BIN_CURRSB_CURRSB_HPP
#define _ODE_BIN_CURRSB_CURRSB_HPP


#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/string/strarray.hpp"
#include "lib/io/cmdline.hpp"
#include "lib/io/sandbox.hpp"
#include "lib/io/ui.hpp"

class CurrentSb : public Tool
{
  public:
    CurrentSb( const char **argv, const char **envp )
             : Tool( envp ), args( argv ), cmdLine( 0 ), sb( 0 ) {};

    static int classMain( const char **argv, const char **envp );
    int        run();
    void       printUsage() const;

    ~CurrentSb()
    {
      delete cmdLine;
      delete sb;
    };


  private:
    const char  **args;
    CommandLine *cmdLine;
    Sandbox     *sb;

    void        checkCommandLine();
    boolean     getSandbox();
    String      getSbName() const;
    StringArray *getInfo( StringArray *buffer = 0 ) const;
    inline void printInfo( const StringArray &info ) const;
};


/******************************************************************************
 *
 */
void CurrentSb::printInfo( const StringArray &info ) const
{
  for (int i = info.firstIndex(); i <= info.lastIndex(); i++)
    Interface::printAlways( info[i] );
}


#endif //_ODE_BIN_CURRENTSB_CURRENTSB_HPP
