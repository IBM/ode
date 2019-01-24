/**
 * Provides both a convenient static wrapper for many
 * java.io.File functions, as well as some ODE-specific
 * functions.  In most cases null is returned on error
 * instead of throwing an exception.  It also takes care
 * of portability issues where appropriate, such as
 * normalizing or canonicalizing paths.
**/

using namespace std;
#include <stdio.h>
#include <string.h>

#define _ODE_LIB_IO_PATH_CPP_
#include "lib/io/path.hpp"

#include "base/odebase.hpp"
#include "lib/exceptn/ioexcept.hpp"
#include "lib/io/ui.hpp"
#include "lib/io/file.hpp"
#include "lib/portable/env.hpp"
#include "lib/string/pattern.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/string/strcon.hpp"
#include "lib/portable/native/proc.h"
#include "lib/portable/native/dirent.h"
#include "lib/portable/native/sleep.h"
#include "lib/portable/hashtabl.hpp"

#ifdef USE_OPENMODE_TYPE
typedef ios::openmode FILE_MODES_TYPE;
#else
typedef int FILE_MODES_TYPE;
#endif

const String Path::SUFFIX_STRING = ".";


/**
 * The system-dependent "\" or "/".
 * The system-dependent ":" or ";"
**/
#if defined(DEFAULT_SHELL_IS_CMD)
const String Path::DIR_SEPARATOR     = "\\";
const char Path::DIR_SEPARATOR_CHAR  = '\\';
const String Path::PATH_SEPARATOR    = ";";
const char Path::PATH_SEPARATOR_CHAR = ';';
#else
const String Path::DIR_SEPARATOR     = "/";
const char Path::DIR_SEPARATOR_CHAR  = '/';
const String Path::PATH_SEPARATOR    = ":";
const char Path::PATH_SEPARATOR_CHAR = ':';
#endif

/**
 * Used for getDirContents() content_type parameter.
 */
const int Path::CONTENTS_BOTH = 0;
/**
 * Used for getDirContents() content_type parameter.
 */
const int Path::CONTENTS_FILES = 1;
/**
 * Used for getDirContents() content_type parameter.
 */
const int Path::CONTENTS_DIRS = 2;

const int Path::TMPFILE_RAND_MAX = 999;
int Path::tmpfile_rand = 0;
String Path::current_dir( Path::_getcwd() );

/**
 * This is mainly used in getcwd(). Unix systems require that the size of
 * the buffer be non zero.
 */
const int Path::MAX_PATH_LENGTH = 1024;

#define READLINE_BUFSIZE 80

// how long to wait for SIGINT when readLine gets EOF
#define ODE_SIGNAL_WAIT_MSEC 750


#ifdef DEFAULT_SHELL_IS_VMS
String &Path::unixizeThisVMS( String &result )
{
  if (result.indexOf( '[' ) == STRING_NOTFOUND &&
      result.indexOf( ':' ) == STRING_NOTFOUND)
    return (result);  // no bracket or colon means not in VMS format

  String newstr;
  int idx = result.indexOf( ':' );

  if (idx != STRING_NOTFOUND)
  {
    newstr += '/';
    newstr += result.substring( STRING_FIRST_INDEX, idx++ );
    newstr += '/';
  }
  else
    idx = STRING_FIRST_INDEX;

  // if malformed or just a filename, return as-is
  if (result[idx++] != '[')
  {
    return (result);
  }

  boolean seen_close_bracket = false;

  while (idx <= result.lastIndex())
  {
    switch (result[idx])
    {
      case '-':
        newstr += "../";
        break;
      case '.':
        if (seen_close_bracket)
          newstr += '.';
        else if (newstr.length() == 0)
          newstr += "./";
        else if (!newstr.endsWith( StringConstants::FORW_SLASH ))
          newstr += '/';
        break;
      case ']':
        seen_close_bracket = true;
        if (newstr.length() == 0)
          newstr += "./";
        else if (!newstr.endsWith( StringConstants::FORW_SLASH ))
          newstr += '/';
        break;
      default:
        if (newstr.length() == 0)
          newstr += '/';
        newstr += result[idx];
    }

    idx++;
  }

  if (newstr.endsWith( StringConstants::FORW_SLASH ))
    newstr.remove( newstr.lastIndex(), 1 );

  result = newstr;
  return (result);
}


String &Path::unix2vmsThis( String &result )
{
  int idx;

  if (result.indexOf( '[' ) != STRING_NOTFOUND ||
      result.indexOf( ':' ) != STRING_NOTFOUND)
    return (result);  // a bracket or colon means already in VMS format

  canonicalizeThis( result, false );
  if (result.length() <= 1)
    result = getcwd();

  result.remove( STRING_FIRST_INDEX, 1 ); // remove leading slash

  if (result.indexOf( '.' ) == STRING_NOTFOUND)
    result += ']';
  else if ((idx = result.lastIndexOf( '/' )) != STRING_NOTFOUND)
    result[idx] = ']';

  if (result.indexOf( '/' ) == STRING_NOTFOUND)
    result.replaceThis( ']', ':', 1 );
  else
    result.replaceThis( "/", ":[", 1 );
  result.replaceThis( '/', '.' );
  return (result);
}


boolean Path::absoluteVMS( const String &path )
{
  const char *ptr = path.toCharPtr();
  int colon_idx, length = path.length();

  // check if Unix style absolute path first
  if (path.startsWith( StringConstants::FORW_SLASH ))
    return (true);

  if ((colon_idx = path.indexOf( ':' )) != STRING_NOTFOUND)
  {
    ptr += colon_idx - STRING_FIRST_INDEX + 1; // go one char beyond the ':'
    length = path.lastIndex() - colon_idx;
  }

  if (length > 2 && *ptr == '[' && *(ptr+1) != ']' &&
      *(ptr+1) != '.' && *(ptr+1) != '-')
    return (true);

  return (false);
}
#endif /* VMS section */


/******************************************************************************
 * Test if a file/directory path is in absolute format
 * Note: Assume path has been "normalized"
 *
 * @param path The path to test
 * @return '\0' if a relative path or '\\', '/', or "c:\\" if an
 * absolute path
 */
String Path::getAbsolutePrefix( const String &path )
{
  char prefix[4];
#if defined(DEFAULT_SHELL_IS_VMS)
  int idx;
#endif

  prefix[0] = '\0';

  if (path.length() == 0)
    return (StringConstants::EMPTY_STRING);
  else if (path[STRING_FIRST_INDEX] == '/' || path[STRING_FIRST_INDEX] == '\\')
  {
    prefix[0] = path[STRING_FIRST_INDEX];
    prefix[1] = '\0';
    return (String(prefix));
  }
#if defined(DEFAULT_SHELL_IS_CMD)
  else if ( (path.length() > 2) &&
     (isalpha( path.charAt( path.firstIndex() ) ) ) &&
     (path.charAt( path.firstIndex() + 1 ) == ':' ) &&
     ((path.charAt( path.firstIndex() + 2 ) == '/' ) ||
     (path.charAt( path.firstIndex() + 2 ) == '\\') ) )
  {
      prefix[0] = path.charAt( path.firstIndex() );
      prefix[1] = ':';
      prefix[2] = path.charAt(path.firstIndex() + 2);
      prefix[3]='\0';
      return (String(prefix));
  }
#elif defined(DEFAULT_SHELL_IS_VMS)
  else if ((idx = path.indexOf( ':' )) != STRING_NOTFOUND)
    return (path.substring( STRING_FIRST_INDEX, idx + 1 ));
#endif
  else   // Is a relative path
     return (StringConstants::EMPTY_STRING);
}


/**
 * Gets the current directory based on SANDBOXBASE, if it
 * is defined (otherwise, just calls getcwd).  This way,
 * it may have symlinks in it and thus can be use to directly
 * compare with other environment variables and data derived from
 * SANDBOXBASE (et al.).
**/
String Path::getCwdFromEnviron()
{
  SmartCaseString origbase( SandboxConstants::getSANDBOXBASE() );
  SmartCaseString cwd( Path::getcwd() );
  SmartCaseString sbbase( origbase ); // this one gets fully canonicalized

  Path::fullyCanonicalize( sbbase ); // resolve symlinks for comparison
  if (sbbase.length() > 0)
  {
    sbbase += Path::DIR_SEPARATOR; // insure this ends on a cwd dirsep boundary
    // cwd is already fully canonicalized

    // are we in a subdir of the sandbox?
    if (cwd.startsWith( sbbase ))
    {
      cwd.substringThis( sbbase.lastIndex() );
      cwd.prepend( origbase ); // prepend the uncanonicalized source base
    }
  }

  return (cwd);
}

