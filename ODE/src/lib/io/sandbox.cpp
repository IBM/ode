/**
 * Sandbox
**/

#include <ctype.h>

#define _ODE_LIB_IO_SANDBOX_CPP_
#include "lib/io/sandbox.hpp"
#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/env.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/variable.hpp"
#include "lib/exceptn/sboxexc.hpp"
#include "lib/exceptn/parseexc.hpp"
#include "lib/io/cmdline.hpp"

const String Sandbox::BOOLVAR_ON = "true";
const String Sandbox::BOOLVAR_OFF = "false";


/**
 * Parse the specified sandbox config file
 * and use the specified sandbox (check_curdir
 * is only meaningful if a null sandbox is passed).
 *
 * @param check_curdir If true (and the sandbox
 * parameter is null), the current directory
 * will be used to try to determine which sandbox is
 * appropriate.  Otherwise, the default sandbox (as
 * designated in the sandbox rc file) is used.
 * @param sandboxrc The fullpath of the sandbox rc file.
 * @param sandbox The sandbox to use.  Required if the
 * intent is to create a new sandbox.  If null is passed,
 * the default sandbox is used (see also check_curdir).
 * @param readnow If true, parse the sb.conf
 * and Buildconf* config files as usual.  If false, do NOT
 * attempt to read these files...simply obtain whatever
 * minimal info from the sandbox rc file can be gotten at
 * this time (normally specified when the plan is to create
 * a new sandbox).
 * @param may_create_sbrc If true, and the sandbox rc file
 * doesn't exist, don't throw an exception (instead, allow it
 * to be created as needed).  If false, throw a SandboxException
 * if the sandbox rc file doesn't exist.
 * @param read_buildlist If true, read the build_list config
 * file.  If false, don't (note that getBuildList() will return
 * null in this case).
 */
Sandbox::Sandbox( boolean check_curdir,
    const String &sandboxrc,
    const String &sandbox,
    boolean readnow, boolean may_create_sbrc,
    boolean read_buildlist, boolean allow_recursion ) :
    sbrc( 0 ), buildlist( 0 ), sbconf( 0 ),
    sbconf_reader( 0 ),
    var_store( 0 ),
    buildconf_vars( new Env(), true, true ),
    sbconf_vars( new Env(), true, true ),
    env_vars( PlatformConstants::onCaseSensitiveOS() ),
    is_backed( false ), allow_recursion( allow_recursion )
//    throws SandboxException
{
  if (sandboxrc == StringConstants::EMPTY_STRING)
  {
    sbrc = new SandboxRCConfigFile();
    this->sandboxrc = sbrc->getPathname();
  }
  else
  {
    this->sandboxrc = sandboxrc;
    Path::normalizeThis( this->sandboxrc );
    String sbpath = Path::filePath( this->sandboxrc );
    String sbfile = Path::fileName( this->sandboxrc );
    sbrc = new SandboxRCConfigFile( sbpath, sbfile );
  }
  if (sandbox == StringConstants::EMPTY_STRING)
    this->sandbox = getDefaultSandbox( check_curdir );
  else
    this->sandbox = sandbox;
  if (!isLegalSandboxName( this->sandbox ))
    throw (SandboxException( this->sandbox + " is an illegal sandbox name." ));
  Env::setenv( SandboxConstants::SANDBOX_VAR, this->sandbox, true );
  if (sbrc->exists())
  {
    Env::setenv( SandboxConstants::SANDBOXRC_VAR, Path::userize(
        Path::canonicalize( this->sandboxrc ) ), true );
    if (!sbrc->sandboxExists( this->sandbox ) && !may_create_sbrc)
      throw (SandboxException( String( "Sandbox " ) + this->sandbox +
          " does not exist in sandbox RC file " + this->sandboxrc ));
    basedir = sbrc->getSandboxBase( this->sandbox );
    Path::unixizeThis( basedir );
    if (basedir.endsWith( "/" ))
      sbdir = basedir + this->sandbox;
    else
      sbdir = basedir + "/" + this->sandbox;
    if (!Path::exists( sbdir ) && !may_create_sbrc)
      throw (SandboxException( String( "Sandbox directory (" ) +
          sbdir + ") does not exist" ));
    SandboxConstants::setSANDBOXBASE( Path::userize( sbdir ) );
    if (read_buildlist)
    {
      try
      {
        readBuildList();
      }
      catch (SandboxException &se)
      {
        Interface::printWarning( "Found problem while searching for build list." );
        Interface::printWarning( se.getMessage() );
        buildlist = 0; // just in case
        build_list_path = StringConstants::EMPTY_STRING; // just in case
      }
    }
    if (readnow)
      readAll();
  }
  else if (!may_create_sbrc)
  {
    throw (SandboxException( String( "Sandbox RC file " ) +
        this->sandboxrc + " does not exist!" ));
  }
  else
  {
    Interface::print( "User rc file, " + this->sandboxrc +
        ", does not exist; will create it." );
  }
}

