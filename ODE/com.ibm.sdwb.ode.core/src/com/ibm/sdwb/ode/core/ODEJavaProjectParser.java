package com.ibm.sdwb.ode.core;

import java.util.Vector;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;


/**
 * @author kiranl
 * 
 * This class contains the utilities to parse a Java project
 */
public class ODEJavaProjectParser
{
	/**
	 * Parses the project or the package fragment looking for java resources
	 * using JDT
	 * 
	 * @param pkgFrag The package fragment to be parsed
	 * @param javaProject The java project containing the package fragment
	 * @param javaMakefilesContainer Makefile container to hold the java
	 *            makefile objects
	 * @param mfProperties Makefile properties
	 * 
	 */
	public void parseFileStructure( IPackageFragment pkgFrag,
			IJavaProject javaProject,
			ODEMakefileContainer javaMakefilesContainer,
			ODEMakefileProperties mfProperties )
	{
		try
		{
			// Get the preferences and properties so I know what to do.
			ODEPreferences prefs = new ODEPreferences();
			prefs.readPreferences();
			// If pkgFrag is not null, it means that the makefile generation is
			// run on
			// a package fragment. Else, it means that the makefile generation
			// is run on the java project
			if (pkgFrag != null)
			{
				if (pkgFrag.hasSubpackages())
				{
					IPackageFragment[] allPackages = javaProject
							.getPackageFragments();
					for (int i = 0; i < allPackages.length; i++)
					{
						if (allPackages[i].getElementName().startsWith(
								pkgFrag.getElementName() + "." )
								|| allPackages[i].getElementName().equals(
										pkgFrag.getElementName() ))
						{
							createMultipleMakefileObjectsFromPackageFragment(
									pkgFrag, javaProject, allPackages[i],
									prefs, mfProperties, javaMakefilesContainer );
						}
					}
				}
				else
				{
					createMultipleMakefileObjectsFromPackageFragment( pkgFrag,
							javaProject, pkgFrag, prefs, mfProperties,
							javaMakefilesContainer );
				}
			}
			else
			{
				IPackageFragment[] allPackages = javaProject
						.getPackageFragments();
				for (int i = 0; i < allPackages.length; i++)
				{
					createMultipleMakefileObjectsFromPackageFragment( pkgFrag,
							javaProject, allPackages[i], prefs, mfProperties,
							javaMakefilesContainer );
				}
			}
		}
		catch (Exception ex)
		{ // ignore for now
		}
	}

	/**
	 * Parses the project or the folder looking for java resources by looking at
	 * the file system Need this method as JDT cannot be used when parsing a
	 * non-java project looking for java resources.
	 * 
	 * @param folder The folder to be parsed
	 * @param project The project containing the folder
	 * @param javaMakefilesContainer Makefile container to hold the java
	 *            makefile objects
	 * @param mfProperties Makefile properties
	 * 
	 */
	public void parseFileStructure( IFolder folder, IProject project,
			ODEMakefileContainer javaMakefilesContainer,
			ODEMakefileProperties mfProperties )
	{
		Vector<Object> foldersList = null;
		try
		{
			// Get the preferences and properties so I know what to do.
			ODEPreferences prefs = new ODEPreferences();
			prefs.readPreferences();
			// If folder is not null, it means that the makefile generation is
			// run on
			// a folder. Else, it means that the makefile generation is run on
			// the project
			if (folder != null)
			{
				foldersList = ODECommonUtilities.getAllFolders( folder );
				foldersList.add( folder );
			}
			else
			{
				IResource[] elemInfo = project.members();
				for (int i = 0; i < elemInfo.length; i++)
				{
					if ((elemInfo[i].getType() == IResource.FOLDER)
							&& (elemInfo[i].getName().equalsIgnoreCase( "src" )))
					{
						foldersList = ODECommonUtilities
								.getAllFolders( (IFolder)elemInfo[i] );
						break;
					}
					continue;
				}
			}
			if ((foldersList != null) && (foldersList.size() > 0))
			{
				for (int i = 0; i < foldersList.size(); i++)
				{
					createMakefileObjectsFromFolder( folder,
							(IFolder)foldersList.elementAt( i ), project,
							prefs, mfProperties, javaMakefilesContainer );
				}
			}
		}
		catch (Exception ex)
		{ // ignore for now
		}
	}

