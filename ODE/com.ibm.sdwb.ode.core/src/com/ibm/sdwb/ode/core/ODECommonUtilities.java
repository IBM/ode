package com.ibm.sdwb.ode.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.framework.Bundle;


/**
 * @author ODE Team This class is a help class. Its purpose is to provide
 *         generic static methods that are called commonly.
 */
public class ODECommonUtilities implements IElementChangedListener
{
	// TODO : To auto-remake Buildconf.local when the classpath changes,
	// register this (or some other) class with this call:
	// JavaCore.addElementChangedListener(new ODECommonUtilities());
	// Then implement the following function to call createBuildconfLocal().
	// Will want to do the same thing when user changes the path to
	// the ODE executables in Preferences.
	public void elementChanged( ElementChangedEvent event )
	{ // implement this someday
	}

	// TODO : change hardcoded "src" usage to ODESRCNAME
//	private static final String BUILDCONF_LOCAL_DIR = "src";
	private static final String BUILDCONF_LOCAL_DIR = ODEBasicConstants.ODESRCNAME;
	private static final String BUILDCONF_LOCAL_FILE = "Buildconf.local";
	
	// sdmcjunk - derived from code by jrmcjunk
	public static final String DEFAULT_CONSOLE_NAME = "ODE Console";
	protected static MessageConsole console = null;
	protected static MessageConsoleStream consoleStream = null;
	protected static MessageConsoleStream consoleErrorStream = null;
	// sdmcjunk - end added fields

	static ODEMakefile findInContainer( IFolder folder,
			ODEMakefileContainer makefiles )
	{
		return makefiles.findInContainer( folder );
	}

	/**
	 * returns the name of the Folder, where the Fragment resides.
	 * 
	 * @param frag is the name of the Fragment
	 * @return IFolder the Folder name.
	 * 
	 */
	public static IFolder getFolderName( IPackageFragment frag )
	{
		try
		{
			IFolder folder = (IFolder)frag.getCorrespondingResource();
			return folder;
		}
		catch (JavaModelException ex)
		{ // ignore for now
		}
		return null;
	}

	public static IFolder getFolderName( IPackageFragmentRoot fragRoot )
	{
		try
		{
			IFolder folder = (IFolder)fragRoot.getCorrespondingResource();
			return folder;
		}
		catch (JavaModelException ex)
		{ // ignore for now
		}
		return null;
	}

	public static void ShowCompilationUnit( ICompilationUnit[] units,
			String str1 )
	{
		String str = "";
		for (int i = 0; i < units.length; i++)
			str = str + units[i].getElementName() + " \n";
		PrintDebugInformation( "Showing Compilation Units", str1 + "\n Files= "
				+ str );
		str = "";
	}

	/**
	 * Substitute extension of group of files, that ends with a specific
	 * extension.
	 * 
	 */
	public static StringBuffer substituteExtensions( ICompilationUnit[] units,
			String[] from, String[] to )
	{
		StringBuffer strBuf = new StringBuffer( "" );
		for (int i = 0; i < units.length; i++)
		{
			String str = units[i].getElementName();
			for (int j = 0; j < from.length; j++)
			{
				if (str.endsWith( from[j] ))
				{
					String str2 = str.substring( 0, str.length()
							- from[j].length() );
					strBuf.append( " " ).append( str2 ).append( to[j] ).append(
							" " );
				}
			}
		}
		return strBuf;
	}

	/**
	 * QToString: Just extends the Qualified Name (LocalName() = QualifiedName)
	 * 
	 * @param val is the qualified name
	 * @return returns val.getLocalName() + getQualifiedName()
	 */
	public static String QToString( QualifiedName val )
	{
		return val.getLocalName() + "=" + val.getQualifier();
	}

	public static void PrintDebugInformation( String method, String action )
	{
		org.eclipse.jface.dialogs.MessageDialog.openInformation( (Shell)null,
				method, action );
	}

	public static void PrintInformation( String title, String mesg )
	{
		org.eclipse.jface.dialogs.MessageDialog.openInformation( (Shell)null,
				title, mesg );
	}

	public static void PrintErrorInformation( String title, String mesg )
	{
		org.eclipse.jface.dialogs.MessageDialog.openError( (Shell)null, title,
				mesg );
	}
	
