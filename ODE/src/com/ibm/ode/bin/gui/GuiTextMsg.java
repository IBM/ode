package com.ibm.ode.bin.gui;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.*;

import java.io.IOException;

/**
 * This class is used to output arbitrary text in a scrollable window.
 * Typical uses are for displaying help text or error messages collected from
 * running a command. The text normally is scrollable, line wraps, and
 * can be copied for pasting elsewhere, but is not editable.
**/
public class GuiTextMsg extends JDialog
{

  /**
   * A dialog with no owner and non-modal. 
   * It is useful for documentation and maybe help windows that should
   * remain visible even if the main application windows are minimized.
  **/
  public GuiTextMsg()
  {
    super();
  }

  /**
   * This is useful for modal and non-modal windows that should be hidden
   * if the owner is minimized.
  **/
  public GuiTextMsg( Frame owner, boolean modal )
  {
    super( owner, modal );
  }

  public void initTextMsg( Frame frame, String msg, String title,
                             int rows, int cols )
  {
    setTitle( title );
    Point loc = new Point( 0, 0 );
    if (frame != null)
      loc = frame.getLocation();
    setLocation( loc.x + 50, loc.y + 50 );

    JTextArea msgArea = new JTextArea( msg, rows, cols );
    msgArea.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    msgArea.setFont( new Font( "Serif", Font.BOLD, 12 ) );
    msgArea.setLineWrap( true );
    msgArea.setWrapStyleWord( true );
    msgArea.setEditable( false );
    JScrollPane msgScrollPane = new JScrollPane( msgArea );
    setDefaultCloseOperation( DISPOSE_ON_CLOSE );
    getContentPane().add( msgScrollPane, BorderLayout.CENTER );
  }

  /**
   * This is a static convenience function for showing a message in a text box.
  **/
  static public void showTextMsg( Frame frame, String msg, String title,
                             int rows, int cols, boolean modal )
  {
    GuiTextMsg textFrame = new GuiTextMsg( frame, modal );
    textFrame.initTextMsg( frame, msg, title, rows, cols );
    textFrame.pack();
    textFrame.setVisible( true );
  }

  /**
   * This is a static convenience function for showing an message in a text
   * box, roughly equivalent to Interface.printError() in intent.
   * It is assumed to be one possibly somewhat long line, and is displayed
   * as a modal text box.
   * One could argue that a JDialog should be used, except that its text
   * cannot be copied and pasted! This is bad if the message is something
   * you want to copy to an email.
  **/
  static public void showErrorMsg( Frame frame, String msg, String title )
  {
    GuiTextMsg textFrame = new GuiTextMsg( frame, true );
    textFrame.initTextMsg( frame, msg, title, 1, 40 );
    textFrame.pack();
    textFrame.setVisible( true );
  }

}
