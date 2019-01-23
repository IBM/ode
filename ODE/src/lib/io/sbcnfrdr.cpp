using namespace std;
#define _ODE_LIB_IO_SBCNFRDR_CPP_


#include "lib/io/path.hpp"
#include "lib/io/sbcnfrdr.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/env.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/variable.hpp"

#include "lib/exceptn/sboxexc.hpp"
#include "lib/io/cmdline.hpp"


/******************************************************************************
 * This constructor both determines the chain (by reading
 * in forward order), and may or may not re-process in
 * reverse order (depending on read_backward_now).
 *
 * @param sandbox_dir The directory of the local sandbox
 * (this should include the sandbox name in the path, of
 * course).
 * @param read_backward_now If true, the sb.conf's will be
 * re-processed in reverse order (which is the normal direction
 * for initializing variables correctly).  If false, the
 * sb.conf's will only be read once, in forward order.
 */
SbconfReader::SbconfReader( const String &sandbox_dir,
    boolean read_backward,
    boolean allow_recursion ) :
    sbconf( 0 ), sbvars( true ), // local vars are always case-sensitive
    envs( PlatformConstants::onCaseSensitiveOS() ),
    sandbox_dir( sandbox_dir ), allow_recursion( allow_recursion )
    //throws SandboxException
{
  if (sandbox_dir == StringConstants::EMPTY_STRING)
    throw SandboxException( "No sandbox given!" );
    
  readChainForward();  // initialize chain

  if (read_backward)
    readChainBackward();
}


/******************************************************************************
 * Process all sb.conf files in the chain, starting with
 * the local sandbox and working toward the root of the
 * chain.  This has to happen before readChainBackward()
 * can occur, since this is where the chain itself is
 * first realized.  Thus, the constructor calls this
 * method automatically (and therefore the user rarely,
 * if ever, will actually need to use this method).
 */
void SbconfReader::readChainForward() //throws SandboxException
{
  String cur_sb, backed_by;

  sbvars.setParent( new Env(), true );
  backing_chain = cur_sb = sandbox_dir;

  while ((backed_by = readFile( cur_sb, sbvars, sandbox_dir )) !=
      StringConstants::EMPTY_STRING)
  {
    if (!Path::exists( backed_by ))
    {
      String errmsg = String( "Backing build directory (" ) + backed_by +
          ") does not exist (referenced in sb.conf of " + cur_sb + ")";

      if (cur_sb == sandbox_dir)
        Interface::printWarning( CommandLine::getProgramName() + ": " +
                                 errmsg );
      else
        throw SandboxException( errmsg );
    }

    if (Path::pathInList( backed_by, backing_chain ) != ELEMENT_NOTFOUND)
    {
      if (allow_recursion)
        break;
      throw SandboxException( String( "You have an infinite " ) +
          "backing_build cycle in your chain (detected in sb.conf of " +
          cur_sb + ")", SbconfReader::RECURSIVE_ERROR_CODE );
    }

    backing_chain += Path::PATH_SEPARATOR + backed_by;
    if (sbconf)
      sbconf->getGlobalVars( envs );
    cur_sb = backed_by;
  }

  Path::separatePaths( backing_chain, &backing_chain_array );
  sbvars.unsetParent();
}


/******************************************************************************
 * Process all sb.conf files in the chain, starting with
 * the root of the chain and working toward the local
 * sandbox.  This is the normal way to initialize the
 * variables in the way the chain is typically read (and
 * so is given as an option in the constructor).
 */
void SbconfReader::readChainBackward()
{
  sbvars.setParent( new Env(), true );

  for (int i = backing_chain_array.lastIndex();
      i >= backing_chain_array.firstIndex(); --i)
  {
    readFile( backing_chain_array[i], sbvars, backing_chain_array[i] );
    if (sbconf)
      sbconf->getGlobalVars( envs );
  }

  sbvars.unsetParent();
}


/**
 * Read an sb.conf file, store all its variables in var_store
 * and return the backing build path.
**/
String SbconfReader::readFile( const String &sb_dir, SetVars &var_store,
                               String &main_sandbox_dir )
{
  const String *backed_by;
  String errmsg;

  if (sb_dir == StringConstants::EMPTY_STRING)
    return (StringConstants::EMPTY_STRING);

  var_store.unset( SandboxConstants::BACKING_BUILD_VAR );


  String path( sb_dir );
  boolean sandbox_exists = Path::exists( path );
  path += Path::DIR_SEPARATOR;
  path += SandboxConstants::getRCFILES_DIR();
  if (sandbox_exists && ! Path::exists( path ))
  {
    errmsg = "Invalid backing build: config file directory (" + path +
                  ") does not exist";
    if (sb_dir != main_sandbox_dir)
      errmsg += String( "; referenced in sb.conf of (" ) + 
                          main_sandbox_dir + ")";
    throw SandboxException( errmsg );
  }
  path += Path::DIR_SEPARATOR;
  path += SandboxConstants::SBCONF_NAME;
  if (sandbox_exists && ! Path::exists( path ))
  {
    errmsg = "Invalid backing build: config file (" + path +
                  ") does not exist";
    if (sb_dir != main_sandbox_dir)
      errmsg += String( "; referenced in sb.conf of (" ) + 
                          main_sandbox_dir + ")";
    throw SandboxException( errmsg );
  }
  delete sbconf;
  sbconf = new SetVarConfigFile( path, var_store );

  if ((backed_by = var_store.get(
      SandboxConstants::BACKING_BUILD_VAR )) != 0)
    return (*backed_by);

  return (StringConstants::EMPTY_STRING);
}


/**
 * check if a sandbox cksb is in the backing chain of sandbox sb.
 * throws SandboxException
 */
boolean SbconfReader::checkInChain( const String &sb, const String &cksb )
{
  StringArray     bchain;
  SmartCaseString normChainSb;
  SmartCaseString cksbNormal( Path::normalize( Variable::envVarEval( cksb ) ) );

  SbconfReader reader( Variable::envVarEval( sb ), true, false );
  bchain = reader.getBackingChainArray();

  for (int j = bchain.firstIndex(); j <= bchain.lastIndex(); ++j)
  {
    normChainSb = Path::normalize( Variable::envVarEval( bchain[j] ) );
    if (normChainSb == cksbNormal)
    {
      return true;
    }
  } // end for j
  return false;
}
