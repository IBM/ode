package com.ibm.ode.bin.makemake;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Vector;

import com.ibm.ode.bin.makemake.MakeMakeOptions;
import com.ibm.ode.bin.makemake.MakeMakeYesNoDialog;
import com.ibm.ode.lib.string.StringTools;
import com.ibm.ode.lib.io.Path;

public class MakeMakeVarWindow extends Frame
{
  private static final int LIST_SIZE = 8;

  private MakeMakeOptions options;
  private MakeMakeFileMaker file_maker;
  private MakeMakeTextArea preview_text;
  private MakeMakeOutputable error_printer;
  private String[] new_optvar_vals;
  private Choice var_choices, targvar_choices, customvar_choices;
  private CheckboxGroup assignments_chkboxgrp;
  private Checkbox[] assignments_chkbox;
  private Checkbox mfinclude_chkbox;
  private Button add_button, del_button, addall_button, delall_button,
      targadd_button, targdel_button, targaddall_button, targdelall_button,
      done_button, abort_button, preview_button, customvar_add_button,
      customvar_del_button, customvar_clear_button, advanced_window_button,
      skip_button;
  private Frame frame, advanced_window, preview_window;
  private TextArea mfpath_label;
  private TextField custom_field, targcustom_field,
      customvar_name_field, customvar_val_field, customvar_comment_field;
  private Label desc_label, mfdate_label;
  private List avail_list, curr_list, targavail_list, targcurr_list;
  private Thread file_maker_thread;
  private class MakeMakeCanvas extends Canvas
  {
    public void paint( Graphics g )
    {
      super.paint( g );
      Rectangle bounds = this.getBounds();
      g.setColor( SystemColor.windowText );
      g.fillRect( 0, 0, bounds.width, 2 );
    }
  }
  

  /**
   *
  **/
  public MakeMakeVarWindow( Frame frame, MakeMakeOptions opts, File start_dir,
      MakeMakeOutputable error_printer )
  {
    super( "Makefile Verification" );
    this.options = opts;
    this.frame = frame;
    this.error_printer = error_printer;
    file_maker = new MakeMakeFileMaker( options, start_dir, error_printer );
    file_maker_thread = new Thread( file_maker );
    file_maker_thread.start();
    if (!file_maker.waitForFirstDir()) // nothing to process?
    {
      complete( false );
      return;
    }
    initTempData();
    setBackground( SystemColor.window );
    setForeground( SystemColor.windowText );
    setFont( new Font( "Dialog", Font.PLAIN, 12 ) );
    Point winloc = frame.getLocationOnScreen();
    setLocation( winloc.x + 20, winloc.y + 20 );
    restart();
  }


  public void restart()
  {
    setVisible( false );
    removeAll();
    if (advanced_window != null)
    {
      advanced_window.dispose();
      advanced_window = null;
    }

    setLayout( new GridBagLayout() );
    GridBagConstraints bag_constraints = new GridBagConstraints();
    bag_constraints.gridx = 0;
    bag_constraints.gridy = 0;
    bag_constraints.ipadx = 1;
    bag_constraints.ipady = 1;
    bag_constraints.gridheight = 1;
    bag_constraints.gridwidth = 1;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 5, 10, 0, 2 );
    bag_constraints.fill = GridBagConstraints.HORIZONTAL;

