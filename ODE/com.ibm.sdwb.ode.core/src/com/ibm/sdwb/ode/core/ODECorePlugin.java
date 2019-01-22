package com.ibm.sdwb.ode.core;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.resources.*;
import org.eclipse.jface.preference.IPreferenceStore;
import java.util.*;


/**
 * The main plugin class to be used in the desktop.
 */
public class ODECorePlugin extends AbstractUIPlugin
{
	// The shared instance.
	private static ODECorePlugin plugin;
	// Resource bundle.
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public ODECorePlugin()
	{
		super();
		plugin = this;
		try
		{
			this.resourceBundle = ResourceBundle
					.getBundle( "com.ibm.sdwb.ode.core.ODEPluginResources" );
		}
		catch (MissingResourceException x)
		{
			this.resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static ODECorePlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace()
	{
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString( String key )
	{
		ResourceBundle bundle = ODECorePlugin.getDefault().getResourceBundle();
		try
		{
			return bundle.getString( key );
		}
		catch (MissingResourceException e)
		{
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle()
	{
		return this.resourceBundle;
	}

	/**
	 * Initializes the default Preferences
	 */
	protected void initializeDefaultPreferences( IPreferenceStore store )
	{
		// These settings will show up when Preference dialog
		// opens up for the first time.
		ODEPreferences prefs = new ODEPreferences();
		store.setDefault( ODECommonUtilities
				.QToString( ODEBasicConstants.DELETEORIGINAL_PROP ), prefs.b2 );
		store.setDefault( ODECommonUtilities
				.QToString( ODEBasicConstants.BUILDALL_PROP ), prefs.b5 );
		store.setDefault( ODECommonUtilities
				.QToString( ODEBasicConstants.PACKAGEALL_PROP ), prefs.b6 );
		store.setDefault( ODECommonUtilities
				.QToString( ODEBasicConstants.MAKEFILENAME_PROP ), prefs.b1 );
		store.setDefault( ODECommonUtilities
				.QToString( ODEBasicConstants.ODETOOLSPATH_PROP ),
				prefs.odeToolsLoc );
	}
}