/**
 * Private "real" getcwd function.
 * Returns pointer to an empty string if unable to get cwd.
**/
const char *Path::_getcwd()
{
  static char tmpcwd[MAX_PATH_LENGTH + 1];
  if (ODEgetcwd( tmpcwd, MAX_PATH_LENGTH + 1 ) == 0)
    tmpcwd[0] = '\0';
  return (tmpcwd);
}

/**
 * Change to another directory.  Puts the results in
 * a class variable so that getcwd() can be as fast as
 * Powdered Toast Man on Saturday morning.
**/
boolean Path::setcwd( const File &path )
{
  if (ODEsetcwd( path.toCharPtr() ) == 0)
  {
    // must get the fully canonicalized version to store in current_dir
    // since path.toString() might contain symlinks.
    const char *cwd = Path::_getcwd(); // make sure this is ok
    if (cwd[0] != '\0')
    {
      current_dir = cwd;
      return (true);
    }
  }

  return (false);
}


/**
 * Get the user's home directory.
 *
 * @return A string containing the user's home directory.
**/
const String Path::gethome()
{
  const String *home = Env::getenv( StringConstants::HOME_VAR );

  if (home == 0)
  {
#ifdef DEFAULT_SHELL_IS_CMD
    const String *homeDrive = Env::getenv( "HOMEDRIVE" );
    const String *homePath = Env::getenv( "HOMEPATH" );

    if ((homeDrive == 0) || (homePath == 0))
      return (StringConstants::EMPTY_STRING);
    else
      return ( *homeDrive + DIR_SEPARATOR + *homePath );
#else
    return (StringConstants::EMPTY_STRING);
#endif
  }
  else
    return (*home);
}


/******************************************************************************
 * Make a directory path.  Intermediate directories will be
 * created as needed.
 *
 * @param path The path to create.
 * @return True if path was created, false if not.
 */
boolean Path::createPath( const String &path )
{
  char *buf = new char[path.length() + 1];
  char *ptr;
  int i = 0;
  boolean return_val = true;
  char *prefix;
  String tempPrefix( getAbsolutePrefix( path ) );
  prefix = tempPrefix.toCharPtr();
  ptr = path.toCharPtr();

  // Get prefix if path is an absolute path and prepend to buf
  // Unix style root-directory prefix
  if ((prefix[0] == '/') || (prefix[0] == '\\'))
  {
     strcpy(buf, prefix);
     i = strlen(prefix);
     ptr += i;
  }
  // DOS style c:\ directory prefix
  else if (prefix[0] != '\0')
  {
     strcpy(buf, prefix);
     ptr += 3;
     i += 3;
  }

  while ((*ptr != '\0') && (return_val))
  {
    if ((*ptr != '/') && (*ptr != '\\'))
    {
      buf[i] = *ptr;
      i++;
    }
    ptr++;
    if (((*ptr == '/') || (*ptr == '\\') || (*ptr == '\0')) &&
        buf[i-1] != DIR_SEPARATOR_CHAR)
    {
      buf[i] = '\0';
      if (!exists(buf))
      {
        if (Interface::isDebug())
          Interface::printDebug( String( "About to create path: " ) +
              String( buf ) );
        if (ODEmkdir(buf) != 0)
        {
#ifdef LAZY_DIR_CREATION
     // mkpath may fail when run in parallel. So sleep for a maximum
     // of 10 sec or until the path is created
          for (int i=0; i<200; i++)
          {
            if (Path::exists( buf ))
              break;
            else
              ODEsleep( 50 );
          }
          if (!Path::exists( buf ))
            return_val = false;
#else
           return_val = false;
#endif
        }
      }
      else
      {
        if ( isFile( buf ))
           return_val = false;      //Already exists as a file.
      }
      buf[i] = DIR_SEPARATOR_CHAR;
      i++;
    }
  }

  delete[] buf;

  return (return_val);
}

/******************************************************************************
 *
 */
boolean Path::deleteFullPath( const String &fullname,
      boolean all, boolean keepgoing )
{
  if (Interface::isDebug())
    Interface::printDebug( "in delete full path: " + fullname );
  String      fullpath = fullname;
  StringArray filelist;

  // might be a directory link, which we do NOT want
  // to recurse into, so just try to delete blindly first.
#ifdef DEFAULT_SHELL_IS_VMS
  if (ODEremove( Path::unix2vms( fullpath ).toCharPtr() ) == 0)
#else
  if (ODEremove( fullpath.toCharPtr() ) == 0)
#endif
    return (true); // cool, nothing else to do

  File tmpfile( fullpath, true );
  if (!tmpfile.doesExist())
    return (true);
  if (tmpfile.isFile() || tmpfile.isLink()) // couldn't delete a file/link?
  {
    if (Interface::isDebug())
      Interface::printDebug( "Unable to delete file/link: " + fullpath );
    return (false);
  }

  // must be a non-empty directory, so recurse if user wants to
  if (all) // remove all files and subdirectories
  {
    // get lists of all files/dirs
    getDirContents( fullpath, false, CONTENTS_BOTH, &filelist, &tmpfile );

    // make sure fullpath ends with a directory separator string
    if (!fullpath.endsWith( DIR_SEPARATOR ))
      fullpath += DIR_SEPARATOR;

    // delete all directory contents first
    for (int i = filelist.firstIndex(); i <= filelist.lastIndex(); ++i)
      if (!deleteFullPath( fullpath + filelist[i], all, keepgoing ) &&
          !keepgoing)
        return (false); // stop prematurely
  }

#ifdef TRAILING_PERIOD_FOR_DIRS
  if (fullpath.endsWith( DIR_SEPARATOR ))
    fullpath += StringConstants::PERIOD;
#endif

#ifdef NO_TRAILING_SLASH_FOR_DIRS
  while (fullpath.endsWith( DIR_SEPARATOR ) && fullpath.length() > 1)
    fullpath.remove( fullpath.lastIndex(), 1 );
#endif

  if (ODErmdir( fullpath.toCharPtr() ) == 0) // now delete the directory itself
    return true;
  else
  {
    if (Interface::isDebug())
      Interface::printDebug( "Unable to delete dir: " + fullpath );
    return false;
  }
}


/******************************************************************************
 * Get a list of all files and/or subdirectories in a
 * directory.  "." and ".." entries are always ignored.
 *
 * @param path The directory of which to return the contents.
 * @param fullpaths If true, return the full path for each
 * file.  If false, just return the filenames.
 * @param content_type If CONTENTS_BOTH, returns both files
 * and subdirectories.  If CONTENTS_FILES, returns only files.
 * If CONTENTS_DIRS, returns only subdirectories.
 * @return An array containing each directory entry, or
 * null if directory is empty or doesn't exist.
 */
Array<File> *Path::getDirContentsAsFiles( const String &path,
      int content_type, Array<File> *buf,
      File *filep, const String& pattern )
{
  String basedir;
  int count, i;
  File *tmpfilep;
  if (filep == 0)
    tmpfilep = new File( path, true );
  else
    tmpfilep = filep;

  if (!tmpfilep->isDir())
  {
    if (filep == 0)
      delete tmpfilep;
    return (0);
  }

  Array<File> *fulllist = (buf == 0) ? new Array<File>() : buf;
  StringArray tmplist;

  if (Interface::isDebug())
    Interface::printDebug( "about to get dir contents" );
  // get list of all files/dirs
  getDirContents( tmpfilep->toString(), pattern, &tmplist, tmpfilep );
  if (Interface::isDebug())
    Interface::printDebug( "got dir contents" );

  basedir = tmpfilep->toString();

  ODEsetcwd( basedir.toCharPtr() );

  // make sure basedir ends with a directory separator string
  if (!basedir.endsWith( DIR_SEPARATOR ))
    basedir += DIR_SEPARATOR;

  for (i=tmplist.firstIndex(), count=1; i <= tmplist.lastIndex(); ++i)
  {
    File tmpfile( basedir, tmplist[i] );
    if (content_type != CONTENTS_BOTH)
    {
      if (tmpfile.isFile() && content_type == CONTENTS_DIRS)
        continue;
      if (tmpfile.isDir() && content_type == CONTENTS_FILES)
        continue;
    }
    fulllist->add( tmpfile );
    count++;
  }

  ODEsetcwd( Path::getcwd().toCharPtr() );

  if (filep == 0)
    delete tmpfilep;

  if (count < 2)
  {
    if (fulllist != buf)
     {
      delete fulllist;
      return (0);
    }
  }

  return (fulllist);
}


