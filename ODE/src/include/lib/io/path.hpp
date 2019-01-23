/**
 * Path
 *
 * Provides both a convenient static wrapper for many
 * file/dir related functions, as well as some ODE-specific
 * functions.  In many cases null is returned on error
 * instead of throwing an exception.  It also takes care
 * of portability issues where appropriate, such as
 * normalizing or canonicalizing paths.
 */
#ifndef _ODE_LIB_IO_PATH_HPP_
#define _ODE_LIB_IO_PATH_HPP_

#include <fstream>

#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/strcon.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/portable/native/file.h"
#include "lib/portable/native/dir.h"
#include "lib/io/file.hpp"


class File;

class Path
{
  public:

    static const String ODEDLLPORT DIR_SEPARATOR;
    static const char ODEDLLPORT DIR_SEPARATOR_CHAR;
    static const String ODEDLLPORT PATH_SEPARATOR;
    static const char ODEDLLPORT PATH_SEPARATOR_CHAR;
    static const int ODEDLLPORT CONTENTS_BOTH;
    static const int ODEDLLPORT CONTENTS_FILES;
    static const int ODEDLLPORT CONTENTS_DIRS;

    static boolean isSamePath( const String &path1, const String &path2 );
    inline static int pathInList( const String &path,
        const String &path_chain );
    static int pathInList( const String &path, const StringArray &paths );
    static int pathInList( const File &path, const Array<File> &paths );
    inline static boolean exists( const String &path );
    inline static boolean absolute( const String &path );
    static String getAbsolutePrefix( const String &path );
    inline static boolean rename( const String &oldpath, const String &newpath,
        boolean overwrite = true );
    static boolean rename(const File &oldpath, const File &newpath,
           boolean overwrite = true );
    inline static const String &getcwd();
    static String getCwdFromEnviron(); // leaves symlinks unresolved
    static const String gethome();
    static boolean createPath( const String &path );
    static boolean deletePath( const String &path, boolean all = false,
        boolean keepgoing = false );
    static boolean deletePath( const File &path, boolean all = false,
        boolean keepgoing = false );
    static Array<File> *getDirContentsAsFiles( const String &path,
        int content_type, Array<File> *buf = 0, File *filep = 0,
        const String& pattern="*");
    static StringArray *getDirContents( const String &path,
        boolean fullpaths, int content_type, StringArray *buf = 0,
        File *filep = 0, const String &pattern = StringConstants::STAR_SIGN );
    static StringArray *getDirContents( const String &path,
        const String &pattern = "*", StringArray *buf = 0,
        File *filep = 0);
    static Array<File> *getDirContents( const File &path,
        const String &pattern = "*", Array<File> *buf = 0,
        File *filep = 0 );
    inline static String canonicalize( const String &path,
        boolean checkexists = true, boolean getabsolutepath = true,
        const String &curdir = "" );
    static String &canonicalizeThis( String &result,
        boolean checkexists = true, boolean getabsolutepath = true,
        const String &curdir = "" );
    static boolean startsWith( const String &path, const String &prefix );
    static String &fullyCanonicalize( String &path );
    inline static String normalize( const String &path );
    inline static String &normalizeThis( String &result );
    inline static String unixize( const String &path );
    inline static String &unixizeThis( String &result );
    inline static String userize( const String &path );
    inline static String &userizeThis( String &result );

#if defined(DEFAULT_SHELL_IS_VMS)
    static String &unixizeThisVMS( String &result );
    inline static String unix2vms( const String &path );
    static String &unix2vmsThis( String &result );
    static boolean absoluteVMS( const String &path );
#endif

