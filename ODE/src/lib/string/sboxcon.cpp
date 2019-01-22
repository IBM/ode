/**
 * SandboxConstants
 *
**/
#define _ODE_LIB_STRING_SBOXCON_CPP_
#include "lib/string/sboxcon.hpp"
#include "lib/portable/env.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/native/file.h"
#include "lib/io/cmdline.hpp"

#define ODE_TEMPDIR_NAME "ODETMP"
#if defined(DEFAULT_SHELL_IS_VMS)
#define UNIX_DEFAULT_TEMPDIR "/sys$scratch"
#else
#define UNIX_DEFAULT_TEMPDIR "/tmp"
#endif

const String SandboxConstants::BACKING_BUILD_VAR = "backing_build";
const String SandboxConstants::BUILDLIST_VAR = "build_list";
const String SandboxConstants::MACHINELIST_VAR = "machine_list";
const String SandboxConstants::BUILDLIST_PATH_VAR = "BUILD_LIST";
const String SandboxConstants::SANDBOX_BASE_VAR = "sandbox_base";
const String SandboxConstants::SANDBOXBASE_VAR = "SANDBOXBASE";
const String SandboxConstants::BACKED_SANDBOXDIR_VAR = "BACKED_SANDBOXDIR";
const String SandboxConstants::BUILDENV_VAR = "ode_build_env";
const String SandboxConstants::SRCNAME_VAR = "ODESRCNAME";
const String SandboxConstants::DIRSEP_VAR = "DIRSEP";
const String SandboxConstants::CMDSEP_VAR = "CMDSEP";
const String SandboxConstants::PATHSEP_VAR = "PATHSEP";
const String SandboxConstants::SANDBOXRC_VAR = "SANDBOXRC";
const String SandboxConstants::SANDBOX_VAR = "SANDBOX";
const String SandboxConstants::CONTEXT_VAR = "CONTEXT";
const String SandboxConstants::WORKON_VAR = "WORKON";
const String SandboxConstants::CONFSINRCFILES_VAR = "ode_confs_in_rcfiles";
const String SandboxConstants::RCFILESDIR_VAR = "ODERCFNAME";

const String SandboxConstants::DEFAULT_RCFILESDIR = "rc_files";
const String SandboxConstants::DEFAULT_SRCNAME = "src";
const String SandboxConstants::SBCONF_NAME = "sb.conf";
const String SandboxConstants::BUILDCONF_NAME = "Buildconf";
const String SandboxConstants::BUILDCONF_EXP_NAME = "Buildconf.exp";
const String SandboxConstants::BUILDCONF_LOCAL_NAME = "Buildconf.local";

String SandboxConstants::SANDBOXBASE;
String SandboxConstants::MAKEDIR;
String SandboxConstants::OBJECTDIR;
String SandboxConstants::BACKED_SANDBOXDIR;

String SandboxConstants::SANDBOXRC_FULLPATH = "";
//    setSbrcFullName();
String SandboxConstants::SANDBOXRC_FILENAME = "";
//    Path.fileName( SANDBOXRC_FULLPATH );
String SandboxConstants::DEFAULT_SANDBOXRCDIR = "";
//    Path.filePath( SANDBOXRC_FULLPATH );
String SandboxConstants::DEFAULT_USER = "";
//    setUserName();
String SandboxConstants::DEFAULT_CMD_SEPARATOR = "&&";
//    setCmdSep();

/**
 * The directory slash that the user wants to use when outputting
 * paths to the environment.  If the user specifies a value in the
 * environment variable "DIRSEP", it is used.  If not, the platform-
 * specific character is used.
**/
String SandboxConstants::USER_DIR_SEPARATOR = "";
//    setDirSep();

/**
 * The command separator that the user wants to use when putting
 * multiple commands on the same command line.
 * If the user specifies a value in the
 * environment variable "CMDSEP", it is used.  If not, the shell-
 * specific characters are used.
**/
String SandboxConstants::USER_CMD_SEPARATOR = "";
//    setCmdSep();

/**
 * The path separator that the user wants to use when outputting
 * path lists to the environment.  If the user specifies a value in the
 * environment variable "PATHSEP", it is used.  If not, the platform-
 * specific character is used.
**/
String SandboxConstants::USER_PATH_SEPARATOR = "";
//    setPathSep();

String SandboxConstants::SRCNAME = "";
//    setSourceDirName();

String SandboxConstants::RCFILES_DIR = "";
//    setRcfilesDirName();

boolean SandboxConstants::inited = false;

