package com.ibm.sdwb.ode.core;

import java.io.File;
import java.io.FileFilter;


/**
 * @author ODE Team This is where the Build is executed. Builds are executed
 *         through a Shell.
 */
public class ODEMkbbAction extends ODESandboxCreateAction
{
	public ODEMkbbAction()
	{
		super( true );
	}

	public ODEMkbbAction( ODEMkbbAction copy )
	{
		super( copy );
	}

	public ODEMkbbAction( String bbname, String basedir, String rcname )
	{
		super( true, bbname, basedir, null, rcname );
	}

	public boolean createRules()
	{
		try
		{
			String plugdir = ODECommonUtilities.getPluginLocation();
			File[] files = new File( plugdir ).listFiles( new FileFilter()
			{
				public boolean accept( File pathname )
				{
					if (pathname.toString().endsWith( "rules.zip" )
							|| pathname.toString().endsWith( "confs.zip" ))
						return (true);
					return (false);
				}
			} );
			String extractDir = this.basedir + "/" + this.sbname + "/src";
			for (int i = 0; i < files.length; ++i)
			{
				if (!ODECommonUtilities.jarExtract( files[i].toString(),
						extractDir ))
					return (false);
			}
		}
		catch (Exception e)
		{
			return (false);
		}
		return (true);
	}
}