	// sdmcjunk - added extra method for convenience
	public static void PrintWarningInformation( String title, String mesg )
	{
		org.eclipse.jface.dialogs.MessageDialog.openWarning( (Shell)null, title,
				mesg );
	}

	public static String allProjectResources( IProject proj )
	{
		if (proj == null)
			return new String( " Project is null " );
		String str = "Now ...";
		return str;
	}

	/**
	 * Check to see, if a specific Package fragment contains a makefile.ode
	 * file. Assume this makefile.ode is used to build this specific directory.
	 * 
	 * @param pack is the PackgeFragment
	 * @return boolean is the nam
	 */
	public static boolean odeMakefileExistsInPackageFragment(
			IPackageFragment pack )
	{
		try
		{
			Object[] nonJavaFiles = pack.getNonJavaResources();
			for (int i = 0; i < nonJavaFiles.length; i++)
			{
				if (nonJavaFiles[i] instanceof IFile)
				{
					IFile newFile = ((IFile)nonJavaFiles[i]);
					if (isODEMakefileName( newFile.getName() ))
						return true;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println( e );
		}
		return false;
	}

	/**
	 * Check to see, if a filename is an ODE makefile name.
	 * 
	 * @param String Name of makefile
	 * @return true or false
	 */
	static boolean isODEMakefileName( String str )
	{
		if (str.equals( "makefile.ode" ) || str.equals( "makefile" )
				|| str.equals( "Makefile.ode" ) || str.equals( "Makefile" ))
			return true;
		return false;
	}

	/**
	 * Utility method. Quote the String, if needed. (get around namespaces)
	 * 
	 * @param s input String
	 * @return s , quoted String
	 */
	public static String quote( String s )
	{
		if (s.indexOf( ' ' ) == -1)
			return s;
		return ("\"" + s + "\"");
	}

	public static void showTextWindow( Shell parent, String title, String text )
	{
		
		Shell textShell = new Shell( parent, SWT.SHELL_TRIM | SWT.BORDER );
		textShell.setText( title );
		textShell.setLayout( new FillLayout() );
		Text textWindow = new Text( textShell, SWT.LEFT | SWT.READ_ONLY
				| SWT.WRAP | SWT.MULTI | SWT.V_SCROLL );
		textWindow.setEditable( false );
		textWindow
				.setFont( new Font( textShell.getDisplay(), "Courier", 10, 0 ) );
		textWindow.setText( text );
		textWindow.pack();
		textShell.open();
		
	}

	public static String findODECommand( ODEPreferences prefs, String command )
	{
		IPath path = new Path( prefs.odeToolsLoc );
		if (File.separatorChar == '/')
			path = path.append( File.separator + command );
		else
			path = path.append( File.separator + command + ".exe" );
		if (path.toFile().exists())
			return path.toOSString();
		return null;
	}

	public static boolean createBuildconfLocal( IJavaProject project )
	{
		if (project == null) // nothing to do
			return (true);
		String path = project.getProject().getLocation().toString();
		path += "/" + BUILDCONF_LOCAL_DIR + "/" + BUILDCONF_LOCAL_FILE;
		return (createBuildconfLocal( path, getClasspath( project ) ));
	}

	public static boolean createBuildconfLocal( IProject project )
	{
		if (project == null) // nothing to do
			return (true);
		String path = project.getLocation().toString();
		path += "/" + BUILDCONF_LOCAL_DIR + "/" + BUILDCONF_LOCAL_FILE;
		return (createBuildconfLocal( path, null ));
	}

	private static boolean createBuildconfLocal( String buildconfPath,
			String classpath )
	{
		PrintWriter outfile = openFileWriter( buildconfPath, false, true );
		if (outfile != null)
		{
			ODEPreferences prefs = new ODEPreferences();
			prefs.readPreferences();
			outfile.println( "replace setenv TOOLSBASE "
					+ prefs.odeToolsLoc.replace( '/', File.separatorChar )
					+ File.separator + " # path to ODE tools" );
			outfile.println( "replace setenv PATH "
					+ prefs.odeToolsLoc.replace( '/', File.separatorChar )
					+ File.pathSeparator + "${PATH}" );
			if (classpath != null) // then set CLASSPATH in Buildconf.local
									// too...
				outfile.println( "replace setenv CLASSPATH " + classpath
						+ File.pathSeparator + "${CLASSPATH}" );
			outfile.close();
			return (true);
		}
		return (false);
	}

	private static String getClasspath( IJavaProject project )
	{
		String classpath = null;
		try
		{
			classpath = project.getProject().getLocation().toString()
					+ "/"
					+ project.getOutputLocation().removeFirstSegments( 1 )
							.toString();
			IClasspathEntry[] classpaths = project.getResolvedClasspath( true );
			IClasspathEntry entry;
			for (int i = 0; i < classpaths.length; i++)
			{
				entry = classpaths[i];
				switch (entry.getEntryKind())
				{
					case IClasspathEntry.CPE_LIBRARY:
						classpath += File.pathSeparator
								+ entry.getPath().toString();
						break;
					default:
				}
			}
		}
		catch (JavaModelException e)
		{ // ignore for now
		}
		catch (NullPointerException e)
		{ // ignore for now
		}
		return classpath;
	}

	/**
	 * Create a PrintWriter object for a file.
	 * 
	 * @param path The pathname to open.
	 * @param append If true, the file will be opened in append mode. If false,
	 *            the file will be opened in overwrite mode.
	 * @param flush If true, println()'s will cause the output buffer to be
	 *            flushed automatically. If false, user must manually call
	 *            flush() [or close()] to flush the output buffer.
	 * @return The file handle, or null if an error occurred.
	 */
	public static PrintWriter openFileWriter( String path, boolean append,
			boolean flush )
	{
		try
		{
			return (new PrintWriter( new BufferedWriter( new FileWriter( path,
					append ) ), flush ));
		}
		catch (Exception e)
		{
			return (null);
		}
	}

	public static String getOutputPath( IJavaProject project )
	{
		try
		{
			IPath opLocation = project.getOutputLocation();
			String classesDir = null; // default value
			if ((opLocation != null) && (opLocation.toString().length() != 0))
			{
				String projectName = project.getProject().getName();
				String[] segments = opLocation.segments();
				for (int i = 0; i < segments.length; i++)
				{
					if (segments[i].equals( projectName ))
					{
						classesDir = opLocation.removeFirstSegments( i + 1 )
								.toString();
						// prepend the separator as it is not returned by the
						// above step
						return IPath.SEPARATOR + classesDir;
					}
				}
			}
			return classesDir;
		}
		catch (Exception e)
		{ // ignore for now
		}
		return null;
	}

	/**
	 * Gets the project to which this file belongs
	 * 
	 * @return file.project() which could be null, if there is no project...
	 */
	private static IProject getProject( IFile file )
	{
		if (file != null)
			return file.getProject();
		return null;
	}

	/**
	 * Gets the name of the file
	 * 
	 * @return file.getName() which could be null, if there is no file...
	 */
	public static String getName( IFile file )
	{
		if (file != null)
			return file.getName();
		return null;
	}

	/**
	 * Gets the FullName of this file. (Full path and name)
	 * 
	 * @return file.getFullName() which could be null, if there is no file...
	 */
	public static String getFullName( IFile file )
	{
		if (file != null)
			return file.getLocation() + file.getName();
		return null;
	}

	/**
	 * Gets the relative name of this file. (Relative to the project name)
	 * 
	 * @return the name relative to the project directory.
	 * 
	 */
	public static String getRelativeName( IFile file )
	{
		if (file != null)
		{
			IFolder folder = (IFolder)file.getParent();
			return (folder.getFullPath().toString() + "/" + file.getName());
		}
		return null;
	}

	protected static String getRelativeFolderName( IFile file )
	{
		IFolder folder = (IFolder)file.getParent();
		return folder.getFullPath().toString();
	}

	public static String getDirectoryName( IFile file )
	{
		if (file != null)
		{
			IFolder folder = (IFolder)file.getParent();
			return folder.getName();
		}
		return null;
	}

	/**
	 * This is a utility file, that returns the name of the project.
	 * 
	 * @return returns the name of the IProject associated with this file
	 */
	static String projectName( IFile file )
	{
		return getProject( file ).getName();
	}

	/**
	 * Determines if the given file is a C/C++ source file
	 * 
	 * @return returns true if the given file is a C/C++ source file and false
	 *         otherwise
	 */
	static boolean isCSourceFile( String fileName )
	{
		int index = fileName.lastIndexOf( "." );
		if (index > 0)
		{
			String fileExtension = fileName.substring( index + 1 );
			if ((fileExtension != null) && (fileExtension.length() > 0))
			{
				if ((fileExtension.equalsIgnoreCase( "c" )
						|| fileExtension.equalsIgnoreCase( "cc" )
						|| fileExtension.equalsIgnoreCase( "cpp" ) || fileExtension
						.equalsIgnoreCase( "cxx" )))
					return true;
			}
		}
		return false;
	}

	/**
	 * Determines if the given file is a C/C++ header file
	 * 
	 * @return returns true if the given file is a C/C++ header file and false
	 *         otherwise
	 */
	static boolean isCHeaderFile( String fileName )
	{
		int index = fileName.lastIndexOf( "." );
		if (index > 0)
		{
			String fileExtension = fileName.substring( index + 1 );
			if ((fileExtension != null) && (fileExtension.length() > 0))
			{
				if (fileExtension.equalsIgnoreCase( "h" ))
					return true;
			}
		}
		return false;
	}

	/**
	 * Determines if the given file is a java source file
	 * 
	 * @return returns true if the given file is a java source file and false
	 *         otherwise
	 */
	static boolean isJavaSourceFile( String fileName )
	{
		int index = fileName.lastIndexOf( "." );
		if (index > 0)
		{
			String fileExtension = fileName.substring( index + 1 );
			if ((fileExtension != null) && (fileExtension.length() > 0))
			{
				if (fileExtension.equals( "java" ))
					return true;
			}
		}
		return false;
	}

	/**
	 * Method getAllFolders.
	 * 
	 * @param IFolder
	 * @return list of all the non-empty folders
	 */
	static Vector<Object> getAllFolders( IFolder folder )
	{
		try
		{
			Vector<Object> travelledDirectories = new Vector<Object>();
			Stack<Object> dirList = new Stack<Object>();
			Vector<Object> finalDirList = new Vector<Object>();
			dirList.push( folder );
			if (dirList.empty())
				return null;
			while (true)
			{
				if (dirList.empty())
					break;
				// get the first element in the stack with out removing it
				IResource dirContent = (IResource)dirList.peek();
				if (dirContent.getType() == IResource.FOLDER)
				{
					boolean containsDirectory = false;
					IResource[] dirs = ((IFolder)dirContent).members();
					if (dirs.length == 0)
					{
						// it is an empty directory, so do not include it and
						// also remove it
						// from the stack so as to go to the next element
						dirList.pop();
						continue;
					}
					for (int i = 0; i < dirs.length; i++)
					{
						IPath fullPath = dirs[i].getFullPath();
						// mark that this directory is already checked to
						// prevent from
						// checking this path again. If this path is already
						// checked,
						// skip it and check the next value
						if (isPathMarked( travelledDirectories, fullPath ))
							continue;
						markPath( travelledDirectories, fullPath );
						if (dirs[i].getType() == IResource.FOLDER)
						{
							// it is a sub directory, so add it to the stack
							dirList.push( dirs[i] );
							containsDirectory = true;
						}
					}
					if (!containsDirectory)
					{
						// this sub directory contains only files and no
						// directories
						// this means that it is the last level in this
						// particular path
						// hence it can be added to the final list
						dirList.pop();
						finalDirList.addElement( dirContent );
					}
					continue;
				}
				// This entry is a file, so skip it
				dirList.pop();
				continue;
			}
			return finalDirList;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Checks if a path is already travelled
	 * 
	 * @param A string representing the path to be checked
	 * 
	 * @return true if travelled and false otherwise
	 */
	static private boolean isPathMarked( Vector travelledDirectories, IPath path )
	{
		if (travelledDirectories.contains( path ))
			return true;
		return false;
	}

	/**
	 * Marks that a path is already travelled
	 * 
	 * @param A string representing the path to be marked
	 */
	static private void markPath( Vector<Object> travelledDirectories, IPath path )
	{
		travelledDirectories.addElement( path );
	}

	static public boolean openYesNoDialog( String title, String message )
	{
		return (MessageDialog.openConfirm( null, title, message ));
	}

	static public boolean jarExtract( String file, String extractDir )
	{
		try
		{
			JarFile jarfile = new JarFile( file );
			Enumeration entries = jarfile.entries();
			while (entries.hasMoreElements())
			{
				ZipEntry entry = (ZipEntry)entries.nextElement();
				InputStream jarstream = jarfile.getInputStream( entry );
				File path = new File( extractDir + File.separator + entry );
				if (entry.isDirectory())
				{
					if (!path.exists() && !path.mkdirs())
						return (false); // give up
				}
				else
				{
					FileOutputStream outfile = new FileOutputStream( path );
					int c;
					while ((c = jarstream.read()) != -1)
						outfile.write( c );
					outfile.close();
				}
				jarstream.close();
			}
		}
		catch (Exception e)
		{
			return (false);
		}
		return (true);
	}

	static public String getPluginLocation()
	{
		try
		{
			Bundle odePlugin = Platform.getBundle( ODEBasicConstants.PLUGIN_ID );
			URL pluginURL = Platform.resolve( odePlugin.getEntry( "/" ) );
			String pluginStr = pluginURL.toString();
			int idx = pluginStr.indexOf( ":" );
			++idx; // move to the first slash after the colon
			while (pluginStr.charAt( idx ) == '/')
				++idx; // keep skipping slashes
			if (File.separatorChar == '/')
				--idx; // make sure Unix paths start with a slash
			pluginStr = pluginStr.substring( idx );
			return (pluginStr.replace( '/', File.separatorChar ));
		}
		catch (IOException e)
		{
			return ("");
		}
	}
	
	
	// sdmcjunk - below are some utility methods for printing info to an ODE
	// console in the console view, which were derived from code by jrmcjunk
	// ADDED plugin dependency for org.eclipse.ui.console
	/**
	   * 
	   * @param name Name of console to get
	   * @return Returns a message console
	   */
	  public static MessageConsole getConsole(IWorkbenchWindow window, 
			  String name)
	  {
	    IConsoleManager consoleManager = ConsolePlugin.getDefault()
	    									.getConsoleManager();
	    IConsole[] consoles = consoleManager.getConsoles();
	    for (int i = 0; i < consoles.length; ++i)
	    {
	      if (name.equals(consoles[i].getName()))
	      {
	        // found it
	        console = (MessageConsole) consoles[i];
	        break;
	      }
	    }
	    
	    if (console == null)
	    {
	      // console was not found, create a new one
	      console = new MessageConsole(name, null);
	      consoleManager.addConsoles(new IConsole[]{console});
	    }
	    
	    consoleStream = console.newMessageStream();
	    consoleErrorStream = console.newMessageStream();
	    // set the color of the stream to red
	    consoleErrorStream.setColor(new Color(window.getShell().getDisplay(),
	    		new RGB(255, 0, 0)));
	    
	    return console;
	  }
	  
	  
	  /**
	   * 
	   * @param window Workbench window in which to open the view
	   * @param messageConsole Message console to show
	   */
	  public static void showConsoleView(IWorkbenchWindow window, 
			  MessageConsole messageConsole)
	  {
	    try
	    {
	      // bring the console into view
	      IConsoleView view = (IConsoleView) window.getWorkbench()
	      		.getActiveWorkbenchWindow().getActivePage()
	      		.showView(IConsoleConstants.ID_CONSOLE_VIEW);
	      view.display(messageConsole);
	    }
	    catch (PartInitException e)
	    {
	      e.printStackTrace();
	    }
	  }
	  
	  /**
	   * 
	   * @param message the error message to print to the console
	   */
	  public static void printErrorMessageToConsole(IWorkbenchWindow window, 
			  String message)
	  {
	    if (consoleErrorStream == null)
	    {
	      getConsoleStreams(window);
	    }
	    // print the message to error stream for this console
	    consoleErrorStream.println(message);
	  }
	  
	  public static void printError(IWorkbenchWindow window, String title, 
			  String message)
	  {
	    printErrorMessageToConsole(window, message);
	    MessageDialog.openError(window.getShell(), title, message);
	  }

	  public static void printMessageToConsole(IWorkbenchWindow window, 
			  String message)
	  {
	    if (consoleStream == null)
	    {
	      getConsoleStreams(window);
	    }
	    consoleStream.println(message);
	  }

	  private static void getConsoleStreams(IWorkbenchWindow window)
	  {
	    if (console == null)
	    {
	      getConsole(window, DEFAULT_CONSOLE_NAME);
	    }
	  }
	// sdmcjunk - end added utility methods for printing to ODE console
	  
	// sdmcjunk - used to assign ode nature and builder to java or c/c++
	// projects
	public static void addODEBuilderToProject( IProject project )
	{
		
		if (!project.isOpen())
			return;
		
		IProjectDescription description;
		try
		{
			description = project.getDescription();
		}
		catch (CoreException e)
		{
			return;
		}
		
		// make sure the builder isn't already associated before adding it
		ICommand[] cmds = description.getBuildSpec();
		for (int i = 0; i < cmds.length; ++i)
		{	
			if (cmds[i].getBuilderName().equals( "org.eclipse.cdt.make.core.makeBuilder" ))
			{
				cmds[i].setBuilding( IncrementalProjectBuilder.FULL_BUILD, false );
				cmds[i].setBuilding( IncrementalProjectBuilder.INCREMENTAL_BUILD, false );
				cmds[i].setBuilding( IncrementalProjectBuilder.AUTO_BUILD, false );
				cmds[i].setBuilding( IncrementalProjectBuilder.CLEAN_BUILD, false );
			}
			
			if (cmds[i].getBuilderName().equals( 
					ODEBasicConstants.ODE_BUILDER_ID ))
				return;
		}
		
		// builder isn't associated with project, so make association
		ICommand newCmd = description.newCommand();
		newCmd.setBuilderName( ODEBasicConstants.ODE_BUILDER_ID );
		//newCmd.setArguments( getBuilderArguments( project ) );
		newCmd.setBuilding( IncrementalProjectBuilder.FULL_BUILD, true );
		newCmd.setBuilding( IncrementalProjectBuilder.INCREMENTAL_BUILD, true );
		newCmd.setBuilding( IncrementalProjectBuilder.AUTO_BUILD, false );
		newCmd.setBuilding( IncrementalProjectBuilder.CLEAN_BUILD, true );
		
		List<ICommand> newCmds = new ArrayList<ICommand>();
		newCmds.addAll( Arrays.asList( cmds ) );
		newCmds.add( newCmd );
		description.setBuildSpec( newCmds.toArray( 
				new ICommand[ newCmds.size() ] ) );
		try
		{
			project.setDescription( description, null );
		}
		catch (CoreException e)
		{
			// do nothing yet
		}
	}
	
	public static void removeODEBuilderFromProject( IProject project ) 
	{
		if (!project.isOpen())
			return;
	
		IProjectDescription description;
		try
		{
			description = project.getDescription();
		}
		catch (CoreException e)
		{
			return;
		}
		
		int index = -1;
		ICommand[] cmds = description.getBuildSpec();
		for (int i = 0; i < cmds.length; ++i)
		{
			if (cmds[i].getBuilderName().equals( 
					ODEBasicConstants.ODE_BUILDER_ID ))
			{
				index = i;
				break;
			}
		}
		if (index == -1)
			return;
		
		// remove the builder here if it was found
		List<ICommand> newCmds = new ArrayList<ICommand>();
		newCmds.addAll( Arrays.asList( cmds ) );
		newCmds.remove( index );
		description.setBuildSpec( newCmds.toArray( 
				new ICommand[ newCmds.size() ] ) );
		
		try
		{
			project.setDescription( description, null );
		}
		catch (CoreException e)
		{
			// do nothing yet
		}
	}
	
	public static Map getBuilderArguments( IProject project )
	{
		Hashtable args = null;
//		args = new Hashtable();
//		String val = null;
//		try
//		{
//			val = project.getPersistentProperty( 
//					ODEBasicConstants.FORCEREBUILDING_PROP );
//			args.put( ODEBasicConstants.FORCEREBUILDING_PROP, val );
//		}
//		catch (CoreException e)
//		{
//			
//		}
		return args;
	}
	
}