    add( new Label( "Current makefile:", Label.RIGHT ), bag_constraints );
    mfpath_label = new TextArea( options.makefile_path.toString(), 1, 1,
        TextArea.SCROLLBARS_HORIZONTAL_ONLY);
    mfpath_label.setBackground( Color.white );
    mfpath_label.setForeground( Color.blue );
    mfpath_label.setEditable( false );
    bag_constraints.gridx = 1;
    bag_constraints.gridwidth = GridBagConstraints.REMAINDER;
    bag_constraints.weightx = 0.6;
    bag_constraints.insets = new Insets( 5, 2, 0, 10 );
    add( mfpath_label, bag_constraints );
    ++bag_constraints.gridy;
    bag_constraints.gridx = 0;
    bag_constraints.gridwidth = 1;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 5, 10, 0, 2 );
    add( new Label( "Last modified:", Label.RIGHT ), bag_constraints );
    mfdate_label = new Label( Path.getFileDate( options.makefile_path ),
        Label.LEFT );
    bag_constraints.gridx = 1;
    bag_constraints.gridwidth = GridBagConstraints.REMAINDER;
    bag_constraints.weightx = 0.6;
    bag_constraints.insets = new Insets( 5, 2, 0, 10 );
    add( mfdate_label, bag_constraints );

    addHorizLine( this, bag_constraints );

    bag_constraints.gridx = 0;
    bag_constraints.gridwidth = 1;
    bag_constraints.weightx = 0.4;
    ++bag_constraints.gridy;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    add( new Label( "ODE Variables:", Label.RIGHT ), bag_constraints );
    var_choices = new Choice();
    var_choices.setBackground( Color.cyan );
    var_choices.setForeground( Color.black );
    for (int i = 0; i <=
        MakeMakeOptions.GUI_LEVEL_OPTVAR_LIMITS[options.gui_level]; ++i)
      var_choices.add( options.optvar_names[i] );
    var_choices.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { desc_label.setText( options.optvar_comments[
        var_choices.getSelectedIndex()] );
        populateLists();
        populateTargetChoices();
        } } );
    bag_constraints.gridx = 1;
    bag_constraints.gridwidth = GridBagConstraints.REMAINDER;
    bag_constraints.weightx = 0.6;
    bag_constraints.insets = new Insets( 2, 2, 0, 10 );
    add( var_choices, bag_constraints );

    desc_label = new Label( options.optvar_comments[0], Label.CENTER );
    desc_label.setFont( new Font( "Dialog", Font.BOLD, 12 ) );
    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.insets = new Insets( 2, 10, 0, 10 );
    bag_constraints.weightx = 1.0;
    add( desc_label, bag_constraints );

    preview_button = new Button( "PREVIEW" );
    setButtonAttributes( preview_button, 14 );
    preview_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { preview(); } } );
    ++bag_constraints.gridy;
    bag_constraints.fill = GridBagConstraints.NONE;
    add( preview_button, bag_constraints );

    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.gridwidth = 1;
    bag_constraints.weightx = 0.4;
    bag_constraints.fill = GridBagConstraints.HORIZONTAL;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    add( new Label( "Available values", Label.CENTER ), bag_constraints );
    bag_constraints.gridx = 2;
    bag_constraints.insets = new Insets( 2, 2, 0, 10 );
    add( new Label( "Current values", Label.CENTER ), bag_constraints );

    avail_list = new List( LIST_SIZE, true );
    avail_list.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { updateCustomValField( custom_field, e ); } } );
    curr_list = new List( LIST_SIZE, true );
    curr_list.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { updateCustomValField( custom_field, e ); } } );
    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    bag_constraints.gridheight = LIST_SIZE;
    bag_constraints.weighty = 1.0;
    bag_constraints.fill = GridBagConstraints.BOTH;
    //bag_constraints.gridheight = GridBagConstraints.REMAINDER;
    add( avail_list, bag_constraints );
    bag_constraints.gridx = 2;
    bag_constraints.insets = new Insets( 2, 2, 0, 10 );
    add( curr_list, bag_constraints );

    int list_y = bag_constraints.gridy;

    add_button = new Button( "ADD >" );
    del_button = new Button( "REMOVE <" );
    addall_button = new Button( "ADD ALL >>>" );
    delall_button = new Button( "REMOVE ALL <<<" );
    setButtonAttributes( add_button );
    setButtonAttributes( del_button );
    setButtonAttributes( addall_button );
    setButtonAttributes( delall_button );
    add_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { addSelectedAvailable( avail_list, curr_list );
        addCustom( custom_field, curr_list ); 
        addCurrentVals();
        } } );
    del_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { removeSelectedCurrent( curr_list );
        addCurrentVals();
        } } );
    addall_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { addAllAvailable( avail_list, curr_list );
        addCurrentVals();
        } } );
    delall_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { removeAllCurrent( curr_list );
        addCurrentVals();
        } } );
    bag_constraints.gridx = 1;
    bag_constraints.gridwidth = 1;
    bag_constraints.gridheight = 1;
    bag_constraints.weighty = 0.0;
    bag_constraints.fill = GridBagConstraints.HORIZONTAL;
    bag_constraints.weightx = 0.2;
    bag_constraints.insets = new Insets( 2, 2, 0, 2 );
    add( add_button, bag_constraints );
    ++bag_constraints.gridy;
    add( del_button, bag_constraints );
    ++bag_constraints.gridy;
    add( addall_button, bag_constraints );
    ++bag_constraints.gridy;
    add( delall_button, bag_constraints );

    bag_constraints.gridy = list_y + LIST_SIZE;

    if (options.gui_level > MakeMakeOptions.NOVICE_GUI)
    {
      bag_constraints.gridx = 0;
      bag_constraints.gridwidth = 1;
      bag_constraints.gridheight = 1;
      bag_constraints.weightx = 0.4;
      bag_constraints.insets = new Insets( 2, 10, 0, 2 );
      add( new Label( "Custom value", Label.CENTER ), bag_constraints );

      custom_field = new TextField( "", 1 ); // size expanded by layout
      setFieldAttributes( custom_field );
      custom_field.addKeyListener( new KeyAdapter()
          { public void keyPressed( KeyEvent e )
          { if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          addCustom( custom_field, curr_list );
          addCurrentVals(); }
          } } );
      ++bag_constraints.gridy;
      bag_constraints.insets = new Insets( 2, 10, 5, 2 );
      add( custom_field, bag_constraints );

      if (options.gui_level > MakeMakeOptions.INTERMEDIATE_GUI)
      {
        advanced_window_button = new Button( "Advanced..." );
        setButtonAttributes( advanced_window_button );
        advanced_window_button.addActionListener( new ActionListener()
            { public void actionPerformed( ActionEvent e )
            { advanced_window.setVisible( true ); } } );
        bag_constraints.gridx = 2;
        bag_constraints.gridwidth = 1;
        bag_constraints.gridheight = 1;
        bag_constraints.weightx = 0.4;
        bag_constraints.fill = GridBagConstraints.NONE;
        bag_constraints.insets = new Insets( 2, 2, 0, 10 );
        add( advanced_window_button, bag_constraints );
      }
    }

    addHorizLine( this, bag_constraints );

    if (options.gui_level > MakeMakeOptions.INTERMEDIATE_GUI)
      addAdvancedComponents();
      
    abort_button = new Button( "ABORT" );
    setButtonAttributes( abort_button, 18 );
    abort_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { abortion(); } } );
    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.gridwidth = 1;
    bag_constraints.weightx = 0.4;
    bag_constraints.fill = GridBagConstraints.NONE;
    bag_constraints.insets = new Insets( 10, 10, 10, 2 );
    add( abort_button, bag_constraints );

    skip_button = new Button( "SKIP" );
    setButtonAttributes( skip_button, 18 );
    // note: getModifiers() is broken in ActionEvent, so use MouseEvent
    skip_button.addMouseListener( new MouseAdapter()
        { public void mouseReleased( MouseEvent e )
        { e.translatePoint( e.getComponent().getLocation().x,
        e.getComponent().getLocation().y );
        if (e.getComponent().getBounds().contains( e.getPoint() ))
        skip( e.isControlDown() ); } } );
    bag_constraints.gridx = 1;
    bag_constraints.weightx = 0.2;
    bag_constraints.insets = new Insets( 10, 2, 10, 2 );
    add( skip_button, bag_constraints );

    done_button = new Button( "SAVE" );
    setButtonAttributes( done_button, 18 );
    // note: getModifiers() is broken in ActionEvent, so use MouseEvent
    done_button.addMouseListener( new MouseAdapter()
        { public void mouseReleased( MouseEvent e )
        { e.translatePoint( e.getComponent().getLocation().x,
        e.getComponent().getLocation().y );
        if (e.getComponent().getBounds().contains( e.getPoint() ))
        complete( e.isControlDown() ); } } );
    bag_constraints.gridx = 2;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 10, 2, 10, 10 );
    add( done_button, bag_constraints );

    populateLists();
    populateTargetChoices();

    pack();
    addWindowListener( new WindowAdapter()
        { public void windowClosing( WindowEvent e )
        { abortion(); }
        public void windowOpened( WindowEvent e )
        { var_choices.requestFocus(); } } );
    setVisible( true );
  }


  private void initTempData()
  {
    this.new_optvar_vals = new String[options.optvar_vals.length];
    for (int i = 0; i < options.optvar_vals.length; ++i)
      new_optvar_vals[i] = options.optvar_vals[i];
  }


  private void addAdvancedComponents()
  {
    advanced_window = new Frame( "Advanced Verification Control" );
    advanced_window.setBackground( SystemColor.window );
    advanced_window.setForeground( SystemColor.windowText );
    advanced_window.setFont( new Font( "Dialog", Font.PLAIN, 12 ) );
    advanced_window.addWindowListener( new WindowAdapter()
        { public void windowClosing( WindowEvent e )
        { advanced_window.setVisible( false ); } } );
    Point winloc = frame.getLocationOnScreen();
    advanced_window.setLocation( winloc.x + 50, winloc.y + 50 );
    advanced_window.setLayout( new GridBagLayout() );
    GridBagConstraints bag_constraints = new GridBagConstraints();
    bag_constraints.gridx = 0;
    bag_constraints.gridy = 0;
    bag_constraints.ipadx = 1;
    bag_constraints.ipady = 1;
    bag_constraints.gridwidth = 1;
    bag_constraints.weightx = 0.4;
    bag_constraints.fill = GridBagConstraints.HORIZONTAL;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    advanced_window.add( new Label( "Target-specific variables:",
        Label.RIGHT ), bag_constraints );
    targvar_choices = new Choice();
    targvar_choices.setBackground( Color.cyan );
    targvar_choices.setForeground( Color.black );
    targvar_choices.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { populateTargetLists(); } } );
    bag_constraints.gridx = 1;
    bag_constraints.gridwidth = GridBagConstraints.REMAINDER;
    bag_constraints.weightx = 0.6;
    bag_constraints.insets = new Insets( 2, 2, 0, 10 );
    advanced_window.add( targvar_choices, bag_constraints );

    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.gridwidth = 1;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    advanced_window.add( new Label( "Available values", Label.CENTER ),
        bag_constraints );
    bag_constraints.gridx = 2;
    bag_constraints.insets = new Insets( 2, 2, 0, 10 );
    advanced_window.add( new Label( "Current values", Label.CENTER ),
        bag_constraints );

    targavail_list = new List( LIST_SIZE, true );
    targavail_list.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { updateCustomValField( targcustom_field, e ); } } );
    targcurr_list = new List( LIST_SIZE, true );
    targcurr_list.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { updateCustomValField( targcustom_field, e ); } } );
    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    bag_constraints.gridheight = LIST_SIZE;
    bag_constraints.weighty = 1.0;
    bag_constraints.fill = GridBagConstraints.BOTH;
    advanced_window.add( targavail_list, bag_constraints );
    bag_constraints.gridx = 2;
    bag_constraints.insets = new Insets( 2, 2, 0, 10 );
    advanced_window.add( targcurr_list, bag_constraints );

    int list_y = bag_constraints.gridy;

    targadd_button = new Button( "ADD >" );
    targdel_button = new Button( "REMOVE <" );
    targaddall_button = new Button( "ADD ALL >>>" );
    targdelall_button = new Button( "REMOVE ALL <<<" );
    setButtonAttributes( targadd_button );
    setButtonAttributes( targdel_button );
    setButtonAttributes( targaddall_button );
    setButtonAttributes( targdelall_button );
    targadd_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { addSelectedAvailable( targavail_list, targcurr_list );
        addCustom( targcustom_field, targcurr_list );
        addCurrentTargVals();
        } } );
    targdel_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { removeSelectedCurrent( targcurr_list );
        addCurrentTargVals();
        } } );
    targaddall_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { addAllAvailable( targavail_list, targcurr_list );
        addCurrentTargVals();
        } } );
    targdelall_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { removeAllCurrent( targcurr_list );
        addCurrentTargVals();
        } } );
    bag_constraints.gridx = 1;
    bag_constraints.gridwidth = 1;
    bag_constraints.gridheight = 1;
    bag_constraints.weighty = 0.0;
    bag_constraints.fill = GridBagConstraints.HORIZONTAL;
    bag_constraints.weightx = 0.2;
    bag_constraints.insets = new Insets( 2, 2, 0, 2 );
    advanced_window.add( targadd_button, bag_constraints );
    ++bag_constraints.gridy;
    advanced_window.add( targdel_button, bag_constraints );
    ++bag_constraints.gridy;
    advanced_window.add( targaddall_button, bag_constraints );
    ++bag_constraints.gridy;
    advanced_window.add( targdelall_button, bag_constraints );

    bag_constraints.gridy = list_y + LIST_SIZE;

    bag_constraints.gridx = 0;
    bag_constraints.gridwidth = 1;
    bag_constraints.gridheight = 1;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    advanced_window.add( new Label( "Custom value", Label.CENTER ),
        bag_constraints );

    targcustom_field = new TextField( "", 1 ); // size expanded by layout
    setFieldAttributes( targcustom_field );
    targcustom_field.addKeyListener( new KeyAdapter()
        { public void keyPressed( KeyEvent e )
        { if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        addCustom( targcustom_field, targcurr_list );
        addCurrentTargVals(); }
        } } );
    ++bag_constraints.gridy;
    bag_constraints.insets = new Insets( 2, 10, 5, 2 );
    advanced_window.add( targcustom_field, bag_constraints );

    addHorizLine( advanced_window, bag_constraints );

    addCustomVarComponents( bag_constraints );
    advanced_window.pack();
  }


  private void addCustomVarComponents( GridBagConstraints bag_constraints )
  {
    bag_constraints.gridx = 0;
    bag_constraints.gridwidth = 1;
    bag_constraints.weightx = 0.4;
    ++bag_constraints.gridy;
    bag_constraints.fill = GridBagConstraints.HORIZONTAL;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    advanced_window.add( new Label( "Custom variables:", Label.RIGHT ),
        bag_constraints );
    customvar_choices = new Choice();
    customvar_choices.setBackground( Color.cyan );
    customvar_choices.setForeground( Color.black );
    for (int i = 0; i < options.customvar_names.size(); ++i)
      customvar_choices.add( (String)options.customvar_names.elementAt( i ) );
    customvar_choices.setEnabled( options.customvar_names.size() > 0 );
    customvar_choices.addItemListener( new ItemListener()
        { public void itemStateChanged( ItemEvent e )
        { populateCustomFields(); } } );
    bag_constraints.gridx = 1;
    bag_constraints.gridwidth = GridBagConstraints.REMAINDER;
    bag_constraints.weightx = 0.6;
    bag_constraints.insets = new Insets( 2, 2, 0, 10 );
    advanced_window.add( customvar_choices, bag_constraints );

    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.weightx = 0.4;
    bag_constraints.gridwidth = 1;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    advanced_window.add( new Label( "Name", Label.CENTER ), bag_constraints );
    bag_constraints.gridx = 1;
    bag_constraints.weightx = 0.2;
    bag_constraints.insets = new Insets( 2, 2, 0, 2 );
    advanced_window.add( new Label( "Value", Label.CENTER ), bag_constraints );
    bag_constraints.gridx = 2;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 2, 2, 0, 10 );
    advanced_window.add( new Label( "Comment", Label.CENTER ),
        bag_constraints );

    customvar_name_field = new TextField( "", 1 ); // size expanded by layout
    setFieldAttributes( customvar_name_field );
    customvar_name_field.addKeyListener( new KeyAdapter()
        { public void keyPressed( KeyEvent e )
        { if (e.getKeyCode() == KeyEvent.VK_ENTER)
        customvar_name_field.transferFocus(); 
        else if (e.getKeyCode() == KeyEvent.VK_SPACE)
        e.consume();
        } } );
    customvar_val_field = new TextField( "", 1 ); // size expanded by layout
    setFieldAttributes( customvar_val_field );
    customvar_val_field.addKeyListener( new KeyAdapter()
        { public void keyPressed( KeyEvent e )
        { if (e.getKeyCode() == KeyEvent.VK_ENTER)
        customvar_val_field.transferFocus(); } } );
    customvar_comment_field = new TextField( "", 1 ); // size expanded by layout
    setFieldAttributes( customvar_comment_field );
    customvar_comment_field.addKeyListener( new KeyAdapter()
        { public void keyPressed( KeyEvent e )
        { if (e.getKeyCode() == KeyEvent.VK_ENTER)
        customvar_comment_field.transferFocus(); } } );
    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    advanced_window.add( customvar_name_field, bag_constraints );
    bag_constraints.gridx = 1;
    bag_constraints.weightx = 0.2;
    bag_constraints.insets = new Insets( 2, 2, 0, 2 );
    advanced_window.add( customvar_val_field, bag_constraints );
    bag_constraints.gridx = 2;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 2, 2, 0, 10 );
    advanced_window.add( customvar_comment_field, bag_constraints );

    bag_constraints.gridx = 1;
    ++bag_constraints.gridy;
    bag_constraints.weightx = 1.0;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    mfinclude_chkbox = new Checkbox( "Include in makefile", false );
    advanced_window.add( mfinclude_chkbox, bag_constraints );

    bag_constraints.gridx = 1;
    ++bag_constraints.gridy;
    bag_constraints.weightx = 1.0;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    advanced_window.add( new Label( "Assignment type:", Label.CENTER ),
        bag_constraints );

    assignments_chkboxgrp = new CheckboxGroup();
    assignments_chkbox = new Checkbox[6];
    assignments_chkbox[0] = new Checkbox( MakeMakeOptions.ASSIGNMENT_TYPES[0],
        false, assignments_chkboxgrp );
    assignments_chkbox[1] = new Checkbox( MakeMakeOptions.ASSIGNMENT_TYPES[1],
        false, assignments_chkboxgrp );
    assignments_chkbox[2] = new Checkbox( MakeMakeOptions.ASSIGNMENT_TYPES[2],
        false, assignments_chkboxgrp );
    assignments_chkbox[3] = new Checkbox( MakeMakeOptions.ASSIGNMENT_TYPES[3],
        false, assignments_chkboxgrp );
    assignments_chkbox[4] = new Checkbox( MakeMakeOptions.ASSIGNMENT_TYPES[4],
        false, assignments_chkboxgrp );
    assignments_chkbox[5] = new Checkbox( MakeMakeOptions.ASSIGNMENT_TYPES[5],
        false, assignments_chkboxgrp );
    assignments_chkboxgrp.setSelectedCheckbox(
        assignments_chkbox[MakeMakeOptions.DEFAULT_ASSIGNMENT_TYPE_INDEX] );
    bag_constraints.gridx = 0;
    bag_constraints.weightx = 0.4;
    ++bag_constraints.gridy;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    advanced_window.add( assignments_chkbox[0], bag_constraints );
    ++bag_constraints.gridx;
    bag_constraints.weightx = 0.2;
    bag_constraints.insets = new Insets( 2, 2, 0, 2 );
    advanced_window.add( assignments_chkbox[1], bag_constraints );
    ++bag_constraints.gridx;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 2, 2, 0, 10 );
    advanced_window.add( assignments_chkbox[2], bag_constraints );
    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.insets = new Insets( 2, 10, 0, 2 );
    advanced_window.add( assignments_chkbox[3], bag_constraints );
    ++bag_constraints.gridx;
    bag_constraints.weightx = 0.2;
    bag_constraints.insets = new Insets( 2, 2, 0, 2 );
    advanced_window.add( assignments_chkbox[4], bag_constraints );
    ++bag_constraints.gridx;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 2, 2, 0, 10 );
    advanced_window.add( assignments_chkbox[5], bag_constraints );

    customvar_add_button = new Button( "ADD/REPLACE" );
    customvar_clear_button = new Button( "CLEAR" );
    customvar_del_button = new Button( "REMOVE" );
    setButtonAttributes( customvar_add_button );
    setButtonAttributes( customvar_clear_button );
    setButtonAttributes( customvar_del_button );
    customvar_add_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { addCustomVar();
        } } );
    customvar_clear_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { clearCustomVarFields();
        } } );
    customvar_del_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { removeCustomVar();
        } } );
    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.fill = GridBagConstraints.NONE;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 10, 10, 10, 2 );
    advanced_window.add( customvar_add_button, bag_constraints );
    bag_constraints.gridx = 1;
    bag_constraints.weightx = 0.2;
    bag_constraints.insets = new Insets( 10, 2, 10, 2 );
    advanced_window.add( customvar_clear_button, bag_constraints );
    bag_constraints.gridx = 2;
    bag_constraints.weightx = 0.4;
    bag_constraints.insets = new Insets( 10, 2, 10, 10 );
    advanced_window.add( customvar_del_button, bag_constraints );
  }


  private void addCurrentVals()
  {
    new_optvar_vals[var_choices.getSelectedIndex()] = getItems( curr_list );
    populateTargetChoices();
  }


  private void addCurrentTargVals()
  {
    int index = options.targvar_names.indexOf(
        targvar_choices.getSelectedItem() );
    if (index >= 0)
      options.targvar_vals.setElementAt( getItems( targcurr_list ), index );
  }


  private void setButtonAttributes( Button button )
  {
    setButtonAttributes( button, 12 );
  }


  private void setButtonAttributes( Button button, int font_size )
  {
    button.setBackground( SystemColor.control );
    button.setForeground( SystemColor.controlText );
    button.setFont( new Font( "SansSerif", Font.BOLD, font_size ) );
  }


  private void setFieldAttributes( TextField field )
  {
    field.setBackground( Color.white );
    field.setForeground( Color.black );
    field.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
  }


  private void addHorizLine( Container container,
      GridBagConstraints bag_constraints )
  {
    bag_constraints.gridx = 0;
    ++bag_constraints.gridy;
    bag_constraints.gridwidth = GridBagConstraints.REMAINDER;
    bag_constraints.fill = GridBagConstraints.HORIZONTAL;
    bag_constraints.weightx = 1.0;
    bag_constraints.insets = new Insets( 5, 0, 0, 0 );
    container.add( new MakeMakeCanvas(), bag_constraints );
  }


  private void populateCustomFields()
  {
    int index = options.customvar_names.indexOf(
        customvar_choices.getSelectedItem() );
    if (index >= 0)
    {
      customvar_name_field.setText(
          (String)options.customvar_names.elementAt( index ) );
      customvar_val_field.setText(
          (String)options.customvar_vals.elementAt( index ) );
      customvar_comment_field.setText(
          (String)options.customvar_comments.elementAt( index ) );
      mfinclude_chkbox.setState(
          ((Boolean)options.customvar_mfinclude.elementAt(
              index )).booleanValue() );
      assignments_chkboxgrp.setSelectedCheckbox(
          assignments_chkbox[((Integer)options.customvar_assignment.elementAt(
              index )).intValue()] );
    }
    else
      clearCustomVarFields();
  }


  private void populateTargetLists()
  {
    if (options.gui_level <= MakeMakeOptions.INTERMEDIATE_GUI)
      return;
    if (targavail_list.isEnabled())
    {
      if (targavail_list.getItemCount() > 0)
        targavail_list.removeAll();
      if (targcurr_list.getItemCount() > 0)
        targcurr_list.removeAll();
      int index = var_choices.getSelectedIndex();
      addItems( targavail_list, StringTools.split(
          new_optvar_vals[index], ' ' ) );
      addItems( targavail_list, StringTools.split(
          options.optvar_vals[index], ' ' ) );

      if ((index == MakeMakeOptions.OFILES_INDEX && !options.use_ofiles) ||
          (index == MakeMakeOptions.OBJECTS_INDEX && options.use_ofiles))
      {
        index = (index == MakeMakeOptions.OFILES_INDEX) ?
            MakeMakeOptions.OBJECTS_INDEX : MakeMakeOptions.OFILES_INDEX;
        addItems( targavail_list, StringTools.split(
            new_optvar_vals[index], ' ' ) );
        addItems( targavail_list, StringTools.split(
            options.optvar_vals[index], ' ' ) );
      }

      index = options.targvar_names.indexOf(
          targvar_choices.getSelectedItem() );
      if (index >= 0)
        addItems( targcurr_list, StringTools.split(
            (String)options.targvar_vals.elementAt( index ), ' ' ) );
    }
  }


  private void populateTargetChoices()
  {
    if (options.gui_level <= MakeMakeOptions.INTERMEDIATE_GUI)
      return;
    targvar_choices.removeAll();
    int index = var_choices.getSelectedIndex();

    String append_str = "";
    String[] vararr;
    Vector vars = new Vector();

    if (index == MakeMakeOptions.OFILES_INDEX ||
        index == MakeMakeOptions.EXTRA_LIBS_INDEX ||
        index == MakeMakeOptions.EXTRA_OFILES_INDEX)
    {
      if (index == MakeMakeOptions.OFILES_INDEX)
        append_str = "_OFILES";
      else if (index == MakeMakeOptions.EXTRA_LIBS_INDEX)
        append_str = "_EXTRA_LIBS";
      else
        append_str = "_EXTRA_OFILES";
      vararr = appendToArray( StringTools.split(
          new_optvar_vals[MakeMakeOptions.PROGRAMS_INDEX], ' ' ), append_str );
      for (int i = 0; i < vararr.length; ++i)
        vars.addElement( vararr[i] );
      vararr = appendToArray( StringTools.split(
          new_optvar_vals[MakeMakeOptions.LIBRARIES_INDEX], ' ' ), append_str );
      for (int i = 0; i < vararr.length; ++i)
        vars.addElement( vararr[i] );
      vararr = appendToArray( StringTools.split(
          new_optvar_vals[MakeMakeOptions.SHARED_LIBRARIES_INDEX], ' ' ),
          append_str );
      for (int i = 0; i < vararr.length; ++i)
        vars.addElement( vararr[i] );
    }
    else if (index == MakeMakeOptions.CFLAGS_INDEX)
    {
      append_str = "_CFLAGS";
      int idx = (options.use_ofiles) ? MakeMakeOptions.OFILES_INDEX :
          MakeMakeOptions.OBJECTS_INDEX;
      vararr = appendToArray( StringTools.split(
          options.optvar_vals[idx], ' ' ), append_str );
      for (int i = 0; i < vararr.length; ++i)
        vars.addElement( vararr[i] );
      vararr = appendToArray( StringTools.split(
          new_optvar_vals[MakeMakeOptions.OFILES_INDEX], ' ' ), append_str );
      for (int i = 0; i < vararr.length; ++i)
        vars.addElement( vararr[i] );
      vararr = appendToArray( StringTools.split(
          new_optvar_vals[MakeMakeOptions.OBJECTS_INDEX], ' ' ), append_str );
      for (int i = 0; i < vararr.length; ++i)
        vars.addElement( vararr[i] );
    }
    else if (index == MakeMakeOptions.LDFLAGS_INDEX)
    {
      append_str = "_LDFLAGS";
      vararr = appendToArray( StringTools.split(
          new_optvar_vals[MakeMakeOptions.PROGRAMS_INDEX], ' ' ), append_str );
      for (int i = 0; i < vararr.length; ++i)
        vars.addElement( vararr[i] );
    }
    else if (index == MakeMakeOptions.SHLDFLAGS_INDEX)
    {
      append_str = "_SHLDFLAGS";
      vararr = appendToArray( StringTools.split(
          new_optvar_vals[MakeMakeOptions.SHARED_LIBRARIES_INDEX], ' ' ),
          append_str );
      for (int i = 0; i < vararr.length; ++i)
        vars.addElement( vararr[i] );
    }
    else if (index == MakeMakeOptions.LIBS_INDEX)
    {
      append_str = "_LIBS";
      vararr = appendToArray( StringTools.split(
          new_optvar_vals[MakeMakeOptions.PROGRAMS_INDEX], ' ' ), append_str );
      for (int i = 0; i < vararr.length; ++i)
        vars.addElement( vararr[i] );
      vararr = appendToArray( StringTools.split(
          new_optvar_vals[MakeMakeOptions.SHARED_LIBRARIES_INDEX], ' ' ),
          append_str );
      for (int i = 0; i < vararr.length; ++i)
        vars.addElement( vararr[i] );
    }

    if (vars.size() < 1)
    {
      setTargComponentsEnabled( false );
      return;
    }

    for (int i = 0; i < options.targvar_names.size(); ++i)
    {
      if (((String)options.targvar_names.elementAt( i )).endsWith(
          append_str ) &&
          !vars.contains( (String)options.targvar_names.elementAt( i ) ))
      {
        if (((String)options.targvar_vals.elementAt( i )).length() < 1)
        {
          options.targvar_names.removeElementAt( i );
          options.targvar_vals.removeElementAt( i );
          --i;
        }
        else
          targvar_choices.add( (String)options.targvar_names.elementAt( i ) );
      }
    }

    for (int i = 0; i < vars.size(); ++i)
    {
      targvar_choices.add( (String)vars.elementAt( i ) );
      if (!options.targvar_names.contains( (String)vars.elementAt( i ) ))
      {
        options.targvar_names.addElement( vars.elementAt( i ) );
        options.targvar_vals.addElement( "" );
      }
    }
    setTargComponentsEnabled( true );
    populateTargetLists();
  }


  private void populateLists()
  {
    if (avail_list.getItemCount() > 0)
      avail_list.removeAll();
    if (curr_list.getItemCount() > 0)
      curr_list.removeAll();

    int index = var_choices.getSelectedIndex();

    // include what's already current in the available list
    addItems( avail_list, StringTools.split(
        options.optvar_vals[index], ' ' ) );

    if (index == MakeMakeOptions.OFILES_INDEX && !options.use_ofiles)
    {
      addItems( avail_list, StringTools.split(
          options.optvar_vals[MakeMakeOptions.OBJECTS_INDEX], ' ' ) );
    }
    else if (index == MakeMakeOptions.OBJECTS_INDEX && options.use_ofiles)
    {
      addItems( avail_list, StringTools.split(
          options.optvar_vals[MakeMakeOptions.OFILES_INDEX], ' ' ) );
    }
    else if (index == MakeMakeOptions.PROGRAMS_INDEX)
    {
      addItems( avail_list, StringTools.split(
          StringTools.replace(
          options.optvar_vals[MakeMakeOptions.OFILES_INDEX],
          "${OBJ_SUFF}", "${PROG_SUFF}", -1 ), ' ' ) );
      addItems( avail_list, StringTools.split(
          StringTools.replace(
          options.optvar_vals[MakeMakeOptions.OBJECTS_INDEX],
          "${OBJ_SUFF}", "${PROG_SUFF}", -1 ), ' ' ) );
      addItems( avail_list, StringTools.split(
          StringTools.replace(
          new_optvar_vals[MakeMakeOptions.OFILES_INDEX],
          "${OBJ_SUFF}", "${PROG_SUFF}", -1 ), ' ' ) );
      addItems( avail_list, StringTools.split(
          StringTools.replace(
          new_optvar_vals[MakeMakeOptions.OBJECTS_INDEX],
          "${OBJ_SUFF}", "${PROG_SUFF}", -1 ), ' ' ) );
      addItem( avail_list, Path.fileName( Path.filePath(
          options.makefile_path.toString() ) ) + "${PROG_SUFF}" );
    }
    else if (index == MakeMakeOptions.LIBRARIES_INDEX)
    {
      addItems( avail_list, prependLibPref( StringTools.split(
          StringTools.replace(
          options.optvar_vals[MakeMakeOptions.OFILES_INDEX],
          "${OBJ_SUFF}", "${STATLIB_SUFF}", -1 ), ' ' ) ) );
      addItems( avail_list, prependLibPref( StringTools.split(
          StringTools.replace(
          options.optvar_vals[MakeMakeOptions.OBJECTS_INDEX],
          "${OBJ_SUFF}", "${STATLIB_SUFF}", -1 ), ' ' ) ) );
      addItems( avail_list, prependLibPref( StringTools.split(
          StringTools.replace(
          new_optvar_vals[MakeMakeOptions.OFILES_INDEX],
          "${OBJ_SUFF}", "${STATLIB_SUFF}", -1 ), ' ' ) ) );
      addItems( avail_list, prependLibPref( StringTools.split(
          StringTools.replace(
          new_optvar_vals[MakeMakeOptions.OBJECTS_INDEX],
          "${OBJ_SUFF}", "${STATLIB_SUFF}", -1 ), ' ' ) ) );
      addItem( avail_list, "${LIB_PREF}" + Path.fileName( Path.filePath(
          options.makefile_path.toString() ) ) + "${STATLIB_SUFF}" );
    }
    else if (index == MakeMakeOptions.SHARED_LIBRARIES_INDEX)
    {
      addItems( avail_list, prependLibPref( StringTools.split(
          StringTools.replace(
          options.optvar_vals[MakeMakeOptions.OFILES_INDEX],
          "${OBJ_SUFF}", "${SHLIB_SUFF}", -1 ), ' ' ) ) );
      addItems( avail_list, prependLibPref( StringTools.split(
          StringTools.replace(
          options.optvar_vals[MakeMakeOptions.OBJECTS_INDEX],
          "${OBJ_SUFF}", "${SHLIB_SUFF}", -1 ), ' ' ) ) );
      addItems( avail_list, prependLibPref( StringTools.split(
          StringTools.replace(
          new_optvar_vals[MakeMakeOptions.OFILES_INDEX],
          "${OBJ_SUFF}", "${SHLIB_SUFF}", -1 ), ' ' ) ) );
      addItems( avail_list, prependLibPref( StringTools.split(
          StringTools.replace(
          new_optvar_vals[MakeMakeOptions.OBJECTS_INDEX],
          "${OBJ_SUFF}", "${SHLIB_SUFF}", -1 ), ' ' ) ) );
      addItem( avail_list, "${LIB_PREF}" + Path.fileName( Path.filePath(
          options.makefile_path.toString() ) ) + "${SHLIB_SUFF}" );
    }
    else if (index == MakeMakeOptions.ILIST_INDEX)
    {
      addItems( avail_list, StringTools.split(
          new_optvar_vals[MakeMakeOptions.PROGRAMS_INDEX], ' ' ) );
      addItems( avail_list, StringTools.split(
          new_optvar_vals[MakeMakeOptions.OBJECTS_INDEX], ' ' ) );
      addItems( avail_list, StringTools.split(
          new_optvar_vals[MakeMakeOptions.LIBRARIES_INDEX], ' ' ) );
      addItems( avail_list, StringTools.split(
          new_optvar_vals[MakeMakeOptions.SHARED_LIBRARIES_INDEX], ' ' ) );
    }
    else if (index == MakeMakeOptions.EXPLIB_TARGETS_INDEX)
    {
      addItems( avail_list, StringTools.split(
          new_optvar_vals[MakeMakeOptions.LIBRARIES_INDEX], ' ' ) );
      addItems( avail_list, prependLibPref( StringTools.split(
          StringTools.replace(
          new_optvar_vals[MakeMakeOptions.SHARED_LIBRARIES_INDEX],
          "${SHLIB_SUFF}", "${IMPLIB_SUFF}", -1 ), ' ' ) ) );
    }
    else if (index == MakeMakeOptions.IDIR_INDEX)
    {
      addItem( avail_list, MakeMakeOptions.DEFAULT_IDIR );
    }
    else if (index == MakeMakeOptions.EXPDIR_INDEX)
    {
      addItem( avail_list, MakeMakeOptions.DEFAULT_EXPDIR );
    }
    else if (index == MakeMakeOptions.EXPINCDIR_INDEX)
    {
      addItem( avail_list, MakeMakeOptions.DEFAULT_EXPINCDIR );
    }
    else if (index == MakeMakeOptions.EXPINCTOP_INDEX)
    {
      addItem( avail_list, MakeMakeOptions.DEFAULT_EXPINCTOP );
    }
    else if (index == MakeMakeOptions.EXPLIBTOP_INDEX)
    {
      addItem( avail_list, MakeMakeOptions.DEFAULT_EXPLIBTOP );
    }
    else if (index == MakeMakeOptions.LIB_SUFF_INDEX)
    {
      addItem( avail_list, "${STATLIB_SUFF}" );
      addItem( avail_list, "${IMPLIB_SUFF}" );
    }
    else if (index == MakeMakeOptions.SUBDIRS_INDEX ||
        index == MakeMakeOptions.EXPINC_SUBDIRS_INDEX ||
        index == MakeMakeOptions.EXPLIB_SUBDIRS_INDEX ||
        index == MakeMakeOptions.OBJECTS_SUBDIRS_INDEX)
    {
      addItems( avail_list, StringTools.split(
          options.available_subdirs, ' ' ) );
    }

    addItems( curr_list, StringTools.split( new_optvar_vals[
        var_choices.getSelectedIndex()], ' ' ) );
  }


  private void clearCustomVarFields()
  {
    if (options.gui_level <= MakeMakeOptions.INTERMEDIATE_GUI)
      return;
    customvar_name_field.setText( "" );
    customvar_val_field.setText( "" );
    customvar_comment_field.setText( "" );
    mfinclude_chkbox.setState( true );
  }


  private boolean removeCustomVar()
  {
    boolean rc = false;
    String var = customvar_name_field.getText().trim();
    if (var.length() > 0)
    {
      int index = options.customvar_names.indexOf( var );
      if (index >= 0)
      {
        options.customvar_names.removeElementAt( index );
        options.customvar_vals.removeElementAt( index );
        options.customvar_comments.removeElementAt( index );
        options.customvar_assignment.removeElementAt( index );
        options.customvar_mfinclude.removeElementAt( index );
        customvar_choices.remove( var );
        clearCustomVarFields();
        if (customvar_choices.getItemCount() < 1)
          customvar_choices.setEnabled( false );
        rc = true;
      }
    }
    return (rc);
  }


  private boolean addCustomVar()
  {
    boolean rc = false;
    String var = customvar_name_field.getText().trim();
    if (var.length() > 0)
    {
      String val = customvar_val_field.getText().trim();
      String comment = customvar_comment_field.getText().trim();
      int index = options.customvar_names.indexOf( var );
      if (index >= 0)
      {
        options.customvar_vals.setElementAt( val, index );
        options.customvar_comments.setElementAt( comment, index );
        options.customvar_assignment.setElementAt(
            new Integer( getAssignmentChkboxIndex(
                assignments_chkboxgrp.getSelectedCheckbox().getLabel() ) ),
            index );
        options.customvar_mfinclude.setElementAt(
            new Boolean( mfinclude_chkbox.getState() ), index );
      }
      else
      {
        options.customvar_names.addElement( var );
        options.customvar_vals.addElement( val );
        options.customvar_comments.addElement( comment );
        options.customvar_assignment.addElement(
            new Integer( getAssignmentChkboxIndex(
                assignments_chkboxgrp.getSelectedCheckbox().getLabel() ) ) );
        options.customvar_mfinclude.addElement(
            new Boolean( mfinclude_chkbox.getState() ) );
        customvar_choices.add( var );
        customvar_choices.setEnabled( true );
      }
      clearCustomVarFields();
    }
    return (rc);
  }


  private int getAssignmentChkboxIndex( String label )
  {
    for (int i = 0; i < MakeMakeOptions.ASSIGNMENT_TYPES.length; ++i)
      if (MakeMakeOptions.ASSIGNMENT_TYPES[i] == label)
        return (i);
    return (MakeMakeOptions.DEFAULT_ASSIGNMENT_TYPE_INDEX);
  }


  private String[] appendToArray( String[] array, String append_str )
  {
    if (array == null)
      return (new String[0]);
    for (int i = 0; i < array.length; ++i)
      if (!array[i].endsWith( append_str ))
        array[i] += append_str;
    return (array);
  }


  private String[] prependLibPref( String[] array )
  {
    if (array == null)
      return (new String[0]);
    for (int i = 0; i < array.length; ++i)
      if (array[i].endsWith( "LIB_SUFF}" )) // catches both LIB and SHLIB
        if (!array[i].startsWith( "${LIB_PREF}" ))
          array[i] = "${LIB_PREF}" + array[i];
    return (array);
  }


  private boolean addItems( List list, String[] items )
  {
    boolean rc = false;

    if (items != null)
      for (int i = 0; i < items.length; ++i)
        if (addItem( list, items[i] ))
          rc = true;

    return (rc);
  }


  private boolean addCustom( TextField field, List list )
  {
    if (field != null)
    {
      String[] values = StringTools.split( field.getText().trim(), ' ' );
      field.setText( "" );
      return (addItems( list, values ));
    }
    return (false);
  }


  private void updateCustomValField( TextField field, ItemEvent e )
  {
    if (field != null)
    {
      int index = ((Integer)e.getItem()).intValue();
      List list = (List)e.getItemSelectable();
      String str = list.getItem( index );
      if (list.isIndexSelected( index ))
        field.setText( str );
      else if (field.getText().equals( str ))
        field.setText( "" );
    }
  }


  private boolean addItem( List list, String item )
  {
    if (item.length() > 0)
    {
      String[] items = list.getItems();
      for (int i = 0; i < items.length; ++i)
        if (items[i].equals( item ))
          return (false);
      list.add( item );
      return (true);
    }
    return (false);
  }


  private String getItems( List list )
  {
    return (StringTools.join( list.getItems(), " " ));
  }


  private void addAllAvailable( List from_list, List to_list )
  {
    for (int i = 0; i < from_list.getItemCount(); ++i)
      addItem( to_list, from_list.getItem( i ) );
  }


  private void addSelectedAvailable( List from_list, List to_list )
  {
    String[] items = from_list.getSelectedItems();
    if (items != null)
      for (int i = 0; i < items.length; ++i)
        addItem( to_list, items[i] );
  }


  private void removeAllCurrent( List list )
  {
    if (list.getItemCount() > 0)
      list.removeAll();
  }


  private void removeSelectedCurrent( List list )
  {
    String[] items = list.getSelectedItems();
    if (items != null)
      for (int i = 0; i < items.length; ++i)
        list.remove( items[i] );
  }


  private void setTargComponentsEnabled( boolean enable )
  {
    targvar_choices.setEnabled( enable );
    targavail_list.setEnabled( enable );
    targcurr_list.setEnabled( enable );
    targadd_button.setEnabled( enable );
    targaddall_button.setEnabled( enable );
    targdel_button.setEnabled( enable );
    targdelall_button.setEnabled( enable );
    targcustom_field.setEnabled( enable );
  }


  private void skip( boolean entire_subtree )
  {
    new_optvar_vals = null;
    complete( entire_subtree );
  }


  private void abortion()
  {
    getToolkit().beep();
    if (new MakeMakeYesNoDialog( this, "Confirm abort",
        "Are you sure you want to abort?" ).run())
    {
      error_printer.print( "PROCESSING ABORTED" );
      options.success = false;
      dispose();
    }
  }


  public void updateMakefilePath()
  {
    mfpath_label.setText( options.makefile_path.toString() );
    mfdate_label.setText( Path.getFileDate( options.makefile_path ) );
  }


  private void complete( boolean entire_subtree )
  {
    options.optvar_vals = new_optvar_vals;
    if (preview_text != null)
      preview_text.setText( "" );

    // write file and find out if there are more
    if (file_maker.moreDirs( entire_subtree ))
    {
      initTempData();
      updateMakefilePath();
      var_choices.select( 0 );
      populateLists();
      populateTargetChoices();
      clearCustomVarFields();
      if (preview_window != null && preview_window.isShowing())
        preview();
    }
    else
    {
      // note: we let MakeMakeFileMaker set options.success accordingly
      dispose();
    }
  }


  private void preview()
  {
    if (preview_window == null)
    {
      preview_window = new Frame( "Makefile Preview" );
      preview_window.setBackground( SystemColor.window );
      preview_window.setForeground( SystemColor.windowText );
      preview_window.setLayout( new GridLayout( 1, 1 ) );
      Point winloc = this.getLocationOnScreen();
      preview_window.setLocation( winloc.x + 20, winloc.y + 20 );
      preview_window.addWindowListener( new WindowAdapter()
          { public void windowClosing( WindowEvent e )
          { preview_window.setVisible( false ); } } );
      if (preview_text == null)
      {
        preview_text = new MakeMakeTextArea(
            "", 24, 80, TextArea.SCROLLBARS_BOTH );
        preview_text.setEditable( false );
      }
      preview_window.add( preview_text );
      preview_window.pack();
    }

    preview_text.setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
    preview_text.setText( "" );

    // careful here...put the temporary new values into options
    // until the preview_text box is filled, then switch back.
    String[] tmp = options.optvar_vals;
    options.optvar_vals = new_optvar_vals;
    MakeMakeFileMaker.writeInfo( preview_text, options );
    options.optvar_vals = tmp;

    preview_text.setCaretPosition( 0 );
    preview_window.setVisible( true );
  }


  public void dispose()
  {
    if (advanced_window != null)
      advanced_window.dispose();
    if (preview_window != null)
      preview_window.dispose();
    if (file_maker_thread != null)
      file_maker_thread.stop();
    super.dispose();
    frame.dispatchEvent( new MakeMakeEvent( this,
        MakeMakeEvent.VERIFY_DONE_EVENT ) );
  }
}
