package com.ibm.ode.lib.util;

import java.io.*;
import java.util.*;

/**
 * This class provides a utility to issue system (platform) specific
 * commands using the Runtime.getRuntime().exec() method. This is similar to
 * the C-style system() function call. 
 *
 * This class provides buffers for output, as a result of the command, to
 * stdout and stderr. The buffer contents stay around until the next exec()
 * call.
 *
 * Commands are expected with a complete absolute path. If a sub-shell is
 * needed, the calling method should specify the shell, with absolute path,
 * as part of the command.
 *
 * All commands may not run with SystemCall. This is becuase if no shell is
 * explicitly specified in the command, it would use /bin/sh and this may
 * be different from the parent shell. Therefore it is recommended that one
 * use ShellSystemCall. ShellSystemCall uses /bin/ksh as the default shell.
 * But this can be changed to whatever the user wants. 
 *
 * @version 1.11 99/01/15
 * @author Miten Mehta, Heng Chu 
 */
public class SystemCall implements Observer
{

  /**
   * The buffer containing the output to stdout (from the standard output
   * stream
   */
  private StringBuffer stdOut_ = new StringBuffer();

  /**
   * The buffer containing the output to stderr (from the error output
   * stream)
   */
  private StringBuffer stdErr_ = new StringBuffer();

  /**
   * The process object representing the command to be run
   */
  private Process process_ = null;

  /**
   * The thread for collecting output
   **/
  protected Monitor outputMonitor_ = null;

  /**
   * The thread for collecting errors
   **/
  protected Monitor errorMonitor_ = null;

  /**
   * Vector of Observer objects for observing errors
   **/
  protected Vector errorObservers_ = new Vector();

  /**
   * Vector of Observer objects for observing output 
   **/
  protected Vector outputObservers_ = new Vector();

  /**  
   * Constructor for the class SystemCall(). Same as default.
   **/
  public SystemCall () 
  {
  }

  /**********************************************************************
   * Execute a command with an array of environment variables setting, and
   * StringBuffer for output to stdout and stderr. This is a static version
   * so an instance of SystemCall isn't needed. See
   * SystemCall.exec(SystemCall, String, String[], StringBuffer,
   * StringBuffer) for more details.  
   *
   * @param command the command to execute
   * @param envp the environment variables array. Could be null.
   * @param stdout If non-null, it contains the output from stdout upon
   * return. If null, nothing will be sent.
   * @param stderr If non-null, it contains the output from stderr upon
   * return. If null, nothing will be sent.
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 

   */
  static public int exec(String         command,
      String[]       envp,
      StringBuffer   stdout,
      StringBuffer   stderr) throws IOException, 
  InterruptedException
  {
    return exec(new SystemCall(), command, envp, stdout, stderr);
  }


  /**********************************************************************
   *
   * Execute a command with an array of environment variables setting, and
   * StringBuffer for output to stdout and stderr. This is a static version
   * so an instance of SystemCall isn't needed. See
   * SystemCall.exec(SystemCall, String, String[], StringBuffer,
   * StringBuffer) for more details.  
   *
   * @param command        The command to execute
   * @param envp           The environment variables array (could be null)
   * @param stdout         If non-null, it contains the output from stdout 
   *                       upon return. If null, nothing will be sent.
   * @param stderr         If non-null, it contains the output from stderr 
   *                       upon return. If null, nothing will be sent.
   * @param interleave     If true, redirect stderr to stdout
   * @return               The exit value
   * @exception IOException          Thrown from Runtime.exec() for 
   *                                 IO problem 
   * @exception InterruptedException Thrown when the process is interrupted 
   *
   **/

  static public int exec
    ( String command, String[] envp, StringBuffer stdout, 
      StringBuffer stderr, boolean interleave ) 
    throws IOException, InterruptedException
    {

      // If interleave is set to true, then redirect the error stream
      // before calling exec
      String updatedCmd = command;
      if ( interleave == true )
      {
        updatedCmd = getCommandWithInterleaving( command );
      }

      return exec( new SystemCall(), updatedCmd, envp, stdout, stderr );

    }


