package com.ibm.sdwb.ode.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

/**
 * <p>
 * The ODEErrorParser class was created to improve the functionality of the
 * com.ibm.sdwb.ode.core package so that an instance could either parse lines
 * of compiler error code returned during an ODE build, or parse a file given
 * its name, to add any necessary problem markers to workspace files that 
 * matched the error patterns.  
 * </p>
 * <p>
 * If the file doesn't exist in the workspace it
 * can be placed on the ODE log file.  Currently, the parseErrorStream(String)
 * method only marks files present in the workspace and lets the ODEBuildAction
 * class take care of marking the log file after the build has completed.
 * </p>
 * 
 * @author sdmcjunk
 */
public class ODEErrorParser 
{
	
	private RandomAccessFile errorLog;
	private IFile log;
	private Matcher lineMatcher;
	private String patternFound;
	private int lastErrorLine;
	private int charPos;
	private int linesParsed;
	
	// error attributes
	private String message;
	private String path;
	private int lineNumber;
	private int severity;
	private int charStart;
	private int charEnd;
	
	/**
	 * Default constructor sets the ODEErrorParser instance with inital values
	 * to parse a new stream or file.
	 *
	 */
	public ODEErrorParser()
	{
		this.patternFound = new String();
		this.lastErrorLine = -1;
		this.charPos = 0;
		this.linesParsed = 0;
		
		this.message = new String();
		this.path = new String();
		this.lineNumber = 1;
		this.severity = IMarker.SEVERITY_INFO;
		this.charStart = 0;
		this.charEnd = 0;
	}
	
	/**
	 * Creates a new ODEErrorParser instance with the path string given as the
	 * path to a file that whould be parsed.  The files is opened in read-write
	 * mode in case writing needs to be implemented in the future.
	 * @param path String of the absolute file location
	 * @throws FileNotFoundException if file can't be obtained and opened in 
	 * 			read-write mode
	 */
	public ODEErrorParser( String path ) throws FileNotFoundException
	{
		this.patternFound = new String();
		this.lastErrorLine = -1;
		this.charPos = 0;
		this.linesParsed = 0;
		
		this.message = new String();
		this.path = new String();
		this.lineNumber = 1;
		this.severity = IMarker.SEVERITY_INFO;
		this.charStart = 0;
		this.charEnd = 0;
	
		openLogFile( path );
	}
	
	
	/**
	 * Creates a new log file for this parser instance.  
	 * 
	 * @param path - the absolute path of the file
	 * @throws FileNotFoundException if the file can't be created
	 */
	public void openLogFile( String path ) throws FileNotFoundException
	{
		try
		{
			this.errorLog = new RandomAccessFile( path, "rw" );
			this.log = ODECorePlugin.getWorkspace().getRoot()
				.getFileForLocation( new Path( path ) );
		}
		catch (FileNotFoundException e)
		{
			throw new FileNotFoundException( "Log file with path " + path 
					+ " can't be found/created" );
		}
	}
	
	/**
	 * <p>
	 * NOT USED WITH com.ibm.sdwb.ode_5.3.0.3 plug-ins
	 * This is a method added to replace the <code>parseErrorStream</code> and
	 * <code>parseLogFile</code> methods, which when used in conjunction, mark
	 * errors on workspace files or at the point in a log file where the error
	 * was reported.
	 * </p>
	 * <p>
	 * The given String is checked to see if it matches a set of predefined
	 * errors, and if it does, a marker is created on the workspace file that
	 * caused the error or, if it doesn't exist in the workspace, on the 
	 * current line in this ODEErrorParser instance's log file.  The given line
	 * is also written to the log file.
	 * </p>
	 * It is assumed that the log file has been created and exists.
	 * 
	 * @param line The String to parse and write to the log file
	 * @throws IOException if there is a problem writing to log file
	 */
//	public void parseAndWriteLine( String line ) throws IOException
//	{
//		parseErrorStream( line );
//		writeStringToLog( line + "\n" );
//	}
	
