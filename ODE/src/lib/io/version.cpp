#define _ODE_LIB_IO_VERSION_CPP_

#include "lib/io/version.hpp"

#ifdef __WEBMAKE__
const String Version::RELEASE    = "WEBMAKE";
const String Version::VERSION    = Version::RELEASE + " (Build 1)";
const String Version::BUILD_DATE = "28-May-2000";
#else
const String Version::RELEASE    = "%RELEASE_NAME%";
const String Version::VERSION    = Version::RELEASE + " (Build %LEVEL_NAME%)";
const String Version::BUILD_DATE = "%BUILD_DATE%";
#endif // __WEBMAKE__
