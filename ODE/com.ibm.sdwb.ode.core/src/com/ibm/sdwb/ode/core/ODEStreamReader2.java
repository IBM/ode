package com.ibm.sdwb.ode.core;

import java.io.IOException;
import java.io.InputStream;

public class ODEStreamReader2 extends Thread 
{
	private InputStream stream;
	private String output, exceptionMsg;
	
	// sdmcjunk - added ODEErrorParser that I created in order to parse lines in
	// this inputstream to record errors with markers on workspace resources
	private ODEErrorParser errParser;
	
	ODEStreamReader2( InputStream stream, ODEErrorParser parser )
	{
		this.stream = stream;
		this.output = new String();
		this.exceptionMsg = null;
		this.errParser = parser;
	}
	
	public void run()
	{	
		int count;
		final int bufsize = 64;
		byte[] buf = new byte[bufsize + 1];
		try
		{
			this.output = "";
			count = 0;
			
			// sdmcjunk - added to store bytes read from stream in String format
			String newOutput = new String();
			
			while (count != -1 && !isInterrupted())
			{
				count = this.stream.read( buf, 0, bufsize );
				
				if (count > 0)
				{	
					// sdmcjunk - changed, used to just concatenate output 
					// string with bytes read that were stored in buf array
					// sdmcjunk - anything currently in newOutput is not a 
					// complete line, so just concatenate with new bytes
					newOutput += new String( buf, 0, count );
					
					// sdmcjunk - added private method to print and concatenate
					// output field for each complete line
					newOutput = printAndParseCompleteLines( newOutput );
				}
				
			} //end while
		}
		catch (IOException e)
		{
			this.exceptionMsg = e.getMessage();
		}
	}

	public String getOutput() throws IOException
	{
		if (this.exceptionMsg != null)
			throw new IOException( this.exceptionMsg );
		return (this.output);
	}
	
	/**
	 * Splits the given String into separate lines and each line is 
	 * passed to the ODEErrorParser for error processing.  This method prints
	 * each line to the ODE console, red for errors and black otherwise.  
	 * The last incomplete line from the given String parameter is returned, 
	 * which is the empty String if the parameter ends in '\n' or '\r'.
	 * 
	 * @param buffer String of output to split into complete lines
	 * @returns String from last incomplete line or empty String if
	 * 			buffer ends in a line terminator
	 * 
	 */
	private String printAndParseCompleteLines( String buffer )
	{
		if (buffer == null || buffer.length() == 0)
			return new String();
		String[] lines = buffer.split( "\n|\r|\r\n", -1 );
		for (int i = 0; i < lines.length - 1; ++i)
		{
			//System.out.println( "lines[" + i + "]=" + lines[i] );
			if (lines[i].length() > 0)
			{
				this.output = this.output + lines[i] + "\n";
				this.errParser.parseErrorStream( lines[i] );
				
				// write to log in ODEBuildRunner after this thread's process
				// is finished
//				try
//				{
//					this.errParser.parseAndWriteLine( lines[i] );
//				}
//				catch (IOException e)
//				{ // do nothing for now, since invalid log file is reported
//				  // after the build finishes
//				}
				
				if (this.errParser.errorFound())
					printErrorMessage( lines[i] );
				else
					printMessage( lines[i] );
			}
		}
		// will be left over String after last line terminator in buffer, or
		// a empty String if buffer ended with a line terminator
		// since buffer has some chars, lines will have at least one element
		return (lines[lines.length - 1]);
	}
	
	private void printMessage( String message )
	{
		if (ODECommonUtilities.consoleStream != null)
			ODECommonUtilities.printMessageToConsole( 
				null, message );
	}
	
	private void printErrorMessage( String message )
	{
		if (ODECommonUtilities.consoleErrorStream != null)
			ODECommonUtilities.printErrorMessageToConsole( 
				null, message );
	}
}