	public void writeStringToLog( String text ) throws IOException
	{
		if (this.errorLog != null)
			this.errorLog.writeBytes( text );
	}
	
	/**
	 * Tries to close this parser instance's log file, once writing is complete.
	 * 
	 * @throws IOException if an error occurs while trying to close the log
	 */
	public void closeLogFile() throws IOException
	{
		if (this.errorLog != null)
			this.errorLog.close();
//		try
//		{
//			if (this.log != null)
//				this.log.touch( null );
//		}
//		catch (CoreException e)
//		{
//			
//		}
	}
	
	/**
	 * This method parses a given String and updates any necessary parser info.
	 * It is assumed that it is a complete line and updates the number of lines
	 * already parsed.  It uses another method to determine if the String 
	 * matches any error patterns from the nested ODEErrorPatterns class.  
	 * 
	 * @param line String to be matched against error patterns
	 */
	public void parseErrorStream( String line )
	{
		this.charStart = this.charPos;
		this.charEnd = this.charPos + line.length() + 1;
		this.linesParsed++;
		
		if (matchesError( line ))
		{
			// these error patterns ONLY relate to ODE build/make errors and
			// don't include any resource file names
			if (!this.patternFound.equals( ODEErrorPatterns.ERROR3 ) &&
					!this.patternFound.equals( ODEErrorPatterns.ERROR4 ) &&
					!this.patternFound.equals( ODEErrorPatterns.ERROR10 ) &&
					!this.patternFound.equals( ODEErrorPatterns.ERROR11 ) &&
					!this.patternFound.equals( ODEErrorPatterns.ERROR14 ) &&
					!this.patternFound.equals( ODEErrorPatterns.WARNING4 ))
			{
				IFile file = ODECorePlugin.getWorkspace().getRoot()
					.getFileForLocation( new Path( this.path ) );
				if (!markWorkspaceFile( ODEBasicConstants.MARKER_TYPE_ID, file ))
					markLog( ODEBasicConstants.MARKER_TYPE_ID );
			}
			else
				markLog( ODEBasicConstants.MARKER_TYPE_ID );
		}
	
		this.charPos += line.length() + 1;
	}
	
	/**
	 * This method parses a file line by line from the given file pointer
	 * location.  It adds problem markers for errors listed in the file 
	 * that match one of the ODEErrorPatterns, only if the error does not 
	 * pertain to a resource that already exists in the workspace.  The 
	 * file is closed after parsing is complete.
	 * 
	 * @param path String of file location
	 * @throws FileNotFoundException 
	 * @throws IOException if file can't be closed properly
	 */
	public void parseLogFile( String path, long filePointer ) 
		throws FileNotFoundException, IOException
	{
		// reset files, or should I just construct a new parser object?
		try
		{
			this.errorLog = new RandomAccessFile( path, "r" );
			this.log = ResourcesPlugin.getWorkspace().getRoot()
				.getFileForLocation( new Path( path ) );
		}
		catch (FileNotFoundException e)
		{
			throw new FileNotFoundException( "Problem finding " 
					+ "log file to parse: " + path );
		}
		this.patternFound = "";
		this.charPos = 0;
		this.linesParsed = 0;
		
		this.message = new String();
		this.path = new String();
		this.lineNumber = 1;
		this.severity = IMarker.SEVERITY_INFO;
		this.charStart = 0;
		this.charEnd = 0;
		
		// closes file after finished parsing
		parseLogFile( filePointer );
	}
	
	/**
	 * This method parses a file line by line from the beginning, and adds
	 * problem markers for errors listed in the file that match one of the 
	 * ODEErrorPatterns, only if the error does not pertain to a resource
	 * that already exists in the workspace.  The file is closed after
	 * parsing is complete.
	 * 
	 * @param path String of file location
	 * @throws FileNotFoundException 
	 * @throws IOException if file can't be closed properly
	 */
	public void parseLogFile( String path ) throws FileNotFoundException, IOException
	{
		// set the file pointer to the start of the file
		parseLogFile( path, 0 );
	}
	
