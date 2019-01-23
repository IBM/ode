using namespace std;
#define _ODE_BIN_MKSB_MKSB_CPP_


#include "bin/mklinks/mklinksc.hpp"
#include "bin/mksb/mksbc.hpp"
#include "bin/mklinks/mklinkex.hpp"
#include "lib/io/path.hpp"
#include "lib/portable/runcmd.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strcon.hpp"
#include "lib/string/variable.hpp"
#include "lib/exceptn/sboxexc.hpp"
#include "lib/exceptn/parseexc.hpp"

#ifdef DEFAULT_SHELL_IS_VMS
#define INST_IMAGES_NAME "inst_images"
#else
#define INST_IMAGES_NAME "inst.images"
#endif

/******************************************************************************
 *
 */
int Mksb::classMain( const char **argv, const char **envp )
{
  Mksb rb( argv, envp );
  return( rb.run() );
}


/******************************************************************************
 *
 */
int Mksb::run()
{
  checkCommandLine();
  String build_list;
  if ((build_list = cmdLine->getQualifiedVariable( "-blist", 1 )) !=
      StringConstants::EMPTY_STRING)
    {
      Env::setenv( SandboxConstants::BUILDLIST_PATH_VAR, build_list, true );
      sandbox_blist = true;
    }
  else
    sandbox_blist = false;
  checkOperationMode( false );

  return (0);
}


/******************************************************************************
 * mksb can be run in three modes - regular, list and undo modes.
 * This method decides which mode to run in.
 */
void Mksb::checkOperationMode( boolean make_backing_build )
{
  String sb_name;

  making_backing_build = make_backing_build;
  program_name = (making_backing_build) ? "mkbb" : "mksb";

  if (cmdLine->isState( "-list" ))
    listSandboxes();
  else if (cmdLine->isState( "-undo" ))
    deleteSandbox();
  else if (cmdLine->isState( "-upgrade" ))
    upgradeSandbox();
  else
    makeSandbox();
}


/******************************************************************************
 *
 */
void Mksb::listSandboxes() const
{
  String              rcfileName = cmdLine->getRCFile();
  SandboxRCConfigFile *rcfile;

  if (rcfileName == StringConstants::EMPTY_STRING)
    rcfile = new SandboxRCConfigFile();
  else
    rcfile = new SandboxRCConfigFile( Path::filePath( rcfileName ),
                                      Path::fileName( rcfileName ) );

  if (rcfile == 0)
    Interface::quitWithErrMsg( program_name +
      ": Sandbox RC file `" + rcfileName + "' not found", 1 );

  StringArray sandboxes;
  rcfile->getSandboxList( &sandboxes ); // get all the sandboxes

  if (sandboxes.length() != 0)  // if we do have any sandboxes
  {             // first print out info about the default sandbox
    String basedirUneval =
               rcfile->getSandboxBase( rcfile->getDefaultSandbox(), false );
    String basedir = rcfile->getSandboxBase( rcfile->getDefaultSandbox() );
    Interface::printAlways( "Default Sandbox: " + rcfile->getDefaultSandbox() );
    if (basedirUneval.indexOf( '$' ) == STRING_NOTFOUND)
      Interface::printAlways( "           Base: " + basedir );
    else
    {
    // if base has variables, print it in non-evaluated form too
      Interface::printAlways( "  Variable Base: " + basedirUneval );
      Interface::printAlways( "           Base: " + basedir );
    }

                     // now print out info for the remaining sandboxes
    for (int count = sandboxes.firstIndex();
         count <= sandboxes.lastIndex(); count++)
    {
      if (!(sandboxes[count] == rcfile->getDefaultSandbox()))
      {
        basedirUneval = rcfile->getSandboxBase( sandboxes[count], false );
        basedir = rcfile->getSandboxBase( sandboxes[count]);
        Interface::printAlways(   "        Sandbox: " + sandboxes[count] );
        if (basedirUneval.indexOf( '$' ) == STRING_NOTFOUND)
          Interface::printAlways( "           Base: " + basedir );
        else
        {
          Interface::printAlways( "  Variable Base: " + basedirUneval );
          Interface::printAlways( "           Base: " + basedir );
        }
      }
    }
  }
  else
  {
    if (rcfile->exists())
      Interface::quitWithErrMsg( program_name +
        ": Sandbox RC file `" + rcfileName + "' not in proper format", 1 );
    else
      Interface::quitWithErrMsg( program_name +
        ": Sandbox RC file `" + rcfileName + "' not found", 1 );
  }

  delete rcfile;
}


/******************************************************************************
 *
 */
