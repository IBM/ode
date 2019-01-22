package com.ibm.ode.bin.gui;

import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.awt.*;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.string.StringTools;
import com.ibm.ode.lib.util.*;

/**
 * This class provides a means of running an ODE (or other) command and
 * capturing its output from stdout and stderr. If ERROR: or WARNING:
 * messages or a non-zero return code are found, then the stdout and
 * stderr output can be displayed in a text window for the user.
**/
public class GuiCommand
{
  private String cmd;
  private Frame frame;

  /**
   * constructor
   *
   * @param cmd;   The command to execute in a shell.
   * @param frame; A Frame window which the error dialog is associated with.
   *
  **/
  public GuiCommand( String cmd, Frame frame )
  {
    this.frame = frame;
    this.cmd = cmd;
  }


  /**
   * Run a command in a shell. If there are errors or warnings,
   * optionally show the output of the command as a text dialog.
   * The command is assumed to not interact with the
   * user, typically being run with -auto if that is necessary.
  **/
  public int runCommand( boolean showOutputIfProblem, boolean modalOutput )
  {
    return runCommand( new StringBuffer(), new StringBuffer(),
                       showOutputIfProblem, modalOutput );
  }

  /**
   * Run a command in a shell. If there are errors or warnings,
   * optionally show the output of the command as a text dialog.
   * The standard out and standard err are set with the output of the command.
   * The command is assumed to not interact with the
   * user, typically being run with -auto if that is necessary.
  **/
  public int runCommand( StringBuffer out, StringBuffer err,
                         boolean showOutputIfProblem, boolean modalOutput )
  {
    int retcode = 0;
    boolean foundError = false;
    try
    {
      
      PlatformShellSystemCall shellCall = new PlatformShellSystemCall();
      //Interface.printDebug( "@@@ OdeGuiCommand running '" + cmd + "'" );
      retcode = shellCall.exec( cmd, null, out, err
                                , true // interleave
                                );
      if (showOutputIfProblem)
      {
        if (retcode != 0)
          foundError = true;
        else
        {
          // now we need to scan the err and out buffers, looking for
          // ends of line, and parse each line.
          // Create the line number reader
          LineNumberReader lineReader =
             new LineNumberReader( new StringReader( err.toString() + 
                                                     out.toString() ) );
          String line;
          
          // Loop until null, looking for warnings or error messages
          while ( ( line = lineReader.readLine() ) != null )
          {
            String tok1 = "";
            String tok2 = "";
            //Interface.printDebug( "@@@ cmd '" + cmd + "' output line: "
            //                      + line );
            StringTokenizer st = new StringTokenizer( line );
            if (st.hasMoreTokens())
              tok1 = st.nextToken();
            if (st.hasMoreTokens())
              tok2 = st.nextToken();
            //Interface.printDebug( "@@@ tok1=" + tok1 + " tok2=" + tok2 );
            if (tok1.equals( "" ))
              continue;
            if (tok1.charAt( 0 ) == '>' )
            { // should be warning or error message
              if (tok2.equals( "WARNING:" ) || tok2.equals( "ERROR:" ))
              {
                foundError = true;
                break;
              }
            }
          } // end while
        }
      }
    }
    catch (IOException e)
    {
      //Interface.printError( cmd + ": IOException: " + e.getMessage() );
      GuiTextMsg.showErrorMsg( frame,
                               cmd + ": " + e.getMessage(),
                               "ERROR: IOException" );
    }
    catch (InterruptedException e)
    {
      //Interface.printError( cmd + ": InterruptedException: " +
      //                      e.getMessage() );
      GuiTextMsg.showErrorMsg( frame,
                               cmd + ": " + e.getMessage(),
                               "ERROR: InterruptedException" );
    }
    if (foundError)
    {
      //Interface.printDebug( "@@@ Error output of " + cmd +
      //                      " (return code = " + retcode + ")" +
      //                      ":\n" + err.toString() +
      //                      "@@@ End of error output" );
      //Interface.printDebug( "@@@ Output of " + cmd + ":\n" +
      //                      out.toString() +
      //                      "@@@ End of output" );
      GuiTextMsg.showTextMsg( frame,
                              err.toString() + out.toString(),
                              "Problem running " + cmd,
                              8, 50, true );
    }
    return retcode;
  }

}
