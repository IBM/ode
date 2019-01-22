package com.ibm.ode.bin.sandboxlist;

import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;


/**
 * exit menu item
**/
class SLExitCmd implements SLAction
{
  public void doAction( SLFrame frame )
  {
    SandboxList.exit( 0 );
  }
}