void Mksb::deleteSandbox()
{
  boolean success = true;
  const String *backing_dir_ptr;
  SandboxRCConfigFile *sbrc = 0;
  int sbCount = 0;
  int lastSBCnt = 0;

  if (cmdLine->isState( "-info" ))
  {
    Interface::printAlways( "Would delete sandbox.");
    return;
  }

  // get sandbox names from command line.
  // note: multiple sandboxes can be deleted.
  // if no sandboxes on command line - display list.
  StringArray sbName;         // Array for qualified and unqualified variables
  StringArray qual_SBName;    // Array for qualified variables
  StringArray unQual_SBName;  // Array for unqualified variables

  cmdLine->getQualifiedVariables( "-sb", &qual_SBName );
  cmdLine->getUnqualifiedVariables( &unQual_SBName );

  if (qual_SBName.length() == 0 && unQual_SBName.length() == 0)
  {
    getSbListFromRCFile( &unQual_SBName );
    if (unQual_SBName.length() == 0)
      Interface::quitWithErrMsg( program_name +
          ": No sandboxes found to undo", 1 );
  }

  // if a (qualified and unqualified) sandbox specified on the command line
  if ( (qual_SBName.length() != 0) && (unQual_SBName.length() != 0) )
  {
    for (sbCount = qual_SBName.firstIndex();
        sbCount <= qual_SBName.lastIndex(); sbCount++)
      sbName.add( qual_SBName[sbCount] );

    for (sbCount = unQual_SBName.firstIndex();
        sbCount <= unQual_SBName.lastIndex(); sbCount++)
      sbName.add( unQual_SBName[sbCount] );

    sbCount   = qual_SBName.firstIndex();
    lastSBCnt = qual_SBName.lastIndex() + unQual_SBName.lastIndex();
  }
  else if (qual_SBName.length() != 0)
  {
    for (sbCount = qual_SBName.firstIndex();
        sbCount <= qual_SBName.lastIndex(); sbCount++)
      sbName.add( qual_SBName[sbCount] );

    sbCount   = qual_SBName.firstIndex();
    lastSBCnt = qual_SBName.lastIndex();
  }
  else if (unQual_SBName.length() != 0)
  {
    for (sbCount = unQual_SBName.firstIndex();
        sbCount <= unQual_SBName.lastIndex(); sbCount++)
      sbName.add( unQual_SBName[sbCount] );

    sbCount   = unQual_SBName.firstIndex();
    lastSBCnt = unQual_SBName.lastIndex();
  }

  for (/* use preset value of sbCount */; sbCount <= lastSBCnt; sbCount++)
  {
    try
    {
      Sandbox sbox( false, cmdLine->getRCFile(),
                    sbName[sbCount], false, true, true );

      backing_dir_ptr = sbox.getBackingDir();
      delete sbrc;
      sbrc = sbox.getSandboxRC();

      if (!sbrc->sandboxExists( sbName[sbCount] ))
      {
        throw (SandboxException( String( "Sandbox " ) + sbName[sbCount] +
            " does not exist in sandbox RC file " + sbrc->getPathname() ));
      }
      else if (!Path::exists( sbox.getSandboxBase() ))
      {
        Interface::printWarning( program_name + ": the base directory of " +
            sbName[sbCount] + " doesn't exist...will only remove sandbox "
            "entry from RC file" );
        if (Interface::getConfirmation( "Delete sandbox <" +
            sbName[sbCount] + ">? {y/[n]}: ", false ))
          sbrc->del( sbName[sbCount] );
      }
      else
      {
        checkDependencyErrors( sbox, sbrc );
        if (Interface::getConfirmation( "Delete sandbox <" +
            sbName[sbCount] + ">? {y/[n]}: ", false ))
        {
          if (inSandbox( sbox ))
          {
            Interface::printWarning( "You are in the sandbox to be deleted." );
            if (!Interface::getConfirmation( "Delete anyway? {y/[n]}", false ))
              continue;
            if (Interface::isVerbose())
              Interface::printVerbose( "Changing dir to sandbox base: " +
                  sbox.getSandboxBaseDir() );
  
            Path::setcwd( sbox.getSandboxBaseDir() );
          }
          if (!sbox.remove())
            success = false;
          removeFromBuildList( sbox );
        }
      }
    }
    catch (SandboxException &se)
    {
      Interface::printError( program_name + ": " + se.getMessage() );
      success = false;
    }
  }

  if (!success)
    Interface::quit( 1 );
}


/******************************************************************************
 * Check for sandboxes that have sbox in their backing chains.
 * checkDependencyErrors returns true if there was an error detected,
 * such as dependency problems or a cycle being detected in any
 * sandbox in the .sandboxrc file.
 * Messages are output if errors are detected.
 */
boolean Mksb::checkDependencyErrors( const Sandbox &sbox,
                                     const SandboxRCConfigFile *sbrc ) const
{
  String              rcfileName = cmdLine->getRCFile();
  StringArray         *rcSandboxes = new StringArray();
  String              sbname = sbox.getSandboxName();
  SmartCaseString     sbbase = Path::normalize( sbox.getSandboxBase() );
  StringArray         *chain = new StringArray();
  SmartCaseString     normChainSb;
  SmartCaseString     normRCSandbox;
  boolean             gotError = false;

  if (sbrc->exists())
  {
    String rcName = sbrc->getPathname();
    rcSandboxes->clear();
    sbrc->getSandboxList( rcSandboxes ); // get all the sandboxes

    if (rcSandboxes->length() != 0)
    {
      for (int i = rcSandboxes->firstIndex();
           i <= rcSandboxes->lastIndex(); ++i)
      {
        if ((*rcSandboxes)[i] == sbname)
          continue;
        try
        {
          Sandbox rcSandbox( false, rcName, (*rcSandboxes)[i] );
          chain->clear();
          rcSandbox.getBackingChainArray( chain );
          for (int j = chain->firstIndex() + 1; j <= chain->lastIndex(); ++j)
          {
            normChainSb = Path::normalize( (*chain)[j] );
            if (normChainSb == sbbase)
            {
              normRCSandbox = Path::normalize( rcSandbox.getSandboxBase() );
              Interface::printWarning( "The sandbox to be deleted (" + sbbase +
                                       ") backs sandbox " + normRCSandbox );
              gotError = true;
            }
          } // end for j
        }
        catch (SandboxException &se)
        {
          Interface::printWarning( program_name + ": " + se.getMessage() );
          gotError = true;
        }
      } // end for i
    }
  }
  else
  {
    Interface::quitWithErrMsg( program_name +
      ": Sandbox RC file `" + sbrc->getPathname() + "' does not exist", 1 );
  }

  delete chain;
  delete rcSandboxes;
  return (gotError);
}


/******************************************************************************
 *
 */
boolean Mksb::removeFromBuildList( const Sandbox &sbox ) const
{
  BuildListConfigFile *build_list = 0;
  SandboxRCConfigFile *sboxrc = sbox.getSandboxRC();
  const String        *buildlist_name =
                        Env::getenv( SandboxConstants::BUILDLIST_PATH_VAR );
  const String        *sbconf_buildlist_name =
                        sbox.getSandboxBuildListName();

  if (sbconf_buildlist_name != 0 ||
      (build_list = sbox.getBuildList()) == 0)
  {
    if (buildlist_name == 0 && sbconf_buildlist_name == 0)
      return (true); // no build list used, so no error
    else
    {
      try
      {
        // use the build_list variable in the local sb.conf if it exists
        build_list = new BuildListConfigFile(
                       Variable::envVarEval( sbconf_buildlist_name ?
                                             *sbconf_buildlist_name :
                                             *buildlist_name ) );
      }
      catch (ParseException &e)
      {
        return (false);
      }
    }
  }

  if (!build_list->inBuildList( sbox.getSandboxName() ))
    return (true);
  if (build_list->del( sbox.getSandboxName() ))
  {
    if (sboxrc != 0)
      build_list->setDefaultBuild( sboxrc->getDefaultSandbox() );
    delete build_list;
    return (true);
  }

  Interface::printWarning( "Couldn't update build list file: " +
      build_list->getPathname() );
  delete build_list;
  return (false);
}


