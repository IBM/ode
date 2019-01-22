package com.ibm.ode.bin.sandboxlist;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.util.*;
import com.ibm.ode.bin.gui.*;
import com.ibm.ode.lib.string.*;


/**
 * This action routine creates a backing build or sandbox
 * using mkbb or mksb.
**/
class SLCreateCmd implements SLAction, ActionListener, ItemListener
{
  boolean createBB;
  JDialog dia;
  JTextField nameField;
  JTextField machineField;
  JTextField blistField;
  boolean defChecked;
  SLFrame mainFrame;
  JComboBox dirList;
  JComboBox backList;

  SLCreateCmd( boolean createBackingBuild )
  {
    createBB = createBackingBuild;
  }

  public void doAction( SLFrame frame )
  {
    mainFrame = frame;
    dia = new JDialog( frame,
                       (createBB ? "Create backing build" :
                                   "Create sandbox"),
                       true ); // modal

    // Add a bunch of things to the contentPane: cp.add( child );
    Container cp = dia.getContentPane();
    Point loc = frame.getLocation();
    dia.setLocation( loc.x + 50, loc.y + 50 );
    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints con = new GridBagConstraints();
    cp.setLayout( lay );

    // Required field, the name of the sandbox. (will become -sb name)
    // check what is entered to verify that it is non-blank and is not
    // already a sandbox.
    JPanel namePanel = getNamePanel();
    con.gridx = 0;
    con.gridy = 0;
    con.anchor = GridBagConstraints.WEST;
    con.fill = GridBagConstraints.HORIZONTAL;
    lay.setConstraints( namePanel, con );
    cp.add( namePanel );

    // Directory in which the sandbox is created (will become -dir dir
    // if the field is used). It is an editable combo box with
    // initial entries from getDirList()
    JPanel dirPanel = getDirListPanel();
    con.gridy = GridBagConstraints.RELATIVE;
    lay.setConstraints( dirPanel, con );
    cp.add( dirPanel );

    // If !createBB then backing build for sandbox (will become -back bb
    // if an entry is made).
    // The user is given an editable combo box with choices from
    // getBackingList(), and no initial value... or we could show the first
    // entry as the initial value since it will be the default used.
    // Indicate that the items can be used, or an absolute path to the
    // backing build.
    if (!createBB)
    {
      JPanel backPanel = getBackListPanel();
      lay.setConstraints( backPanel, con );
      cp.add( backPanel );
    }

    // Single field for machine(s) separated by ':'. The only current 
    // validity checking is for blanks, after trimming.
    // NOTE FOR THE FUTURE: It would be prefered
    // to have some method of getting the valid machines. The backing build
    // determines this for a sandbox. By default the .sandboxrc determines
    // it for a new backing build. It would be nice to have mksb or mkbb
    // return what is acceptable.
    JPanel machinePanel = getMachineListPanel();
    lay.setConstraints( machinePanel, con );
    cp.add( machinePanel );

    // build list file pathname is a blank field (will become -blist buildlist
    // if an entry is made).
    JPanel blistPanel = getBuildListPanel();
    lay.setConstraints( blistPanel, con );
    cp.add( blistPanel );

    // Check box to let the user make this be the default sandbox (will become
    // -def). Initially set false.
    JPanel defPanel = getDefaultCheckPanel();
    lay.setConstraints( defPanel, con );
    cp.add( defPanel );

    // We need some buttons
    JPanel buttonPanel = getButtonPanel();
    con.anchor = GridBagConstraints.EAST;
    lay.setConstraints( buttonPanel, con );
    cp.add( buttonPanel );

    dia.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
    // show the create dialog
    dia.pack();
    dia.setVisible( true );
  }