/******************************************************************************
 * Get a list of all files and/or subdirectories in a
 * directory.  "." and ".." entries are always ignored.
 *
 * @param path The directory of which to return the contents.
 * @param fullpaths If true, return the full path for each
 * file.  If false, just return the filenames.
 * @param content_type If CONTENTS_BOTH, returns both files
 * and subdirectories.  If CONTENTS_FILES, returns only files.
 * If CONTENTS_DIRS, returns only subdirectories.
 * @return An array containing each directory entry, or
 * null if directory is empty or doesn't exist.
 */
StringArray *Path::getDirContents( const String &path,
      boolean fullpaths, int content_type, StringArray *buf,
      File *filep, const String &pattern )
{
  String basedir, fullpath, newpath = normalize( path );
  int count, i;
  File *tmpfilep;
  if (filep == 0)
    tmpfilep = new File( path, true );
  else
    tmpfilep = filep;

  if (!tmpfilep->isDir())
  {
    if (filep == 0)
      delete tmpfilep;
    return (0);
  }

  StringArray tmplist, *fulllist = (buf == 0) ? new StringArray() : buf;

  if (Interface::isDebug())
    Interface::printDebug( "about to get dir contents" );
  // get list of all files/dirs
  getDirContents( newpath, pattern, &tmplist, tmpfilep );
  if (Interface::isDebug())
    Interface::printDebug( "got dir contents" );

  basedir = tmpfilep->toString();

  // make sure basedir ends with a directory separator string
  if (!basedir.endsWith( DIR_SEPARATOR ))
    basedir += DIR_SEPARATOR;
  for (i=tmplist.firstIndex(), count=1; i <= tmplist.lastIndex(); ++i)
  {
    fullpath = basedir + tmplist[i];
    if (content_type != CONTENTS_BOTH)
    {
      if (isFile( fullpath ) && content_type == CONTENTS_DIRS)
        continue;
      if (isDirectory( fullpath ) && content_type == CONTENTS_FILES)
        continue;
    }
    if (fullpaths)
      fulllist->add( fullpath );
    else
      fulllist->add( String( tmplist[i] ) );
    count++;
  }

  if (count < 2)
  {
    if (fulllist != buf)
      delete fulllist;
    if (filep == 0)
      delete tmpfilep;
    return (0);
  }

  if (filep == 0)
    delete tmpfilep;
  return (fulllist);
}


/******************************************************************************
 * Uses "native" functions from "lib\portable\native" to read in
 * the contents of the dir.
 */
StringArray *Path::getDirContents( const String &path,
    const String &pattern, StringArray *buf,
    File *filep  )
{
  String file;
  StringArray *contents = (buf == 0) ? new StringArray() : buf;
  ODEDIR *dirp;
  ODEDIRENT entry;

  if (filep == 0)
    dirp = ODEopendir( canonicalize( path, false ).toCharPtr() );
  else
    dirp = ODEopendir( filep->toCharPtr() );

  if (Interface::isDebug())
    Interface::printDebug( "opened dir - about to read dir: " + path );
  while (ODEreaddir( dirp, &entry ) == 0)
  {
    file = entry.d_name;
    if (Interface::isDebug())
      Interface::printDebug( "read content: " + file );

    if (file.equals( StringConstants::PERIOD ) || file.equals( ".." ))
      continue;

    // get files for the specified pattern - Don't bother checking
    // if the pattern is "*".
    if (!pattern.equals( StringConstants::STAR_SIGN ) &&
        !Pattern::isMatching( pattern, file,
        PlatformConstants::onCaseSensitiveOS() ))
      continue;

    // got a match
    contents->add( file );
  }

  ODEclosedir( dirp );
  if (Interface::isDebug())
    Interface::printDebug( "closed dir" );
  return (contents);
}


String &Path::canonicalizeThis( String &result, boolean checkexists,
  boolean getabsolutepath, const String &curdir )
{
  int limit = 0; // char index of character to stop at (usu. the 1st slash)

  if ((result.length() < 1) || (checkexists && !exists( result )))
  {
    result = StringConstants::EMPTY_STRING;
    return (result);
  }

#ifdef DEFAULT_SHELL_IS_VMS
  if (result.indexOf( '[' ) != STRING_NOTFOUND ||
      result.indexOf( ':' ) != STRING_NOTFOUND) // in VMS format, don't canon
    return (result);
#endif

#ifdef DEFAULT_SHELL_IS_CMD
  limit = 2; // assume path will be of the form "C:\xyz"
  if (!absolute( result ))
  {
    String currentDir = (!absolute( curdir ) ? getcwd() :
        canonicalize( curdir, false )); // prepend current disk if none
    // Is result of form d: or d:path where path does not start with slash?
    if (result.length() > 1 && isalpha( result[STRING_FIRST_INDEX] ) &&
        result[STRING_FIRST_INDEX + 1] == ':')
    {
      // is the disk the same as current disk?
      if (tolower( result[STRING_FIRST_INDEX]) ==
          tolower( currentDir[STRING_FIRST_INDEX] ))
      {
        result = currentDir + DIR_SEPARATOR +
                 result.substring( STRING_FIRST_INDEX + 2 );
      }
      else
      {
        // Disk not same as current disk, so we must get path without the disk.
        // Ignore what curdir or getcwd have, and use disk that result has.
        String newresult = result.substring( STRING_FIRST_INDEX + 2 );
        String oldcwd = getcwd(); // save where we are
        String newcwd;
        if (ODEsetcwd( result.substring( STRING_FIRST_INDEX,
            STRING_FIRST_INDEX + 2 ).toCharPtr() ) != 0)
        { // could not go to other disk so give it the root of the
          // non-existant disk as the current directory
          newresult.prepend( DIR_SEPARATOR )
                   .prepend( result.substring( STRING_FIRST_INDEX,
                                               STRING_FIRST_INDEX + 2 ) );
          Path::canonicalizeThis( newresult, checkexists, getabsolutepath );
        }
        else
        {
          newcwd = _getcwd();
          if (newresult.length() > 0)
          {
            if (newcwd.length() > 3)
              newresult.prepend( DIR_SEPARATOR ).prepend( newcwd );
            else // it should be of the form "d:\"
              newresult.prepend( newcwd );
          }
          else
            newresult = newcwd;
          Path::canonicalizeThis( newresult, checkexists, getabsolutepath );
          setcwd( oldcwd );
        }
        result = newresult;
        return( result );
      }
    }
    else
      result.prepend( DIR_SEPARATOR ).prepend( currentDir );
  }
  // if we have a possible UNC type path, e.g. "\\machine\dir", don't
  // try to prepend a drive letter.
  if (result.length() > 2 && (result[STRING_FIRST_INDEX + 1] == '/' ||
       result[STRING_FIRST_INDEX + 1] == '\\') &&
       isalpha( result[STRING_FIRST_INDEX + 2] ))
  {
    limit = 1; // assume path is UNC path, e.g. "\\xyz"
  }
  // enforce that the string start with the "C:\" format
  else if (result.length() < 3 || !isalpha( result[STRING_FIRST_INDEX] ) ||
      result[STRING_FIRST_INDEX + 1] != ':')
  {
    result.prepend( "x:\\" ); // don't worry, we change the drive letter next
    // getcwd guarantees a canonicalized string with the drive, so use it
    result[STRING_FIRST_INDEX] = Path::getcwd()[STRING_FIRST_INDEX];
  }
#else /* DEFAULT_SHELL_IS_CMD */
  if (!absolute( result ))
    result.prepend( DIR_SEPARATOR ).prepend(
                   (!absolute( curdir ) ? getcwd() : curdir) );
#endif

  normalizeThis( result );

  char *string = result.toCharPtr();
  int  i = result.lastIndex() - result.firstIndex(); // the last char* index
  int  j = i, k = i;
  int  count = 0; // number of ".."'s

  while (i >= 0) // at the start of each iteration a DIR_SEP has just been seen
  {
    if (i > limit && string[i] == '.' && string[i-1] == DIR_SEPARATOR_CHAR)
    {
      if (i == limit + 1) // string == "/."
        i--;
      else
        i -= 2; // if ".", ignore it.
    }
    else if (i >= limit + 2 && string[i] == '.' &&
        string[i - 1] == '.' && string[i - 2] == DIR_SEPARATOR_CHAR)
    { // if ".." increment counter for # of components to remove
      if (i == limit + 2) // string == "/.."
        i -= 2;
      else
        i -= 3;
      ++count;
    }
    else if ((i > limit || j < k) &&
        (string[i] == DIR_SEPARATOR_CHAR && i > 0)) // ignore extra /'s
      --i;
    else if (count > 0) // if counter > 0 and if we find a proper component
    {
      while (i > limit && string[--i] != DIR_SEPARATOR_CHAR)
        /* keep going */;
      --count;
    }
    else do // copy valid components to the end of the string...
    {
      string[j--] = string[i--];
    } while (i >= 0 && string[i + 1] != DIR_SEPARATOR_CHAR);
  }

  result.substringThis( result.firstIndex() + j + 1 );
  return (result);
}


