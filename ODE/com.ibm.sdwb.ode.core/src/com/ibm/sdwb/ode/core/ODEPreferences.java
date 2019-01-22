package com.ibm.sdwb.ode.core;

import java.io.File;
// All the imports
import org.eclipse.jface.preference.*;


/**
 * @author ODE Team This class holds the ODE Preferences and keeps them in
 *         Workbench Preference Store.
 */
public class ODEPreferences
{
	boolean b2 = true; // delete existing makefiles
	boolean b5 = true; // BuildAll
	boolean b6 = false; // packageAll
	String b1; // name of the makefile
	String odeToolsLoc; // ODE Tools Location
	// Default Information
	final static String defaultMakefileName = "makefile.ode";

	/**
	 * @see ODEPreferences#ODEPreferences() This is the ODEPreferences
	 *      constructor.
	 */
	public ODEPreferences()
	{
		// Initialize the Boolean Values
		this.b2 = true;
		this.b5 = true;
		this.b6 = false;
		// Initialize the Text Values
		this.b1 = new String( defaultMakefileName );
		if (File.separatorChar == '/')
		{
			this.odeToolsLoc = new String( ODECommonUtilities
					.getPluginLocation()
					+ "linux" );
		}
		else
		{
			this.odeToolsLoc = new String( ODECommonUtilities
					.getPluginLocation()
					+ "win32" );
		}
	}

	/**
	 * @see ODEPreferences#readPreferences() This will read preferences from the
	 *      Workbench PreferenceStore
	 */
	public void readPreferences()
	{
		// Get the Preference Store
		IPreferenceStore store = ODECorePlugin.getDefault()
				.getPreferenceStore();
		// Read boolean from the store
		this.b2 = store.getBoolean( ODECommonUtilities
				.QToString( ODEBasicConstants.DELETEORIGINAL_PROP ) );
		this.b5 = store.getBoolean( ODECommonUtilities
				.QToString( ODEBasicConstants.BUILDALL_PROP ) );
		this.b6 = store.getBoolean( ODECommonUtilities
				.QToString( ODEBasicConstants.PACKAGEALL_PROP ) );
		// Read Text from the store ...
		this.b1 = store.getString( ODECommonUtilities
				.QToString( ODEBasicConstants.MAKEFILENAME_PROP ) );
		this.odeToolsLoc = store.getString( ODECommonUtilities
				.QToString( ODEBasicConstants.ODETOOLSPATH_PROP ) );
	}

	/**
	 * @see ODEPreferences#savePreferences() throws CoreException This will save
	 *      preferences into the store.
	 */
	public void savePreferences()
	{
		// Get the Store
		IPreferenceStore store = ODECorePlugin.getDefault()
				.getPreferenceStore();
		// Store the boolean values
		store.setValue( ODECommonUtilities
				.QToString( ODEBasicConstants.DELETEORIGINAL_PROP ), this.b2 );
		store.setValue( ODECommonUtilities
				.QToString( ODEBasicConstants.BUILDALL_PROP ), this.b5 );
		store.setValue( ODECommonUtilities
				.QToString( ODEBasicConstants.PACKAGEALL_PROP ), this.b6 );
		// Store the Text Values
		store.setValue( ODECommonUtilities
				.QToString( ODEBasicConstants.MAKEFILENAME_PROP ), this.b1 );
		store.setValue( ODECommonUtilities
				.QToString( ODEBasicConstants.ODETOOLSPATH_PROP ),
				this.odeToolsLoc );
	}
}
