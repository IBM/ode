package com.ibm.ode.bin.sandboxfiles;

import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.bin.gui.*;


/**
 * This menu action displays a dialog indicating that an unimplemented
 * menu item has been chosen.
**/
class SFNewCmd implements SFAction
{
  public void doAction( SFFrame frame )
  {
    JOptionPane.showMessageDialog( frame, 
                        "The 'New...' menu item is not implemented yet!",
                                   "Unimplemented menu item",
                                   JOptionPane.WARNING_MESSAGE );
  }
}


