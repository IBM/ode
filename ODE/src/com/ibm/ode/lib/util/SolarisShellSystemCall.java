package com.ibm.ode.lib.util;

/**
 * SolarisShellSystemCall, the subclass of ShellSystemCall 
 * can be used for Solaris specific system calls 
 */
public class SolarisShellSystemCall extends ShellSystemCall
{
   private String shell_ = "/bin/ksh";

   private String shellOption_ = "-c";

   public SolarisShellSystemCall()
   {
     super();
     //Setting the shell and the shell Options.
     setShell(shell_);
     setShellOption( shellOption_);
   }
}
