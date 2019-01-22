package com.ibm.sdwb.ode.core;

import java.io.File;
import java.util.Date;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;


/**
 * @author kiranl
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public abstract class ODEMakefile
{
	protected IFile file;
	protected IProject project;
	protected ODEMakefileProperties mfProperties;
	protected StringBuffer commentBuffer = new StringBuffer();
	protected StringBuffer includeRulesBuffer = new StringBuffer();
	protected StringBuffer genBackSlashPathBuffer = new StringBuffer();
	protected StringBuffer useFileFindBuffer = new StringBuffer();
	protected static final String COMMENT_LINE = "# ";
	// Add to default properties.
	protected static final StringBuffer DEFAULT_INCLUDE = new StringBuffer(
			"\n# Include the ODE rules.\n.include <${RULES_MK}>\n" );

	/**
	 * Constructor for ODEMakefile.
	 */
	public ODEMakefile()
	{
		super();
	}

	/**
	 * Constructor Creates an ODEMakefile Object. Does not physically create a
	 * makefile on disk.
	 */
	ODEMakefile( IFile fileParm, IProject proj, ODEMakefileProperties mfProps )
	{
		this.file = fileParm;
		this.project = proj;
		this.mfProperties = mfProps;
	}

	/**
	 * This is a utility method. Used to compare 2 Strings.
	 * 
	 * @return int, which is the result of the compareto Method on Strings
	 */
	protected int compareTo( ODEMakefile mf )
	{
		return (ODECommonUtilities.getRelativeName( this.file )
				.compareTo( ODECommonUtilities.getRelativeName( mf.file ) ));
	}

	/**
	 * Given a specific file, calculate the depth of the resource, relative to
	 * the project's top level directory. An ODEMakefile is a Package topLevel
	 * file, if it has Depth=2 and it is a Project TopLevel, if it has depth=1.
	 * 
	 * @return the number of subdirectories below the project directory.
	 */
	protected int getDepth()
	{
		int depth = 0;
		IContainer cont = this.file.getParent();
		if (cont == null)
			return -1;
		IFolder folder = (IFolder)cont;
		String folderName = folder.getFullPath().toString();
		// Count the number of directory seperators...
		for (int i = 0; i < folderName.length(); i++)
			if (folderName.charAt( i ) == '/')
				depth++;
		return depth;
	}

	/**
	 * This is a utility method. Will be used to check if an ODEMakefile is
	 * ProjectTopLevel (meaning, if the ODEMakefile resides in the Project
	 * Directory). When generating one single makefile per project, the makefile
	 * will be implemented at the topLevel Directory.
	 * 
	 * @return true if this is ProjectTopLevel
	 */
	protected boolean isProjectTopLevel()
	{
		return (getDepth() == 1);
	}

	/**
	 * This is a utility method. Will be used to check if an ODEMakefile is
	 * PackageTopLevel (meaning, if the ODEMakefile resides in the Package
	 * Directory).
	 * 
	 * @return true if this is PackageTopLevel
	 */
	protected boolean isPackageTopLevel()
	{
		return (getDepth() == 2);
	}

	/**
	 * Generates the top of the makefile. Which is usually the header, version
	 * generator and a date.
	 * 
	 * @param force determines whether or not to overwrite the same information
	 *            is an existing makefile.ode
	 */
	protected void generateTopOfMakefileFileComments()
	{
		Date today = new Date();
		this.commentBuffer.append( COMMENT_LINE ).append(
				ODECommonUtilities.getRelativeName( this.file ) ).append(
				"\n# Automatically generated on " ).append( today.toString() )
				.append( "\n" );
	}

	/**
	 * Generates the Include Line of the makefile. Which is usually the
	 * ${RULES_MK}
	 */
	protected void generateIncludeRulesLine()
	{
		this.includeRulesBuffer.append( "\n" + DEFAULT_INCLUDE );
	}

	/**
	 * Sets BACKSLASH_PATHS in the makefile on non-Unix platforms
	 */
	protected void generateBackSlashPath()
	{
		if (!File.separator.equals( "/" ))
		{
			this.genBackSlashPathBuffer
					.append( "\n#Set this value to use back slash as the path separator\n" );
			this.genBackSlashPathBuffer.append( "BACKSLASH_PATHS?=1\n" );
		}
	}

	/**
	 * Generates USE_FILEFIND to use the default definitions of some makefile
	 * variables
	 */
	protected void generateUseFileFind()
	{
		this.useFileFindBuffer
				.append( "\n#This variable should be defined to use the default values for variables which find files and subdirs in a directory\n" );
		this.useFileFindBuffer.append( "USE_FILEFIND=\n" );
	}

	/**
	 * Generates the makefile content. Will have to dynamically determine the
	 * content depending on the nature of the project
	 */
	abstract protected void generateMakefileContent( boolean force );
}