/**
 *
**/
String Mksb::addToBuildList( const Sandbox &sbox ) const
{
  String eval_results;
  BuildListConfigFile *build_list = 0;
  String buildlist_name;

  if ((build_list = sbox.getBuildList()) == 0)
  {
    const String *buildlist_name_ptr =
        Env::getenv( SandboxConstants::BUILDLIST_PATH_VAR );
    if (buildlist_name_ptr == 0)
      return (StringConstants::EMPTY_STRING);
    else
    {
      buildlist_name = *buildlist_name_ptr;
      try
      {
        eval_results = Variable::envVarEval( *buildlist_name_ptr );
        // if no variables were in the string, get the full path
        if (buildlist_name == eval_results)
          Path::canonicalizeThis( buildlist_name, false );
        build_list = new BuildListConfigFile( eval_results );
      }
      catch (ParseException &e)
      {
        return (StringConstants::EMPTY_STRING);
      }
    }
  }
  else
  {
    buildlist_name = sbox.getBuildListPath();
    if (buildlist_name == StringConstants::EMPTY_STRING)
      buildlist_name = build_list->getPathname();
  }

  if (!build_list->add( sbox.getSandboxName(), sbox.getSandboxBaseDirUneval(),
      true, cmdLine->isState( "-def" ) ))
  {
    Interface::printWarning( "Couldn't update build list file: " +
        buildlist_name );
    buildlist_name = StringConstants::EMPTY_STRING;
  }

  delete build_list;
  return (buildlist_name);
}


/**
 *
**/
StringArray *Mksb::getSbListFromRCFile( StringArray *sbs ) const
{
  String              rcfileName = cmdLine->getRCFile();
  SandboxRCConfigFile *rcfile;
  StringArray         *sandboxes = (sbs == 0) ? new StringArray() : sbs;

  if (rcfileName == StringConstants::EMPTY_STRING)
    rcfile = new SandboxRCConfigFile();
  else
    rcfile = new SandboxRCConfigFile( Path::filePath( rcfileName ),
                                      Path::fileName( rcfileName ) );

  if (rcfile->exists())
  {
    sandboxes->clear();
    rcfile->getSandboxList( sandboxes ); // get all the sandboxes

    if (sandboxes->length() != 0)
    {
      Interface::printArray( *sandboxes, ":" );
      String msg( "Enter colon-separated list of sandboxes from above list: " );
      StringArray sbNames;
      Interface::getResponse( msg, false ).split( ":", UINT_MAX, &sbNames );
      *sandboxes = sbNames;
    }
  }
  else
  {
    Interface::quitWithErrMsg( program_name +
      ": Sandbox RC file `" + rcfile->getPathname() + "' does not exist", 1 );
  }

  delete rcfile;
  return sandboxes;
}


/******************************************************************************
 * Upgrade the sandbox from the ODE211 era to present.
 */
void Mksb::upgradeSandbox()
{
  String      sb_name;
  StringArray vars;
  cmdLine->getUnqualifiedVariables( &vars );

  if (vars.length() != 0) // if sandboxes specified on the command line
    sb_name = vars[vars.firstIndex()];

  try
  {
    sb = new Sandbox( true, cmdLine->getRCFile(),
        sb_name, true, false, true );
  }
  catch (SandboxException &se)
  {
    Interface::quitWithErrMsg( program_name + ": " + se.getMessage(), 1 );
    return;
  }

  String      machine_list;
  StringArray machines;
  Path::getDirContents( sb->getSandboxBase() + Path::DIR_SEPARATOR + "obj",
                        false, Path::CONTENTS_DIRS, &machines );

  if (machines.length() > 0)
    for (int i = machines.firstIndex(); i <= machines.lastIndex(); ++i)
      machine_list += machines[i] + ((i < machines.lastIndex())
          ? StringConstants::COLON
          : StringConstants::EMPTY_STRING);

  // get the sbconf object and check if the file exists
  if ((sb->getSbconf() != 0) && !sb->getSbconf()->exists())
  {
    if (cmdLine->isState( "-info" ))
    {
      Interface::printAlways( "Would upgrade sandbox " + sb->getSandboxName() );
      return;
    }

    if (making_backing_build)
    {
      if (getBackingDirForUpgrade() != StringConstants::EMPTY_STRING)
        Interface::quitWithErrMsg( program_name + ": " + sb->getSandboxName() +
            " is not a backing build...use mksb to upgrade it", 1 );

      sb->setBuildEnv( true );
      SetVarConfigFile *sbconf = sb->getSbconf();
      String buildlist_path = addToBuildList( *sb );

      if (buildlist_path != StringConstants::EMPTY_STRING)
        sbconf->change( SandboxConstants::BUILDLIST_VAR,
            SetVarConfigFileData( Path::unixize( buildlist_path ),
            false, true ) );

      if (machine_list != StringConstants::EMPTY_STRING)
        sbconf->change( SandboxConstants::MACHINELIST_VAR,
            SetVarConfigFileData( machine_list, false, true ) );

      delete sbconf;
    }
    else
    {
      String backing_dir = getBackingDirForUpgrade();

      if (backing_dir == StringConstants::EMPTY_STRING)
        Interface::quitWithErrMsg( program_name + ": " + sb->getSandboxName() +
            " is a backing build...use mkbb to upgrade it", 1 );

      sb->setBackingDir( backing_dir );
      sb->setBuildEnv( true );
    }

    createInstDirsForUpgrade();
  }
  else
  {
    if (making_backing_build)
    {
      if (machine_list != StringConstants::EMPTY_STRING)
      {
        SetVarConfigFile *sbconf = sb->getSbconf();

        if (sbconf != 0) // then it must exist
        {
          if (sb->getSbconfLocals().get(
              SandboxConstants::MACHINELIST_VAR ) == 0)
          {
            sbconf->change( SandboxConstants::MACHINELIST_VAR,
                SetVarConfigFileData( machine_list, false, true ) );
          }
          else
          {
            Interface::print( "Sandbox <" + sb->getSandboxName() +
                "> is current - can't upgrade anymore.");
          }
        }

        delete sbconf;
      }
    }
    else
      Interface::print("Sandbox <" + sb->getSandboxName() +
               "> is current - can't upgrade anymore.");
  }
}


/******************************************************************************
 *
 */
