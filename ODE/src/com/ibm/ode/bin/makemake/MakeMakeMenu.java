package com.ibm.ode.bin.makemake;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import com.ibm.ode.lib.string.StringTools;
import com.ibm.ode.lib.io.Version;
import com.ibm.ode.bin.makemake.MakeMakeOptions;
import com.ibm.ode.bin.makemake.MakeMakeTextDialog;
import com.ibm.ode.bin.makemake.MakeMakeFileMaker;
import com.ibm.ode.bin.makemake.MakeMakeEvent;

public class MakeMakeMenu extends MenuBar
{
  private Frame frame;
  private Dialog about_window;
  private Font font;
  private MakeMakeOptions options;
  private Menu filemenu, opmenu, helpmenu, guimenu;
  private MenuItem run, save, quit, mfname, about, osuffs, jsuffs, isuffs;
  private CheckboxMenuItem backup, subdirs, info, ofiles, verify, autoskip;
  private CheckboxMenuItem[] guilevels;
  private Button ok_button;


  public MakeMakeMenu( Frame frame, MakeMakeOptions options )
  {
    this.frame = frame;
    this.options = options;
    setFont( new Font( "Serif", Font.BOLD, 12 ) );
    initMenus();
    frame.setMenuBar( this );
  }


  public void setEnabled( boolean enable )
  {
    run.setEnabled( enable );
    info.setEnabled( enable );
    verify.setEnabled( enable );
    subdirs.setEnabled( enable );
  }


