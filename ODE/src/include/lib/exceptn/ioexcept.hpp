/**
 * IOException
 *
**/
#ifndef _ODE_LIB_EXCEPTN_IOEXCEPT_HPP_
#define _ODE_LIB_EXCEPTN_IOEXCEPT_HPP_

#include <base/odebase.hpp>
#include "lib/exceptn/exceptn.hpp"
#include "lib/string/string.hpp"


/**
 * Indicates that some kind of IO error has occured
 */
class IOException : public Exception
{
  public:
  
    IOException() :
        Exception( "IO Exception!" ) {};
    IOException( const IOException &copy ) :
        Exception( copy ) {};
    IOException( const String &msg, unsigned long err = 0 ) :
        Exception( msg.toCharPtr(), err ) {};
    IOException( const char *msg, unsigned long err = 0 ) :
        Exception( msg, err ) {};
};

/**
 * Indicates that a file doesn't exist.
 */
class FileNotFoundException : public Exception
{
  public:
  
    FileNotFoundException() :
        Exception( "FileNotFound Exception!" ) {};
    FileNotFoundException( const FileNotFoundException &copy ) :
        Exception( copy ) {};
    FileNotFoundException( const String &msg, unsigned long err = 0 ) :
        Exception( msg.toCharPtr(), err ) {};
    FileNotFoundException( const char *msg, unsigned long err = 0 ) :
        Exception( msg, err ) {};
};

#endif /* _ODE_LIB_EXCEPTN_IOEXCEPT_HPP_ */