void Mksb::makeSandbox()
{
  String rcfile    = cmdLine->getRCFile();
  String backingDir;

  try
  {
    delete sb;

    sb = new Sandbox( false, rcfile,
                      getSbName(), false, true, true );

    if (sb->sbExists())
    {
      // Should check if the sandbox dir really exists. sbExists() effectively
      // only checks if the name is in the .sandboxrc file.
      String base = Path::userize( sb->getSandboxBase() );
      if (!Path::exists( base ))
      {
        throw SandboxException( "sandbox or backing build (" +
                                sb->getSandboxName() +
                                ") is in .sandboxrc but base directory (" +
                                base + ") does not exist." );
      }
      boolean legal_flag_found = addMachineToExistingSandbox();
      legal_flag_found |= makeExistingSandboxDefault();
      if (legal_flag_found)
        return;
      throw SandboxException( String( "Sandbox " ) + sb->getSandboxName() +
                              " already exists in sandbox RC file " +
                              Path::canonicalize( sb->getSandboxRCBase() ) +
                              Path::DIR_SEPARATOR + sb->getSandboxRCName() );
    }
  }
  catch (SandboxException &se)
  {
    Interface::quitWithErrMsg( program_name + ": " + se.getMessage(), 1 );
  }

  StringArray *mksbList = sb->getSandboxRC()->getMKSBList();
  String basedir = getBaseDirForMKSB( sb->getSandboxRC(), *mksbList );
  if (!making_backing_build)
  {
    String sandbox_full_path = Variable::envVarEval( basedir ) +
                               Path::DIR_SEPARATOR + sb->getSandboxName();
    backingDir = getBackingDir( &sandbox_full_path );
  }

  String machines;
  if (mksbList->length() < 1)
  {
    makeMKSBList( backingDir, basedir, mksbList );
    machines = fromMKSBList( *mksbList, "-m", 2 );
  }
  else
  {
    //$$$ check the offset - may have to be less by one
    machines = cmdLine->getQualifiedVariable( "-m", 1 );
    if (machines == StringConstants::EMPTY_STRING)
      machines = fromMKSBList( *mksbList, "-m", 2 );
    else
    {
      // remove duplicates - they look silly and may cause trouble some day
      StringArray machineArray;
      machines.split( ":", UINT_MAX, &machineArray );
      for (int i = machineArray.lastIndex();
           i > machineArray.firstIndex(); --i)
      {
        for (int j = i - 1; j >= machineArray.firstIndex(); --j)
        {
          if (machineArray[i] == machineArray[j])
          {
            machineArray.removeAtPosition(i);
            break;
          }
        }
      }
      machines = machineArray.join( ":" );
    }
    Env::setenv( SandboxConstants::CONTEXT_VAR, machines, true );
    getMachinesForMKSB( machines, backingDir, sb->getSandboxRC() );
  }

  if (cmdLine->isState( "-info" ))
  {
    Interface::printAlways( "Would create sandbox " + sb->getSandboxName() );
    return;
  }

  Interface::print( "Creating sandbox..." );

  try
  {
    sb->create( *mksbList, backing_dir_uneval, basedir,
        cmdLine->isState( "-def" ) );
  }
  catch (SandboxException &se)
  {
    Interface::quitWithErrMsg( program_name + ": Unable to create sandbox: " +
                               se.getMessage(), 1 );
  }

  buildDirStructure( machines ); // build the remaining dir structure

  if (making_backing_build)
  {
    sb->setBuildEnv( !cmdLine->isState( "-nobld" ) );
    SetVarConfigFile *sbconf = sb->getSbconf();
    String buildlist_path = addToBuildList( *sb );

    if (buildlist_path != StringConstants::EMPTY_STRING)
      sbconf->change( SandboxConstants::BUILDLIST_VAR,
          SetVarConfigFileData( Path::unixize( buildlist_path ),
              false, true ) );

    if (machines != StringConstants::EMPTY_STRING && machines.length() > 0)
      sbconf->change( SandboxConstants::MACHINELIST_VAR,
          SetVarConfigFileData( machines, false, true ) );

    delete sbconf;
  }
  else
  {
    if (sandbox_blist)
    {
      SetVarConfigFile *sbconf = sb->getSbconf();
      String buildlist_path = addToBuildList( *sb );

      if (buildlist_path != StringConstants::EMPTY_STRING)
        sbconf->change( SandboxConstants::BUILDLIST_VAR,
            SetVarConfigFileData( Path::unixize( buildlist_path ),
                false, true ) );
      delete sbconf;
    }
    if (!createLinks( *mksbList, rcfile, machines )) // setup links if specified
    {
      if (Interface::getConfirmation( "Could not create/link files. "
          "Delete sandbox <" + getSbName() + ">? {[y]/n}", true))
      {
        sb->remove();
        if (making_backing_build || sandbox_blist)
          removeFromBuildList( *sb );
      }

      Interface::quit( 1 );
    }
  }
}


/******************************************************************************
 *
 */
boolean Mksb::addMachineToExistingSandbox()
{
  String newMachines, allMachines;
  StringArray newArray, allArray;
  newMachines = cmdLine->getQualifiedVariable( "-m", 1 );
  if (newMachines != StringConstants::EMPTY_STRING)
  {
    sb->read();
    const String *backing_dir_ptr = sb->getBackingDir();
    if (making_backing_build && (backing_dir_ptr != 0 &&
        !backing_dir_ptr->equals( StringConstants::EMPTY_STRING )))
    {
      throw SandboxException( sb->getSandboxName() +
          " is not a backing build..." + "use mksb to add a machine" );
    }
    else if (!making_backing_build && (backing_dir_ptr == 0 ||
        backing_dir_ptr->equals( StringConstants::EMPTY_STRING )))
    {
      throw SandboxException( sb->getSandboxName() +
          " is a backing build..." + "use mkbb to add a machine" );
    }
    newMachines.split( ":", UINT_MAX, &newArray );
    allMachines = sb->getMachineList();
    allMachines.split( ":", UINT_MAX, &allArray );

    int i, j;
    if (making_backing_build)
    {
      // remove duplicate machines
      for (i = newArray.lastIndex();
           i >= newArray.firstIndex(); --i)
      {
        for (j = allArray.firstIndex();
             j <= allArray.lastIndex(); ++j)
        {
          if (newArray[i] == allArray[j])
          {
            newArray.removeAtPosition( i );
            break;
          }
        }
      }
      if (newArray.size() == 0)
        return true;
    }
    else
    {
      // check if new sandbox machines are defined in the backing build
      for (i = newArray.firstIndex();
           i <= newArray.lastIndex(); ++i)
      {
        boolean found = false;
        for (j = allArray.firstIndex();
             j <= allArray.lastIndex(); ++j)
        {
          if (newArray[i] == allArray[j])
          {
            found = true;
            break;
          }
        }
        if (!found)
          throw SandboxException( String( "New machine " ) + newArray[i] +
                                  " does not exist in backing build" );
      }
    }
    newMachines = newArray.join( ":" );
    Interface::print( String( "Adding machine(s) " + newMachines +
                              " to sandbox." ) );
    buildDirStructure( newMachines ); // build the remaining dir structures
    if (making_backing_build)
    {
      allMachines += ":" + newMachines;
      SetVarConfigFile *sbconf = sb->getSbconf();
      sbconf->change( SandboxConstants::MACHINELIST_VAR,
          SetVarConfigFileData( allMachines, false, true ) );

      delete sbconf;
    }
    return true;
  }
  return false;
}


/******************************************************************************
 *
 */
