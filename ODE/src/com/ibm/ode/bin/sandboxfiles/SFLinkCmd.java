package com.ibm.ode.bin.sandboxfiles;

import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.bin.gui.*;


/**
 * This menu action displays a dialog indicating that an unimplemented
 * link files menu item has been chosen.
**/
class SFLinkCmd implements SFAction
{
  public void doAction( SFFrame frame )
  {
    JOptionPane.showMessageDialog( frame, 
                     "The link files menu item is not implemented yet!",
                                   "Unimplemented menu item",
                                   JOptionPane.WARNING_MESSAGE );
  }
}


