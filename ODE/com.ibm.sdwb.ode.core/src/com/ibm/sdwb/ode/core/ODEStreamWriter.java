package com.ibm.sdwb.ode.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * This class is basically a copy of the ODEStreamReader class implemented by
 * the ODE Team, but with a few extra private fields and methods to parse and 
 * print to a ODE console every complete line is received from the input
 * stream.  
 * 
 * @author sdmcjunk - derived from ODEStreamReader class created by the
 * 			ODE Team
 *
 */
public class ODEStreamWriter extends Thread {
	private InputStream stream;
	private String output, exceptionMsg;
	
	// added ODEErrorParser that I created in order to parse lines in this
	// inputstream to record errors with markers on workspace resources
	private ODEErrorParser errParser;
	
	// sdmcjunk - Added error parser so errors from the stream can be marked
	public ODEStreamWriter( InputStream stream )
	{
		this.stream = stream;
		this.output = new String();
		this.exceptionMsg = null;
		this.errParser = new ODEErrorParser();
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
			// sdmcjunk - reset instance in case run method has already been run
			this.errParser = new ODEErrorParser();
			
			while (count != -1 && !isInterrupted())
			{
				count = this.stream.read( buf, 0, bufsize );
				
				if (count > 0)
				{
					// sdmcjunk - anything currently in newOutput is not a 
					// complete line, so just concatenate with new bytes
					newOutput += new String( buf, 0, count );
					
					newOutput = printAndParseCompleteLines( newOutput );
				}
					
			} // end while
			
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
	 * Splits the given String into separate lines and prints each line to the
	 * ODE console in red.  Each line is also passed to the ODEErrorParser
	 * for error processing.  The last incomplete line from the given String
	 * parameter is returned, which is the empty String if the parameter ends
	 * in '\n' or '\r'.
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
				printErrorMessage( lines[i] );
				this.errParser.parseErrorStream( lines[i] );
			}
		}
		// will be left over String after last line terminator in buffer, or
		// a empty String if buffer ended with a line terminator
		return (lines[lines.length - 1]);
	}
	
	private void printErrorMessage( String message )
	{
		if (ODECommonUtilities.consoleErrorStream != null)
			ODECommonUtilities.printErrorMessageToConsole( null, message );
	}
	
} // end ODEStreamWriter class