Sandbox::~Sandbox()
{
  delete sbrc;
  sbrc = 0;
  delete buildlist;
  buildlist = 0;
  delete sbconf;
  sbconf = 0;
  delete sbconf_reader;
  sbconf_reader = 0;
  delete var_store;
}

String Sandbox::getDefaultSandbox( boolean check_curdir ) const
{
  String sandbox;
  const String *sandboxptr;

  if ((sandboxptr = Env::getenv( SandboxConstants::SANDBOX_VAR )) != 0)
    sandbox = *sandboxptr;
  else if (check_curdir)
    sandbox = inSandboxDir( *sbrc );
  if (sandbox == StringConstants::EMPTY_STRING)
    sandbox = sbrc->getDefaultSandbox();
  return (sandbox);
}

/**
 * Find out if the current working directory is a subdirectory
 * of a sandbox in the specified sandboxrc file object.
 *
 * @param sandboxrc The SandboxRCConfigFile object to check for
 * sandboxes.
 * @return The name of the sandbox of which the current working
 * directory is a subdirectory.  Returns the empty string if the
 * working directory is not within a sandbox.
 */
String Sandbox::inSandboxDir( const SandboxRCConfigFile &sandboxrc ) const
{
  StringArray sandboxes;
  sandboxrc.getSandboxPathList( &sandboxes );
  int index = Path::findPrefix( Path::getcwd(), sandboxes );

  if (index != ELEMENT_NOTFOUND)
    return (Path::fileName( sandboxes[index] ));
  return ("");
}

/**
 * Read the sb.conf/Buildconf* config
 * files.  Recommended that this only be called once,
 * and only if the constructor explicitly specified
 * readnow==FALSE.
 */
void Sandbox::read()
//    throws SandboxException
{
  readAll();
}


/**
 * Create a new sandbox.  This modifies the sandbox rc
 * file, creates the sandbox directory, the rc_files
 * directory, and the sb.conf files.
 *
 * @param mksblist The list of mksb lines that are
 * appended to the sandbox rc file (but only if the
 * sandboxrc file is just now being created).
 * @param backdir The backing directory of the sandbox.
 * @param basedir The base directory of the sandbox.
 * basedir can contain variables.  
 * @param is_default If true, this sandbox will become
 * the new default sandbox in the rc file.  If false, the
 * existing default will remain so (unless one does not
 * exist).
 */
boolean Sandbox::create( const StringArray &mksblist, const String &backdir,
    const String &basedir, boolean is_default )
//    throws SandboxException
{
  boolean use_mksblist=false;
  String basedirEval;

  if (!sbrc->exists())
  {
    use_mksblist = true; // only use mksblist for first sandbox
    is_default = true; // first sandbox is always default
  }
  if (sandbox == StringConstants::EMPTY_STRING)
    throw (SandboxException( "No sandbox name given" ));
  if (sbrc->sandboxExists( sandbox ))
    throw (SandboxException( String( "Sandbox " ) + sandbox +
        " already exists" + " in " + sbrc->getPathname() ));
  basedirEval = Variable::envVarEval( basedir );
  // evaluate basedir and save in basedirEval, then do
  // basedirEval.canonicalizeThis(), and do tests below on
  // canonicalized version.
  if (!Path::exists( basedirEval ) ||
      !Path::isDirectory( basedirEval ))
    throw (SandboxException( String( "Sandbox base (" ) + basedirEval +
        ") is nonexistent or not a directory" ));
  this->basedir = basedirEval;
  this->basedirUneval = basedir;
  Path::canonicalizeThis( this->basedir );
  sbdir = this->basedir + Path::DIR_SEPARATOR + sandbox;

  // when creating the sbrc entry, don't use canonicalized basedir,
  // so that OS2/NT users can avoid specifying the drive letter (and
  // thereby allow runtime drive mapping) and so that it can contain
  // variables, which add() should store unevaluated.
  if (!sbrc->add( sandbox, basedir, is_default ))
    throw (SandboxException(
        String( "Couldn't create/change the sandboxrc" ) +
        " file (" + sbrc->getPathname() + ")" ));

  // if sbrc doesn't yet contain the mksb lines, makes sure
  // we create them now.
  if (!use_mksblist)
  {
    StringArray mksb_check;
    sbrc->getMKSBList( &mksb_check );
    if (mksb_check.size() < 1)
      use_mksblist = true;
  }

  if (use_mksblist && mksblist.length() > 0)
    sbrc->setMKSBList( mksblist );

  try
  {
    createRCFiles( backdir );
  } catch (SandboxException &e)
  {
    sbrc->del( sandbox );
    throw;
  }
  return (true);
}

