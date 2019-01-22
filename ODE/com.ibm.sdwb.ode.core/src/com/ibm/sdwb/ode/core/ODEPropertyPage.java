package com.ibm.sdwb.ode.core;

import java.io.File;
// Imports
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;


/**
 * @author ODE Team
 */
public class ODEPropertyPage extends PropertyPage implements Listener
{
	// Build options
	private Button b1Box; // Keep building on Error (-i )
	private Button b2Box; // Force Rebuilding of Targets ( -a )
	protected Button b3Box; // ODE Debug -dc
	protected Button b31Box; // ODE Debug -dd
	protected Button b32Box; // ODE Debug -dm
	protected Button b33Box; // ODE Debug -dt
	protected Button b34Box; // ODE Debug -dA
	private Button b5Box; // Build for Real (not -n )
	private Button spaceIndent; // Allow spaces to indent commands (-w)
	// Which targets to generate
	private Button b6Box; // Generate JAVADOC targets
	private Button b7Box; // Generate JAVA_CLASSES targets
	private Button b8Box; // Generate JAR Targets
	private Button b12Box; // Generate OBJECTS targets
	private Button b13Box; // Generate EXPORT targets
	protected Button b14Box; // Run JAVADOC after JAVAC
	private Button componentBox; // Generate EXPORT targets
	private Button clobberBox, buildBox, packageBox;
	private Button expincBox, objectsBox, explibBox, standardBox, allBox;
	// Locations and Extra flags
	private Text t1Text; // Sandbox name
	private Text t2Text; // Location of the .sandboxrc file
	private Text targDir; // Path of the folder below src that is set as target
						  // root of the tree built when a FULL_BUILD is run
	private Text t3Text; // Extra Build Flags
	private Text jarfileText; // Name of the jar file
	private Text logFileName; // Name of the log file to hold build output
	private Text backingBuild;
	protected Text componentText;
	private Text contextName; // CONTEXT value
	private Text numJobs; // number of concurrent jobs
	private IProject proj;
	private TabFolder tabFolder;

	/**
	 * Creates a tab of one horizontal spans.
	 * 
	 * @param parent the parent in which the tab should be created
	 */
	/*
	 * Not currently used, maybe someday will be of use? private void
	 * tabForward(Composite parent) { Label vfiller = new Label(parent,
	 * SWT.LEFT); GridData gridData = new GridData(); gridData = new GridData();
	 * gridData.horizontalAlignment = GridData.BEGINNING;
	 * gridData.grabExcessHorizontalSpace = false; gridData.verticalAlignment =
	 * GridData.CENTER; gridData.grabExcessVerticalSpace = false;
	 * vfiller.setLayoutData(gridData); }
	 */
	/**
	 * Create a text field specific for this application
	 * 
	 * @param parent the parent of the new text field
	 * @return the new text field
	 */
	private Text createTextField( Composite parent )
	{
		return (createTextField( parent, Text.LIMIT ));
	}

	private Text createTextField( Composite parent, int limit )
	{
		Text text = new Text( parent, SWT.SINGLE | SWT.BORDER );
		text.setTextLimit( limit );
		GridData data = new GridData();
		data.horizontalAlignment = (limit == Text.LIMIT) ? GridData.FILL
				: GridData.BEGINNING;
		data.grabExcessHorizontalSpace = (limit == Text.LIMIT);
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		text.setLayoutData( data );
		return text;
	}

	/**
	 * Utility method that creates a new label and sets up its layout data.
	 * 
	 * @param parent the parent of the label
	 * @param text the text of the label
	 * @return the newly-created label
	 */
	protected Label createLabel( Composite parent, String text )
	{
		Label label = new Label( parent, SWT.LEFT );
		label.setText( text );
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData( data );
		return label;
	}

	/**
	 * Creates an new checkbox instance and sets the default layout data.
	 * 
	 * @param group the composite in which to create the checkbox
	 * @param label the string to set into the checkbox
	 * @return the new checkbox
	 */
	private Button createCheckBox( Composite group, String label )
	{
		return createButtonType( group, label, SWT.CHECK );
	}

	/**
	 * Creates an new push button instance and sets the default layout data.
	 * 
	 * @param group the composite in which to create the button
	 * @param label the string to set into the button
	 * @return the new button
	 */
	/*
	 * Not currently used, maybe someday will be of use?
	 * 
	 * private Button createButton(Composite group, String label) { return
	 * (createButtonType(group, label, SWT.PUSH)); }
	 */
	/**
	 * Creates an new push button instance and sets the default layout data.
	 * 
	 * @param group the composite in which to create the button
	 * @param label the string to set into the button
	 * @return the new button
	 */
	private Button createRadioBox( Composite group, String label )
	{
		return (createButtonType( group, label, SWT.RADIO ));
	}

