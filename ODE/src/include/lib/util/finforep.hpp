/**
 * FileInfoReportable
 *
 * Abstract class to report some info about a file.
 *
**/
#ifndef _ODE_LIB_UTIL_FINFOREP_HPP_
#define _ODE_LIB_UTIL_FINFOREP_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"

class FileInfoReportable
{
  public:

    // We're a base class, so we have this responsibility...
    virtual ~FileInfoReportable() {};

    // Return the name of the file.
    virtual const String &getPathname() const = 0;

    // Return the current line number of the file.
    virtual int getLineNumber() const = 0;
};

#endif /* _ODE_LIB_UTIL_FINFOREP_HPP_ */
