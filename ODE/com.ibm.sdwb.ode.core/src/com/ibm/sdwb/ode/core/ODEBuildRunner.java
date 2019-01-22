package com.ibm.sdwb.ode.core;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

/**
 * <p>This class is very similar to ODEProcessRunner, execpt it is used
 * specifically to run build processes, which requires a stream returned
 * from the process to be printed to a console view, written to a log file,
 * and parsed to add markers in the workspace for any errors.
 * </p>
 * <p>To accomplish this, it uses a new ODEStreamReader2 class that takes
 * a buffered stream, which is the stdout and stderr streams from the process
 * combined into one stream.
 * </p>
 * 
 * @author sdmcjunk, derived from ODEProcessRunner
 *
 */
public class ODEBuildRunner implements IRunnableWithProgress 
{
	private String command, output, logFileName;
	private int rc;
	private final int numWorkPieces = 100; // sdmcjunk - scaled by factor of 2
	private final long workPieceMillis = 3000, sleepTimeForDoneUpdate = 1500;
	private IProgressMonitor monitor;
	
	private String exceptionMsg;
	
	
	/**
	 * Constructs a new instance that is used to run a build and perform
	 * any pertienent build-related tasks that improve the user's interaction
	 * with the build action process.  Very similar to ODEProcessRunner
	 * constructor, except the log file location is passed in order to create
	 * and write the log file to which the build output is written.  Also,
	 * markers are created on this log file if a build error occurs due to a
	 * that can't be marked because it doesn't exist in the workspace.
	 * 
	 * @param command the String that contains the command with args that will
	 * 			be used to run as a process
	 * @param logFilePath the absolute path of file to be used to log output
	 */
	public ODEBuildRunner( String command, String logFilePath )
	{
		this.command = command;
		this.rc = -1;
		this.output = new String();
		this.monitor = null;
		this.logFileName = logFilePath;
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
	* This is the method that does the actual process execution.  Different 
	* from {@link ODEProcessRunner#run()}, the build process' standard output
	* and standard error streams are combined into a single stream so that
	* related output and errors can be displayed and parsed in chunks.  This
	* interspersed output/error info makes problem determination easier because
	* the output string is written to the log file in this form.  Instead of
	* the original {@link ODEStreamReader}, this <code>run()</code> method
	* utilizes {@link ODEStreamReader2}, an altered form of ODEStreamReader 
	* that improves build processing by reading the output/error stream, 
	* printing it to a console, writing it to a log file, and parsing it for 
	* errors.
	*   
	*/
	public void run() throws IOException, InterruptedException
	{
		Process p = null;
		ODEStreamReader2 streamReader = null;
		this.exceptionMsg = null;
		
		final long sleepMillis = 100;
		long sleptMillis = 0;
		int worked = 0;
		this.rc = -1;
		this.output = "";
		
		// sdmcjunk - uses ProcessBuilder to start a process that combines its
		// stdout and stderr streams into one input stream
		String[] cmdarray = getCommandArray( this.command );
		
		ProcessBuilder pb = new ProcessBuilder( cmdarray );
		pb = pb.redirectErrorStream( true );
		p = pb.start();
		OutputStream stdin = p.getOutputStream();
		InputStream stdout = p.getInputStream();
		
		ODEErrorParser parser = new ODEErrorParser();
		try
		{
			parser.openLogFile( this.logFileName );
		}
		catch (FileNotFoundException e)
		{
			this.exceptionMsg = e.getMessage();
		}
		streamReader = new ODEStreamReader2( new BufferedInputStream( stdout ),
							parser );
		
		// not needed, so close the stream
		stdin.close();
	
		streamReader.start();
		
		// sdmcjunk - added extra try and finally block to make sure
		// input streams from the process are closed
		try
		{
			while (streamReader.isAlive())
			{
				try
				{
					if (streamReader.isAlive())
					{
						streamReader.join( sleepMillis );
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
			
			stdout.close();
			
			// added
			this.output = streamReader.getOutput();
			this.rc = p.exitValue();
			parser.writeStringToLog( this.output );
			// end additions
			
			parser.closeLogFile();
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
	
	/*
	 * sdmcjunk - this method takes the single String which contains the program
	 * command and its argument and returns the tokens as an String array in 
	 * order to construct a ProcessBuilder instance.
	 */
	private String[] getCommandArray( String command )
	{
		StringTokenizer st = new StringTokenizer( command );
		String[] temp = new String[ st.countTokens() ];
		int idx = 0;
		while (st.hasMoreTokens())
		{	
			try
			{
				temp[idx++] = st.nextToken();
			}
			catch (NoSuchElementException e)
			{ 
            // do nothing yet
			}
		}
		return temp;
	}
	
//	/**
//     * This method returns a stored message relating to a caught exception
//     * that was ignored.  ODEBuildAction utilizes this method to throw a
//     * FileNotFoundException caught in the <code>run</code> method if the log 
//     * file can not be created properly in order to record the process' output.
//     *
//     * @author sdmcjunk
//     *
//     * @return String an exception message, or null if no exception was caught
//     *                      within the <code>run</code> method
//     */
//    public String getExceptionMsg()
//    {
//            return (this.exceptionMsg);
//    }

}