	private Button createButtonType( Composite group, String label, int kind )
	{
		Button button = new Button( group, kind | SWT.LEFT );
		button.setText( label );
		GridData data = new GridData();
		button.setLayoutData( data );
		return button;
	}

	/**
	 * Utility method that creates a new composite and sets up its layout data.
	 * 
	 * @param parent the parent of the composite
	 * @param numColumns the number of columns in the new composite
	 * @return the newly-created composite
	 */
	protected Composite createComposite( Composite parent, int numColumns )
	{
		Composite composite = new Composite( parent, SWT.NULL );
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout( layout );
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData( data );
		return composite;
	}

	protected Composite createGroupComposite( Composite parent, int numColumns,
			String name )
	{
		Group versionGrp = new Group( parent, SWT.SHADOW_ETCHED_OUT );
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = numColumns;
		versionGrp.setLayout( groupLayout );
		versionGrp.setText( name );
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		versionGrp.setLayoutData( gridData );
		return versionGrp;
	}

	private IProject getProject()
	{
		Object element = getElement();
		// This element is always of type IProject even if the selection is
		// made on a java project or a C project.
		if (element instanceof IProject)
			return (IProject)element;
		return null;
	}

	private Object getResource()
	{
		return (getElement());
	}

	// sdmcjunk - implemented this method after creating ODE Nature, but not yet
	// used
	private boolean isODEProject() 
	{ 
		try 
	  	{ 
			return this.proj.hasNature(ODEBasicConstants.NATURE_ID); 
		} 
		catch (CoreException ex) 
		{ 
			System.out.println("PropertyPage: Trouble getting the right Nature:\n" 
					+ ex.getMessage() );
	  	} 
		return false; 
	}
	 
