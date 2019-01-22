package com.ibm.ode.bin.sandboxlist;

import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;


/**
 * These classes carry out various menu actions. They all implement
 * the same SLAction interface for selections of SLInfo objects.
 * They are passed the SLFrame so that selections can be gotten.
**/

public interface SLAction
{
  public void doAction( SLFrame frame );
}
