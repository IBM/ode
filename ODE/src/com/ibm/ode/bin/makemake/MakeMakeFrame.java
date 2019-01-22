package com.ibm.ode.bin.makemake;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.JFileChooser;

import com.ibm.ode.bin.makemake.MakeMakeMenu;
import com.ibm.ode.bin.makemake.MakeMakeOptions;
import com.ibm.ode.bin.makemake.MakeMakeEvent;
import com.ibm.ode.bin.makemake.MakeMakeVarWindow;
import com.ibm.ode.bin.makemake.MakeMakeOutputable;
import com.ibm.ode.bin.makemake.MakeMakeConfigFile;

public class MakeMakeFrame extends Frame implements MakeMakeOutputable
{
  private static String READY_MSG = "READY";
  private MakeMakeMenu menu;
  private MakeMakeOptions options;
  private MakeMakeVarWindow var_window;
  private Button start_button, dirreq_button;
  private TextField startdir_field;
  private TextArea status_label;
  

  public MakeMakeFrame( MakeMakeOptions opts )
  {
    super( "ODE MakeMake" );
    this.options = opts;
    setBackground( SystemColor.window );
    setForeground( SystemColor.windowText );
    setFont( new Font( "Dialog", Font.BOLD, 12 ) );
    setLayout( new GridBagLayout() );
    GridBagConstraints bag_constraints = new GridBagConstraints();
    bag_constraints.gridwidth = GridBagConstraints.REMAINDER;
    bag_constraints.gridheight = 1;
    bag_constraints.gridx = 0;
    bag_constraints.gridy = 0;
    bag_constraints.ipadx = 1;
    bag_constraints.ipady = 1;
    bag_constraints.insets = new Insets( 2, 10, 0, 10 );
    bag_constraints.weightx = 1.0;
    bag_constraints.fill = GridBagConstraints.HORIZONTAL;
    setLocation( 100, 100 );
    enableEvents( AWTEvent.WINDOW_EVENT_MASK );
    menu = new MakeMakeMenu( this, options );

    add( new Label( "Start directory:", Label.CENTER ), bag_constraints );

    dirreq_button = new Button( "Browse..." );
    dirreq_button.setBackground( SystemColor.control );
    dirreq_button.setForeground( SystemColor.controlText );
    dirreq_button.setFont( new Font( "SansSerif", Font.BOLD, 12 ) );
    dirreq_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { doDirRequest(); } } );
    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.gridwidth = 1;
    bag_constraints.weightx = 0.2;
    bag_constraints.insets = new Insets( 2, 10, 0, 5 );
    add( dirreq_button, bag_constraints );

    startdir_field = new TextField( options.start_dir.toString(), 50 );
    startdir_field.setBackground( Color.white );
    startdir_field.setForeground( Color.black );
    startdir_field.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
    startdir_field.addFocusListener( new FocusAdapter()
        { public void focusLost( FocusEvent e )
        { options.start_dir = new File( startdir_field.getText() ); } } );
    bag_constraints.gridx = 1;
    bag_constraints.gridwidth = GridBagConstraints.REMAINDER;
    bag_constraints.weightx = 0.8;
    bag_constraints.insets = new Insets( 2, 5, 0, 10 );
    add( startdir_field, bag_constraints );

    start_button = new Button( "START" );
    start_button.setBackground( SystemColor.control );
    start_button.setForeground( SystemColor.controlText );
    start_button.setFont( new Font( "SansSerif", Font.BOLD, 18 ) );
    start_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { dispatchEvent( new MakeMakeEvent( start_button,
        MakeMakeEvent.RUN_EVENT ) ); } } );
    ++bag_constraints.gridy;
    bag_constraints.gridx = 0;
    bag_constraints.gridwidth = GridBagConstraints.REMAINDER;
    bag_constraints.weightx = 1.0;
    bag_constraints.fill = GridBagConstraints.NONE;
    bag_constraints.insets = new Insets( 20, 10, 0, 10 );
    add( start_button, bag_constraints );

    ++bag_constraints.gridy;
    bag_constraints.fill = GridBagConstraints.HORIZONTAL;
    add( new Label( "Last message:", Label.CENTER ), bag_constraints );

    status_label = new TextArea( "", 1, 1,
        TextArea.SCROLLBARS_HORIZONTAL_ONLY);
    status_label.setBackground( Color.black );
    status_label.setEditable( false );
    printStatus( READY_MSG, MakeMakeOptions.OK_COND );
    ++bag_constraints.gridy;
    bag_constraints.insets = new Insets( 2, 10, 5, 10 );
    add( status_label, bag_constraints );

    pack();
    show();
  }


  public void print( String msg )
  {
    printStatus( msg, MakeMakeOptions.ERROR_COND );
  }


  public void println( String msg )
  {
    print( msg );
  }


  public void printStatus( String msg, Color color )
  {
    status_label.setForeground( color );
    status_label.setText( msg );
  }


  private void doDirRequest()
  {
    printStatus( "SELECTING DIRECTORY...", MakeMakeOptions.PROGRESS_COND );
    try
    {
      JFileChooser chooser = new JFileChooser( options.start_dir );
      chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
      if (chooser.showDialog( this, "Select Dir" ) ==
          JFileChooser.APPROVE_OPTION)
      {
        options.start_dir = new File(
            chooser.getSelectedFile().getCanonicalPath() );
        startdir_field.setText( options.start_dir.toString() );
      }
      printStatus( READY_MSG, MakeMakeOptions.OK_COND );
    }
    catch (IOException e)
    {
      printStatus( "ERROR: FILE SYSTEM ERROR", MakeMakeOptions.ERROR_COND );
      getToolkit().beep();
    }
    catch (NoClassDefFoundError e)
    {
      printStatus( "ERROR: THE JFC/SWING CLASSES AREN'T AVAILABLE",
          MakeMakeOptions.ERROR_COND );
      getToolkit().beep();
    }
  }


  /**
   *
  **/
  public synchronized void verify()
  {
    options.success = false;
    disableComponents();
    var_window = new MakeMakeVarWindow( this, options,
        options.start_dir, this );
  }


  private void disableComponents()
  {
    menu.setEnabled( false );
    dirreq_button.setEnabled( false );
    startdir_field.setEnabled( false );
    start_button.setEnabled( false );
  }


  private void enableComponents()
  {
    menu.setEnabled( true );
    dirreq_button.setEnabled( true );
    startdir_field.setEnabled( true );
    start_button.setEnabled( true );
  }


  public void dispose()
  {
    super.dispose();
    System.exit( 0 );
  }
  

  private void shutdown()
  {
    if (var_window != null && var_window.isShowing())
    {
      getToolkit().beep();
      if (!(new MakeMakeYesNoDialog( this, "Confirm exit",
          "Verification in progress!  Are you sure you want to exit?" ).run()))
        return;
      var_window.dispose();
    }

    dispose();
  }


  protected void processWindowEvent( WindowEvent event )
  {
    if (event.getID() == WindowEvent.WINDOW_CLOSING)
      shutdown();
    else if (event.getID() == WindowEvent.WINDOW_OPENED)
      startdir_field.requestFocus();
  }


  protected void processEvent( AWTEvent event )
  {
    if (event.getID() == MakeMakeEvent.RUN_EVENT)
    {
      printStatus( "RUNNING...", MakeMakeOptions.PROGRESS_COND );

      if (!options.start_dir.isDirectory())
      {
        printStatus( "ERROR: START DIRECTORY IS NOT A DIRECTORY",
            MakeMakeOptions.ERROR_COND );
        getToolkit().beep();
      }
      else if (!options.start_dir.canWrite())
      {
        printStatus( "ERROR: DO NOT HAVE WRITE ACCESS TO " +
            "START DIRECTORY", MakeMakeOptions.ERROR_COND );
        getToolkit().beep();
      }
      else
      {
        if (options.verify)
          verify(); // comes back as a VERIFY_DONE_EVENT later on
        else if (MakeMakeFileMaker.createMakefiles( options,
            options.start_dir, this ))
          printStatus( READY_MSG, MakeMakeOptions.OK_COND );
        else
          getToolkit().beep();
      }
    }
    else if (event.getID() == MakeMakeEvent.VERIFY_DONE_EVENT)
    {
      enableComponents();
      if (options.success)
        printStatus( READY_MSG, MakeMakeOptions.OK_COND );
      else
        getToolkit().beep();
    }
    else if (event.getID() == MakeMakeEvent.SAVE_EVENT)
    {
      boolean success = MakeMakeConfigFile.write( options );
      if (var_window == null || !var_window.isShowing())
      {
        if (success)
          printStatus( READY_MSG, MakeMakeOptions.OK_COND );
        else
        {
          printStatus( "ERROR: COULDN'T SAVE CONFIG FILE (" +
              MakeMakeConfigFile.CONFIG_FILE_PATH + ")",
              MakeMakeOptions.ERROR_COND );
          getToolkit().beep();
        }
      }
    }
    else if (event.getID() == MakeMakeEvent.MFNAME_CHANGE_EVENT)
    {
      if (var_window != null && var_window.isShowing())
      {
        MakeMakeFileMaker.updateMakefilePath( options );
        var_window.updateMakefilePath();
      }
    }
    else if (event.getID() == MakeMakeEvent.GUILEVEL_CHANGE_EVENT)
    {
      if (var_window != null && var_window.isShowing())
        var_window.restart();
    }
    else
    {
      super.processEvent( event );
    }
  }
}
