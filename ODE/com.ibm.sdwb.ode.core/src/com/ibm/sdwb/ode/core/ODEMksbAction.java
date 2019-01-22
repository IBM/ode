package com.ibm.sdwb.ode.core;

/**
 * @author ODE Team This is where the Build is executed. Builds are executed
 *         through a Shell.
 */
public class ODEMksbAction extends ODESandboxCreateAction
{
	public ODEMksbAction()
	{
		super( false );
	}

	public ODEMksbAction( ODEMksbAction copy )
	{
		super( copy );
	}

	public ODEMksbAction( String sbname, String basedir, String bbname,
			String rcname )
	{
		super( false, sbname, basedir, bbname, rcname );
	}
}