/**
 * readLine
 *
 * Reads a line of text from a stream up to a newline sequence,
 * and appends it (not including the newline) to the buffer.
 *
 * @return true on success.  false if buffer is a null pointer,
 * or when EOF is reached.
 * Note that if EOF is encountered on a non-empty line before
 * a newline is found, the characters up to the EOF will be
 * appended to buffer and true is returned (false will be
 * returned on the following call).
**/
boolean Path::readLine( istream &fdr, String *buffer )
{
  static char ip[READLINE_BUFSIZE + 1];
  int ch;
  int count = 0;

  if (buffer == 0)
    return (false);
  *buffer = StringConstants::EMPTY_STRING;
  do
  {
    ch = fdr.get();

#ifdef SLOW_SIGNALS
    // only wait for death if istream is stdin (interactive)
    if (ch == EOF && fdr == cin)
      ODEsleep( ODE_SIGNAL_WAIT_MSEC );
#endif

    switch (ch)
    {
      case '\r':
        if (fdr.peek() == '\n') // EOL for OS2 & DOS/Win
          continue; // skip \r and act only on the \n next time around
        /* FALL THROUGH */
      case '\n':
      case EOF:
        ip[count] = '\0';
        *buffer += String( ip );
        return (buffer->length() > 0 || ch != EOF);
      default:
        if (count >= READLINE_BUFSIZE)
        {
          ip[count] = '\0';
          *buffer += String( ip );
          count = 0;
        }
        ip[count++] = ch;
    }
  } while (ch != EOF);

  return (true);
}

/**
 *
**/
void Path::closeFileReader( ifstream *file )
{
  file->close();
  delete file;
}


/**
 *
**/
void Path::closeFileWriter( fstream *file )
{
  file->close();
  delete file;
}


/******************************************************************************
 * Returns the name portion of a path.  This is, in effect,
 * everything following the last directory separator.
 */
String &Path::fileNameThis( String &pathname )
{
  if (pathname.length() == 0)
    return (pathname);
  int index = pathname.lastIndexOfAny( "/\\" );
  if (index == pathname.lastIndex())
    pathname = StringConstants::EMPTY_STRING;
  else if (index != STRING_NOTFOUND)
    pathname.substringThis( index + 1 );
#if defined(DEFAULT_SHELL_IS_CMD)
  else if (pathname.length() > 1 && isalpha( pathname[STRING_FIRST_INDEX] ) &&
           pathname[STRING_FIRST_INDEX + 1] == ':')
  { // argument of form d: or d:file
    if (pathname.length() == 2)
      pathname = StringConstants::EMPTY_STRING;
    else
      pathname.substringThis( STRING_FIRST_INDEX + 2 );
  }
#elif defined(DEFAULT_SHELL_IS_VMS)
  else if ((index = pathname.lastIndexOf( ']' )) != STRING_NOTFOUND)
    pathname.substringThis( index + 1 );
#endif
  return (pathname);
}


/**
 * Returns the path portion of a path.  This is, in effect,
 * everything preceding the last directory separator.
**/
String &Path::filePathThis( String &pathname )
{
  if (pathname.length() == 0)
    return (pathname);
  int index=pathname.lastIndexOfAny( "/\\" );
  if (index == STRING_NOTFOUND)
  {
#if defined(DEFAULT_SHELL_IS_CMD)
    // check if form of d: or d:file
    if (pathname.length() > 1 && isalpha( pathname[STRING_FIRST_INDEX] ) &&
        pathname[STRING_FIRST_INDEX + 1] == ':')
      pathname.substringThis( STRING_FIRST_INDEX,
                              STRING_FIRST_INDEX + 2 ).
                              append( StringConstants::PERIOD );
    else
#elif defined(DEFAULT_SHELL_IS_VMS)
    if ((index = pathname.lastIndexOf( ']' )) != STRING_NOTFOUND)
      pathname.remove( index + 1, pathname.lastIndex() - index );
    else
#endif
      pathname = StringConstants::PERIOD;
  }
  else if (index == pathname.firstIndex())
    pathname = StringConstants::EMPTY_STRING;
  else
    pathname.remove( index, pathname.lastIndex() - index + 1 );
  return (pathname);
}


String Path::removeExtraSlashes( const String &pathname )
{
  char* buf = new char[pathname.length() + 1];
  char* ptr;
  char* tmpptr1;
  char* tmpptr2;
  int i = 0;

  ptr = pathname.toCharPtr();

  while (*ptr != '\0')
  {
      if (i == 0)
      {
         if ((*ptr == '\\') || (*ptr == '/')) {

            // set first buf char to a slash
            buf[0] = DIR_SEPARATOR_CHAR;
            i++;        // i = 1

            // set tmp ptrs;
            tmpptr1 = ptr + 1;
            tmpptr2 = ptr + 2;

            // have three or more slashes
            if ( ( (*tmpptr1 == '/') || (*tmpptr1 == '\\')) &&
                 ( (*tmpptr2 == '/') || (*tmpptr2 == '\\')) )
            {
               while ((*ptr == '\\') || (*ptr == '/'))
                  ptr++;
               buf[1] = *ptr;
               i++;  // i = 2
            }

            // only have two slashes - keep for UNC path
            else if ( (*tmpptr1 == '/') || (*tmpptr1 == '\\'))
            {
               buf[1] = DIR_SEPARATOR_CHAR;
               ptr++;
               i++;   // i = 2
            }

            // do nothing for one leading slash

         }
         else // first char not a slash - set buf[0] = ptr
         {
            buf[0] = *ptr;
            i++;        // i = 1
         }

      }

      // No slash - copy into buffer
      else if ((*ptr != '/') && (*ptr != '\\'))
      {
        buf[i] = *ptr;
        i++;
      }

      // Slash not preceeded by another slash
      else if (buf[i-1] != DIR_SEPARATOR_CHAR)
      {
        buf[i] = DIR_SEPARATOR_CHAR;
        i++;
      }
      ptr++;
   }

  // Remove last slash and null terminate
  if (buf[i-1] == DIR_SEPARATOR_CHAR)
     buf[i-1] = '\0';
  else
     buf[i] = '\0';

  String return_str(buf);
  delete[] buf;

  return(return_str);
}


/******************************************************************************
 * Returns the suffix portion of a path/file.  This value may
 * depend on the value of smart: if smart is true, fileName()
 * is called first to ensure the suffix is searched for only
 * in the last component of the path.  If smart is false,
 * everthing following the last suffix character is returned
 * (even if it is part of a directory name and not a filename).
 * The makefile string modifer "E" uses smart==false.
 */
