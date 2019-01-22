/**
 * ParseException
 *
**/
#ifndef _ODE_LIB_EXCEPTN_PARSEEXC_HPP_
#define _ODE_LIB_EXCEPTN_PARSEEXC_HPP_

#include <base/odebase.hpp>
#include "lib/exceptn/exceptn.hpp"
#include "lib/string/string.hpp"

class ParseException : public Exception
{
  public:

    ParseException() :
        Exception( "Parse Exception!" ) {};

    ParseException( const String &file, int line, const String &msg,
        unsigned long err = 0 ) :
        Exception( msg.toCharPtr(), err )
    {
      if (file != String( "" ))
      {
        String longmsg( '\"' );
        longmsg += file;
        longmsg += "\", line ";
        longmsg += line;
        longmsg += ": ";
        longmsg += msg;
        setText( longmsg.toCharPtr() );
      }
    };

    ParseException( const String &msg, unsigned long err = 0 ) :
        Exception( msg.toCharPtr(), err ) {};

    ParseException( const ParseException &copy ) :
        Exception( copy ) {};

    inline String toString() const
    {
      return (getMessage());
    };
};

#endif /* _ODE_LIB_EXCEPTN_PARSEEXC_HPP_ */
