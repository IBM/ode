package com.ibm.ode.bin.sandboxfiles;

import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;


/**
 * refresh menu item
**/
class SFRefreshCmd implements SFAction
{
  public void doAction( SFFrame frame )
  {
    frame.refresh();
  }
}