String &Path::fileSuffixThis( String &pathname, boolean smart )
{
  int root_end_idx, suff_beg_idx;

  if (pathname.length() == 0)
    return (pathname);

  if (smart)
    fileNameThis( pathname );

  root_end_idx = indexOfSplit( suff_beg_idx, pathname );
  return( pathname.substringThis( suff_beg_idx ) );
}


/******************************************************************************
 * Returns the root portion of a path/file.  This value may
 * depend on the value of smart: if smart is true, fileName()
 * is called first to ensure the root is searched for only
 * in the last component of the path.  If smart is false,
 * everthing preceding the last suffix character is returned
 * (which could include the path or part of the path).
 * The makefile string modifer "R" uses smart==false.
 */
String &Path::fileRootThis( String &pathname, boolean smart )
{
  int root_end_idx, suff_beg_idx;

  if (pathname.length() == 0)
    return (pathname);

  if (smart)
    fileNameThis( pathname );

  root_end_idx = indexOfSplit( suff_beg_idx, pathname );

  // We need to add one to parameter 2 because "substringThis" stops at
  // index prior to the value of parameter 2.
  return ( pathname.substringThis( pathname.firstIndex(), root_end_idx + 1 ) );
}


/******************************************************************************
 * This function is a combination of fileRootThis() and fileSuffixThis().
 * The result StringArray will contain (0) the complete filename without the
 * vertical bar "|" separator, and (1) the suffix by itself, and (2) the
 * root by itself.
 */
void Path::fileRootSuffixThis( const String &pathname, StringArray &result )
{
  int root_end_idx, suff_beg_idx;
  String root, suffix;

  if (pathname.length() != 0)
  {

    String tmppathname( pathname );
    root_end_idx = indexOfSplit( suff_beg_idx, tmppathname );

    // We need to add one to parameter 2 because "substringThis" stops at
    // index prior to the value of parameter 2.
    root = tmppathname.substring( tmppathname.firstIndex(), root_end_idx + 1);

    suffix = tmppathname.substring( suff_beg_idx );

    // Reconstruct the new targetname without the special character.
    result.append( root+suffix );
    result.append( suffix );
    result.append( root );
  }
  else //We are given an empty string, so all results are empty strings.
  {
    result.append( StringConstants::EMPTY_STRING );
    result.append( StringConstants::EMPTY_STRING );
    result.append( StringConstants::EMPTY_STRING );
  }
}


/******************************************************************************
 * indexOfSplit() will return the index of the last character of the root.
 * It will also set parameter1 to the index of the first character of the suffix
 *
 * Consider the example where pathname is "foo|.c"   The "|" separates the
 * root and suffix.
 * This function will return 3, and set parameter1 to 5.
 *
 * Note1: If there is no root, then the returned value will be 0
 * Note2: If there is no suffix, then parameter1 will be one more than the
 *        index of the last character of the given string.
 * Note3: The pathname given will be split based on the following priorities.
 *        First priority is given to the last vertical bar "|"
 *        Second priority is given to the last period "."
 *        If neither a "|" or "." is present, then it is all root and no suffix
 */
int Path::indexOfSplit( int &suffix_beg, String &pathname )
{
  int fst_idx  = pathname.firstIndex();
  int sep_idx  = pathname.lastIndexOf(StringConstants::BAR);
  int last_idx = 0;


  // First look for the suffix separator character. "|"
  //
  if ( sep_idx >= fst_idx )
  {
    // This is the case that a "|" is present. Ex. "foo|.c"
    //
    suffix_beg = sep_idx + 1;
    return( sep_idx - 1 );
  }
  else
  {
    int suff_idx = pathname.lastIndexOf('.');
    last_idx = pathname.lastIndex();

    // Next look for the default separator "."
    //
    if ( suff_idx >= fst_idx )
    {
      int path_idx = pathname.lastIndexOf(Path::DIR_SEPARATOR_CHAR);

      if ( (path_idx > fst_idx) && (suff_idx < path_idx) )
      {
        // This is the case of a target like ../../dir1/targ
        //  suff_idx with point to the directory not a suffix
        //
        suffix_beg = last_idx + 1;
        return( last_idx );
      }
      else
      {
        // This is the typical case like  Ex. "foo.c"
        //
        suffix_beg = suff_idx;
        return( suff_idx - 1 );
      }
    }
  }

  // If we havent already exited, then it must be the case that the filename
  // is all root with no suffix.  Ex. "foobar"
  //
  suffix_beg = last_idx + 1;
  return( last_idx );
}


/******************************************************************************
 * Returns the full pathname for filename.
 * The file is searched in each directory (starting at
 * array index zero)
 * of the path chain (an array, each element of which
 * contains a single directory).
 * If filename is absolute and exists, it will be returned
 * as-is, without any search of the path chain.
 */
String Path::findFileInChain( const String &filename,
  const StringArray &path_chain, const String &dirsep )
{
  String fullname;

  if (filename.trim() == StringConstants::EMPTY_STRING)
    return (StringConstants::EMPTY_STRING);

  if (absolute(filename))
  {
    if (exists(filename))
      return (filename);
    else
      return (StringConstants::EMPTY_STRING);
  }

  if (path_chain.length() > 0)
  {
    for (int i = path_chain.firstIndex(); i <= path_chain.lastIndex(); ++i)
    {
      fullname = path_chain[i];
      if (!fullname.endsWith( "/" ) && !fullname.endsWith( "\\" ))
        fullname += dirsep;
      fullname += filename;
      if (exists( fullname ))
        return (fullname);
    }
  }

  return (StringConstants::EMPTY_STRING);
}


/******************************************************************************
 * Returns full pathnames of files in the path_chain that match the target.
 * The full pathnames are added to the returned StringArray *found if they
 * are not already in it.
 * StringArray *found is set to buf if buf is not 0, otherwise a new
 * StringArray* is allocated to found.
 * If buf != 0, the matched files are appended to the end of
 * buf, and buf is the StringArray returned to the caller.
 * Once a file is found that matches the target, further matches for that file
 * later in the path_chain are ignored.  The target is a series of one more
 * more subpatterns separated by DIR_SEPARATOR characters.  The last
 * subpattern may match either files or directories; the rest can only
 * match directories.  Any subpattern may contain wildcards.
 * For example, the target ab*c/d*ef would be split into the
 * subpattern ab*c for a search on directories, followed by the
 * subpattern d*ef for files and directories. A result might be
 * /sb/abwxc/def /proj/bb/abwxc/dmef /sb/aby23c/drref
 * where the path_chain was /sb:/proj/bb.  If /proj/bb/abwxc/def exists
 * it would not be returned, since a match for abwxc/def was found
 * in /sb first.
 * If the target is absolute, the path chain is not used, although
 * the file or files that match are checked for existance, and returned
 * in the StringArray.
 * If an item in the path chain is not a directory, that item
 * is ignored.
 * If matching != 0, when findFilesInChain returns, the matching files
 * in relative form have been appended to the StringArray *matching.
 * The initial contents of matching are ignored and unchanged.
 * In the example above, the following strings would be added to matching:
 * abwxc/def abwxc/dmef aby23c/drref
 */
