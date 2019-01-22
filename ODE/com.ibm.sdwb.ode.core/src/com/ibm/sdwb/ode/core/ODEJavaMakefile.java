package com.ibm.sdwb.ode.core;

import java.io.File;
import java.util.Vector;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;


/**
 * @author kiranl
 * 
 * This class represents the makefile for java targets. It implements the
 * abstract method generateMakefileContent inherited from ODEMakefile.
 */
public class ODEJavaMakefile extends ODEMakefile
{
	// packageFragment will be set if this makefile object belongs to a java
	// project
	protected IPackageFragment packageFragment;
	// folder will be set if this makefile object belongs to a non-java project.
	// In this case, packageFragment will be null
	protected IFolder folder;
	protected IJavaProject javaProject;
	StringBuffer javaClassesBuffer = new StringBuffer();
	// This needs to be a vector as the list of subdirs has to be parsed
	// later for determining JAVAC_SUBDIRS line
	Vector<Object> javaSubdirs = new Vector<Object>();
	StringBuffer javaDocSubdirsBuffer = new StringBuffer();
	StringBuffer javaBooleanValuesBuffer = new StringBuffer();
	StringBuffer javaDocBooleanValuesBuffer = new StringBuffer();
	StringBuffer jarTargetsBuffer = new StringBuffer();
	StringBuffer classGenSubdirsBuffer = new StringBuffer();

	/**
	 * Constructor Creates an ODEMakefile Object for a pagkage fragment. Does
	 * not physically create a makefile on disk.
	 */
	ODEJavaMakefile( IJavaProject project, IPackageFragment pack,
			IFolder folder, ODEPreferences prefs,
			ODEMakefileProperties mfProperties )
	{
		super( folder.getFile( prefs.b1 ), project.getProject(), mfProperties );
		this.javaProject = project;
		this.packageFragment = pack;
	}

	/**
	 * Constructor Creates an ODEMakefile Object for a folder. Does not
	 * physically create a makefile on disk.
	 */
	ODEJavaMakefile( IProject project, IFolder makefileFolder,
			ODEPreferences prefs, ODEMakefileProperties mfProperties )
	{
		super( makefileFolder.getFile( prefs.b1 ), project, mfProperties );
		this.javaProject = null;
		this.folder = makefileFolder;
	}

	/**
	 * This is a utility file, that returns the name of the package.
	 * 
	 * @return returns the name of the packageFragment associated with this file
	 */
	String packageName()
	{
		return this.packageFragment.getElementName();
	}

	/**
	 * Generates the makefile variables needed to enable the usage of java rules
	 */
	protected void generateJavaBooleanValues()
	{
		this.javaBooleanValuesBuffer
				.append( "\n#These values should be set to enable java targets\n" );
		this.javaBooleanValuesBuffer.append( "BUILDJAVA=\n" );
		this.javaBooleanValuesBuffer.append( "USE_JAVA_PASSES=\n" );
	}

	/**
	 * Generates the makefile variables needed to enable the usage of javadoc
	 * rules
	 */
	protected void generateJavaDocBooleanValues()
	{
		this.javaDocBooleanValuesBuffer
				.append( "\n#This value should be set to enable javadoc targets\nBUILDJAVADOCS=\n" );
		if (this.mfProperties.runJavadocAfterJavac)
		{
			this.javaDocBooleanValuesBuffer
					.append( "\n#Set this variable to run JAVADOC only after compiling all the java files\nJAVADOC_AFTER_JAVAC=\n" );
		}
	}

	/**
	 * This is where the makefilecontent is generated.
	 * 
	 * @param force: tells whether to overwrite the existing content or not
	 * @return void
	 */
	protected void generateMakefileContent( boolean force )
	{
		if (this.file.exists() && !force)
			return;
		ODEPreferences prefs = new ODEPreferences();
		prefs.readPreferences();
		generateTopOfMakefileFileComments();
		if (this.mfProperties.generateJavaClasses
				|| this.mfProperties.generateJars)
			generateJavaBooleanValues();
		generateBackSlashPath();
		if (this.mfProperties.generateJavaDocs)
		{
			generateJavaDocBooleanValues();
		}
		if (prefs.b6)
		{
			generateUseFileFind();
		}
		if (this.mfProperties.generateJavaClasses
				|| this.mfProperties.generateJavaDocs)
		{
			generateSubdirsLine();
			generateTargetsLine( prefs.b5 );
			generateClassGenSubdirLine();
		}
		if ((getDepth() == 2) && (this.mfProperties.generateJars))
			generateJarTargetsLine( prefs.b5 );
		generateIncludeRulesLine();
	}

