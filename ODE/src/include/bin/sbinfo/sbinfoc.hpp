#ifndef _ODE_BIN_SBINFO_SBINFOC_HPP_
#define _ODE_BIN_SBINFO_SBINFOC_HPP_

#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/io/cmdline.hpp"
#include "bin/sbinfo/sbinfoex.hpp"

/**
 * A Class to print out sandbox information, including current
 * sandbox name, sandbox base directory,
 * backing build, and backing chain.
 *
**/
class SbInfo : public Tool
{
  public:

    SbInfo( const char **argv, const char **envp ) :
        Tool( envp ), PROGRAM_NAME( "sbinfo" ),
        args( argv ), envs( envp ), cmdline( 0 ) {};

    static int classMain( const char **argv, const char **envp );
    int run();
    String toString() const;
    void printUsage() const;

    ~SbInfo()
    {
      delete cmdline;
    };


  private:

    const String PROGRAM_NAME;
    const char **args;
    const char **envs;
    CommandLine *cmdline;

    // variables following key on command line
    String rcfilename;
    String sboxname;

    // other variables
    StringArray entry_variables; // the command line variables

    void parseCmdLine();
    void readPrintInfo();
};

#endif /* _ODE_BIN_SBINFO_SBINFOC_HPP_ */
