package com.ibm.ode;

/**
 * Convenience class for running SandboxList (makes the command
 * line shorter, hides internal directory structure, and gives
 * an easy way to provide a nifty name for the GUI).
**/
public class gui
{
  /**
   * Program entry point.
   *
   * @param args The arguments passed to the SandboxList command.
  **/
  public static void main( String[] args )
  {
    com.ibm.ode.bin.sandboxlist.SandboxList.main( args );
  }
}
