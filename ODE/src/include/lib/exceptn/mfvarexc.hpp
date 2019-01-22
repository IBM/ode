/**
 * MalformedVariable
 *
**/
#ifndef _ODE_LIB_EXCEPTN_MFVAREXC_HPP_
#define _ODE_LIB_EXCEPTN_MFVAREXC_HPP_

#include <base/odebase.hpp>
#include "lib/exceptn/exceptn.hpp"
#include "lib/string/string.hpp"


/**
 * Indicates that an improperly formatted variable
 * string was encountered.
 */
class MalformedVariable : public Exception
{
  public:
  
    MalformedVariable() :
        Exception( "Variable string is malformed!" ) {};
    MalformedVariable( const MalformedVariable &copy ) :
        Exception( copy ) {};
    MalformedVariable( const String &msg, unsigned long err = 0 ) :
        Exception( msg.toCharPtr(), err ) {};
    MalformedVariable( const char *msg, unsigned long err = 0 ) :
        Exception( msg, err ) {};
};

#endif /* _ODE_LIB_EXCEPTN_MFVAREXC_HPP_ */
