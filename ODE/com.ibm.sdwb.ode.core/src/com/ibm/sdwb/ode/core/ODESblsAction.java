package com.ibm.sdwb.ode.core;

import java.io.*;
import org.eclipse.jface.action.IAction;


/**
 * @author ODE Team This is where the sbls command is executed.
 */
public class ODESblsAction extends ODEAction
{
	public void run( IAction action )
	{
		super.run( action );
		run();
	}

	public int run()
	{
		String command = "";
		String commandName = "workon";
		String flags = " ";
		int code = -1;
		try
		{
			String odeExecutable = ODECommonUtilities.findODECommand(
					this.prefs, commandName );
			if (odeExecutable == null)
				throw new IOException( commandName
						+ " command doesn't exist (required for sbls)" );
			flags += getWorkonFlags();
			flags += " -c \"sbls ";
			flags += getSblsFlags();
			flags += "\" ";
			command = commandName + " " + flags;
			ODEProcessRunner runner = new ODEProcessRunner( command );
			runner.run();
			code = runner.getReturnCode();
			String output = runner.getOutputString();
			if (output.length() > 0)
				ODECommonUtilities.showTextWindow( this.shell,
						"Sandbox File List", output );
			if (code != 0)
				throw new IOException( commandName
						+ " command returned nonzero: " + code + "\n\n"
						+ output );
		}
		catch (InterruptedException e)
		{
			ODECommonUtilities.PrintErrorInformation( "sbls Interrupted", e
					.getClass()
					+ ": "
					+ e.getMessage()
					+ "\n"
					+ "The sbls command was interrupted" );
		}
		catch (IOException e)
		{
			ODECommonUtilities.PrintErrorInformation( "Error invoking "
					+ commandName + " command", e.getClass() + ":\n"
					+ e.getMessage() );
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
	 * Create the string representing the flags for sbls commands. The returned
	 * string is either empty or ends with a space character.
	 * 
	 * @return the flags in a String object
	 */
	protected String getWorkonFlags()
	{
		String flags = "-quiet ";
		if (!this.props.t1.equals( "" ))
			flags += "-sb " + this.props.t1 + " ";
		if (!this.props.t2.equals( "" ))
			flags += "-rc " + this.props.t2 + " ";
		return (flags);
	}

	/**
	 * Create the string representing the flags for sbls commands. The returned
	 * string is either empty or ends with a space character.
	 * 
	 * @return the flags in a String object
	 */
	protected String getSblsFlags()
	{
		// Flags from ODEProperties
		String flags = "-alpR ";
		String folderName = getFolderAsString();
		if (folderName.length() > 0)
			flags += folderName.substring( 1 );
		return (flags);
	}
}