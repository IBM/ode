package com.ibm.ode;

/**
 * Convenience class for MakeMake (makes the command
 * line shorter and hides internal directory structure).
**/
public class MakeMake
{
  /**
   * Program entry point.
   *
   * @param args The arguments passed to the MakeMake command.
  **/
  public static void main( String[] args )
  {
    com.ibm.ode.bin.makemake.MakeMake.main( args );
  }
}
