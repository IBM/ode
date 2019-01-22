/**
 * MkLinksException
**/
#ifndef _ODE_BIN_MKLINKS_MKLINKEX_HPP_
#define _ODE_BIN_MKLINKS_MKLINKEX_HPP_

#include "lib/exceptn/exceptn.hpp"

class MkLinksException : public Exception
{
  public:

    MkLinksException() : Exception( "Unknown mklinks exception" ) {};
    MkLinksException( const MkLinksException &copy ) : Exception( copy ) {};
    MkLinksException( const String &msg, unsigned long err = 0 ) :
        Exception( msg.toCharPtr(), err ) {};
    MkLinksException( const char *msg, unsigned long err = 0 ) :
        Exception( msg, err ) {};
};

#endif /* _ODE_BIN_MKLINKS_MKLINKEX_HPP_ */
