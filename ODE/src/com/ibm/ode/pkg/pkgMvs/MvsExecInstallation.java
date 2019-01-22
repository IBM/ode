package com.ibm.ode.pkg.pkgMvs;

import java.io.*;
import java.util.zip.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.StringTokenizer;
import com.ibm.ode.lib.util.ShellSystemCall;

/**
 * Installation program for MVS rexx execs.  Extracts the rexx execs needed
 * for MVS packaging from the jar file and copies them to a MVS PDS data set.
 * @version 1.2 98/09/24
 * @author  Mark DeBiase
**/

class MvsExecInstallation
{

  private static final String jarFileHead_ = "ode";
  private static final String jarFileTail_ = "tools.jar";
  private static final String tempDir_     = "/tmp";

  private boolean debug = false;

  private ShellSystemCall sysCall_;

  MvsExecInstallation()  
  { 
    sysCall_ = new ShellSystemCall();
    sysCall_.setShell("/bin/tso");
    sysCall_.setShellOption("-t");  

    if (System.getProperty("bps.mvs.install.debug") != null)
      debug = true;
  }

  /**
   * Returns the full pathname of the odetools.jar file from the
   * CLASSPATH or null if it is not found.
  **/
  private String getJarFileFromClasspath()
  {
    String classPath = System.getProperty("java.class.path");
    StringTokenizer st = new StringTokenizer(classPath, ":");
    String classPathEntry;
    String jarFile;
    while (st.hasMoreElements())
    {
      classPathEntry = st.nextToken();
      // getting the part of the string after the last directory separator
      jarFile = classPathEntry.substring( 
                                    classPathEntry.lastIndexOf( "/" ) + 1 );
      if ((jarFile.startsWith( jarFileHead_ )) && 
          (jarFile.endsWith( jarFileTail_ )))
        return classPathEntry;
    }
    return null;
  }