boolean Mksb::makeExistingSandboxDefault()
{
  if ( cmdLine->isState( "-def" ) )
  {
    sb->read();
    const String *backing_dir_ptr = sb->getBackingDir();
    if (making_backing_build && (backing_dir_ptr != 0 &&
        !backing_dir_ptr->equals( StringConstants::EMPTY_STRING )))
    {
      throw SandboxException( sb->getSandboxName() +
          " is not a backing build..." +
          "use mksb to make it the default sandbox" );
    }
    else if (!making_backing_build && (backing_dir_ptr == 0 ||
        backing_dir_ptr->equals( StringConstants::EMPTY_STRING )))
    {
      throw SandboxException( sb->getSandboxName() +
          " is a backing build..." +
          "use mkbb to make it the default sandbox" );
    }
    SandboxRCConfigFile *sbrc = sb->getSandboxRC();
    Interface::print( "Making sandbox " + sb->getSandboxName() +
                      " be the default." );
    sbrc->setDefaultSandbox( sb->getSandboxName() );
    delete sbrc;
    return true;
  }
  return false;
}


/******************************************************************************
 *
 */
String Mksb::getSbName() const
{
  StringArray qual_SBName;    // Array for qualified variables
  StringArray unQual_SBName;  // Array for unqualified variables

  cmdLine->getQualifiedVariables( "-sb", &qual_SBName );
  cmdLine->getUnqualifiedVariables( &unQual_SBName );

  // if more than one (qualified or unqualified) sandbox on the command line
  if( (qual_SBName.lastIndex() > 1) ||
      (unQual_SBName.lastIndex() > 1)  )
  {
     Interface::printError( CommandLine::getProgramName() +
              ": only one sandbox name can be given.");
     printUsage();
     Interface::quit(1);
  }

  // if a (qualified and unqualified) sandbox specified on the command line
  if ( (qual_SBName.length() != 0) && (unQual_SBName.length() != 0) )
  {
     Interface::printError( CommandLine::getProgramName() +
              ": only one sandbox name can be given.");
     printUsage();
     Interface::quit(1);
  }
  else if (qual_SBName.length() != 0)
      return qual_SBName[qual_SBName.firstIndex()];
  else if (unQual_SBName.length() != 0)
     return unQual_SBName[unQual_SBName.firstIndex()];
  else
  {
    String name = Interface::getResponse( "Enter sandbox name: ", true ).trim();
    // if -auto was specified on the command line, the above
    // query would have given us a null response.
    if (name == StringConstants::EMPTY_STRING)
      Interface::quitWithErrMsg( program_name + ": Cannot use the <-auto> "
          "option without sandbox name.", 1 );
    else
      return name;
  }

  return (StringConstants::EMPTY_STRING); // to appease the compiler
}


/******************************************************************************
 *
 */
String Mksb::getBackingDir( const String *sandbox_path_ptr )
{
  String              fs  = Path::DIR_SEPARATOR;
  String              dir = cmdLine->getQualifiedVariable( "-back", 1 );
  SandboxRCConfigFile *sbrc = sb->getSandboxRC();

  if ((dir == StringConstants::EMPTY_STRING) && (sbrc != 0) && !sbrc->exists())
  {
    do
    {
      dir = Interface::getResponse( "Enter absolute path to backing build: ",
          true ).trim();

      // if the auto option is used, dir will be null.
      if (dir == StringConstants::EMPTY_STRING)
        Interface::quitWithErrMsg( program_name + ": Cannot use the <-auto> "
            "option without backing build dir.", 1 );
    } while((dir = getProperBackingBuild( dir, 0, sandbox_path_ptr )) ==
        StringConstants::EMPTY_STRING);
  }
  else
  {
    // Get the build list file from the backing build of the default sandbox,
    // if there is any.
    String defaultSbName;
    Sandbox *defaultSb = 0;
    BuildListConfigFile *bldcf = 0;
    if ( sbrc != 0 && sbrc->exists()
         && (defaultSbName = sbrc->getDefaultSandbox()) !=
         StringConstants::EMPTY_STRING
         )
    {
      try
      {
        defaultSb = new Sandbox( false, cmdLine->getRCFile(),
                                 defaultSbName, true, false, true );
      }
      catch (SandboxException &se)
      {
        Interface::printWarning( program_name + ": " + se.getMessage() );
        Interface::printWarning(
                     "Default sandbox and buildlist could not be found." );
      }
      if (defaultSb != 0)
      {
        String bldcfPath = defaultSb->getBuildListPath();
        if (bldcfPath != StringConstants::EMPTY_STRING)
          bldcf = defaultSb->getBuildList();
      }
    }
    delete defaultSb;
    defaultSb = 0;
    if ((dir == StringConstants::EMPTY_STRING) && (sbrc != 0) &&
        sbrc->exists())
    {
      StringArray buildList;

      if (bldcf != 0)
        bldcf->getBuildList( &buildList );

      do
      {
        if (buildList.length() == 0)
        {
          dir = Interface::getResponse(
              "Enter absolute path to backing build: ", true ).trim();

          // if the auto option is used, dir will be null.
          if (dir == StringConstants::EMPTY_STRING)
            Interface::quitWithErrMsg( program_name + ": Cannot use the <-auto> "
                "option without backing build dir.", 1 );
        }
        else
        {
          Interface::printArrayWithDefault( buildList, " : ",
                                            buildList.firstIndex() );
          String msg( "Select one or enter absolute path to backing build: " );
          dir = Interface::getResponse( msg, false).trim();

          if (dir == StringConstants::EMPTY_STRING)
            dir = buildList[buildList.firstIndex()];
        }
      } while ((dir = getProperBackingBuild( dir, bldcf,
                                             sandbox_path_ptr )) == 
               StringConstants::EMPTY_STRING);
    }
    else if (dir != StringConstants::EMPTY_STRING)
    {
      dir = getProperBackingBuild( dir, bldcf, sandbox_path_ptr );

      if (dir == StringConstants::EMPTY_STRING)
      {
        do
        {
          dir = Interface::getResponse(
              "Enter absolute path to backing build: ", true ).trim();

          // if the auto option is used, dir will be null.
          if (dir == StringConstants::EMPTY_STRING)
            Interface::quitWithErrMsg( program_name + ": Cannot use the <-auto> "
                "option without backing build dir.", 1 );
        } while((dir = getProperBackingBuild( dir, bldcf,
                                              sandbox_path_ptr )) ==
            StringConstants::EMPTY_STRING);
      }
    }
    delete bldcf;
  }

  if (dir == StringConstants::EMPTY_STRING)
    Interface::quitWithErrMsg( program_name +
      ": Invalid backing build directory", 1 );

  delete sbrc;
  return dir;
}


