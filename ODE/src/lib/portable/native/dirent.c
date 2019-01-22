#define _ODE_LIB_PORTABLE_NATIVE_DIRENT_C_

#include <sys/stat.h>
#include <string.h>
#include <stdlib.h>
#ifdef UNIX
#include <unistd.h>
#endif

#include "lib/portable/native/dirent.h"
#include "lib/portable/native/file.h"


/**
 * Return an ODEDIR structure pointer, or 0 (NULL) if
 * there was any kind of failure.
 *
 * WARNING: the "dir" parameter must NOT be NULL!
 *
**/
ODEDIR *ODEopendir( char *dir )
{
#ifdef OS2
  unsigned long num_entries = 1;
#endif
  char *files;
  ODEDIR *result = 0;
  int dirlen;

#ifndef UNIX
  result = (ODEDIR*)malloc( sizeof( ODEDIR ) );
#endif

  /* append a wild card "*" to the dir name to get all files */
  dirlen = strlen( dir );
  files = (char*)malloc( (dirlen + 3) * sizeof( char ) );
  strcpy( files, dir );

#ifndef UNIX
  if (files[dirlen - 1] != '\\')
    strcat( files, "\\" );
  strcat( files, "*" );
  result->eof = 0; /* usually */
#endif

#ifdef WIN32
  if ((result->__dd_fd = FindFirstFile( files, &result->FileData )) ==
      INVALID_HANDLE_VALUE)
    result->eof = 1;
#else /* not WIN32... */
#ifdef OS2
  result->__dd_fd = HDIR_CREATE;
  if (DosFindFirst( files, &result->__dd_fd,
      FILE_DIRECTORY | FILE_ARCHIVED | FILE_READONLY,
      &result->FileData, sizeof( result->FileData ),
      &num_entries, FIL_STANDARD ) != 0)
    result->eof = 1;
  if (num_entries < 1)
    result->eof = 1;
#else /* must be UNIX */
  result = opendir( files );
#endif /* OS2 */
#endif /* WIN32 */

  free( files );

  return (result);
}

/**
 * This function returns the dir entries one by one -
 * it must be called repeatedly (until the return value
 * is nonzero) to get all the elements.
 *
 * Returns zero on success, nonzero on failure.
 *
 * WARNING: DO NOT pass "result" as NULL!
 *
**/
int ODEreaddir( ODEDIR *dirp, ODEDIRENT *result )
{
#ifdef UNIX
  struct dirent *dir_ent;
#endif
#ifdef OS2
  unsigned long num_entries = 1;
#endif
  int rc = 0; /* assume success */

  if (dirp == 0) /* ODEopendir must have failed */
    return (-1); /* no entries to process */

#if defined(WIN32)
  if (dirp->eof || dirp->__dd_fd == INVALID_HANDLE_VALUE)
    return (-1);
  strncpy( result->d_name, dirp->FileData.cFileName, MAX_DIRENT_PATH_LEN );
  result->d_name[MAX_DIRENT_PATH_LEN] = '\0';
  if (FindNextFile( dirp->__dd_fd, &dirp->FileData ) == FALSE)
    dirp->eof = 1;
#elif defined(OS2)
  if (dirp->eof)
    return (-1);
  strncpy( result->d_name, dirp->FileData.achName, MAX_DIRENT_PATH_LEN );
  result->d_name[MAX_DIRENT_PATH_LEN] = '\0';
  if (DosFindNext( dirp->__dd_fd, &dirp->FileData, sizeof( dirp->FileData ),
      &num_entries ) != 0)
    dirp->eof = 1;
  if (num_entries < 1)
    dirp->eof = 1;
#else /* Unix platforms */
  dir_ent = readdir( dirp );
  if (dir_ent != 0)
    strcpy( result->d_name, dir_ent->d_name );
  else
    rc = -1;
#endif

  return (rc);
}


/**
 * Closes an open ODEDIR handle (MUST have been
 * opened with ODEopendir!).
**/
int ODEclosedir( ODEDIR *dirp )
{
  int result = 0;

  if (dirp == 0) /* ODEopendir must have failed */
    return (0);  /* nothing to close, so OK! */

#ifdef WIN32
  if (dirp->__dd_fd != INVALID_HANDLE_VALUE)
    result = (int)FindClose( dirp->__dd_fd );
  free( dirp );
#else
#ifdef OS2
  result = (int)DosFindClose( dirp->__dd_fd );
  free( dirp );
#else
  result = closedir( dirp );
#endif /* OS2 */
#endif /* WIN32 */

  return (result);
}
