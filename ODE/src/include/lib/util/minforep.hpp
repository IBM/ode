/**
 * MakeInfoReportable
 *
 * Abstract class to define some stuff that Cond
 * class expects (which Make provides).
 *
**/
#ifndef _ODE_LIB_UTIL_MINFOREP_HPP_
#define _ODE_LIB_UTIL_MINFOREP_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"

class MakeInfoReportable
{
  public:

    // We're a base class, so we have this responsibility...
    virtual ~MakeInfoReportable() {};

    // Check if a file exists (search path is class-dependent)
    virtual boolean exists( const String &name ) const = 0;

    // Has the given target been defined?
    virtual boolean isTarget( const String &tgt ) const = 0;

    // Is the given target the main target?
    virtual boolean isMainTarget( const String &tgt ) const = 0;
};

#endif /* _ODE_LIB_UTIL_MINFOREP_HPP_ */
