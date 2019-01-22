
package com.ibm.sdwb.ode.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * @author sdmcjunk
 *
 * ODEBuildJob class is meant to implement an ODE build constructed by an
 * ODEBuildAction by means of a job that works in the background and reports
 * its progress to the progress view and is displayed in the status bar.
 */
public class ODEBuildJob extends Job {
 
 private IProject project;
 private String command;
 private String logFileName;
 private String msg;
 private int rc;
 
 private final int totalWorkItems = 100;
 private final int preBuildItems = 3;
 private final int buildItems = 94;
 
	
	public ODEBuildJob( IProject project, String command, String logFileName )
	{
		super( "ODE Build in sandbox " + project.getName() );
		this.project = project;
		this.command = command;
		this.logFileName = logFileName;
		this.rc = 0;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor)
	{	
		this.msg = null;
		this.rc = -1;
		int worked = 0;
		if (monitor == null)
		{
			monitor = new NullProgressMonitor();
		}
		try
		{
			monitor.beginTask( "ODE Build",	this.totalWorkItems );
			monitor.subTask( "Performing build setup" );
			
			// open ODE console, initialize its streams and bring to front
			performBuildInitialization();
			printCommandToConsole();
			
			String absPath = this.logFileName;
			try
			{
				absPath = prepareLogFile( absPath );
			}
			catch (FileNotFoundException e)
			{
				this.msg = e.getMessage();
			}
			ODEBuildRunner runner = new ODEBuildRunner( this.command, absPath );
			
			monitor.worked( this.preBuildItems );
			worked += this.preBuildItems;
			
		
			monitor.subTask( "Building necessary files" );
			runner.run( new SubProgressMonitor( monitor, this.buildItems ) );
			worked += this.buildItems;
			
			if (monitor.isCanceled())
				throw new InterruptedException( "User requested cancellation" );
			
			monitor.subTask( "Preparing Build Results" );
			this.rc = runner.getReturnCode();
			
			this.project.refreshLocal( IResource.DEPTH_INFINITE, null );	
		}
		catch (InterruptedException e)
		{
			displayCancellationMessage();
			return new Status( IStatus.CANCEL, ODEBasicConstants.PLUGIN_ID, 
					this.rc, e.getClass() + ": " + "The build was interrupted"
					+ "\n" + e.getMessage(), e );
		}
		catch (CoreException e)
		{ // occurred when refreshing project, ignore for now
			return new Status( IStatus.WARNING, ODEBasicConstants.PLUGIN_ID, 
					IStatus.OK, e.getMessage(), e );
		}
		catch (FileNotFoundException e)
		{
			return new Status(IStatus.ERROR, ODEBasicConstants.PLUGIN_ID, 
					IStatus.OK, "Error writing to log file\n" + e.getClass() 
					+ ":\n" + e.getMessage(), e);
		}
		catch (IOException e) 
		{
//			e.printStackTrace();
			return new Status(IStatus.ERROR, ODEBasicConstants.PLUGIN_ID, 
					IStatus.OK, e.getMessage(), e);
		}
		catch (InvocationTargetException e)
		{
//			e.printStackTrace();
			return new Status( IStatus.ERROR, ODEBasicConstants.PLUGIN_ID, 
					IStatus.OK, e.getMessage(), e );
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
			return new Status( IStatus.ERROR, ODEBasicConstants.PLUGIN_ID, 
					IStatus.OK, "Null Pointer Exception: " + e.getMessage()
					+ "\n" + "unknown problem - contact support", e );
		}
		catch (Exception e)
		{
			return new Status( IStatus.ERROR, ODEBasicConstants.PLUGIN_ID,
					IStatus.OK, "Errors occurred during the build:\n" 
					+ e.getMessage(), e );
		}
		finally
		{
			monitor.worked( this.totalWorkItems - worked );
			monitor.done();
		}
		if (this.rc != 0)
		{
			setProperty( IProgressConstants.KEEP_PROPERTY, Boolean.TRUE );
			setProperty( IProgressConstants.ACTION_PROPERTY, 
						getOpenProblemsViewAction() );
			return new Status( IStatus.OK, ODEBasicConstants.PLUGIN_ID,
					IStatus.OK, "Errors occurred during the build:\n" 
					+ "build command returned nonzero: " + this.rc, null );
		}
		else
			showSuccessfulBuildResults();
		
		return new Status( IStatus.OK, ODEBasicConstants.PLUGIN_ID, 
				IStatus.OK,	"ODE Build Completed", null );
	}
	
	
	/**
	 * If a log has already been written to the file with the log file location
	 * it is moved to a new file with a '.prev' extension and the absolute path
	 * of the file to where the log will be written is returned.  If a file 
	 * already exists with the same name, its ode-related problem markers are
	 * deleted and both the old and new logs are refreshed in the workspace.
	 * 
	 * @return String the absolute path of the log file property
	 * @throws IOException
	 */
	private String prepareLogFile( String logFileName ) throws IOException
	{		
		if ((logFileName != null) && (logFileName.length() != 0))
		{
			File logFile = new File( logFileName );
			
			// if the logfile path is relative, treat it as relative to
			// the root of the project
			if (!logFile.isAbsolute())
			{
				logFile = new File( this.project.getLocation().toOSString()
						+ File.separator + logFileName );
			}
			// creating any missing directories
			if (!logFile.getParentFile().exists())
				logFile.getParentFile().mkdirs();
			
			IFile file = ODECorePlugin.getWorkspace().getRoot()
				.getFileForLocation( new Path( logFile.getPath() ) );
			
			if (logFile.exists())
			{
				// delete the log file markers before creating the new log file
				try
				{
					file.deleteMarkers(	ODEBasicConstants.MARKER_TYPE_ID, 
							true, IResource.DEPTH_INFINITE );
				}
				catch (CoreException e)
				{
               // do nothing yet
				}
				
				// Rename the existing file
				File oldFile = new File( logFile + ".prev" );
				if (oldFile.exists())
					oldFile.delete();	
			    
				logFile.renameTo( oldFile );	
				
				try
				{
					IFile old = ODECorePlugin.getWorkspace().getRoot()
						.getFileForLocation( new Path( oldFile.getPath() ) );
					if (old != null)
						old.refreshLocal( IResource.DEPTH_ZERO, null );
				}
				catch (CoreException e)
				{ // do nothing
					
				}
			}
			
			try
			{
				RandomAccessFile newFile = new RandomAccessFile( logFile, "rw" );
				newFile.close();
				
				if (file != null)
					file.refreshLocal( IResource.DEPTH_ZERO, null );
			}
			catch (CoreException e)
			{ // do nothing
				//e.printStackTrace();
			}
			
			return logFile.getAbsolutePath();
		}
		return logFileName;
	}
	
	
	protected void displayCancellationMessage()
	{
		Display.getDefault().asyncExec( new Runnable() {
			public void run() {
				getDisplayCancellationAction().run();
			}
		});
	}
	
