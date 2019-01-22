package com.ibm.ode.lib.util;

import com.ibm.ode.lib.string.*;
import com.ibm.ode.lib.io.Interface;

import java.io.*;
import java.lang.*;
import java.util.*;

/**
 * This class exists to supply what should have been supplied in 
 * com/ibm/ode/lib/util/ShellSystemCall.java.
 *
 * A set of methods should have been supplied that queries
 * the PlatformConstants to select the correct shell and shellOption.
 *
 * Sigh... Unfortunately at this time (February 27, 2002) I don't have a
 * whole lot of time to do all that myself, and so will add only what is 
 * needed to make my GUI work.
**/
public class PlatformShellSystemCall extends ShellSystemCall
{
  public final int CMD_SHELL     = 0; // value for shellType_ Win/NT & OS/2
  public final int COMMAND_SHELL = 1; // value for shellType_ Win95/98
  public final int SH_SHELL      = 2; // value for shellType_ MVS
  public final int KSH_SHELL     = 3; // value for shellType_ UNIX
  private int shellType_; // type of shell
  private String cmdsep_; // type of command separator && or |

  private String shell_;       // copied to the super class
  private String shellOption_; // copied to the super class

  public PlatformShellSystemCall()
  {
    if (PlatformConstants.isMvsMachine( PlatformConstants.CURRENT_MACHINE ))
    {
      shell_ = "/bin/sh";
      shellOption_ = "-c";
      shellType_ = SH_SHELL;
      cmdsep_ = "&&";
    }
    else if (PlatformConstants.isUnixMachine(
                                        PlatformConstants.CURRENT_MACHINE ))
    {
      shell_ = "/bin/ksh";
      shellOption_ = "-c";
      shellType_ = KSH_SHELL;
      cmdsep_ = "&&";
    }
    else if ( PlatformConstants.X86_95_4_MACHINE.equals( 
                                    PlatformConstants.CURRENT_MACHINE )
            ) // test this before general test for Windows machine
    {
      shell_ = "command.com";
      shellOption_ = "/C";
      shellType_ = COMMAND_SHELL;
      cmdsep_ = "|";
    }
    else if ( PlatformConstants.isWindowsMachine( 
                                    PlatformConstants.CURRENT_MACHINE ) ||
              PlatformConstants.X86_OS2_4_MACHINE.equals( 
                                    PlatformConstants.CURRENT_MACHINE )
            )
    {
      shell_ = "cmd.exe";
      shellOption_ = "/C";
      shellType_ = CMD_SHELL;
      cmdsep_ = "&&";
    }
    else // default
    {
      shell_ = "/bin/ksh";
      shellOption_ = "-c";
      shellType_ = KSH_SHELL;
      cmdsep_ = "&&";
    }
    // in case we call the superclass code
    super.setShell( shell_ );
    super.setShellOption( shellOption_ );
  }


  public String getShell()
  {
    return shell_;
  }


  public String getShellOption()
  {
    return shellOption_;
  }


  public String getCmdsep()
  {
    return cmdsep_;
  }


  public int getShellType()
  {
    return shellType_;
  }


