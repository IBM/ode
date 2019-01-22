package com.ibm.ode.lib.util;

import java.io.*;
import java.lang.*;
import java.util.*;

/**
 * Execute the command in an NT shell defaulted to "cmd.exe /C". Note that from
 * unofficial Java doc (FAQ for Programmers), command like '/bin/ksh -c
 * "mycommand options"' has to be broken up into three tokens "cmd.exe",
 * "/C", and "mycommand options". Otherwise the shell will just treat
 * mycommand as the only command for cmd.exe to run.
 *
 * Also note that command passed in as a whole (whether itself has options
 * or arguments) is treated as an argument to the shell command.
 *
 * @author Chary Lingachary
 * @version      1.15.1.2 99/04/14
 */
public class NTShellSystemCall extends ShellSystemCall
{
  /**
   * Java's classpath property name
   **/
  public final String CLASSPATHPROPERTY = "java.class.path";

  /**
   * jni library name
   **/
  private final String dllName_ = "bpsnt.dll";

  /**
   * RMS Provider Jar file name
   **/
  private final String provJarFile_ = "bpsRMSProvider.jar";

  /**
   * Trace flag
   **/
  private boolean traceEnabled_ = false;

  /***************************************************************
   * Constructor sets 'cmd.exe' as
   * SHELL and '/C' as the SHELL option in the base class
   **/
  public NTShellSystemCall()
  {
    super();
    setShell( "cmd.exe" );
    setShellOption( "/C" );
    String tString = System.getProperty("bps.nt.trace.enabled");
    if ( tString != null && tString.equals("true") )
    {
      traceEnabled_ = true;
    }
  }

  /***************************************************************
   * Overides the method in the base class "SystemCall.java".
   * This uses jni library for creating NT process and
   * sets the standard out and standard error from the child
   * process in the base class's stdOut_ and stdErr_ buffers.
   *
   * @param cmdarray string array containing command to be run
   * @param envp string array conataining env strings to be used
   *             <br>for running the command
   * @exception IOException
   * @exception InterruptedException
   *
   * @author Chary Lingachary 1/21/98
   **/
  /* D 7428 - Chary Lingachary
   * This method is commented for now we want to use
   * the jdk fix hurshley is putting in JDK1.1.7p
   *
  public int exec( String[] cmdarray, String[] envp )
    throws IOException, InterruptedException
    {
      // get bpsnt.dll path. Path is extracted from CLASSPATH.
      // bpsnt.dll is expected to be in the same directory as the bpsRMSProvider.jar
      // file.
      printTrace("Extracting bpsnt.dll path from CLASSPATH..." );
      String dllPath = getFilePath( provJarFile_ );
      printTrace( "bpsnt.dll path: " + dllPath );
      // load dll
      if( !dllPath.equals("") )
      {
        printTrace( "JNI Library " + dllName_ + " is loading from " + dllPath );
        System.load( dllPath + "\\" + dllName_ );
      }
      else
      {
        printTrace( "JNI Library " + dllName_ + " is loading from current directory or PATH" );
        System.load( dllName_ );
      }

      // D 7327
      // create monitors
      outputMonitor_ = new FileHandleMonitor( "FileOutputMonitor");
      errorMonitor_ = new FileHandleMonitor( "FileErrorMonitor" );

      // set error monitor type = 1
      // 1 means stderr monitor
      errorMonitor_.setMonitorType(1);	

      // If there are no observers, then add this system call as the observer
      // and buffer the output and errors as before
      if ( outputObservers_.size() == 0 )
      {
        this.addOutputObserver( this );
      }
      if ( errorObservers_.size() == 0 )
      {
        this.addErrorObserver( this );
      }

      // Add any observers to the threads
      outputMonitor_.addObservers( outputObservers_ );
      errorMonitor_.addObservers( errorObservers_ );

      // call jni routine to create an NT process
      printTrace( "Executing native method call..." );
      int exitCode = modifiedExecInternal( cmdarray, envp, getOutput(), getError() );

      // wait for monitor threads to finish	
      // reading stdout and stderr
      outputMonitor_.join();
      errorMonitor_.join();

      // return the exit code
      printTrace( "Return code from native call: " + exitCode );
      return exitCode;
    }
  */

  /**
   * Start monitor threads
   * Starts both monitors
   */
  public void startMonitorThreads()
  {
    outputMonitor_.start();
    errorMonitor_.start();
  }

  /**
   * set stdout file handle
   */
  void setOutFileHandle( int handle )
  {
    if( outputMonitor_ != null )
    {
      ((FileHandleMonitor)outputMonitor_).setFileHandle( handle );
    }
  }

  /**
   * set stderr file handle
   */
  void setErrFileHandle( int handle )
  {
    if( errorMonitor_ != null )
    {
      ((FileHandleMonitor)errorMonitor_).setFileHandle( handle );
    }
  }

  /************************************************************************
   *
   * Override the dceExec method in the base class
   *
   * @param command              Command to execute
   * @param id                   DCE login id
   * @param password             DCE password
   * @param envp                 Environment variables
   * @param dcelogin             Path and name of dce login executable
   * @return                     Return code
   * @exception IOException thrown from Runtime.exec() for IO problem
   * @exception InterruptedException thrown when the process is interrupted
   *
   **/

  public int dceExec( String command, String id, String password,
      String envp[], String dcelogin )
    throws IOException, InterruptedException
    {

      String[] commandArray;

      commandArray = new String[7];
      commandArray[0] = dcelogin;
      commandArray[1] = id;
      commandArray[2] = password;
      commandArray[3] = "-e";
      commandArray[4] = getShell();
      commandArray[5] = getShellOption();
      commandArray[6] = command;

      printTrace( "Running command under DCE authentication..." );
      int retValue = exec( commandArray, envp );

      return retValue;

    }

