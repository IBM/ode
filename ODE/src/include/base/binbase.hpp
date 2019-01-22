/**
 * All ODE tools (mksb, workon, etc.) should derive
 * from this class and implement the pure virtual
 * methods described.
**/
#ifndef _ODE_BINBASE_HPP_
#define _ODE_BINBASE_HPP_

#include <lib/io/ui.hpp>
#include <lib/portable/env.hpp>
#include <lib/portable/platcon.hpp>
#include <lib/string/sboxcon.hpp>
#include <lib/string/strarray.hpp>
#include <lib/util/signal.hpp>

#ifdef ODE_USE_GLOBAL_ENVPTR
extern const char **environ;
#endif


class Tool : public Signalable
{
  public:

    // constructors
    Tool(){};
    Tool( const char **envp )
    {
      init( envp );
    };
    Tool( const StringArray &envp )
    {
      init( envp );
    }

    static void init( const char **envp )
    {
#ifdef ODE_USE_GLOBAL_ENVPTR
      Env::init( environ );
#else
      Env::init( envp );
#endif
      SandboxConstants::init();
    };

    static void init( const StringArray &envp )
    {
#ifdef ODE_USE_GLOBAL_ENVPTR
      Env::init( environ );
#else
      Env::init( envp );
#endif
      SandboxConstants::init();
    };

    virtual ~Tool() {};

    // tools must implement these
    virtual void printUsage() const = 0; // print the usage text
    virtual int run() = 0; // akin to main()
    virtual void handleInterrupt() {}; // override if desired
};

#endif //_ODE_BINBASE_HPP_