	/**
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	protected Control createContents( Composite parent )
	{
		this.proj = getProject();
		this.tabFolder = new TabFolder( parent, 0 );
		TabItem gentab = new TabItem( this.tabFolder, 0 );
		TabItem buildtab1 = new TabItem( this.tabFolder, 0 );
		TabItem buildtab2 = new TabItem( this.tabFolder, 0 );
		TabItem mftab = new TabItem( this.tabFolder, 0 );
		gentab.setText( "General" );
		buildtab1.setText( "Build Flags" );
		buildtab2.setText( "Other Build Options" );
		mftab.setText( "Makefile Generation" );
		Composite composite1 = createGeneralComponent( this.tabFolder );
		Composite composite2 = createBuildFlagsComponent( this.tabFolder );
		Composite composite3 = createBuildOptionsComponent( this.tabFolder );
		Composite composite4 = createMakefileComponent( this.tabFolder );
		initializeValues();
		gentab.setControl( composite1 );
		buildtab1.setControl( composite2 );
		buildtab2.setControl( composite3 );
		mftab.setControl( composite4 );
		this.tabFolder.pack();
		return this.tabFolder;
	}

	private Composite createGeneralComponent( Composite parent )
	{
		Composite composite1 = createGroupComposite( parent, 2, "" );
		createLabel( composite1, "Sandbox name:" );
		this.t1Text = createTextField( composite1 );
		createLabel( composite1, "Project is a component\nof the sandbox" );
		this.componentBox = createCheckBox( composite1, "" );
		createLabel( composite1, "Component name:" );
		this.componentText = createTextField( composite1 );
		this.componentBox.addSelectionListener( new SelectionListener()
		{
			public void widgetSelected( SelectionEvent event )
			{
				if (((Button)event.widget).getSelection())
				{
					ODEPropertyPage.this.componentText.setEnabled( true );
					// sbButton.setEnabled(false);
				}
				else
				{
					ODEPropertyPage.this.componentText.setEnabled( false );
					// sbButton.setEnabled(true);
				}
			}

			public void widgetDefaultSelected( SelectionEvent e )
			{ // nothing here for now
			}
		} );
		
		// sdmcjunk - path to dir below src folder that is used as root when
		// a tree is built after a FULL_BUILD is initiated by the Platform
		createLabel( composite1, "Target Folder for Full Builds:" );
		this.targDir = createTextField( composite1 );
		
		createLabel( composite1, "Backing build path:" );
		this.backingBuild = createTextField( composite1 );
		createLabel( composite1, "Sandbox RC pathname:" );
		this.t2Text = createTextField( composite1 );
		createLabel( composite1, "Jar file name:" );
		this.jarfileText = createTextField( composite1 );
		createLabel( composite1, "Log file for the Build output:" );
		this.logFileName = createTextField( composite1 );
		return composite1;
	}

	private Composite createBuildFlagsComponent( Composite parent )
	{
		Composite composite2 = createGroupComposite( parent, 1, "" );
		Composite composite21 = createGroupComposite( composite2, 2,
				"Misc Options" );
		createLabel( composite21, "CONTEXT (-m flag):" );
		this.contextName = createTextField( composite21 );
		createLabel( composite21, "# of concurrent jobs (-j flag):" );
		this.numJobs = createTextField( composite21, 2 );
		this.b1Box = createCheckBox( composite21, "Ignore errors (-i flag)" );
		this.b2Box = createCheckBox( composite21, "Forced rebuild (-a flag)" );
		this.b5Box = createCheckBox( composite21, "Do not build (-n flag)" );
		this.spaceIndent = createCheckBox( composite21,
				"Space indent (-w flag)" );
		Composite composite22 = createGroupComposite( composite2, 3,
				"Debugging Options" );
		this.b3Box = createCheckBox( composite22, "Conditionals (-dc)" );
		this.b31Box = createCheckBox( composite22, "File Searching (-dd)" );
		this.b33Box = createCheckBox( composite22, "Target List (-dt)" );
		this.b32Box = createCheckBox( composite22, "Making targets (-dm)" );
		this.b34Box = createCheckBox( composite22, "All Debug Options (-dA)" );
		this.b34Box.addSelectionListener( new SelectionListener()
		{
			public void widgetSelected( SelectionEvent event )
			{
				if (((Button)event.widget).getSelection())
				{
					ODEPropertyPage.this.b3Box.setEnabled( false );
					ODEPropertyPage.this.b3Box.setSelection( false );
					ODEPropertyPage.this.b31Box.setEnabled( false );
					ODEPropertyPage.this.b31Box.setSelection( false );
					ODEPropertyPage.this.b32Box.setEnabled( false );
					ODEPropertyPage.this.b32Box.setSelection( false );
					ODEPropertyPage.this.b33Box.setEnabled( false );
					ODEPropertyPage.this.b33Box.setSelection( false );
					ODEPropertyPage.this.b34Box.setEnabled( true );
					ODEPropertyPage.this.b34Box.setSelection( true );
				}
				else
				{
					ODEPropertyPage.this.b3Box.setEnabled( true );
					ODEPropertyPage.this.b31Box.setEnabled( true );
					ODEPropertyPage.this.b32Box.setEnabled( true );
					ODEPropertyPage.this.b33Box.setEnabled( true );
					ODEPropertyPage.this.b34Box.setEnabled( true );
				}
			}

			public void widgetDefaultSelected( SelectionEvent e )
			{ // nothing here for now
			}
		} );
		return composite2;
	}

	private Composite createBuildOptionsComponent( Composite parent )
	{
		Composite composite2 = createGroupComposite( parent, 1, "" );
		Composite composite23 = createGroupComposite( composite2, 3,
				"Primary Target(s)" );
		this.clobberBox = createCheckBox( composite23, "clobber_all" );
		this.buildBox = createCheckBox( composite23, "build_all" );
		this.packageBox = createCheckBox( composite23, "package_all" );
		Composite composite24 = createGroupComposite( composite2, 3, "Pass" );
		this.expincBox = createRadioBox( composite24, "EXPINC/JAVAH" );
		this.objectsBox = createRadioBox( composite24, "OBJECTS/JAVAC" );
		this.explibBox = createRadioBox( composite24, "EXPLIB/JAR" );
		this.standardBox = createRadioBox( composite24, "STANDARD" );
		this.allBox = createRadioBox( composite24, "all passes" );
		createLabel( composite2, "Other build/mk arguments:" );
		this.t3Text = createTextField( composite2 );
		return composite2;
	}

	private Composite createMakefileComponent( Composite parent )
	{
		Composite composite3 = createGroupComposite( parent, 1, "" );
		Composite composite31 = createGroupComposite( composite3, 1,
				"Java Targets" );
		Composite composite31_1 = createGroupComposite( composite31, 2, "" );
		this.b7Box = createCheckBox( composite31_1, "Compile Java files" );
		this.b8Box = createCheckBox( composite31_1, "Create Jar libraries" );
		// b9Box = createCheckBox(composite12, "Generate RMI Targets ");
		Composite composite31_2 = createGroupComposite( composite31, 1, "" );
		// b10Box = createCheckBox(composite14, "Generate EAR Targets ");
		// b11Box = createCheckBox(composite14, "Generate WAR Targets ");
		this.b6Box = createCheckBox( composite31_2, "Create Java docs" );
		this.b14Box = createCheckBox( composite31_2,
				"Run JAVADOC after compiling java files" );
		this.b6Box.addSelectionListener( new SelectionListener()
		{
			public void widgetSelected( SelectionEvent event )
			{
				if (((Button)event.widget).getSelection())
				{
					ODEPropertyPage.this.b14Box.setEnabled( true );
				}
				else
				{
					ODEPropertyPage.this.b14Box.setEnabled( false );
					ODEPropertyPage.this.b14Box.setSelection( false );
				}
			}

			public void widgetDefaultSelected( SelectionEvent e )
			{ // nothing here for now
			}
		} );
		Composite composite32 = createGroupComposite( composite3, 2,
				"C/C++ Targets" );
		this.b12Box = createCheckBox( composite32, "Compile C/C++ files" );
		this.b13Box = createCheckBox( composite32, "Export C/C++ header files" );
		return composite3;
	}

	/**
	 * Initializes states of the controls using saved values in the Properties
	 * store.
	 */
	private void initializeValues()
	{
		ODEProperties props = new ODEProperties( this.proj );
		props.readProperties();
		initVals( props );
	}

