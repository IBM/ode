/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:44:44
 * Date and Time File was extracted/checked out: 06/04/13 16:47:00
 ****************************************************************************/
package com.ibm.ode.pkg.pkgMvs.service;

import java.io.*;
import com.ibm.ode.pkg.pkgMvs.MvsProperties;
import com.ibm.ode.lib.string.PlatformConstants;

/**
 *
 * @version 1.1
 * @see B390PTFDriver, B390APARDriver
 */
public class B390CommandHandler
{
  public static final String LEADING_SPACE = "    ";

  private boolean traceOnly = true;
  private boolean showB390Output = true;
  private static Runtime rt = Runtime.getRuntime();
  private String shell_;
  private String shellOption_;

  /**
   * Constructor
   */
  public B390CommandHandler()
  {
    this.traceOnly = MvsProperties.isB390TraceOnly();
    this.showB390Output = MvsProperties.isDisplayOutput();
    setShell();
  }

  /**
   *
   */
  private void setShell()
  {
    if (PlatformConstants.isMvsMachine(PlatformConstants.CURRENT_MACHINE))
    {
      shell_ = "/bin/sh";
      shellOption_ = "-c";
    }
    else if (PlatformConstants.isUnixMachine(PlatformConstants.CURRENT_MACHINE))
    {
      shell_ = "/bin/ksh";
      shellOption_ = "-c";
    }
    else if (PlatformConstants.isWindowsMachine(PlatformConstants.CURRENT_MACHINE))
    {
      shell_ = "cmd.exe";
      shellOption_ = "/C";
    }
    else // default
    {
      shell_ = "/bin/ksh";
      shellOption_ = "-c";
    }
  }

  /**
   *
   */
  public void handleReturnCode( int returnCode )
    throws B390CommandException
  {
    B390CommandException.handleReturnCode(returnCode);
  }

  /**
   *
   */
  public void handleNextOutputLine( String nextLine )
    throws B390CommandException
  {
    if (showB390Output)
    {
      System.out.println(nextLine);
    }
  }

  /**
   *
   */
  public void executeCommand( String command )
    throws B390CommandException, IOException, InterruptedException
  {
    String[] cmdarray = new String[3];
    System.out.println(LEADING_SPACE + command);
    if (!traceOnly)
    {
      cmdarray[0] = shell_;
      cmdarray[1] = shellOption_;
      cmdarray[2] = command;
      Process process = rt.exec(cmdarray);

      String nextLine;
      String nextErr;
      BufferedReader execInfoStream =
        new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader execErrorStream =
        new BufferedReader(new InputStreamReader(process.getErrorStream()));
      while ((nextLine = execInfoStream.readLine()) != null)
      {
        this.handleNextOutputLine(LEADING_SPACE + nextLine);
      }
      while ((nextErr = execErrorStream.readLine()) != null)
      {
        this.handleNextOutputLine(LEADING_SPACE + nextErr);
      }

      process.waitFor();
      this.handleReturnCode(process.exitValue());
    }
  }

  /**
   *
   */
  public void executeCommand( String command, boolean trace )
    throws B390CommandException, IOException, InterruptedException
  {
    this.traceOnly = trace;
    executeCommand(command);
  }
}