  /************************************************************************
   *
   * Override the dceExec method in the base class
   *
   * @param command              Command to execute
   * @param id                   DCE login id
   * @param password             DCE password
   * @param envp                 Environment variables
   * @param dcelogin             Path and name of dce login executable
   * @param interleave           Indicates if interleaving should be done
   * @return                     Return code
   * @exception IOException thrown from Runtime.exec() for IO problem
   * @exception InterruptedException thrown when the process is interrupted
   *
   **/

  public int dceExec( String command, String id, String password,
      String envp[], String dcelogin, boolean interleave )
    throws IOException, InterruptedException
    {

      String[] commandArray;

      // If interleave is set to true, then redirect the error stream
      // before calling exec
      String updatedCmd = command;
      if ( interleave == true )
      {
        updatedCmd = getCommandWithInterleaving( command );
      }

      commandArray = new String[7];
      commandArray[0] = dcelogin;
      commandArray[1] = id;
      commandArray[2] = password;
      commandArray[3] = "-e";
      commandArray[4] = getShell();
      commandArray[5] = getShellOption();
      commandArray[6] = updatedCmd;

      printTrace( "Running command under DCE authentication..." );
      int retValue = exec( commandArray, envp );

      return retValue;

    }

  /***************************************************************
   * Override the method in the base class
   **/
  public int dceExec( String command, String id,
      String password, String envp[] )
    throws IOException, InterruptedException
    {

      return dceExec( command, id, password, envp, "dce_login" );

    }

  /***************************************************************
   * Override the method in the base class
   **/
  public int dceExec( String command, String id,
      String password, String envp[], boolean interleave )
    throws IOException, InterruptedException
    {

      return dceExec( command, id, password, envp, "dce_login", interleave );

    }

  /*************************************************************************
   * This native method uses NT's WIN32 api CreateProcess(). CreateProcess
   * is used to spawn a child process and redirect child's stdout and stderr
   * to parent's handles. The side effect of this is that child's stdout and
   * stderr will not appear on parent's console. The only way to get them
   * is to use methods getOutput() and stdError().
   *
   * @param cmdarray Command to be executed in the form of String[]
   * @param envp Environment variables to be passed into the child process,
   *             <br>in the form of String[]
   * @param stdOut Place holder for child's stdout
   * @param stdErr Place holder fot child's stderr
   * @return child process's exit code
   *
   * @author Chary Lingachary 1/20/98
   **/
  private native int modifiedExecInternal( String[] cmdarray,
      String[] envp,
      StringBuffer stdOut,
      StringBuffer stdErr )
    throws IOException;

  /**************************************************************
   * Extract the directory path from CLASSPATH for a given file.
   *
   * @return bpsnt.dll path
   **/
  private String getFilePath( String filename )
  {
    String dirPath = "";
    String clsPath = System.getProperty( CLASSPATHPROPERTY );
    StringTokenizer st = new StringTokenizer( clsPath, ";" );
    while ( st.hasMoreElements() )
    {
      String path = st.nextToken();
      int end = path.lastIndexOf( filename );
      if ( end != -1 )
      {
        dirPath = path.substring( 0, end );
        break;
      }
    }

    // remove trailing '\' if present.
    if ( dirPath.length() > 0 && dirPath.charAt( dirPath.length()-1 ) == '\\' )
    {
      dirPath = dirPath.substring( 0, dirPath.length()-1 );
    }

    // Return value of the bps.library.dir property if the
    // serviceProvider.jar is not there in the classpath. This
    // would be the case when this method is run by BPS server on
    // windows NT.
    // NOTE:This change is made to make BPS server run on Windows NT.
    if ( dirPath.trim().length() == 0 )          // AAK-D5683
    {
      dirPath = System.getProperty( "bps.nt.library.dir", "" ); //amm-D5745
      return dirPath;
    }
    else
    {
      return dirPath;
    }
  }


  /***************************************************************
   * Print trace messages if the trace flag "bps.nt.trace.enabled"
   * is set to "true"
   **/
  private void printTrace( String msg )
  {
    if ( traceEnabled_ )
    {
      System.out.println( msg );
    }
  }

  /***************************************************************
   * This method extracts a file from the jar file
   * ( specified on classpath ) and creates a physical file
   *
   * @param fileToExtract file to be extracted from classpath
   * @param whereToExtract fullpath of the file to be created
   * @exception throws IOException if file cannot be extracted
   *            or file cannot be created
   *
   * @author Chary Lingachary 1/21/98
   **/
  public static void
    extractResourceFromJar( String fileToExtract,
        String whereToExtract )
    throws IOException
    {
      // check to see if the dll already exits
      if ( new File( whereToExtract ).exists() )
        return;

      InputStream infile = fileToExtract.getClass().getResourceAsStream( fileToExtract );
      OutputStream outfile = new FileOutputStream( whereToExtract );

      if ( infile == null )
      {
        throw new IOException( "Error extracting " + fileToExtract );
      }

      if ( outfile == null )
      {
        throw new IOException( "Error creating " + whereToExtract );
      }

      // buffer for block read
      byte[] buffer = new byte[1000];
      int bytesRead;

      // Now just copy the stuff over
      while ( (bytesRead = infile.read(buffer)) >= 0 )
      {
        outfile.write( buffer, 0, bytesRead );
      }

      infile.close();
      outfile.close();
    }
}
