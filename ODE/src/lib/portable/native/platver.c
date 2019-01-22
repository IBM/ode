#define _ODE_LIB_PORTABLE_NATIVE_PLATVER_C_
#include "lib/portable/native/platver.h"


#if defined(WIN32)
#include <windows.h>
#elif defined(OS2)
#define INCL_DOSMISC
#define INCL_DOSERRORS
#include <os2.h>
#endif


#ifdef WIN32
/**
 * Returns -1 on failure (if buf was null, or
 * version info could not be obtained).
 * Returns 0 on success (and fills in the
 * fields of buf).
**/
int ODEuname( ODE_VERSION_DATA *buf )
{
  OSVERSIONINFO osinfo;
  
  osinfo.dwOSVersionInfoSize = sizeof( OSVERSIONINFO );
  if (buf == 0 || GetVersionEx( &osinfo ) != TRUE)
    return (-1);

  switch (osinfo.dwPlatformId)
  {
    case VER_PLATFORM_WIN32s:
      buf->platform = WINDOWS31_PLATFORM;
      break;
    case VER_PLATFORM_WIN32_WINDOWS:
      buf->platform = WINDOWS95_PLATFORM;
      break;
    case VER_PLATFORM_WIN32_NT:
      buf->platform = WINDOWSNT_PLATFORM;
      break;
  }
  buf->major_version = osinfo.dwMajorVersion;
  buf->minor_version = osinfo.dwMinorVersion;
  buf->build_version = osinfo.dwBuildNumber;

  return (0);
}
#endif /* WIN32 */


#ifdef OS2
/**
 * Returns -1 on failure (if buf was null, or
 * version info could not be obtained).
 * Returns 0 on success (and fills in the
 * fields of buf).
**/
int ODEuname( ODE_VERSION_DATA *buf )
{
  ULONG ptr[3];

  if (buf == 0 || DosQuerySysInfo( QSV_VERSION_MAJOR, QSV_VERSION_REVISION,
      ptr, 3*sizeof(ULONG) ) != 0)
    return (-1);

  buf->major_version = ptr[0];
  buf->minor_version = ptr[1];
  buf->revision_version = ptr[2];

  return (0);
}
#endif /* OS2 */


#ifdef UNIX
/**
 * Returns -1 on failure (if buf was null, or
 * version info could not be obtained).
 * Returns 0 on success (and fills in the
 * fields of buf).
**/
int ODEuname( ODE_VERSION_DATA *buf )
{
#ifdef OS400
/* BROKEN: uname() doesn't exist under OS/400, not sure what to use yet */
  return (-1);
#else
  if (buf == 0 || uname( buf ) < 0)
    return (-1);

  return (0);
#endif
}
#endif /* UNIX */