StringArray *Path::findFilesInChain( const String &target,
      const StringArray &path_chain, StringArray *buf, boolean find_dirs,
      StringArray *matching, boolean dirs_only )
{
  String fullname;
  StringArray *found = (buf != 0 ? buf : new StringArray());
  // declare static Hashtable fullPaths and initialize to contents of buf.
  // There could be an option to pass Hashtable fullPaths in preloaded?
  // Seems a little unsafe, unless we wrap things in an object, to
  // keep the buf and fullPaths contents synchronized.  For now, if the
  // caller has several targets that should be related, and is keeping
  // the results in buf, we reload fullPaths at each call.
  static Hashtable< SmartCaseString, int > fullPaths;
  fullPaths.clear();
  if (buf != 0)
  {
    for (int bi = buf->firstIndex(); bi <= buf->lastIndex(); ++bi)
      fullPaths.put( (*buf)[bi], 1 );
  }

  // Keep matches of the target in matchesHash (not the absolute paths).
  static Hashtable< SmartCaseString, int > matchesHash;
  matchesHash.clear();

  static StringArray pchain;
  static StringArray targets;
  static StringArray newSubPaths;
  pchain.clear();
  if (Path::absolute( target ))
  {
    int firstSlash = target.indexOfAny( "/\\" );
    // use initial '/' or 'd:/' as the chain to search
    pchain.append( target.substring( STRING_FIRST_INDEX, firstSlash + 1 ));
    // the rest is the target
    target.substring( firstSlash + 1 ).split( "/\\", 0, &targets );
  }
  else
  {
    // Guess what! path_chain components might not be absolute!
    // That has to be taken care of somehow, since the caller wants
    // only absolute output.
    String temp;
    for (int pi = path_chain.firstIndex(); pi <= path_chain.lastIndex(); ++pi)
    {
      if ((temp = canonicalize( path_chain[pi] )) !=
               StringConstants::EMPTY_STRING)
      {
        pchain.append( temp );
      }
    }
    // fill targets with the target, split at the DIR_SEPARATOR characters
    target.split( "/\\", 0, &targets );
  }

  StringArray targSubPaths, matchDirsOrFiles, outputSubPaths;
  if (pchain.length() > 0)
  {
    for (int ipc = pchain.firstIndex(); ipc <= pchain.lastIndex(); ++ipc)
    { // iterate through chain
      // iterate through the subtargets in targets, going deeper in the
      // directory tree with each iteration.  At the last subtarget,
      // we match for both files and directories, otherwise just directories.
      for (int tgti = targets.firstIndex();
          tgti <= targets.lastIndex();
          tgti++)
      { // iterate through subtargets
        String pathToNext;
        if (tgti > targets.firstIndex() && targSubPaths.length() == 0)
          break; // there won't be any matches in this pchain[ipc]
        // iterate through the array of subpaths, calling getDirContents for
        // each subpath using targets[tgti] as the pattern, accumulating
        // a new set of subpaths that is one directory (or file) longer
        int contents_type = (tgti == targets.lastIndex() && !dirs_only ?
                                  (find_dirs ?
                                   CONTENTS_BOTH : CONTENTS_FILES) :
                                   CONTENTS_DIRS);
        newSubPaths.clear();
        if (tgti == targets.firstIndex())
        {
          pathToNext = pchain[ipc];
          getDirContents( pathToNext,
                          false, // save dirs or files, not full paths
                          contents_type,
                          &targSubPaths,
                          0,
                          targets[tgti]);
        }
        else
        {
          for (int tpi = targSubPaths.firstIndex();
               tpi <= targSubPaths.lastIndex(); ++tpi)
          {
            pathToNext = pchain[ipc] + DIR_SEPARATOR + targSubPaths[tpi];
            matchDirsOrFiles.clear();
            // pathToNext is the absolute path for the directory in which
            // we will search for matches to the targets[tgti] subpattern,
            // which will be returned in matchDirsOrFiles.
            getDirContents( pathToNext,
                            false, // save dirs or files, not full paths
                            contents_type,
                            &matchDirsOrFiles,
                            0,
                            targets[tgti]);
            // concatenate the subpath with each directory or file that
            // was found in that subpath, the results going in newSubPaths.
            for (int mi = matchDirsOrFiles.firstIndex();
                 mi <= matchDirsOrFiles.lastIndex(); ++mi)
            {
              newSubPaths.append( targSubPaths[tpi] + DIR_SEPARATOR +
                                  matchDirsOrFiles[mi] );
            }
          }
          targSubPaths = newSubPaths;
        }
      } // end iterate through subtargets

      // For each match in targSubPaths put the match in the matchesHash if
      // it is not there already.  For each full path, put it in found, if it
      // is not there already, using the fullPaths Hashtable
      for ( int tsfi = targSubPaths.firstIndex();
            tsfi <= targSubPaths.lastIndex(); ++tsfi)
      {
        if (! matchesHash.containsKey( targSubPaths[tsfi] ))
        {
          if (matching != 0)
            matching->append( targSubPaths[tsfi] );
          if (ipc < pchain.lastIndex())
          {
            // speedup, won't need it in hash table since we won't look
            // at more pchain items
            matchesHash.put( targSubPaths[tsfi], 1);
          }
          String absMatch;
          int lastix = pchain[ipc].lastIndex();
          if ( pchain[ipc][lastix] == '/' || pchain[ipc][lastix] == '\\')
            absMatch = pchain[ipc] + targSubPaths[tsfi];
          else
            absMatch = pchain[ipc] + DIR_SEPARATOR + targSubPaths[tsfi];
          if (! fullPaths.containsKey( absMatch ))
          {
            fullPaths.put( absMatch, 1 );
            found->append( absMatch );
          }
        }
      }

    } // end iterate through chain
  }

  return (found);
}


/******************************************************************************
 * Determine if one path is a prefix of the other.
 * Note that the prefix is required to end on
 * a directory boundary of the full path in order
 * to match.
 *
 */
boolean Path::isPrefix( const String &fullpath, const String &prefix,
    boolean check_exists )
{
  StringArray tmp(1, 1);
  tmp[tmp.firstIndex()] = prefix;
  return (findPrefix( fullpath, tmp, check_exists ) != ELEMENT_NOTFOUND);
}


/******************************************************************************
 * Find the first directory that is a proper
 * prefix of the full path.
 * Note that the prefix is required to end on
 * a directory boundary of the full path in order
 * to match.
 * @return An index into the array which is a prefix,
 * or ELEMENT_NOTFOUND if none are a prefix.
 */
int Path::findPrefix( const String &fullpath, const StringArray &prefixes,
    boolean check_exists )
{
  int match = ELEMENT_NOTFOUND;
  String fullpathstr = canonicalize( fullpath, false );

  if (prefixes.length() > 0  && fullpathstr.length() > 0)
  {
    if (!check_exists && !fullpathstr.endsWith( DIR_SEPARATOR ))
      fullpathstr += DIR_SEPARATOR;
    SmartCaseString cmp( fullpathstr );
    for (int i = prefixes.firstIndex();
         match == ELEMENT_NOTFOUND && i <= prefixes.lastIndex(); ++i)
    {
      fullpathstr = canonicalize( prefixes[i], false );
      if (fullpathstr.length() > 0)
      {
        if (check_exists)
        {
          if (Path::startsWith( cmp, fullpathstr ))
            match = i;
        }
        else
        {
          fullpathstr += DIR_SEPARATOR;
          if (cmp.startsWith( fullpathstr ))
            match = i;
        }
      }
    }
  }
  return (match);
}


/**
 * Checks if path starts with prefix.  Prefix must end on
 * a dirsep boundary in path to be true (so "/x/y" is NOT a
 * prefix of "/x/yyy/z").
**/
boolean Path::startsWith( const String &path, const String &prefix )
{
  SmartCaseString real_path( path ), real_prefix( prefix );
  Path::fullyCanonicalize( real_path );
  Path::fullyCanonicalize( real_prefix );
  if (real_path.length() < 1 || real_prefix.length() < 1)
    return (false);

  // ensure prefix is on a directory boundary
  real_path += DIR_SEPARATOR;
  real_prefix += DIR_SEPARATOR;
  return (real_path.startsWith( real_prefix ));
}


/**
 * On non-Unix platforms (which don't have symlinks), just
 * canonicalize normally.
 *
 * On Unix, canonicalize path by changing to that directory
 * (if path is a file, it cd's to the directory in which the
 * file supposedly resides), then performing an ODEgetcwd().
 * Changes back to the starting directory before returning.
 * This resolves symbolic links in path in addition to typical
 * canonicalization.
 *
 * Modifies and returns path.  If the directory doesn't exist,
 * path will contain the empty string.
 *
**/
String &Path::fullyCanonicalize( String &path )
{
#ifdef NO_SYMLINKS
  Path::canonicalizeThis( path );
#else
  String orig_dir( Path::getcwd() );

  if (!Path::setcwd( path ))
  {
    if (!Path::setcwd( Path::filePath( path ) ))
      path = StringConstants::EMPTY_STRING;
    else
    {
      path = Path::fileName( path );
      path.prepend( DIR_SEPARATOR );
      path.prepend( Path::getcwd() );
    }
  }
  else
    path = Path::getcwd();
  Path::setcwd( orig_dir );
#endif

  return (path);
}


