/**
 * Archive utilities
**/
#ifndef _ODE_LIB_UTIL_ARCHCACH_HPP_
#define _ODE_LIB_UTIL_ARCHCACH_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/util/cacharch.hpp"
#include "lib/portable/hashtabl.hpp"

class ArchiveCache
{
  public:

    // when output_header_msg is true, the function will output a message
    // telling whether or not the header was intact (the first time the
    // archive is opened).
    static Archive *get( const String &name, const String &cwd,
        const StringArray &paths, boolean output_header_msg );


  private:

    static Hashtable< SmartCaseString, Archive * > ODEDLLPORT cache;
    static StringArray *formPaths( const String &name, const String &cwd,
        const StringArray &paths, StringArray *buf );
};

#endif //_ODE_LIB_UTIL_ARCHCACH_HPP_