  /**********************************************************************
   * Execute a command with an array of environment variables setting, and
   * StringBuffer for output to stdout and stderr. This is a static version
   * so an instance of SystemCall isn't needed. See SystemCall.exec(String,
   * String[]) for more details.
   *
   * @param command the command to execute
   * @param envp the environment variables array. Could be null.
   * @param stdout If non-null, it contains the output from stdout upon
   * return. If null, nothing will be sent.
   * @param stderr If non-null, it contains the output from stderr upon
   * return. If null, nothing will be sent.
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  static public int exec(SystemCall     system,
      String         command,
      String[]       envp,
      StringBuffer   stdout,
      StringBuffer   stderr) throws IOException, 
  InterruptedException
  {
    // First just run the command
    int exitValue = system.exec(command, envp);

    // If the stdout/stderr are not null, initialize them and copy results
    // from system stdout/stderr output buffers
    if (stdout != null)
    {
      stdout.setLength(0);
      stdout.append(system.getOutput());
    }
    if (stderr != null)
    {
      stderr.setLength(0);
      stderr.append(system.getError());
    }

    return exitValue;
  }


  /**********************************************************************
   * Execute a command with an array of environment variables setting, and
   * StringBuffer for output to stdout and stderr. This is a static version
   * so an instance of SystemCall isn't needed. See SystemCall.exec(String,
   * String[]) for more details.
   *
   * @param command the command to execute
   * @param envp the environment variables array. Could be null.
   * @param stdout If non-null, it contains the output from stdout upon
   * return. If null, nothing will be sent.
   * @param stderr If non-null, it contains the output from stderr upon
   * return. If null, nothing will be sent.
   * @param interleave  If true, redirect stderr to stdout
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  static public int exec(SystemCall     system,
      String         command,
      String[]       envp,
      StringBuffer   stdout,
      StringBuffer   stderr,
      boolean        interleave) 
    throws IOException, InterruptedException
    {

      // If the interleave flag is true, then get the updated command before
      // calling exec
      String updatedCmd = command;
      if ( interleave == true )
      {
        updatedCmd = getCommandWithInterleaving( command );
      }

      return exec( system, updatedCmd, envp, stdout, stderr );

    }


  /**********************************************************************
   * Execute a command with an array of environment variables setting. The
   * stdOut_ and stdErr_ contain output sent to stdout and stderr. Note the
   * difference from the Runtime.exec() method.
   *
   * The command string should be a single command specified with an
   * absolute path (such as "/bin/ls"), or started in a sub-shell (such as
   * "/bin/sh ls"). This method doesn't start a sub-shell automatically.
   *
   * Subclasses can overwrite this method to add say shell comannd in front
   * of the command.
   *
   * @param command the command to execute
   * @param envp the environment variables array. Could be null.
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  public int exec ( String      command, 
      String[]    envp ) throws IOException, InterruptedException
  {
    process_ = Runtime.getRuntime().exec(command, envp); 
    return waitForCompletion( process_ );
  }


  /**********************************************************************
   * Execute a command with an array of environment variables setting. The
   * stdOut_ and stdErr_ contain output sent to stdout and stderr. Note the
   * difference from the Runtime.exec() method.
   *
   * The command string should be a single command specified with an
   * absolute path (such as "/bin/ls"), or started in a sub-shell (such as
   * "/bin/sh ls"). This method doesn't start a sub-shell automatically.
   *
   * Subclasses can overwrite this method to add say shell comannd in front
   * of the command.
   *
   * @param command the command to execute
   * @param envp the environment variables array. Could be null.
   * @param interleave   If true, redirect stderr to stdout
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  public int exec ( String      command, 
      String[]    envp,
      boolean     interleave ) 
    throws IOException, InterruptedException
    {

      // If the interleave flag is true, redirect stderr to stdout before
      // calling exec
      String updatedCmd = command;
      if ( interleave == true )
      {
        updatedCmd = getCommandWithInterleaving( command );
      }

      return exec( updatedCmd, envp );

    }


  /**********************************************************************
   * Execute a command. The stdOut_ and stdErr_ contain output sent to
   * stdout and stderr. Note the difference from the Runtime.exec() method.
   *
   * @param command the command to execute
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  public int exec( String command ) throws IOException, InterruptedException
  {
    return exec( command, null );
  }


  /**********************************************************************
   * Execute a command. The stdOut_ and stdErr_ contain output sent to
   * stdout and stderr. Note the difference from the Runtime.exec() method.
   *
   * @param command the command to execute
   * @param interleave   If true, redirect stderr to stdout
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  public int exec( String command, boolean interleave ) 
    throws IOException, InterruptedException
    {

      // If the interleave flag is true, redirect stderr to stdout before
      // calling exec
      String updatedCmd = command;
      if ( interleave == true )
      {
        updatedCmd = getCommandWithInterleaving( command );
      }

      return exec( updatedCmd );

    }


  /**********************************************************************
   * Return the output from stdout
   *
   * @return the output from stdout
   **/
  public StringBuffer getOutput () 
  {

    return stdOut_;

  }


  /**********************************************************************
   * Return the output from stderr
   *
   * @return the output from stderr
   **/
  public StringBuffer getError () 
  {

    return stdErr_;

  }

