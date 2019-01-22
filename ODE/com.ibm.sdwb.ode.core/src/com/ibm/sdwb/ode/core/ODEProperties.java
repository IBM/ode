package com.ibm.sdwb.ode.core;

// All the imports
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.JavaCore;


/**
 * @author ODE Team
 */
public class ODEProperties
{
	// The buttons
	boolean b1; // Keep building on error
	boolean b2; // Force Rebuilding of Targets -a
	boolean b3; // -dc
	boolean b31; // -dd
	boolean b32; // -dm
	boolean b33; // -dt
	boolean b34; // -dA
	boolean b5; // Build for real
	boolean spaceIndent; // allow spaces to indent commands (-w)
	// Which targets to generate
	boolean b6; // Generate JAVADOC targets
	boolean b7; // Generate JAVAC_CLASSES targets
	boolean b8; // Generate JARS Targets
	boolean b12; // Generate OBJECTS Targets
	boolean b13; // Generate EXPORT Targets
	boolean b14; // Run JAVADOC after JAVAC
	boolean isComponent;
	boolean clobberTarget, buildTarget, packageTarget;
	boolean expincPass, objectsPass, explibPass, standardPass, allPasses;
	// The text fields
	String t1; // Sandbox name
	String t2; // Location of the .sandboxrc files
	String t3; // Extra options
	String jarfileName; // name of the jar file for the target JAR_LIBRARIES
	String logFileName;
	String targDir; // sdmcjunk - path to dir below src used as root for FULL_BUILDS
	String backingBuild;
	String componentName;
	String contextName;
	String numJobs;
	// These are the default settings
	final static String defaultExtraOptions = "";
	private IProject prj;

	/**
	 * @see ODEProperties#ODEProperties() This is the ODEProperties contructor.
	 */
	ODEProperties( IProject prj )
	{
		this.prj = prj;
		this.t1 = prj.getLocation().lastSegment();
		
		// sdmcjunk - changed default rc location to user's home directory
		final String HOME = getHomeDir();
		if (HOME != null)
		{
			this.t2 = HOME + File.separator + ".sandboxrc";
		}
		else
		{
			this.t2 = prj.getLocation().toOSString() + File.separator
					+ ".sandboxrc";
		}
		
		this.t3 = defaultExtraOptions;
		// sdmcjunk - initialize default build directory as src folder
		this.targDir = prj.getLocation().toOSString() + File.separator
				+ ODEBasicConstants.ODESRCNAME;
		this.jarfileName = prj.getName() + ".jar";
		this.logFileName = prj.getLocation().toOSString() + File.separator
				+ "logs" + File.separator + "BuildOutputLog.txt";
		if (File.separatorChar == '/')
			this.backingBuild = "/ode/mybb";
		else
			this.backingBuild = "C:\\ode\\mybb";
		this.componentName = "";
		this.contextName = "";
		this.numJobs = "";
		this.b1 = false;
		this.b2 = false;
		this.b3 = false;
		this.b31 = false;
		this.b32 = false;
		this.b33 = false;
		this.b34 = false;
		this.b5 = false;
		this.b6 = false;
		this.b7 = false;
		this.b8 = false;
		this.b12 = false;
		this.b13 = false;
		this.b14 = false;
		try
		{
			if (prj.hasNature( JavaCore.NATURE_ID ))
			{
				this.b6 = true;
				this.b7 = true;
				this.b8 = true;
			}
			if (CoreModel.hasCNature( prj ) || CoreModel.hasCCNature( prj ))
			{
				this.b12 = true;
				this.b13 = true;
			}
		}
		catch (Throwable e)
		{ // ignore all errors
		}
		this.isComponent = false;
		this.spaceIndent = false;
		this.clobberTarget = false;
		this.buildTarget = true;
		this.packageTarget = false;
		this.expincPass = false;
		this.objectsPass = false;
		this.explibPass = false;
		this.standardPass = false;
		this.allPasses = true;
	}