boolean Sandbox::createRCFiles( const String &back_dir )
//    throws SandboxException
{
  static Variable var_eval( Env::getSetVars(), false, '$' );
  StringArray eval_results( 2, 2 );
  boolean buildenv=true;

  try
  {
    var_eval.parseUntil( back_dir, "", true, false, &eval_results, 0 );
    Path::normalizeThis( eval_results[ARRAY_FIRST_INDEX] );
  }
  catch (ParseException &e)
  {
    throw (SandboxException( "Backing dir (" + back_dir + ") is invalid." ));
  }

  const String &backdir = eval_results[ARRAY_FIRST_INDEX];

  if (backdir == StringConstants::EMPTY_STRING)
  {
    is_backed = false;
  }
  else
  {
    is_backed = true;
    if (!Path::exists( backdir ))
      throw (SandboxException(
          String( "Backing directory (" ) + backdir +
          ") does not exist" ));
    if (!Path::isDirectory( backdir ))
      throw (SandboxException(
          String( "Backing directory (" ) + backdir +
          ") is not a directory" ));
    if (!Path::absolute( backdir ))
      throw (SandboxException(
          String( "Backing directory (" ) + backdir +
          ") must be an absolute path" ));

    try
    {
      SetVars *test = getBackedSbconfVars( backdir );
      if (test != 0)
      {
        const String *buildenv_var = test->get( SandboxConstants::BUILDENV_VAR );
        buildenv = (buildenv_var == 0) ? true :
            BOOLVAR_ON.equalsIgnoreCase( *buildenv_var );
        delete test;
      }
    } catch (SandboxException &e)
    {
      // maybe do something more meaningful later
    }
  }
  if (!createSbconfFile( back_dir, buildenv ))
    throw (SandboxException( "Couldn't create sb.conf file" ));
  return (true);
}


boolean Sandbox::createSbconfFile( const String &backdir, boolean buildenv )
{
  delete var_store;
  var_store = new SetVars( new Env(), true, true );
  String sbconf_path( sbdir );
  sbconf_path += Path::DIR_SEPARATOR;
  sbconf_path += SandboxConstants::getRCFILES_DIR();
  sbconf_path += Path::DIR_SEPARATOR;
  sbconf_path += SandboxConstants::SBCONF_NAME;

  sbconf = new SetVarConfigFile( sbconf_path, *var_store );

  SetVarConfigFileData data( "", false, true, false, "" );
  if (backdir != StringConstants::EMPTY_STRING)
  {
    data.value = backdir;
    Path::unixizeThis( data.value );
    sbconf->change( SandboxConstants::BACKING_BUILD_VAR, data );
  }
  data.value = (buildenv) ? BOOLVAR_ON : BOOLVAR_OFF;
  return (sbconf->change( SandboxConstants::BUILDENV_VAR, data ));
}

/**
 * Remove a sandbox.  First removes the sandbox from
 * the .sandboxrc config file (which may cause the
 * default sandbox to change, if this sandbox used to
 * be the default), then deletes the entire sandbox
 * directory structure.
 *
 * @return True on success, false on failure.
 */