  /**********************************************************************
   * Execute a command with an array of environment variables setting, and
   * StringBuffer for output to stdout and stderr. This is a static version
   * so an instance of PlatformShellSystemCall isn't needed. See
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
    ShellSystemCall call = new PlatformShellSystemCall();
    int retcode = call.exec( command, envp );
    if (stdout != null)
    {
      stdout.setLength(0);
      stdout.append( call.getOutput() );
    }
    if (stderr != null)
    {
      stderr.setLength(0);
      stderr.append( call.getError() );
    }
    return retcode;
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
    ShellSystemCall call = new PlatformShellSystemCall();
    int retcode = call.exec( command, envp, interleave );
    if (stdout != null)
    {
      stdout.setLength(0);
      stdout.append( call.getOutput() );
    }
    if (stderr != null)
    {
      stderr.setLength(0);
      stderr.append( call.getError() );
    }
    return retcode;
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
                  String[]      envp
                  ) throws IOException, InterruptedException
  {
    String[] commandArray;
    
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

    return super.exec(commandArray, envp);
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
                  boolean       interleave
                  ) throws IOException, InterruptedException
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

    return super.exec(commandArray, envp);
  }


  /**********************************************************************
   * This method runs the shell with the specified command, but does
   * not wait for completion. It returns the process in case the callers
   * wishes to wait for completion of the process. This method can be used
   * to start a process that runs independently, such as build, workon, etc.
   * Unfortunately, it does not cause a new window to display.
   *
   * @param command the command to execute
   * @param envp the environment variables array
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
  **/
  public Process execNoWait( String command,
                             String[] envp
                           ) throws IOException, InterruptedException
  {
    String[] commandArray;

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

    // The super class only has exec() methods that wait on the
    // process completion. Therefore we call our own execNoWait().
    return execNoWait( commandArray, envp );
  }


  /**********************************************************************
   * This method starts a new window with the specified command, but does
   * not wait for completion. It returns the process in case the caller
   * wishes to wait for completion of the process. This method can be used
   * to start a process that runs independently, such as build, workon, etc.
   * Note that workon when started this way will start a shell of its own
   * in the window. The workon window will terminate when the user types exit.
   *
   * It has been determined that while a workon can be done this way, this
   * is not valid for a -k or -c operand for workon on all platforms.
   * For that situation, runCommandInWorkonWindow() is recommended instead.
   *
   * @param command the command to execute
   * @param envp the environment variables array
   * @param title the title on the window; if null or blank, command is used.
   * @param simpleTitle the window title to use if there is a platform specific
   *        reason for not using title, such as longer than 60 chars on OS/2
   *        The simple title should be short, alphanumeric and without blanks.
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  public Process execWindow( String command,
                             String[] envp,
                             String title,
                             String simpleTitle )
                             throws IOException, InterruptedException
  {
    switch (shellType_)
    {
      case CMD_SHELL:
      case COMMAND_SHELL:
        return execCmdWindow( command, envp, title, simpleTitle );
//        return execCmdWindow( command, envp, title, simpleTitle, null );
      case SH_SHELL:
      case KSH_SHELL:
      default:
        return execUnixWindow( command, envp, title, simpleTitle );
//        return execUnixWindow( command, envp, title, simpleTitle, null );
    }
  }


//  /**********************************************************************
//   * This method starts a new window with the specified command, but does
//   * not wait for completion. It returns the process in case the caller
//   * wishes to wait for completion of the process. This method can be used
//   * to start a process that runs independently, such as edit, workon, etc.
//   * The subdir is a directory that the command will be run in.
//   * [It will be used in the Runtime.exec( cmd, envp, subdir ) call,
//   * if we ever go to Java 1.2. In that case, uncomment this routine and
//   * and change the two routines it calls.]
//   * Note that workon when started this way will start a shell of its own
//   * in the window. The workon window will terminate when the user types exit.
//   *
//   * It has been determined that while a workon can be done this way, this
//   * is not valid for a -k or -c operand for workon on all platforms.
//   * For that situation, runCommandInWorkonWindow() is recommended instead.
//   *
//   * @param command the command to execute
//   * @param envp the environment variables array
//   * @param title the title on the window; if null or blank, command is used.
//   * @param simpleTitle the window title to use if there is a platform specific
//   *        reason for not using title, such as longer than 60 chars on OS/2
//   *        The simple title should be short, alphanumeric and without blanks.
//   * @param subdir the directory where the command will be run.
//   *        The directory may be relative or absolute.
//   * @return the exit value
//   * @exception IOException thrown from Runtime.exec() for IO problem 
//   * @exception InterruptedException thrown when the process is interrupted 
//  **/
//  public Process execWindow( String command,
//                             String[] envp,
//                             String title,
//                             String simpleTitle,
//                             String subdir
//                           ) throws IOException, InterruptedException
//  {
//    switch (shellType_)
//    {
//      case CMD_SHELL:
//      case COMMAND_SHELL:
//        return execCmdWindow( command, envp, title, simpleTitle, subdir );
//      case SH_SHELL:
//      case KSH_SHELL:
//      default:
//        return execUnixWindow( command, envp, title, simpleTitle, subdir );
//    }
//  }


