package com.ibm.ode.lib.io;

import java.io.*;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This class contains static file- and directory-oriented
 * functionality which complements Java's File class.
**/
public class Path
{
  /**
   * Used for getDirContents() content_type parameter.
  **/
  public static final int CONTENTS_FILES = 1;

  /**
   * Used for getDirContents() content_type parameter.
  **/
  public static final int CONTENTS_DIRS = 2;

  /**
   * Used for getDirContents() content_type parameter.
  **/
  public static final int CONTENTS_BOTH = 3;

  private static final String SUFFIX_STRING = ".";


  /**
   * Test if a file/directory exists.
   *
   * @param path The relative or absolute pathname to test.
   * @return True if path exists, false if not.
   */
  public static boolean exists( String path )
  {
    File fd = new File( path );

    if (fd == null)
      return (false);
    return (fd.exists());
  }
	
  /**
   * Delete a file.
   *
   * @param path The relative or absolute pathname to delete.
   * @return True if deletion successful, false if not.
   */
  public static boolean delete( String path )
  {
    File fd = new File( path );

    if ((fd == null) || (!fd.delete()))
      return (false);
    return true;
  }

  /**
   * Make a directory path.  Intermediate directories will be
   * created as needed.
   *
   * @param path The path to create.
   * @return True if path was created, false if not.
   */
  public static boolean createPath( String path )
  {
    File fd = new File( path );
    if (fd == null)
      return (false);
    return (fd.mkdirs());
  }

  /**
   * Get the current working directory.
   *
   * @return A string containing the current working directory.
  **/
  public static String getcwd()
  {
    return (System.getProperty( "user.dir" ));
  }

  /**
   * Create a symbolic link.
   *
   * @param from The name of the file to link to.
   * @param to The name of the symbolic link to create.
   * @param force If true, overwrite any existing symlinks.
   * @return True on success, false on failure.
   */
  public static boolean symLink( String from, String to, boolean force )
  {
    if ((from = normalize( from )) != null)
    {
      to = normalize( to );
      // optionally delete symlink  
      if ( force )
        delete( to );

      String cmd = "ln -s " + from + " " + to;
      try
      {
        if (Runtime.getRuntime().exec( cmd ).waitFor() == 0)
          return (true);
      }
      catch (IOException e)
      {
        return (false);
      }
      catch (InterruptedException e)
      {
        return (false);
      }
      return (false);
    }
    return (false);
  }

  /**
   * Get a list of all files and/or directories in a
   * directory.
   *
   * @param path The directory in which to get contents.
   * @param fullpaths If true, absolute paths will be used
   * for every directory entry.  If false, only the filenames
   * will be used.
   * @param content_type Should be one of CONTENTS_FILES,
   * CONTENTS_DIRS, or CONTENTS_BOTH.  This is used to
   * restrict the results to either files or directories.
   * @return The array of directory entries, or null on error
   * (a zero-length array is returned if the directory yielded
   * no entries).
  **/
  public static String[] getDirContents( File path,
      boolean fullpaths, int content_type )
  {
    String basedir;
    String[] filelist;
    int i;

    if (path == null || !path.isDirectory())
      return (null);
    // get list of all files/dirs
    filelist = path.list( getFilenameFilter( content_type ) );
    if (filelist == null || filelist.length < 1)
      return (new String[0]);
    basedir = path.getPath();
    // make sure basedir ends with a directory separator string
    if (!basedir.endsWith( File.separator ))
      basedir += File.separator;
    if (fullpaths)
      for (i = 0; i < filelist.length; ++i)
        filelist[i] = basedir + filelist[i];
    return (filelist);
  }


  /**
   * This is used by getDirContents() to return only the
   * desired type of files.
   *
   * @param content_type Should be one of CONTENTS_FILES,
   * CONTENTS_DIRS, or CONTENTS_BOTH.  This is used to
   * restrict the results to either files or directories.
   * @return A FilenameFilter object suitable for calling
   * File.list().
  **/
  private static FilenameFilter getFilenameFilter( int content_type )
  {
    switch (content_type)
    {
      case CONTENTS_DIRS:
        return (new FilenameFilter() {
            public boolean accept( File dir, String file )
            {
              return (new File( dir.getPath() + File.separator +
                  file ).isDirectory());
            }});
      case CONTENTS_FILES:
        return (new FilenameFilter() {
            public boolean accept( File dir, String file )
            {
              return (new File( dir.getPath() + File.separator +
                  file ).isFile());
            }});
     
      default:
        return (new FilenameFilter() {
            public boolean accept( File dir, String file )
            {
              return (true);
            }});
    }
  }


