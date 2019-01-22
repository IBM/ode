package com.ibm.ode.bin.gui;

/**
 * Used when parsing output of ODE commands and a parsing or other error
 * was discovered.
 * Usually it will indicate a problem, but need not be used that way.
**/
public class GuiCommandOutputException extends Exception
{
  public GuiCommandOutputException( String e )
  {
    super( e );
  }
}
