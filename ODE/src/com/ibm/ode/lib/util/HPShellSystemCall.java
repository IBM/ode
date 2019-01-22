package com.ibm.ode.lib.util;

/**
 * HPShellSystemCall, the subclass of ShellSystemCall 
 * can be used for HP specific system calls 
 */
public class HPShellSystemCall extends ShellSystemCall
{
   private String shell_ = "/bin/ksh";

   private String shellOption_ = "-c";

   public HPShellSystemCall()
   {
     super();
     //Setting the shell and the shell Options.
     setShell(shell_);
     setShellOption( shellOption_);
   }
}
