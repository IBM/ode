package com.ibm.sdwb.ode.core;

import org.eclipse.jface.action.IAction;

/**
 * @author ODE Team This is where the Build is executed. Builds are executed
 *         through a Shell.
 */
public class ODEBuildAction extends ODEAction
{
	
	public void run( IAction action )
	{
		super.run( action );
//		run();
	}

	// sdmcjunk - run() is no longer used here, the build command with flags
	// and targets is retrieved in the ODEBuilder#scheduleBuild() method, which
	// creates a new ODEBuildJob instance that is scheduled so it can be run in
	// the background if the user prefers.
	
/*	public int run()
	{
		String command;
		String flags = " ";
		int code = -1;
		
		try
		{	
			String odeExecutable = ODECommonUtilities.findODECommand(
					this.prefs, "build" );
			if (odeExecutable == null)
				throw new IOException( "build command doesn't exist" );
			flags = getBuildFlags();
			command = ODECommonUtilities.quote( odeExecutable );
			command += " " + buildTargets( flags );			
	
			ODEProcessRunner runner = new ODEProcessRunner( command );
						
			runner.runWithProgressBar( this.shell );
			code = runner.getReturnCode();
			String output = runner.getOutputString();
			
			
			ODEBuildJob buildJob = new ODEBuildJob( this.project, command, 
					this.props.logFileName );
			buildJob.setUser( true );
			buildJob.setPriority( Job.BUILD );
			buildJob.setRule( getODEBuildRule() );
			buildJob.schedule();


			if (buildJob.getResult().getCode() != 0)
				throw new Exception( "build command returned nonzero: "
						+ code );
		}
		catch (InterruptedException e)
		{
			ODECommonUtilities.PrintErrorInformation( "Build Interrupted", 
					e.getClass()
					+ ": "
					+ e.getMessage()
					+ "\n"
					+ "The build was interrupted" );
		}
		catch (FileNotFoundException e)
		{
			ODECommonUtilities
				.PrintErrorInformation( 
					"Error writing to log file\n", 
					e.getClass() + ":\n" + e.getMessage() );
		}
		catch (IOException e)   
		{
			ODECommonUtilities
			.PrintErrorInformation( 
				"Error trying to execute ODE command", 
				e.getMessage() + ":\n"
				+ "check ODE tools location preference to make sure the "
				+ "commands exist in the specified directory and that you "
				+ "have the proper permission to execute them" );
		}
		catch (CoreException e)
		{
			ODECommonUtilities.PrintErrorInformation(
					"Problem obtaining resources", e.getClass() + ": "
							+ e.getMessage() + "\n"
							+ "Problem obtaining resources" );
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
			ODECommonUtilities.PrintErrorInformation( "Null Pointer Exception",
					e.getClass() + ": " + e.getMessage() + "\n"
							+ "unknown problem - contact support" );
		}
		catch (Exception e)
		{ // added to notify users build failed due to build, not plugin code
			ODECommonUtilities
					.PrintErrorInformation(
							"Errors ocurred during the build", 
							e.getMessage() );
		}
		
		return (code);
	}
*/
	
	/**
	 * Create the string representing the flags for ODE build Command. The
	 * returned string is either empty or ends with a space character. The flags
	 * will be generated from both the ODEPreferences and ODEProperties
	 * 
	 * @param project is the current Project
	 * @return the Build flags in a String variable
	 */
	public String getBuildFlags()
	{
		// Flags from ODEProperties
		String flags = "";
		if (this.props.b34)
		{ // -dA, ignore the rest of debug flags
			flags += "-dA ";
		}
		else
		{
			if (this.props.b3)
				flags += "-dc ";
			if (this.props.b31)
				flags += "-dd ";
			if (this.props.b32)
				flags += "-dm ";
			if (this.props.b33)
				flags += "-dt ";
		}
		if (this.props.b1)
			flags += "-i ";
		if (this.props.b2)
			flags += "-a ";
		if (this.props.b5)
			flags += "-n ";
		if (this.props.spaceIndent)
			flags += "-w ";
		String contextName = this.props.contextName.trim();
		if (contextName.length() > 0)
			flags += "-m " + contextName + " ";
		String jobs = this.props.numJobs.trim();
		if (jobs.length() > 0)
			flags += "-j" + jobs + " ";
		if (!this.props.t1.equals( "" ))
			flags += "-sb " + this.props.t1 + " ";
		if (!this.props.t2.equals( "" ))
			flags += "-rc " + this.props.t2 + " ";
		if (!this.props.t3.equals( "" ))
			flags += this.props.t3 + " ";
		String expincPassName, objectsPassName, explibPassName, stdPassName;
		if (this.javaProject == null)
		{
			expincPassName = "EXPINC";
			objectsPassName = "OBJECTS";
			explibPassName = "EXPLIB";
		}
		else
		{
			expincPassName = "JAVAH";
			objectsPassName = "JAVAC";
			explibPassName = "JAR";
		}
		stdPassName = "STANDARD";
		if (this.props.expincPass)
			flags += "MAKEFILE_PASS=" + expincPassName + " ";
		else if (this.props.objectsPass)
			flags += "MAKEFILE_PASS=" + objectsPassName + " ";
		else if (this.props.explibPass)
			flags += "MAKEFILE_PASS=" + explibPassName + " ";
		else if (this.props.standardPass)
			flags += "MAKEFILE_PASS=" + stdPassName + " ";
		return flags;
	}

	protected String buildTargets( String flags )
	{
		String command = "";
		String targPrepend = "";
		String folderName = getFolderAsString();
		int begin_index = 0, find_index, targ_index;
		if (this.props.isComponent && this.props.componentName.length() > 0)
			targPrepend += "/" + this.props.componentName;
		targPrepend += folderName;
		find_index = flags.indexOf( "_all" );
		if (find_index > 0)
		{
			do
			{
				targ_index = find_index - 1;
				find_index += 4; // skip past "_all"
				while (targ_index >= 0 && flags.charAt( targ_index ) != ' ')
					--targ_index;
				++targ_index; // move back on to first char of targ
				if (flags.charAt( targ_index ) == '/'
						|| flags.charAt( targ_index ) == '\\') // don't mess
																// with abs
																// targs
					command += flags.substring( begin_index, find_index );
				else
					command += flags.substring( begin_index, targ_index )
							+ targPrepend + "/"
							+ flags.substring( targ_index, find_index );
				begin_index = find_index;
				find_index = flags.indexOf( "_all", begin_index );
			}
			while (find_index > 0);
			if (begin_index < flags.length())
				command += flags.substring( begin_index );
		}
		else
			command = flags;
		if (this.props.clobberTarget)
			command += " " + targPrepend + "/clobber_all";
		if (this.props.buildTarget)
			command += " " + targPrepend + "/build_all";
		if (this.props.packageTarget)
			command += " " + targPrepend + "/package_all";
		return (command);
	}
	
} // end ODEBuildAction class