	/**
	 * This method is intended to be used by the public <code>parseLogFile</code>
	 * methods to do the actual parsing of the file after the parser's 
	 * attributes have been initialized.
	 * @param filePointer The location in the file where parsing should begin
	 * @throws IOException possible reasons include:
	 * 			- <code>filePointer</code> is not a valid point in the file
	 * 			- the RandomAcessFile being parsed couldn't be closed properly
	 */
	private void parseLogFile( long filePointer ) throws IOException
	{
		try
		{
			if (this.errorLog == null || this.log == null)
				throw new IOException( "log file doesn't exist " 
						+ "or hasn't been specfied");
			if ( filePointer > this.errorLog.length() )
				throw new IOException( "Unable to parse log file from given"
						+ " file pointer: " + filePointer );
			
			String line;
			this.errorLog.seek( filePointer );
			line = this.errorLog.readLine();
			while (line != null)
			{
				this.charStart = this.charPos;
				this.charEnd = this.charPos + line.length() + 1;
				this.linesParsed++;
				
				if (matchesError( line ))
				{
					IFile file = ResourcesPlugin.getWorkspace().getRoot()
									.getFileForLocation( new Path( this.path ) );
					// if either is true, we know file doesn't exist in the
					// workspace, so mark the log file in order to create a marker
					if (file == null || !file.exists())
						markLog( ODEBasicConstants.MARKER_TYPE_ID );
				}
				
				this.charPos += line.length() + 1;
				line = this.errorLog.readLine();
			}
			
			this.errorLog.close();
		}
		catch (IOException e )
		{
			throw new IOException( "Problem while parsing log file:\n"
					+ e.getMessage() );
		}
	}
	