/**
 * This static initializer loads the environment variables
 * from the shell if needed, then adds various required ones
 * that may not already be set.
**/
boolean SandboxConstants::init()
{
  if (!inited)
  {
    PlatformConstants::setMACHINE();
    PlatformConstants::setShellInfo();
    setDirSep();
    setCmdSep();
    setPathSep();
    Env::setenv( StringConstants::HOME_VAR,
        Path::userize( Path::gethome() ), false );
    setSourceDirName();
    setSbrcFullName();
    SANDBOXRC_FILENAME = Path::fileName( SANDBOXRC_FULLPATH );
    DEFAULT_SANDBOXRCDIR = Path::filePath( SANDBOXRC_FULLPATH );
    setUserName();
    setRcfilesDirName();

    inited = true;
  }

  return (inited);
}

/**
 * Get the name of the source directory (usually "src",
 * but the user can override this setting).  [SRCNAME]
 *
**/
void SandboxConstants::setSourceDirName()
{
  const String *name;

  if ((name = Env::getenv( SRCNAME_VAR )) == 0)
  {
    name = &DEFAULT_SRCNAME;
    Env::setenv( SRCNAME_VAR, *name, true );
  }

  SRCNAME = *name;
}

/**
 * Set the directory separator character that the
 * user wishes to use.  [USER_DIR_SEPARATOR]
 *
**/
void SandboxConstants::setDirSep()
{
  const String *name;

  if ((name = Env::getenv( DIRSEP_VAR )) == 0)
  {
    name = &Path::DIR_SEPARATOR;
    Env::setenv( DIRSEP_VAR, *name, true );
  }

  USER_DIR_SEPARATOR = *name;
}

/**
 * Set the command separator characters that the
 * user wishes to use.  [USER_CMD_SEPARATOR]
 *
**/
void SandboxConstants::setCmdSep()
{
  const String *name;

  if ((name = Env::getenv( CMDSEP_VAR )) == 0)
  {
    name = &DEFAULT_CMD_SEPARATOR;
    Env::setenv( CMDSEP_VAR, *name, true );
  }

  USER_CMD_SEPARATOR = *name;
}

/**
 * Set the path separator character that the
 * user wishes to use.  [USER_PATH_SEPARATOR]
 *
**/
void SandboxConstants::setPathSep()
{
  const String *name;

  if ((name = Env::getenv( PATHSEP_VAR )) == 0)
  {
    name = &Path::PATH_SEPARATOR;
    Env::setenv( PATHSEP_VAR, *name, true );
  }

  USER_PATH_SEPARATOR = *name;
}

/**
 * Get the full path to the default sandbox rc file.  [SANDBOXRC_FULLPATH]
 *
**/
void SandboxConstants::setSbrcFullName()
{
  const String *name;

  if ((name = Env::getenv( SANDBOXRC_VAR )) == 0)
  {
    SANDBOXRC_FULLPATH = Path::gethome() + Path::DIR_SEPARATOR + ".sandboxrc";
    Env::setenv( SANDBOXRC_VAR, Path::userize( SANDBOXRC_FULLPATH ), true );
  }
  else
    SANDBOXRC_FULLPATH = *name;
}

/**
 * Get the user name.  Could be different from what
 * Java says, depending on if the user changes/sets
 * the value of USER.  [DEFAULT_USER]
 *
**/
void SandboxConstants::setUserName()
{
  const String *name;

  if ((name = Env::getenv( StringConstants::USER_VAR )) == 0)
  {
    if ((name = Env::getenv( "LOGNAME" )) == 0)
      if ((name = Env::getenv( "USERNAME" )) == 0)
        name = &StringConstants::UNKNOWN_USERNAME;
    Env::setenv( StringConstants::USER_VAR, *name, true );
  }

  DEFAULT_USER = *name;
}

/**
 * Set the directory name for where the sb.conf,
 * sets, and projects files are normally located.
 * [RCFILES_DIR]
**/
void SandboxConstants::setRcfilesDirName()
{
  const String *name;

  if ((name = Env::getenv( RCFILESDIR_VAR )) == 0)
  {
    name = &DEFAULT_RCFILESDIR;
    Env::setenv( RCFILESDIR_VAR, *name, true );
  }

  RCFILES_DIR = *name;
}

const String &SandboxConstants::getODETEMP_DIR()
{
  static const String ODETEMP_DIR( getTempDir() );

  return (ODETEMP_DIR);
}

