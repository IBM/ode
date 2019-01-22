package com.ibm.ode.lib.util;

import java.io.*;

/**
 * Execute the command in a shell defaulted to "/bin/ksh -c". Note that from
 * unofficial Java doc (FAQ for Programmers), command like '/bin/ksh -c
 * "mycommand options"' has to be broken up into three tokens "/bin/ksh",
 * "-c", and "mycommand options". Otherwise the shell will just treat
 * mycommand as the only command for /bin/ksh to run. 
 *
 * Also note that command passed in as a whole (whether itself has options
 * or arguments) is treated as an argument to the shell command.
 */
public class ShellSystemCall extends SystemCall
{
  /**
   * The shell that starts the command
   */
  private String shell_ = "/bin/ksh";

  /**
   * The option to the shell_
   */
  private String shellOption_ = "-c";


  /**********************************************************************
   * Execute a command with an array of environment variables setting, and
   * StringBuffer for output to stdout and stderr. This is a static version
   * so an instance of ShellSystemCall isn't needed. See
   * ShellSystemCall.exec(String, String[]) for more details.
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
    return exec(new ShellSystemCall(), command, envp, stdout, stderr);
  }

  /**********************************************************************
   * Execute a command with an array of environment variables setting, and
   * StringBuffer for output to stdout and stderr. This is a static version
   * so an instance of ShellSystemCall isn't needed. See
   * ShellSystemCall.exec(String, String[]) for more details.
   *
   * @param command the command to execute
   * @param envp the environment variables array. Could be null.
   * @param stdout If non-null, it contains the output from stdout upon
   * return. If null, nothing will be sent.
   * @param stderr If non-null, it contains the output from stderr upon
   * return. If null, nothing will be sent.
   * @param interleave - indicates if stdout/stderr should be interleaved
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  static public int exec(String         command,
                         String[]       envp,
                         StringBuffer   stdout,
                         StringBuffer   stderr,
                         boolean        interleave ) 
    throws IOException, InterruptedException
  {
    return exec(new ShellSystemCall(), command, envp, stdout, 
                stderr, interleave);
  }

  /**********************************************************************
   * Overwrite the base class method so we can add the shell command and its
   * option. 
   *
   * @param command the command to execute
   * @param envp the environment variables array
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */

  public int exec(String        command,
                  String[]      envp) throws IOException, InterruptedException
  {
    String[] commandArray;

    // put some intelligence here so we omit empty shell option. This is
    // needed so Java don't choke on empty command in the command array

    if (shellOption_.equals(""))
    {
      commandArray = new String[2];
      commandArray[0] = shell_;
      commandArray[1] = command;
    }
    else
    {
      commandArray = new String[3];
      commandArray[0] = shell_;
      commandArray[1] = shellOption_;
      commandArray[2] = command;
    }

    return exec(commandArray, envp);
  }

  /**********************************************************************
   * Overwrite the base class method so we can add the shell command and its
   * option. 
   *
   * @param command the command to execute
   * @param envp the environment variables array
   * @param interleave - indicates if stdout/stderr should be interleaved
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */

  public int exec(String        command,
                  String[]      envp,
                  boolean       interleave) 
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

    // put some intelligence here so we omit empty shell option. This is
    // needed so Java don't choke on empty command in the command array

    if (shellOption_.equals(""))
    {
      commandArray = new String[2];
      commandArray[0] = shell_;
      commandArray[1] = updatedCmd;
    }
    else
    {
      commandArray = new String[3];
      commandArray[0] = shell_;
      commandArray[1] = shellOption_;
      commandArray[2] = updatedCmd;
    }

    return exec(commandArray, envp);
  }

  /**********************************************************************
   * Set the shell command
   *
   * @param shell the new shell command
   */
  public void setShell(String shell) 
  {
    shell_ = shell;
  }

  /**********************************************************************
   * Set the shell command option. This should be rately used.
   *
   * @param option the new shell command option
   */
  public void setShellOption(String option) 
  {
    shellOption_ = option;
  }

  /**********************************************************************
   * Get the shell command
   *
   * @return the shell command
   */
  public String getShell() 
  {
    return shell_;
  }

  /**********************************************************************
   * Get the shell command option
   *
   * @return the shell command option
   */
  public String getShellOption() 
  {
    return shellOption_;
  }


  /**********************************************************************
   *
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
   *
  **/

  public int dceExec( String command, String id, String password, 
                      String envp[], String dcelogin )
             throws IOException, InterruptedException
  {
    String[] commandArray;

    // put some intelligence here so we omit empty shell option. This is
    // needed so Java don't choke on empty command in the command array
    if ( shellOption_.equals( "" ) )
    {
      commandArray = new String[6];
      commandArray[0] = dcelogin;
      commandArray[1] = id;
      commandArray[2] = password;
      commandArray[3] = "-e";
      commandArray[4] = shell_;
      commandArray[5] = command;
    }
    else
    {
      commandArray = new String[7];
      commandArray[0] = dcelogin;
      commandArray[1] = id;
      commandArray[2] = password;
      commandArray[3] = "-e";
      commandArray[4] = shell_;
      commandArray[5] = shellOption_;
      commandArray[6] = command;
    }

    return exec( commandArray, envp );
  }

  /**********************************************************************
   *
   * Set up the dce login command along with the shell and shell option
   *
   * @param command the command to execute
   * @param id DCE login id
   * @param password DCE password
   * @param envp Environment variables
   * @param dcelogin Path and name of DCE login executable
   * @param interleave - indicates if stdout/stderr should be interleaved
   * @return the exit value
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

    // put some intelligence here so we omit empty shell option. This is
    // needed so Java don't choke on empty command in the command array
    if ( shellOption_.equals( "" ) )
    {
      commandArray = new String[6];
      commandArray[0] = dcelogin;
      commandArray[1] = id;
      commandArray[2] = password;
      commandArray[3] = "-e";
      commandArray[4] = shell_;
      commandArray[5] = updatedCmd;
    }
    else
    {
      commandArray = new String[7];
      commandArray[0] = dcelogin;
      commandArray[1] = id;
      commandArray[2] = password;
      commandArray[3] = "-e";
      commandArray[4] = shell_;
      commandArray[5] = shellOption_;
      commandArray[6] = updatedCmd;
    }

    return exec( commandArray, envp );
  }

  /**********************************************************************
   *
   * Set up the dce login command along with the shell and shell option
   *
   * @param command the command to execute
   * @param id DCE login id
   * @param password DCE password
   * @param envp Environment variables
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   *
  **/

  public int dceExec( String command, String id, String password, 
                      String envp[] )
             throws IOException, InterruptedException
  {
    return dceExec( command, id, password, envp, "dce_login" );
  }

  /**********************************************************************
   *
   * Set up the dce login command along with the shell and shell option
   *
   * @param command the command to execute
   * @param id DCE login id
   * @param password DCE password
   * @param envp Environment variables
   * @param interleave - indicates if stdout/stderr should be interleaved
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   *
  **/

  public int dceExec( String command, String id, String password, 
                      String envp[], boolean interleave )
             throws IOException, InterruptedException
  {
    return dceExec( command, id, password, envp, "dce_login", interleave );
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
     return dceExec( command, id, password, null );
  }

  /**
   * Set up the dce login command along with the shell and shell option
   *
   * @param command the command to execute
   * @param id DCE login id
   * @param password DCE password
   * @param interleave - indicates if stdout/stderr should be interleaved
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
  **/
  public int dceExec( String command, String id, String password,
                      boolean interleave )
             throws IOException, InterruptedException
  {
     return dceExec( command, id, password, null, interleave );
  }

}