	/**
	 * Knows how to generate a JAVA_CLASSES line in ODE, given certain criteria
	 * There are different ways, a JAVA_CLASSES line can be generated. if
	 * useExplicitName is defined: JAVA_CLASSES=file${JAVA_SUFF}
	 * file2${CLASS_SUFF} otherwise JAVA_CLASSES=${X:!sbls
	 * ${}!:M*.java:S;.java;${CLASS_SUFF};g} or if generateOneMakefilePerPackage
	 * 
	 * JAVA_CLASSES_DIRECTORY= A new rule should be added, to find javafiles
	 * down a subdirectory Here is how the new rules should look like: Avoid to
	 * go down each directory (speed factor) instead, collect java files from
	 * the top of the directory. Should only be added to the topLevelDirectory
	 * makefiles)
	 * 
	 */
	protected void generateTargetsLine( boolean hardCodeFileNames )
	{
		String javaSuffix = ".java";
		String classSuffix = "${CLASS_SUFF}";
		String javaClasses = "";
		try
		{
			// If package fragment is not null, it means that this makefile is
			// created
			// for a java project. Hence JDT can be used.
			// Else, it means that it is not a java project. Hence do not use
			// JDT to create makefiles
			if (this.packageFragment != null)
			{
				ICompilationUnit[] units = this.packageFragment
						.getCompilationUnits();
				for (int i = 0; i < units.length; i++)
				{
					if (inSameDirectory( units[i] ))
					{
						String str = units[i].getElementName();
						if (ODECommonUtilities.isJavaSourceFile( str ))
						{
							javaClasses += units[i].getElementName().substring(
									0,
									units[i].getElementName().lastIndexOf(
											javaSuffix ) )
									+ classSuffix + " ";
						}
					}
				}
			}
			else if (this.folder != null)
			{
				IResource[] members = this.folder.members();
				for (int i = 0; i < members.length; i++)
				{
					if ((members[i].getType() == IResource.FILE)
							&& (ODECommonUtilities.isJavaSourceFile( members[i]
									.getName() )))
					{
						javaClasses += members[i].getName().substring( 0,
								members[i].getName().lastIndexOf( javaSuffix ) )
								+ classSuffix + " ";
					}
				}
			}
			if (this.mfProperties.generateJavaClasses)
			{
				if (javaClasses.length() != 0)
				{
					this.javaClassesBuffer
							.append( "\n#The class files to build \nJAVA_CLASSES=" );
					if (hardCodeFileNames)
						this.javaClassesBuffer.append( javaClasses + "\n" );
					else
					{
						this.javaClassesBuffer.append( "${ALL_JAVA_CLASSES}\n" );
						this.javaClassesBuffer
								.append( "#The above includes all the classes in this directory\n" );
					}
				}
			}
			if (this.mfProperties.generateJavaDocs)
			{
				if (javaClasses.length() != 0)
				{
					this.javaClassesBuffer
							.append( "\n#Package that will be used to generate API documentation with the JAVADOC tool\n" );
					this.javaClassesBuffer
							.append( "#This will get the current package\n" );
					this.javaClassesBuffer
							.append( "JAVADOCS=${JAVA_PACKAGE_NAME}\n" );
					this.javaClassesBuffer
							.append( "#Directory where the documentation files are generated\n" );
					this.javaClassesBuffer.append( "JAVADOC_GENDIR=" );
					if (this.packageFragment != null)
					{
						String outputPath = ODECommonUtilities
								.getOutputPath( this.javaProject );
						if (outputPath != null)
						{
							if ((!outputPath.startsWith( "/" ))
									&& (!outputPath.startsWith( "\\" )))
								outputPath = File.separator + outputPath;
							if ((!outputPath.endsWith( "/" ))
									&& (!outputPath.endsWith( "\\" )))
								outputPath += File.separator;
							outputPath = "${SANDBOXBASE}" + outputPath + ".."
									+ File.separator + "javadocs"
									+ File.separator + "${MAKEDIR}";
							this.javaClassesBuffer.append( outputPath + "\n" );
						}
						else
						{
							this.javaClassesBuffer
									.append( "/export/javadocs\n" );
						}
					}
					else
						this.javaClassesBuffer.append( "/export/javadocs\n" );
				}
			}
		}
		catch (JavaModelException ex)
		{
			System.out.println( "Exception occured while generating makefiles "
					+ ex );
		}
		catch (CoreException ex)
		{
			System.out.println( "Exception occured while generating makefiles "
					+ ex );
		}
	}