boolean Sandbox::remove()
{
  if (sbrc->del( sandbox ))
  {
    if (Path::deletePath( sbdir, true, true ))
      return (true);
    Interface::printWarning( CommandLine::getProgramName() + ": " +
                             "Couldn't remove all files from the sandbox." );
    Interface::printWarning( getSandboxBase() + " must be deleted manually." );
  }
  else
  {
    Interface::printWarning( CommandLine::getProgramName() + ": " +
        String( "Don't have write access to " ) +
        sbrc->getPathname() ); 
  }
  return (false);
}

void Sandbox::readAll()
//    throws SandboxException
{
  if (Interface::isVerbose())
    Interface::printVerbose( "Checking/reading config files for sandbox " +
                             getSandboxName() );
  readSbconf();
  if (isBuildEnv())
    readBuildconf();
}


SetVars *Sandbox::getBackedSbconfVars( const String &backdir ) const
//    throws SandboxException
{
  return (new SetVars(
      SbconfReader( backdir, true, allow_recursion ).getLocalVars() ));
}

SetVarConfigFile *Sandbox::getBackedSbconf( const String &backdir ) const
//    throws SandboxException
{
  static SbconfReader *sr = 0;
  delete sr;
  sr = new SbconfReader( backdir, false, allow_recursion );
  return (sr->getSbconf());
}

void Sandbox::readSbconf()
//    throws SandboxException
{
  String backdir_var;
  if (sbconf_reader == 0)
  {
    if (Interface::isVerbose())
      Interface::printVerbose( "Checking/reading sb.conf file for sandbox " +
                               getSandboxName() );
    sbconf_reader = new SbconfReader( sbdir, false, allow_recursion );
  }
  sbconf_reader->readChainBackward();
  backing_chain = sbconf_reader->getBackingChain();
  backing_chain_array = sbconf_reader->getBackingChainArray();
  SandboxConstants::setBACKED_SANDBOXDIR( Path::userize( backing_chain ) );
  is_backed = (backing_chain_array.length() > 1);
  sbconf = sbconf_reader->getSbconf();
  StringArray vars;
  sbconf_reader->getLocalVars().get( false, &vars );
  sbconf_vars.put( vars );
  vars.clear();
  sbconf_reader->getGlobalVars().get( false, &vars );
  env_vars.put( vars );
}

boolean Sandbox::isNonRecursiveBackingDir( const String &path ) const
{
  try
  {
    SbconfReader tester( path, false, false );
  }
  catch (SandboxException &e)
  {
    return (e.errorId() != SbconfReader::RECURSIVE_ERROR_CODE);
  }

  return (true);
}

/*
 * It looked like this thing should have the side effects of putting
 * the BuildListConfigFile object in Sandbox::buildlist.
 * It also looked like sometimes the side effect is to set build_list_path,
 * but not always :-(  So change it to always set build_list_path where
 * it appears appropriate, and maybe the public getBuildListPath() will
 * work correctly. 
 * (Also remove the local variable sandbox, which was not used for anything.)
 */
void Sandbox::readBuildList()
{
  static Variable var_eval( Env::getSetVars(), false, '$' );
  StringArray eval_results( 2, 2 );
  const String *pathnameptr;
  String pathname;

  if ((pathnameptr = Env::getenv( SandboxConstants::BUILDLIST_PATH_VAR )) == 0)
  {
    if ((pathname = getBuildListPath( this->sandbox )) ==
        StringConstants::EMPTY_STRING &&
        (this->sandbox == StringConstants::EMPTY_STRING ||
        !this->sandbox.equals( sbrc->getDefaultSandbox() )))
      pathname = getBuildListPath( sbrc->getDefaultSandbox() );
  }
  else
  {
    build_list_path = *pathnameptr; // side effect
    try
    {
      var_eval.parseUntil( *pathnameptr, StringConstants::EMPTY_STRING,
          true, false, &eval_results, 0 );
      Path::normalizeThis( eval_results[ARRAY_FIRST_INDEX] );
      pathname = eval_results[ARRAY_FIRST_INDEX];
    }
    catch (ParseException &e)
    {
      pathname = StringConstants::EMPTY_STRING;
    }
  }

  if (pathname != StringConstants::EMPTY_STRING)
  {
    if (Interface::isVerbose())
      Interface::printVerbose(
              String( "Checking/reading Buildlist config file " ) + pathname );
    buildlist = new BuildListConfigFile( pathname );
  }
}

/**
 * This should return the unevaluated version of
 * the build list pathname (i.e., it should contain
 * any variable specifications found in sb.conf).
 *
**/
String Sandbox::getBuildListPath() const
{
  return (build_list_path);
}

