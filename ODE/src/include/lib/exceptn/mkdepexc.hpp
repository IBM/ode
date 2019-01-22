/**
 * MkDepException
 *
**/
#ifndef _ODE_LIB_IO_MKDEPEXP_HPP_
#define _ODE_LIB_IO_MKDEPEXP_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/exceptn/exceptn.hpp"
#include "lib/string/string.hpp"


/**
 * Indicates that some error occurred while
 * processing sandbox config files or input.
 */
class MkDepException : public Exception {
  public:
  
    MkDepException() : 
        Exception( "Error in mkdep or .rmkdep!" ) {}
    MkDepException( const MkDepException &copy ) :
        Exception( copy ) {}
    MkDepException( const String &msg, unsigned long err = 0 ) :
        Exception( msg.toCharPtr(), err ) {}
    MkDepException( const char *msg, unsigned long err = 0 ) :
        Exception( msg, err ) {}

};

#endif /* _ODE_LIB_IO_MKDEPEXP_HPP_ */


