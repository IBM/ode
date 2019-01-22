#ifndef _ODE_LIB_IO_GENPATH_HPP_
#define _ODE_LIB_IO_GENPATH_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/io/cmdline.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/string.hpp"
#include "lib/portable/env.hpp"
#include "lib/string/sboxcon.hpp"


class Genpath : public Tool
{
  public:

    // ctr normally used by the library
    inline Genpath() :
        args( 0 ), cmdLine( 0 ),
        backedDirs( 4 ), vpathFormat( false ), shouldExist( false ),
        objDirabs( false ), onlySrc( false ), onlyObj( false ),
        output_string( 0 )
    {
      env_vars = Env::getSetVars();
    }

    // ctr normally used by the genpath command
    inline Genpath( const char **argv, const char **envp ) :
        Tool( envp ), cmdLine( 0 ),
        backedDirs( 4 ), vpathFormat( false ), shouldExist( false ),
        objDirabs( false ), onlySrc( false ), onlyObj( false ),
        output_string( 0 )
    {
      env_vars = Env::getSetVars();
      args = new StringArray( argv );
    }

    void reset(); // reset object as if it had just been constructed
    int run();
    // static version so other library classes can use us easily
    static boolean run( const StringArray &args, String &buf,
        const SetVars *vars );
    void printUsage() const;

    inline ~Genpath()
    {
      //@@@ Leak it for speed...  When program exits the memory will be
      //@@@ given back
      //@@@ delete cmdLine;
    };


  private:

    const StringArray *args;
    CommandLine *cmdLine;
    String objDir;
    StringArray backedDirs;
      // output parameters
    boolean vpathFormat;
    boolean shouldExist;
    boolean objDirabs;
    boolean onlySrc;
    boolean onlyObj;
    String *output_string;
    const SetVars *env_vars;
    String flag;

    void    checkCommandLine();
    void    getSandbox();
    void    createOutputDirs();
    inline const String &getILpathExtensions() const;
    void    generatePaths( const StringArray &qflags, const StringArray &dirs );
    inline  boolean inSandbox() const;
    void    getDirs();
    void    setOutputParameters();
    inline  boolean inSbEnv() const;
    void    printPath( const String &path ) const;
    inline  boolean inLibraryMode() const;
};

/**
 * Check to see if you are in a sb environ.
**/
inline boolean Genpath::inSbEnv() const
{
  return (!SandboxConstants::getSANDBOXBASE().isEmpty());
}

inline boolean Genpath::inLibraryMode() const
{
  return (output_string != 0);
}

/**
 * For the "-I", "-L" path inputs this method generates the
 * possible extensions to each path.
**/
inline const String &Genpath::getILpathExtensions() const
{
  return (SandboxConstants::getMAKEDIR());
}

#endif /* _ODE_BIN_GENPATH_GENPATHC_HPP_ */