	/**
	 * Initializes states of the controls using default values in the Properties
	 * store.
	 */
	private void initializeDefaults()
	{
		ODEProperties props = new ODEProperties( this.proj );
		initVals( props );
	}

	private void initVals( ODEProperties prop )
	{
		//
		this.t1Text.setText( prop.t1 );
		this.t2Text.setText( prop.t2 );
		this.t3Text.setText( prop.t3 );
		this.targDir.setText( prop.targDir ); // sdmcjunk - added for FULL_BUILDS
		this.jarfileText.setText( prop.jarfileName );
		this.logFileName.setText( prop.logFileName );
		// logFileName.setEnabled(prop.b4);
		this.backingBuild.setText( prop.backingBuild );
		this.contextName.setText( prop.contextName );
		this.numJobs.setText( prop.numJobs );
		this.componentBox.setSelection( prop.isComponent );
		this.componentText.setText( prop.componentName );
		this.componentText.setEnabled( prop.isComponent );
		// sbButton.setEnabled(!prop.isComponent);
		this.clobberBox.setSelection( prop.clobberTarget );
		this.buildBox.setSelection( prop.buildTarget );
		this.packageBox.setSelection( prop.packageTarget );
		this.expincBox.setSelection( prop.expincPass );
		this.objectsBox.setSelection( prop.objectsPass );
		this.explibBox.setSelection( prop.explibPass );
		this.standardBox.setSelection( prop.standardPass );
		this.allBox.setSelection( prop.allPasses );
		//
		// b0Box.setSelection(prop.b0);
		this.b1Box.setSelection( prop.b1 );
		this.b2Box.setSelection( prop.b2 );
		this.b5Box.setSelection( prop.b5 );
		// b9Box.setSelection(prop.b9);
		// b10Box.setSelection(prop.b10);
		this.spaceIndent.setSelection( prop.spaceIndent );
		updateDebuggingSelections( prop.b3, prop.b31, prop.b32, prop.b33,
				prop.b34 );
		updateMakefileTargetSelections( prop.b6, prop.b7, prop.b8, prop.b12,
				prop.b13, prop.b14 );
	}

	/**
	 * 
	 * 
	 */
	private void updateDebuggingSelections( boolean b3, boolean b31,
			boolean b32, boolean b33, boolean b34 )
	{
		//
		this.b3Box.setSelection( b3 );
		this.b31Box.setSelection( b31 );
		this.b32Box.setSelection( b32 );
		this.b33Box.setSelection( b33 );
		this.b34Box.setSelection( b34 );
		if (b34)
		{
			this.b3Box.setEnabled( false );
			this.b31Box.setEnabled( false );
			this.b32Box.setEnabled( false );
			this.b33Box.setEnabled( false );
			this.b34Box.setEnabled( true );
		}
	}

	private void updateMakefileTargetSelections( boolean b6, boolean b7,
			boolean b8, boolean b12, boolean b13, boolean b14 )
	{
		this.b6Box.setSelection( b6 );
		this.b7Box.setSelection( b7 );
		this.b8Box.setSelection( b8 );
		// b10Box.setSelection(b10);
		// b11Box.setSelection(b11);
		this.b12Box.setSelection( b12 );
		this.b13Box.setSelection( b13 );
		this.b14Box.setSelection( b14 );
		this.b14Box.setEnabled( b6 );
	}

