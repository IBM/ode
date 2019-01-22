#ifndef _ODE_BIN_WORKON_WORKON_HPP_
#define _ODE_BIN_WORKON_WORKON_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/io/cmdline.hpp"
#include "lib/io/sandbox.hpp"


class Workon : public Tool
{
  public:
    Workon( const char **argv, const char **envp )
        : Tool( envp ), args( argv ), envs( envp ) {};

    static int classMain( const char **argv, const char **envp );
    int        run();
    void       printUsage() const;

    ~Workon()
    {
      delete cmdLine;
      delete sb;
    };

  private:
    const char       **args;
    const char       **envs;
    CommandLine      *cmdLine;
    Sandbox          *sb;

    void checkCommandLine();
    boolean getSandbox();
    String  getSbName() const;
    void setEnvVariables();
    void openNewShell();
    int runCommand();
};

#endif //_ODE_BIN_WORKON_WORKON_HPP_

