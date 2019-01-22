package com.ibm.ode.bin.sandboxfiles;

import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;


/**
 * exit menu item
**/
class SFExitCmd implements SFAction
{
  public void doAction( SFFrame frame )
  {
    frame.dispose();
  }
}