String SandboxConstants::getTempDir()
{
  const String *tmpdir = 0;
  String result;
  const char *tempvars[] =
      { ODE_TEMPDIR_NAME, "TMPDIR", "TMP", "TEMP", 0 };
  int var_index = 0;

  while (tempvars[var_index] != 0)
  {
    if ((tmpdir = Env::getenv( tempvars[var_index] )) != 0)
    {
      if (testTempDir( *tmpdir, tempvars[var_index] ))
      {
        result = *tmpdir;
        Path::canonicalizeThis( result, false );
        break;
      }
    }

    ++var_index;
  }

  if (result.length() == 0) // never found a valid dir in the env vars, so...
    return (getDefaultTempDir());
  else
    return (result);
}

/**
 * First try the common local file system temp directory (if
 * such a thing is able to be determined without environment
 * variables), or use the current dir as a last resort.
**/
String SandboxConstants::getDefaultTempDir()
{
  String result;
  struct ODEstat statinfo;

#ifndef DEFAULT_SHELL_IS_CMD
  result = UNIX_DEFAULT_TEMPDIR;
  if (ODEstat( result.toCharPtr(), &statinfo, OFFILE_ODEMODE, 1 ) == 0 &&
      statinfo.is_dir && statinfo.is_writable)
    return (result);
#endif

  result = Path::getcwd();
  if (ODEstat( result.toCharPtr(), &statinfo, OFFILE_ODEMODE, 1 ) != 0 ||
      !statinfo.is_writable)
  {
    Interface::printError( CommandLine::getProgramName() + ": " +
                           "Do not have write access to current directory" );
    Interface::printError( "Please set " ODE_TEMPDIR_NAME " or equivalent" );
  }

  return (result);
}

boolean SandboxConstants::testTempDir( const String &dirname,
    const String &varname )
{
  boolean rc = false; // assume the worst
  struct ODEstat statinfo;

  if (ODEstat( dirname.toCharPtr(), &statinfo, OFFILE_ODEMODE, 1 ) != 0)
    Interface::printError( CommandLine::getProgramName() + ": " +
                           String( "Variable \"" ) + varname +
        String( "\" refers to a nonexistent directory (" ) + dirname +
        String( ")" ) );
  else if (!statinfo.is_dir)
    Interface::printError( CommandLine::getProgramName() + ": " +
                           String( "Variable \"" ) + varname +
        String( "\" does not contain a directory (" ) + dirname +
        String( ")" ) );
  else if (!statinfo.is_writable)
    Interface::printError( CommandLine::getProgramName() + ": " +
                           String( "Variable \"" ) + varname + 
        String( "\" refers to a readonly directory (" ) + dirname +
        String( ")" ) );
  else
    rc = true;

  return (rc);
}

const String &SandboxConstants::getSANDBOXBASE()
{
  const String *baseptr;
  if (SANDBOXBASE.length() < 1 &&
      (baseptr = Env::getenv( SANDBOXBASE_VAR )) != 0)
    return (*baseptr);
  return (SANDBOXBASE);
}

const String &SandboxConstants::getMAKEDIR()
{
  const String *makedir;
  if (MAKEDIR.length() < 1 && (makedir = Env::getenv( "MAKEDIR" )) != 0)
    return (*makedir);
  return (MAKEDIR);
}

const String &SandboxConstants::getOBJECTDIR()
{
  const String *objdir;
  if (OBJECTDIR.length() < 1 && ((objdir = Env::getenv( "MAKEOBJDIR" )) != 0 ||
      (objdir = Env::getenv( "OBJECTDIR" )) != 0))
    return (*objdir);
  return (OBJECTDIR);
}

const String &SandboxConstants::getBACKED_SANDBOXDIR()
{
  const String *bsdir;
  if (BACKED_SANDBOXDIR.length() < 1 &&
      (bsdir = Env::getenv( "BACKED_SANDBOXDIR" )) != 0)
    return (*bsdir);
  return (BACKED_SANDBOXDIR);
}

void SandboxConstants::setSANDBOXBASE( const String &base )
{
  SANDBOXBASE = base;
  Env::setenv( "SANDBOXBASE", base, true );
}

void SandboxConstants::setMAKEDIR( const String &makedir )
{
  MAKEDIR = makedir;
  Env::setenv( "MAKEDIR", makedir, true );
}

void SandboxConstants::setOBJECTDIR( const String &objdir )
{
  OBJECTDIR = objdir;
}

void SandboxConstants::setBACKED_SANDBOXDIR( const String &sbdir )
{
  BACKED_SANDBOXDIR = sbdir;
  Env::setenv( "BACKED_SANDBOXDIR", sbdir, true );
}
