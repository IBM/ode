package com.ibm.ode;

/**
 * Convenience class for running SandboxList (makes the command
 * line shorter and hides internal directory structure.
**/
public class SandboxList
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