  /**
   * Extract all the files that end with ".rexx" from the jar file and write
   * them to the temp directory.  Returns a Vector of File objects identifying
   * the rexx files that were extracted on success or null on failure.
  **/
  private Vector extractRexxFilesFromJar(String jarFileName)
  {
    Vector rexxFiles = new Vector();

    ZipFile zf;
    try
    {
      zf = new ZipFile(jarFileName);
    }
    catch (IOException x)
    {
      System.err.println("Error opening jar file " + jarFileName + ":");
      System.err.println(x);
      return null;
    }
    System.out.println("Processing jar file " + jarFileName);
    System.out.println("Extracting files to " + tempDir_ + ":");

    Enumeration e = zf.entries();
    while (e.hasMoreElements())
    {
      ZipEntry ze = (ZipEntry) e.nextElement();

      String zipEntryName = ze.getName();
      if ( !zipEntryName.endsWith(".rexx") )
      {
        continue;
      }

      String rexxFileName;
      rexxFileName = zipEntryName.substring(zipEntryName.lastIndexOf('/') + 1);
      int fileSize = (int) ze.getSize();
      if (fileSize == -1) 
        fileSize = 5000;  // some adequate default
      StringBuffer fileContents = new StringBuffer(fileSize);
      int b;

      // read the file from the jar file
      System.out.println("  " + rexxFileName);
      try
      {
        InputStream is = zf.getInputStream(ze);
        while ( (b = is.read()) != -1)
        {
          fileContents.append((char) b);
        }
        is.close();
      }
      catch (IOException x)
      {
        System.err.println("Error extracting " + zipEntryName + ":");
        System.err.println(x);
        deleteFiles(rexxFiles);  // clean up
        return null;
      }

      File tempRexxFile = new File(tempDir_, rexxFileName);
      rexxFiles.addElement(tempRexxFile);
      // write the file to a temporary file
      try
      {
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempRexxFile));
        bw.write(fileContents.toString());
        bw.close();
      }
      catch (IOException x)
      {
        System.err.println("Error writing " + tempRexxFile.getPath() + ":");
        System.err.println(x);
        deleteFiles(rexxFiles);  // clean up
        return null;
      }
    }
    
    try
    {
      zf.close();
    }
    catch (IOException x) {}  // ignore it

    return rexxFiles;
  }

  /**
   * Deletes all the files represented by File objects in the Vector argument.
   * @param fileList a Vector of File objects.
  **/
  private void deleteFiles(Vector fileList)
  {
    for (int i=0; i<fileList.size(); i++)
    {
      File thisFile = (File) fileList.elementAt(i);
      if (thisFile.exists())
        thisFile.delete();
    }
  }

  /**
   * Check to see whether the specified MVS data set exists by doing
   * a LISTCAT on the specified data set name.
  **/
  private boolean checkForExistingMvsDataSet(String mvsDataSetName)
  {
    String command = "LISTCAT ENT('" + mvsDataSetName + "')";

    if (debug) System.out.println(command);

    int rc;
    try
    {
      rc = sysCall_.exec(command);
    }
    catch(Exception x)
    {
      System.err.println("Error trying to execute \"" + command + "\":");
      System.err.println(x);
      return false;
    }
 
    if (rc == 0)
      return true;
    else
      return false;
  }

  private boolean allocateNewMvsDataSet(String mvsDataSetName)
  {
    String command = "ALLOCATE DATASET('" + mvsDataSetName + "') NEW CATALOG " +
                     "SPACE(1,1) TRACKS DIR(5) " +
                     "RECFM(F B) LRECL(80) BLKSIZE(8800)";

    if (debug) System.out.println(command);

    int rc;
    try
    {
      rc = sysCall_.exec(command);
    }
    catch (Exception x)
    {
      System.err.println("Error trying to execute \"" + command + "\":");
      System.err.println(x);
      return false;
    }
 
    if (rc == 0)
    {
      return true;
    }
    else
    {
      System.out.print(sysCall_.getOutput());
      return false;
    }
  }

  private boolean copyRexxFile(String sourceFile, String mvsDataSet, 
                               String memberName)
  {
    String command = "OGET '" + sourceFile + "' '" + mvsDataSet + 
                     "(" + memberName + ")' TEXT";

    if (debug) System.out.println(command);

    int rc;
    try
    {
      rc = sysCall_.exec(command);
    }
    catch (Exception x)
    {
      System.err.println("Error trying to execute \"" + command + "\":");
      System.err.println(x);
      return false;
    }
 
    if (rc == 0)
    {
      return true;
    }
    else
    {
      System.out.print(sysCall_.getOutput());
      return false;
    }
  }

  /**
   * MVS Rexx exec installation program.  Extracts Rexx execs from the
   * odetools.jar file and moves them to the specified MVS PDS.
  **/
  public static void main(String[] args)
  {

    MvsExecInstallation install = new MvsExecInstallation();

    System.out.println();

    String jarFilePath = install.getJarFileFromClasspath();
    if (jarFilePath == null)
    {
      // this should actually never happen....
      System.err.println("Could not locate a file starting with " + jarFileHead_ + 
                         " and ending with " + jarFileTail_);
      System.err.println("Please ensure that the required file is in your " +
                         "CLASSPATH");
      System.exit(1);
    }

    Vector rexxFiles = install.extractRexxFilesFromJar(jarFilePath);
    if (rexxFiles == null)
    {
      System.err.println("Terminating due to errors");
      System.exit(1);
    }

    System.out.println();
    System.out.println("The extracted Rexx execs will now be copied to a MVS " +
                       "PDS data set.  This may");
    System.out.println("be an existing data set or a new data set.  Please " +
                       "enter the fully qualified");
    System.out.println("name of the MVS PDS where the Rexx execs should be " +
                       "installed:");
    String mvsDataSetName = null;
    try
    {
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      mvsDataSetName = in.readLine().toUpperCase();
      while (mvsDataSetName.length() == 0)
      {
        System.out.println();
        System.out.println("You must enter a non-empty name.");
        System.out.println("Please enter the fully qualified name of the " +
                           "MVS PDS where the Rexx execs");
        System.out.println("should be installed, or the letter Q to quit.");
        System.out.println("If you want to use Q as the name, surround it " +
                           "with single-quotes; 'Q':");
        mvsDataSetName = in.readLine().toUpperCase();
        if (mvsDataSetName.equals("Q"))
        {
          System.err.println("Terminating at user's request");
          System.exit(1);
        }
      }
    }
    catch (IOException x) {} 

    // if the data set is enclosed in single quotes, strip them off
    if (mvsDataSetName.charAt(0) == '\'')
      mvsDataSetName = mvsDataSetName.substring(1);
    if (mvsDataSetName.charAt(mvsDataSetName.length()-1) == '\'')
      mvsDataSetName = mvsDataSetName.substring(0, mvsDataSetName.length()-1);

    // allocate a new data set if needed
    System.out.println();
    System.out.println("Checking for '" + mvsDataSetName + "'");
    if ( !install.checkForExistingMvsDataSet(mvsDataSetName) )
    {
      System.out.println(mvsDataSetName + " does not exist - allocating a " +
                         "new data set");
      if ( !install.allocateNewMvsDataSet(mvsDataSetName) )
      {
        System.err.println("Failed to allocate " + mvsDataSetName + 
                           " - exiting");
        install.deleteFiles(rexxFiles);
        System.exit(1);
      }
    }
    else
    {
      System.out.println("Found " + mvsDataSetName);
    }

    //copy each extracted rexx file to the MVS data set 
    boolean errors = false;
    System.out.println();
    System.out.println("Copying rexx execs to " + mvsDataSetName + ":");
    for (int i=0; i<rexxFiles.size(); i++)
    {
      File thisFile = (File) rexxFiles.elementAt(i);
      String memberName = thisFile.getName();
      // strip off the ".rexx"
      memberName = memberName.substring(0, memberName.indexOf(".rexx"));

      System.out.println("  " + thisFile.getPath() + " --> " + mvsDataSetName +
                         "(" + memberName + ")");

      if (!install.copyRexxFile(thisFile.getPath(), mvsDataSetName, memberName))
      {
        System.err.println("Failed to copy " + thisFile.getPath() + " to " +
                           mvsDataSetName + "(" + memberName + ")");
        errors = true;
      }
    }

    if (errors)
    {
      System.out.println();
      System.out.println("Failed to copy one or more execs to " + 
                         mvsDataSetName + ".");
      System.out.println("Attempt to correct the problem based on the error " +
                         "messages and rerun");
      System.out.println("MvsExecInstallation or use the TSO OGET command to " +
                         "copy the failed files");
      System.out.println("manually (the extracted files have been left in  " +
                         "the /tmp directory).");
    }
    else
    {
      install.deleteFiles(rexxFiles);
    }
  }
}
