package com.ibm.ode.bin.sandboxfiles;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.util.*;
import com.ibm.ode.bin.gui.*;


/**
 * @@@ Replace this with specific code for documentation!
 * It would be nice to show the URLs for the current documentation.
 * It would be even nicer to somehow cause the 'current browser' to
 * run with the appropriate URL.
 * Or maybe we don't need this menu item at all?
 * If we show the URL, make the text be copy-and-paste enabled.
**/
class SFDocumentationCmd implements SFAction
{
  public void doAction( SFFrame frame )
  {
    String msg = "The latest information about ODE may be found at " +
                 "\nhttp://w3.ode.raleigh.ibm.com/"
                 ;
    GuiTextMsg textFrame = new GuiTextMsg();
    textFrame.initTextMsg( frame, msg, "Documentation", 3, 30 );
    textFrame.pack();
    textFrame.setVisible( true );
    Interface.printDebug( "'Documentation' window was set visible" );
  }
}
