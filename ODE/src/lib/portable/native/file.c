#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>

#ifdef UNIX
#include <unistd.h>
#ifdef VMS
#include "lib/portable/native/cutime.h"
#else
#include <utime.h>
#endif
#else
#include <io.h>
#include <sys/utime.h>
#endif

#define _ODE_LIB_PORTABLE_NATIVE_FILE_C_
#include "lib/portable/native/file.h"
#include "lib/portable/native/dirent.h"
#include "lib/portable/native/proc.h"

/**
 * Since this uses fopen to check for writability, the
 * filename must already exist (otherwise it will leave a
 * zero-length file lying around).  This function is only
 * used internally by ODEstat, and only when the file exists,
 * so this should not be a problem.
 *
 * Note also that since another process may have the file
 * open for writing (locked), this may give a "false" negative
 * response for writability...however, this behavior is perhaps
 * a bit more accurate, since it tells the user if they will
 * have the ability to create/modify the file [soon] after this
 * function is called.  Therefore, the boolean is_writable will
 * represent "can I write now?" as opposed to "can I ever write?"
 *
 * Note #3: Because a symbolic link may exist but point to a
 * nonexistent file, we have to be careful not to perform the
 * write test (it may create a file when it did not exist before).
 * Besides, access should reflect only the link itself when lstat
 * was performed, not the file it points to.
 *
**/
void getFileAccess( char *filename, struct ODEstat *buf )
{
  if (buf->is_link) /* symlinks are rwx for everyone */
  {
    buf->is_readable = 1;
    buf->is_writable = 1;
  }
  else
  {
    FILE *fd;

    if ((fd = fopen( filename, "rb" )) != 0)
    {
      fclose( fd );
      buf->is_readable = 1;
    }
    else
      buf->is_readable = 0;

#ifdef VMS
    if (access( filename, W_OK ) == 0)
    {
#else
    if ((fd = fopen( filename, "ab" )) != 0)
    {
      fclose( fd );
#endif
      buf->is_writable = 1;
    }
    else
      buf->is_writable = 0;
  }
}

/**
 * Read access to a directory means being able to get the
 * directory contents (ODEopendir).
 *
 * Write access means being able to create a file in that
 * directory.
**/
void getDirAccess( char *dirname, struct ODEstat *buf )
{
  ODEDIR *dd;
  FILE *fd;
  char *filename, *ptr;
  long i;

  if ((dd = ODEopendir( dirname )) != 0)
  {
    ODEclosedir( dd );
    buf->is_readable = 1;
  }
  else
    buf->is_readable = 0;

  /* allocation is dirlen + enough room for a temp filename + the null char. */
  /* temp filename will be PID + _ + a number + ".ODE"...not bulletproof, */
  /* but this doesn't really need to be. */
  filename = (char *)malloc( strlen( dirname ) + 20 + 1 );
  strcpy( filename, dirname );
#ifdef UNIX
  strcat( filename, "/" );
#else
  strcat( filename, "\\" );
#endif
  ptr = filename + strlen( filename );

  buf->is_writable = 0; /* in case loop is exhausted (!!!) */
  for (i = 0L; i < 999999L; ++i)
  {
    sprintf( ptr, "%d", ODEgetpid() );
    strcat( ptr, "_" );
    sprintf( ptr + strlen( ptr ), "%ld", i );
    strcat( ptr, ".ODE" );
    if (ODEstat( filename, 0, OFFILE_ODEMODE, 0 ) != 0)
    {
      if ((fd = fopen( filename, "a" )) != 0)
      {
        fclose( fd );
        remove( filename );
        buf->is_writable = 1;
      }
      break; /* either way, we're done */
    }
  }
  free( filename );
}

/**
 * Get info about a file or link.
 *
 * @param name The file to get info about.
 * @param buf The buffer in which the info is stored.
 * If buf is NULL, ODEstat will simply return the return
 * code of the stat or lstat function (which is enough to
 * detect if the file/directory exists).
 * @param mode If name specifies a symbolic link, this
 * parameter is meaningful: if set to OFFILE_ODEMODE,
 * info obtained is about the file that is pointed to,
 * whereas if OFLINK_ODEMODE is set, info is about the
 * link itself.
 * @return 0 if info was obtained, nonzero if not (in
 * the latter case, buf will be not be modified).
**/
int ODEstat( char *name, struct ODEstat *buf, enum ODEstatmode mode,
    int getrwinfo )
{
  struct stat filestat;
  int rc;

  if (name == 0)
    return (-1);
#if !defined(NO_SYMLINKS)
  if (mode == OFLINK_ODEMODE)
    rc = lstat( name, &filestat );
  else
#endif
    rc = stat( name, &filestat );

  if (rc == 0 && buf != 0)
  {
#if !defined(NO_SYMLINKS)
    buf->is_link = S_ISLNK( filestat.st_mode );
#else
    buf->is_link = 0; /* no such thing as links on NT/OS2 */
#endif

#ifdef UNIX
    buf->is_dir = S_ISDIR( filestat.st_mode );
    buf->is_reg = S_ISREG( filestat.st_mode );
#else
    buf->is_dir = filestat.st_mode & S_IFDIR;
    buf->is_reg = filestat.st_mode & S_IFREG;
#endif
    buf->is_file = !buf->is_dir;
    buf->size = filestat.st_size;
    buf->atime = filestat.st_atime;
    buf->mtime = filestat.st_mtime;
    buf->ctime = filestat.st_ctime;
    /* getDirAccess/getFileAccess can only be called AFTER buf is filled in */
    if (getrwinfo)
    {
      if (buf->is_dir)
        getDirAccess( name, buf );
      else
        getFileAccess( name, buf );
    }
  }
#ifdef WIN32
  // Take care of WIN32 bug in stat() for UNC directory of form "\\host\dir"
  // and use different routines to get a bit more info.  Note that the
  // UNC sharing can only work for directories, not files.
  if (rc != 0 && (*name == '\\' || *name == '/') && 
        (*(name + 1) == '\\' || *(name + 1) == '/'))
  {
    DWORD attrs;
    attrs = GetFileAttributes( name );
    if (attrs != 0xFFFFFFFF && buf !=0)
    {
      // Windows NT Visual Age docs claim that we could use CreateFile with
      // FILE_FLAG_BACKUP_SEMANTICS to get a handle for a directory,
      // although I could not get it to work.  Some other flags may also
      // be needed.  It is not clear from the writeup whether such a handle
      // could be used for getting the modification time by using
      // GetFileInformationByHandle.
      buf->is_link = 0;
      buf->is_dir = attrs & FILE_ATTRIBUTE_DIRECTORY;
      buf->is_file = !buf->is_dir;
      buf->is_reg = buf->is_file;
      buf->is_writable = !(FILE_ATTRIBUTE_READONLY & attrs);
      buf->size = 0;
      // WARNING! We could with considerable effort, translate from FILETIME
      // to time_t if we could get the time using GetFileInformationByHandle.
      // Instead, we fake it, since it is almost certainly a directory
      // whose mtime almost certainly does not matter.
      buf->atime = 0;
      buf->ctime = 0;
      buf->mtime = 0;
    }
    rc = (attrs != 0xFFFFFFFF) ? 0 : -1;
  }
#endif

  return (rc);
}

/**
 * Change the last modified date/time of a file to be the
 * same as another file.  Doesn't allow symbolic links to
 * be modified.
**/
int ODEclonetime( char *src, char *dst )
{
  struct stat srcstat, dststat;
  struct utimbuf dsttime;

  if (stat( src, &srcstat ) == 0)
  {
#if !defined(NO_SYMLINKS)
    if (lstat( dst, &dststat ) != 0 || S_ISLNK( dststat.st_mode ))
      return (-1);
#endif
    dsttime.actime  = srcstat.st_atime;
    dsttime.modtime = srcstat.st_mtime;
    return (utime( dst, &dsttime ));
  }
  return (-1);
}

/**
 * Set the file mode (permissions) of a file to be the
 * same as another file.
 */
int ODEclonemode( char *src, char *dst )
{
  struct stat srcstat;
#ifdef UNIX
  struct stat dststat;
  mode_t filemode;
#else
  int filemode;
#endif

  if (stat( src, &srcstat ) != 0)
    return (-1);

#ifdef UNIX
  filemode = srcstat.st_mode & (S_IRUSR | S_IWUSR | S_IXUSR |
      S_IRGRP | S_IWGRP | S_IXGRP | S_IROTH | S_IWOTH | S_IXOTH);
#if !defined(NO_SYMLINKS)
  if (lstat( dst, &dststat ) != 0 || S_ISLNK( dststat.st_mode ))
    return (-1);
#endif
#else
  filemode = srcstat.st_mode & (S_IREAD | S_IWRITE | S_IEXEC);
#endif
  return (chmod( dst, filemode ));
}

/**
 * Touch a file.  This updates the file's last modified time
 * to be the current time.
 */
int ODEtouch( char *name )
{
  return (utime( name, (struct utimbuf *)0 ));
}

/**
 * Create a symbolic link.
 *
 * @param pathname The original file or directory to which
 * the link will point.
 * @param linkname The name for the symbolic link.
 * @return 0 if successful, nonzero if not.
 */
int ODEsymlink( char *pathname, char *linkname )
{
#if !defined(NO_SYMLINKS)
  if (pathname != 0 && linkname != 0 && symlink( pathname, linkname ) == 0)
    return (0);
#endif
  return (-1);
}


/**
 * Delete a file.  On VMS, filename should be in VMS format.
 *
**/
int ODEremove( const char *filename )
{
  int rc;

#ifdef VMS
  char *buf;
  buf = malloc( sizeof( char ) * (strlen( filename ) + 3) );
  if (buf)
  {
    strcpy( buf, filename );
    strcat( buf, ";*" ); /* remove all versions of the file */
    filename = buf;
  }
#endif

  rc = remove( filename );

#ifdef VMS
  if (buf)
    free( buf );
#endif

  return (rc);
}