/* 
 * Return the path to the build list file, and as a side effect
 * it should set build_list_path as well.
 */
String Sandbox::getBuildListPath( const String &sandbox )
{
  const String *pathnameptr = 0;
  String sandbox_dir, pathname;

  if (sandbox == StringConstants::EMPTY_STRING)
    return (StringConstants::EMPTY_STRING);
  sandbox_dir = sbrc->getSandboxBase( sandbox );
  if (sandbox_dir == StringConstants::EMPTY_STRING)
    return (StringConstants::EMPTY_STRING);
  sandbox_dir += Path::DIR_SEPARATOR + sandbox;
  SetVarConfigFile *svcf = getBackedSbconf( sandbox_dir );
  if (svcf != 0)
  {
    obtainBuildListPath( *svcf );
    SetVars sv;
    svcf->getLocalVars( sv );
    if ((pathnameptr = sv.get( SandboxConstants::BUILDLIST_VAR )) != 0)
      pathname = *pathnameptr;
    delete svcf;
  }

  return (pathname);
}

/*
 * If build_list is one of the variables in the sb.conf file, return a
 * pointer to its value, otherwise return 0
 */
const String *Sandbox::getSandboxBuildListName() const
{
  const String *resultptr = sbconf_vars.get( SandboxConstants::BUILDLIST_VAR );
  return resultptr;
}

/*
 * Side effect is to set build_list_path from the "build_list" entry
 * (if any) found in some sb.conf in the backing chain, presumably
 * the one for the backing build.  svcf has the local vars
 * that come from the sb.conf files along the backing chain.
 */
void Sandbox::obtainBuildListPath( const SetVarConfigFile &svcf )
{
  SetVarsTemplate< SetVarConfigFileData > vars;
  svcf.getLocalVars( vars );
  const SetVarConfigFileData *data =
      vars.get( SandboxConstants::BUILDLIST_VAR );
  if (data != 0)
  {
    StringArray splitstr;
    data->value.split( StringConstants::SPACE_TAB, 2, &splitstr );
    build_list_path = splitstr[ARRAY_FIRST_INDEX];
  }
  else
    build_list_path = StringConstants::EMPTY_STRING;
}


void Sandbox::readBuildconf()
{
  SetVarConfigFile *cfg;
  String odeconfsdir = SandboxConstants::getSRCNAME();

  if (isConfsInRcfilesEnv())
  {
    odeconfsdir = SandboxConstants::getRCFILES_DIR();
    Env::putenv("_ODE_CONFS_IN_RCFILES=true");
  }

  String bcf=Path::findFileInChain( odeconfsdir + Path::DIR_SEPARATOR +
      SandboxConstants::BUILDCONF_NAME, backing_chain_array );
  String bcef=Path::findFileInChain( odeconfsdir + Path::DIR_SEPARATOR + 
      SandboxConstants::BUILDCONF_EXP_NAME, backing_chain_array );
  String bclf=sbdir + Path::DIR_SEPARATOR + odeconfsdir +
      Path::DIR_SEPARATOR + SandboxConstants::BUILDCONF_LOCAL_NAME;

  buildconf_vars.set( SandboxConstants::SANDBOX_BASE_VAR, Path::userize(
      backing_chain_array[backing_chain_array.lastIndex()] ), true );

  cfg = new SetVarConfigFile( bcf, buildconf_vars );
  if (cfg->exists())
  {
    if (Interface::isVerbose())
      Interface::printVerbose( String( "Reading Buildconf " ) + bcf +
                               " for sandbox " + getSandboxName() );
    cfg->getGlobalVars( env_vars );
  }
  else
    Interface::printWarning( CommandLine::getProgramName() +
        ": Unable to find Buildconf (" + bcf + ") for sandbox " +
        getSandboxName() );
  delete cfg;

  if (Path::exists( bcef ))
  {
    if (Interface::isVerbose())
      Interface::printVerbose( String( "Reading Buildconf.exp " ) + bcef +
                               " for sandbox " + getSandboxName() );
    for (int i = backing_chain_array.lastIndex() - 1;
        i >= backing_chain_array.firstIndex(); --i)
    {
      buildconf_vars.set( SandboxConstants::SANDBOX_BASE_VAR, Path::userize(
          backing_chain_array[i] ), true );
      cfg = new SetVarConfigFile( bcef, buildconf_vars );
      cfg->getGlobalVars( env_vars );
      delete cfg;
    }
  }
  else if (backing_chain_array.size() > 1)
    Interface::printWarning( CommandLine::getProgramName() +
        ": Unable to find Buildconf.exp (" + bcef + ") for sandbox " +
        getSandboxName() );
  buildconf_vars.set( SandboxConstants::SANDBOX_BASE_VAR, Path::userize(
      backing_chain_array[backing_chain_array.firstIndex()] ), true );

  if (Interface::isVerbose())
    Interface::printVerbose( String( "Reading Buildconf.local: " ) + bclf );
  cfg = new SetVarConfigFile( bclf, buildconf_vars );
  cfg->getGlobalVars( env_vars );
  delete cfg;
}


