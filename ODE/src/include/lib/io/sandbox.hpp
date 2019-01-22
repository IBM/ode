/**
 * Sandbox
**/
#ifndef _ODE_LIB_IO_SANDBOX_HPP_
#define _ODE_LIB_IO_SANDBOX_HPP_


#include <base/odebase.hpp>
#include "lib/io/bldlstcf.hpp"
#include "lib/io/setvarcf.hpp"
#include "lib/io/sbrccf.hpp"
#include "lib/io/sbcnfrdr.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/setvars.hpp"
#include "lib/string/svarlink.hpp"

/**
 * Provides almost all the information and lots of
 * operations revolving around a sandbox.  Remember
 * that a single instantiation represents a single
 * sandbox.  To add multiple sandboxes to a single
 * rc file in one go requires the creation of multiple
 * Sandbox objects.
 *
 * When the goal is to create a new sandbox, one must use
 * the constructors with 2 or more arguments...it is best
 * to use the 4-argument constructor with readnow==false
 * and may_create_sbrc==true.
 * In all other cases, it is preferable to use the constructors
 * with less than 3 arguments (which all use readnow==true and
 * may_create_sbrc==false).
**/
class Sandbox
{
  public:

    Sandbox( boolean check_curdir = true,
        const String &sandboxrc = "",
        const String &sandbox = "",
        boolean readnow = true, boolean may_create_sbrc = false,
        boolean read_buildlist = false, boolean allow_recursion = false );
    virtual ~Sandbox();

    void read();
    boolean create( const StringArray &mksblist,
        const String &backdir, const String &basedir,
        boolean is_default = false );
    boolean remove();
    SandboxRCConfigFile *getSandboxRC() const;
    SetVarConfigFile *getSbconf() const;
    BuildListConfigFile *getBuildList() const;
    String getBuildListPath() const;
    const String *getSandboxBuildListName() const;
    const SetVars &getSbconfLocals() const;
    const SetVars &getBuildconfLocals() const;
    const SetVars &getEnvs() const;
    SetVarLinkable *getVariables() const;
    const String *getBackingDir() const;
    boolean setBackingDir( const String &dir );
    boolean isBuildEnv() const;
    boolean setBuildEnv( boolean is_build_env );
    boolean isConfsInRcfilesEnv() const;
    boolean setConfsInRcfilesEnv( boolean is_confsinrcfiles_env );
    boolean isBacked() const;
    String getSandboxName() const;
    String getSandboxBaseDir() const;
    String getSandboxBaseDirUneval() const;
    String getSandboxBase() const;
    String getSandboxRCName() const;
    String getSandboxRCBase() const;
    StringArray *getBackingChainArray( StringArray *buf ) const;
    String getBackingChain() const;
    boolean verifyMachine() const;
    boolean verifyMachine( const String &backdir, boolean multiple ) const;
    String getMachineList() const;
    String getMachineList( const String &backdir ) const;
    boolean sbExists() const;
    boolean isNonRecursiveBackingDir( const String &path ) const;
    static boolean isLegalSandboxName( const String &sbname );


  private:

    static const String ODEDLLPORT BOOLVAR_ON;
    static const String ODEDLLPORT BOOLVAR_OFF;
    SandboxRCConfigFile *sbrc;
    BuildListConfigFile *buildlist;
    SetVarConfigFile *sbconf;
    SbconfReader *sbconf_reader;
    SetVars buildconf_vars;
    SetVars sbconf_vars;
    SetVars env_vars;
    String sandbox, sandboxrc, basedir, basedirUneval, sbdir, 
        backing_chain, build_list_path;
    StringArray backing_chain_array;
    boolean is_backed, allow_recursion;
    SetVars *var_store;

    String getDefaultSandbox( boolean check_curdir ) const;
    String inSandboxDir( const SandboxRCConfigFile &sandboxrc ) const;
    boolean createRCFiles( const String &back_dir );
    boolean createSbconfFile( const String &backdir, boolean buildenv );
    void readAll();
    SetVars *getBackedSbconfVars( const String &backdir ) const;
    SetVarConfigFile *getBackedSbconf( const String &backdir ) const;
    void readSbconf();
    void readBuildList();
    void obtainBuildListPath( const SetVarConfigFile &svcf );
    String getBuildListPath( const String &sandbox );
    void readBuildconf();
};

#endif /* _ODE_LIB_IO_SANDBOX_HPP_ */
