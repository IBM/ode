package com.ibm.ode.bin.sandboxlist;


import java.awt.*;
import java.awt.event.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import com.ibm.ode.lib.io.Interface;

/**
 *
**/
public class SLMenuItem extends JMenuItem
{
  private SLAction cmd;
  private SLFrame frame;


  /**
   *
  **/
  public SLMenuItem( SLFrame slf, SLAction cmd,
                     boolean disable, String label,
                     int mnemonic )
  {
    super( label, mnemonic );
    this.cmd = cmd;
    if (disable) // the menu item is enabled by default
      this.setEnabled( false );
    addActionListener( slf );
    frame = slf; // remember the frame for the menu action later
  }


  /**
   *
  **/
  public SLMenuItem( SLFrame slf, SLAction cmd,
                     boolean disable, String label,
                     int mnemonic, int keymask )
  {
    this( slf, cmd, disable, label, mnemonic );
    super.setAccelerator( KeyStroke.getKeyStroke( mnemonic, keymask ) );
  }


  /**
   * This is called from the ActionEvent listener that looks for the menu
   * actions.
   * Get the selection(s) and call the command or dialog func.
  **/
  public void sblMenuAction()
  {
    // Pass the action routine the frame, so it can get selection or
    // other information.
    Interface.printDebug( "Action event detected. Menu item: " +
                          this.getText() );
    cmd.doAction( frame );
  }

}
