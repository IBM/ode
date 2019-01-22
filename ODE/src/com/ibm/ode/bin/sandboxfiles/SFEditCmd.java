package com.ibm.ode.bin.sandboxfiles;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.ibm.ode.lib.io.*;
import com.ibm.ode.lib.util.*;
import com.ibm.ode.bin.gui.*;


/**
 * This is a dialog box that starts an edit command in a separate window.
**/
class SFEditCmd implements SFAction, ActionListener
{
  SFFrame mainFrame;
  JTextField subdirField; // subdirectory where to do the edit
  JTextField editorField; // editor and options
  JDialog dia;
  SandboxFiles main_class;

  public SFEditCmd( SandboxFiles main_class )
  {
    this.main_class = main_class;
  }

  /**
   * This is called when the menu item is clicked to start an edit.
   * It must look at what files are selected, if any.
   * It is assumed that multiple files may be edited; i.e. it is a
   * real programmer's editor that will be used.
  **/
  public void doAction( SFFrame frame )
  {
    mainFrame = frame;
    dia = new JDialog( mainFrame,
                       "edit",
                       true // modal
                       );
    // Add a bunch of things to the contentPane: cp.add( child );
    Container cp = dia.getContentPane();
    Point loc = mainFrame.getLocation();
    dia.setLocation( loc.x + 50, loc.y + 50 );
    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints con = new GridBagConstraints();
    cp.setLayout( lay );

    // Single field for the optional subdirectory of the sandbox in which 
    // to do the edit.
    //JPanel subdirPanel = getSubdirPanel();
    //con.gridx = 0;
    //con.gridy = 0;
    //con.anchor = GridBagConstraints.WEST;
    //con.fill = GridBagConstraints.HORIZONTAL;
    //lay.setConstraints( subdirPanel, con );
    //cp.add( subdirPanel );

    // field for editor and its options
    JPanel editorPanel = getEditorPanel();
    con.gridy = GridBagConstraints.RELATIVE;
    lay.setConstraints( editorPanel, con );
    cp.add( editorPanel );

    // We need some buttons
    JPanel buttonPanel = getButtonPanel();
    con.gridy = GridBagConstraints.RELATIVE;
    con.anchor = GridBagConstraints.EAST;
    lay.setConstraints( buttonPanel, con );
    cp.add( buttonPanel );

    dia.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
    // show the create dialog
    dia.pack();
    dia.setVisible( true );
  }