    inline static StringArray *separatePaths( const String &pathlist,
        StringArray *buf = 0 );
    static ifstream *openFileReader( const String &path,
        boolean binary = false );
        //  throws IOException
    static fstream *openFileWriter( const String &path,
        boolean append = true, boolean flush = true,
        boolean binary = false );
        //  throws IOException
    static fstream *openFileReadWriter( const String &path,
        boolean binary = false );
        //  throws IOException
    static void closeFileReader( ifstream *file ); // deallocates too
    static void closeFileWriter( fstream *file ); // deallocates too
    static void closeFileReadWriter( fstream *file ); // deallocates too
    static long timeCompare( const String &path1, const String &path2 );
        //  throw( FileNotFoundException )
    static long timeCompare( File &path1, File &path2 );
    inline static String fileName( const String &pathname );
    static String &fileNameThis( String &pathname );
    inline static String filePath( const String &pathname );
    static String &filePathThis( String &pathname );
    inline static String fileSuffix( const String &pathname, boolean smart );
    static String &fileSuffixThis( String &pathname, boolean smart );
    inline static String fileRoot( const String &pathname, boolean smart );
    static String &fileRootThis( String &pathname, boolean smart );
    static void   fileRootSuffixThis( const String &pathname,
                                      StringArray &result );
    static int    indexOfSplit( int &suffix_beg, String &pathname );
    static String removeExtraSlashes( const String &pathname );
    inline static String findFileInChain( const String &filename,
      const StringArray &path_chain );
    static String findFileInChain( const String &filename,
      const StringArray &path_chain, const String &dirsep );
    inline static String findFileInChain( const String &filename,
      const String &path_chain );
    inline static String findFileInChain( const String &filename,
      const String &path_chain, const String &dirsep );
    static StringArray * findFilesInChain( const String &filename,
      const StringArray &path_chain, StringArray *buf,
      boolean find_dirs = false, StringArray *matching = 0,
      boolean dirs_only = false );
    static boolean isPrefix( const String &fullpath, const String &prefix,
        boolean check_exists = true );
    static int findPrefix( const String &fullpath,
        const StringArray &prefixes, boolean check_exists = true );
    inline static boolean copy( const String &src, const String &dest,
        boolean overwrite = true );
    static boolean copy( File &src, File &dest,
        boolean overwrite = true );

    // Note: for both versions of copyFile, the 4th parameter (srcfile)
    //       is now being ignored and is obsolete.
    inline static boolean copyFile( const String &src, const String &dest,
        boolean overwrite, File *srcfile = 0, boolean binary = false );
    static boolean copyFile( File &src, File &dest,
        boolean overwrite, File *srcfile = 0, boolean binary = false );
      // throw (IOException)

    inline static boolean chModifiedTime( const String &src,
        const String &dst );
    inline static boolean chMod( const String &src, const String &dst );
    static boolean touch( const String &filename );
    static boolean symLink( const String &from, const String &to );
    static boolean isLink( const String &path );
    static boolean isFile( const String &pathname );
    static boolean isDirectory( const String &pathname );
    static boolean setcwd( const File &path );
    inline static boolean setcwd( const String &path );
    static String tempFilename( const String &tmpdir = "" );
    static String nullFilename();
    static long lastModified( const String &path );
    static long linkLastModified( const String &path );
    static unsigned long size( const String &path );
    static boolean readLine( istream &fdr, String *buffer );
    inline static ostream &putLine( ostream &fdw, const String &line );
    static StringArray *getFileContents( const String &pathname,
                                         StringArray *buffer = 0 );


  private:

    static String ODEDLLPORT current_dir;
    static const String ODEDLLPORT SUFFIX_STRING;
    static const int ODEDLLPORT TMPFILE_RAND_MAX;
    static int ODEDLLPORT tmpfile_rand;
    static const int ODEDLLPORT MAX_PATH_LENGTH;

    static const char *_getcwd();
    static boolean deleteFullPath( const String &fullpath, boolean all,
       boolean keepgoing );
};


inline int Path::pathInList( const String &path, const String &path_chain )
{
  StringArray buf;
  return (pathInList( path, *separatePaths( path_chain, &buf ) ));
}

/**
 * Convert a path list to an array of paths.
 */
inline StringArray *Path::separatePaths( const String &pathlist,
    StringArray *buf )
{
  return (pathlist.split( PATH_SEPARATOR, 0, buf ));
}