  /**
   * Convert either directory slash to the platform-specific
   * directory slash.  Length of the returned string is
   * guaranteed to be the same length as the original.
   *
   * @param path The path to normalize.
   * @return The normalized path.
   */
  public static String normalize( String path )
  {
    if (path != null)
      return (path.replace( '\\', '/' ).replace( '/', File.separatorChar ));
    else
      return (null);
  }


  /**
   * Canonicalize a path.  Gets rid of "." and ".." components,
   * removes extra directory separators (e.g., "dir1////dir2"),
   * prepends the drive letter (on OS/2 and NT), etc.  May return
   * null if an intermediate directory in the path doesn't exist.
   *
   * @param path The path to canonicalize.
   * @return The canonicalized path, or null on error.
  **/
  public static String canonicalize( File path )
  {
    String fullpath;

    if (path == null)
      return (null);
    try
    {
      fullpath = path.getCanonicalPath();
      if (fullpath == null || fullpath.length() < 1)
        return (null);
    } catch (IOException e)
    {
      return (null);
    }
    return (fullpath);
  }


  /**
   * Take all trailing '\\' and '/' off the path
   *
   * @param path The path to remove slashes from.
   * @return The path with no trailing slashes.
   */
  public static String stripTrailingSlashes( String path )
  {
    if (path.length() == 0)
      return (path);

    int index = path.length()-1;
    while (path.charAt( index ) == '\\' || path.charAt( index ) == '/')
      index--;
    return (path.substring( 0, index+1 ));
  }

  /**
   * Create a RandomAccessFile handle for a file, in
   * read/write mode.
   *
   * @param path The pathname to open.
   * @return The file handle, or null if an error occurred.
  **/
  public static RandomAccessFile openRWFile( String path )
  {
    try
    {
      return (new RandomAccessFile( path, "rw" ));
    } catch (Exception e)
    {
      return (null);
    }
  }
  

  /**
   * Create a BufferedReader handle for a file.
   *
   * @param path The pathname to open.
   * @return The file handle, or null if an error occurred.
  **/
  public static BufferedReader openFileReader( String path )
  {
    try
    {
      return (new BufferedReader( new FileReader( path ) ));
    } catch (Exception e)
    {
      return (null);
    }
  }


  /**
   * Create a PrintWriter object for a file.
   *
   * @param path The pathname to open.
   * @param append If true, the file will be opened in append mode.
   * If false, the file will be opened in overwrite mode.
   * @param flush If true, println()'s will cause the output
   * buffer to be flushed automatically.  If false, user must
   * manually call flush() [or close()] to flush the output
   * buffer.
   * @return The file handle, or null if an error occurred.
  **/
  public static PrintWriter openFileWriter( String path,
      boolean append, boolean flush )
  {
    try
    {
      return (new PrintWriter( new BufferedWriter( new FileWriter(
          path, append ) ), flush ));
    } catch (Exception e)
    {
      return (null);
    }
  }

  public static String getFileDate( File file )
  {
    if (file.exists())
    {
      long millis = file.lastModified();
      Date date = new Date( millis );
      SimpleDateFormat date_format = new SimpleDateFormat(
          "dd-MMM-yyyy HH:mm:ss" );
      return (date_format.format( date ));
    }
    else
    {
      return ("N/A");
    }
  }

  /**
   * Returns the name portion of a path.  This is, in effect,
   * everything following the last directory separator.
   *
   * @param pathname The path to analyze.
   * @return The name portion of the path.  This may be the
   * path itself if no directory separators exist in it.
   */
  public static String fileName( String pathname )
  {
    if (pathname == null)
      return (null);
    int index = normalize( pathname ).lastIndexOf( File.separator );
    if (index < 0)
      return (pathname);
    return (pathname.substring( index + File.separator.length() ));
  }
	