	protected Action getBuildSuccessDialogAction()
	{
		return new Action ( "ODE Build Sucessful" ) {
			public void run() {
				if (ignoreErrors())
				{
					ODECommonUtilities.PrintInformation( "ODE Build Complete " 
						+ "for " + project.getName(), 
						"Check problems view to see any errors that "
						+ "were ignored during the build" );
				}
				else
				{
					ODECommonUtilities.PrintInformation( "ODE Build Complete " 
							+ "for " + project.getName(), 
							"Build completed successfully" );
				}
			}
		};
	}
	
	protected Action getClearedConsoleViewAction()
	{
		return new Action( "Open and Clear ODE Console" ) {
			public void run() {
				try
				{
					ODECommonUtilities.getConsole( getActiveWorkbenchWindow(), 
							ODECommonUtilities.DEFAULT_CONSOLE_NAME );
					ODECommonUtilities.console.clearConsole();
					ODECommonUtilities.showConsoleView( 
							getActiveWorkbenchWindow(), 
							ODECommonUtilities.console );
				}
				catch (NullPointerException e)
				{
               // do nothing yet
				}
			}
		};
	}
	
	protected Action getDisplayCancellationAction()
	{
		return new Action( "Display User Cancellation" ) {
			public void run() {
				ODECommonUtilities.printErrorMessageToConsole(
						getActiveWorkbenchWindow(), 
						"The build was interrupted for " + project.getName()
						+ ":\n"	+ "User requested cancellation" );
			}
		};
	}
	
