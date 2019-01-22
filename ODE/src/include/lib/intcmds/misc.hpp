//***********************************************************************
//* Misc
//*
//***********************************************************************
#ifndef _ODE_LIB_IO_MISC_HPP_
#define _ODE_LIB_IO_MISC_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/string/strarray.hpp"
#include "lib/portable/vector.hpp"

class Misc
{
  public:

    static void copyArrayToVector( const StringArray &src,
        Vector<String> &dest );
    static boolean stringStartsWith( const String &str, const String &prefix );
    static boolean stringEndsWith( const String &str, const String &suffix );
    static void normalizePaths( StringArray &paths );
};

#endif //_ODE_LIB_IO_MISC_HPP_
