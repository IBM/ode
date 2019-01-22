/**
 * 
 */
package com.ibm.sdwb.ode.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * @author sdmcjunk
 *
 */
public class ODENature implements IProjectNature 
{
	private static final String PROPERTY_PAGE_ID = ODEBasicConstants.PLUGIN_ID
				+ ".ODEPropertyPage";
	
	private IProject project;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException 
	{
		ODECommonUtilities.addODEBuilderToProject( getProject() );
		
		PlatformUI.getWorkbench().getDisplay().syncExec( new Runnable() 
		{
			public void run() 
			{
				PreferenceDialog odeProps = 
					PreferencesUtil.createPropertyDialogOn( getShell(), getProject(), 
						PROPERTY_PAGE_ID, null, null );
				odeProps.setBlockOnOpen( true );
				odeProps.open();
			}
		} );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException 
	{
		this.project.deleteMarkers( ODEBasicConstants.MARKER_TYPE_ID, true, 
				IResource.DEPTH_INFINITE );
		ODECommonUtilities.removeODEBuilderFromProject( getProject() );	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() 
	{
		return this.project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) 
	{
		this.project = project;
	}
	
	private Shell getShell()
	{
		return PlatformUI.getWorkbench().getDisplay().getActiveShell();
	}
}
