/**
 * RunTimeException
 *
**/
#ifndef _ODE_LIB_IO_RTIMEEXP_HPP_
#define _ODE_LIB_IO_RTIMEEXP_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/exceptn/exceptn.hpp"
#include "lib/string/string.hpp"


/**
 * Indicates that some error occurred while
 * processing a runtime command.
 */
class RunTimeException : public Exception {
  public:

    RunTimeException() :
        Exception( "Error in runtime command." ) {}
    RunTimeException( const RunTimeException &copy ) :
        Exception( copy ) {}
    RunTimeException( const String &msg, unsigned long err = 0 ) :
        Exception( msg.toCharPtr(), err ) {}
    RunTimeException( const char *msg, unsigned long err = 0 ) :
        Exception( msg, err ) {}

};

#endif /* _ODE_LIB_IO_RTIMEEXP_HPP_ */