  /**
   * Make a JPanel for entering a required sandbox name
  **/
  private JPanel getNamePanel()
  {
    String info;
    if (createBB)
      info = "Required backing build name";
    else
      info = "Required sandbox name";
    JPanel pan = new JPanel();
//    pan.setLayout( new BoxLayout( pan, BoxLayout.Y_AXIS ) );
    pan.setLayout( new GridLayout( 0, 1 ) );
    nameField = new JTextField( 20 );
    nameField.addActionListener( this );
    pan.setBorder( BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder( " name " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    JLabel lab1 = new JLabel( info );
    pan.add( lab1 );
    pan.add( nameField );
    return pan;
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
   * Make a JPanel for entering a -dir value.
   * It will be a combo box, preceded by instructions.
  **/
  private JPanel getDirListPanel()
  {
    JPanel pan = new JPanel();
    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints con = new GridBagConstraints();
    pan.setLayout( lay );
    pan.setBorder( BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder( " -dir directory " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    JLabel lab1 = new JLabel( "Choose a directory to put the sandbox in," ); 
    con.gridx = 0;
    con.gridy = 0;
    con.fill = GridBagConstraints.HORIZONTAL;
    con.anchor = GridBagConstraints.WEST;
    pan.add( lab1 );
    lay.setConstraints( lab1, con );
    JLabel lab2 = new JLabel ( "or enter an absolute path to the directory." );
    con.gridy = GridBagConstraints.RELATIVE;
    con.anchor = GridBagConstraints.WEST;
    pan.add( lab2 );
    lay.setConstraints( lab2, con );
    dirList = new JComboBox( getDirList() );
    dirList.setEditable( true );
    con.anchor = GridBagConstraints.WEST;
    lay.setConstraints( dirList, con );
    pan.add( dirList );
    return pan;
  }


  /**
   * Make a JPanel for entering a -back value.
   * It will be a combo box, followed by some explanatory text.
  **/
  private JPanel getBackListPanel()
  {
    JPanel pan = new JPanel();
    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints con = new GridBagConstraints();
    pan.setLayout( lay );
    pan.setBorder( BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                                    " -back backing_build " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    JLabel lab1 = new JLabel( "Choose a backing build for the sandbox, or" );
    con.gridx = 0;
    con.gridy = 0;
    con.anchor = GridBagConstraints.WEST;
    con.fill = GridBagConstraints.HORIZONTAL;
    lay.setConstraints( lab1, con );
    pan.add( lab1 );
    JLabel lab2 = new JLabel( "enter an absolute path to a backing build." );
    con.gridy = GridBagConstraints.RELATIVE;
    lay.setConstraints( lab2, con );
    pan.add( lab2 );
    JLabel lab3 = new JLabel( 
               "The default is the initial value shown, if there is one." );
    lay.setConstraints( lab3, con );
    pan.add( lab3 );
    backList = new JComboBox( getBackingList() );
    backList.setEditable( true );
    lay.setConstraints( backList, con );
    pan.add( backList );
//    backList.addActionListener(
//            new ActionListener()
//            {
//              public void actionPerformed( ActionEvent e )
//              {
//                JComboBox cb = (JComboBox)e.getSource();
//                String newSelection = (String)cb.getSelectedItem();
////                currentPattern = newSelection;
//              }
//            });
    return pan;
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
                        BorderFactory.createTitledBorder( " -m machines " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    pan.add( new JLabel( "Enter machine types, separated by ':' characters."
                         ) );
    pan.add( new JLabel( "Otherwise, reasonable defaults will be used." ) );
    machineField = new JTextField();
    machineField.addActionListener( this );
    pan.add( machineField );
    return pan;
  }


  /**
   * Make a JPanel for entering a -blist value.
   * It will be a text box, followed by some explanatory text.
  **/
  private JPanel getBuildListPanel()
  {
    JPanel pan = new JPanel();
    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints con = new GridBagConstraints();
    pan.setLayout( lay );
    pan.setBorder( BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder( " -blist file " ),
                        BorderFactory.createEmptyBorder( 0,5,5,5 )));
    JLabel lab1 = new JLabel(
                "Enter full path of build list file if one is used by your" );
    con.gridx = 0;
    con.gridy = 0;
    con.anchor = GridBagConstraints.WEST;
    lay.setConstraints( lab1, con );
    pan.add( lab1 );
    JLabel lab2 = new JLabel(
                "project and you want this sandbox listed there." );
    con.gridy = GridBagConstraints.RELATIVE;
    lay.setConstraints( lab2, con );
    pan.add( lab2 );
    blistField = new JTextField( 20 );
    blistField.addActionListener( this );
    con.fill = GridBagConstraints.HORIZONTAL;
    lay.setConstraints( blistField, con );
    pan.add( blistField );
    return pan;
  }


  /**
   * Make a JPanel for entering a -def flag.
   * It will be a check box, followed by some explanatory text.
  **/
  private JPanel getDefaultCheckPanel()
  {
    JCheckBox checkDefault;
    defChecked = false;
    JPanel pan = new JPanel();
    pan.setBorder( BorderFactory.createCompoundBorder(
                       BorderFactory.createTitledBorder(
                           " -def " ),
                           BorderFactory.createEmptyBorder( 0,5,5,5 ) ) );
    checkDefault = new JCheckBox( 
                           "If checked, make this sandbox be the default." );
    checkDefault.addItemListener( this );
    pan.add( checkDefault );
    pan.setAlignmentX( Component.RIGHT_ALIGNMENT );
    return pan;
  }


  public void itemStateChanged( ItemEvent e )
  {
    if (e.getStateChange() == ItemEvent.DESELECTED)
      defChecked = false;
    else if (e.getStateChange() == ItemEvent.SELECTED)
      defChecked = true;
    //@@@ printCurrentValues();  //@@@ for debugging
  }


  private JPanel getButtonPanel()
  {
    JButton cancelButton = new JButton( "Cancel" );
    JButton createButton = new JButton( (createBB ? "Create backing build" :
                                                 "Create sandbox") );
    cancelButton.addActionListener(
                   new ActionListener()
                   {
                     public void actionPerformed( ActionEvent e )
                     {
                       dia.dispose();  // zap the window
                     }
                   });
    createButton.addActionListener(
                   new ActionListener()
                   {
                     public void actionPerformed( ActionEvent e )
                     {
                       // call the routine to create the sandbox
                       if (createSandbox())
                       {
                         // it succeeded so dispose of the window
                         dia.dispose();
                         // show the changed table contents
                         mainFrame.refresh();
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
    pan.add( createButton );
    return pan;
  }

  public void printCurrentValues() //@@@ for debugging
  {
    Interface.printAlways( "@@@ current sandbox name '" +
                           nameField.getText() + "'" );
    Interface.printAlways( "@@@ current -dir value '" +
                           (String)dirList.getSelectedItem() + "'" );
    if (!createBB)
      Interface.printAlways( "@@@ current -back value '" +
                             backList.getSelectedItem() + "'" );
    Interface.printAlways( "@@@ current -m value '" +
                           machineField.getText() + "'" );
    Interface.printAlways( "@@@ current -blist value '" +
                           blistField.getText() + "'" );
    Interface.printAlways( "@@@ -def check box is set " + defChecked );
  }

  /**
   * Check input and create sandbox here.
   * If inputs are not okay (i.e. a missing sandbox name or more than one
   * token in a field), then pop up an error dialog, and return false.
   * Put together the command and run it, showing errors if they occur.
   * If there are errors then return false otherwise return true.
  **/
  public boolean createSandbox()
  {
    //@@@ printCurrentValues(); //@@@ for debugging

    // do validity checking first
    String name = nameField.getText().trim();
    if (name.equals( "" ))
    {
      JOptionPane.showMessageDialog( dia, 
                                     (createBB ? 
                             "Please enter the required backing build name." :
                             "Please enter the required sandbox name."),
                                     "ERROR: blank name field",
                                     JOptionPane.ERROR_MESSAGE );
      return false;
    }
    if (hasBlanks( name, (createBB ? "backing build name" : "sandbox name") ))
      return false;
    String dir = ((String)dirList.getSelectedItem());
    if (dir == null)
    {
      JOptionPane.showMessageDialog( dia, 
                      "Please enter an absolute path for the -dir directory",
                                     "ERROR: blank -dir directory field",
                                     JOptionPane.ERROR_MESSAGE );
      return false;
    }
    dir = dir.trim();
    if (hasBlanks( dir, "-dir value" ))
      return false;
    String back = "";
    if (!createBB)
    {
      back = ((String)backList.getSelectedItem()).trim();
      if (hasBlanks( back, "-back value" ))
        return false;
    }
    String machine = machineField.getText().trim();
    if (hasBlanks( machine, "-m value" ))
      return false;
    String blist = blistField.getText().trim();
    if (hasBlanks( blist, "-blist value" ))
      return false;

    // assemble the command
    StringBuffer buf = new StringBuffer( 40 );
    buf.append( createBB ? "mkbb" : "mksb" ).append( " -auto " ).append( name );
    if (!createBB && !back.equals( "" ))
      buf.append( " -back " ).append( back );
    if (!dir.equals( "" ))
      buf.append( " -dir " ).append( dir );
    if (!machine.equals( "" ))
      buf.append( " -m " ).append( machine );
    if (!blist.equals( "" ))
      buf.append( " -blist " ).append( blist );
    if (defChecked)
      buf.append( " -def " );
    buf.append( " " ).append( SandboxList.list_state.getRcFileFlag() );
    String cmd = buf.toString();

    // run the command
    Interface.printDebug( "About to run: " + cmd );
    GuiCommand guiCmd = new GuiCommand( cmd, mainFrame );
    guiCmd.runCommand( true, // show output if error or warning
                       true  // modal
                       );
    return true;
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


  /**
   * This routine gets a list of possible backing builds to use with the
   * -back flag, by the following sneaky technique: 
   * Use a sandbox name that is not for an existing sandbox, i.e. take the
   * longest sandbox name in the table and add one character at the end.
   * Run: mksb -auto -info sandbox
   *  or: mksb -auto -info sandbox -rc rcfile
   * and you will get one of the two situations:
   *
   * D:\als\info>mksb -info newone -auto
   * [26] : boojum : 25 : 9 : 8 : 7 : 24 : lemmaafter : lemmabefore : 33 : 32
   * Select one or enter absolute path to backing build: Using default...
   * Would create sandbox newone
   *
   * D:\als\info>mksb -info newone -auto -rc snark
   * User rc file, snark, does not exist; will create it.
   * Enter path of the sandbox base: Using default...
   * >> ERROR: mksb: Cannot use the <-auto> option without valid base dir.
   *
   * So far, it appears that the first item is enclosed in [] so it is easy
   * to determine if the line is valid. Every other items (there must be an
   * odd number of items) will be ':' as well. Note in the first example
   * (edited to fit the space) that lemmaafter and lemmabefore are listed in
   * .sandboxrc rather than in the build_list. Unfortunately these local
   * backing builds are not displayed if there is no build_list!
   * 
   * Note that anything that is in the table also can be used for -back,
   * whether or not they are backing builds.
   * So make them accessible in some way: show all the table entries with
   * full paths (using variable base if available) in the -back options.
   * Alternative for current table entries: Have a button that uses the
   * first selected table entry for -back. Make the dialog be non-modal
   * so that the selection can be made.
  **/
  public String[] getBackingList()
  {
    String fakeName = "";
    // find longest name in sandbox list
    for (int i = 0; i < SandboxList.list_state.size(); ++i)
    {
      SLInfo sli = SandboxList.list_state.getSLInfo( i );
      String name = sli.getSandboxName();
      if (name.length() > fakeName.length())
        fakeName = name;
    }
    // append '_' to the longest name and store in fakeName
    fakeName = fakeName + "_";

    // run: mksb -info -auto fakeName [rc rcfile]
    StringBuffer out = new StringBuffer();
    String cmd = "mksb -auto -info " + fakeName + " " +
                                       SandboxList.list_state.getRcFileFlag();
    String line;
    Vector tokens = new Vector();
    try
    {
      Interface.printDebug( "Will run '" + cmd + "'" );
      new GuiCommand( cmd, mainFrame ).runCommand( out, null, false, false );
      //@@@ Interface.printDebug( "@@@ output was '" + out + "'" );

      // scan for first line beginning with '['.
      // If first token is more than 2 characters and
      // the first and last are [], remove the chars, and store as first
      // token in a StringBuffer. Scan the rest, making sure that the 2nd, 4th, 
      // 6th etc are ':', otherwise the line is not valid.
      // Create a String[] from the tokens and return it; otherwise return an
      // empty String[].
      LineNumberReader lineReader =
         new LineNumberReader( new StringReader( out.toString() ) );
      while ((line = lineReader.readLine()) != null)
      {
        // pick out the tokens and especially sniff at the first one
        StringTokenizer st = new StringTokenizer( line );
        if (st.hasMoreTokens())
        {
          String first = st.nextToken();
          if (first.length() >= 3 && first.charAt(0) == '[' &&
              first.charAt( first.length() - 1 ) == ']')
          {
            // the first token seems okay; this probably is the build_list line
            tokens.addElement( first.substring( 1, first.length() - 1 ) );
            while (st.hasMoreTokens())
            {
              // We are a little too trusting here, and assume that the
              // names alternate with ":" tokens, but do not check that they
              // in fact do so.
              String next = st.nextToken();
              if (!next.equals( ":" ))
                tokens.addElement( next );
            }
            break; // there is only one line with this data, so stop reading
          }
        }
      }
    }
    catch (IOException e)
    {
      //@@@ Interface.printError( cmd + ": " + e.getMessage() );
      GuiTextMsg.showErrorMsg( SandboxList.frame,
                                cmd + ": " + e.getMessage(),
                                "ERROR: IOException" );
    }
    // add in the absolute paths to the sandboxes in our rcfile
    for (int i = 0; i < SandboxList.list_state.size(); ++i)
    {
      SLInfo sli = SandboxList.list_state.getSLInfo( i );
      String base = sli.getVariableBase();
      if (base.equals( "" ))
        base = sli.getBase();
      if (!base.equals( "" ))
      {
        tokens.addElement( base + File.separatorChar + sli.getSandboxName() );
      }
    }
    // convert the tokens to a String array.
    String[] backingBuilds = new String[tokens.size()];
    if (tokens.size() > 0)
    {
      // fill the String array with values
      Enumeration e = tokens.elements();
      for (int i = 0; e.hasMoreElements(); ++i)
        backingBuilds[i] = (String)(e.nextElement());
    }
    return backingBuilds;
  }

  /**
   * Return a list of possible values for -dir. Scan the bases and variable
   * bases in the table for unique values, and return those.
  **/
  public String[] getDirList()
  {
    // put all the unique sandbox bases in the table
    Hashtable ht = new Hashtable();
    for (int i = 0; i < SandboxList.list_state.size(); ++i)
    {
      SLInfo sli = SandboxList.list_state.getSLInfo( i );
      String base = sli.getVariableBase();
      if (!base.equals( "" ) && !ht.containsKey( base ))
        ht.put( base, base );
      base = sli.getBase();
      if (!base.equals( "" ) && !ht.containsKey( base ))
        ht.put( base, base );
    }
    // put the Strings in an array
    String bases[] = new String[ht.size()];
    if (ht.size() > 0)
    {
      Enumeration e = ht.elements();
      for (int i = 0; e.hasMoreElements(); ++i)
        bases[i] = (String)(e.nextElement());
    }
    return bases;
  }
}
