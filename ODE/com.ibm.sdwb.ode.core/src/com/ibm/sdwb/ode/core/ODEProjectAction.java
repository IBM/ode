package com.ibm.sdwb.ode.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


/**
 * @author ODE Team
 */
public abstract class ODEProjectAction implements IObjectActionDelegate
{
	protected IWorkbenchPart part;
	private IProject project = null;
	private IJavaProject javaProject = null;
	// Ensure, there is only one MakefileContainer per project.
	static public ODEMakefileContainer makefiles = null;

	public void setActivePart( IAction action, IWorkbenchPart targetPart )
	{
		this.part = targetPart;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		if (selection instanceof IStructuredSelection)
		{
			Object obj = ((IStructuredSelection)selection).getFirstElement();
			if (obj instanceof IJavaProject)
			{
				this.javaProject = (IJavaProject)obj;
				this.project = this.javaProject.getProject();
			}
			else if (obj instanceof IProject)
				this.project = (IProject)obj;
			makefiles = new ODEMakefileContainer();
		}
	}

	public IJavaProject getJavaProject()
	{
		return (this.javaProject);
	}

	public IProject getProject()
	{
		return (this.project);
	}

	public boolean isJavaProject()
	{
		return (getJavaProject() != null);
	}

	public boolean isProject()
	{
		return (getProject() != null);
	}
}
