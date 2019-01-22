#ifndef _ODE_BIN_RESB_RESB_HPP_
#define _ODE_BIN_RESB_RESB_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/io/cmdline.hpp"
#include "lib/io/sandbox.hpp"


class Resb : public Tool
{
  public:
    Resb( const char **argv, const char **envp )
        : Tool( envp ), args( argv ), envs( envp ), cmdLine( 0 ), sb( 0 ) {};

    static int classMain( const char **argv, const char **envp );
    int        run();
    void       printUsage() const;

    ~Resb()
    {
      delete cmdLine;
      delete sb;
    };

  private:
    const char       **args;
    const char       **envs;
    CommandLine      *cmdLine;
    Sandbox          *sb;

    void    checkCommandLine();
    String  getBackingBuild();
    String  getProperBackingBuild( const String &dir );
    boolean retarget( const String &newBackingBuild );
};

#endif //_ODE_BIN_RESB_RESB_HPP_
