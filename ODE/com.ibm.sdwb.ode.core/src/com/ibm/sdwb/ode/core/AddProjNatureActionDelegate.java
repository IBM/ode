/**
 * 
 */
package com.ibm.sdwb.ode.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author sdmcjunk
 * 
 * This class is meant to add the ODE nature to a project.
 * This action is available through an ODE menu that is availble when a Java,
 * C or C++ project is selected.
 *
 */
public class AddProjNatureActionDelegate implements IObjectActionDelegate {
	
	private IWorkbenchPart part;
	private ISelection selection;
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart )
	{
		this.part = targetPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) 
	{	
		if (!(this.selection instanceof IStructuredSelection))
			return;
		
		Iterator iter = ((IStructuredSelection) this.selection).iterator();
		while (iter.hasNext())
		{
			Object item = iter.next();
			
			IProject project;
			if (item instanceof IJavaProject)
				project = ((IJavaProject) item).getProject();
			else if (item instanceof ICProject)
				project = ((ICProject) item).getProject();
			else if (item instanceof IProject)
				project = (IProject) item;
			else
				continue;
			
			if (!project.isOpen())
				continue;
			
			IProjectDescription description;
			try
			{
				description = project.getDescription();
			}
			catch (CoreException e)
			{
				continue;
			}
			
			List<String> newIds = new ArrayList<String>();
			newIds.addAll( Arrays.asList( description.getNatureIds() ) );
			int index = newIds.indexOf( ODEBasicConstants.NATURE_ID );
			if (index == -1)
				newIds.add( ODEBasicConstants.NATURE_ID );
			
			// set the updated nature collection to the project description
			description.setNatureIds( 
					newIds.toArray( new String[ newIds.size() ] ) );
			
			// save the description with added/removed nature
			try
			{
				project.setDescription( description, null );
			}
			catch (CoreException e)
			{
				continue;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) 
	{
		this.selection = selection;
	}

}
