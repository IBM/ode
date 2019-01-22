package com.ibm.sdwb.ode.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


public class ODEProcessRunner implements IRunnableWithProgress
{
	private String command, output;
	private int rc;
	private final int numWorkPieces = 100; // sdmcjunk - scaled by factor of 2
	private final long workPieceMillis = 3000, sleepTimeForDoneUpdate = 1500;
	private IProgressMonitor monitor;
	
	/*
	 * sdmcjunk - The ODE console to which
	 * output and error information from the process will be displayed is 
	 * retrieved and shown in the currently active IWorkbenchWindow.
	 */
	public ODEProcessRunner( String command )
	{
		this.command = command;
		this.rc = -1;
		this.output = new String();
		this.monitor = null;
	}
	
	public void run( IProgressMonitor newMonitor )
			throws InvocationTargetException, InterruptedException
	{
		this.monitor = newMonitor;
		try
		{
			this.monitor.beginTask( "Running " + this.command,
					this.numWorkPieces );
			this.run();
			this.monitor.done();
		}
		catch (IOException e)
		{
			throw new InvocationTargetException( e );
		}
	}

	public void runWithProgressBar( Shell shell ) throws IOException,
			InterruptedException
	{
		try
		{
			ProgressMonitorDialog dlg = new ProgressMonitorDialog( shell );
			dlg.run( true, true, this );
			dlg.close();
		}
		catch (InvocationTargetException e)
		{
			Throwable tmp = e.getTargetException();
			if (tmp instanceof IOException)
				throw (IOException)tmp;
		}
	}

	/**
	 * This is the function that does the actual process execution.
	 * 
	 * sdmcjunk - changed errReader to errWriter, an instance of 
	 * ODEStreamWriter, which is a class very similar to ODEStreamReader, 
	 * but with a few additional fields/methods for processing error
	 * information.
	 */
	public void run() throws IOException, InterruptedException
	{
		Process p = null;
		ODEStreamReader outReader = null;
		
		// sdmcjunk - added ODEStreamWriter to handle ErrorStream
		// replaces ODEStreamReader errReader
		ODEStreamWriter errWriter = null;
		// sdmcjunk - end additions
		
		final long sleepMillis = 100;
		long sleptMillis = 0;
		int worked = 0;
		this.rc = -1;
		this.output = "";
		
		// sdmcjunk - since this is used to run processes other than a build,
		// the process output and error info is printed to the ODE console,
		// which is also opened, but it isn't cleared
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				try
				{
					IWorkbenchWindow window = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow();
					ODECommonUtilities.getConsole( window, 
							ODECommonUtilities.DEFAULT_CONSOLE_NAME );
					ODECommonUtilities.showConsoleView( window, 
							ODECommonUtilities.console );
				}
				catch (NullPointerException e)
				{
					// do nothing yet
				}
			}
		});
		
		p = Runtime.getRuntime().exec( this.command );
		OutputStream stdin = p.getOutputStream();
		InputStream stdout = p.getInputStream();
		InputStream stderr = p.getErrorStream();
		
		outReader = new ODEStreamReader( new BufferedInputStream( stdout ) ); 
		
		// sdmcjunk - changed to use new ODEStreamWriter class instead of
		// ODEStreamReader in order to display the error stream info in red
		// and parse it as the process continues, instead of waiting until
		// the process completes
		errWriter = new ODEStreamWriter( new BufferedInputStream( stderr ) ); 
		
		stdin.close();
				
		outReader.start();
		errWriter.start();
		
		// sdmcjunk - added extra try and finally block to make sure
		// input streams from the process are closed
		try
		{
			while (outReader.isAlive() || errWriter.isAlive())
			{
				try
				{
					if (outReader.isAlive())
					{
						outReader.join( sleepMillis );
						sleptMillis += sleepMillis;
					}
					if (errWriter.isAlive())
					{
						errWriter.join( sleepMillis );
						sleptMillis += sleepMillis;
					}
				}
				catch (InterruptedException e)
				{ // do nothing
				}
				if (this.monitor != null)
				{
					if (sleptMillis >= this.workPieceMillis)
					{
						if (++worked < this.numWorkPieces)
							this.monitor.worked( 1 );
						sleptMillis = 0;
					}
					if (this.monitor.isCanceled())
					{
						p.destroy();
						throw new InterruptedException(
								"User requested cancellation" );
					}
				}
			}
		}
		finally
		{
			stdout.close();
			stderr.close();
			p.destroy();
			
			if (this.monitor != null)
			{
				this.monitor.worked( this.numWorkPieces - worked );
				this.monitor.setTaskName( "Finished!" );
				try
				{
					Thread.sleep( this.sleepTimeForDoneUpdate ); // allow monitor
																	// to update
																	// completed
																	// status
				}
				catch (InterruptedException e)
				{ // do nothing
				}
			}
			
			try
			{
				this.output = outReader.getOutput() + errWriter.getOutput();
				this.rc = p.exitValue();
			}
			catch (IllegalThreadStateException e)
			{
            // do nothing yet
			}
		}
	} // end run() method

	public int getReturnCode()
	{
		return (this.rc);
	}

	public String getOutputString()
	{
		return (this.output);
	}
	
}
