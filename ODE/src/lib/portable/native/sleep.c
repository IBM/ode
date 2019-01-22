#define _ODE_LIB_PORTABLE_NATIVE_SLEEP_C_
#ifdef UNIX
#ifdef MVSOE
#define _XOPEN_SOURCE_EXTENDED 1
#endif
#ifdef SOLARIS
#define __EXTENSIONS__
#endif
#include <unistd.h>
#include <time.h>
#include <sys/time.h>
#include <float.h>
#include <stdlib.h>
#endif

#ifdef HPOSS
#include <ktdmtyp.h>
#include <spthread.h>
#endif

#ifdef BEOS
#include <kernel/OS.h>
#endif

#include "lib/portable/native/sleep.h"

void ODEsleep( unsigned long msec )
{
#ifdef WIN32
  Sleep( msec );
#else
#ifdef OS2
  DosSleep( msec );
#else
#ifdef AIX
  struct timestruc_t timeout;
  timeout.tv_sec = (msec - (msec % 1000))/1000;
  timeout.tv_nsec = (msec % 1000) * 1000000;
  nsleep( &timeout, 0 );
#else
#ifdef BEOS
  sleep( (msec - (msec % 1000))/1000 );
  snooze( (msec % 1000) * 1000 );
#else
#if defined(MVSOE) || defined(SCO) || defined(DYNIXPTX) || \
    defined(OS400) || defined(TRU64) || defined(VMS) || defined(HPOSS) || \
    defined(INTERIX)
  sleep( (msec - (msec % 1000))/1000 );
  usleep( (msec % 1000) * 1000 );
#else
  struct timespec timeout;
  timeout.tv_sec = (msec - (msec % 1000))/1000;
  timeout.tv_nsec = (msec % 1000) * 1000000;
  nanosleep( &timeout, 0 );
#endif /* MVSOE */
#endif /* BEOS */
#endif /* AIX */
#endif /* OS2 */
#endif /* WIN32 */
}