/**
 * Returns the full pathname for filename.
 * The file is searched in each directory (starting at
 * first array index)
 * of the path chain (an array, each element of which
 * contains a single directory).
 * If filename is absolute and exists, it will be returned
 * as-is.
 */
inline String Path::findFileInChain( const String &filename,
  const StringArray &path_chain )
{
  return (findFileInChain( filename, path_chain, DIR_SEPARATOR ));
}

inline String Path::findFileInChain( const String &filename,
  const String &path_chain )
{
  return (findFileInChain( filename, *separatePaths( path_chain ) ));
}

inline String Path::findFileInChain( const String &filename,
  const String &path_chain, const String &dirsep )
{
  return (findFileInChain( filename, *separatePaths( path_chain ),
          dirsep ));
}

inline void Path::closeFileReadWriter( fstream *file )
    // throws IOException
{
  closeFileWriter( file );
}

/**
 * putLine
 *
**/
inline ostream &Path::putLine( ostream &fdw, const String &line )
    // throws IOException
{
  fdw << line << endl;
  return (fdw);
}

/**
 * Test if a file/directory exists.
 *
 * @param path The relative or absolute pathname to test.
 * @return True if path exists, false if not.
 */
inline boolean Path::exists( const String &path )
{
  return (ODEstat( canonicalize( path, false ).toCharPtr(),
      0, OFFILE_ODEMODE, 0 ) == 0);
}

inline boolean Path::chModifiedTime( const String &src, const String &dst )
{
  return (ODEclonetime( canonicalize( src, false ).toCharPtr(),
      canonicalize( dst, false ).toCharPtr() ) == 0);
}

inline boolean Path::chMod( const String &src, const String &dst )
{
  return (ODEclonemode( canonicalize( src, false ).toCharPtr(),
      canonicalize( dst, false ).toCharPtr() ) == 0);
}


/**
 * Return the current working directory.
 */
inline const String &Path::getcwd()
{
  return (current_dir);
}

/**
 * Change the current working directory to the
 * specified one.
 */
inline boolean Path::setcwd( const String &path )
{
  File tmpfile( path, true );

  return (Path::setcwd( tmpfile ));
}

inline boolean Path::rename( const String &oldpath, const String &newpath,
    boolean overwrite )
{
  File oldfile( oldpath, true ), newfile( newpath, true );

  return rename( oldfile, newfile );
}


inline boolean Path::deletePath( const String &path,
  boolean all, boolean keepgoing )
{
  File tmpfile( path,true );

  return deletePath( tmpfile, all, keepgoing );
}

inline long Path::timeCompare( const String &path1, const String &path2 )
{
  File tmpfile1( path1, true ), tmpfile2( path2, true );

  return timeCompare( tmpfile1, tmpfile2 );
}

/******************************************************************************
 * Copy file from src to dest. There are four formats:
 * .    1. file -> file
 * .    2. file -> directory
 * .    3. directory -> directory
 * .    4. pattern_file -> directory ( where pattern_file is like
 *                                  "*.java", "ode*.c" )
 */
inline boolean Path::copy( const String &src, const String &dest,
    boolean overwrite )
{
  File tmpsrcfile( src, true ), tmpdestfile( dest, true );

  return (copy( tmpsrcfile, tmpdestfile, overwrite ));
}

/******************************************************************************
 * Copy file from src to dest.
 */
inline boolean Path::copyFile( const String &src, const String &dest,
    boolean overwrite, File *srcfile, boolean binary )
{
  File tmpsrcfile( src, true ), tmpdestfile( dest, true );

  return (copyFile( tmpsrcfile, tmpdestfile, overwrite, srcfile, binary ));
}