  /**********************************************************************
   * This command is specialized for a Windows-like shell command
   * and is called by execWindow().
   *
   * @param command the command to execute
   * @param envp the environment variables array
   * @param title the title on the window; if null or blank, command is used.
   * @param simpleTitle the window title to use if there is a platform specific
   *        reason for not using title, such as longer than 60 chars on OS/2
   *        The simple title should be short, alphanumeric and without blanks.
//   * @param subdir the directory where the command will be run.
//   *        The directory may be relative or absolute.
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  public Process execCmdWindow( String command,
                                String[] envp,
                                String title,
                                String simpleTitle )
                                throws IOException, InterruptedException
  {
    String newTitle = (title == null || title.equals( "" ) ?
                        command : title);
    if (PlatformConstants.X86_OS2_4_MACHINE.equals(
                          PlatformConstants.CURRENT_MACHINE ))
    {
      // we should actually check if newTitle is okay instead of just
      // blasting in the simpleTitle. And of course we are trusting 
      // ourselves to use a really simple title, and not checking it either!
      newTitle = simpleTitle; 
    }
    if (!newTitle.equals( "" ))
      newTitle = quoteOperand( newTitle ) + " ";
    String fullCmd = shell_ + " /C start " + newTitle + command;
    Interface.printDebug( "execCmdWindow about to run: " + fullCmd );
//    return Runtime.getRuntime().exec( fullCmd, envp, subdir );
    return Runtime.getRuntime().exec( fullCmd, envp );
  }


  /**********************************************************************
   * This command is specialized for a UNIX-like shell command
   * and is called by execWindow().
   *
   * Because the exec( String cmd, ... ) version is used, there is no way
   * I have found to quote the title. Therefore only the first word of the
   * title is used. If you really want to have a multiword title on a
   * command window for UNIX, consider how runCommandInWorkonWindow()
   * does it, and write a routine that works similarly.
   *
   * @param command the command to execute
   * @param envp the environment variables array
   * @param title the title on the window; if null or blank, command is used.
   * @param simpleTitle the window title to use if there is a platform specific
   *        reason for not using title, such as longer than 60 chars on OS/2
   *        The simple title should be short, alphanumeric and without blanks.
//   * @param subdir the directory where the command will be run.
//   *        The directory may be relative or absolute.
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  public Process execUnixWindow( String command,
                                 String[] envp,
                                 String title,
                                 String simpleTitle
//                                 , String subdir
                               ) throws IOException, InterruptedException
  {
    String newTitle = "";
    String[] titleWords = StringTools.split( title, ' ' );
    if (titleWords != null && titleWords.length > 0)
      newTitle =  "-T " +  titleWords[0]  + " ";
    String fullCmd;
    if (PlatformConstants.isAixMachine( PlatformConstants.CURRENT_MACHINE ))
      fullCmd = "xterm " + newTitle + "-e odeguirun " + command;
    else
      fullCmd = "xterm " + newTitle + "-e " + command;
    Interface.printDebug( "execUnixWindow about to run: " + fullCmd );
//    return Runtime.getRuntime().exec( fullCmd, envp, subdir );
    return Runtime.getRuntime().exec( fullCmd, envp );
  }


  /**********************************************************************
   * This method starts a new window with the specified command, but does
   * not wait for completion. It returns the process in case the caller
   * wishes to wait for completion of the process. This method can be used
   * to start a process that runs independently, such as edit, workon, etc.
   * The subdir is a directory that the command will be run in.
   * Note that workon when started this way will start a shell of its own
   * in the window. The workon window will terminate when the user types exit.
   *
   * @param command the command to execute
   * @param envp the environment variables array
   * @param title the title on the window; if null or blank, command is used.
   * @param simpleTitle the window title to use if there is a platform specific
   *        reason for not using title, such as longer than 60 chars on OS/2
   *        The simple title should be short, alphanumeric and without blanks.
   * @param rundir the directory where the command will be run.
   *        The directory may be relative or absolute.
   * @return the exit value
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
  **/
  public Process runCommandInWindow( 
                             String command,
                             String[] envp,
                             String title,
                             String simpleTitle,
                             String rundir
                           ) throws IOException, InterruptedException
  {
    boolean isTitle = (title != null && !title.trim().equals( "" ));
    boolean isSimpleTitle = (simpleTitle != null && 
                             !simpleTitle.trim().equals( "" ));
    boolean isRundir = (rundir != null && !rundir.trim().equals( "" ));
    boolean isCommand = (command != null && !command.trim().equals( "" ));
    StringBuffer sb = new StringBuffer( 200 ); // arbitrary initial size
    Vector v;
    String[] arrayCmd;

    switch (shellType_)
    {
      case CMD_SHELL:
      case COMMAND_SHELL:

//        // This could be made to work in 1.2 java, because the subdir
//        // bit could be passed off to exec(), letting the window be one
//        // that would go away when the editor ends.
//        v = new Vector( 15 ); // almost certainly big enough
//        v.addElement( shell_ );
//        v.addElement( shellOption_ );
//        v.addElement( "start" );
//        if (PlatformConstants.X86_OS2_4_MACHINE.equals(
//                              PlatformConstants.CURRENT_MACHINE ))
//        {
//          // We should actually check if simpleTitle is okay instead of just
//          // blasting in the simpleTitle. Of course we are trusting 
//          // ourselves to use a really simple title, and not checking it
//          // either!
//          v.addElement( quoteOperand( simpleTitle.trim() ) );
//        }
//        else if (isTitle)
//        {
//          v.addElement( quoteOperand( title.trim() ) );
//        }
//        else if (isSimpleTitle)
//        {
//          v.addElement( quoteOperand( simpleTitle.trim() ) );
//        }
//        else if (isCommand)
//        {
//          v.addElement( quoteOperand( command.trim() ) );
//        }
//        v.addElement( shell_ );
//        if (isCommand)
//        {
//          v.addElement( shellOption_ );
//          v.addElement( command );
//        }
//
//        arrayCmd = new String[v.size()];
//        v.copyInto( arrayCmd );
//        Interface.printDebug( "runCommandInWindow about to run: " +
//                              StringTools.join( arrayCmd, " " ) );
//        return Runtime.getRuntime().exec( arrayCmd,
//                                          envp,
//                                          (isRundir ? rundir : null )
//                                        );

        // For Windows/2000 and probably all Windows-like shells:
        // This is the only way I found to start a window, do a cd rundir
        // and run an editor in the window. Unfortunately after the editor
        // is terminated, the window hangs around. See the above code
        // for a way to make it work (not tested) if JAVA 1.2 can be used.
        // Then Runtime.exec( String[] cmd, String[] envp, String rundir)
        // would be available.
        v = new Vector( 15 ); // almost certainly big enough
        v.addElement( shell_ );
        if (isRundir || isCommand)
        {
          v.addElement( shellOption_ );
          if (isRundir)
          {
            sb.append( "cd " + rundir );
          }
          if (isRundir && isCommand)
            sb.append( " " + cmdsep_ + " " );
          if (isCommand)
          {
            sb.append( "start" );
            if (PlatformConstants.X86_OS2_4_MACHINE.equals(
                                  PlatformConstants.CURRENT_MACHINE ))
            {
              // We should actually check if simpleTitle is okay instead of
              // just blasting it in. Of course we are trusting 
              // ourselves to use a really simple title, and not checking it
              // either!
              sb.append( " " + quoteOperand( simpleTitle.trim() ) );
            }
            else if (isTitle)
              sb.append( " " + quoteOperand( title.trim() ) );
            else if (isSimpleTitle)
              sb.append( " " + quoteOperand( simpleTitle.trim() ) );
            else if (isCommand)
              sb.append( " " + quoteOperand( command.trim() ) );
            sb.append( " " + command );
          }
          v.addElement( sb.toString() );
        }

        arrayCmd = new String[v.size()];
        v.copyInto( arrayCmd );
        Interface.printDebug( "runCommandInWindow about to run: " +
                              StringTools.join( arrayCmd, " " ) );
        return Runtime.getRuntime().exec( arrayCmd, envp );

      case SH_SHELL:
      case KSH_SHELL:
      default:
        v = new Vector( 15 ); // almost certainly big enough
        v.addElement( "xterm" );
        if (isTitle)
        {
          v.addElement( "-T" );
          v.addElement( title );
        }
        else if (isSimpleTitle)
        {
          v.addElement( "-T" );
          v.addElement( simpleTitle );
        }
        else if (isCommand)
        {
          v.addElement( "-T" );
          v.addElement( command );
        }
        v.addElement( "-e" );
        v.addElement( shell_ );
        if (isRundir || isCommand)
        {
          v.addElement( shellOption_ );
          if (isRundir)
            sb.append( "cd " + rundir );
          if (isRundir && isCommand)
            sb.append( " " + cmdsep_ + " " );
          if (isCommand)
            sb.append( command );
          v.addElement( sb.toString() );
        }

        arrayCmd = new String[v.size()];
        v.copyInto( arrayCmd );
        Interface.printDebug( "runCommandInWindow about to run: " +
                              StringTools.join( arrayCmd, " " ) );
        return Runtime.getRuntime().exec( arrayCmd, envp );
    }
  }


