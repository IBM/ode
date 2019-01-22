package com.ibm.ode;

/**
 * Convenience class for SandboxFiles (makes the command
 * line shorter and hides internal directory structure).
**/
public class SandboxFiles
{
  /**
   * Program entry point.
   *
   * @param args The arguments passed to the SandboxFiles command.
  **/
  public static void main( String[] args )
  {
    com.ibm.ode.bin.sandboxfiles.SandboxFiles.main( args );
  }
}
