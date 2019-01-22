package com.ibm.ode.bin.sandboxlist;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.util.*;
import com.ibm.ode.bin.gui.*;


/**
 * This is a dialog box that starts a window with workon.
**/
class SLWorkonCmd implements SLAction, ActionListener
{
  String quietString = "-quiet";
  String normalString = "-normal";   // default
  String verboseString = "-verbose";
  String debugString = "-debug";

  SLFrame mainFrame;
  String sandboxName;
  JTextField machineField; // -m value
  String messageLevel; // radio button value
  JDialog dia;


  /**
   * Runs a dialog and after it is dismissed, the input data is checked.
   * If the data suffices for starting a workon, return true.
  **/
  private void doWorkonDialog()
  {
    dia = new JDialog( mainFrame,
                       "workon",
                       true // modal
                       );
    // Add a bunch of things to the contentPane: cp.add( child );
    Container cp = dia.getContentPane();
    Point loc = mainFrame.getLocation();
    dia.setLocation( loc.x + 50, loc.y + 50 );
    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints con = new GridBagConstraints();
    cp.setLayout( lay );

    // Single field for machine/platform. The only current 
    // validity checking is for blanks, after trimming.
    // NOTE FOR THE FUTURE: It would be prefered
    // to have some method of getting the valid machines.
    // To get a list of valid machine names run the following,
    // and parse the output shown:
    // build -sb sandboxname -m snark -info
    // >> ERROR: build: Invalid machine name...
    // Valid machine names: x86_nt_4:x86_os2_4:rios_aix_4:hp9000_ux_10
    JPanel machinePanel = getMachineListPanel();
    con.gridx = 0;
    con.gridy = 0;
    con.anchor = GridBagConstraints.WEST;
    con.fill = GridBagConstraints.HORIZONTAL;
    lay.setConstraints( machinePanel, con );
    cp.add( machinePanel );

    // radio buttons for message level
    JPanel messageLevelPanel = getMessageLevelPanel();
    con.gridy = GridBagConstraints.RELATIVE;
    lay.setConstraints( messageLevelPanel, con );
    cp.add( messageLevelPanel );

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
   * Make a JPanel for entering a -m value.
   * It will be a text box, followed by some explanatory text.
  **/
  private JPanel getMachineListPanel()
  {
    JPanel pan = new JPanel();
    pan.setLayout( new BoxLayout( pan, BoxLayout.Y_AXIS ) );
    pan.setBorder( BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder( " -m machine " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    pan.add( new JLabel( "Enter machine type (optional)." ) );
    machineField = new JTextField();
    machineField.addActionListener( this );
    pan.add( machineField );
    return pan;
  }


  /**
   * Make a JPanel for radio buttons for the message level (-quiet, -normal,
   * -verbose, or -debug).
  **/
  private JPanel getMessageLevelPanel()
  {
    JPanel pan = new JPanel();
    pan.setLayout( new BoxLayout( pan, BoxLayout.Y_AXIS ) );
    pan.setBorder( BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                    " -quiet, -normal, -verbose, -debug " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    JLabel lab1 = new JLabel( "Choose the message level" );
    pan.add( lab1 );
    
    JRadioButton quietButton = new JRadioButton( quietString );
    quietButton.setMnemonic( KeyEvent.VK_Q );
    quietButton.setActionCommand( quietString );
    pan.add( quietButton );

    JRadioButton normalButton = new JRadioButton( normalString );
    normalButton.setMnemonic( KeyEvent.VK_N );
    normalButton.setActionCommand( normalString );
    normalButton.setSelected(true);         // default selection
    pan.add( normalButton );

    JRadioButton verboseButton = new JRadioButton( verboseString );
    verboseButton.setMnemonic( KeyEvent.VK_V );
    verboseButton.setActionCommand( verboseString );
    pan.add( verboseButton );

    JRadioButton debugButton = new JRadioButton( debugString );
    debugButton.setMnemonic( KeyEvent.VK_D );
    debugButton.setActionCommand( debugString );
    pan.add( debugButton );

    ButtonGroup group = new ButtonGroup();
    group.add( quietButton );
    group.add( normalButton );
    group.add( verboseButton );
    group.add( debugButton );

    RadioListener buttonListener = new RadioListener();
    quietButton.addActionListener( buttonListener );
    normalButton.addActionListener( buttonListener );
    verboseButton.addActionListener( buttonListener );
    debugButton.addActionListener( buttonListener );

    return pan;
  }

  class RadioListener implements ActionListener
  { 
    public void actionPerformed(ActionEvent e)
    {
      messageLevel = e.getActionCommand() ;
    }
  }


  private JPanel getButtonPanel()
  {
    JButton cancelButton = new JButton( "Cancel" );
    JButton workonButton = new JButton( "Start workon" );
    cancelButton.addActionListener(
                   new ActionListener()
                   {
                     public void actionPerformed( ActionEvent e )
                     {
                       dia.dispose();  // zap the window
                     }
                   });
    workonButton.addActionListener(
                   new ActionListener()
                   {
                     public void actionPerformed( ActionEvent e )
                     {
                       if (startWorkon())
                       {
                         // it succeeded so dispose of the window
                         dia.dispose();
                       }
                       else
                       {
                         // Need to bring dialog window to the top.
                         // For some reason it is left under the main window!
                         dia.show();
                       }
                     }
                   });
    //Lay out the buttons from left to right.
    JPanel pan = new JPanel();
    pan.setLayout( new BoxLayout( pan, BoxLayout.X_AXIS ) );
    pan.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
    pan.add( Box.createHorizontalGlue() );
    pan.add( cancelButton );
    pan.add( Box.createRigidArea( new Dimension( 10, 0 ) ) );
    pan.add( workonButton );
    return pan;
  }


  /**
   * This is called when the menu item is clicked to start a workon.
   * It must look at whether any sandboxes are selected. Only one may
   * be selected. A dialog is popped up for the user to fill in.
  **/
  public void doAction( SLFrame frame )
  {
    mainFrame = frame;
    SLInfo[] sel = frame.getSandboxSelections();
    if (sel.length != 1)
    {
      JOptionPane.showMessageDialog( frame, 
                           "You must select exactly one sandbox for workon!",
                                     "Workon sandbox",
                                     JOptionPane.WARNING_MESSAGE );
    }
    else
    {
      sandboxName = sel[0].getSandboxName();
      messageLevel = normalString;
      doWorkonDialog();
    }
  }


  /**
   * Start the workon command.
  **/
  boolean startWorkon()
  {
    String machine = machineField.getText().trim();
    if (hasBlanks( machine, "-m value" ))
      return false;

    String cmd = "workon " +
                 SandboxList.list_state.getRcFileFlag() +
                 " -sb " + sandboxName + " " + messageLevel +
                 (machine.equals( "" ) ? "" : (" -m " + machine))
                 ;
    String infoCmd = cmd + " -info";
    try
    {
      // Run cmd with -info first, to catch error messages and display them.
      // Okay, so there is some extra stuff saying "would run" because of
      // -info, but at least the user sees the problems, and only if there
      // are any. Note that if it is only warnings
      // the start is done because retcode == 0 in that case.
      int retcode;
      GuiCommand infoCmdRunner = new GuiCommand( infoCmd, mainFrame );
      // run it with false at first, so no message box.
      if ((retcode = infoCmdRunner.runCommand( false, true )) != 0)
      {
        Interface.printDebug( "workon dialog: cmd='" + infoCmd + "' retcode=" +
                              retcode );
        // Only in error situations do we run it again so the messages are
        // shown in a popup. In the other case, the workon will run and the
        // messages will be seen in context, unless -quiet is done.
        infoCmdRunner.runCommand( true, true );
        return false;
      }
      Interface.printDebug( "About to start in separate window: " + cmd );
      PlatformShellSystemCall caller = new PlatformShellSystemCall();
      caller.execWindow( cmd, null, null, "workon" );
    }
    catch (IOException e)
    {
      //Interface.printError( cmd + ": IOException: " + e.getMessage() );
      GuiTextMsg.showErrorMsg( SandboxList.frame,
                                cmd + ": " + e.getMessage(),
                                "ERROR: IOException" );
      return false;
    }
    catch (InterruptedException e)
    {
      //Interface.printError( cmd + ": InterruptedException: " +
      //                      e.getMessage() );
      GuiTextMsg.showErrorMsg( SandboxList.frame,
                                cmd + ": " + e.getMessage(),
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
