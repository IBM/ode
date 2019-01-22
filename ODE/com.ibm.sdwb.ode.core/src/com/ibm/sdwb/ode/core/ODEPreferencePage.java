package com.ibm.sdwb.ode.core;

import org.eclipse.ui.*;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;


/**
 * @author ODE Team This class holds the preference page defined in the
 *         Workbench
 * 
 */
public class ODEPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, Listener
{
	// Builder Installation
	// private Button enableODEBuilder; //Enable it to build with ODE
	// private Button genOnResourceChange; //Regenerate makefile on resource
	// Change(RMI)
	// private Button genOnDirectoryChange; //regenerate on Directory Change
	// private Button genOnProjectChange; // Regenerate on Project Change
	// private Button genOnWorkbenchExit; // Regenerate on Project Exit
	// Makefile Generator
	private Text makefileName; // Name of the Makefile
	private Button deleteExistingMakefiles; // Delete existing makefiles
	// private Button genSingleMakBut; //Generate a single Makefile/project
	// private Button genMultMakBut; //Generates Multiple Makefiles/project
	private Button buildAllBut; // Generate All as target
	private Button packageAllBut; // PackageAll
	private Text odeToolsLoc; // ODE Home Location
	// private Text odeZipsLoc; //ODE Zip Files Location

	/**
	 * Creates an new radiobutton instance and sets the default layout data.
	 * 
	 * @param group the composite in which to create the checkbox
	 * @param label the string to set into the checkbox
	 * @return the new checkbox
	 */
	private Button createRadioButton( Composite group, String label )
	{
		Button button = new Button( group, SWT.RADIO | SWT.LEFT );
		button.setText( label );
		button.addListener( SWT.Selection, this );
		GridData data = new GridData();
		button.setLayoutData( data );
		return button;
	}

	/**
	 * Creates an new checkbox instance and sets the default layout data.
	 * 
	 * @param group the composite in which to create the checkbox
	 * @param label the string to set into the checkbox
	 * @return the new checkbox
	 */
	private Button createCheckbox( Composite group, String label )
	{
		Button button = new Button( group, SWT.CHECK | SWT.LEFT );
		button.setText( label );
		GridData data = new GridData();
		button.setLayoutData( data );
		return button;
	}

	/**
	 * Creates Group composite control and sets the default layout data.
	 * 
	 * @param parent the parent of the new composite
	 * @param numColumns the number of columns for the new composite
	 * @param title the title of the Group.
	 * @return the newly-created coposite
	 */
	protected Composite createGroupComposite( Composite parent, int numColumns,
			String titlel )
	{
		Group versionGrp = new Group( parent, SWT.SHADOW_ETCHED_OUT );
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = numColumns;
		versionGrp.setLayout( groupLayout );
		versionGrp.setText( titlel );
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		versionGrp.setLayoutData( gridData );
		return versionGrp;
	}

	/**
	 * This method takes the string for the title of a text field and the value
	 * for the text of the field.
	 * 
	 * @return org.eclipse.swt.widgets.Text
	 * @param labelString java.lang.String
	 * @param textValue java.lang.String
	 * @param parent Composite
	 */
	private Text addLabelAndText( String labelString, String textValue,
			Composite parent )
	{
		Label label = new Label( parent, SWT.LEFT );
		label.setText( labelString );
		Text text = new Text( parent, SWT.LEFT | SWT.BORDER );
		GridData data = new GridData();
		text.addListener( SWT.Modify, this );
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = false;
		text.setLayoutData( data );
		text.setText( textValue );
		return text;
	}

