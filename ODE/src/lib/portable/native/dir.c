#include <ctype.h>

#ifdef UNIX
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#else
#include <direct.h>
#endif

#include <errno.h>
#ifdef WIN32
#include <windows.h>
#endif
#define _ODE_LIB_PORTABLE_NATIVE_DIR_C_
#include "lib/portable/native/dir.h"

/**
 * Create a directory.
 * All intermediate directories must already exist.
 *
 * @return 0 on success, nonzero on failure.
 *
**/
int ODEmkdir( char *path )
{
int mkdir_return, mkdir_err;
#ifdef UNIX
  mkdir_return = (mkdir( path, S_IRUSR | S_IWUSR | S_IXUSR |
      S_IRGRP | S_IWGRP | S_IXGRP | S_IROTH | S_IWOTH | S_IXOTH ));
  if ( mkdir_return != 0 )
  {
    mkdir_err = errno;
    if ( mkdir_err != EEXIST && mkdir_err != ENOTEMPTY)
      return(1);
  }
#elif defined( WIN32 )
 if (!CreateDirectory( path, NULL ))
 {
   mkdir_err = GetLastError();
   if ( mkdir_err != ERROR_ALREADY_EXISTS && mkdir_err != ERROR_FILE_NOT_FOUND )
     return(1);
 }
#else
   return(mkdir( path ));
#endif

   return(0);
}

/**
 * Remove a directory.  The directory must be empty.
 *
 * @return 0 on success, nonzero on failure.
 *
**/
int ODErmdir( char *path )
{
  return (rmdir( path ));
}

/**
 * Get the current working directory.
 *
 * @param buf A buffer in which to store the path.  If 0 is
 * passed, malloc() will be used to create the buffer instead.
 * @param buflen If buf wasn't 0, this will indicate the maximum
 * number of characters to place into buflen (buf must be large
 * enough to hold buflen+1 characters, since the path is null
 * terminated).
 * @return A malloc'ed pointer if buf is 0, otherwise it copies
 * up to buflen characters of the path into buf.  Null (0) is
 * returned if the current directory could not be obtained.
 */
char *ODEgetcwd( char *buf, int buflen )
{
#if defined(VMS)
  return (getcwd( buf, buflen, 0 ));
#elif defined(UNIX)
  return (getcwd( buf, buflen ));

#elif defined(WIN32)
#define MAX_PATH_LENGTH 1024
  char *token;
  char shortDir[MAX_PATH_LENGTH + 1];
  char tempDir[MAX_PATH_LENGTH + 1];

  _getcwd( shortDir, MAX_PATH_LENGTH + 1 );
  strcpy( tempDir, strtok(shortDir, "\\") );
  strcpy( buf, tempDir );

  // Because the C Runtime call (_getcwd) above may return a shortname for
  // the current directory, we need to convert it to the longname.  We
  // iteratively call FindFirstFile() for each subdirectory to be sure
  // we have the longname, which is found in FindFileData.cFileName
  while ((token = strtok(NULL, "\\")) != 0)
  {
    WIN32_FIND_DATA  FindFileData;
    HANDLE           hFind;

    strcat( tempDir, "\\");
    strcat( tempDir, token );

    hFind = FindFirstFile( tempDir, &FindFileData);

    if (hFind == INVALID_HANDLE_VALUE)
    {
      FindClose( hFind );
      strcpy( buf, shortDir );
      return( buf );
    }
    strcat( buf, "\\" );
    strcat( buf, FindFileData.cFileName );
    FindClose( hFind );
  }

  // Just in case the current directory is the root of a drive (ex: C:), add
  // the trailing backslash.
  if (strlen( buf ) < 3)
  {
    strcat( buf, "\\" );
  }
  return( buf );

#else
  return (_getcwd( buf, buflen ));
#endif
}

/**
 * Change the current working directory.
 *
 * @param path A string containing the directory to change to.
 * @return 0 if the directory was changed to successfully,
 * nonzero if not.
 *
 * If path is "", return non-zero like UNIX chdir() and do not change
 * dir.  If "d:" on non-UNIX system, switch to drive "d".
 */
int ODEsetcwd( char *path )
{
  char *tmppath = path;

  if (tmppath == 0 || *tmppath == '\0')
    return (-1);
#ifndef UNIX
  if (isalpha( *tmppath ) && *(tmppath + 1) == ':')
  {
    if (_chdrive( toupper( *tmppath) - 'A' + 1  ) != 0)
      return (-1);
    tmppath += 2; /* skip over drive and colon for the chdir */
    /* do not let chdir("") change us to root on Windows NT! */
    if (*tmppath == '\0')
      return (0);
  }
#endif
  return( chdir( tmppath ));
}
