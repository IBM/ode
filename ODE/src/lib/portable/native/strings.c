#if defined(AIX) || defined(MVSOE) || defined(SCO) || defined(VMS) || \
    defined(HPOSS)
#include <strings.h>
#else
#include <string.h>
#endif

#define _ODE_LIB_PORTABLE_NATIVE_STRINGS_C_
#include "lib/portable/native/strings.h"


/**
 * OS400's string.h doesn't let stricmp be seen by C code (just C++),
 * so when compiling in "C" mode (with the -+ flag), we must use the
 * underscore versions manually.
**/
#ifdef OS400
#ifdef __cplusplus
#define strcasecmp  stricmp
#define strncasecmp strnicmp
#else
#define strcasecmp  __stricmp
#define strncasecmp __strnicmp
#endif
#endif


int ODEstrcasecmp( const char *str1, const char *str2 )
{
#if defined(UNIX)
  return (strcasecmp( str1, str2 ));
#else
  return (stricmp( str1, str2 ));
#endif
}

int ODEstrncasecmp( const char *str1, const char *str2, int n )
{
#if defined(UNIX)
  return (strncasecmp( str1, str2, n ));
#else
  return (strnicmp( str1, str2, n ));
#endif
}