/**
 * Get the SandboxRCConfigFile object corresponding
 * to the sandboxrc file for this sandbox.  Should
 * probably check against null and exists() before
 * using.
 *
 * @return The SandboxRCConfigFile object.
 */
SandboxRCConfigFile *Sandbox::getSandboxRC() const
{
  if (sbrc == 0)
    return (0);
  return (new SandboxRCConfigFile( *sbrc ));
}


/**
 * Get the SetVarConfigFile object corresponding
 * to the sb.conf file for this sandbox.  Should
 * probably check against null and exists() before
 * using.
 *
 * @return The SetVarConfigFile object for sb.conf.
 */
SetVarConfigFile *Sandbox::getSbconf() const
{
  if (sbconf == 0)
    return (0);
  return (new SetVarConfigFile( *sbconf ));
}

/**
 * Get the BuildListConfigFile object corresponding
 * to the build_list file for this sandbox.  Should
 * probably check against null and exists() before
 * using.
 *
 * @return The BuildListConfigFile object.
 */
BuildListConfigFile *Sandbox::getBuildList() const
{
  if (buildlist == 0)
    return (0);
  return (new BuildListConfigFile( *buildlist ));
}

/**
 * Returns a copy of the list of sb.conf
 * local variables.
 *
 * @return A new SetVars object (with no parent)
 * which contains all sb.conf local variables.
 */
const SetVars &Sandbox::getSbconfLocals() const
{
  return (sbconf_vars);
}

/**
 * Returns a copy of the list of all Buildconf*
 * local variables.
 *
 * @return A new SetVars object (with no parent)
 * which contains all Buildconf* local variables.
 */
const SetVars &Sandbox::getBuildconfLocals() const
{
  return (buildconf_vars);
}

/**
 * Returns a copy of the list of all sb.conf and
 * Buildconf* environment variables.
 *
 * @return A new SetVars object (with no parent)
 * which contains all environment variables set
 * in sb.conf and Buildconf*.
 */
const SetVars &Sandbox::getEnvs() const
{
  return (env_vars);
}

/**
 * Returns a hierarchical variable list of all variables
 * read from the config files.  Intended only to be used
 * with the find() method to determine the value of any
 * variable.
 *
 * The object returned consists of sb.conf local
 * variables.  Its parent is an object consisting of
 * all Buildconf* local variables.  The grandparent
 * is the environment variable list that was set by
 * sb.conf and Buildconf* (this is normally a small
 * subset of the full Env list, of course).  The
 * great-grandparent is the full Env list.  Note that
 * it is not possible to determine which files the
 * environment variables came from, nor to determine
 * which Buildconf file that the parent object's
 * variables came from.
 *
 * @return Hierarchical list of sb.conf, Buildconf,
 * Buildconf.exp, and Buildconf.local variables
 * (local and environment).  This list is a separate
 * copy, not the one used internally (so it is both
 * potentially dangerous [to cast to SetVars, since
 * there's no guarantee that it's the actual underlying
 * object type] and useless to modify these objects).
 *
 * The user need only deallocate (with delete) the returned
 * pointer...all parents will be deallocated as needed
 * automatically.
 */
SetVarLinkable *Sandbox::getVariables() const
{
  SetVars *env_list = new SetVars( env_vars );
  SetVars *build_list = new SetVars( buildconf_vars );
  SetVars *sb_list = new SetVars( sbconf_vars );

  env_list->setParent( new Env(), true );
  build_list->setParent( env_list, true );
  sb_list->setParent( build_list, true );
  return (sb_list);
}

