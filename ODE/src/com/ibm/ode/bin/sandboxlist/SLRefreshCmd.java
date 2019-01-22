package com.ibm.ode.bin.sandboxlist;

import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;


/**
 * refresh menu item
**/
class SLRefreshCmd implements SLAction
{
  public void doAction( SLFrame frame )
  {
    frame.refresh();
  }
}
