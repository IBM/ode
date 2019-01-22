package com.ibm.ode.bin.sandboxfiles;

import java.io.*;
import java.util.StringTokenizer;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.string.*;


/**
 * This encapsulates the per-file/directory information from the sbls output.
 * It also has the job of parsing a line of output from sbls -alp to store
 * the information in the SFInfo object.
**/
class SFInfo
{
  public final static String TYPE_FILE = "file";
  public final static String TYPE_DIR  = "dir";
  public final static String TYPE_OTHER = "other";
  public final static int STATUS_ERROR = -1; // incorrect format of input
  public final static int STATUS_OK = 0;     // input not blank or invalid
  public final static int STATUS_BLANK = 1;  // input is blank

  private static String msgNoError = "No error";
  private static String msgBlank = "Blank input line";
  private static String msgLineFormatError = "Incorrect line format: ";
  private static String msgDateTimeFormatError =
                                              "Incorrect date or time format";
  private static String msgSizeError = "Incorrect size format.";
  private static String msgNotFileOrDirectoryError = "Not file or directory.";

  private String[] chain;
  private int chainLength;
  private String fullPath;
  private String base;
  private String path;
  private String date;
  private long size;
  private String fileName;
  private String suffix;
  private String type; // "file", "dir", or "other"
      // Is /dev/tty0 a file as seen by Java?
      // I think symbolic links are resolved to dir or file.
  private int pos;     // 0=not on chain, 1=sandbox, etc
  private int status;
  private String statusMsg;
  private static final String[] monthNames = { "Jan", "Feb", "Mar", "Apr",
                                               "May", "Jun", "Jul", "Aug",
                                               "Sep", "Oct", "Nov", "Dec" };
  private static final String[] monthNumbers = { "01", "02", "03", "04",
                                                 "05", "06", "07", "08",
                                                 "09", "10", "11", "12" };
                      

  /**
   * The constructor creates an object representing information about one 
   * file or directory.
   * This constructor is used before running parse(). The idea is to create
   * one SFInfo object and reuse it to parse each line from sbls to save
   * some time.
   * @param chain is null or the backing chain, where chain[0] is the
   * sandbox itself.
  **/
  SFInfo( String[] chain )
  {
    this.chain = chain;
    if (chain != null)
      chainLength = chain.length;
    else
      chainLength = 0;
  }


  /**
   * This method parses the line from sbls and stores the results in itself.
   * It assumes that any backing chain to be considered will be given to the
   * constructor first.
   * @param String non-blank trimmed line from output typically of sbls -alpR
   * of the form "ssssss MMM dd hh:mm:ss yyyy fullpath"
   * -lp is a required part of the sbls flag. -F should not be used in the
   * in the sbls flag since it can append characters that make the fullpath
   * be ambiguous. '^' and '@' can be legal filename characters.
   * "ssssss" is one or more characters indicating the size of the file,
   * "MMM dd hh:mm:ss yyyy" represents the timestamp of the file.
   * sbls can also output blank lines or lines that begin with '>', which
   * are warning or error messages. If the first non-blank character is
   * '>', then that line will be used as the error message and status will
   * be set to STATUS_ERROR.
   * @param sblsLine is a line of output from sbls.
   * After running the constructor the status of the object should be checked
   * using getStatus(). The object is only useful if the status is STATUS_OK,
   * except for the getStatusMsg() method.
  **/
  public void parse( String sblsLine )
  {
    if (sblsLine.trim().equals( "" ))
    {
      status = STATUS_BLANK;
      statusMsg = msgBlank;
      return;
    }
    status = STATUS_OK; // this may change later if we find an error
    statusMsg = msgNoError;
    // crank up a token reader
    String month = new String();  // keep the compiler happy
    // use split such that we get the file last, in token[5] even if it
    // has blanks in the filepath (because of Windows!).
    // Although ODE does not support sandboxes with blank filenames, we
    // could be given a filespec that gets filenames with blanks.
    String[] token = StringTools.split( sblsLine, " ", 6 );
    // do some sanity checking
    if (token == null || token.length < 6 || token[5].equals( "" ))
    {
      statusMsg = msgLineFormatError + "'" + sblsLine + "'";
      status = STATUS_ERROR;
      return;
    }
    if (token[0].charAt( 0 ) == '>')
    {
      statusMsg = sblsLine;
      status = STATUS_ERROR;
      return;
    }
    if (!allDigits( token[0] ))
    {
      status = STATUS_ERROR;
      statusMsg = msgSizeError;
      return;
    }
    month = translateMonth( token[1] );
    if (month.equals( "" ) ||
        !allDigits( token[2] ) || token[2].length() > 2 ||
        !isTime( token[3] ) ||
        !allDigits( token[4] ))
    {
      status = STATUS_ERROR;
      statusMsg = msgDateTimeFormatError;
      return;
    }
    
    if (token[2].length() == 1)
      token[2] = "0" + token[2];
    date = token[4] + "-" + month + "-" + token[2] + " " + token[3];
    size =  Long.parseLong( token[0] );
    fullPath = token[5];
    pos = splitAtBase( fullPath );
    fileName = Path.fileName( path );
    if (fileName == null)
      fileName = "";
    suffix = Path.fileSuffix( fileName, true );
    if (suffix == null)
      suffix = "";
    File fp = new File( fullPath );
    if (fp.isDirectory())
      type = TYPE_DIR;
    else if (fp.isFile())
      type = TYPE_FILE;
    else
    {
      // some of the /dev items on UNIX do not look like a file or directory
      // but otherwise have a legitimate format.
      status = STATUS_OK;
      statusMsg = msgNotFileOrDirectoryError;
      type = TYPE_OTHER;
      return;
    }
  }


