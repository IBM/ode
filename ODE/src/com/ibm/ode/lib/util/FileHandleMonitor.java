package com.ibm.ode.lib.util;

import java.io.IOException;

/**
 * This class represents a monitor that collects stream 
 * data from file handle
 *
 * @author Chary Lingachary
 **/
public class FileHandleMonitor extends Monitor
{
  private int fileHandle_;
  private int type_ = 0;

  /**
   * construct with monitor name
   */
  public FileHandleMonitor( String name )
  {
    super(name);
  }

  /**
   * construct with monitor name and file handle
   */
  public FileHandleMonitor( String name, int fileHandle )
  {
    super( name );
    fileHandle_ = fileHandle;	  
  }

  /**
   * read stream data from file handle
   */
  public void run()
  {
    try
    {
      //System.out.println("Monitor " + getMonitorType() + " running" );
      String str;
      while( (str = readFile( fileHandle_ )) != null )
      {
        // notify observers
        //System.out.println(getName() + " read the following message");
        //System.out.println( str );
        notifyAllObservers( str );
      }

      //System.out.println("Returning from Run method");
    }
    catch( Exception x )
    {
      String message = "An IOException occurred in ErrorProcessMonitor.run()." +
        '\n' +
        "Details of the exception follow : " +
        '\n' +
        x + " " + x.getMessage() +
        '\n';

      notifyAllObservers( message );		  
    }
  }

  /**
   * notify all observer monitors
   */
  public void notifyAllObservers( String line )
  {
    this.setChanged();
    this.notifyObservers( line );
  }

  /**
   * set file handle for reading in 
   * the stream data
   */
  public void setFileHandle( int handle )
  {
    fileHandle_ = handle;
  }

  /**
   * read as many bytes as available from
   * native code
   */
  public static native String readFile( int handle );
}
