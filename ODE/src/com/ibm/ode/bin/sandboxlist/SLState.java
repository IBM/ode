package com.ibm.ode.bin.sandboxlist;

import java.io.*;
import java.util.Vector;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.string.StringTools;



/**
 * This class encapsulates whether an RC file exists and what its sandbox 
 * contents are.
 * There are two major states for this object, depending upon whether 
 * the RC file exists or not. If there are 0 sandboxes in the object
 * then the RC file is presumed to not exist. Note that if the 
 * constructor is not passed a path to the RC file, it may still exist
 * and information would be obtained from it by the usual default search
 * methods that mksb would use.
 * Methods include a constructor and data access methods, and a means
 * of asking how many sandboxes are in the object.
 * Note that after the object is constructed, it is 'read only'.
**/
public class SLState
{
  private String rcFilePath; // it is the rcFile passed to the gui or ""
  private SLInfo[] list; // refresh this every time a command is
                         // run that might change it (mkbb/mksb)


  /**
   * This constructor gets the sandbox list information. 
   * The rcfile location can be specified to the constructor. 
   * If it is not, then the file will be found by mksb -list without using
   * the -rcfile flag. Note that we will then have no way of knowing for sure
   * what sandbox rc file is being used, since the user could specify it with
   * a SANDBOXRC environment variable. This implies that all ODE commands that
   * the ODE GUI uses should be passed -rc rcFile if it was specified.
  **/
  public SLState( String rcFile )
  {
    // get a name for the file
    if (rcFile == null)
      rcFilePath = "";
    else
      rcFilePath = rcFile;
    list = SandboxListParser( rcFilePath );
  }


  /**
   * This routine returns a String of the form "rcfile" which is the rcFile
   * value passed to the contructor. If the rcfile was null, "" is returned.
  **/
  public String getRcFile()
  {
    return rcFilePath;
  }


  /**
   * This routine returns a String of the form "-rc rcfile" for use in
   * constructing ODE commands to execute. If the rcfile is not known, ""
   * is returned.
  **/
  public String getRcFileFlag()
  {
    if (rcFilePath.equals( "" ))
      return "";
    else
      return "-rc " + rcFilePath;
  }


  /**
   * Returns the number of sandboxes.
  **/
  public int size()
  { 
    return list.length;
  }

  /**
   * return all the info about a specific sandbox
  **/
  public SLInfo getSLInfo( int i )
  {
    return list[i];
  }


  /**
   * returns the sandbox name of the i-th sandbox, where i is 0 to size() - 1.
  **/
  String getSandboxName( int i )
  {
    return list[i].getSandboxName();
  }

  /**
   * For the i-th sandbox, if there are environment variables specified for the base of the
   * sandbox, then this method returns the base before environment
   * variable evaluation; otherwise "" is returned.
  **/
  String getVariableBase( int i )
  {
    return list[i].getVariableBase();
  }

  /**
   * returns the i-th sandbox base after evaluation of environment variables
  **/
  String getBase( int i )
  {
    return list[i].getBase();
  }

  /**
   * returns true if the i-th sandbox is valid and is a backing build.
  **/
  boolean isBackingBuild( int i )
  {
    return list[i].isBackingBuild();
  }
                        
  /**
   * This indicates whether the i-th sandbox is the default sandbox in the 
   * .sandboxrc file.
  **/
  boolean isDefault( int i )
  {
    return list[i].isDefault();
  }
                        
  /**
   * Indicates if the i-th sandbox base directory is OK, or has have WARNING
   * messages when running currentsb -chain, or has had ERROR messages.
   * A good indication of whether there will be problems in you do a workon
   * or whatever.
  **/
  String getStatus( int i )
  {
    return list[i].getStatus();
  }

  
  /**
   * This method obtains the information from a sandbox list file (.sandboxrc).
   * This class hides how the information actually is obtained.
   * Currently the information is gotten by running mksb -list [-rc rcfile], 
   * but if we choose to access environment variables
   * (bypassing Java's natural tendencies) we could read the .sandboxrc file
   * itself some day and parse it directly to obtain more information than
   * mksb currently provides.
  **/
  private SLInfo[] SandboxListParser( String rcFilePath )
  {
    // We present the data only in the natural order we get it, currently as 
    // a Vector of SLInfo Objects.
    Vector v = new Vector();
    SLInfo[] a;
    new SLListParser( getRcFileFlag() ).parse( v );
    a = new SLInfo[v.size()];
    v.copyInto( a );
    return a;
  }


  public void printInfo()
  {
    int i;

    Interface.printAlways( "" );
    Interface.printAlways( "SandboxList has " + list.length + " sandboxes." );
    for (i = 0; i < list.length; ++i)
      this.printSandbox( i );
    Interface.printAlways( "" );
  }


  public void printSandbox( int i )
  {
    Interface.printAlways( "index=" + i + " " + list[i].toString() );
  }

}