  /**
   * Make a JPanel for entering an optional subdirectory in which the
   * edit is done.
   * The JPanel will be a text box, preceded by some explanatory text.
  **/
  private JPanel getSubdirPanel()
  {
    JPanel pan = new JPanel();
    pan.setLayout( new BoxLayout( pan, BoxLayout.Y_AXIS ) );
    pan.setBorder( BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                              " subdirectory for the edit " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    pan.add( new JLabel(
                  "Enter an optional subdirectory to do the edit in," ) );
    pan.add( new JLabel(
                  "either relative to the current directory or absolute." ) );
//    // This alternative displays the current directory in the subdirField.
//    pan.add( new JLabel(
//                  "If omitted, the edit is done in the current directory," ) );
//    pan.add( new JLabel( "which is the initial value below." ) );
//    pan.add( new JLabel(
//                  "(The editor is given absolute paths to the files.)" ) );
//    subdirField = new JTextField( System.getProperty( "user.dir" ) );
    // This alternative does not initialize subdirField to the current dir.
    pan.add( new JLabel(
                  "If omitted, the edit is done in the current directory:" ) );
    pan.add( new JLabel( System.getProperty( "user.dir" ) ) );
    pan.add( new JLabel(
                  "(The editor is given absolute paths to the files.)" ) );
    subdirField = new JTextField();
    subdirField.addActionListener( this );
    pan.add( subdirField );
    return pan;
  }


  /**
   * Make a JPanel for entering editor and options.
   * @@@ Actually change this one into the field to enter the editor name etc.
   * It will be a text box, preceded by some explanatory text.
  **/
  private JPanel getEditorPanel()
  {
    JPanel pan = new JPanel();
    pan.setLayout( new BoxLayout( pan, BoxLayout.Y_AXIS ) );
    pan.setBorder( BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                              " editor name and options " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    pan.add( new JLabel( "Enter editor and its options." ) );
    editorField = new JTextField( main_class.defaultEditor );
    editorField.addActionListener( this );
    pan.add( editorField );
    return pan;
  }


  private JPanel getButtonPanel()
  {
    JButton cancelButton = new JButton( "Cancel" );
    JButton editButton = new JButton( "Start edit" );
    cancelButton.addActionListener(
                   new ActionListener()
                   {
                     public void actionPerformed( ActionEvent e )
                     {
                       dia.dispose();  // zap the window
                     }
                   });
    editButton.addActionListener(
                   new ActionListener()
                   {
                     public void actionPerformed( ActionEvent e )
                     {
                       if (startEdit())
                         dia.dispose();  // zap the window
                     }
                   });
    //Lay out the buttons from left to right.
    JPanel pan = new JPanel();
    pan.setLayout( new BoxLayout( pan, BoxLayout.X_AXIS ) );
    pan.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
    pan.add( Box.createHorizontalGlue() );
    pan.add( cancelButton );
    pan.add( Box.createRigidArea( new Dimension( 10, 0 ) ) );
    pan.add( editButton );
    return pan;
  }


  /**
   * Start the edit command.
  **/
  boolean startEdit()
  {
    String editor = editorField.getText().trim();
    if (editor.equals( "" ))
    {
      JOptionPane.showMessageDialog( dia, 
                                     "No editor was specified!",
                                     "edit files",
                                     JOptionPane.ERROR_MESSAGE );
      return false;
    }
    main_class.defaultEditor = editor;
    StringBuffer filesSb = new StringBuffer( "" ); // put files here
    // get selected files here

    SFInfo[] sel = mainFrame.getFileSelections();
    for (int si = 0; si < sel.length; ++si)
    {
      if (!sel[si].getType().equals( SFInfo.TYPE_FILE ))
      {
        JOptionPane.showMessageDialog( dia, 
                                      sel[si].getFullPath() +
                                      " isn't a file (ignoring)!",
                                      "edit files",
                                      JOptionPane.ERROR_MESSAGE );
      }
      else
        filesSb.append( " " ).append( sel[si].getFullPath() );
    }

    if (filesSb.length() < 1) // nothing to edit, just return
      return true;

    String editCmd = editor + filesSb.toString();
    try
    {
      PlatformShellSystemCall caller = new PlatformShellSystemCall();
      caller.runCommandInWindow( 
                         editCmd,  // edit command
                         null,     // no environment vars, they are inherited
                         editCmd,  // use edit command as title
                         "edit",   // Keep title simple for OS/2 and other
                                   // title-challenged platforms
                         ""
                         );
    }
    catch (IOException e)
    {
      GuiTextMsg.showErrorMsg( mainFrame,
                               "runCommandInWindow: " + e.getMessage(),
                               "ERROR: IOException" );
      return false;
    }
    catch (InterruptedException e)
    {
      GuiTextMsg.showErrorMsg( mainFrame,
                               "runCommandInWindow: " + e.getMessage(),
                               "ERROR: InterruptedException" );
      return false;
    }
    return true;
  }


  public void actionPerformed( ActionEvent e )
  {
    //String s = e.getActionCommand();
    //if (s == nameCommand)
    //{
    //  // get the current value for checking.
    //  // If non-blank, check for nasty not-allowed characters.
    //  // This provides a way for the user to get checking done, if we
    //  // choose to not use Enter key to end the dialog.
    //  // At least check for blanks in the non-blank text.
    //  String name = nameField.getText();
    //}
    //else if (s == machineCommand)
    //{
    //  // get the current value for checking
    //  // At least check for blanks in the non-blank text.
    //  String name = machineField.getText();
    //}
    //@@@ printCurrentValues();    // @@@ for debugging
    // Question: Do we want to have Enter key cause the dialog to end?
    // If focus is on a text field, this is the place to catch it.
    // We should call a common routine that checks validity of all items
    // and then either complains (e.g. missing sandbox name) or runs
    // the mkbb/mksb command.
  }


  /**
   * The value of String s is trimmed. If it equals "" then false is returned.
   * If it does not equal "" then it has at least one non-blank character.
   * If there are any blanks in the remainder, it is considered
   * an error and a dialog box is popped up. After dismissal of the box,
   * true is returned. If there were no blanks then false is returned.
  **/
  boolean hasBlanks( String s, String field )
  {
    if (s.trim().equals( "" ) || s.indexOf( ' ' ) == -1)
      return false;
    else
    {
      // pop up a dialog
      JOptionPane.showMessageDialog( dia, 
                         "Blanks are not allowed in the " + field + ".",
                                     "ERROR: blank found",
                                     JOptionPane.ERROR_MESSAGE );
      return true;
    }
  }


}