  private void initMenus()
  {
    helpmenu = new Menu( "Help" );
    about = new MenuItem( "About", new MenuShortcut( KeyEvent.VK_A ) );
    about.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { showAboutWindow(); } } );
    helpmenu.add( about );

    filemenu = new Menu( "File" );
    run = new MenuItem( "Start", new MenuShortcut( KeyEvent.VK_R ) );
    run.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { frame.dispatchEvent( new MakeMakeEvent( frame,
        MakeMakeEvent.RUN_EVENT ) ); } } );
    save = new MenuItem( "Save settings", new MenuShortcut( KeyEvent.VK_S ) );
    save.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { frame.dispatchEvent( new MakeMakeEvent( frame,
        MakeMakeEvent.SAVE_EVENT ) ); } } );
    quit = new MenuItem( "Exit", new MenuShortcut( KeyEvent.VK_Q ) );
    quit.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { frame.dispatchEvent( new WindowEvent( frame,
        WindowEvent.WINDOW_CLOSING ) ); } } );
    filemenu.add( run );
    filemenu.add( save );
    filemenu.add( new MenuItem( "-" ) );
    filemenu.add( quit );

    opmenu = new Menu( "Options" );
    osuffs = new MenuItem( "Object suffixes..." );
    osuffs.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { String result = new MakeMakeTextDialog( frame, "Object suffixes",
        "Enter the suffixes that will be put into OBJECTS/OFILES:",
        vectorToString( options.obj_suffs ), "/\\<>", 0 ).run();
        if (result != null) options.obj_suffs =
        stringToVector( result ); } } );
    jsuffs = new MenuItem( "Java suffixes..." );
    jsuffs.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { String result = new MakeMakeTextDialog( frame, "Java suffixes",
        "Enter the suffixes that will be put into JAVA_CLASSES:",
        vectorToString( options.java_suffs ), "/\\<>", 0 ).run();
        if (result != null) options.java_suffs =
        stringToVector( result ); } } );
    isuffs = new MenuItem( "Header suffixes..." );
    isuffs.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { String result = new MakeMakeTextDialog( frame, "Header suffixes",
        "Enter the suffixes that will be put into INCLUDES:",
        vectorToString( options.hdr_suffs ), "/\\<>", 0 ).run();
        if (result != null) options.hdr_suffs =
        stringToVector( result ); } } );
    mfname = new MenuItem( "Makefile name..." );
    mfname.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { String result = new MakeMakeTextDialog( frame, "Makefile name",
        "Enter the name for the makefile(s):", options.makefile_name,
        " /\\#<>", 0 ).run();
        if (result != null) { options.makefile_name = result;
        frame.dispatchEvent( new MakeMakeEvent( frame,
        MakeMakeEvent.MFNAME_CHANGE_EVENT ) ); } } } );
    guimenu = new Menu( "GUI level" );
    guilevels = new CheckboxMenuItem[MakeMakeOptions.GUI_LEVELS.length];
    for (int i = 0; i < MakeMakeOptions.GUI_LEVELS.length; ++i)
    {
      guilevels[i] = new CheckboxMenuItem( MakeMakeOptions.GUI_LEVELS[i] );
      guilevels[i].setState( i == options.gui_level );
      guilevels[i].addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { if (processGuiMenuEvent( e ))
        frame.dispatchEvent( new MakeMakeEvent( frame,
        MakeMakeEvent.GUILEVEL_CHANGE_EVENT ) ); } } );
      guimenu.add( guilevels[i] );
    }
    backup = new CheckboxMenuItem( "Backup makefiles" );
    backup.setState( options.backup );
    backup.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { options.backup = !options.backup; } } );
    subdirs = new CheckboxMenuItem( "Process subdirectories" );
    subdirs.setState( options.process_subdirs );
    subdirs.addItemListener(  new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { options.process_subdirs = !options.process_subdirs; } } );
    info = new CheckboxMenuItem( "Info only" );
    info.setState( options.info );
    info.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { options.info = !options.info; } } );
    ofiles = new CheckboxMenuItem( "Use OFILES" );
    ofiles.setState( options.use_ofiles );
    ofiles.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { options.use_ofiles = !options.use_ofiles;
        String tmp = options.optvar_vals[MakeMakeOptions.OFILES_INDEX];
        options.optvar_vals[MakeMakeOptions.OFILES_INDEX] =
        options.optvar_vals[MakeMakeOptions.OBJECTS_INDEX];
        options.optvar_vals[MakeMakeOptions.OBJECTS_INDEX] = tmp; } } );
    verify = new CheckboxMenuItem( "Verify contents" );
    verify.setState( options.verify );
    verify.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { options.verify = !options.verify; } } );
    autoskip = new CheckboxMenuItem( "Autoskip" );
    autoskip.setState( options.autoskip );
    autoskip.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { options.autoskip = !options.autoskip; } } );
    opmenu.add( info );
    opmenu.add( subdirs );
    opmenu.add( backup );
    opmenu.add( ofiles );
    opmenu.add( verify );
    opmenu.add( autoskip );
    opmenu.add( guimenu );
    opmenu.add( mfname );
    opmenu.add( osuffs );
    opmenu.add( jsuffs );
    opmenu.add( isuffs );

    add( filemenu );
    add( opmenu );
    setHelpMenu( helpmenu );
  }


  private boolean processGuiMenuEvent( ItemEvent e )
  {
    boolean rc = false;

    for (int i = 0; i < guilevels.length; ++i)
    {
      if (e.getItemSelectable() == guilevels[i])
      {
        guilevels[i].setState( true );
        if (options.gui_level != i)
        {
          options.gui_level = i;
          rc = true;
        }
      }
      else
        guilevels[i].setState( false );
    }

    return (rc);
  }


  private String vectorToString( Vector vec )
  {
    if (vec.size() < 1)
      return ("");
    String[] arr = new String[vec.size()];
    vec.copyInto( arr );
    return (StringTools.join( arr, ", " ));
  }


  private Vector stringToVector( String str )
  {
    String[] arr = StringTools.split( str, ", " );
    Vector vec = new Vector();
    if (arr != null)
      for (int i = 0; i < arr.length; ++i)
        vec.addElement( arr[i] );
    return (vec);
  }


  private void showAboutWindow()
  {
    about_window = new Dialog( frame, "About", true );
    about_window.setBackground( Color.blue );
    about_window.setForeground( Color.white );
    about_window.setFont( new Font( "Dialog", Font.BOLD, 14 ) );
    Point winloc = frame.getLocationOnScreen();
    about_window.setLocation( winloc.x + 20, winloc.y + 20 );
    ok_button = new Button( "  OK  " );
    ok_button.setBackground( SystemColor.control );
    ok_button.setForeground( SystemColor.controlText );
    ok_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { about_window.dispose(); } } );
    about_window.addWindowListener( new WindowAdapter()
        { public void windowClosing( WindowEvent e )
        { about_window.dispose(); }
        public void windowOpened( WindowEvent e )
        { ok_button.requestFocus(); } } );

    about_window.setLayout( new GridLayout( 4, 1, 10, 4 ) );
    about_window.add( new Label( "ODE MakeMake - Makefile Generation Tool",
        Label.CENTER ) );
    about_window.add( new Label( "Version: " + Version.getOdeVersionNumber() +
        " (Build " + Version.getOdeLevelName() + ")", Label.CENTER ) );
    about_window.add( new Label( "\u00a9 1999 IBM Corporation",
        Label.CENTER ) );
    about_window.add( ok_button );

    about_window.pack();
    about_window.show();
  }
}