  /**
   * Returns the path portion of a path.  This is, in effect,
   * everything preceding the last directory separator.
   *
   * @param pathname The path to analyze.
   * @return The path portion of the path.  This may be the
   * empty string if the only directory separator is the first
   * character of the path, or the string "." if there are
   * no directory separators in the path.
   */
  public static String filePath( String pathname )
  {
    if (pathname == null)
      return (null);
    int index = normalize( pathname ).lastIndexOf( File.separator );
    if (index < 0)
      return (".");
    if (index == 0)
      return ("");
    return (pathname.substring( 0, index ));
  }
	

  /**
   * Returns the suffix portion of a path/file.  This value may
   * depend on the value of smart: if smart is true, fileName()
   * is called first to ensure the suffix is searched for only
   * in the last component of the path.  If smart is false,
   * everthing following the last suffix character is returned
   * (even if it is part of a directory name and not a filename).
   * The makefile string modifer "E" uses smart==false.
   *
   * @param pathname The path to analyze.
   * @param smart If true, only look for suffixes in the last
   * component of the path (i.e., the filename).  If false, look
   * for suffixes anywhere in the path.
   * @return The suffix portion of the path.  This may be null
   * if no suffix characters exist in it.
   */
  public static String fileSuffix( String pathname, boolean smart )
  {
    String filename;
    int index;
    
    if (pathname == null)
      return (null);
    if (smart)
      filename = fileName( pathname );
    else
      filename = pathname;
    index = filename.lastIndexOf( SUFFIX_STRING );
    if (index < 0)
      return (null);
    return (filename.substring( index + SUFFIX_STRING.length() ));
  }


  /**
   * Returns the root portion of a path/file.  This value may
   * depend on the value of smart: if smart is true, fileName()
   * is called first to ensure the root is searched for only
   * in the last component of the path.  If smart is false,
   * everthing preceding the last suffix character is returned
   * (which could include the path or part of the path).
   * The makefile string modifer "R" uses smart==false.
   *
   * @param pathname The path to analyze.
   * @param smart If true, only look for the root in the last
   * component of the path (i.e., the filename).  If false, look
   * for the root anywhere in the path.
   * @return The root portion of the path.  This may be the path
   * itself if no suffix characters exist in it.
   */
  public static String fileRoot( String pathname, boolean smart )
  {
    String filename;
    int index;
    
    if (pathname == null)
      return (null);
    if (smart)
      filename = fileName( pathname );
    else
      filename = pathname;
    index = filename.lastIndexOf( SUFFIX_STRING );
    if (index < 0)
      return (filename);
    return (filename.substring( 0, index ));
  }

  public static void main( String [] args )
  {
    System.out.println( "Testing class: Path" );
    try{
  //      System.out.println( "Testing method: Path.symLink(/tmp/from,/tmp/to,true) = " + symLink("/tmp/from","/tmp/to", true));
        System.out.println( "Testing method: Path.stripTrailingSlashes()");
        String s1 = "foo////";
        System.out.println( "in " + s1 + " out " + Path.stripTrailingSlashes( s1 ));
        s1 = "foo/";
        System.out.println( "in " + s1 + " out " + Path.stripTrailingSlashes( s1 ));
        s1 = "foo/f/";
        System.out.println( "in " + s1 + " out " + Path.stripTrailingSlashes( s1 ));
        s1 = "foo";
        System.out.println( "in " + s1 + " out " + Path.stripTrailingSlashes( s1 ));
        s1 = "";
        System.out.println( "in " + s1 + " out " + Path.stripTrailingSlashes( s1 ));
        s1 = "foo/";
        System.out.println( "in " + s1 + " out " + Path.stripTrailingSlashes( s1 ));
        s1 = "/";
        System.out.println( "in " + s1 + " out " + Path.stripTrailingSlashes( s1 ));
        s1 = "/\\\\ff/\\\\/";
        System.out.println( "in " + s1 + " out " + Path.stripTrailingSlashes( s1 ));
    }
    catch (Exception e)
    {
    }  
  }
}