	/**
	 * @see ODEProperties#getBooleanProperty(IProject, QualifiedName, boolean )
	 *      throws CoreException Gets one Boolean property from the Persistent
	 *      Store, given the Qualified Name.
	 */
	private boolean getBooleanProperty( QualifiedName name, boolean defaultValue )
	{
		String val;
		try
		{
			val = this.prj.getPersistentProperty( name );
		}
		catch (CoreException e)
		{
			return defaultValue;
		}
		if (val != null)
			return val.equals( "true" );
		return defaultValue;
	}

	/**
	 * @see ODEProperties#readProperties(IProject prj)
	 * 
	 * @param IProject is the IProject name of the project.
	 * @return void
	 */
	public void readProperties()
	{
		// initialized the default values
		// Get the all the boolean properties.
		this.b1 = getBooleanProperty( ODEBasicConstants.KEEPBUILDING_PROP,
				this.b1 );
		this.b2 = getBooleanProperty( ODEBasicConstants.FORCEREBUILDING_PROP,
				this.b2 );
		this.b3 = getBooleanProperty( ODEBasicConstants.ODEDEBUGCOND_PROP,
				this.b3 );
		this.b31 = getBooleanProperty( ODEBasicConstants.ODEDEBUGFSEARCH_PROP,
				this.b31 );
		this.b32 = getBooleanProperty(
				ODEBasicConstants.ODEDEBUGMAKINGTGT_PROP, this.b32 );
		this.b33 = getBooleanProperty( ODEBasicConstants.ODEDEBUGTGTLIST_PROP,
				this.b33 );
		this.b34 = getBooleanProperty( ODEBasicConstants.ODEDEBUGALL_PROP,
				this.b34 );
		this.b5 = getBooleanProperty( ODEBasicConstants.ODEBUILDREAL_PROP,
				this.b5 );
		this.b6 = getBooleanProperty( ODEBasicConstants.ODEGENJAVADOC_PROP,
				this.b6 );
		this.b7 = getBooleanProperty( ODEBasicConstants.ODEGENJAVAC_PROP,
				this.b7 );
		this.b8 = getBooleanProperty( ODEBasicConstants.ODEGENJAR_PROP, this.b8 );
		this.b12 = getBooleanProperty( ODEBasicConstants.ODEGENOBJECTS_PROP,
				this.b12 );
		this.b13 = getBooleanProperty( ODEBasicConstants.ODEGENEXPORTS_PROP,
				this.b13 );
		this.b14 = getBooleanProperty(
				ODEBasicConstants.ODERUNJAVADOCAFTERJAVAC_PROP, this.b14 );
		this.isComponent = getBooleanProperty(
				ODEBasicConstants.ISCOMPONENT_PROP, this.isComponent );
		this.spaceIndent = getBooleanProperty(
				ODEBasicConstants.SPACEINDENT_PROP, this.spaceIndent );
		this.clobberTarget = getBooleanProperty(
				ODEBasicConstants.CLOBBERTARGET_PROP, this.clobberTarget );
		this.buildTarget = getBooleanProperty(
				ODEBasicConstants.BUILDTARGET_PROP, this.buildTarget );
		this.packageTarget = getBooleanProperty(
				ODEBasicConstants.PACKAGETARGET_PROP, this.packageTarget );
		this.expincPass = getBooleanProperty(
				ODEBasicConstants.EXPINCPASS_PROP, this.expincPass );
		this.objectsPass = getBooleanProperty(
				ODEBasicConstants.OBJECTSPASS_PROP, this.objectsPass );
		this.explibPass = getBooleanProperty(
				ODEBasicConstants.EXPLIBPASS_PROP, this.explibPass );
		this.standardPass = getBooleanProperty(
				ODEBasicConstants.STANDARDPASS_PROP, this.standardPass );
		this.allPasses = getBooleanProperty( ODEBasicConstants.ALLPASSES_PROP,
				this.allPasses );
		// Get all the String properties.
		try
		{
			String textOp1 = this.prj
					.getPersistentProperty( ODEBasicConstants.SANDBOXNAME_PROP );
			if (textOp1 != null)
				this.t1 = textOp1;
			String compName = this.prj
					.getPersistentProperty( ODEBasicConstants.COMPONENTNAME_PROP );
			if (compName != null)
				this.componentName = compName;
			String textOp2 = this.prj
					.getPersistentProperty( ODEBasicConstants.SANDBOXRCLOC_PROP );
			if (textOp2 != null)
				this.t2 = textOp2;
			String textOp3 = this.prj
					.getPersistentProperty( ODEBasicConstants.ODEEXTRAOPTS_PROP );
			if (textOp3 != null)
				this.t3 = textOp3;
			String jarfile = this.prj
					.getPersistentProperty( ODEBasicConstants.JARFILE_NAME );
			if (jarfile != null)
				this.jarfileName = jarfile;
			String bbName = this.prj
					.getPersistentProperty( ODEBasicConstants.BACKINGBUILD_NAME );
			if (bbName != null)
				this.backingBuild = bbName;
			
			// sdmcjunk - start additions
			setCurrentBB();
			
			String buildDir = this.prj
				.getPersistentProperty( ODEBasicConstants.TARGDIR_NAME );
			if (buildDir != null)
				this.targDir = buildDir;
			// sdmcjunk - end additions	
			
			String logFile = this.prj
					.getPersistentProperty( ODEBasicConstants.LOGFILE_NAME );
			if (logFile != null)
				this.logFileName = logFile;
			String context = this.prj
					.getPersistentProperty( ODEBasicConstants.CONTEXT_NAME );
			if (context != null)
				this.contextName = context;
			String jobs = this.prj
					.getPersistentProperty( ODEBasicConstants.NUM_JOBS );
			if (jobs != null)
				this.numJobs = jobs;
		}
		catch (CoreException e)
		{ // ignore all errors
		}
	}
	
