/**
 * 
 */
package com.ibm.sdwb.ode.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author sdmcjunk
 * 
 * This class is used to create an implementation of the 
 * IncrementalProjectBuilder for the ODE build process.
 * The ODE build is scheduled as a job that the user can force to run in the
 * background via the {@link build(int kind, Map args, IProgressMonitor monitor)}
 * method.  This is called either when the {@link run(IAction action)} method
 * is initiated for a folder action or when the workbench initiates the builders
 * on a given project via the workbench menu or project context menu.
 *
 */
public class ODEBuilder extends IncrementalProjectBuilder 
	implements IObjectActionDelegate
{
	private static final int MANUAL_BUILD = 20;
	
	private ODEBuildAction action;
	private IResource target;
	
	/**
	 * 
	 */
	public ODEBuilder() 
	{
		this.action = new ODEBuildAction();
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException 
	{	
		int worked = 0;
		int totalWork = 100;
		if (monitor == null)
		{
			monitor = new NullProgressMonitor();
		}
		
		if (this.action == null)
			this.action = new ODEBuildAction();
		
		this.action.setResource( getProject() );
		this.target = this.action.project;
		
		this.action.setPrefs();
		this.action.setProps();
		
		try
		{
			String projName = "";
			if (this.action.project != null)
				projName = this.action.project.getName();
			
			monitor.beginTask( "Checking need to build "
					+ projName, totalWork );
			
			monitor.worked( 25 );
			worked += 25;
			
			if (fullBuild( kind ))
			{
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				
				monitor.subTask( "Invoking Full ODE Build on " 
						+ projName );
				
				IFolder dir = getTargBuildFolder( this.action.project );
				if (dir != null)
					this.target = dir;
				
				// sdmcjunk - full build calls will only force rebuilding of
				// all files if that property is set in project's ODE props
//				String rebuild = this.action.project.getPersistentProperty( 
//						ODEBasicConstants.FORCEREBUILDING_PROP );
//				
//				this.action.project.setPersistentProperty( 
//						ODEBasicConstants.FORCEREBUILDING_PROP, 
//						String.valueOf( true ) );
				
				monitor.worked( 50 );
				worked += 50;
				
				monitor.subTask( "Scheduling build on " + projName );
				prepareForBuild();
				scheduleBuild();
				
				// reset property to what it specified before
//				this.action.project.setPersistentProperty( 
//						ODEBasicConstants.FORCEREBUILDING_PROP, rebuild );
			}
			else if (manualBuild( kind ))
			{
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				
				monitor.subTask( "User initiated build in " + projName );
				
				if (this.action.folder != null)
					this.target = this.action.folder;
				else
				{
					IFolder dir = getTargBuildFolder( this.action.project );
					if (dir != null)
						this.target = dir;
				}
			
				monitor.worked( 50 );
				worked += 50;
				
				monitor.subTask( "Scheduling build on "	+ projName );
				prepareForBuild();
				scheduleBuild();
			}
			else if (incrementalBuild( kind ))
			{
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				
				monitor.subTask( "Invoking Incremental ODE Build on " 
						+ projName );
				
				if (!findLowestAffectedTree())
				{
					monitor.subTask( "No changed files found in source folder " 
							+ "in "	+ projName );
					throw new OperationCanceledException();
				}
				monitor.subTask( "Found changed files in source folder.  " 
						+ "Preparing to build " + projName );
				
				String rebuild = this.action.project.getPersistentProperty( 
						ODEBasicConstants.FORCEREBUILDING_PROP );
				this.action.project.setPersistentProperty( 
						ODEBasicConstants.FORCEREBUILDING_PROP, 
						String.valueOf( false ) );
				
				monitor.worked( 50 );
				worked += 50;
				
				monitor.subTask( "Scheduling build on "	+ projName );
				prepareForBuild();				
				scheduleBuild();
				
				// reset property to what it specified before
				this.action.project.setPersistentProperty( 
						ODEBasicConstants.FORCEREBUILDING_PROP, rebuild );
			}
			else if (cleanBuild( kind ))
			{
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				
				monitor.subTask( "ODE Builder Cleaning " 
						+ projName );
				
				monitor.worked( 50 );
				worked += 50;
				
				clean( monitor );
			}
			
		}
		catch (OperationCanceledException e)
		{
			// do nothing yet
		}
		finally
		{
			if (worked < totalWork)
				monitor.worked( totalWork - worked );
			monitor.done();
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		try
		{
			this.target = getProject();
			if (this.target != null)
				this.target.deleteMarkers( ODEBasicConstants.MARKER_TYPE_ID, 
						true, IResource.DEPTH_INFINITE );
		}
		catch (CoreException e)
		{
         // do nothing yet
		}
	}
	
	private boolean fullBuild( int kind )
	{
		return (IncrementalProjectBuilder.FULL_BUILD == kind);
	}
	
	private boolean incrementalBuild( int kind )
	{
		return (kind == IncrementalProjectBuilder.INCREMENTAL_BUILD
				|| kind == IncrementalProjectBuilder.AUTO_BUILD);
	}
		
	private boolean manualBuild( int kind )
	{
		return (kind == MANUAL_BUILD);
	}
	
	private boolean cleanBuild( int kind )
	{
		return (kind == IncrementalProjectBuilder.CLEAN_BUILD);
	}
	
	/*
	 * This method searches the resource delta tree from the build delta
	 * to find the lowest folder under the sandbox's src folder that contains
	 * files whose content has changed and are direct descendants of the folder.
	 * 
	 * returns true - if there is a file somewhere below the src folder whose
	 * 				  contents have changed since the last build
	 */
	private boolean findLowestAffectedTree()
	{
		IResourceDelta delta = getDelta( getProject() );
		
		if (delta == null)
			return false;
		
		this.target = this.action.project.getFolder( 
				ODEBasicConstants.ODESRCNAME );
		
		IResourceDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; ++i)
		{
			IResourceDelta child = children[i];
			if (child.getResource().getName()
					.equalsIgnoreCase( ODEBasicConstants.ODESRCNAME )
					&& (child.getResource() instanceof IFolder))
			{
				IResourceDelta[] srcChildren = child.getAffectedChildren( 
						IResourceDelta.CHANGED, IResourceDelta.CONTENT );
				
				final List<IFolder> changedFolders = new ArrayList<IFolder>();
				
				for (int j = 0; j < srcChildren.length; ++j)
				{
					try
					{
						srcChildren[j].accept( new IResourceDeltaVisitor() 
						{
							public boolean visit( IResourceDelta resourceDelta )
							{
								IResource res = resourceDelta.getResource();
								if (res instanceof IFolder)
								{
									if (res.getName().equals( "rules_mk" ))
										return false;
									return true;
								}
								if (res instanceof IFile)
								{
									changedFolders.add( (IFolder) res.getParent() );
									return false;
								}
								return false;
							}
						} );
					}
					catch (CoreException e)
					{
                  // do nothing yet
					}
				}
				
				IFolder[] folders = changedFolders.toArray(
						new IFolder[ changedFolders.size() ] );
				
				if (folders.length > 0)
					this.target = folders[0];
				
				// if there is more than one folder at the same level in the
				// tree that has affected files, set the parent container as
				// the root of the build tree
				int segCount = this.target.getFullPath().segmentCount();
				for (int k = 1; k < folders.length; ++k)
				{
					int segments = folders[k].getFullPath().segmentCount();
					if (segments == segCount)
					{
						IContainer parent = this.target.getParent();
						// parent will only be null if target is workspace root
						if (parent == null)
							continue;
						if (parent.exists() && (parent instanceof IFolder))
						{
							this.target = (IFolder) parent;
							segCount = this.target.getFullPath().segmentCount();
						}
					}
					if (segments < segCount)
					{
						this.target = folders[k];
						segCount = segments;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	
	private void prepareForBuild()
	{
		if (this.target != null && this.target.exists())
		{
			this.action.setResource( this.target );
			try
			{
				String val = this.action.project.getPersistentProperty( 
						ODEBasicConstants.FORCEREBUILDING_PROP );
				if (val.equals( "true" ))
				{
					this.action.project.deleteMarkers( 
							ODEBasicConstants.MARKER_TYPE_ID, true, 
							IResource.DEPTH_INFINITE );
				}
				else
				{
					this.target.deleteMarkers( 
							ODEBasicConstants.MARKER_TYPE_ID, true, 
							IResource.DEPTH_INFINITE );
				}
			}
			catch (CoreException e)
			{
            // do nothing yet
			}
		}
		
		// make sure any properties changed are read from the store
		this.action.setProps();
	}
	
	private void scheduleBuild()
	{
		String command;
		String flags = " ";
		
		try
		{	
			String odeExecutable = ODECommonUtilities.findODECommand(
					this.action.prefs, "build" );
			if (odeExecutable == null)
				throw new IOException( "build command doesn't exist" );
			flags = this.action.getBuildFlags();
			command = ODECommonUtilities.quote( odeExecutable );
			command += " " + this.action.buildTargets( flags );
			
			ODEBuildJob buildJob = new ODEBuildJob( this.action.project, 
					command, this.action.props.logFileName );
			buildJob.setUser( true );
			buildJob.setPriority( Job.BUILD );
			buildJob.setRule( getODEBuildRule() );
			buildJob.schedule();
		}
		catch (IOException e)
		{
			ODECommonUtilities
				.PrintErrorInformation( 
					"Error trying to execute ODE command", 
					e.getMessage() + ":\n"
					+ "check ODE tools location preference to make sure the "
					+ "commands exist in the specified directory and that you "
					+ "have the proper permission to execute them" );
		}
	}
		
	
	/*
	 * sdmcjunk - created to combined multiple scheduling rules to use when
	 * scheduling the build as a job that a user can choose to run in the 
	 * background.
	 */
	private ISchedulingRule getODEBuildRule()
	{
		Path path = new Path( this.action.props.logFileName );
		if (!path.isAbsolute())
		{
			path = new Path( this.action.project.getLocation().toOSString() 
					+ File.separator + this.action.props.logFileName );
		}
		IFile log = ODECorePlugin.getWorkspace().getRoot()
				.getFileForLocation( path );
		
		IResourceRuleFactory ruleFactory = ODECorePlugin.getWorkspace()
				.getRuleFactory();
		ISchedulingRule combinedRule = ruleFactory.buildRule();
		if (log != null && log.exists())
		{
			combinedRule = MultiRule.combine( ruleFactory.createRule( log ), 
					combinedRule );
			combinedRule = MultiRule.combine( ruleFactory.modifyRule( log ), 
					combinedRule );
			// marker rule combined in case it is implemented in future, currently
			// it doesn't affect the rule
			combinedRule = MultiRule.combine( ruleFactory.markerRule( log ), 
					combinedRule );
		}
		return (combinedRule);
	}
	
	/*
	 * Returns the folder specified in the project's ODE properties for the
	 * target directory at the top of the resource tree where the build will
	 * be executed.  If the path given in the props is relative, it is treated
	 * as relative to the <project_location>/src folder.
	 */
	private IFolder getTargBuildFolder( IProject proj ) throws CoreException
	{
		IFolder targ = null;
		if (proj == null)
			return null;
		
		String targDir = proj.getPersistentProperty( 
				ODEBasicConstants.TARGDIR_NAME );
		if (targDir != null)
		{
			Path dirPath = new Path( targDir );
			if (!dirPath.isAbsolute())
			{
				dirPath = new Path( proj.getFolder( 
						ODEBasicConstants.ODESRCNAME )
						.getLocation().toOSString() + File.separator 
						+ dirPath );
			}
			IContainer dir = ResourcesPlugin.getWorkspace()
				.getRoot().getContainerForLocation( dirPath );
			if (dir instanceof IFolder)
				targ = (IFolder) dir;
		}
		
		return targ;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) 
	{
		this.action.setActivePart( action, targetPart );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) 
	{
		this.action.run( action );
		run();
	}
	
	/*
	 * This method sets up an ODE build to run on this project.  It is called
	 * by the run(IAction) method, which is initiated when a user selects
	 * ODE > Build Tree from the context menu of a IFolder object in the
	 * workspace.
	 */
	private void run()
	{		
		Job build = new Job( "ODE Build Tree" )
		{
			protected IStatus run( IProgressMonitor monitor )
			{
				try
				{
					if (monitor == null)
						monitor = new NullProgressMonitor();
					
					monitor.beginTask( "Invoking ODE Builder", 100 );
					
					build( MANUAL_BUILD, 
							null, new SubProgressMonitor( monitor, 95 ) );
					
				}
				catch (CoreException e)
				{
					ODECommonUtilities.PrintErrorInformation(
							"Problem trying to build resources", e.getClass() 
							+ ": " + e.getMessage() + "\n"
							+ "Problem obtaining resources" );
					return new Status( IStatus.ERROR, 
							ODEBasicConstants.PLUGIN_ID, 
							IStatus.OK, e.getMessage(), e );
				}
				finally
				{
					monitor.done();
				}
				return new Status( IStatus.OK, ODEBasicConstants.PLUGIN_ID, 
						IStatus.OK, "User Initiated Incremental ODE Build", 
						null );
			}
		};
		build.setUser( true );
		build.schedule();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) 
	{
		this.action.selectionChanged( action, selection );
	}
	
}