/**
 * @param dir Backing build directory or name of build in build list,
 * with possible variables to expand.  After variable evaluation, if
 * it is an absolute path to an existing directory etc, that is returned.
 * Otherwise, dir is looked up in the build list file, if there is any,
 * to see if dir was a build name.  If so, its backing dir is returned.
 * @sandbox_path_ptr points to a String with the full path of the
 * sandbox, if it is desired to check if the name is in the backing chain
 * of the backing build.
 * @return backing dir after variables are evaluated, or empty string
 * if failure.
**/
String Mksb::getProperBackingBuild( const String &dir,
                                    const BuildListConfigFile *buildList,
                                    const String *sandbox_path_ptr )
{
  String backDir;
  String returnDir;

  backing_dir_uneval = dir;

  try
  {
    const String &dir_test = Variable::envVarEval( dir );
    // is it an absolute legal path?
    if (Path::absolute( dir_test ) && Path::exists( dir_test ) &&
        Path::isDirectory( dir_test ))
    {
      // well, ok, but is it really a sandbox directory?
      // NOTE: this doesn't check for "proper" contents of the rc_files
      // directory.  To be more robust, this would have to create a Sandbox
      // object for the the backing build.
      String check_dir = dir_test + Path::DIR_SEPARATOR +
          SandboxConstants::getRCFILES_DIR();
      if (Interface::isDebug())
        Interface::printDebug( "Checking to see if backing build "
            "subdir exists (" + check_dir + ")" );
      if (Path::isDirectory( check_dir ))
        returnDir = dir_test;
    }
    else if ((buildList != 0) && buildList->exists())
      if ((backDir = buildList->getBuildDir( dir_test )) !=
          StringConstants::EMPTY_STRING)
      {
        backing_dir_uneval = backDir + Path::DIR_SEPARATOR + dir;
        returnDir = Variable::envVarEval( backDir + Path::DIR_SEPARATOR +
                                          dir_test );
      }
    if (returnDir != StringConstants::EMPTY_STRING)
    {
      if (sandbox_path_ptr != 0)
      {
        try
        {
          if (SbconfReader::checkInChain( returnDir, *sandbox_path_ptr ))
          {
            Interface::printWarning( "Using " + returnDir +
                    " would introduce an infinite cycle in the backing chain." );
            return (StringConstants::EMPTY_STRING);
          }
        }
        catch (SandboxException &se)
        {
          Interface::printWarning( se.getMessage() );
          return (StringConstants::EMPTY_STRING);
        }
      }
      return returnDir;
    }
  }
  catch (ParseException &e)
  {
  }

  Interface::printWarning( "Invalid backing build path" );
  return (StringConstants::EMPTY_STRING);
}


/******************************************************************************
 * Create the MKSB list
 */
StringArray *Mksb::makeMKSBList( const String &backingDir,
    const String &basedir, StringArray *buffer ) const
{
  StringArray         *mksb = (buffer == 0) ? new StringArray() : buffer;
  SandboxRCConfigFile *sbrc = sb->getSandboxRC();
  String              dir;
  mksb->clear();

  // get the "-dir ..." line
  mksb->add( String( "-dir " + basedir) );

  // get the "-m ..." line
  String machine = cmdLine->getQualifiedVariable( "-m", 1 );
  if (machine == StringConstants::EMPTY_STRING)
  {
    const String *context = Env::getenv( SandboxConstants::CONTEXT_VAR );
    if (context == 0)
      machine = PlatformConstants::CURRENT_MACHINE;
    else
      machine = *context;
  }

  mksb->add( String( "-m " +
                     getMachinesForMKSB( machine, backingDir, sbrc ) ) );

  // get the "-tools ..." line
  mksb->add( String( "-tools " + getMode( "-tools" )) );

  // get the "-obj ..." line
  if ((dir = cmdLine->getQualifiedVariable( "-obj", 2 )) ==
      StringConstants::EMPTY_STRING) //get obj dir
    dir = "/";                                     // default dir
  mksb->add( String( "-obj " + getMode("-obj") + " " +
      Path::unixizeThis( dir ) ) );

  // get the "-src ..." line
  if ((dir = cmdLine->getQualifiedVariable( "-src", 2 )) ==
      StringConstants::EMPTY_STRING) //get src dir
    dir = "/";                                     // default dir
  mksb->add( String( "-src " + getMode("-src") + " " +
      Path::unixizeThis( dir ) ) );

  delete sbrc;
  return mksb;
}


/******************************************************************************
 * Get the base dir for the MKSB list
 */
String Mksb::getBaseDirForMKSB( SandboxRCConfigFile *sbrc,
    const StringArray &mksb_list ) const
{
  String base = cmdLine->getQualifiedVariable( "-dir", 1 );
  if (base == StringConstants::EMPTY_STRING)
    base = fromMKSBList( mksb_list, "-dir", 2 );

  // if base is STILL null and is sbrc exists get default base
  if (base == StringConstants::EMPTY_STRING)
    base = sbrc->getDefaultBaseDir();

  // canonicalize relative paths so they're absolute, as long
  // as it doesn't start with a variable
  if (!Path::absolute( base ) &&
      base[STRING_FIRST_INDEX] != VARIABLE_START_CHAR)
    Path::canonicalizeThis( base, false );
  String baseEval = Variable::envVarEval( base );

  while (!Path::isDirectory( baseEval ))
  {
    if (base != StringConstants::EMPTY_STRING)
      Interface::printWarning( program_name + ": " + base + " is not an "
          "existing directory" );

    base = Interface::getResponse(
        "Enter path of the sandbox base: ", true ).trim();

    // if -auto has been specified and we still have no valid
    // basedir, we must exit.
    if (base == StringConstants::EMPTY_STRING)
      Interface::quitWithErrMsg( program_name + ": Cannot use the <-auto> "
          "option without valid base dir.", 1 );

    if (!Path::absolute( base ) &&
        base[STRING_FIRST_INDEX] != VARIABLE_START_CHAR)
      Path::canonicalizeThis( base, false );
    baseEval = Variable::envVarEval( base );
  }

  return (Path::unixizeThis( base ));
}


