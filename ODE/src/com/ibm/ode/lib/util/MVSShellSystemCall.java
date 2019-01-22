package com.ibm.ode.lib.util;

/**
 * MVSShellSystemCall, the subclass of ShellSystemCall 
 * can be used for MVS specific system calls 
 */
public class MVSShellSystemCall extends ShellSystemCall
{
   private String shell_ = "/bin/sh";

   private String shellOption_ = "-c";

   public MVSShellSystemCall()
   {
     super();
     //Setting the shell and the shell Options.
     setShell(shell_);
     setShellOption( shellOption_);
   }
}
