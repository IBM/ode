/**
 * SandboxException
 *
**/
#ifndef _ODE_LIB_EXCEPTN_SBOXEXC_HPP_
#define _ODE_LIB_EXCEPTN_SBOXEXC_HPP_

#include <base/odebase.hpp>
#include "lib/exceptn/exceptn.hpp"
#include "lib/string/string.hpp"


/**
 * Indicates that some error occurred while
 * processing sandbox config files or input.
 */
class SandboxException : public Exception
{
  public:
  
    SandboxException() : 
        Exception( "Error found while processing sandbox info!" ) {};
    SandboxException( const SandboxException &copy ) :
        Exception( copy ) {};
    SandboxException( const String &msg, unsigned long err = 0 ) :
        Exception( msg.toCharPtr(), err ) {};
    SandboxException( const char *msg, unsigned long err = 0 ) :
        Exception( msg, err ) {};
};

#endif /* _ODE_LIB_EXCEPTN_SBOXEXC_HPP_ */
