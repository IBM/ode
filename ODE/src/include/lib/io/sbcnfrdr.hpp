#ifndef _ODE_LIB_IO_SBCNFRDR_HPP_
#define _ODE_LIB_IO_SBCNFRDR_HPP_


#include <base/odebase.hpp>
#include "lib/io/setvarcf.hpp"
#include "lib/string/setvars.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/string.hpp"


/******************************************************************************
 * Class which knows how to read the sb.conf files
 * throughout a backing chain.  Starting with the
 * sandbox_dir given in the constructor, it processes
 * each sb.conf through the chain (to build the complete
 * chain path).  The constructor may also (if the user
 * specifies) then re-process the files in reverse order
 * to insure the variables are set in the correct way.
 * Alternately, the user may wish the constructor to simply
 * build the chain and not read backwards immediately (if
 * the chain is all that is desired, for example).  The
 * user may re-process in either direction at any time
 * after construction.
 */
class SbconfReader
{
  public:

    enum { RECURSIVE_ERROR_CODE = 1 }; // for SandboxException (nonzero!)

    SbconfReader( const String &sandbox_dir,
        boolean read_backward = true, boolean allow_recursion = false );
        
    virtual ~SbconfReader()
    {
      delete sbconf;
      sbconf = 0;
    };
    
    void readChainForward();
    void readChainBackward();
    inline String getBackingChain() const;
    inline const StringArray &getBackingChainArray() const;
    inline const SetVars &getLocalVars() const;
    inline const SetVars &getGlobalVars() const;
    inline SetVarConfigFile *getSbconf() const;
    static boolean checkInChain( const String &sb, const String &cksb );


  private:

    boolean allow_recursion;
    SetVarConfigFile *sbconf;
    SetVars sbvars, envs;
    String sandbox_dir, backing_chain;
    StringArray backing_chain_array;

    String readFile( const String &sb_dir, SetVars &var_store,
                     String &main_sandbox_dir );
};


/******************************************************************************
 * Get the backing chain as a Path.PATH_SEPARATOR separated
 * string.
 *
 * @return The full paths to each sandbox/backing build in
 * the chain, starting with the local sandbox.  Each directory
 * is separated by Path.PATH_SEPARATOR.
 */
String SbconfReader::getBackingChain() const
{
  return (backing_chain);
}


/******************************************************************************
 * Get the backing chain as an array of directories.
 *
 * @return The full paths to each sandbox/backing build in
 * the chain, starting with the local sandbox in the first
 * array element.
 */
const StringArray &SbconfReader::getBackingChainArray() const
{
  return (backing_chain_array);
}


/******************************************************************************
 * Get the variables NOT preceded with "setenv" that were
 * set in the sb.conf file(s).
 *
 * @return A SetVars object (with no parent) which
 * contains all of the local variables
 * set in the sb.conf file(s).
 */
const SetVars &SbconfReader::getLocalVars() const
{
  return (sbvars);
}


/******************************************************************************
 * Get the variables preceded with "setenv" that were
 * set in the sb.conf file(s).
 *
 * @return A SetVars object (with no parent) which
 * contains all of the global (environment) variables
 * set in the sb.conf file(s).
 */
const SetVars &SbconfReader::getGlobalVars() const
{
  return (envs);
}


/******************************************************************************
 * Gets the SetVarConfigFile object for the most recently
 * read sb.conf file.
 *
 * @return If readChainBackward() hasn't been called yet,
 * this will return the object which refers to the backing
 * build (the root of the backing chain) sb.conf file.
 * If readChainBackward() has been called (one or more
 * times), the object refers to the sandbox's sb.conf
 * file.  User must deallocate this pointer (with delete).
 *
 */
SetVarConfigFile *SbconfReader::getSbconf() const
{
  if (sbconf == 0)
    return (0);
  return (new SetVarConfigFile( *sbconf ));
}


#endif // _ODE_LIB_IO_SBCNFRDR_HPP_
