#ifndef _ODE_LIB_UTIL_DATE_HPP_
#define _ODE_LIB_UTIL_DATE_HPP_


#include "lib/string/string.hpp"

#include <time.h>

class Date
{
  public:
    inline static String convertToString( long seconds );
    inline static String getCurrentDateAndTime();

  private:
};


/******************************************************************************
 * If seconds = 0, return present time
 */
String Date::convertToString( long seconds )
{
  time_t time_str;
  time_str = (seconds == 0) ? time( NULL ) : (time_t)seconds ;
  String mtime( ctime( &time_str ) );
  return( mtime.substring( mtime.firstIndex() + 4, mtime.length() ) );
}


/******************************************************************************
 *
 */
String Date::getCurrentDateAndTime()
{
  return convertToString( 0 );
}

#endif // _ODE_LIB_UTIL_DATE_HPP_