/**
 * Get the backing directory of the sandbox, as specified
 * in sb.conf.
 */
const String *Sandbox::getBackingDir() const
{
  return (sbconf_vars.get( SandboxConstants::BACKING_BUILD_VAR ));
}

/**
 * Set the backing directory of the sandbox, which
 * is stored in sb.conf.
 */
boolean Sandbox::setBackingDir( const String &dir )
{
  String canonical;

  if (dir == StringConstants::EMPTY_STRING)
    return (false);
  if (Path::isSamePath( dir, sbdir ))
    return (false);
  try
  {
    if (!createRCFiles( dir ))
      return (false);
  } catch (SandboxException &e)
  {
    return (false);
  }
  SetVarConfigFileData data( Path::unixize( dir ), false, true, false,
      StringConstants::EMPTY_STRING );
  return (sbconf->change( SandboxConstants::BACKING_BUILD_VAR, data ));
}

/**
 * Check if user wants to use ODE build environment (i.e.,
 * Buildconf settings), as specified in sb.conf.
 */
boolean Sandbox::isBuildEnv() const
{
  const String *buildenv_var = sbconf_vars.get(
      SandboxConstants::BUILDENV_VAR );
  if (buildenv_var == 0)
    return (true);
  return (BOOLVAR_ON.equalsIgnoreCase( *buildenv_var ));
}

/**
 * Set whether or not user wants to use ODE build environment,
 * which is stored in sb.conf.
 */
boolean Sandbox::setBuildEnv( boolean is_build_env )
{
  SetVarConfigFileData data( (is_build_env) ? BOOLVAR_ON : BOOLVAR_OFF,
      false, true, false, "" );
  return (sbconf->change( SandboxConstants::BUILDENV_VAR, data ));
}


/**
 * Check if user wants to specify ode config files
 * under rc_files directory
 * as specified in sb.conf.
 */
boolean Sandbox::isConfsInRcfilesEnv() const
{
  const String *conf_var = sbconf_vars.get(
      SandboxConstants::CONFSINRCFILES_VAR );
  if (conf_var == 0)
    return (false);
  return (BOOLVAR_ON.equalsIgnoreCase( *conf_var ));
}

/**
 * Set whether or not user wants to specify ode config
 * files under rc_files directory,
 * which is stored in sb.conf.
 */
boolean Sandbox::setConfsInRcfilesEnv( boolean is_confsinrcfiles_env )
{
  SetVarConfigFileData data( (is_confsinrcfiles_env) ? BOOLVAR_ON : BOOLVAR_OFF,
      false, true, false, "" );
  return (sbconf->change( SandboxConstants::CONFSINRCFILES_VAR, data ));
}

/**
 * See if we're in a sandbox or a backing build.
 *
 * @return True if sb.conf contained a valid backing_build
 * specifier, false otherwise.
 */
boolean Sandbox::isBacked() const
{
  return (is_backed);
}

/**
 * Get the name of the sandbox.
 */
String Sandbox::getSandboxName() const
{
  return (sandbox);
}

/**
 * Get the base directory of the sandbox (does
 * NOT include the sandbox name in the path).
 */
String Sandbox::getSandboxBaseDir() const
{
  return (basedir);
}

/**
 * Get the unevaluated base directory of the sandbox
 * (it may not be canonicalized and may contain variables)
 */
String Sandbox::getSandboxBaseDirUneval() const
{
  return (basedirUneval);
}

/**
 * Get the base directory of the sandbox (DOES
 * include the sandbox name in the path).
 */
String Sandbox::getSandboxBase() const
{
  return (sbdir);
}

/**
 * Get the sandbox rc filename (does not include
 * the path).
 */
String Sandbox::getSandboxRCName() const
{
  return (Path::fileName( sbrc->getPathname() ));
}

/**
 * Get the sandbox rc base directory (does not include
 * the filename).
 */
String Sandbox::getSandboxRCBase() const
{
  return (Path::filePath( sbrc->getPathname() ));
}

/**
 * Get the backing chain (starting with the sandbox
 * directory) as an array.
 */
StringArray *Sandbox::getBackingChainArray( StringArray *buf ) const
{
  StringArray *rc = (buf == 0) ? new StringArray() : buf;
  *rc = backing_chain_array;
  return (rc);
}