/******************************************************************************
 * Touch a file.  This updates the file's last modified time
 * to be the current time.
 */
boolean Path::touch( const String &filename )
{
  if (!isLink( filename ))
    return (ODEtouch( canonicalize( filename, false ).toCharPtr() ) == 0);
  else
    return (false);
}


/******************************************************************************
 * Create a symbolic link.
 */
boolean Path::symLink( const String &pathname, const String &linkname )
{
  return (ODEsymlink( canonicalize( pathname, false ).toCharPtr(),
      canonicalize( linkname, false ).toCharPtr() ) == 0);
}

/******************************************************************************
 * Check if a file is actually a symbolic link.
 */
boolean Path::isLink( const String &path )
{

  boolean rc = false; // assume the worst
  struct ODEstat filestat;

  if (ODEstat( canonicalize( path, false ).toCharPtr(), &filestat,
      OFLINK_ODEMODE, 0 ) == 0)
    rc = filestat.is_link != 0;

  return (rc);
}


/******************************************************************************
 * Check if a file is actually a file
 */
boolean Path::isFile( const String &path )
{
  boolean rc = false; // assume the worst
  struct ODEstat filestat;

  if (ODEstat( canonicalize( path, false ).toCharPtr(), &filestat,
      OFFILE_ODEMODE, 0 ) == 0)
    rc = filestat.is_file != 0;

  return (rc);
}


/******************************************************************************
 * Check if a file is actually a directory
 */
boolean Path::isDirectory( const String &path )
{
  boolean rc = false; // assume the worst
  struct ODEstat filestat;

  if (ODEstat( canonicalize( path, false ).toCharPtr(), &filestat,
      OFFILE_ODEMODE, 0 ) == 0)
    rc = filestat.is_dir != 0;

  return (rc);
}


/******************************************************************************
 * Obtain a filename for temporary usage.
 */
String Path::tempFilename( const String &tmpdir )
{
  String filename;
  const String &temp_dir =
      (tmpdir == StringConstants::EMPTY_STRING || !isDirectory( tmpdir )) ?
      SandboxConstants::getODETEMP_DIR() : tmpdir;

  do
  {
    filename = temp_dir;
    filename += DIR_SEPARATOR;
    filename += ODEgetpid();
    filename += "_";
    filename += tmpfile_rand;
    filename += ".ODE";
    if (++tmpfile_rand > TMPFILE_RAND_MAX)
      tmpfile_rand = 0;
  } while (exists( filename ));

  return (filename);
}


/******************************************************************************
 *
 */
String Path::nullFilename()
{
#if defined(DEFAULT_SHELL_IS_CMD)
  return ("nul:");
#elif defined(DEFAULT_SHELL_IS_VMS)
  return ("NL:");
#elif defined(DEFAULT_SHELL_IS_QSH)
  return ("/dev/qsh-stdin-null");
#else
  return ("/dev/null");
#endif
}


/******************************************************************************
 * Get the last modified time of a file.
 */
long Path::lastModified( const String &path )
{
  String newpath = canonicalize( path, false );

  struct ODEstat filestat;
  if (ODEstat( newpath.toCharPtr(), &filestat, OFFILE_ODEMODE, 0 ) < 0)
    return (0);
  return (filestat.mtime);
}


/******************************************************************************
 * Get the last modified time of a file. If the file
 * is a link file, it just returns the created time of this
 * link. ( File.lastModified(path) returns the last modified
 * time of the source file to this link). For nonUnix
 * platform, the result is the same with File.lastModified(path).
 */
long Path::linkLastModified( const String &path )
{
#ifdef NO_SYMLINKS
  return (0);
#else
  String newpath = canonicalize( path, false );

  struct ODEstat linkstat;
  if (ODEstat( newpath.toCharPtr(), &linkstat, OFLINK_ODEMODE, 0 ) != 0)
    return (0);
  return (linkstat.mtime);
#endif
}


/******************************************************************************
 * Get the size of a file.
 */
unsigned long Path::size( const String &path )
{
  String newpath = canonicalize( path, false );

  struct ODEstat filestat;
  if (ODEstat( newpath.toCharPtr(), &filestat, OFFILE_ODEMODE, 0 ) < 0)
    return (0);
  return (filestat.size);
}


/******************************************************************************
 *
 */
StringArray *Path::getFileContents( const String &pathname,
    StringArray *buffer )
// throw (IOException)
{
  StringArray *conts = (buffer == 0) ? new StringArray() : buffer ;

  ifstream *rdr = Path::openFileReader( pathname );
  if (rdr != 0)
  {
    String buffer;
    while (Path::readLine( *rdr, &buffer ))
    {
      conts->add( buffer );
      buffer = StringConstants::EMPTY_STRING;
    }
  }

  Path::closeFileReader( rdr );
  return conts;
}


/******************************************************************************
 * Determine if a path is contained in an array of paths.
 * Uses isSamePath(), and so is bound by its restrictions.
 *
 * @param path The path to search for.
 * @param paths The array of paths in which to search.
 * @return The index into the array of where path is.
 * Returns -1 if path is not found in paths.
 */
int Path::pathInList( const File& path, const Array<File>& paths )
{
  int index = ELEMENT_NOTFOUND;

  for (int i = paths.firstIndex();
       index < paths.firstIndex() && i <= paths.lastIndex(); ++i)
    if (isSamePath( path, paths[i] ))
      index = i;

  return (index);
}

int Path::pathInList( const String &path, const StringArray &paths )
{
  int index = ELEMENT_NOTFOUND;

  for (int i = paths.firstIndex();
       index < paths.firstIndex() && i <= paths.lastIndex(); ++i)
    if (isSamePath( path, paths[i] ))
      index = i;

  return (index);
}

/******************************************************************************
 * Rename a file or directory.  Does NOT overwrite
 * non-empty directories.
 *
 * @param oldpath The original file/dir.
 * @param newpath The new name for the file.
 * @param overwrite If true, newpath will be overwritten
 * if it already exists.  If false, rename will fail if
 * newpath already exists.
 * @return True if file was renamed, false if not.
 */
boolean Path::rename( const File &oldpath, const File &newpath,
    boolean overwrite )
{
  struct ODEstat file_stat;

  if (ODEstat( oldpath.toCharPtr(), &file_stat, OFFILE_ODEMODE, 0 ) != 0)
    return (false); // can't find original file
  if (!file_stat.is_readable)
    return (false); // can't read original file

  if (newpath.doesExist())
  {
    if (!overwrite)
      return (false); // target exists, but we shouldn't overwrite it
#ifdef DEFAULT_SHELL_IS_VMS
    ODEremove( Path::unix2vms( newpath ).toCharPtr() );
#else
    ODEremove( newpath.toCharPtr() );
#endif
  }

  return (::rename( oldpath.toCharPtr(), newpath.toCharPtr() ) == 0);
}



/******************************************************************************
 * Delete a file or directory.  Can optionally delete
 * an entire directory tree.
 *
 * @param path The file or directory to delete.
 * @param all If true, allow removal of a directory's
 * contents (files AND subdirectories).  If false, only
 * delete files and empty directories.
 * @param keepgoing If true, don't stop when one of a
 * directory's contents cannot be removed (i.e., remove
 * all files/subdirectories possible).  If false, stop
 * processing a directory when one of its files or
 * subdirectories cannot be deleted.
 * @return True if delete was successful, false if not.
 */
boolean Path::deletePath( const File &path,
  boolean all, boolean keepgoing )
{
  if (Interface::isDebug())
    Interface::printDebug( "About to delete path: " + path.toString() );

  if (path.length() < 1)
    return (false);
  else
    return (deleteFullPath( path.toString(), all, keepgoing ));
}




/******************************************************************************
 * Create a BufferedReader handle for a file.
 *
 */
