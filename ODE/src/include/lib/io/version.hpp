#ifndef _ODE_LIB_IO_VERSION_HPP_
#define _ODE_LIB_IO_VERSION_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"

class Version
{
   public:
      static const String ODEDLLPORT RELEASE;
      static const String ODEDLLPORT VERSION;
      static const String ODEDLLPORT BUILD_DATE;
};


#endif //_ODE_LIB_IO_VERSION_HPP_
