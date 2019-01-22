package com.ibm.ode.bin.sandboxfiles;

import java.io.*;
import java.util.Vector;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.string.StringTools;



/**
 * This class encapsulates the files and directories to be displayed.
 * Methods include a constructor and data access methods, and a means
 * of asking how many files are in the object.
 * Note that after the object is constructed, it is 'read only'.
**/
public class SFState
{
  private Object[][] data;   // refresh this every time a command is
                             // run that might change it
  private int dataSize;


  /**
   * This constructor gets the sandbox list information. 
  **/
  public SFState( SandboxFiles main_class )
  {
    // We present the data only in the natural order we get it, as 
    // an array Object[][] which is set in the data variable.
    // The number of rows with data is also saved in dataSize.
    SFSblsParser par = new SFSblsParser( main_class );
    dataSize = par.getDataSize();
    data = par.getDataArray();
  }


  /**
   * Returns the number of files.
  **/
  public int size()
  { 
    return dataSize;
  }


  /**
   * Return all the info about a specific file.
   * Used for selections.
  **/
  public SFInfo getSFInfo( int i )
  {
    return new SFInfo( (String)data[i][0],  // base
                       (String)data[i][1],  // path
                       (String)data[i][2],  // date
                       (Long)data[i][3],    // size
                       (String)data[i][4],  // name
                       (String)data[i][5],  // suffix
                       (String)data[i][6],  // type
                       (Integer)data[i][7], // position
                       (String)data[i][8]   // fullPath
                       );
  }

  /**
   * return the array with all the data, for use by the table model, etc.
  **/
  public Object[][] getDataArray()
  {
    return data;
  }

}