/**
 * Get the backing chain (starting with the sandbox
 * directory) as a Path::PATH_SEPARATOR-separated
 * list in a single string.
 */
String Sandbox::getBackingChain() const
{
  return (backing_chain);
}


/**
 * Check if the current setting of the CONTEXT environment
 * variable is valid (i.e., is in the user-specified
 * machine_list list).
 *
 * @return True if machine is valid, false if not.
 */
boolean Sandbox::verifyMachine() const
{
  return (verifyMachine( "", false ));
}

/**
 * Check if the current setting of the CONTEXT environment
 * variable is valid (i.e., is in the user-specified
 * machine_list list).  If backdir is null, check in the
 * current sandbox (not always available to mksb).
 *
 * @param backdir The build directory in which to
 * start reading the sb.conf chain (which is where
 * the machine_list variable is located).
 * @param multiple If true, the CONTEXT environment
 * variable will be considered to be a colon-separated
 * list of machines (instead of limiting it to one).
 * If false, CONTEXT is expected to contain
 * a single machine.
 * @return True if (all) machine(s) is (are) valid,
 * false if not.
 */
boolean Sandbox::verifyMachine( const String &backdir,
    boolean multiple ) const
{
  const String *test_machine = Env::getenv( SandboxConstants::CONTEXT_VAR );
  String machine_list = getMachineList( backdir );
  StringArray machines, test_machines;
  int i;

  machine_list.split( ": ", 0, &machines );

  if (test_machine == 0 || machine_list.length() < 1)
    return (false);

  if (multiple)
    test_machine->split( ": ", 0, &test_machines );
  else
    test_machines += *test_machine;

  for (int j = test_machines.firstIndex();
      j <= test_machines.lastIndex(); ++j)
  {
    for (i = machines.firstIndex(); i <= machines.lastIndex(); ++i)
      if (machines[i].equals( test_machines[j] ))
        break;
    if (i > machines.lastIndex())
      return (false);
  }
  return (true);
}

/**
 * Get the value of machine_list (from sb.conf).
 *
 * @return The value of the machine_list variable
 * (or the empty string if it wasn't found for some reason).
 */
String Sandbox::getMachineList() const
{
  return (getMachineList( "" ));
}

/**
 * Geared for the mksb command, which often cannot
 * provide the amount of information required at
 * construction time to read the sb.conf chain.
 * There is one variable, machine_list, that is
 * important for it to be able to retrieve.
 * For other commands, to look in the sb.conf of
 * the current sandbox, pass null as the backdir
 * parameter.
 *
 * @param backdir The build directory in which to
 * start reading the sb.conf chain.
 * @return The value of the machine_list variable
 * (or the empty string if it wasn't found for some reason).
 */
String Sandbox::getMachineList( const String &backdir ) const
{
  const String *resultptr;
  String result;

  if (backdir == StringConstants::EMPTY_STRING)
  {
    if ((resultptr = sbconf_vars.get(
        SandboxConstants::MACHINELIST_VAR )) != 0)
      result = *resultptr;
  }
  else
  {
    try
    {
      SetVars *sbcv = getBackedSbconfVars( backdir );
      if (sbcv != 0)
      {
        if ((resultptr = sbcv->get(
            SandboxConstants::MACHINELIST_VAR )) != 0)
          result = *resultptr;
        delete sbcv;
      }
    } catch (SandboxException &e)
    {
    }
  }
  return (result);
}

boolean Sandbox::sbExists() const
{
  return (sandbox != StringConstants::EMPTY_STRING && sbrc != 0 &&
      sbrc->exists() && sbrc->sandboxExists( sandbox ));
}

boolean Sandbox::isLegalSandboxName( const String &sbname )
{
  if (sbname[STRING_FIRST_INDEX] == '-' ||
      sbname[STRING_FIRST_INDEX] == '~' ||
      sbname.equals( "." ) || sbname.equals( ".." ) || sbname.equals( "..." ))
    return (false);

  for (int i = STRING_FIRST_INDEX; i <= sbname.lastIndex(); ++i)
    if (!isalpha( sbname[i] ) && !isdigit( sbname[i] ) &&
        sbname[i] != '_' && sbname[i] != '-' && sbname[i] != '.' &&
        sbname[i] != '~')
      return (false);

  return (true);
}
