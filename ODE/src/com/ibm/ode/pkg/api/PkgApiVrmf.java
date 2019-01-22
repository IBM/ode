package com.ibm.ode.pkg.api;
import java.io.*;

/**
  * This class is the version of the Vrmf class used by the 
  * Packaging API.
  **/

public class PkgApiVrmf implements Serializable
{ 
  
  private String version_;
  private String release_;
  private int    modification_;
  private int    fix_;
 
  /**********************************************************************
   * Allocates a new Vrmf object.
   *
   **/
  public PkgApiVrmf () 
  {
    version_ = "";
    release_ = "";
    modification_ = 0;
    fix_ = 0;
  }
  
  /**********************************************************************
   * Allocates a new Vrmf object.
   *
   * @param version version number
   * @param release the release number
   * @param modification the modification number
   * @param fix the fix number
   *
   **/
  public PkgApiVrmf (String version, String release, 
               int modification, int fix) 
  {
    version_ = version;
    release_ = release;
    modification_ = modification;
    fix_ = fix;
  }
  
  /*****************************************************************************
   * Returns a String representation of this PkgApiVrmf object.
   * @return
   *    A String representation of this object.
  **/
  public String toString () 
  {
    return "VRMF is : " + version_ + " " + release_ + " " +
      modification_ + " " + fix_ + System.getProperty("line.separator");
  }

  /*****************************************************************************
   * Sets the version number.
   * @param The version number.
  **/  
  public void setVersion ( String version )
  {
    version_ = version;
  }

  /*****************************************************************************
   * Returns the version number.
   * @return The version number.
  **/
  public String getVersion ()
  {
    return (version_); 
  }
    
  /*****************************************************************************
   * Sets the release number.
   * @param The release number.
  **/ 
  public void setRelease ( String release )
  {
    release_ = release;
  }
    
  /*****************************************************************************
   * Returns the release number.
   * @return The release number.
  **/ 
  public String getRelease ()
  {
    return (release_); 
  }

  /*****************************************************************************
   * Sets the modification number.
   * @param The modification number.
  **/ 
  public void setModification ( int modification )
  {
    modification_= modification;
  }

  /*****************************************************************************
   * Returns the modification number.
   * @return The modification number.
  **/ 
  public int getModification ()
  {
    return (modification_); 
  }
   
  /*****************************************************************************
   * Sets the fix number.
   * @param The fix number.
  **/  
  public void setFix ( int fix )
  {
    fix_= fix; 
  }
    
  /*****************************************************************************
   * Returns the fix number.
   * @return The fix number.
  **/ 
  public int getFix ()
  {
    return (fix_); 
  }
  
} 
