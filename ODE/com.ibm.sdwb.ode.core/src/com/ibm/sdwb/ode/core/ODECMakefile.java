package com.ibm.sdwb.ode.core;

import java.util.Vector;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;


/**
 * @author kiranl
 * 
 * This class represents the makefile for c/c++ targets. It implements the
 * abstract method generateMakefileContent inherited from ODEMakefile.
 */
public class ODECMakefile extends ODEMakefile
{
	StringBuffer objectsBuffer = new StringBuffer();
	Vector<Object> objectsSubdirs = new Vector<Object>();
	StringBuffer includesBuffer = new StringBuffer();
	StringBuffer expincSubdirsBuffer = new StringBuffer();

	/**
	 * Constructor for ODECMakefile.
	 */
	public ODECMakefile( IProject proj, IFolder folder, ODEPreferences prefs,
			ODEMakefileProperties mfProperties )
	{
		super( folder.getFile( prefs.b1 ), proj, mfProperties );
	}

	/**
	 * @see com.ibm.sdwb.ode.core.ODEMakefile#generateMakefileContent(boolean)
	 */
	protected void generateMakefileContent( boolean force )
	{
		if (this.file.exists() && !force)
			return;
		ODEPreferences prefs = new ODEPreferences();
		prefs.readPreferences();
		generateTopOfMakefileFileComments();
		if (prefs.b6)
		{
			generateUseFileFind();
		}
		if (this.mfProperties.generateObjects)
		{
			generateObjectsSubdirsLine();
			generateObjectsLine( prefs.b5 );
		}
		if (this.mfProperties.generateIncludes)
		{
			generateExpincSubdirsLine( prefs.b5 );
			generateIncludesLine( prefs.b5 );
		}
		generateIncludeRulesLine();
	}

	/**
	 * Method generateObjectsLine.
	 * 
	 * @param b
	 */
	private void generateObjectsSubdirsLine()
	{
		try
		{
			IResource[] members = this.file.getParent().members();
			for (int i = 0; i < members.length; i++)
			{
				if ((members[i].getType() == IResource.FOLDER))
				{
					ODECMakefile cMakefile = (ODECMakefile)ODECommonUtilities
							.findInContainer( (IFolder)members[i],
									ODEMakefileGenerator.cMakefilesContainer );
					if (cMakefile != null)
					{
						this.objectsSubdirs.add( ODECommonUtilities
								.getDirectoryName( cMakefile.file ) );
					}
				}
			}
		}
		catch (Exception e)
		{
			System.out
					.println( "Exception occured while generating makefiles: "
							+ e );
		}
	}

	/**
	 * Method generateObjectsSubdirsLine.
	 * 
	 * @param b
	 */
	private void generateObjectsLine( boolean hardcodeFileNames )
	{
		try
		{
			String objects = "";
			IResource[] members = this.file.getParent().members();
			for (int i = 0; i < members.length; i++)
			{
				if ((members[i].getType() == IResource.FILE)
						&& (ODECommonUtilities.isCSourceFile( members[i]
								.getName() )))
				{
					objects += members[i].getName().substring( 0,
							members[i].getName().lastIndexOf( "." ) )
							+ "${OBJ_SUFF} ";
				}
			}
			if (objects.length() != 0)
			{
				this.objectsBuffer
						.append( "\n#The object files to be created\nOBJECTS= " );
				if (hardcodeFileNames)
					this.objectsBuffer.append( objects + "\n" );
				else
				{
					this.objectsBuffer.append( "${ALL_C_OFILES}\n" );
				}
				this.objectsBuffer
						.append( "\n#Add below the list of executables to be created \nPROGRAMS=\n" );
			}
		}
		catch (Exception e)
		{
			System.out
					.println( "Exception occured while generating makefiles: "
							+ e );
		}
	}

	/**
	 * Method generateIncludesLine.
	 * 
	 * @param b
	 */
	private void generateIncludesLine( boolean hardcodeFileNames )
	{
		try
		{
			String includes = "";
			IResource[] members = this.file.getParent().members();
			for (int i = 0; i < members.length; i++)
			{
				if ((members[i].getType() == IResource.FILE)
						&& (ODECommonUtilities.isCHeaderFile( members[i]
								.getName() )))
				{
					includes += members[i].getName() + " ";
				}
			}
			if (includes.length() != 0)
			{
				this.includesBuffer
						.append( "\n#list of header files to export\nINCLUDES = " );
				if (hardcodeFileNames)
					this.includesBuffer.append( includes + "\n" );
				else
				{
					this.includesBuffer.append( includes + "\n" );
				}
				this.includesBuffer
						.append( "\n#This is set by default, should be changed if needed\nEXPDIR ?= /usr/include/\n" );
			}
		}
		catch (Exception e)
		{
			System.out.println( "Got exception " + e );
		}
	}

	/**
	 * Method generateExpincSubdirsLine.
	 * 
	 * @param b
	 */
	private void generateExpincSubdirsLine( boolean hardcodeFileNames )
	{
		try
		{
			String subdirs = "";
			IResource[] members = this.file.getParent().members();
			for (int i = 0; i < members.length; i++)
			{
				if ((members[i].getType() == IResource.FOLDER))
				{
					ODECMakefile cMakefile = (ODECMakefile)ODECommonUtilities
							.findInContainer( (IFolder)members[i],
									ODEMakefileGenerator.cMakefilesContainer );
					if (cMakefile != null)
					{
						subdirs += ODECommonUtilities
								.getDirectoryName( cMakefile.file )
								+ " ";
					}
				}
			}
			if (subdirs.length() != 0)
			{
				this.expincSubdirsBuffer
						.append( "\n#Subdirectories to traverse to generate INCLUDE Targets\nEXPINC_SUBDIRS= " );
				if (hardcodeFileNames)
					this.expincSubdirsBuffer.append( subdirs + "\n" );
				else
				{
					if (getDepth() == 2)
						this.expincSubdirsBuffer
								.append( "${ALL_SUBDIRS:Nrules_mk}\n" );
					else
						this.expincSubdirsBuffer.append( "${ALL_SUBDIRS}\n" );
				}
			}
		}
		catch (Exception e)
		{
			System.out.println( "Got exception " + e );
		}
	}
}
