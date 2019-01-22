#include <string.h>

#define _ODE_LIB_EXCEPTN_EXCEPTN_CPP_
#include "lib/exceptn/exceptn.hpp"

#define DEFAULT_EXCEPTION_TEXT "unknown ODE exception"
#define DEFAULT_EXCEPTION_TEXT_LEN 21

Exception::Exception() :
    errtext( 0 ), errcode( 0 )
{
  setMessage( DEFAULT_EXCEPTION_TEXT, DEFAULT_EXCEPTION_TEXT_LEN );
}

Exception::~Exception()
{
  delete[] errtext;
  errtext = 0;
}

Exception::Exception( const Exception &copy ) :
    errtext( 0 )
{
  if (&copy != this)
  {
    setMessage( copy.errtext, strlen( copy.errtext ) );
    errcode = copy.errcode;
  }
}

Exception::Exception( const char *msg, unsigned long err ) :
    errtext( 0 ), errcode( err )
{
  setText( msg );
}

void Exception::setText( const char *msg )
{
  if (msg != 0)
    setMessage( msg, strlen( msg ) );
  else
    setMessage( DEFAULT_EXCEPTION_TEXT, DEFAULT_EXCEPTION_TEXT_LEN );
}

void Exception::setMessage( const char *msg, unsigned long len )
{
  char *tmp = new char[len + 1];
  char *freeme = errtext;

  strncpy( tmp, msg, len );
  tmp[len] = '\0';
  errtext = tmp;
  delete[] freeme;
}
