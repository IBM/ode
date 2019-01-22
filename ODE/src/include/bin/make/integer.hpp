/**
 * Integer
 *
**/
#ifndef _ODE_BIN_MAKE_INTEGER_HPP_
#define _ODE_BIN_MAKE_INTEGER_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/exceptn/mfvarexc.hpp"

class Integer
{
  public:
    // access functions
    inline static unsigned int parseInt( const String &str );
};

inline unsigned int Integer::parseInt( const String &str )
// throw exception (NumberFormatException
{
  if (!str.isDigits())
    throw MalformedVariable( "Number Format Exception" );
  return (unsigned int)str.asInt();
}

#endif //_ODE_BIN_MAKE_INTEGER_HPP_