	/**
	 * Creates the makefile objects in the given package
	 * 
	 * @param pkgFrag The package fragment on which the selection is made
	 * @param project The java project containing the package fragment
	 * @param singlePackage The package in which the makefiles are to be created
	 * @param prefs The user preferences
	 * @param makefileProperties The makefile properties for this package
	 * @param javaMakefilesContainer The container to store the created java
	 *            makefile objects
	 */
	private void createMultipleMakefileObjectsFromPackageFragment(
			IPackageFragment pkgFrag, IJavaProject project,
			IPackageFragment singlePackage, ODEPreferences prefs,
			ODEMakefileProperties makefileProperties,
			ODEMakefileContainer javaMakefilesContainer )
	{
		String topLevelResource;
		String folderName;
		try
		{
			// Currently this will only list files, which are compilation units.
			// Files with the .java file extension.
			if (singlePackage.containsJavaResources())
			{
				ICompilationUnit[] units = singlePackage.getCompilationUnits();
				if (units != null && units.length > 0)
				{
					IFolder folder = ODECommonUtilities
							.getFolderName( singlePackage );
					ODEJavaMakefile makefile = new ODEJavaMakefile( project,
							singlePackage, folder, prefs, makefileProperties );
					javaMakefilesContainer.add( makefile );
					// pkgFrag will be null if the selection is made on a java
					// project
					if (pkgFrag != null)
					{
						// set topLevelResource to the package fragment
						topLevelResource = (ODECommonUtilities
								.getFolderName( pkgFrag )).getName();
						if (folder.getName().equals( topLevelResource ))
							return;
					}
					else
					{
						// set topLevelResource to the java project
						topLevelResource = project.getProject().getName();
					}
					folderName = folder.getParent().getName();
					// Recursively generate Makefiles in all the folders
					// going up until topLevelResource is reached
					while (!folderName.equals( topLevelResource ))
					{
						folder = (IFolder)folder.getParent();
						ODEJavaMakefile newMakefile = new ODEJavaMakefile(
								project, singlePackage, folder, prefs,
								makefileProperties );
						javaMakefilesContainer.add( newMakefile );
						folderName = folder.getParent().getName();
					}
					if (pkgFrag != null)
					{
						if (folderName.equals( topLevelResource ))
						{
							folder = (IFolder)folder.getParent();
							ODEJavaMakefile newMakefile = new ODEJavaMakefile(
									project, singlePackage, folder, prefs,
									makefileProperties );
							javaMakefilesContainer.add( newMakefile );
						}
					}
				}
			}
		}
		catch (JavaModelException ex)
		{
			System.out
					.println( "ODEGenerateMakefile: Error while generating the ODE makefiles"
							+ ex );
			ex.printStackTrace();
		}
		catch (Exception ex)
		{
			System.out
					.println( "ODEGenerateMakefile: Error while generating the ODE makefiles"
							+ ex );
			ex.printStackTrace();
		}
	}

	/**
	 * Creates the makefile objects in the given folder. It has the same
	 * functionality as the above method, except that this does not use JDT.
	 * 
	 * @param topLevelFolder The folder on which the selection is made
	 * @param folder The folder in which the makefiles are to be created
	 * @param project The project containing the folder
	 * @param prefs The user preferences
	 * @param makefileProperties The makefile properties for this package
	 * @param javaMakefilesContainer The container to store the created java
	 *            makefile objects
	 */
	private void createMakefileObjectsFromFolder( IFolder topLevelFolder,
			IFolder folder, IProject project, ODEPreferences prefs,
			ODEMakefileProperties mfProperties,
			ODEMakefileContainer javaMakefilesContainer )
	{
		String topLevelResource;
		String folderName;
		boolean hasSrcFiles = containsSourceFiles( folder );
		// if this folder has a java src file, create a makefile object in it
		// and then recursively create makefile objects in all the folders going
		// up to
		// the project level or the folder where the makefile generation is run
		if (hasSrcFiles)
		{
			ODEJavaMakefile javaMakefile = new ODEJavaMakefile( project,
					folder, prefs, mfProperties );
			javaMakefilesContainer.add( javaMakefile );
			if (topLevelFolder != null)
			{
				// this means that the makefile generation is run on a folder
				// set topLevelResource to the folder
				topLevelResource = topLevelFolder.getName();
				if (folder.getName().equals( topLevelResource ))
					return;
			}
			else
			{
				// this means that the makefile generation is run on a project
				// set topLevelResource to the project
				topLevelResource = project.getProject().getName();
			}
			folderName = folder.getParent().getName();
			// Recursively generate Makefiles in all the folders
			// going up until topLevelResource is reached
			while (!folderName.equals( topLevelResource ))
			{
				folder = (IFolder)folder.getParent();
				ODEJavaMakefile newMakefile = new ODEJavaMakefile( project,
						folder, prefs, mfProperties );
				javaMakefilesContainer.add( newMakefile );
				folderName = folder.getParent().getName();
			}
			if (topLevelFolder != null)
			{
				if (folderName.equals( topLevelFolder.getName() ))
				{
					folder = (IFolder)folder.getParent();
					ODEJavaMakefile newMakefile = new ODEJavaMakefile( project,
							folder, prefs, mfProperties );
					javaMakefilesContainer.add( newMakefile );
				}
			}
		}
	}

	/**
	 * Determines if a folder has Java source files
	 */
	private boolean containsSourceFiles( IFolder folder )
	{
		try
		{
			IResource[] files = folder.members();
			for (int i = 0; i < files.length; i++)
			{
				if ((files[i].getType() == IResource.FILE)
						&& (ODECommonUtilities.isJavaSourceFile( files[i]
								.getName() )))
					return true;
			}
			return false;
		}
		catch (Exception e)
		{
			System.out.println( "Exception " + e );
			return false;
		}
	}
}
