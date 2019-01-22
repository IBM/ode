package com.ibm.sdwb.ode.core;

import java.util.Vector;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;


/**
 * @author kiranl
 * 
 * This class contains the utilities to parse a Java project
 */
public class ODECProjectParser
{
	public void parseFileStructure( IFolder folder, IProject project,
			ODEMakefileContainer cMakefilesContainer,
			ODEMakefileProperties mfProperties )
	{
		Vector<Object> foldersList = null;
		try
		{
			ODEPreferences prefs = new ODEPreferences();
			prefs.readPreferences();
			// get the folders under the input folder if the makefile generation
			// is run on a folder, otherwise get the foldres under the "src"
			// folder
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
					createMakefileObject( folder, (IFolder)foldersList
							.elementAt( i ), project, prefs, mfProperties,
							cMakefilesContainer );
				}
			}
		}
		catch (Exception ex)
		{ // ignore for now
		}
	}

	/**
	 * Creates a c makefile in a given folder
	 */
	private void createMakefileObject( IFolder topLevelFolder, IFolder folder,
			IProject project, ODEPreferences prefs,
			ODEMakefileProperties mfProperties,
			ODEMakefileContainer cMakefilesContainer )
	{
		String topLevelResource;
		String folderName;
		boolean hasSrcFiles = containsSourceFiles( folder );
		// if this folder has a c/c++ src file, create a makefile object in it
		// and then recursively create makefile objects in all the folders going
		// up to
		// the project or the topLevelFolder
		if (hasSrcFiles)
		{
			ODECMakefile cMakefile = new ODECMakefile( project, folder, prefs,
					mfProperties );
			cMakefilesContainer.add( cMakefile );
			if (topLevelFolder != null)
			{
				topLevelResource = topLevelFolder.getName();
				if (folder.getName().equals( topLevelResource ))
					return;
			}
			else
				topLevelResource = project.getProject().getName();
			folderName = folder.getParent().getName();
			while (!folderName.equals( topLevelResource ))
			{
				folder = (IFolder)folder.getParent();
				ODECMakefile newMakefile = new ODECMakefile( project, folder,
						prefs, mfProperties );
				cMakefilesContainer.add( newMakefile );
				folderName = folder.getParent().getName();
			}
			if (topLevelFolder != null)
			{
				if (folderName.equals( topLevelFolder.getName() ))
				{
					folder = (IFolder)folder.getParent();
					ODECMakefile newMakefile = new ODECMakefile( project,
							folder, prefs, mfProperties );
					cMakefilesContainer.add( newMakefile );
				}
			}
		}
	}

	/**
	 * Determines if a folder has c/c++ source or header files
	 */
	private boolean containsSourceFiles( IFolder folder )
	{
		try
		{
			IResource[] files = folder.members();
			for (int i = 0; i < files.length; i++)
			{
				if ((files[i].getType() == IResource.FILE)
						&& (ODECommonUtilities.isCSourceFile( files[i]
								.getName() ) || ODECommonUtilities
								.isCHeaderFile( files[i].getName() )))
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
