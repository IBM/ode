//***********************************************************************
//* MkDep
//*
//***********************************************************************
#ifndef _ODE_LIB_IO_MKDEPC_HPP_
#define _ODE_LIB_IO_MKDEPC_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/io/cmdline.hpp"
#include "lib/io/file.hpp"
#include "lib/string/strarray.hpp"
#include "lib/portable/vector.hpp"

#include "lib/intcmds/target.hpp"
#include "lib/intcmds/depmkfil.hpp"



class MkDep : public Tool {

  public:
    // used normally by command
    MkDep( const char **argv, const char **envp )
      : Tool( envp ), envs( envp ),
        kKey( false ), iKey( false ), topKey( false ),
        rmKey( false ), elxdepKey( false ), absKey( false ), 
        quoteBlanks( false ), // currently only affects Windows
        cmdLine( 0 ), depFileNames(0), isCommand( true ), 
        isRuntimeCommand( false ),
        mkdepName( "mkdep" )
    {
      args = new StringArray( argv );
    }
    // used normally by library
    MkDep()
      : kKey( false ), iKey( false ), topKey( false ),
        rmKey( false ), elxdepKey( false ), absKey( false ), 
        quoteBlanks( false ), // currently only affects Windows
        cmdLine( 0 ), depFileNames(0), isCommand( false),
        isRuntimeCommand( true ),
        mkdepName( ".rmkdep" )
    {
    }

    static int classMain( const char **argv, const char **envp );
    int        run();
    static boolean run( StringArray *pargs, const SetVars *const envs );
    void       printUsage() const;

    ~MkDep()
    {
      if (cmdLine) {
        delete cmdLine;
        cmdLine = 0;
      }

      if(depFileNames) {
        delete depFileNames;
        depFileNames = 0;
      }
      // Do not free args, if it comes from the library code.
      if (isCommand)
        delete args;
    }

    /**
     * print options
     */
    void printOptions();

    static const String ODEDLLPORT WORD_SPLIT_STRING; // " \t\r\n"

  private:

    // Data members
    //

    static const String ODEDLLPORT      DEFAULT_DEPMK_NAME;
    static const StringArray ODEDLLPORT DEFAULT_DEPFILE_SUFFIX;
    static const String ODEDLLPORT      BACKED_SANDBOXDIR;
    static const String ODEDLLPORT      MAKEOBJDIR;
    static const String ODEDLLPORT      OBJECTDIR;

    StringArray   *args;
    const char    **envs;
    String        mkdepName;

    CommandLine   *cmdLine;

    // keys set on command line
    boolean       kKey;
    boolean       iKey;
    boolean       topKey;
    boolean       rmKey;
    boolean       elxdepKey;
    boolean       elpdepKey;
    boolean       eKey;
    boolean       absKey;
    boolean       quoteBlanks; // currently only affects Windows
    boolean       isCommand;
    boolean       isRuntimeCommand;

    // variables following key on command line
    Vector<String>  depFileSuffix;
    String          depMkName;
    Vector<String>  removeDirs;
    Vector<String>  keepDirs;
    Vector<String>  removeSuffs;
    Array<File>    *depFileNames;

    Array<StringArray> substStrings;
    Array<StringArray> patterns; 

    Vector<SmartCaseString>  elpdepTargs;

    // shell and sandbox variables
    StringArray   backedSrcDirs;
    StringArray   backedObjDirs;
    const String *objectDir;
    StringArray   backedDirs;
    String        sandboxBase;
    String        relCurDir;

    // Methods
    //

    static StringArray initDefaultSuffixes();

    /**
     * for each path, when first time to get it, use Path.normalize()
     * to simplify it. So when comparing with other paths, we don't
     * need to normalize it. But when write it to file or display it, we
     * use Path.unixize() to keep the integration of the system.
     * @param
    **/
    void parseCmdLine();

    void getDepFileNames( const StringArray &paths );

    /**
     * remove those depFileNames that have same root name
     * the priority is defined in suffix vector
     *
     **/
    void filterDepFiles();

    /**
     * get sandboxBase, relCurDir, backedSrcDirs, backedDirs
     * from shell environment. So that to copy depend.mk from backing build
     * or create depend.mk locally.
     *
     * Varibales: sandboxBase, relCurDir, backedSrcDirs, backedDirs
     **/
    void getEnvVariables();

    /**
     * create or copy depend.mk file from backing build,
     * update depend.mk file with all dependent files
     *
     **/
    void updateDepMk( );

    /**
     * Build targets directly from a file (Avoid creating DependFile object) 
     *
     * More than one target  can be built from a given file name.
     **/
    void buildTarget(const String&  depFileName, DepMkFile& dependMkFile );


    /**
     * Deletes the dependency files, if necessary.
     **/
    void removeDepFiles();

    /* **
     * Build a new line based on the options given on the command line.
     * i.e add ${MAKETOP} if necessary. The line argument is either a
     * source or a target that is to be normalized.
     * If the target is not StringConstants::EMPTY_STRING then line is
     * assumed to be a source for that target. 
     * normalizeLine can return EMPTY_STRING, meaning that it has been
     * determined that the line should not be put out as a dependency.
     * line and target are assumed to have been normalized with
     * Path::normalizeThis()
     * **/
    String normalizeLine( const String &line, const String &target );


    /* **
     * See, if a line starts with the backed src dir or not.
     *
     * **/
    int lineStartsWith( const Vector<String> &heads, const String &line );


    /* **
     * See, if a line starts with the backed src dir or not.
     *
     * **/
    int lineStartsWith( const StringArray &heads, const String &line );
};

#endif //_ODE_LIB_IO_MKDEPC_HPP_


