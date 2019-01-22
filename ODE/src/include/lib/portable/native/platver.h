#ifndef _ODE_LIB_PORTABLE_NATIVE_PLATVER_H_
#define _ODE_LIB_PORTABLE_NATIVE_PLATVER_H_

#if defined(OS400)
/* BROKEN: OS/400 doesn't have uname(), not sure what to use yet */
#elif defined(UNIX)
#include <sys/utsname.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifdef WIN32
  enum WINDOWS_PLATFORM_TYPE
  {
    WINDOWS31_PLATFORM,
    WINDOWS95_PLATFORM,
    WINDOWSNT_PLATFORM
  };

  typedef struct
  {
    enum WINDOWS_PLATFORM_TYPE platform;
    unsigned long major_version;
    unsigned long minor_version;
    unsigned long build_version;
  } ODE_VERSION_DATA;
#elif defined(OS2)
  typedef struct
  {
    unsigned long major_version;
    unsigned long minor_version;
    unsigned long revision_version;
  } ODE_VERSION_DATA;
#elif defined(OS400)
  /* BROKEN: OS/400 doesn't have uname(), not sure what to use yet */
  typedef int ODE_VERSION_DATA; /* doesn't matter */
#else /* UNIX */
  typedef struct utsname ODE_VERSION_DATA;
#endif

  int ODEuname( ODE_VERSION_DATA *buf );

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* _ODE_LIB_PORTABLE_NATIVE_PLATVER_H_ */
