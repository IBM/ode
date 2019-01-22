package com.ibm.sdwb.ode.core;

import java.io.*;
import java.util.StringTokenizer;
import org.eclipse.jface.action.IAction;


/**
 * @author ODE Team This is where the Build is executed. Builds are executed
 *         through a Shell.
 */
public abstract class ODESandboxCreateAction extends ODEAction
{
	protected boolean isBackingBuild, sbExists;
	protected String sbname, bbname, rcname, basedir, oldbbpath, oldbase;

	public ODESandboxCreateAction( boolean isBackingBuild )
	{
		this( isBackingBuild, null, null, null, null );
	}

	public ODESandboxCreateAction( boolean isBackingBuild, String sbname,
			String basedir, String bbname, String rcname )
	{
		super();
		this.isBackingBuild = isBackingBuild;
		this.sbname = sbname;
		this.basedir = basedir;
		this.bbname = bbname;
		this.rcname = rcname;
	}

	public ODESandboxCreateAction( ODESandboxCreateAction copy )
	{
		super( copy );
		this.isBackingBuild = copy.isBackingBuild;
		this.sbname = new String( copy.sbname );
		this.basedir = new String( copy.basedir );
		if (copy.bbname == null)
			this.bbname = null;
		else
			this.bbname = new String( copy.bbname );
		this.rcname = new String( copy.rcname );
	}

	public void run( IAction action )
	{
		super.run( action );
		this.sbname = this.props.t1;
		this.bbname = this.props.backingBuild;
		this.rcname = this.props.t2;
		run();
	}

	protected int run()
	{
		// start with some assumptions
		this.sbExists = false;
		this.oldbbpath = null;
		this.oldbase = null;
		if (getSandboxInfo())
		{
			if (this.sbExists)
				return (retargetSandbox());
			return (createSandbox());
		}
		return (-1);
	}

	/**
	 * getSandboxInfo
	 * 
	 * Determine what needs to be done...we'll either need to create a new
	 * sandbox, retarget an existing one, or do nothing.
	 * 
	 */
	protected boolean getSandboxInfo()
	{
		String commandName = "currentsb";
		String command = "";
		boolean ok = false;
		try
		{
			String odeExecutable = ODECommonUtilities.findODECommand(
					this.prefs, commandName );
			if (odeExecutable == null)
				throw new IOException( commandName + " command doesn't exist" );
			command = ODECommonUtilities.quote( odeExecutable );
			command += " -dir -back ";
			if (!this.rcname.equals( "" ))
				command += "-rc " + this.rcname + " ";
			if (!this.sbname.equals( "" ))
				command += this.sbname;
			ODEProcessRunner runner = new ODEProcessRunner( command );
			runner.run();
			int code = runner.getReturnCode();
			if (code == 0) // means sb already exists
			{
				this.sbExists = true;
				String output = runner.getOutputString().trim();
				StringTokenizer tok = new StringTokenizer( output );
				if (tok.hasMoreTokens())
					this.oldbase = tok.nextToken();
				if (tok.hasMoreTokens())
					this.oldbbpath = tok.nextToken();
			}
			ok = true;
		}
		catch (InterruptedException e)
		{
			ODECommonUtilities.PrintErrorInformation( commandName
					+ " Interrupted", e.getClass() + ": " + e.getMessage()
					+ "\n" + "The " + commandName + " command was interrupted" );
		}
		catch (IOException e)
		{
			ODECommonUtilities.PrintErrorInformation( "Error invoking "
					+ command, e.getClass() + ":\n" + e.getMessage() + "\n"
					+ "Check ODE tools directory location in ODE Preferences" );
		}
		catch (NullPointerException e)
		{
			ODECommonUtilities.PrintErrorInformation( "Null Pointer Exception",
					e.getClass() + ": " + e.getMessage() + "\n"
							+ "unknown problem - contact support" );
		}
		return (ok);
	}

