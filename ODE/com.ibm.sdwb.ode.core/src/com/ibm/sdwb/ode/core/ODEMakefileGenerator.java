package com.ibm.sdwb.ode.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Vector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;


/**
 * @author ODE Team
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class ODEMakefileGenerator extends ODEAction
{
	// holds all the java makefiles in the project
	protected static ODEMakefileContainer javaMakefilesContainer;
	// holds all the c/c++ makefiles in the project
	protected static ODEMakefileContainer cMakefilesContainer;

	public void run( IAction arg0 )
	{
		super.run( arg0 );
		run();
	}

	public void run()
	{
		try
		{
			javaMakefilesContainer = null;
			cMakefilesContainer = null;
			// need this variable to remember if the java project
			// has non-java (c/c++) code
			boolean javaProjectContainsOtherCode = true;
			if (this.javaProject != null)
			{
				ODECommonUtilities.createBuildconfLocal( this.javaProject );
				ODEMakefileProperties mfProperties = new ODEMakefileProperties(
						this.project );
				mfProperties.readProperties();
				javaMakefilesContainer = new ODEMakefileContainer();
				// parse this Java project
				ODEJavaProjectParser javaParser = new ODEJavaProjectParser();
				javaParser.parseFileStructure( this.pkgFragment,
						this.javaProject, javaMakefilesContainer, mfProperties );
				// if any of the C targets are selected, then parse this project
				// looking for c/c++ files
				if (mfProperties.generateObjects
						|| mfProperties.generateIncludes)
				{
					cMakefilesContainer = new ODEMakefileContainer();
					ODECProjectParser cParser = new ODECProjectParser();
					cParser.parseFileStructure( this.folder, this.project,
							cMakefilesContainer, mfProperties );
				}
				else
					javaProjectContainsOtherCode = false;
			}
			else if ((this.cProject != null) || (this.project != null))
			{
				ODECommonUtilities.createBuildconfLocal( this.project );
				ODEMakefileProperties mfProperties = new ODEMakefileProperties(
						this.project );
				mfProperties.readProperties();
				cMakefilesContainer = new ODEMakefileContainer();
				ODECProjectParser cParser = new ODECProjectParser();
				// parse this c/c++ project
				cParser.parseFileStructure( this.folder, this.project,
						cMakefilesContainer, mfProperties );
				// if any of the Java targets are selected, parse this project
				// looking for java files
				if (mfProperties.generateJavaClasses
						|| mfProperties.generateJars
						|| mfProperties.generateJavaDocs)
				{
					javaMakefilesContainer = new ODEMakefileContainer();
					ODEJavaProjectParser javaParser = new ODEJavaProjectParser();
					// call the method which is not using JDT in
					// ODEJavaProjectParser as we
					// this is a non-java project and hence JDT cannot be relied
					// upon
					javaParser.parseFileStructure( this.folder, this.project,
							javaMakefilesContainer, mfProperties );
				}
			}
			// create the java makefile objects
			if ((javaMakefilesContainer != null)
					&& (javaMakefilesContainer.hasElements()))
			{
				javaMakefilesContainer
						.generateMakefileContentFromContainer( this.prefs.b2 );
			}
			// create the c/c++ makefile objects
			if ((cMakefilesContainer != null)
					&& (cMakefilesContainer.hasElements()))
			{
				cMakefilesContainer
						.generateMakefileContentFromContainer( this.prefs.b2 );
			}
			IResource[] members = this.project.members();
			Vector list = null;
			for (int i = 0; i < members.length; i++)
			{
				// get the paths to all the non-empty folders under src
				if ((members[i].getType() == IResource.FOLDER)
						&& (members[i].getName().equalsIgnoreCase( "src" )))
				{
					list = ODECommonUtilities
							.getAllFolders( (IFolder)members[i] );
					break;
				}
			}
			// create makefile in each of the directories from the list
			// obtained above
			
			if (list != null)  // sdmcjunk - added if condition in case no
			{				   // members are found
				for (int i = 0; i < list.size(); i++)
				{
					createMakefileInFolder( (IFolder)list.elementAt( i ),
							javaProjectContainsOtherCode );
				}
				this.project.refreshLocal( IResource.DEPTH_INFINITE, null );
			}
		}
		catch (Exception e)
		{
			System.out.println( "Exception: " + e );
			e.printStackTrace();
		}
	}

	/**
	 * Creates the makefile in the input folder by first checking if the java
	 * and C makefile objects exist in that folder. It then merges the contents
	 * of the two makefiles into one
	 * 
	 * @param iFolder The folder where the makefile has to be created
	 * @param javaProjectContainsOtherCode Indicates if the project contains any
	 *            other code except java
	 */
	private void createMakefileInFolder( IFolder iFolder,
			boolean javaProjectContainsOtherCode ) throws CoreException,
			IOException
	{
		ODEJavaMakefile javaMakefile = null;
		ODECMakefile cMakefile = null;
		boolean hasJavaMakefile = false;
		boolean hasCMakefile = false;
		StringBuffer makefileContent = new StringBuffer();
		// check if there is a java makefile for this folder
		if ((javaMakefilesContainer != null)
				&& (javaMakefilesContainer.hasElements()))
		{
			javaMakefile = (ODEJavaMakefile)ODECommonUtilities.findInContainer(
					iFolder, javaMakefilesContainer );
			if (javaMakefile != null)
			{
				IResource makefile = iFolder.findMember( this.prefs.b1 );
				if ((makefile != null) && (makefile.exists()))
				{
					if (this.prefs.b2)
						makefile.delete( true, null );
					else
						return;
				}
				hasJavaMakefile = true;
			}
		}
		// check if there is a c makefile for this folder
		if ((cMakefilesContainer != null)
				&& (cMakefilesContainer.hasElements()))
		{
			cMakefile = (ODECMakefile)ODECommonUtilities.findInContainer(
					iFolder, cMakefilesContainer );
			if (cMakefile != null)
			{
				IResource makefile = iFolder.findMember( this.prefs.b1 );
				if ((makefile != null) && (makefile.exists()))
				{
					if (this.prefs.b2)
						makefile.delete( true, null );
					else
						return;
				}
				hasCMakefile = true;
			}
		}
		if (hasJavaMakefile || hasCMakefile)
		{
			// get the content for the makefile
			createContent( javaMakefile, cMakefile, makefileContent,
					javaProjectContainsOtherCode );
			// create the actual file, will need to call the method only once
			// as only one makefile should be created
			// if the directory has the java makefile, it'll also contain
			// the information for c/c++ targets
			if (hasJavaMakefile)
				createActualFile( javaMakefile.file, makefileContent );
			else if (hasCMakefile)
				createActualFile( cMakefile.file, makefileContent );
		}
	}

	/**
	 * Creates the content for the makefile
	 * 
	 * @param javaMakefile makefile containing the information for java targets
	 * @param cMakefile makefile containing the information for c targets
	 * @param javaProjectContainsOtherCode Indicates if the project contains any
	 *            other code except java
	 */
	private void createContent( ODEJavaMakefile javaMakefile,
			ODECMakefile cMakefile, StringBuffer makefileContent,
			boolean javaProjectContainsOtherCode )
	{
		String makefileEntry;
		// create top-level comments in the makefile, need to get them only once
		if (javaMakefile != null)
			makefileContent.append( javaMakefile.commentBuffer.toString() );
		else if (cMakefile != null)
			makefileContent.append( cMakefile.commentBuffer.toString() );
		// the SUBDIRS variable is common to both Java and C
		makefileEntry = getObjectsSubdirsInfo( javaMakefile, cMakefile,
				javaProjectContainsOtherCode );
		if (makefileEntry.length() != 0)
			makefileContent.append( makefileEntry );
		// create the other c/c++ targets
		makefileEntry = getCTargetsInfo( cMakefile );
		if (makefileEntry.length() != 0)
			makefileContent.append( makefileEntry );
		// create the other java targets
		makefileEntry = getJavaTargetsInfo( javaMakefile,
				javaProjectContainsOtherCode );
		if (makefileEntry.length() != 0)
			makefileContent.append( makefileEntry );
		makefileContent.append( getRules( javaMakefile, cMakefile ) );
	}

	/**
	 * Creates a string for JAVAC_SUBDIRS or OBJECTS_SUBDIRS
	 * 
	 * @param javaMakefile makefile containing the information for java targets
	 * @param cMakefile makefile containing the information for c targets
	 * @param javaProjectContainsOtherCode Indicates if the project contains any
	 *            other code except java
	 * @return String for JAVAC_SUBDIRS or OBJECTS_SUBDIRS
	 */
	private String getObjectsSubdirsInfo( ODEJavaMakefile javaMakefile,
			ODECMakefile cMakefile, boolean javaProjectContainsOtherCode )
	{
		String subdirs = "";
		int depth = 0;
		// the java and c makefiles can have subdirs in common and there
		// should not be any duplicates in OBJECTS_SUBDIRS
		if (javaMakefile != null)
		{
			depth = javaMakefile.getDepth();
			for (int i = 0; i < javaMakefile.javaSubdirs.size(); i++)
			{
				subdirs += javaMakefile.javaSubdirs.elementAt( i ) + " ";
			}
			// got the subdirs from the java makefile, now add only
			// any extra subdirs from the c makefile
			if (cMakefile != null)
			{
				for (int i = 0; i < cMakefile.objectsSubdirs.size(); i++)
				{
					if (!javaMakefile.javaSubdirs
							.contains( cMakefile.objectsSubdirs.elementAt( i ) ))
						subdirs += cMakefile.objectsSubdirs.elementAt( i )
								+ " ";
				}
			}
		}
		else if (cMakefile != null)
		{
			depth = cMakefile.getDepth();
			for (int i = 0; i < cMakefile.objectsSubdirs.size(); i++)
			{
				subdirs += cMakefile.objectsSubdirs.elementAt( i ) + " ";
			}
			// at this point, we know that there is no java makefile
			// for this directory. Hence need not check for it
		}
		if (subdirs.length() != 0)
		{
			String comment = "\n# Directories to traverse to generate OBJECTS and JAVAC Targets \n";
			// if the project contains only java code, then use JAVAC_SUBDIRS
			if (javaProjectContainsOtherCode)
				comment += "OBJECTS_SUBDIRS = ";
			else
				comment += "JAVAC_SUBDIRS = ";
			if (this.prefs.b5)
				comment += subdirs + "\n";
			else
			{
				if (depth == 2)
					comment += "${ALL_SUBDIRS:Nrules_mk}\n";
				else
					comment += "${ALL_SUBDIRS}\n";
			}
			return comment;
		}
		return ("");
	}

	/**
	 * Returns the makefile content for the c/c++ targets
	 * 
	 * @param cMakefile the makefile containing the c/c++ targets
	 */
	private String getCTargetsInfo( ODECMakefile cMakefile )
	{
		String cTargetInfo = "";
		if (cMakefile == null)
			return "";
		if (cMakefile.objectsBuffer.length() != 0)
			cTargetInfo += cMakefile.objectsBuffer.toString();
		if (cMakefile.expincSubdirsBuffer.length() != 0)
			cTargetInfo += cMakefile.expincSubdirsBuffer.toString();
		if (cMakefile.includesBuffer.length() != 0)
			cTargetInfo += cMakefile.includesBuffer.toString();
		return cTargetInfo;
	}

	/**
	 * Returns the makefile content for the java targets
	 * 
	 * @param javaMakefile the makefile containing the java targets
	 * @param javaProjectContainsOtherCode Indicates if the project contains any
	 *            other code except java
	 */
	private String getJavaTargetsInfo( ODEJavaMakefile javaMakefile,
			boolean javaProjectContainsOtherCode )
	{
		String javaTargetInfo = "";
		if (javaMakefile == null)
			return "";
		if (javaMakefile.javaBooleanValuesBuffer.length() != 0)
		{
			String javaBooleans = javaMakefile.javaBooleanValuesBuffer
					.toString();
			// if the java project has other code, then do not set
			// USE_JAVA_PASSES
			if (javaProjectContainsOtherCode)
			{
				int index = javaBooleans.indexOf( "USE_JAVA_PASSES" );
				if (index != -1)
					javaBooleans = javaBooleans.substring( 0, index );
			}
			javaTargetInfo += javaBooleans;
		}
		if (javaMakefile.genBackSlashPathBuffer.length() != 0)
			javaTargetInfo += javaMakefile.genBackSlashPathBuffer.toString();
		if (javaMakefile.useFileFindBuffer.length() != 0)
			javaTargetInfo += javaMakefile.useFileFindBuffer.toString();
		if (javaMakefile.javaDocBooleanValuesBuffer.length() != 0)
			javaTargetInfo += javaMakefile.javaDocBooleanValuesBuffer
					.toString();
		if (javaMakefile.javaClassesBuffer.length() != 0)
			javaTargetInfo += javaMakefile.javaClassesBuffer.toString();
		if (javaMakefile.javaDocSubdirsBuffer.length() != 0)
			javaTargetInfo += javaMakefile.javaDocSubdirsBuffer.toString();
		if (javaMakefile.classGenSubdirsBuffer.length() != 0)
			javaTargetInfo += javaMakefile.classGenSubdirsBuffer.toString();
		if (javaMakefile.jarTargetsBuffer.length() != 0)
			javaTargetInfo += javaMakefile.jarTargetsBuffer.toString();
		return javaTargetInfo;
	}

	/**
	 * Returns the include rules buffer
	 * 
	 * @paran javaMakefile the makefile containing the java targets
	 * @param cMakefile the makefile containing the c/c++ targets
	 */
	private String getRules( ODEJavaMakefile javaMakefile,
			ODECMakefile cMakefile )
	{
		// get the include rules buffer from the java makefile, if it
		// is null, get it from the c makefile. Will be same in both
		// the cases
		if (javaMakefile != null)
			return (javaMakefile.includeRulesBuffer.toString());
		if (cMakefile != null)
			return (cMakefile.includeRulesBuffer.toString());
		return "";
	}

	/**
	 * Creates the actual makefile
	 * 
	 * @param iFile The makefile to be created
	 * @param makefileContent The content for the makefile
	 */
	private void createActualFile( IFile iFile, StringBuffer makefileContent )
			throws CoreException, IOException
	{
		byte[] buf = new byte[makefileContent.length()];
		for (int i = 0; i < makefileContent.length(); i++)
			buf[i] = (byte)makefileContent.charAt( i );
		ByteArrayInputStream byteStream = new ByteArrayInputStream( buf );
		iFile.create( byteStream, this.prefs.b2, null );
		byteStream.close();
	}
}
