package com.ibm.ode.bin.sandboxfiles;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JOptionPane;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.util.*;
import com.ibm.ode.bin.gui.*;


/**
 * This displays some text, at the very least. In the long run it should
 * not be a simple JOptionPane.showMessageDialog. JTextArea or JTextPane
 * should be used instead, allowing for word wrap and scrolling of the
 * help information. If the information is grouped in several catagories,
 * there could be a JTabbedPane to select which category.
 * Instead of the text being wired in, it should be loaded from a
 * property file or other NLS-approved resource.
**/
class SFHelpCmd implements SFAction
{
  public void doAction( SFFrame frame )
  {
    String msg =
       "Column meanings:\n" +
       "Base - base of the sandbox; blank if not in a " +
       "sandbox on the backing chain.\n" +
       "Path - path relative to the base; " +
       "full path if not in a sandbox.\n" +
       "Date - time stamp of file in sortable format.\n" +
       "Size - size in bytes.\n" +
       "Filename - last part of path, repeated to be separately sortable.\n" +
       "Suffix - suffix of filename, repeated to be separately sortable.\n" +
       "Type - file, directory, and possibly other on some systems.\n" +
       "Pos - position on backing chain; 0 = not on chain, 1 = sandbox, " +
       "2 = backing the sandbox, etc.\n" +
       "\n" +
       "Sorting on columns:\n" +
       "Click on column title for ascending order. " +
       "Shift-click for decending order. " + 
       "Use Refresh menu item to get original order.\n" +
       "\n" +
       "Selecting:\n" +
       "Use left click for a single item. " +
//       "Click, drag, release to select several adjacent items. " +
// The above is not true for java 1.1.8 although it works for 1.2.2 and above.
       "Control-click to select non-adjacent items.\n" +
       "\n" +
       "Keyboard access to menus:\n" +
       "The main menus are reached using Alt-f, Alt-s, etc. " +
       "Certain other menu options are also accessed with Alt. " +
       "Once a menu has focus, a key without Alt accesses choices."
       ;
    GuiTextMsg textFrame = new GuiTextMsg();
    textFrame.initTextMsg( frame, msg, "Help", 14, 40 );
    textFrame.pack();
    textFrame.setVisible( true );
    Interface.printDebug( "'Help' window was set visible" );
  }
}