	protected int createSandbox()
	{
		String command = "";
		String commandName = (this.isBackingBuild) ? "mkbb" : "mksb";
		int code = -1;
		try
		{
			String odeExecutable = ODECommonUtilities.findODECommand(
					this.prefs, commandName );
			if (odeExecutable == null)
				throw new IOException( commandName + " command doesn't exist" );
			command = ODECommonUtilities.quote( odeExecutable );
			if (!this.isBackingBuild
					&& (this.bbname == null || this.bbname.trim().length() < 1))
				throw new IOException( "Backing build path not specified" );
			if (!this.isBackingBuild
					&& (this.sbname == null || this.sbname.trim().length() < 1))
				throw new IOException( "Sandbox name not specified" );
			command += " " + getMksbFlags();
			ODEProcessRunner runner = new ODEProcessRunner( command );
			runner.run();
			code = runner.getReturnCode();
			String output = runner.getOutputString();
			if (output.indexOf( "already exists" ) >= 0)
				code = 0;
			if (output.indexOf( "Creating sandbox" ) >= 0)
				code = 0;
			if (code != 0)
				throw new IOException( commandName
						+ " command returned nonzero: " + code + "\n\n"
						+ output );
		}
		catch (InterruptedException e)
		{
			ODECommonUtilities.PrintErrorInformation( commandName
					+ " Interrupted", e.getClass() + ": " + e.getMessage()
					+ "\n" + "The " + commandName + " command was interrupted" );
		}
		catch (IOException e)
		{	// don't do anything since sandbox is still created
//			e.printStackTrace();
//			ODECommonUtilities.PrintErrorInformation( "Error invoking "
//					+ command, e.getClass() + ":\n" + e.getMessage() );
		}
		catch (NullPointerException e)
		{
			ODECommonUtilities.PrintErrorInformation( "Null Pointer Exception",
					e.getClass() + ": " + e.getMessage() + "\n"
							+ "unknown problem - contact support" );
		}
		return (code);
	}

	/**
	 * retargetSandbox
	 * 
	 * For the case where a sandox/bb already exists in the .sandboxrc. The
	 * action in this case could be any of the following: 1. if an sb, use resb
	 * if necessary to switch the bb it uses 2. if a bb,
	 */
	protected int retargetSandbox()
	{
		String command = "";
		String commandName = "resb";
		int code = -1;
		try
		{
			String odeExecutable = ODECommonUtilities.findODECommand(
					this.prefs, commandName );
			if (odeExecutable == null)
				throw new IOException( commandName + " command doesn't exist" );
			command = ODECommonUtilities.quote( odeExecutable );
			if (this.isBackingBuild)
				return (0); // don't do anything now for backing builds
			if (this.oldbbpath != null && this.oldbbpath.equals( this.bbname ))
				return (0);
			if (this.bbname == null || this.bbname.trim().length() < 1)
				throw new IOException( "Backing build path not specified" );
			if (this.sbname == null || this.sbname.trim().length() < 1)
				throw new IOException( "Sandbox name not specified" );
			command += " " + getResbFlags();
			ODEProcessRunner runner = new ODEProcessRunner( command );
			runner.run();
			code = runner.getReturnCode();
			String output = runner.getOutputString();
			if (code != 0)
				throw new IOException( commandName
						+ " command returned nonzero: " + code + "\n\n"
						+ output );
		}
		catch (InterruptedException e)
		{
			ODECommonUtilities.PrintErrorInformation( commandName
					+ " Interrupted", e.getClass() + ": " + e.getMessage()
					+ "\n" + "The " + commandName + " command was interrupted" );
		}
		catch (IOException e)
		{
			ODECommonUtilities.PrintErrorInformation( "Error invoking "
					+ command, e.getClass() + ":\n" + e.getMessage() + "\n"
					+ "Check ODE tools directory location in ODE Preferences" );
		}
		catch (NullPointerException e)
		{
			ODECommonUtilities.PrintErrorInformation( "Null Pointer Exception",
					e.getClass() + ": " + e.getMessage() + "\n"
							+ "unknown problem - contact support" );
		}
		return (code);
	}

	/**
	 * Create the string representing the flags for ODE mkbb and mksb commands.
	 * The returned string is either empty or ends with a space character. The
	 * flags will be generated from both the ODEPreferences and ODEProperties
	 * 
	 * @return the flags in a String variable
	 */
	protected String getMksbFlags()
	{
		String flags = "-auto ";
		if (this.bbname != null && this.bbname.length() > 0)
			flags += "-back " + this.bbname + " ";
		flags += "-dir " + this.basedir + " ";
		if (!this.rcname.equals( "" ))
			flags += "-rc " + this.rcname + " ";
		if (!this.sbname.equals( "" ))
			flags += this.sbname + " ";
		return flags;
	}

	protected String getResbFlags()
	{
		String flags = "-auto ";
		if (!this.rcname.equals( "" ))
			flags += "-rc " + this.rcname + " ";
		if (!this.sbname.equals( "" ))
			flags += "-sb " + this.sbname + " ";
		if (this.bbname != null && this.bbname.length() > 0)
			flags += this.bbname + " ";
		return flags;
	}
}