  /**********************************************************************
   * The actual exec function taking a command and a env var arrays. This
   * method is recommended for command with arguments.
   *
   * @param command the command to execute
   * @param envp the environment variables array. Could be null.
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  public int exec(String[] commandArray,
      String[] envp) throws IOException, InterruptedException
  {
    process_ = Runtime.getRuntime().exec(commandArray, envp);
    return waitForCompletion( process_ );
  }

  /**********************************************************************
   * Wait for the process to terminate and collect the output from stdout
   * and stderr. 
   *
   * @param process the process which runs thecommand
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  protected int waitForCompletion(Process process) 
    throws IOException, InterruptedException
    {

      // Create the threads for monitoring errors and output
      // D 7327 - Chary Lingachary
      outputMonitor_ = new OutputProcessMonitor( "OutputProcessMonitor", process );
      errorMonitor_ = new ErrorProcessMonitor( "ErrorProcessMonitor", process );

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

      // Start the threads
      outputMonitor_.start();
      errorMonitor_.start();

      // CHU-D1396
      // We have to do this waitFor() *after* we capture the output, otherwise
      // it will hang on large output (buffered output has to be read/flushed
      // in order for the process to move on/terminate).
      //
      // now we wait for the process to complete
      int exitValue = process.waitFor();

      // The process is done, but we now must wait for the threads collecting
      // the output and the error stream to complete
      outputMonitor_.join();
      errorMonitor_.join();

      return exitValue;

    } 

  /**
   * Set up the dce login command along with the shell and shell option
   *
   * @param command the command to execute
   * @param id DCE login id
   * @param password DCE password
   * @param envp Environment variables
   * @param dcelogin Path and name of DCE login executable
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem
   * @exception InterruptedException thrown when the process is interrupted
   **/
  public int dceExec( String command, String id, String password, 
      String envp[], String dcelogin )
    throws IOException, InterruptedException
    {
      String[] commandArray;

      commandArray = new String[5];
      commandArray[0] = dcelogin;
      commandArray[1] = id;
      commandArray[2] = password;
      commandArray[3] = "-e";
      commandArray[4] = command;

      return exec( commandArray, envp );
    }


  /**
   * Set up the dce login command along with the shell and shell option
   *
   * @param command the command to execute
   * @param id DCE login id
   * @param password DCE password
   * @param envp Environment variables
   * @param dcelogin Path and name of DCE login executable
   * @param interleave  If true, redirect stderr to stdout
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem
   * @exception InterruptedException thrown when the process is interrupted
   **/
  public int dceExec( String command, String id, String password, 
      String envp[], String dcelogin, boolean interleave )
    throws IOException, InterruptedException
    {

      // If the interleave flag is true, redirect stderr to stdout before
      // calling exec
      String updatedCmd = command;
      if ( interleave == true )
      {
        updatedCmd = getCommandWithInterleaving( command );
      }

      return dceExec( updatedCmd, id, password, envp, dcelogin );

    }


  /**
   * Set up the dce login command along with the shell and shell option
   *
   * @param command the command to execute
   * @param id DCE login id
   * @param password DCE password
   * @param envp Environment variables
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem
   * @exception InterruptedException thrown when the process is interrupted
   **/
  public int dceExec( String command, String id, String password, String envp[] )
    throws IOException, InterruptedException
    {
      return dceExec( command, id, password, envp, "dce_login" );
    }


  /**
   * Set up the dce login command along with the shell and shell option
   *
   * @param command the command to execute
   * @param id DCE login id
   * @param password DCE password
   * @param envp Environment variables
   * @param interleave  If true, redirect stderr to stdout
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem
   * @exception InterruptedException thrown when the process is interrupted
   **/
  public int dceExec
    ( String command, String id, String password, String envp[],
      boolean interleave )
    throws IOException, InterruptedException
    {

      // If the interleave flag is true, redirect stdout to stderr before
      // calling exec
      String updatedCmd = command;
      if ( interleave == true )
      {
        updatedCmd = getCommandWithInterleaving( command );
      }

      return dceExec( updatedCmd, id, password, envp );

    }


  /**
   * Set up the dce login command along with the shell and shell option
   *
   * @param command the command to execute
   * @param id DCE login id
   * @param password DCE password
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem
   * @exception InterruptedException thrown when the process is interrupted
   **/
  public int dceExec( String command, String id, String password )
    throws IOException, InterruptedException
    {
      return dceExec( command, id, password, null, "dce_login" );
    }


