package com.ibm.ode.bin.sandboxlist;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.ibm.ode.bin.sandboxfiles.SandboxFiles;
import com.ibm.ode.lib.io.*;
import com.ibm.ode.lib.string.*;
import com.ibm.ode.lib.util.*;
import com.ibm.ode.bin.gui.*;


/**
 * This menu action command provides a way of starting the SandboxFiles
 * gui program for a specific sandbox. A dialog is presented that allows
 * certain options to be entered. If no options are chosen, the selected
 * sandbox is used and all files are displayed.
**/
class SLShowFilesCmd implements SLAction, ActionListener, ItemListener
{
  String quietString = "-quiet";
  String normalString = "-normal";   // default
  String verboseString = "-verbose";
  String debugString = "-debug";

  SLFrame mainFrame;
  SLInfo sandboxInfo;
  JTextField subdirField;  // subdirectory where to start SandboxFiles
  JTextField optionsField; // options and targets passed to mk
  String messageLevel;     // radio button value
  boolean sandboxOnlyChecked;
  boolean noRecurseChecked;
  JDialog dia;
  JCheckBox checkNoRecurse;
  JCheckBox checkSandboxOnly;


  /**
   * Runs a dialog and after it is dismissed, the input data is checked.
   * If the data suffices for starting SandboxFiles, return true.
  **/
  private void doShowFilesDialog()
  {
    dia = new JDialog( mainFrame,
                       "show files",
                       false // not modal
                       );
    // Add a bunch of things to the contentPane: cp.add( child );
    Container cp = dia.getContentPane();
    Point loc = mainFrame.getLocation();
    dia.setLocation( loc.x + 50, loc.y + 50 );
    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints con = new GridBagConstraints();
    cp.setLayout( lay );

    // Single field for the optional subdirectory of the sandbox in which 
    // to run SandboxFiles.
    JPanel subdirPanel = getSubdirPanel();
    con.gridx = 0;
    con.gridy = 0;
    con.anchor = GridBagConstraints.WEST;
    con.fill = GridBagConstraints.HORIZONTAL;
    lay.setConstraints( subdirPanel, con );
    cp.add( subdirPanel );

    // field for SandboxFiles options and targets
    JPanel optionsPanel = getOptionsPanel();
    con.gridy = GridBagConstraints.RELATIVE;
    lay.setConstraints( optionsPanel, con );
    cp.add( optionsPanel );

    // -sandboxonly flag check box
    JPanel sandboxOnlyCheckPanel = getSandboxOnlyCheckPanel();
    con.gridy = GridBagConstraints.RELATIVE;
    lay.setConstraints( sandboxOnlyCheckPanel, con );
    cp.add( sandboxOnlyCheckPanel );

    // -norecurse flag check box
    JPanel noRecurseCheckPanel = getNoRecurseCheckPanel();
    con.gridy = GridBagConstraints.RELATIVE;
    lay.setConstraints( noRecurseCheckPanel, con );
    cp.add( noRecurseCheckPanel );

    // radio buttons for message level
    //JPanel messageLevelPanel = getMessageLevelPanel();
    //con.gridy = GridBagConstraints.RELATIVE;
    //lay.setConstraints( messageLevelPanel, con );
    //cp.add( messageLevelPanel );

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
   * so it should be a relative path.
   * The JPanel will be a text box, preceded by some explanatory text.
  **/
  private JPanel getSubdirPanel()
  {
    JPanel pan = new JPanel();
    pan.setLayout( new BoxLayout( pan, BoxLayout.Y_AXIS ) );
    pan.setBorder( BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                              " subdirectory to show files " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    pan.add( new JLabel(
                  "Enter the optional subdirectory to show files in." ) );
    pan.add( new JLabel(
                  "It should be relative to the src directory." ) );
    pan.add( new JLabel(
                  "If omitted, the files in the src directory will be shown." ) );
    subdirField = new JTextField();
    subdirField.addActionListener( this );
    pan.add( subdirField );
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
                                          " optional file specifications " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    pan.add( new JLabel( "Enter optional file specifications:" ) );
    optionsField = new JTextField();
    optionsField.addActionListener( this );
    pan.add( optionsField );
    return pan;
  }


  /**
   * Make a JPanel for entering a -sandboxonly flag.
   * It will be a check box, followed by some explanatory text.
  **/
  private JPanel getSandboxOnlyCheckPanel()
  {
    sandboxOnlyChecked = false;
    JPanel pan = new JPanel();
    pan.setBorder( BorderFactory.createCompoundBorder(
                       BorderFactory.createTitledBorder(
                           " -sandboxonly " ),
                           BorderFactory.createEmptyBorder( 0,5,5,5 ) ) );
    checkSandboxOnly = new JCheckBox( 
              "If checked, files from backing sandboxes are not displayed." );
    checkSandboxOnly.addItemListener( this );
    pan.add( checkSandboxOnly );
    pan.setAlignmentX( Component.RIGHT_ALIGNMENT );
    return pan;
  }


  /**
   * Make a JPanel for entering a -norecurse flag.
   * It will be a check box, followed by some explanatory text.
  **/
  private JPanel getNoRecurseCheckPanel()
  {
    noRecurseChecked = false;
    JPanel pan = new JPanel();
    pan.setBorder( BorderFactory.createCompoundBorder(
                       BorderFactory.createTitledBorder(
                           " -norecurse " ),
                           BorderFactory.createEmptyBorder( 0,5,5,5 ) ) );
    checkNoRecurse = new JCheckBox( 
                    "If checked, files in subdirectories are not displayed." );
    checkNoRecurse.addItemListener( this );
    pan.add( checkNoRecurse );
    pan.setAlignmentX( Component.RIGHT_ALIGNMENT );
    return pan;
  }


  public void itemStateChanged( ItemEvent e )
  {
    Object source = e.getItemSelectable();
    if (source == checkSandboxOnly)
    {
      if (e.getStateChange() == ItemEvent.DESELECTED)
        sandboxOnlyChecked = false;
      else if (e.getStateChange() == ItemEvent.SELECTED)
        sandboxOnlyChecked = true;
    }
    else if (source == checkNoRecurse)
    {
      if (e.getStateChange() == ItemEvent.DESELECTED)
        noRecurseChecked = false;
      else if (e.getStateChange() == ItemEvent.SELECTED)
        noRecurseChecked = true;
    }
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
    JButton showFilesButton = new JButton( "Show files" );
    cancelButton.addActionListener(
                   new ActionListener()
                   {
                     public void actionPerformed( ActionEvent e )
                     {
                       dia.dispose();  // zap the window
                     }
                   });
    showFilesButton.addActionListener(
                   new ActionListener()
                   {
                     public void actionPerformed( ActionEvent e )
                     {
                       startShowFiles();
                     }
                   });
    //Lay out the buttons from left to right.
    JPanel pan = new JPanel();
    pan.setLayout( new BoxLayout( pan, BoxLayout.X_AXIS ) );
    pan.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
    pan.add( Box.createHorizontalGlue() );
    pan.add( cancelButton );
    pan.add( Box.createRigidArea( new Dimension( 10, 0 ) ) );
    pan.add( showFilesButton );
    return pan;
  }


  /**
   * This is called when the menu item is clicked to show files.
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
                        "You must select exactly one sandbox for Show Files!",
                                     "Show files",
                                     JOptionPane.WARNING_MESSAGE );
    }
    else
    {
      sandboxInfo = sel[0];
      if (sandboxInfo.getStatus().equals( SLInfo.STATUS_ERROR ))
      {
        JOptionPane.showMessageDialog( frame, 
                  "You cannot select this option if the sandbox status is " +
                                       SLInfo.STATUS_ERROR,
                                       "Show files",
                                       JOptionPane.WARNING_MESSAGE );
        return;
      }
      doShowFilesDialog();
    }
  }


  /**
   * Start the command to show files.
  **/
  boolean startShowFiles()
  {
    String subdir = subdirField.getText().trim();
    if (hasBlanks( subdir, "subdirectory to show files" ))
      return false;
    // test if directory is absolute
    if (!subdir.equals( "" ) && new File( subdir ).isAbsolute())
    {
      JOptionPane.showMessageDialog( mainFrame, 
                                     "Subdirectory to show files '" + subdir +
                                         "'\nshould not be absolute!",
                                     "Show files in sandbox",
                                     JOptionPane.WARNING_MESSAGE );
      return false;
    }
    // The cd to a missing directory after the workon, would cause a complaint
    // from the shell and then leave you in the current directory.
    //@@@ WARNING! We really should discover what the user uses instead of
    //@@@ wiring in "src" below. It IS controllable.
    //@@@ One way would be to get the environment variables in a workon
    //@@@ for the sandbox. If errors are had, the show files program would not
    //@@@ run in the correct directory, so complain in that case.
    //@@@ The ODESRCNAME variable should have what we want.
    String showFilesDir = sandboxInfo.getBase() + File.separator +
                      sandboxInfo.getSandboxName() + File.separator + "src";
    if (!subdir.equals( "" ))
      showFilesDir += File.separator + subdir;
    showFilesDir = Path.canonicalize( new File( showFilesDir ) );
    String[] opts = StringTools.split( optionsField.getText(), " " );
    String[] sf_args = new String[opts.length + 6]; // 6 = # of static args
    sf_args[0] = (SandboxList.defaultEditor.equals( "" ) ?
                 "" : "-ed " + SandboxList.defaultEditor);
    sf_args[1] = (sandboxOnlyChecked ? "-sandboxonly" : "");
    sf_args[2] = (noRecurseChecked ? "-norecurse" : "");
    sf_args[3] = SandboxList.list_state.getRcFileFlag();
    sf_args[4] = "-sb " + sandboxInfo.getSandboxName();
    boolean found_file_arg = false;
    for (int i = 0, j = 5; i < opts.length; ++i, ++j)
    {
      if (!opts[i].startsWith( "-" ))
      {
        sf_args[j] = showFilesDir + File.separator + opts[i];
        found_file_arg = true;
      }
    }
    sf_args[sf_args.length - 1] = (found_file_arg ? "" : showFilesDir);
    Interface.printDebug( "Running SandboxFiles with the following args:" );
    Interface.printDebug( sf_args );
    new SandboxFiles( sf_args ).run();
    return (true);
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
