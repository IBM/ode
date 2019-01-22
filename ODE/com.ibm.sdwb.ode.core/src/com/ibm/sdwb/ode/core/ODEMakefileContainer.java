package com.ibm.sdwb.ode.core;

import java.util.Iterator;
import java.util.Comparator;
import java.util.TreeSet;
import org.eclipse.core.resources.IFolder;


/**
 * MakefileContainer contains the logic to handle makefiles in the Project
 * environment. Each MakefileContainer will contain makefiles on one given
 * project. First, it will contain preferences and properties information. Then
 * it will contain makefiles saved in a set.
 */
public class ODEMakefileContainer
{
	private TreeSet<Object> treeSet;
	private int depth = 0;
	final static String VERSION_INFORMATION = "Version 1.0";
	final static String SEPARATOR = ";";

	/**
	 * Contructor: Creates an empty makefile container.
	 */
	public ODEMakefileContainer()
	{
		this.treeSet = new TreeSet<Object>( new Comparator<Object>()
		{
			public int compare( Object obj1, Object obj2 )
			{
				ODEMakefile c1 = (ODEMakefile)obj1;
				ODEMakefile c2 = (ODEMakefile)obj2;
				return c1.compareTo( c2 );
			}
		} );
	}

	int Max( int a, int b )
	{
		if (a > b)
			return a;
		return b;
	}

	/**
	 * Add a new makefile to the Makefile Container A new makefile will be added
	 * to the treeSet. Uses the compareTo(Object obj) to insert the files. Only
	 * new files (not matching compareTo()) will be added.
	 */
	void add( ODEMakefile makefile )
	{
		this.depth = Max( makefile.getDepth(), this.depth );
		this.treeSet.add( makefile );
	}

	/**
	 * Find Makefile in a container.
	 * 
	 */
	ODEMakefile findInContainer( IFolder folder )
	{
		String folderName = folder.getFullPath().toString();
		for (Iterator iter = this.treeSet.iterator(); iter.hasNext();)
		{
			ODEMakefile makefile = (ODEMakefile)iter.next();
			if (folderName.equals( ODECommonUtilities
					.getRelativeFolderName( makefile.file ) ))
			{
				return makefile;
			}
		}
		return null;
	}

	void generateMakefileContentFromContainer( boolean force )
	{
		int currentDepth = this.depth;
		// If generate Makefiles for each directory
		do
		{
			for (Iterator iter = this.treeSet.iterator(); iter.hasNext();)
			{
				ODEMakefile makefile = (ODEMakefile)iter.next();
				if (makefile.getDepth() == currentDepth)
				{
					makefile.generateMakefileContent( force );
				}
			}
		}
		while (currentDepth-- > 1);
	}

	boolean hasElements()
	{
		if (this.treeSet.isEmpty())
			return false;
		return true;
	}
}