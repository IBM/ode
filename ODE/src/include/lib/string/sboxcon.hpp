/**
 * SandboxConstants
 *
**/
#ifndef _ODE_LIB_STRING_SBOXCON_HPP_
#define _ODE_LIB_STRING_SBOXCON_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"


/**
 * Commonly used sandbox constants.
**/
class SandboxConstants
{
  public:

    // env/local variable names
    static const String ODEDLLPORT BACKING_BUILD_VAR; // "backing_build";
    static const String ODEDLLPORT BUILDLIST_VAR; // "build_list";
    static const String ODEDLLPORT MACHINELIST_VAR; // "machine_list";
    static const String ODEDLLPORT BUILDLIST_PATH_VAR; // "BUILD_LIST";
    static const String ODEDLLPORT SANDBOX_BASE_VAR; // "sandbox_base";
    static const String ODEDLLPORT SANDBOXBASE_VAR; // "SANDBOXBASE";
    static const String ODEDLLPORT BACKED_SANDBOXDIR_VAR; // "BACKED_SANDBOXDIR"

    static const String ODEDLLPORT BUILDENV_VAR; // "ode_build_env";
    static const String ODEDLLPORT SRCNAME_VAR; // "ODESRCNAME";
    static const String ODEDLLPORT DIRSEP_VAR; // "DIRSEP";
    static const String ODEDLLPORT CMDSEP_VAR; // "CMDSEP";
    static const String ODEDLLPORT PATHSEP_VAR; // "PATHSEP";
    static const String ODEDLLPORT SANDBOXRC_VAR; // "SANDBOXRC";
    static const String ODEDLLPORT SANDBOX_VAR; // "SANDBOX";
    static const String ODEDLLPORT CONTEXT_VAR; // "CONTEXT";
    static const String ODEDLLPORT WORKON_VAR; // "WORKON";
    static const String ODEDLLPORT CONFSINRCFILES_VAR; // "ode_confs_in_rcfiles"
    static const String ODEDLLPORT RCFILESDIR_VAR; // "ODERCFNAME";

    // directories and  ODEDLLPORT files
    static const String ODEDLLPORT DEFAULT_RCFILESDIR; // "rc_files";
    static const String ODEDLLPORT DEFAULT_SRCNAME; // "src";
    static const String ODEDLLPORT SBCONF_NAME; // "sb.conf";
    static const String ODEDLLPORT BUILDCONF_NAME; // "Buildconf";
    static const String ODEDLLPORT BUILDCONF_EXP_NAME; // "Buildconf.exp";
    static const String ODEDLLPORT BUILDCONF_LOCAL_NAME; // "Buildconf.local";

    static boolean init();
    inline static const String &getSANDBOXRC_FULLPATH();
    inline static const String &getSANDBOXRC_FILENAME();
    inline static const String &getDEFAULT_SANDBOXRCDIR();
    inline static const String &getDEFAULT_USER();
    inline static const String &getUSER_DIR_SEPARATOR();
    inline static const String &getUSER_CMD_SEPARATOR();
    inline static const String &getUSER_PATH_SEPARATOR();
    inline static const String &getSRCNAME();
    inline static const String &getRCFILES_DIR();
    static const String &getODETEMP_DIR();

    static const String &getSANDBOXBASE();
    static const String &getMAKEDIR();
    static const String &getOBJECTDIR();
    static const String &getBACKED_SANDBOXDIR();

    static void setSANDBOXBASE( const String &base );
    static void setMAKEDIR( const String &makedir );
    static void setOBJECTDIR( const String &objdir );
    static void setBACKED_SANDBOXDIR( const String &sbdir );


  private:

    static boolean ODEDLLPORT inited;
    static void setSourceDirName();
    static void setDirSep();
    static void setCmdSep();
    static void setPathSep();
    static void setSbrcFullName();
    static void setUserName();
    static void setRcfilesDirName();
    static String getTempDir();
    static String getDefaultTempDir();
    static boolean testTempDir( const String &dirname, const String &varname );

    static String ODEDLLPORT SANDBOXRC_FULLPATH;
    static String ODEDLLPORT SANDBOXRC_FILENAME;
    static String ODEDLLPORT DEFAULT_SANDBOXRCDIR;
    static String ODEDLLPORT DEFAULT_USER;
    static String ODEDLLPORT DEFAULT_CMD_SEPARATOR;
    static String ODEDLLPORT USER_DIR_SEPARATOR;
    static String ODEDLLPORT USER_CMD_SEPARATOR;
    static String ODEDLLPORT USER_PATH_SEPARATOR;
    static String ODEDLLPORT SRCNAME;
    static String ODEDLLPORT RCFILES_DIR;

    static String ODEDLLPORT SANDBOXBASE;
    static String ODEDLLPORT MAKEDIR;
    static String ODEDLLPORT OBJECTDIR;
    static String ODEDLLPORT BACKED_SANDBOXDIR;
};

inline const String &SandboxConstants::getSANDBOXRC_FULLPATH()
{
  return (SANDBOXRC_FULLPATH);
}

inline const String &SandboxConstants::getSANDBOXRC_FILENAME()
{
  return (SANDBOXRC_FILENAME);
}

inline const String &SandboxConstants::getDEFAULT_SANDBOXRCDIR()
{
  return (DEFAULT_SANDBOXRCDIR);
}

inline const String &SandboxConstants::getDEFAULT_USER()
{
  return (DEFAULT_USER);
}

inline const String &SandboxConstants::getUSER_DIR_SEPARATOR()
{
  return (USER_DIR_SEPARATOR);
}

inline const String &SandboxConstants::getUSER_CMD_SEPARATOR()
{
  return (USER_CMD_SEPARATOR);
}

inline const String &SandboxConstants::getUSER_PATH_SEPARATOR()
{
  return (USER_PATH_SEPARATOR);
}

inline const String &SandboxConstants::getSRCNAME()
{
  return (SRCNAME);
}

inline const String &SandboxConstants::getRCFILES_DIR()
{
  return (RCFILES_DIR);
}

#endif /* _ODE_LIB_STRING_SBOXCON_HPP_ */