/**
 * Get the machines list for the MKSB list
 *
**/
String Mksb::getMachinesForMKSB( String machine,
                                 const String &backingDir,
                                 SandboxRCConfigFile *sbrc ) const
{
  if (Interface::isDebug())
    Interface::printDebug( "checking machines for " + backingDir + "." );
  if (machine == StringConstants::EMPTY_STRING ||
      (!making_backing_build && !sb->verifyMachine( backingDir, true )))
  {
    // try getting machine list from backing build
    if (!making_backing_build && sb->getMachineList( backingDir ) ==
        StringConstants::EMPTY_STRING)
      Interface::quitWithErrMsg( program_name +
          ": Couldn't get machine list from sb.conf", 1 );

    // check the machine names on command line
    if (machine != StringConstants::EMPTY_STRING)
      Interface::quitWithErrMsg( program_name +
          ": Invalid machine name(s) - " + machine, 1);

    if (making_backing_build)
      machine = Interface::getResponse(
          "Enter colon separated machine list: ", true ).trim();
    else
    {
      // get user's choice of machines
      do
      {
        Interface::printAlways( sb->getMachineList( backingDir ));
        String msg( "Enter colon separated list of machines from "
            "above list: " );
        machine = Interface::getResponse( msg, false).trim();

        // the default is to use the whole list
        if (machine == StringConstants::EMPTY_STRING)
          machine = sb->getMachineList( backingDir );

        Env::setenv( SandboxConstants::CONTEXT_VAR, machine, true );
      } while (!sb->verifyMachine( backingDir, true ));
    }
  }

  return machine;
}


/******************************************************************************
 * Get the mode - "b" or "c" or "l" for "tools", "obj" or "src"
 */
String Mksb::getMode( const String &flag ) const
{
  String mode = cmdLine->getQualifiedVariable( flag, 1 );  // get  mode

  if (mode == StringConstants::EMPTY_STRING ||
      (!mode.equals( "b" ) && !mode.equals( "c" ) && !mode.equals( "l" )))
    mode = "b"; // default mode

  return mode;
}


/******************************************************************************
 * Get an entry from the MKSB list.
 * In case of the line "mksb -m machine1:...", to get the value "machine1..."
 * the args for this method are, the mksb list, "-m" and "2"
 */
String Mksb::fromMKSBList( const StringArray &mksb, const String &key,
                           int offset) const
{
  // if no mskb list or invalid offset
  if ((offset == 0) || (mksb.length() == 0))
    return (StringConstants::EMPTY_STRING);

  String entry;
      // try to get the entry with the specified key
  for (int i = mksb.firstIndex(); i <= mksb.lastIndex(); i++)
    if (mksb[i].startsWith( key ))
    {
      entry = mksb[i];
      break;
    }

  String value;
  // if no entry with that key is present
  if (entry != StringConstants::EMPTY_STRING)
  {
    StringArray tokens;
    entry.split( " ", UINT_MAX, &tokens );

    // offset should be less than num of tokens
    if (tokens.length() >= offset)
      value = tokens[offset];
  }

  return value;
}


/******************************************************************************
 * Build the sandbox dir structure.
 */
void Mksb::buildDirStructure( const String &machineList ) const
{
  if (Interface::isVerbose())
    Interface::printVerbose( "Building sandbox directory structure..." );

  if (machineList == StringConstants::EMPTY_STRING)
    Interface::quitWithErrMsg( program_name +
      ": Unable to create dir structure. Couldn't get machines list", 1 );

  String      fs = Path::DIR_SEPARATOR;
  String      root = sb->getSandboxBase();
  Path::userizeThis( root );
  StringArray machines;
  machineList.split( ":", UINT_MAX, &machines );

                        //create /src subdir
  Path::createPath( root + fs + SandboxConstants::getSRCNAME() );

  for (int i = machines.firstIndex(); i <= machines.lastIndex(); i++)
  {
    if (!Path::createPath( root + fs + "obj" + fs + machines[i] ))
      Interface::printWarning( program_name + ": Unable to create dir - " +
                               root + fs + "obj" + fs + machines[i] );
    if (!Path::createPath( root + fs + "tools" + fs + machines[i] ))
      Interface::printWarning( program_name + ": Unable to create dir - " +
                               root + fs + "tools" + fs + machines[i] );
    if (!Path::createPath( root + fs + "export" + fs + machines[i] ))
      Interface::printWarning( program_name + ": Unable to create dir - " +
                               root + fs + "export" + fs + machines[i] );
    if (!Path::createPath( root + fs + INST_IMAGES_NAME + fs + machines[i] +
                               fs + "images" ))
      Interface::printWarning( program_name + ": Unable to create dir - " +
                               root + fs + INST_IMAGES_NAME + fs + machines[i] +
                               fs + "images" );
    if (!Path::createPath( root + fs + INST_IMAGES_NAME + fs + machines[i] + fs +
                           "mdata" ))
      Interface::printWarning( program_name + ": Unable to create dir - " +
                               root + fs + INST_IMAGES_NAME + fs + machines[i] +
                               fs + "mdata" );
    if (!Path::createPath( root + fs + INST_IMAGES_NAME + fs + machines[i] + fs +
                           "tmp" ))
      Interface::printWarning( program_name + ": Unable to create dir - " +
                               root + fs + INST_IMAGES_NAME + fs + machines[i] +
                               fs + "tmp" );
  }
}


/******************************************************************************
 * If links to be made are specified call the "mklinks" class to create
 * the links.
 */
boolean Mksb::createLinks( const StringArray &mksbList, const String &rcfile,
    const String &machine )
{
  StringArray machines;
  machine.split( ":", 0, &machines );

  String toolsMode = cmdLine->getQualifiedVariable( "-tools", 1 );
  if (toolsMode == StringConstants::EMPTY_STRING)
    toolsMode = fromMKSBList( mksbList, "-tools", 2 );

  if (!toolsMode.equals( "b" ))  // if anything other than "b"
  {
    if (Interface::isVerbose())
      Interface::printVerbose( String( "Creating links for tools " ) +
                               "directory with mode: " + toolsMode );
    for (int i = machines.firstIndex(); i <= machines.lastIndex(); i++)
    {
      try
      {
        MkLinks( sb->getSandboxName(), rcfile, "tools/" + machines[i], "/",
                 toolsMode, cmdLine->isState( "-auto" ) );
      }
      catch (MkLinksException &me)
      {
        Interface::printError( program_name + ": " + me.getMessage() );
        return false;
      }
    }
  }

  String objMode = cmdLine->getQualifiedVariable( "-obj", 1 );
  String objDir = cmdLine->getQualifiedVariable( "-obj", 2 );
  if (objMode == StringConstants::EMPTY_STRING)
  {
    objMode = fromMKSBList( mksbList, "-obj", 2 );
    objDir  = fromMKSBList( mksbList, "-obj", 3 );
  }
  StringArray *dirs = 0;

  if (!objMode.equals( "b" ))  // if anything other than "b"
  {
    dirs = objDir.split( ":" );
    for (int i = dirs->firstIndex(); i <= dirs->lastIndex(); i++)
    {
      if (!((*dirs)[i].startsWith( "/" ))) // dirs (srcDir) was unixized
      {
        Interface::printError( program_name + ": Dir <" + (*dirs)[i] +
                               "> doesn't start with a slash");
        return false;
      }

      if (Interface::isVerbose())
        Interface::printVerbose( String( "Creating links for obj directory " ) +
                                 "with mode: " + objMode );
      for (int j = machines.firstIndex(); j <= machines.lastIndex(); j++)
      {
        try
        {
          MkLinks( sb->getSandboxName(), rcfile, "obj/" + machines[j],
                   (*dirs)[i], objMode, cmdLine->isState( "-auto" ) );
        }
        catch (MkLinksException &me)
        {
          Interface::printError( program_name + ": " + me.getMessage() );
          return false;
        }
      }
    }
  }

  delete dirs;
  dirs = 0;
  String srcMode = cmdLine->getQualifiedVariable( "-src", 1 );
  String srcDir = cmdLine->getQualifiedVariable( "-src", 2 );
  if (srcMode == StringConstants::EMPTY_STRING)
  {
    srcMode = fromMKSBList( mksbList, "-src", 2 );
    srcDir  = fromMKSBList( mksbList, "-src", 3 );
  }

  if (!srcMode.equals( "b" ))  // if anything other than "b"
  {
    dirs = srcDir.split( ":" );
    for (int i = dirs->firstIndex(); i <= dirs->lastIndex(); i++)
    {
      if (!(*dirs)[i].startsWith( "/" )) // remember, dirs (srcDir) was unixized
      {
        Interface::printError( program_name + ": Dir <" + (*dirs)[i] +
                               "> doesn't start with a slash.");
        return false;
      }

      if (Interface::isVerbose())
        Interface::printVerbose( String( "Creating links for src directory " ) +
                                 "with mode: " + srcMode);
      try
      {
        MkLinks( sb->getSandboxName(), rcfile, SandboxConstants::getSRCNAME(),
                 (*dirs)[i], srcMode, cmdLine->isState( "-auto" ) );
      }
      catch (MkLinksException &me)
      {
        Interface::printError( program_name + ": " + me.getMessage() );
        return false;
      }
    }
  }

  delete dirs;
  return true;
}