	/**
	 * This is a utility method. Checks to see if a compilation unit is located
	 * in the same file as the current makefile.
	 * 
	 * @param unit represents the Compilation unit
	 * @return is true is the unit is in the same physical directory as current
	 *         makefile. and false otherwise.
	 * 
	 */
	private boolean inSameDirectory( ICompilationUnit unit )
	{
		String currentFolderName = "";
		String givenFolderName = "";
		try
		{
			IFolder newFolder = (IFolder)unit.getCorrespondingResource()
					.getParent();
			givenFolderName = newFolder.getFullPath().toString();
			IFolder parentFolder = (IFolder)this.file.getParent();
			currentFolderName = parentFolder.getFullPath().toString();
		}
		catch (JavaModelException ex)
		{
			System.out.println( "Exception occured while generating makefiles "
					+ ex );
		}
		return currentFolderName.equals( givenFolderName );
	}

	/**
	 * Knows how to generate a JAVAC_SUBDIRS= line in ODE, given certain
	 * criteria
	 * 
	 */
	protected void generateSubdirsLine()
	{
		try
		{
			IFolder parentFolder = (IFolder)this.file.getParent();
			IResource[] members = parentFolder.members();
			String javaDocDirs = "";
			for (int i = 0; i < members.length; i++)
			{
				if (members[i].getType() == IResource.FOLDER)
				{
					ODEJavaMakefile newMake = (ODEJavaMakefile)ODECommonUtilities
							.findInContainer( (IFolder)members[i],
									ODEMakefileGenerator.javaMakefilesContainer );
					if (newMake != null)
					{
						if (this.mfProperties.generateJavaClasses)
							this.javaSubdirs.add( ODECommonUtilities
									.getDirectoryName( newMake.file ) );
						if (this.mfProperties.generateJavaDocs)
							javaDocDirs += ODECommonUtilities
									.getDirectoryName( newMake.file )
									+ " ";
					}
				}
			}
			if (javaDocDirs.length() != 0)
			{
				this.javaDocSubdirsBuffer
						.append( "\n#Directories to traverse to generate JAVADOC Targets\n" );
				this.javaDocSubdirsBuffer.append( "JAVADOC_SUBDIRS="
						+ javaDocDirs + "\n" );
			}
		}
		catch (CoreException ex)
		{
			System.out.println( "Exception occured while generating makefiles "
					+ ex );
		}
	}

	protected void generateJarTargetsLine( boolean hardCodeFileNames )
	{
		try
		{
			IFolder parentFolder = (IFolder)this.file.getParent();
			String listOfDirs = "";
			// get the value for OTHER_JAR_OBJECTS
			IResource[] members = parentFolder.members();
			for (int i = 0; i < members.length; i++)
			{
				if (members[i].getType() == IResource.FOLDER)
				{
					ODEJavaMakefile newMake = (ODEJavaMakefile)ODECommonUtilities
							.findInContainer( (IFolder)members[i],
									ODEMakefileGenerator.javaMakefilesContainer );
					if (newMake != null)
						listOfDirs += ((IFolder)members[i]).getName() + " ";
				}
			}
			this.jarTargetsBuffer.append( "\n#The jar file to be created \n" )
					.append( "JAR_LIBRARIES=" );
			this.jarTargetsBuffer.append( this.mfProperties.jarfileName );
			if (listOfDirs.length() != 0)
			{
				this.jarTargetsBuffer
						.append( "\n#List of directories to be included in the jar file\n" );
				if (hardCodeFileNames)
					this.jarTargetsBuffer.append( "OTHER_JAR_OBJECTS="
							+ listOfDirs );
				else
					this.jarTargetsBuffer
							.append( "OTHER_JAR_OBJECTS=${ALL_SUBDIRS:Nrules_mk}\n" );
			}
		}
		catch (Exception ex)
		{
			System.out.println( "Exception occured while generating makefiles "
					+ ex );
		}
	}

	protected void generateClassGenSubdirLine()
	{
		this.classGenSubdirsBuffer.append(
				"\n#Location to output class files \n" ).append(
				"CLASSGEN_SUBDIR=" );
		// get the output path if it is a java project, else set it to
		// /export/classes
		if (this.packageFragment != null)
		{
			String outputPath = ODECommonUtilities.getOutputPath(
					this.javaProject ).replace( '\\', '/' );
			if (outputPath != null)
			{
				int index;
				StringBuffer op;
				while ((index = outputPath.indexOf( '/' )) >= 0)
				{
					op = new StringBuffer( outputPath );
					op = op.replace( index, index + 1, "${DIRSEP}" );
					outputPath = op.toString();
				}
				this.classGenSubdirsBuffer.append( outputPath + "\n" );
				return;
			}
		}
		// since the output path or the package fragment is null, use the
		// default value
		this.classGenSubdirsBuffer.append( "${DIRSEP}export${DIRSEP}classes\n" );
	}
}