/**
 * Canonicalize a path.
 * ASSUMPTION: If you try to go "below" the root dir you still
 * end up at the root dir. So for example "d:\..\test" would
 * reduce to "d:\test".
 * In the algorithm used here, we simply count the number of ".." and ignore
 * a corresponding number of the path components.. All the remaining components
 * are valid components and are copied to the end of the string..
**/
inline String Path::canonicalize( const String &path, boolean checkexists,
    boolean getabsolutepath, const String &curdir )
{
  String result( path );
  return (canonicalizeThis( result, checkexists, getabsolutepath, curdir ));
}

/**
 * Convert either directory slash to the platform-specific
 * directory slash.  Length of the returned string is
 * guaranteed to be the same length as the original.
**/
inline String Path::normalize( const String &path )
{
  String result( path );
  return (normalizeThis( result ));
}

inline String &Path::normalizeThis( String &result )
{
#if defined(DEFAULT_SHELL_IS_CMD)
  return (result.replaceThis( '/', '\\' ));
#elif defined(DEFAULT_SHELL_IS_VMS)
  return (Path::unixizeThisVMS( result ));
#else
  return (result.replaceThis( '\\', '/' ));
#endif
}

/**
 * Convert any backslashes to forward slashes.
 * Length of the returned string is guaranteed
 * to be the same length as the original.
**/
inline String Path::unixize( const String &path )
{
  String result( path );
  return (unixizeThis( result ));
}

inline String &Path::unixizeThis( String &result )
{
#ifdef DEFAULT_SHELL_IS_VMS
  return (Path::unixizeThisVMS( result ));
#else
  return (result.replaceThis( '\\', '/' ));
#endif
}

/**
 * Convert any directory slashes to the user-specified
 * DIRSEP character.  If user never set the DIRSEP
 * environment variable, the platform-specific slash
 * is used instead (see SandboxConstants).
 * Length of the returned string is NOT guaranteed
 * to be the same length as the original (since the
 * user may specify a multi-character separator string).
**/
inline String Path::userize( const String &path )
{
  String result( path );
  return (userizeThis( result ));
}

inline String &Path::userizeThis( String &result )
{
#ifdef DEFAULT_SHELL_IS_VMS
  return (Path::unixizeThisVMS( result ));
#else
  return (result.replaceThis( "/",
      SandboxConstants::getUSER_DIR_SEPARATOR() ).replaceThis( "\\",
      SandboxConstants::getUSER_DIR_SEPARATOR() ));
#endif
}

#ifdef DEFAULT_SHELL_IS_VMS
inline String Path::unix2vms( const String &path )
{
  String result( path );
  return (unix2vmsThis( result ));
}
#endif

/******************************************************************************
 * Test if a file/directory path is in absolute format
 *
 * @param path The path to test
 * @return True if path is an absolute path, false if not.
 */
inline boolean Path::absolute( const String &path )
{
#ifdef DEFAULT_SHELL_IS_VMS
  return (Path::absoluteVMS( path ));
#else
  if (path[STRING_FIRST_INDEX] == '/' || path[STRING_FIRST_INDEX] == '\\')
    return (true);
#ifdef DEFAULT_SHELL_IS_CMD
  if (path.length() >= 3 &&
      isalpha( path[STRING_FIRST_INDEX] ) &&
      path[STRING_FIRST_INDEX + 1] == ':' &&
      (path[STRING_FIRST_INDEX + 2] == '/' ||
      path[STRING_FIRST_INDEX + 2] == '\\'))
    return (true);
#endif
  return (false);
#endif
}

inline String Path::filePath( const String &pathname )
{
  String tmp( pathname );
  return (filePathThis( tmp ));
}

inline String Path::fileName( const String &pathname )
{
  String tmp( pathname );
  return (fileNameThis( tmp ));
}

inline String Path::fileRoot( const String &pathname, boolean smart )
{
  String tmp( pathname );
  return (fileRootThis( tmp, smart ));
}

inline String Path::fileSuffix( const String &pathname, boolean smart )
{
  String tmp( pathname );
  return (fileSuffixThis( tmp, smart ));
}

#endif /* _ODE_LIB_IO_PATH_HPP_ */