/******************************************************************************
 * Being in a sandbox is defined as being in a sub dir of the sandbox
 * base
 */
boolean Mksb::inSandbox( const Sandbox &sbox ) const
{
  SmartCaseString cwd( Path::getcwd() );
  SmartCaseString sb_base( Path::userize( sbox.getSandboxBase() ) );

  if (Interface::isDebug())
  {
    Interface::printDebug( "CWD: " + cwd );
    Interface::printDebug( "BASE: " + sb_base );
  }

  if (Path::isPrefix( cwd, sb_base ))
    return true;

  return false;
}


/******************************************************************************
 *
 */
String Mksb::getBackingDirForUpgrade() const
{
  String sbBase = sb->getSandboxBase();
  String backdir;

  if (sbBase != StringConstants::EMPTY_STRING)
  {
    backdir = sbBase + Path::DIR_SEPARATOR + "link";
    Path::unixizeThis( Path::canonicalizeThis( backdir ));
    if (!Path::exists( backdir ))
      backdir = StringConstants::EMPTY_STRING;
  }

  return backdir;
}


/******************************************************************************
 *
 */
void Mksb::createInstDirsForUpgrade() const
{
  String      fs = Path::DIR_SEPARATOR;
  StringArray machines;
  Path::getDirContents( sb->getSandboxBase() + fs + "obj",
                        false, Path::CONTENTS_DIRS, &machines );

  if (machines.length() != 0)
  {
    String dir( sb->getSandboxBase() + fs + INST_IMAGES_NAME );

    for (int i = machines.firstIndex(); i <= machines.lastIndex(); i++)
    {
      Path::createPath( dir + fs + machines[i] + fs + "images" );
      Path::createPath( dir + fs + machines[i] + fs + "mdata" );
      Path::createPath( dir + fs + machines[i] + fs + "tmp" );
    }
  }
  else
    Interface::quitWithErrMsg( program_name +
      ": Unable to create the '" INST_IMAGES_NAME "' subdirectories. " +
      "Could not get machine list", 1);
}



/******************************************************************************
 *
 */
void Mksb::checkCommandLine()
{
  StringArray           arguments( args );
  const char            *sts[] = { "-def", "-list", "-upgrade", "-undo", 0 };
  StringArray           states( sts );
  const char            *qv[]  = { "-back", "-dir", "-m", "-tools", "-blist",
                                   "-obj^2", "-src^2", "-rc", "-sb", 0 };
  StringArray           qVars( qv );
  const char            *ma1[] = { "-list", "-undo", "-upgrade", 0 };
  StringArray           mutexArray1( ma1 );
  Vector< StringArray > mutex;
  mutex.addElement( mutexArray1 );

  cmdLine = new CommandLine( &states, &qVars, true, arguments, true,
                             &mutex, 0, *this );
  cmdLine->process();
}


/******************************************************************************
 *
 */
void Mksb::printUsage() const
{
  Interface::printAlways( "Usage: mksb [-back <path>] "
      "[-dir <dir>] [-def]" );
  Interface::printAlways( "            [-m <machines>] "
      "[-blist <build_list>]" );
  Interface::printAlways( "            [sb_opts] [ODE_options] "
      "[populate_opts] <sandbox>" );
  Interface::printAlways( "       -back <path>: path of backing build" );
  Interface::printAlways( "       -dir <dir>: directory to make "
      "sandbox in" );
  Interface::printAlways( "       -def: make new sandbox the default" );
  Interface::printAlways( "       -m <machines>: colon separated list of "
      "machines to set up" );
  Interface::printAlways( "       -blist <build_list>: use the specified "
      "build_list file" );
  Interface::printAlways( "   sb_opts:" );
  Interface::printAlways( "       -rc <user rc>, -sb <sandbox>" );
  Interface::printAlways( "   ODE_options:" );
  Interface::printAlways( "       -auto -info -quiet -normal -verbose -debug "
      "-usage -version -rev" );
  Interface::printAlways( "   populate_opts:" );
  Interface::printAlways( "       -tools <mode>: populate tools area" );
  Interface::printAlways( "       -obj   <mode> <dirs>: populate "
      "object area" );
  Interface::printAlways( "       -src   <mode> <dirs>: populate "
      "source area" );
  Interface::printAlways( "           mode: 'c' - copy, 'l' - link, "
      "'b' - no copy or link" );
  Interface::printAlways( "           dirs: colon separated list of "
      "directories to populate" );
  Interface::printAlways( "   sandbox: name of sandbox to create" );
  Interface::printAlways( StringConstants::EMPTY_STRING );
  Interface::printAlways( "       mksb -list" );
  Interface::printAlways( "       mksb -undo [<sandbox> | -sb <sandbox>]" );
  Interface::printAlways( "       mksb -upgrade <sandbox>" );
}
