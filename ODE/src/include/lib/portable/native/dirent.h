#ifndef _ODE_LIB_PORTABLE_NATIVE_DIRENT_H_
#define _ODE_LIB_PORTABLE_NATIVE_DIRENT_H_

#ifdef _ODE_LIB_PORTABLE_NATIVE_DIRENT_C_
#ifdef WIN32
#include <windows.h>
#else
#ifdef OS2
#define INCL_DOSFILEMGR
#include <os2.h>
#endif /* OS2 */
#endif /* WIN32 */
#endif /* _ODE_LIB_PORTABLE_NATIVE_DIRENT_C_ */

#ifdef UNIX
#include <dirent.h>
#endif

#define MAX_DIRENT_PATH_LEN  512

#ifdef __cplusplus
extern "C"
{
#endif

#if !defined(_ODE_LIB_PORTABLE_NATIVE_DIRENT_C_)
  typedef void ODEDIR_TYPE;
#elif defined(WIN32)
  typedef struct
  {
    int eof;
    HANDLE __dd_fd;
    WIN32_FIND_DATA FileData;
  } ODEDIR_TYPE;
#elif defined(OS2)
  typedef struct
  {
    int eof;
    HDIR __dd_fd;
    FILEFINDBUF3 FileData;
  } ODEDIR_TYPE;
#endif

#ifdef UNIX
  typedef DIR ODEDIR;
#else
  typedef ODEDIR_TYPE ODEDIR;
#endif

  typedef struct
  {
    char d_name[MAX_DIRENT_PATH_LEN + 1];
  } ODEDIRENT;

  ODEDIR *ODEopendir( char *dirname );
  int ODEreaddir( ODEDIR *dirp, ODEDIRENT *result );
  int ODEclosedir( ODEDIR *dirp );

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* _ODE_LIB_PORTABLE_NATIVE_DIRENT_H_ */