ifstream *Path::openFileReader( const String &path, boolean binary )
  // throw ( IOException )
{
  FILE_MODES_TYPE modes = ios::in;

#ifdef NOCREATE_READ_OPENMODE
  modes |= ios::nocreate;
#endif

  if (binary)
  {
#ifndef NO_BINARY_OPENMODE
    modes |= ios::binary;
#endif
  }
  else // text
  {
#ifdef FORCE_TEXT_OPENMODE
    modes |= ios::text;
#endif
  }

  ifstream *ip = new ifstream( canonicalize( path, false ).toCharPtr(), modes );

  if (!*ip)
  {
    delete ip;
    throw (IOException( "Unable to open " + path + " for reading." ));
  }

  return (ip);
}


/******************************************************************************
 * Create a PrintWriter object for a file.
 */
fstream *Path::openFileWriter( const String &path,
      boolean append, boolean flush, boolean binary )
  // throw( IOException )
{
  fstream *op;
  FILE_MODES_TYPE modes = ios::out;

  if (append)
    modes |= ios::app;

  if (binary)
  {
#ifndef NO_BINARY_OPENMODE
    modes |= ios::binary;
#endif
  }
  else // text
  {
#ifdef FORCE_TEXT_OPENMODE
    modes |= ios::text;
#endif
  }

  op = new fstream( canonicalize( path, false ).toCharPtr(), modes );

  if (!*op)
  {
    delete op;
    throw (IOException( "Unable to open " + path + " for writing." ));
  }

  return op;
}

/******************************************************************************
 *
 */
fstream *Path::openFileReadWriter( const String &path, boolean binary )
  // throw( IOException )
{
  fstream *op;
  FILE_MODES_TYPE modes = ios::in | ios::out;

  if (binary)
  {
#ifndef NO_BINARY_OPENMODE
    modes |= ios::binary;
#endif
  }
  else // text
  {
#ifdef FORCE_TEXT_OPENMODE
    modes |= ios::text;
#endif
  }

  op = new fstream( canonicalize( path, false ).toCharPtr(), modes );

  if (!*op)
  {
    delete op;
    throw (IOException( "Unable to open file for read-write." ));
  }

  return op;
}


/******************************************************************************
 * Compares the last modified times of two files.  If
 * path1 is newer (has been modified more recently), the
 * return value is positive.  If path2 is newer, the return
 * value is negative.  If the files have the same last
 * modified time, zero is returned.
*/
long Path::timeCompare( File &path1, File &path2)
{
    if(!path1.doesExist())
      throw (FileNotFoundException( String( "File not found: " ) + path1 ));
    if(!path2.doesExist())
      throw (FileNotFoundException( String( "File not found: " ) + path2 ));

    return (path1.getModTime() - path2.getModTime());
}

/******************************************************************************
 * Copy file from src to dest. There are four formats:
 * .    1. file -> file
 * .    2. file -> directory
 * .    3. directory -> directory
 * .    4. pattern_file -> directory ( where pattern_file is like
 *                                  "*.java", "ode*.c" )
 */
boolean Path::copy( File &src, File &dest,
    boolean overwrite )
// throw (IOException)
{
  File srcFilePath(filePath(src.toString()), true);
  String srcFileName=fileName(src.toString());

  if (srcFilePath.isDir())
  {
    Array<File> *contents = getDirContents( srcFilePath, srcFileName );

    if (contents && (contents->length() > 0))
    {
      if (contents->length() > 1) // must be a pattern match
      {
        if (!dest.isDir())
          throw (IOException( dest + " is not a directory !" ));
        for (int i = contents->firstIndex(); i <= contents->lastIndex(); i++)
        {
          // we do not want to recursively copy the subdirs.
          // MAY modify it later if needed.
          File temp((*contents)[i]);
          if(!temp.isDir() && !copyFile(temp,dest,overwrite))
            throw (IOException( temp + " could not be copied " ));
        }
        return true;
      }
      else // single match - must be case 1, 2 or 3.
      {
        if (src.isFile())
          return( copyFile( src, dest, overwrite ));
        if (src.isDir()) // src is a dir
        {
          File file(src + DIR_SEPARATOR + "*",true);
          return( copy( file, dest, overwrite ) );
        } else {
        //pattern Match has returned only one file
          File temp((*contents)[contents->firstIndex()]);
          return(copyFile(temp,dest,overwrite));
       }
      }
    }
    else
      throw (IOException( "No source file(s) found" ));
  }
  return false;
}


Array<File> *Path::getDirContents( const File &path,
    const String &pattern, Array<File> *buf,
    File *filep  )
{
  Array<File> *contents = (buf == 0) ? new Array<File>() : buf;
  ODEDIR *dirp;
  ODEDIRENT entry;

  if (filep == 0)
    dirp = ODEopendir( path.toCharPtr());
  else
    dirp = ODEopendir( filep->toCharPtr() );

  if (Interface::isDebug())
    Interface::printDebug( "opened dir - about to read dir: " + path );
  while (ODEreaddir( dirp, &entry ) == 0)
  {
    String tmp(entry.d_name);
    if (Interface::isDebug())
      Interface::printDebug( "read content: " + tmp );

    if (tmp.equals( StringConstants::PERIOD ) || tmp.equals( ".." ))
      continue;

    // get files for the specified pattern - Don't bother checking
    // if the pattern is "*".
    if (!pattern.equals( StringConstants::STAR_SIGN ) &&
        !Pattern::isMatching( pattern, tmp,
        PlatformConstants::onCaseSensitiveOS() ))
      continue;

    // got a match
    File file(path + DIR_SEPARATOR+tmp,true);
    contents->append( file );
  }

  ODEclosedir( dirp );
  if (Interface::isDebug())
    Interface::printDebug( "closed dir" );

  return (contents);
}

/******************************************************************************
 * Copy file from src to dest.
 */
boolean Path::copyFile( File &src, File &dest,
    boolean overwrite, File *srcfile, boolean binary )
// throw (IOException)
{
  File     destFilePath( filePath(dest.toString()), true );
  File     *ptrDest = &dest, *srcfilep = &src;
  File     tmp;
  ifstream *ip;
  fstream  *op;

  // check if the src file exists, is a file and is readable.
  if (!srcfilep->doesExist())
  {
    throw (IOException( *srcfilep + " doesn't exist !" ));
  }

  if (!srcfilep->isFile())
  {
    throw (IOException( *srcfilep + " is not a file !" ));
  }

  // check if the parent dir of the dest exists.
  // if it does, and dest is a file - then copy based on overwrite flag.
  // if it does, and dest is a dir - then append src file name to dest
  //    and recall this func.
  // if it does, and dest is not a file or dir then have to create a new file.
  if (!destFilePath.doesExist())
  {
    throw (IOException( dest + " parent dir doesn't exist !" ));
  }
  else if (dest.isDir())
  {
    tmp = File(dest + DIR_SEPARATOR + fileName(srcfilep->toString()), true);
    ptrDest = &tmp;
  }

  if (*srcfilep == *ptrDest)
  {
    throw (IOException( *srcfilep + " and " + *ptrDest + " are identical !" ));
  }

  if (!ptrDest->doesExist() || overwrite)
  {
    ip = openFileReader( src, binary ); // open src file to read
    op = openFileWriter( *ptrDest, false, true, binary ); // open dest file

    if (ip && op)
    {
      int ch;
      while ((ch = ip->get()) != EOF) // read from
        op->put( (char)ch ); // write into
    }

    closeFileReader( ip );
    closeFileWriter( op );
  }

  return (true);
}


/**
 * Check the string equality first, just in case one of the
 * paths doesn't exist (doesn't affect whether the paths are
 * the same or not, so it's important to try it).
 *
 * Then, fullyCanonicalize each path to resolve symbolic links
 * on Unix (and ensure the drive letter is prepended if necessary
 * on NT/OS2).
 *
 * Remember to wrap the canonicalized versions in SmartCaseString
 * objects, since paths are implicitly assumed to be case-aware.
 *
**/
boolean Path::isSamePath( const String &path1, const String &path2 )
{
  if (path1 == path2) // no extra work needed
    return (true);
  SmartCaseString smart_path1( path1 ), smart_path2( path2 );
  Path::fullyCanonicalize( smart_path1 );
  Path::fullyCanonicalize( smart_path2 );
  if (smart_path1.length() < 1 || smart_path2.length() < 1 ||
      smart_path1 != smart_path2)
    return (false);

  return (true);
}
