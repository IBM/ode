#ifndef _ODE_BIN_MKBB_MKBB_HPP_
#define _ODE_BIN_MKBB_MKBB_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "bin/mksb/mksbc.hpp"
#include "lib/io/cmdline.hpp"
#include "lib/io/sandbox.hpp"


class Mkbb : public Mksb
{
  public:
    Mkbb( const char **argv, const char **envp )
        : Mksb( argv, envp, "mkbb", true ) {};

    static int classMain( const char **argv, const char **envp );
    int        run();
    void       printUsage() const;

    ~Mkbb()
    {};

  private:
    void        checkCommandLine();
};

#endif //_ODE_BIN_MKBB_MKBB_HPP_
