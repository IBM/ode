#ifndef _ODE_BIN_CRLFCON_CRLFCON_HPP_
#define _ODE_BIN_CRLFCON_CRLFCON_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/io/cmdline.hpp"
#include "lib/string/strarray.hpp"


class CRLFCon : public Tool
{
  public:

    static String program_name;

    CRLFCon( const char **argv, const char **envp ) :
        Tool( envp ), args( argv ), envs( envp ), cmdline( 0 ) {}

    inline ~CRLFCon()
    {
      delete cmdline;
    }

    static int classMain( const char **argv, const char **envp );
    boolean run();
    void printUsage() const;


  private:

    const char **args;
    const char **envs;
    boolean use_eof;
    char format;
    CommandLine *cmdline;

    void checkCommandLine();
    boolean verifyFile( const String &file, boolean writable );
    boolean convertFile( const StringArray &files );
};

#endif // _ODE_BIN_CRLFCON_CRLFCON_HPP_
