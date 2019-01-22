#ifndef _ODE_BIN_MKPATH_MKPATH_HPP_
#define _ODE_BIN_MKPATH_MKPATH_HPP_

#include <base/odebase.hpp>
#include "lib/io/cmdline.hpp"
#include "lib/string/strarray.hpp"


class Mkpath : public Tool
{
  public:
    Mkpath( const char **argv, const char **envp )
          : Tool( envp ), args( argv ), envs( envp ), cmdLine( 0 ) {};

    static int classMain( const char **argv, const char **envp );
    int        run();
    void       printUsage() const;

    ~Mkpath()
    {
      delete cmdLine;
    };

  private:
    const char       **args;
    const char       **envs;
    CommandLine      *cmdLine;

    void checkCommandLine();
    void createPaths( const StringArray &path );
};

#endif //_ODE_BIN_MKPATH_MKPATH_HPP_