	/*
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	protected void performDefaults()
	{
		super.performDefaults();
		initializeDefaults();
	}

	public boolean performOk()
	{
		storeValues();
		return (makeSandbox());
	}

	protected boolean makeSandbox()
	{
		if (this.t1Text.getText().trim().length() == 0)
			return (false);
		if (this.backingBuild.getText().trim().length() == 0)
			return (makeBackingBuild( this.t1Text.getText(), this.proj
					.getLocation().removeLastSegments( 1 ).toString(),
					this.t2Text.getText() ));
		// first make sure the backing build exists
		Path bbpath = new Path( this.backingBuild.getText() );
		File bbcheck = new File( bbpath + "/rc_files/sb.conf" );
		File bbbase = new File( bbpath.removeLastSegments( 1 ).toString() );
		if (!bbcheck.exists())
		{
			if (!bbbase.exists())
				bbbase.mkdirs();
			makeBackingBuild( bbpath.segment( bbpath.segmentCount() - 1 )
					.toString(), bbbase.toString(), bbpath.toString()
					+ "/.sandboxrc" );
		}
		// now make the sandbox itself
		ODEMksbAction mksb = new ODEMksbAction( this.t1Text.getText(),
				this.proj.getLocation().removeLastSegments( 1 ).toString(),
				this.backingBuild.getText(), this.t2Text.getText() );
		mksb.setActionInfo( getShell(), getResource() );
		if (mksb.run() == 0)
		{
			boolean rc;
			rc = ODECommonUtilities.createBuildconfLocal( mksb.javaProject );
			try
			{
				mksb.project.refreshLocal( IResource.DEPTH_INFINITE, null );
			}
			catch (Exception e)
			{ // ignore errors when trying to refresh
			}
			return (rc);
		}
		return (false);
	}

	protected boolean makeBackingBuild( String bbname, String basedir,
			String rcname )
	{
		ODEMkbbAction mkbb = new ODEMkbbAction( bbname.trim(), basedir.trim(),
				rcname.trim() );
		mkbb.setActionInfo( getShell(), getResource() );
		int mkbbCode = mkbb.run();
		if (mkbbCode == 0)
			return (mkbb.createRules());
		return (false);
	}

	/**
	 * Stores the values of the controls back to the preference store.
	 */
	private void storeValues()
	{
		ODEProperties prop = new ODEProperties( this.proj );
		// prop.b0=b0Box.getSelection();
		prop.b1 = this.b1Box.getSelection();
		prop.b2 = this.b2Box.getSelection();
		prop.b5 = this.b5Box.getSelection();
		prop.b6 = this.b6Box.getSelection();
		prop.b7 = this.b7Box.getSelection();
		prop.b8 = this.b8Box.getSelection();
		// prop.b9=b9Box.getSelection();
		// prop.b10 = b10Box.getSelection();
		// prop.b11 = b11Box.getSelection();
		prop.b12 = this.b12Box.getSelection();
		prop.b13 = this.b13Box.getSelection();
		prop.b14 = this.b14Box.getSelection();
		prop.b3 = this.b3Box.getSelection();
		prop.b31 = this.b31Box.getSelection();
		prop.b32 = this.b32Box.getSelection();
		prop.b33 = this.b33Box.getSelection();
		prop.b34 = this.b34Box.getSelection();
		prop.spaceIndent = this.spaceIndent.getSelection();
		prop.clobberTarget = this.clobberBox.getSelection();
		prop.buildTarget = this.buildBox.getSelection();
		prop.packageTarget = this.packageBox.getSelection();
		prop.expincPass = this.expincBox.getSelection();
		prop.objectsPass = this.objectsBox.getSelection();
		prop.explibPass = this.explibBox.getSelection();
		prop.standardPass = this.standardBox.getSelection();
		prop.allPasses = this.allBox.getSelection();
		prop.isComponent = this.componentBox.getSelection();
		prop.componentName = this.componentText.getText();
		// Property files
		prop.t1 = this.t1Text.getText();
		prop.t2 = this.t2Text.getText();
		prop.t3 = this.t3Text.getText();
		prop.jarfileName = this.jarfileText.getText();
		prop.logFileName = this.logFileName.getText();
		prop.targDir = this.targDir.getText(); // sdmcjunk - added for FULL_BUILDS
		prop.backingBuild = this.backingBuild.getText();
		prop.contextName = this.contextName.getText();
		prop.numJobs = this.numJobs.getText();
		try
		{
			prop.saveProperties();
		}
		catch (CoreException ex)
		{
			System.out.println( "PropertyPage: Trouble saving properties" + ex );
		}
	}

	/**
	 * Handles events generated by controls on this page.
	 * 
	 * @param e the event to handle
	 */
	public void handleEvent( Event e )
	{
		// add the code that should react to
		// some widget event
	}
}