	protected Action getLogFileNotFoundMessageAction()
	{
		return new Action( "Log File Not Found" ) {
			public void run() {
				try
				{
					ODECommonUtilities.printErrorMessageToConsole(
							getActiveWorkbenchWindow(),
							"Failure to write to log file:" + "\n" 
							+ "Could not create the log file at the location " 
							+ "specified in the project's ODE properties:\n" 
							+ logFileName + "\n"
							+ "Please make sure you have specified a valid "
							+ "location and that you have the required "
							+ "permission." + "\n" 
							+ "Alternatively, any non-absolute "
							+ "path will be treated as project relative." );
					ODECommonUtilities.showConsoleView( 
							getActiveWorkbenchWindow(),
							ODECommonUtilities.console );
				}
				catch (NullPointerException e)
				{
               // do nothing yet
				}
			}
		};
	}
	
	protected Action getOpenProblemsViewAction() 
	{
		return new Action( "View Problems" ) {
			public void run() {
				try
				{
					getActiveWorkbenchWindow().getActivePage().showView( 
							ODEBasicConstants.PROBLEMS_VIEW_ID, null,
								IWorkbenchPage.VIEW_ACTIVATE );
				}
				catch (PartInitException e)
				{
               // do nothing yet
				}
			}
		};
	}
	
	protected Action getPrintCommandAction() 
	{
		return new Action( "Print command to ODE console" ) {
			public void run() {
				try
				{
					ODECommonUtilities.printMessageToConsole( 
							getActiveWorkbenchWindow(), command );
				}
				catch (NullPointerException e)
				{
               // do nothing yet
				}
			}
		};
	}
	
	/*
	 * Used to determine if the user has forced the job to run in the
	 */
	private boolean isModal( Job job )
	{	
		Boolean isModal = (Boolean)job.getProperty(
                               IProgressConstants.PROPERTY_IN_DIALOG );
        if(isModal == null) 
        	return false;
        return isModal.booleanValue();
	}
	
	protected void performBuildInitialization()
	{
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				getClearedConsoleViewAction().run();
			}
		});
	}
	
	protected void printCommandToConsole()
	{
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				getPrintCommandAction().run();
			}
		});
	}
	
	private int getReturnCode()
	{
		return (this.rc);
	}
	
	protected void showSuccessfulBuildResults()
	{	
		if (this.msg != null)
		{
			Display.getDefault().asyncExec( new Runnable() {
				public void run() {
					getLogFileNotFoundMessageAction().run();
				}
			});
		}
		if ( getReturnCode() == 0)
		{		
			Display.getDefault().asyncExec( new Runnable() {
				public void run() {
					ODECommonUtilities.printMessageToConsole( 
							getActiveWorkbenchWindow(), "ODE Build Complete" 
							+ " for " + project.getName() + ":" );
					if (ignoreErrors())
					{
						ODECommonUtilities.printMessageToConsole( 
								getActiveWorkbenchWindow(), 
								"Check problems view to see any errors that "
								+ "were ignored during the build" );
					}
					else
					{
						ODECommonUtilities.printMessageToConsole( 
								getActiveWorkbenchWindow(), 
								"Build completed successfully" );
					}
				}
			});	
			
			if (isModal(this))
			{
				setProperty( IProgressConstants.KEEP_PROPERTY, Boolean.TRUE );
				setProperty( IProgressConstants.ACTION_PROPERTY, 
						getBuildSuccessDialogAction() );
				Display.getDefault().asyncExec( new Runnable() 
				{
					public void run() 
					{
						ODECommonUtilities.showConsoleView( 
								getActiveWorkbenchWindow(),
								ODECommonUtilities.console );
					}
				} );
			}
			
		}
	}
	
	private IWorkbenchWindow getActiveWorkbenchWindow()
	{
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}
	
	private boolean ignoreErrors()
	{
		try
		{
			String val = this.project.getPersistentProperty( 
					ODEBasicConstants.KEEPBUILDING_PROP );
			return (val.equals( "true" ));
		}
		catch (CoreException e)
		{
         // do nothing yet
		}
		return false;
	}
	
} // end ODEBuildJob class