  /**
   * This constructor is useful when we have a row in the Object[][] data array
   * and want to pass information to code that is interested in selections.
   * An SLInfo object is getter than Object[].
  **/
  SFInfo( String  base, String  path, String date, Long size, String name,
          String suffix, String type, Integer pos, String fullPath )
  {
    chain = null;
    this.base = base;
    this.path = path;
    this.date = date;
    this.size = size.longValue();
    this.fileName = name;
    this.suffix = suffix;
    this.type = type;
    this.pos = pos.intValue();
    this.fullPath = fullPath;
  }
    
    
  private String translateMonth( String letterMonth )
  { 
    for (int i = 0; i < 12; ++i)
      if (letterMonth.equalsIgnoreCase( monthNames[i] ))
        return monthNumbers[i];
    return "";
  }


  private boolean isTime( String t )
  {
    return (t.length() == 8 &&
            Character.isDigit( t.charAt( 0 ) ) &&
            Character.isDigit( t.charAt( 1 ) ) &&
            t.charAt( 2 ) == ':' &&
            Character.isDigit( t.charAt( 3 ) ) &&
            Character.isDigit( t.charAt( 4 ) ) &&
            t.charAt( 5 ) == ':' &&
            Character.isDigit( t.charAt( 6 ) ) &&
            Character.isDigit( t.charAt( 7 ) ));
  }
   

  private boolean allDigits( String s )
  {
    for (int i = 0; i < s.length(); ++i)
      if (!Character.isDigit( s.charAt( i ) ))
        return false;
    return true;
  }


  // returns position in chain; 0 is not in backing chain, 1 is current
  // sandbox, 2 is what backs that sandbox, etc.
  // As a side effect, the object base and path fields are set.
  private int splitAtBase( String fullPath )
  {
    int ix;
    for (int i = 0; i < chainLength; ++i)
    {
      ix = splitPoint( chain[i], fullPath );
      if (ix > 0)
      {
        // in a sandbox
        base = chain[i];
        path = fullPath.substring( ix + 1 );
        return (i + 1);
      }
    }
    // not in a sandbox
    path = fullPath;
    base = "";
    return 0;
  }


  private int splitPoint( String chi, String fullPath )
  {
    // comparison must ignore case on some platforms
    if (chi.length() >= fullPath.length())
      return 0;
    if (PlatformConstants.onCaseSensitiveOS())
    {
      if (fullPath.startsWith( chi ))
        return chi.length();
    }
    else
    {
      if (fullPath.toUpperCase().startsWith( chi.toUpperCase() ))
        return chi.length();
    }
    return 0;
  }


  /**
   * Indicate whether the object was constructed from valid data (STATUS_OK)
   * or the data was blank (STATUS_BLANK) or the data had incorrect format
   * (STATUS_ERROR).
  **/
  public int getStatus()
  {
    return status;
  }


  /**
   * Provide message explaining the status.
  **/
  public String getStatusMsg()
  {
    return statusMsg;
  }


  /**
   * returns the sandbox base if the file is in a sandbox, otherwise ""
  **/
  String getBase()
  {
    return base;
  }


  /**
   * returns the path of the file, either relative to the base if it is in
   * a sandbox, or the absolute path of the file if it is not in a sandbox.
  **/
  String getPath()
  {
    return path;
  }


  /**
   * returns the full path (base concatenated with path)
  **/
  String getFullPath()
  {
    return fullPath;
  }


  /**
   * returns the file name (root + suffix)
  **/
  String getFileName()
  {
    return fileName;
  }


  /**
   * returns the file name (root + suffix)
  **/
  String getSuffix()
  {
    return suffix;
  }


  /**
   * return size of file
  **/
  long getSize()
  {
    return size;
  }


  /**
   * returns 0 if the file is not in the backing chain, or a non-zero integer
   * indicating position in the chain, 1 being the sandbox itself.
  **/
  int getPosition()
  {
    return pos;
  }


  /**
   * return date-time in the sortable form "yyyy-mm-dd hh:mm:ss"
  **/
  String getDate()
  {
    return date;
  }


  /**
   * return type as a string of value "file", "dir", or "other"
  **/
  String getType()
  {
    return type;
  }


  public String toString()
  {
    return ( "SFInfo: base=" + base + " path=" + path +
             " date=" + date + " size=" + size +
             " fileName=" + fileName + " suffix=" + suffix +
             " type=" + type + " pos=" + pos
           );
  }
                        
}
