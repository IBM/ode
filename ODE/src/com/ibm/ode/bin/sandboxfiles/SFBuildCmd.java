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
 * This is a dialog box that starts a build command in a separate window.
**/
class SFBuildCmd implements SFAction, ActionListener, ItemListener
{
  String quietString = "-quiet";
  String normalString = "-normal";   // default
  String verboseString = "-verbose";
  String debugString = "-debug";

  SFFrame mainFrame;
  SFInfo sandboxInfo;
  JTextField subdirField;  // subdirectory where to do the build
  JTextField machineField; // -m value
  JTextField optionsField; // options and targets passed to mk
  String messageLevel; // radio button value
  boolean ignoreChecked;
  JDialog dia;


  /**
   * Runs a dialog and after it is dismissed, the input data is checked.
   * If the data suffices for starting a build, return true.
  **/
  private void doBuildDialog()
  {
    dia = new JDialog();
    // Add a bunch of things to the contentPane: cp.add( child );
    Container cp = dia.getContentPane();
    Point loc = mainFrame.getLocation();
    dia.setLocation( loc.x + 50, loc.y + 50 );
    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints con = new GridBagConstraints();
    cp.setLayout( lay );

    // Single field for the optional subdirectory of the sandbox in which 
    // to build.
    JPanel subdirPanel = getSubdirPanel();
    con.gridx = 0;
    con.gridy = 0;
    con.anchor = GridBagConstraints.WEST;
    con.fill = GridBagConstraints.HORIZONTAL;
    lay.setConstraints( subdirPanel, con );
    cp.add( subdirPanel );

    // Single field for machine/platform to build for. The only current 
    // validity checking is for blanks, after trimming.
    // NOTE FOR THE FUTURE: It would be prefered
    // to have some method of getting the valid machines.
    // To get a list of valid machine names run the following,
    // and parse the output similar to the example shown:
    // build -sb sandboxname -m snark -info
    // >> ERROR: build: Invalid machine name...
    // Valid machine names: x86_nt_4:x86_os2_4:rios_aix_4:hp9000_ux_10
    JPanel machinePanel = getMachineListPanel();
    con.gridy = GridBagConstraints.RELATIVE;
    lay.setConstraints( machinePanel, con );
    cp.add( machinePanel );

    // For user convenience, put a set of four radio buttons here, to select
    // among four 'file-erasing targets': 1. no file erase target (default),
    // 2. clean_all, 3. rmtarget_all, 4. clobber_all. Whichever is selected
    // will be included in the build command before the remaining targets
    // and options.
    // NOT IMPLEMENTED YET.

    // For user convenience, put several check boxes with associated targets.
    // If checked, the targets will be included in the order shown.
    // (Therefore pick an order that makes reasonable sense if more than
    // one item is checked!). Obviously many of the standard targets can
    // be included here: depend_all export_all build_all etc.
    // The target(s) chosen here will appear before the options below
    // on the build command line.
    // NOT IMPLEMENTED YET.

    // field for mk options and targets
    JPanel optionsPanel = getOptionsPanel();
    lay.setConstraints( optionsPanel, con );
    cp.add( optionsPanel );

    // -ignore flag check box
    JPanel ignoreCheckPanel = getIgnoreCheckPanel();
    lay.setConstraints( ignoreCheckPanel, con );
    cp.add( ignoreCheckPanel );

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
   * Make a JPanel for entering an optional subdirectory in which the
   * buld is done. It will be concatenated with the base of the sandbox,
   * so it should be a relative path, if we do not let build itself do
   * the checking.
   * The JPanel will be a text box, preceded by some explanatory text.
  **/
  private JPanel getSubdirPanel()
  {
    JPanel pan = new JPanel();
    pan.setLayout( new BoxLayout( pan, BoxLayout.Y_AXIS ) );
    pan.setBorder( BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                              " subdirectory for build " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    pan.add( new JLabel(
                  "Enter the optional subdirectory to do the build in." ) );
    pan.add( new JLabel(
                  "It should be relative to the src directory." ) );
    pan.add( new JLabel(
                  "If omitted the build is in the src directory." ) );
    subdirField = new JTextField();
    subdirField.addActionListener( this );
    pan.add( subdirField );
    return pan;
  }


  /**
   * Make a JPanel for entering a -m value.
   * It will be a text box, preceded by some explanatory text.
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
   * Make a JPanel for entering mk options and targets.
   * It will be a text box, preceded by some explanatory text.
  **/
  private JPanel getOptionsPanel()
  {
    JPanel pan = new JPanel();
    pan.setLayout( new BoxLayout( pan, BoxLayout.Y_AXIS ) );
    pan.setBorder( BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                              " mk options and targets " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    pan.add( new JLabel( "Enter options and targets to pass to mk." ) );
    optionsField = new JTextField();
    optionsField.addActionListener( this );
    pan.add( optionsField );
    return pan;
  }


  /**
   * Make a JPanel for entering an -ignore flag.
   * It will be a check box, followed by some explanatory text.
  **/
  private JPanel getIgnoreCheckPanel()
  {
    JCheckBox checkIgnore;
    ignoreChecked = false;
    JPanel pan = new JPanel();
    pan.setBorder( BorderFactory.createCompoundBorder(
                       BorderFactory.createTitledBorder(
                           " -ignore " ),
                           BorderFactory.createEmptyBorder( 0,5,5,5 ) ) );
    checkIgnore = new JCheckBox( 
                           "If checked, build is passed the -ignore flag." );
    checkIgnore.addItemListener( this );
    pan.add( checkIgnore );
    pan.setAlignmentX( Component.RIGHT_ALIGNMENT );
    return pan;
  }


  public void itemStateChanged( ItemEvent e )
  {
    if (e.getStateChange() == ItemEvent.DESELECTED)
      ignoreChecked = false;
    else if (e.getStateChange() == ItemEvent.SELECTED)
      ignoreChecked = true;
    //@@@ printCurrentValues();  //@@@ for debugging
  }


  /**
   * Make a JPanel for radio buttons for the message level (-quiet, -normal,
   * -verbose, or -debug).
  **/
  private JPanel getMessageLevelPanel()
  {
    messageLevel = normalString; // initialize to default value
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
    JButton buildButton = new JButton( "Start build" );
    cancelButton.addActionListener(
                   new ActionListener()
                   {
                     public void actionPerformed( ActionEvent e )
                     {
                       dia.dispose();  // zap the window
                     }
                   });
    buildButton.addActionListener(
                   new ActionListener()
                   {
                     public void actionPerformed( ActionEvent e )
                     {
                       if (startBuild())
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
    pan.add( buildButton );
    return pan;
  }


  /**
   * This is called when the menu item is clicked to start a build.
   * It must look at whether any files|directories are selected. Only one may
   * be selected. A dialog is popped up for the user to fill in.
  **/
  public void doAction( SFFrame frame )
  {
    mainFrame = frame;
    SFInfo[] sel = frame.getFileSelections();
    if (sel.length != 1)
    {
      JOptionPane.showMessageDialog( frame, 
                    "You must select exactly one file|directory for a build!",
                                     "Build file",
                                     JOptionPane.WARNING_MESSAGE );
    }
    else
    {
      sandboxInfo = sel[0];
      doBuildDialog();
    }
  }


  /**
   * Start the build command.
  **/
  boolean startBuild()
  {
//    String subdir = subdirField.getText().trim();
//    if (hasBlanks( subdir, "subdirectory for build" ))
//      return false;
//    // WARNING! We really should discover what the user uses instead of
//    // wiring in "src" below. It IS controllable.
//    String buildDir = sandboxInfo.getBase() + File.separator +
//                      sandboxInfo.getSandboxName() + File.separator + "src";
//    if (!subdir.equals( "" ))
//      buildDir += File.separator + subdir;
//    buildDir = Path.canonicalize( new File( buildDir ) );
//    if (!Path.exists( buildDir ) || !new File( buildDir ).isDirectory())
//    {
//      JOptionPane.showMessageDialog( mainFrame, 
//                                     "Build path '" + buildDir +
//                                         "'\ndoes not exist in the sandbox " +
//                                         "or is not a directory!",
//                                     "Build sandbox",
//                                     JOptionPane.WARNING_MESSAGE );
//      return false;
//    }
//    Interface.printAlways( "@@@ will cd to " + buildDir );
//    String machine = machineField.getText().trim();
//    if (hasBlanks( machine, "-m value" ))
//      return false;
//    String options = optionsField.getText().trim();
//
//    String cmd = "build " +
//                 SandboxFiles.list_state.getRcFileFlag() +
//                 " -sb " + sandboxInfo.getSandboxName() + " " + messageLevel +
//                 (machine.equals( "" ) ? "" : (" -m " + machine)) +
//                 (options.equals( "" ) ? "" : (" " + options)) +
//                 (ignoreChecked ? " -ignore" : "")
//                 ;
//    String infoCmd = cmd + " -info";
////@@@ begin section to uncomment when really trying to run build
////    try
////    {
////      // Run cmd with -info first, to catch error messages and display them.
////      // Okay, so there is some extra stuff saying "would run" because of
////      // -info, but at least the user sees the problems, and only if there
////      // are any. Note that if it is only warnings
////      // the start is done because retcode == 0 in that case.
////      int retcode;
////      GuiCommand infoCmdRunner = new GuiCommand( infoCmd, mainFrame );
////      // run it with false at first, so no message box.
////      if ((retcode = infoCmdRunner.runCommand( false, true )) != 0)
////      {
////        Interface.printDebug( "build dialog: cmd='" + infoCmd + "' retcode=" +
////                              retcode );
////        // Only in error situations do we run it again so the messages are
////        // shown in a popup. In the other case, the build will run and the
////        // messages will be seen in context, unless -quiet is done.
////        infoCmdRunner.runCommand( true, true );
////        return false;
////      }
//      Interface.printAlways( "@@@ About to run: " + cmd );
////      Interface.printDebug( "About to start in separate window: " + cmd );
//      JOptionPane.showMessageDialog( mainFrame, 
//                                     "Build option is not implemented yet!",
//                                     "Build sandbox",
//                                     JOptionPane.WARNING_MESSAGE );
//// We need a version of execWindow that takes buildDir. 
//// The option should be added in PlatformShellSystemCall.
////      PlatformShellSystemCall caller = new PlatformShellSystemCall();
////      caller.execWindow( cmd, null, buildDir );
////    }
////    catch (IOException e)
////    {
////      //Interface.printError( cmd + ": IOException: " + e.getMessage() );
////      GuiTextMsg.showErrorMsg( SandboxFiles.frame,
////                                cmd + ": " + e.getMessage(),
////                                "ERROR: IOException" );
////      return false;
////    }
////    catch (InterruptedException e)
////    {
////      //Interface.printError( cmd + ": InterruptedException: " +
////      //                      e.getMessage() );
////      GuiTextMsg.showErrorMsg( SandboxFiles.frame,
////                                cmd + ": " + e.getMessage(),
////                                "ERROR: InterruptedException" );
////      return false;
////    }
////@@@ end section to uncomment when really trying to run build
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
