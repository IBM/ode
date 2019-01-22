package com.ibm.ode.lib.util;

import java.io.*;

/**
 * To make a subclass of ShellSystemCall, the AixShellSystemCall sets the
 * environment variable in case of ksh.
 */


public class AixShellSystemCall extends ShellSystemCall
{ 
  private String shell_ = "/bin/ksh";

  private String shellOption_ = "-c";
  
  public AixShellSystemCall()
  {
    super();
    //Setting the shell and the shell Options.
    setShell(shell_);
    setShellOption( shellOption_);
  }

  /**
   * Export env variables using 'export' command
   * and return the concatenated command.
   *
   * @param command Command to be run
   * @param envp Environment strings in the form of key=value
   * @return concatenated command
   * @author Chary Lingachary 9/9/98
   */
  private String getExportCommand( String command, String[] envp )
  {
     String finalCommand;
     String exportCommand = "";
     if( envp != null && envp.length > 0 )
     {
       StringBuffer sb = new StringBuffer();
       for(int i=0; i<envp.length; i++)
       {
         // make sure key=value pairs are
         // separated by a white space
         sb.append( envp[i] + " " );
       }

       // export variables using 'export' command
       // and attach user command 'command' after export
       exportCommand = "export " + sb.toString();
    }

    finalCommand = exportCommand + "; " + command;
    return finalCommand;
  }
}
