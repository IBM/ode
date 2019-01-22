/**
 * SbInfoException
**/
#ifndef _ODE_BIN_SBINFO_SBINFOEX_HPP_
#define _ODE_BIN_SBINFO_SBINFOEX_HPP_

#include "lib/exceptn/exceptn.hpp"

class SbInfoException : public Exception
{
  public:

    SbInfoException() : Exception( "Unknown sbinfo exception" ) {};
    SbInfoException( const SbInfoException &copy ) : Exception( copy ) {};
    SbInfoException( const String &msg, unsigned long err = 0 ) :
        Exception( msg.toCharPtr(), err ) {};
    SbInfoException( const char *msg, unsigned long err = 0 ) :
        Exception( msg, err ) {};
};

#endif /* _ODE_BIN_SBINFO_SBINFOEX_HPP_ */