  /**********************************************************************
   * This method starts a new command window, does a workon, and optionally
   * starts a commmand with a -k operand after optionally going to a
   * subdirectory. After the optional command is is complete, the user is
   * still in the workon, and may type exit to terminate the window.
   * The title of the command window may also be specified.
   *
   * @param title the title on the window; if null or blank, command is used.
   * @param simpleTitle the window title to use if there is a platform specific
   *        reason for not using title, such as longer than 60 chars on OS/2
   *        The simple title should be short, alphanumeric and without blanks.
   * @param workonOperands are flags to be passed to workon; can be null or
   *        blank, which means default sandbox will be used.
   *        Example: "-sb snark -rc myrc"
   * @param rundir is the directory to go to after the workon and before 
   *        running the command. It should be relative to the workon source
   *        directory. rundir may be null or blank.
   * @param command the command to execute; may be null or blank.
   * @param envp the environment variables array; may be null
   * @return the Process that was started.
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  public Process runCommandInWorkonWindow( String title,
                                           String simpleTitle,
                                           String workonOperands,
                                           String rundir,
                                           String command,
                                           String[] envp
                                         ) throws IOException,
                                                  InterruptedException
  {
    boolean isTitle = (title != null && !title.equals( "" ));
    boolean isRundir = (rundir != null && !rundir.trim().equals( "" ));
    boolean isCommand = (command != null && !command.trim().equals( "" ));
    StringBuffer sb = new StringBuffer( 200 ); // arbitrary initial size

    switch (shellType_)
    {
      case CMD_SHELL:
      case COMMAND_SHELL:
        sb.append( shell_ + " " + shellOption_ + " start" );
        if (PlatformConstants.X86_OS2_4_MACHINE.equals(
                              PlatformConstants.CURRENT_MACHINE ))
        {
          //@@@ we should actually check if simpleTitle is okay instead of just
          //@@@ blasting in the simpleTitle. And of course we are trusting 
          //@@@ ourselves to use a really simple title, and not checking it
          //@@@ either!
          sb.append( " " + quoteOperand( simpleTitle ) );
        }
        else if (isTitle)
          sb.append( " " + quoteOperand( title ) );
        else if (isCommand)
          sb.append( " " + quoteOperand( command ) );
        else
          sb.append( " " + quoteOperand( "workon " + workonOperands ) );
        sb.append( " workon " + workonOperands );
        if (isRundir || isCommand)
        {
          sb.append( " -k \"" ); // must pass on quotes around the rest
          if (isRundir)
            sb.append( "cd " + rundir );
          if (isRundir && isCommand)
            sb.append( " " + cmdsep_ + " " );
          if (isCommand)
            sb.append( command );
          sb.append( "\"" );
        }

        String strCmd = sb.toString();
        Interface.printDebug( "runCommandInWorkonWindow about to run: " +
                              strCmd );
        return Runtime.getRuntime().exec( strCmd, envp );

      case SH_SHELL:
      case KSH_SHELL:
      default:
        Vector v = new Vector( 15 ); // almost certainly big enough
        v.addElement( "xterm" );
        v.addElement( "-T" );
        if (isTitle)
          v.addElement( title );
        else if (isCommand)
          v.addElement( command );
        else
          v.addElement( "workon " + workonOperands );
        v.addElement( "-e" );
        if (PlatformConstants.isAixMachine( PlatformConstants.CURRENT_MACHINE ))
          v.addElement( "odeguirun" );
        v.addElement( "workon" );
        String[] tempOps = StringTools.split( workonOperands, ' ' );
        for (int i = 0; i < tempOps.length; ++i)
          v.addElement( tempOps[i] );
        if (isRundir || isCommand)
        {
          v.addElement( "-k" );
          if (PlatformConstants.isAixMachine(
              PlatformConstants.CURRENT_MACHINE ))
            sb.append( "\"" );
          if (isRundir)
            sb.append( "cd " + rundir );
          if (isRundir && isCommand)
            sb.append( " " + cmdsep_ + " " );
          if (isCommand)
            sb.append( command );
          if (PlatformConstants.isAixMachine(
              PlatformConstants.CURRENT_MACHINE ))
            sb.append( "\"" );
          v.addElement( sb.toString() );
        }
        String[] arrayCmd = new String[v.size()];
        v.copyInto( arrayCmd );
        Interface.printDebug( "runCommandInWorkonWindow about to run: " +
                              StringTools.join( arrayCmd, " " ) );
        return Runtime.getRuntime().exec( arrayCmd, envp );
    }
  }


  /**********************************************************************
   * This method provides a way of switching directories, either relative
   * to the current directory or absolute, followed by the running of a
   * command in a new Process. The command is intended to run independently,
   * unless the caller wishes to wait until the Process ends.
   * An example use is switching to a subdirectory of a sandbox and running
   * java com.ibm.ode.SandboxFiles with the envp variable containing an
   * appropriate value for BACKED_SANDBOXDIR.
   *
   * @param command the command to execute; may be null or blank.
   * @param rundir is the directory to go to before running the command.
   *        It should be relative to the sandbox source directory.
   *        rundir may be null or blank.
   * @param envp the environment variables array; may be null
   * @return the Process that was started.
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  public Process runCommandInDirectory( String command,
                                        String rundir,
                                        String[] envp
                                      ) throws IOException,
                                               InterruptedException
  {
    boolean isRundir = (rundir != null && !rundir.trim().equals( "" ));
    boolean isCommand = (command != null && !command.trim().equals( "" ));
    StringBuffer sb = new StringBuffer( 200 ); // arbitrary initial size
    Vector v;
    String[] tempOps;
    int i;
    int vix;
    String[] arrayCmd;

    switch (shellType_)
    {
      case CMD_SHELL:
      case COMMAND_SHELL:
      //@@@ WARNING!!! This code does not work on Win2000 to run the java
      //@@@ command. This is true even if no "cd rundir && " is prepended.
      //@@@ This suggests that it might not help to go to JAVA 1.2 where
      //@@@ the rundir can be a third argument to Runtime.exec().
      //@@@ On the other hand, maybe they fixed whatever problems we are
      //@@@ bumping into here. Or maybe not. See the comments for 
      //@@@ java.lang.Process in Java 1.3.1. They admit platform specific
      //@@@ weaknesses there.
      //@@@ Remove the outer layer of '//' to see what was being attempted
      //@@@ last.
////        sb.append( shell_ + " " + shellOption_ + " start" );
////        if (PlatformConstants.X86_OS2_4_MACHINE.equals(
////                              PlatformConstants.CURRENT_MACHINE ))
////        {
////          //@@@ we should actually check if simpleTitle is okay instead of just
////          //@@@ blasting in the simpleTitle. And of course we are trusting 
////          //@@@ ourselves to use a really simple title, and not checking it
////          //@@@ either!
////          sb.append( " " + quoteOperand( simpleTitle ) );
////        }
////        else if (isTitle)
////          sb.append( " " + quoteOperand( title ) );
////        else if (isCommand)
////          sb.append( " " + quoteOperand( command ) );
////        else
////          sb.append( " " + quoteOperand( "workon " + workonOperands ) );
//        sb.append( " workon " + workonOperands );
//        if (isRundir || isCommand)
//        {
//          sb.append( " -k \"" ); // must pass on quotes around the rest
//          sb.append( shell_ + " " + shellOption_ + " " );
//          if (isRundir)
//            sb.append( "cd " + rundir );
//          if (isRundir && isCommand)
//            sb.append( " " + cmdsep_ + " " );
//          if (isCommand)
//            sb.append( command );
//          sb.append( "\"" );
//        }
//
//        String strCmd = sb.toString();
//        Interface.printDebug( "runCommandInWorkon about to run: " +
//                              strCmd );
//        return Runtime.getRuntime().exec( strCmd, envp );
//        v = new Vector( 15 ); // almost certainly big enough
//        v.addElement( "xterm" );
//        v.addElement( "-T" );
//        if (isTitle)
//          v.addElement( title );
//        else if (isCommand)
//          v.addElement( command );
//        else
//          v.addElement( "workon " + workonOperands );
//        v.addElement( "-e" );
//        v.addElement( "workon" );
//@@@        v.addElement( "/ode/build/bldrel/latest/inst.images/hp9000_ux_10/bin/workon" );
//@@@        v.addElement( "d:\\ode30\\workon.exe" );
//        tempOps = StringTools.split( workonOperands, ' ' );
//        for (i = 0; i < tempOps.length; ++i)
//          v.addElement( tempOps[i] );
//        sb.append( shell_ ).append( " " ).
//                            append( shellOption_ ).append( " " );
//        v.addElement( shell_ );
//!!!        sb.append( shell_ );
        if (isRundir || isCommand)
        {
//          v.addElement( "-c" );
//          v.addElement( shellOption_ );
//!!!          sb.append( " " ).append( shellOption_ ).append( " " );
//!!!          sb.append( "\"" ); //yuck!!!
          if (isRundir)
//!!!            sb.append( "cd " + rundir );
          if (isRundir && isCommand)
//!!!            sb.append( " " + cmdsep_ + " " );
          if (isCommand)
          {
            sb.append( shell_ ).append( " ");
            sb.append( shellOption_ ).append( " " );
//!!!            sb.append( "\"" ); //yuck!!!
            sb.append( command );
            sb.append( " -info" ); //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//!!!            sb.append( "\"" ); //yuck!!!
          }
//!!!          sb.append( "\"" ); //yuck!!!
//          v.addElement( sb.toString() );
        }
        String stringCmd = sb.toString();

//        arrayCmd = new String[v.size()];
//        v.copyInto( arrayCmd );
//        Interface.printDebug( "runCommandInDirectory about to run: " +
//                              StringTools.join( arrayCmd, " " ) );
//        for (vix = 0; vix < arrayCmd.length; ++vix)     //@@@
//          Interface.printDebug( "@@@ arraycmd[" + vix + "]'" +
//                                arrayCmd[vix] + "'" ); //@@@
//        if (envp == null)
//          Interface.printDebug( "@@@ envp==null" );
//        else if (envp.length == 0)
//          Interface.printDebug( "@@@ envp.length==0" );
//        else
//          Interface.printDebug( "@@@ envp[0]=='" + envp[0] + "'" );
//        return Runtime.getRuntime().exec( arrayCmd, envp );

        Interface.printDebug( "runCommandInDirectory about to run: " +
                              stringCmd );
//!!!        return Runtime.getRuntime().exec( stringCmd, envp );
        arrayCmd = StringTools.split( stringCmd, " " );
        for (vix = 0; vix < arrayCmd.length; ++vix)     //@@@
          Interface.printDebug( "@@@ arraycmd[" + vix + "]'" +
                                arrayCmd[vix] + "'" ); //@@@
        return Runtime.getRuntime().exec( arrayCmd, envp );

      case SH_SHELL:
      case KSH_SHELL:
      default:
        v = new Vector();
        v.addElement( shell_ );
        if (isRundir || isCommand)
        {
          v.addElement( shellOption_ );
          if (isRundir)
            sb.append( "cd " + rundir );
          if (isRundir && isCommand)
            sb.append( " " + cmdsep_ + " " );
          if (isCommand)
          {
            sb.append( command );
          }
          v.addElement( sb.toString() );
        }

        arrayCmd = new String[v.size()];
        v.copyInto( arrayCmd );
        Interface.printDebug( "runCommandInDirectory about to run:" ); //@@@
        for (vix = 0; vix < arrayCmd.length; ++vix)                    //@@@
          Interface.printDebug( "arraycmd[" + vix + "]'" +             //@@@
                                arrayCmd[vix] + "'" );                 //@@@
//        if (envp == null)
//          Interface.printDebug( "@@@ envp==null" );
//        else if (envp.length == 0)
//          Interface.printDebug( "@@@ envp.length==0" );
//        else
//          Interface.printDebug( "@@@ envp[0]=='" + envp[0] + "'" );
        return Runtime.getRuntime().exec( arrayCmd, envp );
    }
  }


  /**
   * This routine adds double quotes before and after the string if it is
   * a windows-like system and does single quotes if it is a unix-like
   * system. No check is made for whether there are already such quotes.
   * It is recommended that something more intelligent be written for
   * cases where quoting can be tricky.
  **/
  public String quoteOperand( String cmd )
  {
    switch (shellType_)
    {
      case CMD_SHELL:
      case COMMAND_SHELL:
        return "\"" + cmd + "\"";
      case SH_SHELL:
      case KSH_SHELL:
      default:
        return "'" + cmd + "'";
    }
  }


  /**********************************************************************
   * The actual exec function taking a command and a env var arrays. This
   * method is recommended for command with arguments.  
   *
   * @param commandArray the command to execute.
   * @param envp the environment variables array. Could be null.
//   * @param dir the directory to execute the command in. Can be null or blank.
   * @return the Process that is started.
   * @exception IOException thrown from Runtime.exec() for IO problem 
   * @exception InterruptedException thrown when the process is interrupted 
   */
  public Process execNoWait( String[] commandArray,
                             String[] envp
//                             , String   dir
                           ) throws IOException, InterruptedException
  {
    Interface.printDebug( "@@@ execNoWait commandArray:" );
    for (int i = 0; i < commandArray.length; ++i)
      Interface.printDebug( "@@@ '" + commandArray[i] + "'" );
//    if (dir != null && !dir.equals( "" ))
//    {
//      Interface.printDebug( "@@@ execNoWait working directory: " + dir );
//      File workingDir = new File( dir );
//      return Runtime.getRuntime().exec( commandArray, envp, workingDir );
//    }
//    else
      return Runtime.getRuntime().exec( commandArray, envp );
  }

}
