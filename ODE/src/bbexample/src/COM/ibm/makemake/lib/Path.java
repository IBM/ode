package COM.ibm.makemake.lib;

import java.io.*;

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
}
