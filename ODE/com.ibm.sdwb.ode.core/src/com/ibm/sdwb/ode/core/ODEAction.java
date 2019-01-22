package com.ibm.sdwb.ode.core;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


/**
 * @author ODE Team
 */
public abstract class ODEAction implements IObjectActionDelegate
{
	private IWorkbenchPart part = null;
	protected Shell shell = null;
	protected IProject project = null;
	protected IJavaProject javaProject = null;
	protected ICProject cProject = null;
	protected IPackageFragmentRoot pkgFragmentRoot = null;
	protected IPackageFragment pkgFragment = null;
	protected IFolder folder = null;
	protected ODEProperties props = null;
	protected ODEPreferences prefs = null;

	public ODEAction()
	{
		super();
	}

	/**
	 * (copy constructor)
	 * 
	 * Note this assigns local refs to the same objects as the original for all
	 * Eclipse objects (and ODE derivations of them). New objects are created
	 * for most other classes..
	 */
	public ODEAction( ODEAction copy )
	{
		this.part = copy.part;
		this.shell = copy.shell;
		this.project = copy.project;
		this.javaProject = copy.javaProject;
		this.cProject = copy.cProject;
		this.pkgFragmentRoot = copy.pkgFragmentRoot;
		this.pkgFragment = copy.pkgFragment;
		this.folder = copy.folder;
		this.props = copy.props;
		this.prefs = copy.prefs;
	}

	public void setActivePart( IAction action, IWorkbenchPart targetPart )
	{
		this.part = targetPart;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		try
		{
			if (selection instanceof IStructuredSelection)
			{
				this.project = null;
				this.javaProject = null;
				this.cProject = null;
				this.folder = null;
				this.pkgFragment = null;
				this.pkgFragmentRoot = null;
				setResource( ((IStructuredSelection)selection)
						.getFirstElement() );
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setResource( Object resource )
	{
		if (resource != null)
		{
			try
			{
				if (resource instanceof IPackageFragment)
				{
					this.pkgFragment = (IPackageFragment)resource;
					this.javaProject = this.pkgFragment.getJavaProject();
					this.project = this.javaProject.getProject();
					IResource tmpResource = this.pkgFragment
							.getUnderlyingResource();
					if (tmpResource instanceof IFolder)
						this.folder = (IFolder)tmpResource;
				}
				else if (resource instanceof IPackageFragmentRoot)
				{
					this.pkgFragmentRoot = (IPackageFragmentRoot)resource;
					this.javaProject = this.pkgFragmentRoot.getJavaProject();
					this.project = this.javaProject.getProject();
					IResource tmpResource = this.pkgFragmentRoot
							.getUnderlyingResource();
					if (tmpResource instanceof IFolder)
						this.folder = (IFolder)tmpResource;
				}
				else if (resource instanceof IJavaProject)
				{
					this.javaProject = (IJavaProject)resource;
					this.project = this.javaProject.getProject();
				}
				else if (resource instanceof ICProject)
				{
					this.cProject = (ICProject)resource;
					this.project = this.cProject.getProject();
				}
				else if (resource instanceof IFolder)
				{
					this.folder = (IFolder)resource;
					this.project = this.folder.getProject();
				}
				else if (resource instanceof IProject)
				{
					this.project = (IProject)resource;
					if (this.project.hasNature( JavaCore.NATURE_ID ))
						this.javaProject = JavaCore.create( this.project );
					if (CoreModel.hasCNature( this.project )
							|| CoreModel.hasCCNature( this.project ))
						this.cProject = CoreModel.getDefault().create(
								this.project );
				}
			}
			catch (Throwable e)
			{ // ignore all exceptions
			}
		}
	}

	public void setShell( Shell shell )
	{
		this.shell = shell;
	}
	
	public void setProps()
	{
		this.props = new ODEProperties( this.project );
		this.props.readProperties();
	}

	public void setPrefs()
	{
		this.prefs = new ODEPreferences();
		this.prefs.readPreferences();
	}

	public void run( IAction action )
	{
		if (this.part != null)
			this.shell = this.part.getSite().getWorkbenchWindow().getShell();
		setProps();
		setPrefs();
	}

	public void setActionInfo( Shell shell, Object resource )
	{
		setResource( resource );
		setShell( shell );
		setProps();
		setPrefs();
	}

	public String getFolderAsString()
	{
		int subidx;
		String folderString = "", tmpString;
		if (this.folder != null)
		{
			tmpString = this.folder.getProjectRelativePath().toString();
			subidx = tmpString.indexOf( "/" );
			if (subidx >= 0)
				folderString = tmpString.substring( subidx );
		}
		return (folderString);
	}
	
}