package com.ibm.sdwb.ode.core;

import java.io.IOException;
import java.io.InputStream;


public class ODEStreamReader extends Thread
{
	private InputStream stream;
	private String output, exceptionMsg;
	
	
	public ODEStreamReader( InputStream stream )
	{
		this.stream = stream;
		this.output = new String();
		this.exceptionMsg = null;
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
					newOutput = printCompleteLines( newOutput );
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
	 * Splits the given String into separate lines and prints each line to the
	 * ODE console. The last line after the split is returned, which will be an
	 * empty String if the given String parameter ends with '\n' or '\r'.
	 * 
	 * @param buffer String of output to split into complete lines
	 * @returns String from last line terminator to end, or empty String if
	 * 			buffer ends in a line terminator
	 * 
	 */
	private String printCompleteLines( String buffer )
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
			ODECommonUtilities.printMessageToConsole( null, message );
	}
	
}