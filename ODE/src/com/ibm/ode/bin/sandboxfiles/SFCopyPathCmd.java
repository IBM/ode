package com.ibm.ode.bin.sandboxfiles;

import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.bin.gui.*;


/**
 * This menu action displays a dialog indicating that an unimplemented
 * copy path menu item has been chosen.
**/
class SFCopyPathCmd implements SFAction
{
  public void doAction( SFFrame frame )
  {
    JOptionPane.showMessageDialog( frame, 
                     "The copy path menu item is not implemented yet!",
                                   "Unimplemented menu item",
                                   JOptionPane.WARNING_MESSAGE );
  }
}


