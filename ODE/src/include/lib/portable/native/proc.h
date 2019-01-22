#ifndef _ODE_LIB_PORTABLE_NATIVE_PROC_H_
#define _ODE_LIB_PORTABLE_NATIVE_PROC_H_

#ifdef WIN32
#undef boolean
#include <windows.h>
#undef ERROR
#define boolean int
#define ODEPROC_ID_TYPE      HANDLE
#define ODERET_CODE_TYPE     DWORD
#define ODEDEF_ERROR_CODE    0x20000000
#else
#define ODEPROC_ID_TYPE      int
#define ODERET_CODE_TYPE     int 
#define ODEDEF_ERROR_CODE    -1
#endif

#define INVALID_PROCESS (ODEPROC_ID_TYPE)-1

#ifdef __cplusplus
extern "C"
{
#endif

#ifdef OS2
#define ODE_BEGIN_LIBPATH 1
#define ODE_END_LIBPATH   2
#define ODE_LIBPATH_LEN   512
  void ODEgetExtLibPath( char *buf, int libpath_type );
  ODEPROC_ID_TYPE ODEfork( char **args, char **envs,
      char *beginlibpath, char *endlibpath );
#else
  ODEPROC_ID_TYPE ODEfork( char **args, char **envs );
#endif

#ifdef VMS
  void ODEsetSymbols( char **envs );
#endif

  ODEPROC_ID_TYPE  ODEwaitForAny( ODERET_CODE_TYPE *result );
  ODERET_CODE_TYPE ODEwait( ODEPROC_ID_TYPE pid );
  void ODEkill( ODEPROC_ID_TYPE pid );
  int  ODEgetpid( void );

#ifndef NO_PIPES_FOR_OUTPUT
  void ODEfreePipeBuffer( void );
  char *ODEgetPipeBuffer( void );
  ODEPROC_ID_TYPE ODEforkWithPipeOutput( char **args, char **envs,
      int keep_output );
#endif

#ifdef __cplusplus
}
#endif

#endif /* _ODE_LIB_PORTABLE_NATIVE_PROC_H_ */
