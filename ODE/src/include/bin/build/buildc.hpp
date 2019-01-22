#ifndef _ODE_BIN_BUILD_BUILD_HPP_
#define _ODE_BIN_BUILD_BUILD_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "bin/build/target.hpp"
#include "lib/io/cmdline.hpp"
#include "lib/portable/native/proc.h"


class Build : public Tool
{
  public:

    Build( const char **argv, const char **envp ) :
        Tool( envp ), args( argv ), envs( envp ), cmdLine( 0 ), targets( 0 ),
        sb( 0 ) {};

    static int classMain( const char **argv, const char **envp );
    int        run();
    void       printUsage() const;

    ~Build()
    {
      delete cmdLine;
      delete targets;
      delete sb;
    };

    virtual void handleInterrupt(); // Handle ctrl-c, ctrl-Break interrupt

  private:

    const char       **args;
    const char       **envs;
    CommandLine      *cmdLine;
    Target           *targets;
    Sandbox          *sb;
    StringArray      initialTargets;
    String mk_args;
    String make_command_name;
    static volatile int interrupt_cnt;

    void         checkCommandLine();
    void         checkOperationMode();
    boolean      getSandbox();
    void         separateTargsAndMkArgs();
    void         startBuildProcess() const;
    void         buildTarget( const String &cdTo, const String &target ) const;
    int          runMake( const StringArray &arguments ) const;
    boolean      changeDir( const String &cdTo ) const;
    StringArray  &constructMakeCommand( const String &target,
        StringArray &mkCommand ) const;
    void         doRemoteOps();
    String       getNewCommandLine();
};

#endif //_ODE_BIN_BUILD_BUILD_HPP_

