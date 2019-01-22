#ifndef _ODE_BIN_GENDEP_GENDEP_HPP_
#define _ODE_BIN_GENDEP_GENDEP_HPP_

#include <base/odebase.hpp>
#include "lib/io/cmdline.hpp"
#include "lib/string/strarray.hpp"
#include "lib/portable/hashtabl.hpp"
#include "lib/string/string.hpp"
#include "lib/io/file.hpp"
#include "lib/io/path.hpp"
#include "lib/io/cfgf.hpp"

class GenDep : public Tool
{
  public:
    static const String DEFAULT_TARGET_SUFF;
  
    GenDep( const char **argv, const char **envp )
          : Tool( envp ), args( argv ), envs( envp ), cmdline( 0 ) {};

    static int classMain( const char **argv, const char **envp );
    int        run();
    void       printUsage() const;

    ~GenDep()
    {
      delete cmdline;
      delete variables;
      
    };

  private:
    const char       **args;
    const char       **envs;
    CommandLine      *cmdline;
    boolean          quit_if_not_found;
    boolean          no_dup_files;
    boolean          disp_table;
    boolean          full_target_path;
    boolean          cond_truth;
    boolean          headersOnly;
    StringArray      targetNames;
    StringArray      includeDirs;
    StringArray      excludeDirs;
    SetVars          *variables;
    StringArray      srcFileNames;
    int              nestLevel;
    boolean          exceededNestLimit;
    int              hash_hits;
    int              hash_misses;
    String           curSourceDir;
    int              includeFileType;
    boolean          continueProcessing;
    Hashtable< SmartCaseString, char > depends;
  
    void checkCommandLine();
    void checkRespfile( StringArray &arguments );
    void initVars( const StringArray &inputs );
    void processSource( const String& srcfile );
    void parseSource( const String& srcfile );
    String getIncludeFileName( String &incline );
    String findIncludeFile( String &incfile, ConfigFile &srcfile );
    boolean isExcludedFile( String &incpath );
    boolean processInclude( String &incline, ConfigFile &srcfile );
    boolean processDefine( String &defline, ConfigFile &srcfile );
    boolean processUndef( String &undefline, ConfigFile &srcfile );
    void writeDepInfo( const String& srcfilename );
    void displayWarning( const String&, ConfigFile &srcfile );


};

#endif //_ODE_BIN_GENDEP_GENDEP_HPP_
