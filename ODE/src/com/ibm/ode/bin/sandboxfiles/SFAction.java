package com.ibm.ode.bin.sandboxfiles;

import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;


/**
 * These classes carry out various menu actions. They all implement
 * the same SFAction interface for selections of SFInfo objects.
 * They are passed the SFFrame so that selections can be gotten.
**/

public interface SFAction
{
  public void doAction( SFFrame frame );
}