	/**
	 * Takes String and matches it against various pattern scenarios to see
	 * if it matches any.  Original implementation uses patterns created in 
	 * nested ODEErrorPatterns class.  Any additional error patterns needed 
	 * should be added to ODEErrorPatterns class and tested against String in
	 * this method.
	 * 
	 * @param line String to parse for matching error patterns
	 * @returns true - if <code>line</code> matches an appropriate error pattern
	 * 			false - if <code>line</code> is null, empty, 
	 * 					or doesn't match correctly
	 */
	protected boolean matchesError( String line )
	{
		if (line == null || line.length() <= 0)
			return (false);
		
		Pattern pat;
		int idx = 0;
		
		try 
		{ // use this block to catch any IllegalStateExceptions produced by trying	
		  // to access certain matcher properties when its state doesn't coincide
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR1 );
				this.lineMatcher = pat.matcher( line );
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.ERROR1;
					this.lastErrorLine = this.linesParsed;
					this.path = this.lineMatcher.group( 1 );
					this.message = "error: No such file or directory: " 
						+ this.lineMatcher.group( 
								this.lineMatcher.groupCount() );
					this.lineNumber = Integer.parseInt( 
							this.lineMatcher.group( 3 ) );
					this.severity = IMarker.SEVERITY_ERROR;
					this.charStart = this.charPos + this.lineMatcher.start( 2 );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR2 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					if (this.lineMatcher.group().contains( "candidate" )
							|| this.lineMatcher.group()
								.contains( ":                 " ))
						return (false);
					this.patternFound = ODEErrorPatterns.ERROR2;
					this.lastErrorLine = this.linesParsed;
					this.path = this.lineMatcher.group( 1 );
					idx = this.lineMatcher.start( 4 );
					this.message = "error: " + line.substring( idx );
					this.lineNumber = Integer.parseInt( 
							this.lineMatcher.group( 3 ) );
					this.severity = IMarker.SEVERITY_ERROR;
					this.charStart = this.charPos + this.lineMatcher.start( 2 );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR3 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.ERROR3;
					this.lastErrorLine = this.linesParsed;
					this.path = Path.EMPTY.toOSString();
					this.message = "Make error(" 
						+ this.lineMatcher.group( 2 ) + "): "
						+ this.lineMatcher.group( 
								this.lineMatcher.groupCount() );
					this.lineNumber = this.linesParsed;
					this.severity = IMarker.SEVERITY_ERROR;
					this.charStart = this.charPos 
						+ this.lineMatcher.start( 1 ) - 1;
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR4 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.ERROR4;
					this.lastErrorLine = this.linesParsed;
					this.path = Path.EMPTY.toOSString();
					this.message = "error: could not find a makefile";
					this.lineNumber = this.linesParsed;
					this.severity = IMarker.SEVERITY_ERROR;
					this.charStart = this.charPos + this.lineMatcher.start( 
							this.lineMatcher.groupCount() );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR5 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.ERROR5;
					this.path = this.lineMatcher.group( 1 );
					this.message = this.lineMatcher.group( 2 ) + ": error in "
				 		+ this.lineMatcher.group( 
				 				this.lineMatcher.groupCount() );
					this.lineNumber = 1;
					this.severity = IMarker.SEVERITY_ERROR;
					return (false);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR6 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.ERROR6;
					this.lastErrorLine = this.linesParsed;
					
					// add on message to previous error5 message
					// since it returned false
					this.message += ": undefined reference to "
						+ this.lineMatcher.group( 
								this.lineMatcher.groupCount() );
					
					// set these values for marking the log file in case
					// the resource that produced the error doesn't exist in
					// the workspace
					this.charStart = this.charPos + 2;
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR7 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.ERROR7;
					this.lastErrorLine = this.linesParsed;
					this.path = this.lineMatcher.group( 1 );
					this.message = this.lineMatcher.group( 2 ) 
						+ ": error: undefined reference to "
						+ this.lineMatcher.group( 
								this.lineMatcher.groupCount() );
					this.lineNumber = 1;	
					this.severity = IMarker.SEVERITY_ERROR;
					this.charStart = this.charPos + this.lineMatcher.start( 2 );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR12 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.ERROR12;
					this.lastErrorLine = this.linesParsed;
					this.path = this.lineMatcher.group( 1 );
					this.message = this.lineMatcher.group( 2 ) 
						+ ": error: undefined reference to "
						+ this.lineMatcher.group( 
								this.lineMatcher.groupCount() );
					this.lineNumber = 1;	
					this.severity = IMarker.SEVERITY_ERROR;
					this.charStart = this.charPos + this.lineMatcher.start( 2 );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR10 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.ERROR10;
					this.lastErrorLine = this.linesParsed;
					this.path = Path.EMPTY.toOSString();
					this.message = "error: " + this.lineMatcher.group();
					this.lineNumber = this.linesParsed;
					this.severity = IMarker.SEVERITY_ERROR;
					this.charStart = this.charPos + this.lineMatcher.start();
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR11 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.ERROR11;
					this.lastErrorLine = this.linesParsed;
					this.path = Path.EMPTY.toOSString();
					this.message = "error: No such file or directory: " 
						+ this.lineMatcher.group( 1 );
					this.lineNumber = this.linesParsed;
					this.severity = IMarker.SEVERITY_ERROR;
					this.charStart = this.charPos + this.lineMatcher.start( 1 );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR14 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.ERROR14;
					this.lastErrorLine = this.linesParsed;
					this.path = Path.EMPTY.toOSString();
					this.message = "ode " + this.lineMatcher.group( 1 ) 
						+ " error: " + this.lineMatcher.group( 
								this.lineMatcher.groupCount() );
					this.lineNumber = this.linesParsed;
					this.severity = IMarker.SEVERITY_ERROR;
					this.charStart = this.charPos + this.lineMatcher.start( 
							this.lineMatcher.groupCount() );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			// Check for warning1 and warning2 patterns before error8 and error9  
			// since warning1 and warning2's patterns will match error8 or error9, 
			// but not vice-versa
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.WARNING1 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.WARNING1;
					this.lastErrorLine = this.linesParsed;
					this.path = this.lineMatcher.group( 1 );
					this.lineNumber = Integer.parseInt(	
							this.lineMatcher.group( 3 ) );
					this.message = "warning: " 
						+ this.lineMatcher.group( 
								this.lineMatcher.groupCount() );
					this.severity = IMarker.SEVERITY_WARNING;
					this.charStart = this.charPos + this.lineMatcher.start( 2 );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.WARNING2 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.WARNING2;
					this.lastErrorLine = this.linesParsed;
					this.path = this.lineMatcher.group( 1 );
					this.message = "warning: " 
						+ this.lineMatcher.group( 
								this.lineMatcher.groupCount() );
					this.lineNumber = Integer.parseInt( 
							this.lineMatcher.group( 3 ) );
					this.severity = IMarker.SEVERITY_WARNING;
					this.charStart = this.charPos + this.lineMatcher.start( 2 );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR8 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					if (this.lineMatcher.group().contains( " note:" ) 
							|| this.lineMatcher.group()
								.contains( ":                 " ) )
						return (false);
					this.patternFound = ODEErrorPatterns.ERROR8;
					this.lastErrorLine = this.linesParsed;
					this.path = this.lineMatcher.group( 1 );
					idx = this.lineMatcher.start( 4 );
					this.message = "error: " 
						+ line.substring( idx );
					this.lineNumber = Integer.parseInt( 
							this.lineMatcher.group( 3 ) );
					this.severity = IMarker.SEVERITY_ERROR;
					this.charStart = this.charPos + this.lineMatcher.start( 2 );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR9 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					if (this.lineMatcher.group().contains( " note:" ) 
							|| this.lineMatcher.group()
								.contains( ":                 " ) )
						return (false);
					this.patternFound = ODEErrorPatterns.ERROR9;
					this.lastErrorLine = this.linesParsed;
					this.path = this.lineMatcher.group( 1 );
					idx = this.lineMatcher.start( 5 );
					this.message = "error: " 
						+ line.substring( idx );
					this.lineNumber = Integer.parseInt( 
							this.lineMatcher.group( 3 ) );
					this.severity = IMarker.SEVERITY_ERROR;
					this.charStart = this.charPos + this.lineMatcher.start( 2 );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
				
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.ERROR13 );
				this.lineMatcher = pat.matcher( line );
	            //this.lineMatcher.reset();
	            if (this.lineMatcher.matches())
	            {
	            	if (this.lineMatcher.group().contains( ": In " )
	            			|| this.lineMatcher.group()
								.contains( ":                 " ))
	            		return (false);
	            	this.patternFound = ODEErrorPatterns.ERROR13;
	            	this.lastErrorLine = this.linesParsed;
	                this.path = this.lineMatcher.group( 1 );
	                idx = this.lineMatcher.start( 3 );
	                this.message = "error: "
	                	+ line.substring( idx );
	                this.lineNumber = 1;
	                this.severity = IMarker.SEVERITY_ERROR;
	                this.charStart = this.charPos + this.lineMatcher.start( 2 );
	                this.charEnd = this.charPos + this.lineMatcher.end();
	                return (true);
	            }
			}
			catch (PatternSyntaxException e)
			{
				// do nothing yet
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.WARNING3 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.WARNING3;
					this.lastErrorLine = this.linesParsed;
					this.path = this.lineMatcher.group( 1 );
					this.message = "warning: " + this.lineMatcher.group( 2 )
						+ " (line number not given): "
						+ this.lineMatcher.group( this.lineMatcher.groupCount() );
					this.lineNumber = 1;
					this.severity = IMarker.SEVERITY_WARNING;
					this.charStart = this.charPos + this.lineMatcher.start( 2 );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch (PatternSyntaxException e)
			{ // Ignore faulty pattern
				
			}
			
			try
			{
				pat = Pattern.compile( ODEErrorPatterns.WARNING4 );
				this.lineMatcher = pat.matcher( line );
				//this.lineMatcher.reset();
				if (this.lineMatcher.matches())
				{
					this.patternFound = ODEErrorPatterns.WARNING4;
					this.lastErrorLine = this.linesParsed;
					this.path = Path.EMPTY.toOSString();
					this.message = "warning: " + this.lineMatcher.group( 1 )
						+ ": " + this.lineMatcher.group( 2 );
					this.lineNumber = this.linesParsed;
					this.severity = IMarker.SEVERITY_WARNING;
					this.charStart = this.charPos + this.lineMatcher.start( 2 );
					this.charEnd = this.charPos + this.lineMatcher.end();
					return (true);
				}
			}
			catch( PatternSyntaxException e)
			{
				// do nothing yet
			}
			
		}	
		catch (IllegalStateException e)
		{ // Ignore bad matcher state due to parsing
			
		}
		
		return (false);
	}
	
	/**
	 * If last String passed to this error parser instance matched an
	 * ODEErrorPattern, this method will return true, otherwise false.
	 * 
	 * @return true if the last string line passed matched an error
	 * 		   false otherwise
	 */
	public boolean errorFound()
	{
		if (this.patternFound.equals( ODEErrorPatterns.WARNING1 ) ||
				this.patternFound.equals( ODEErrorPatterns.WARNING2 ) ||
				this.patternFound.equals( ODEErrorPatterns.WARNING3 ) ||
				this.patternFound.equals( ODEErrorPatterns.WARNING4 ))
			return (false);
		return (this.lastErrorLine == this.linesParsed);
	}
	
	/**
	 * Currently this method is not used, but is left for convenience.  Its 
	 * functionality was replaced by the <code>markLog</code> and 
	 * <code>markWorkspaceFile</code> protected methods.
	 * @param markerType
	 * @param file
	 * @return true if a marker with the given type was successfully added to
	 * 				the file
	 */
	protected boolean createMarker( String markerType, IFile file )
	{
		if (file != null && file.exists())
		{
			try 
			{
				IMarker marker = file.createMarker( markerType );
				marker.setAttribute( IMarker.MESSAGE, this.message );
				marker.setAttribute( IMarker.LINE_NUMBER, this.lineNumber );
				marker.setAttribute( IMarker.SEVERITY, this.severity );
				if (file == this.log)
				{
					marker.setAttribute( IMarker.CHAR_START, this.charStart );
					marker.setAttribute( IMarker.CHAR_END, this.charEnd );
				}
				return (true);
			}
			catch (CoreException e)
			{ 
				return (false);
			}
		}
		return (false);
	}
	
	/**
	 * Marks the workspace log file stored in the <code>log</code> field if it
	 * exists with a marker using the current attributes stored in this
	 * ODEErrorParser instance.
	 * @param markerType The id of the marker type to use
	 * @return true - if the file exists in the workspace and a marker was 
	 * 					successfully added
	 * 		   false - if the file doesn't exist or a CoreException occurred
	 */
	private boolean markLog( String markerType )
	{
		if (this.log == null || !this.log.exists())
			return (false);
		try
		{
			IMarker marker = this.log.createMarker( markerType );
			marker.setAttribute( IMarker.MESSAGE, this.message );
			marker.setAttribute( IMarker.LINE_NUMBER, this.linesParsed );
			marker.setAttribute( IMarker.SEVERITY, this.severity );
			marker.setAttribute( IMarker.CHAR_START, this.charStart );
			marker.setAttribute( IMarker.CHAR_END, this.charEnd );
			return (true);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
			return (false);
		}
	}
	
	/**
	 * Marks the given file with a marker using the current attributes
	 * stored in this ODEErrorParser instance if it exists in the workspace.
	 * @param markerType The id of the marker type to use
	 * @param file The IFile on which to create the marker
	 * @return true - if the file exists in the workspace and a marker was 
	 * 					successfully added
	 * 		   false - if the file doesn't exist or a CoreException occurred
	 */
	private boolean markWorkspaceFile( String markerType, IFile file )
	{
		if (file == null || !file.exists())
			return (false);
		try
		{
			IMarker marker = file.createMarker( markerType );
			marker.setAttribute( IMarker.MESSAGE, this.message );
			marker.setAttribute( IMarker.LINE_NUMBER, this.lineNumber );
			marker.setAttribute( IMarker.SEVERITY, this.severity );
			return (true);
		}
		catch (CoreException e)
		{
			return (false);
		}
	}
	
	/**
	 * This class is meant to contain constant Strings that make up
	 * the current patterns used by the ODEErrorParser class 
	 * to determine if a given String is an error.  These 
	 * patterns were originally compiled according to the error 
	 * messages produced during an ODE build by the gcc compiling tool.  
	 * If any String has a regular expression syntax error,
	 * a PatternSyntaxException will be thrown when the String is compiled
	 * as to construct a Pattern object.
	 * 
	 * @author sdmcjunk
	 *
	 */
	protected static class ODEErrorPatterns
	{	
		private static final String OS_DIR_PREFIX = File.separator;
		
		// Regular expression patterns to catch any possible
		// errors patterns in output that match 
		static final String ERROR1 = "(" + OS_DIR_PREFIX 
				+ ".+" + File.separator
				+ "([a-zA-Z0-9_.]+)):(\\d+):\\d+: (.+): " 
				+ "No such file or directory";
		static final String ERROR2 = "(" + OS_DIR_PREFIX
				+ ".+" + File.separator 
				+ "([a-zA-Z0-9_.]+)):(\\d+): error:[ ]+([\\w\\p{Punct}]+)(.*)";
		static final String ERROR3 = ".*ERROR: mk: \"(" + OS_DIR_PREFIX 
				+ ".+" + File.separator 
				+ "([a-zA-Z0-9_.]+))\", line (\\d+): (.+)";
		static final String ERROR4 = ".*ERROR: mk: (could not find a makefile)";
		static final String ERROR5 = "(" + OS_DIR_PREFIX
				+ ".+" + File.separator + "([a-zA-Z0-9_.]+))\\((.+)\\): " 
				+ "In (.*):";
		static final String ERROR6 = ": undefined reference to (.+)";
		static final String ERROR7 = "(" + OS_DIR_PREFIX
				+ ".+" + File.separator + "([a-zA-Z0-9_.]+))\\((.+)\\): " 
				+ "undefined reference to (.+)";
		static final String ERROR8 = "(" + OS_DIR_PREFIX 
				+ ".+" + File.separator 
				+ "([a-zA-Z0-9_.]+)):(\\d+):[ ]+([\\w\\p{Punct}]+)(.*)";
		static final String ERROR9 = "(" + OS_DIR_PREFIX 
				+ ".+" + File.separator 
				+ "([a-zA-Z0-9_.]+)):(\\d+):(\\d+):[ ]+([\\w\\p{Punct}]+)(.*)";
		static final String ERROR10 = "(.+): (.* not found)";
		static final String ERROR11 = ".*`([a-zA-Z0-9_." 
				+ File.separator + "]+)': No such file or directory";
		static final String ERROR12 = "(" + OS_DIR_PREFIX 
				+ ".+" + File.separator
				+ "([a-zA-Z0-9_.]+)): undefined reference to (.+)";
		static final String ERROR13 = "(" + OS_DIR_PREFIX
		    + ".+" + File.separator
		    + "([a-zA-Z0-9_.]+)):[ ]+([\\w\\p{Punct}]+)([.[^:]]*)";
		static final String ERROR14 = ".*ERROR: (.*): (.* does not exist.*)";


		//warning patterns
		static final String WARNING1 = "(" + OS_DIR_PREFIX
				+ ".+" + File.separator 
				+ "([a-zA-Z0-9_.]+)):(\\d+): warning: (.+)";
		static final String WARNING2 = "(" + OS_DIR_PREFIX
				+ ".+" + File.separator 
				+ "([a-zA-Z0-9_.]+)):(\\d+):(\\d+): warning: (.+)";
		static final String WARNING3 = "(" + OS_DIR_PREFIX
				+ ".+" + File.separator 
				+ "([a-zA-Z0-9_.]+)): warning: (.+)"; 
		static final String WARNING4 = ".*WARNING: (.+): (.+)";
	} // end nested ODEErrorPatterns class

} // end ODEErrorParser class