  /**
   * Set up the dce login command along with the shell and shell option
   *
   * @param command the command to execute
   * @param id DCE login id
   * @param password DCE password
   * @param interleave  If true, redirect stderr to stdout
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem
   * @exception InterruptedException thrown when the process is interrupted
   **/
  public int dceExec
    ( String command, String id, String password, boolean interleave )
    throws IOException, InterruptedException
    {

      // If the interleave flag is true, redirect stdout to stderr before
      // calling exec
      String updatedCmd = command;
      if ( interleave == true )
      {
        updatedCmd = getCommandWithInterleaving( command );
      }

      return dceExec( updatedCmd, id, password );

    }


  /**
   * Destroy the process
   */
  public void destroy()
  {
    if( process_ != null )
    {
      process_.destroy();
      process_ = null;
    }
  }


  /**********************************************************************
   *
   * Add an error stream observer
   * 
   * @param errorO          Error stream observer
   *
   **/

  public void addErrorObserver( Observer errorO )
  {

    errorObservers_.addElement( errorO );

  }


  /**********************************************************************
   *
   * Add a vector of error stream observers
   * 
   * @param errorV          Vector of error stream observers
   *
   **/

  public void addErrorObservers( Vector errorV )
  {

    for ( Enumeration enumer = errorV.elements(); enumer.hasMoreElements(); )
    {
      errorObservers_.addElement( (Observer)( enumer.nextElement() ) );
    }

  }


  /**********************************************************************
   *
   * Add an array of error stream observers
   * 
   * @param errorA          Array of error stream observers
   *
   **/

  public void addErrorObservers( Observer errorA[] )
  {

    for ( int i = 0; i < errorA.length; i++ )
    {
      errorObservers_.addElement( errorA[i] );
    }

  }


  /**********************************************************************
   *
   * Add an output stream observer
   * 
   * @param outputO         Output stream observer
   *
   **/

  public void addOutputObserver( Observer outputO )
  {

    outputObservers_.addElement( outputO );

  }


  /**********************************************************************
   *
   * Add a vector of output stream observers
   * 
   * @param outputV         Vector of output stream observers
   *
   **/

  public void addOutputObservers( Vector outputV )
  {

    for ( Enumeration enumer = outputV.elements(); enumer.hasMoreElements(); )
    {
      outputObservers_.addElement( (Observer)( enumer.nextElement() ) );
    }

  }


  /**********************************************************************
   *
   * Add an array of output stream observers
   * 
   * @param outputA         Array of output stream observers
   *
   **/

  public void addOutputObservers( Observer outputA[] )
  {

    for ( int i = 0; i < outputA.length; i++ )
    {
      outputObservers_.addElement( outputA[i] );
    }

  }


  /************************************************************************
   *
   * Update the stdout and/or stderr buffers
   *
   * @param o               Observable object
   * @param arg             Object that was updated
   *
   **/

  public void update( Observable o, Object arg )
  {

    // Determine if the data is from the output stream or the error
    // stream and then update the buffers appropriately
    if ( o instanceof Monitor )
    {
      if( ((Monitor)o).getMonitorType() == 0 )
      {
        stdOut_.append( (String)( arg ) );
        //System.out.println("SystemCall out: " + (String)arg );
      }
      else
      {
        stdErr_.append( (String)( arg ) );
        //System.out.println("SystemCall err: " + (String)arg );
      }
    }
  }


  /************************************************************************
   *
   * Return a command that appends an error redirection command to the
   * original command
   *
   * @param cmd             Original command 
   * @return                Updated command with error redirection
   *
   **/

  public static String getCommandWithInterleaving( String cmd )
  {

    String pipe = "2>&1";

    // Parse the original command into a vector of single commands...each
    // single command will have to have the error stream redirected
    Vector singleCmds = Utils.parseData( cmd, "&&" );

    // Add the error stream redirection for each single command and add
    // all of the single commands back together
    StringBuffer updatedCmd = new StringBuffer( "" );
    for ( int i = 0; i < singleCmds.size() - 1; i++ )
    {
      String single = ( (String)( singleCmds.elementAt( i ) ) ).trim();
      if ( single.endsWith( pipe ) == true )
      {
        updatedCmd.append( single + " && " );
      }
      else
      {
        updatedCmd.append( single + " " + pipe + " && " );
      }
    }

    // Don't forget the last element (which doesn't add the "&&")
    String lastCmd = ( (String)( singleCmds.lastElement() ) ).trim();
    if ( lastCmd.endsWith( pipe ) == true )
    {
      updatedCmd.append( lastCmd );
    }
    else
    {
      updatedCmd.append( lastCmd + " " + pipe );
    }

    // Return the updated command
    return updatedCmd.toString();

  }

}