	/*
	 * sdmcjunk - added to update backing build path in case it was modified
	 * outside of eclipse.  
	 */
	private void setCurrentBB()
	{
		try
		{
			String com = "currentsb -back " + this.prj.getName();
			Process p = Runtime.getRuntime().exec( com );
			InputStream stream = p.getInputStream();
			BufferedInputStream reader = new BufferedInputStream( stream );
			String temp = "";
			int count = 0;
			byte[] buf = new byte[65];
			while (count != -1)
			{
				count = reader.read( buf, 0, 64);
				if (count > 0)
					temp += new String( buf, 0, count );
			}
			
			temp = temp.trim();
			if (temp.length() > 0)
			{	
				if (temp.charAt(0) == File.separatorChar)
					this.backingBuild = temp;
			}
			p.destroy();
		}
		catch (IOException e)
		{
         // do nothing yet
		}
	}

	/**
	 * @see ODEProperties#saveBooleanProperty(IProject, QualifiedName, boolean )
	 *      Saves the boolean property as a string in the PropertyStore
	 */
	private void saveBooleanProperty( QualifiedName name, boolean val )
			throws CoreException
	{
		this.prj.setPersistentProperty( name, String.valueOf( val ) );
	}

	/**
	 * @see ODEProperties#saveProperties(IProject ) throws CoreException
	 * @param IProject is the IProject name of the Project.
	 */
	public void saveProperties() throws CoreException
	{
		// Saves the Boolean values
		saveBooleanProperty( ODEBasicConstants.KEEPBUILDING_PROP, this.b1 );
		saveBooleanProperty( ODEBasicConstants.FORCEREBUILDING_PROP, this.b2 );
		saveBooleanProperty( ODEBasicConstants.ODEDEBUGCOND_PROP, this.b3 );
		saveBooleanProperty( ODEBasicConstants.ODEDEBUGFSEARCH_PROP, this.b31 );
		saveBooleanProperty( ODEBasicConstants.ODEDEBUGMAKINGTGT_PROP, this.b32 );
		saveBooleanProperty( ODEBasicConstants.ODEDEBUGTGTLIST_PROP, this.b33 );
		saveBooleanProperty( ODEBasicConstants.ODEDEBUGALL_PROP, this.b34 );
		saveBooleanProperty( ODEBasicConstants.ODEBUILDREAL_PROP, this.b5 );
		saveBooleanProperty( ODEBasicConstants.ODEGENJAVADOC_PROP, this.b6 );
		saveBooleanProperty( ODEBasicConstants.ODEGENJAVAC_PROP, this.b7 );
		saveBooleanProperty( ODEBasicConstants.ODEGENJAR_PROP, this.b8 );
		saveBooleanProperty( ODEBasicConstants.ODERUNJAVADOCAFTERJAVAC_PROP,
				this.b14 );
		saveBooleanProperty( ODEBasicConstants.ODEGENOBJECTS_PROP, this.b12 );
		saveBooleanProperty( ODEBasicConstants.ODEGENEXPORTS_PROP, this.b13 );
		saveBooleanProperty( ODEBasicConstants.ISCOMPONENT_PROP,
				this.isComponent );
		saveBooleanProperty( ODEBasicConstants.SPACEINDENT_PROP,
				this.spaceIndent );
		saveBooleanProperty( ODEBasicConstants.CLOBBERTARGET_PROP,
				this.clobberTarget );
		saveBooleanProperty( ODEBasicConstants.BUILDTARGET_PROP,
				this.buildTarget );
		saveBooleanProperty( ODEBasicConstants.PACKAGETARGET_PROP,
				this.packageTarget );
		saveBooleanProperty( ODEBasicConstants.EXPINCPASS_PROP, this.expincPass );
		saveBooleanProperty( ODEBasicConstants.OBJECTSPASS_PROP,
				this.objectsPass );
		saveBooleanProperty( ODEBasicConstants.EXPLIBPASS_PROP, this.explibPass );
		saveBooleanProperty( ODEBasicConstants.STANDARDPASS_PROP,
				this.standardPass );
		saveBooleanProperty( ODEBasicConstants.ALLPASSES_PROP, this.allPasses );
		// Saving the text information
		this.prj.setPersistentProperty( ODEBasicConstants.SANDBOXNAME_PROP,
				this.t1 );
		this.prj.setPersistentProperty( ODEBasicConstants.COMPONENTNAME_PROP,
				this.componentName );
		this.prj.setPersistentProperty( ODEBasicConstants.SANDBOXRCLOC_PROP,
				this.t2 );
		this.prj.setPersistentProperty( ODEBasicConstants.ODEEXTRAOPTS_PROP,
				this.t3 );
		this.prj.setPersistentProperty( ODEBasicConstants.JARFILE_NAME,
				this.jarfileName );
		this.prj.setPersistentProperty( ODEBasicConstants.TARGDIR_NAME,
				this.targDir ); // sdmcjunk - added for FULL_BUILDS
		this.prj.setPersistentProperty( ODEBasicConstants.LOGFILE_NAME,
				this.logFileName );
		this.prj.setPersistentProperty( ODEBasicConstants.BACKINGBUILD_NAME,
				this.backingBuild );
		this.prj.setPersistentProperty( ODEBasicConstants.CONTEXT_NAME,
				this.contextName );
		this.prj.setPersistentProperty( ODEBasicConstants.NUM_JOBS,
				this.numJobs );
	}
	
	// sdmcjunk - added to set default rc file location as home directory
	private String getHomeDir()
	{
		String val = System.getProperty( "user.home" );
		if (val != null)
		{
			File rcfile = new File( val );
			if (rcfile.exists() && rcfile.isDirectory())
			{
				return rcfile.getAbsolutePath();
			}
		}
		return null;
	}
}