	/**
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	protected Control createContents( Composite parent )
	{
		Composite composite = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout( layout );
		composite.setLayoutData( new GridData( GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL ) );
		Composite composite0 = createGroupComposite( composite, 1,
				"General Options" );
		// buildOptions = addLabelAndText("Build Options", "", composite);
		this.odeToolsLoc = addLabelAndText( "ODE Tools Directory", "",
				composite0 );
		// odeZipsLoc = addLabelAndText("ODE Zip Archives
		// Directory","",composite);
		// buildFlags = addLabelAndText("Build Flags","",composite);
		// extraJarsLoc = addLabelAndText("Extra Jars Location","",composite);
		Composite composite1 = createGroupComposite( composite, 2,
				"Makefile Generation Options" );
		this.makefileName = addLabelAndText( "ODE Makefile Name", "",
				composite1 );
		this.deleteExistingMakefiles = createCheckbox( composite1,
				"Overwrite existing makefiles" );
		Composite composite2 = createGroupComposite( composite1, 1, "" );
		this.buildAllBut = createRadioButton( composite2,
				"Use file names from the project" );
		this.packageAllBut = createRadioButton( composite2,
				"Use file names from the file system" );
		/*
		 * Composite composite2 = createGroupComposite(parent, 2,"Makefile
		 * Generation "); genSingleMakBut = createRadioButton(composite2,
		 * "Generate single makefile per package"); genMultMakBut =
		 * createRadioButton(composite2, "Generate single makefile per
		 * directory");
		 */
		/*
		 * Composite composite4 = createGroupComposite(parent, 2,"Makefile
		 * Generation Timing"); genOnResourceChange = createCheckbox(composite4,
		 * "Regenerate on source file change"); genOnDirectoryChange=
		 * createCheckbox(composite4, "Regenerate on directory change");
		 * genOnProjectChange= createCheckbox(composite4, "Regenerate on project
		 * change"); genOnWorkbenchExit= createCheckbox(composite4, "Regenerate
		 * on workbench exit"); enableODEBuilder = createCheckbox(composite4,
		 * "Enables ODE Builder");
		 */
		initializeValues();
		return composite;
	}

	/**
	 * The <code>ODEPreferencePage</code> implementation of this
	 * <code>PreferencePage</code> method returns preference store that
	 * belongs to the our plugin. This is important because we want to store our
	 * preferences separately from the desktop.
	 */
	protected IPreferenceStore doGetPreferenceStore()
	{
		return ODECorePlugin.getDefault().getPreferenceStore();
	}

	/**
	 * (non-Javadoc) Method declared on IWorkbenchPreferencePage
	 */
	public void init( IWorkbench workbench )
	{ // do nothing for now
	}

	/**
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	protected void performDefaults()
	{
		super.performDefaults();
		initializeDefaults();
	}

	/**
	 * (non-Javadoc) Method declared on PreferencePage
	 */
	public boolean performOk()
	{
		storeValues();
		return true;
	}

	/**
	 * Initializes states of the controls using default values in the preference
	 * store.
	 */
	private void initializeDefaults()
	{
		ODEPreferences prefs = new ODEPreferences();
		// Sets the value of Booleans
		this.odeToolsLoc.setText( prefs.odeToolsLoc );
		// odeZipsLoc.setText(prefs.odeZipsLoc);
		// Sets the Location of Text fields
		this.makefileName.setText( prefs.b1 );
		this.deleteExistingMakefiles.setSelection( prefs.b2 );
		// genSingleMakBut.setSelection(prefs.b3);
		// genMultMakBut.setSelection(prefs.b4);
		this.buildAllBut.setSelection( prefs.b5 );
		this.packageAllBut.setSelection( prefs.b6 );
		// Sets the values of makefile regeneration
		/*
		 * enableODEBuilder.setSelection(prefs.b7);
		 * genOnResourceChange.setSelection(prefs.b8);
		 * genOnDirectoryChange.setSelection(prefs.b9);
		 * genOnProjectChange.setSelection(prefs.b10);
		 * genOnWorkbenchExit.setSelection(prefs.b11);
		 */
	}

	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues()
	{
		ODEPreferences prefs = new ODEPreferences();
		prefs.readPreferences();
		// Sets the value of Booleans
		this.odeToolsLoc.setText( prefs.odeToolsLoc );
		// odeZipsLoc.setText(prefs.odeZipsLoc);
		// Sets the Location of Text fields
		this.makefileName.setText( prefs.b1 );
		this.deleteExistingMakefiles.setSelection( prefs.b2 );
		// genSingleMakBut.setSelection(prefs.b3);
		// genMultMakBut.setSelection(prefs.b4);
		this.buildAllBut.setSelection( prefs.b5 );
		this.packageAllBut.setSelection( prefs.b6 );
		// Sets the values of makefile regeneration
		// enableODEBuilder.setSelection(prefs.b7);
		// genOnResourceChange.setSelection(prefs.b8);
		// genOnDirectoryChange.setSelection(prefs.b9);
		// genOnProjectChange.setSelection(prefs.b10);
		// genOnWorkbenchExit.setSelection(prefs.b11);
	}

	/**
	 * Stores the values of the controls back to the preference store.
	 */
	private void storeValues()
	{
		ODEPreferences prefs = new ODEPreferences();
		// Store the boolean values.
		prefs.b1 = this.makefileName.getText();
		prefs.b2 = this.deleteExistingMakefiles.getSelection();
		// prefs.b3 = genSingleMakBut.getSelection();
		// prefs.b4 = genMultMakBut.getSelection();
		prefs.b5 = this.buildAllBut.getSelection();
		prefs.b6 = this.packageAllBut.getSelection();
		// Store the String values
		prefs.odeToolsLoc = this.odeToolsLoc.getText();
		// prefs.odeZipsLoc = odeZipsLoc.getText();
		// Store more boolean Values
		/*
		 * prefs.b7 = enableODEBuilder.getSelection(); prefs.b8 =
		 * genOnResourceChange.getSelection(); prefs.b9 =
		 * genOnDirectoryChange.getSelection(); prefs.b10
		 * =genOnProjectChange.getSelection(); prefs.b11 =
		 * genOnWorkbenchExit.getSelection();
		 */
		prefs.savePreferences();
	}

	/**
	 * (non-Javadoc) Method declared on SelectionListener
	 */
	// public void widgetDefaultSelected(SelectionEvent event) {
	// Handle a default selection. Do nothing in this example
	// }
	/**
	 * (non-Javadoc) Method declared on SelectionListener
	 */
	// public void widgetSelected(SelectionEvent event) {
	// Do nothing on selection in this example;
	// }
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
