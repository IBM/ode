package com.ibm.sdwb.ode.core;

import org.eclipse.core.runtime.QualifiedName;


/**
 * @author ODE Team This class holds the constants used during this project.
 */
public interface ODEBasicConstants
{
	// Name of this plugin
	public static final String PLUGIN_ID = "com.ibm.sdwb.ode.core";
	
	// sdmcjunk - added marker type id string for adding/deleting markers
	public static final String MARKER_TYPE_ID = 
		"com.ibm.sdwb.ode.core.errorMarker";
	// sdmcjunk - added problems view id so it can be activated by user after
	// a build job completes
	public static final String PROBLEMS_VIEW_ID = 
		"org.eclipse.ui.views.ProblemView";
	// sdmcjunk - used to register ODE as an actual builder and nature
	public static final String ODE_BUILDER_ID = "com.ibm.sdwb.ode.core.odeBuilder";
	public static final String ODESRCNAME = "src";
	
	// Statics used in ODEProperties
	public static final QualifiedName KEEPBUILDING_PROP = new QualifiedName(
			PLUGIN_ID, "flags.keepBuilding" );
	public static final QualifiedName FORCEREBUILDING_PROP = new QualifiedName(
			PLUGIN_ID, "flags.forceRebuilding" );
	public static final String NATURE_ID = "com.ibm.sdwb.ode.core.ODEProjectNature";
	// Makefile generation Information
	public static final QualifiedName ODEBUILDER_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeBuilder" );
	public static final QualifiedName ODEGENONRESCHANGE_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeGenerateOnResourceChange" );
	public static final QualifiedName ODEGENONDIRCHANGE_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeGenerateOnDirectoryChange" );
	public static final QualifiedName ODEGENONPROJECTCHANGE_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeGenerateOnProjectChange" );
	public static final QualifiedName ODEGENONWORKBENCHEXIT_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeGenerateOnWorkbenchExit" );
	// ODE Debug (-dc, -dd, -dA, -da, -dm, -dt)
	public static final QualifiedName ODEDEBUGCOND_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeDebugConditional" );
	public static final QualifiedName ODEDEBUGFSEARCH_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeDebugFileSearch" );
	public static final QualifiedName ODEDEBUGMAKINGTGT_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeDebugdm" );
	public static final QualifiedName ODEDEBUGALL_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeDebugAll" );
	public static final QualifiedName ODEDEBUGTGTLIST_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeDebugTgtList" );
	public static final QualifiedName ODEDEBUGTGTLISTE_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeDebugtargetlist" );
	// Build for Real (no -n)
	public static final QualifiedName ODEBUILDREAL_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeBuildReal" );
	public static final QualifiedName SPACEINDENT_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeSpaceIndent" );
	// Target Generation
	public static final QualifiedName ODEGENJAVADOC_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeGenJavadoc" );
	public static final QualifiedName ODERUNJAVADOCAFTERJAVAC_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeRunJavadocAfterJavac" );
	public static final QualifiedName ODEGENJAVAC_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeGenJavac" );
	public static final QualifiedName ODEGENJAR_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeGenJar" );
	public static final QualifiedName ODEGENOBJECTS_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeGenObjects" );
	public static final QualifiedName ODEGENEXPORTS_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeGenExports" );
	// is the project a component of the sandbox?
	public static final QualifiedName ISCOMPONENT_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeIsComponent" );
	public static final QualifiedName COMPONENTNAME_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeComponentName" );
	public static final QualifiedName CLOBBERTARGET_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeClobberTarget" );
	public static final QualifiedName BUILDTARGET_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeBuildTarget" );
	public static final QualifiedName PACKAGETARGET_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odePackageTarget" );
	public static final QualifiedName EXPINCPASS_PROP = new QualifiedName(
			PLUGIN_ID, "flags.expincPassTarget" );
	public static final QualifiedName OBJECTSPASS_PROP = new QualifiedName(
			PLUGIN_ID, "flags.objectsPassTarget" );
	public static final QualifiedName EXPLIBPASS_PROP = new QualifiedName(
			PLUGIN_ID, "flags.explibPassTarget" );
	public static final QualifiedName STANDARDPASS_PROP = new QualifiedName(
			PLUGIN_ID, "flags.standardPassTarget" );
	public static final QualifiedName ALLPASSES_PROP = new QualifiedName(
			PLUGIN_ID, "flags.allPassesTarget" );
	// Name of the jar file for the makefile target JAR_LIBRARIES
	public static final QualifiedName JARFILE_NAME = new QualifiedName(
			PLUGIN_ID, "flags.jarfileName" );
	// Name of the sandbox.
	public static final QualifiedName SANDBOXNAME_PROP = new QualifiedName(
			PLUGIN_ID, "flags.sandboxName" );
	// Name of the backing build of the sandbox
	public static final QualifiedName BACKINGBUILD_NAME = new QualifiedName(
			PLUGIN_ID, "flags.bbName" );
	// Name of the path to the folder that is root of build tree for FULL_BUILDS
	public static final QualifiedName TARGDIR_NAME = new QualifiedName( 
			PLUGIN_ID, "flags.targDir" ); // sdmcjunk - added for builder
	// Location of the DotSandboxrc
	public static final QualifiedName SANDBOXRCLOC_PROP = new QualifiedName(
			PLUGIN_ID, "flags.sandboxLocation" );
	// Extra Build Options
	public static final QualifiedName ODEEXTRAOPTS_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeExtraOpts" );
	// Name of the log file for the build output
	public static final QualifiedName LOGFILE_NAME = new QualifiedName(
			PLUGIN_ID, "flags.logFileName" );
	// CONTEXT name
	public static final QualifiedName CONTEXT_NAME = new QualifiedName(
			PLUGIN_ID, "flags.contextName" );
	// Number of jobs to run concurrently
	public static final QualifiedName NUM_JOBS = new QualifiedName( PLUGIN_ID,
			"flags.numJobs" );
	/***************************************************************************
	 * ODE Preferences ***********************************
	 */
	// Static used in ODEPreferences
	// i.e. Defines and Undefines
	public static final QualifiedName BUILDOPTIONS_PROP = new QualifiedName(
			PLUGIN_ID, "flags.buildProp" );
	// i.e. -i, -n, ...
	public static final QualifiedName BUILDFLAGS_PROP = new QualifiedName(
			PLUGIN_ID, "flags.buildFLag" );
	// Mutually exclusive flags
	public static final QualifiedName MAKEFILENAME_PROP = new QualifiedName(
			PLUGIN_ID, "flags.makefileName" );
	public static final QualifiedName DELETEORIGINAL_PROP = new QualifiedName(
			PLUGIN_ID, "flags.deleteOriginal" );
	// Generate a single makefile per project
	public static final QualifiedName SINGLEMAKEFILE_PROP = new QualifiedName(
			PLUGIN_ID, "flags.singleMake" );
	// Generate multiple makefiles per project.
	public static final QualifiedName MULTMAKEFILE_PROP = new QualifiedName(
			PLUGIN_ID, "flags.multiMake" );
	// Location of executables
	public static final QualifiedName ODETOOLSPATH_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeToolsPath" );
	// Location of zip files
	public static final QualifiedName ODEZIPSPATH_PROP = new QualifiedName(
			PLUGIN_ID, "flags.odeZipsPath" );
	// Build_all as default Target
	public static final QualifiedName BUILDALL_PROP = new QualifiedName(
			PLUGIN_ID, "flags.buildAll" );
	// package_all as default target
	public static final QualifiedName PACKAGEALL_PROP = new QualifiedName(
			PLUGIN_ID, "flags.packageAll" );
	// Firefly plugin id
	public static String fireflyPluginId = "com.ibm.sdwb.firefly.core";
}
