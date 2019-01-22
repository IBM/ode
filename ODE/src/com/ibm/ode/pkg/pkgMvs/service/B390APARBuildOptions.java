/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:44:25
 * Date and Time File was extracted/checked out: 06/04/13 16:46:57
 ******************************************************************************/
package com.ibm.ode.pkg.pkgMvs.service;

import com.ibm.ode.pkg.pkgMvs.MvsProperties;

/**
 * This class encapsulates the information required to pass to the
 * APARBUILD Build/390 command.
 *
 * @version 1.1
 * @author xxx 
 * @see B390APARInfo, B390CommandInterface, B390APARDriver
 */
public class B390APARBuildOptions 
{
  /**
   * The value of this is used to set the ship option of APARBUILD
   * command.
   */
  private String shipTo = "";

  /**
   * The value of this is used to set the rebuild option of APARBUILD
   * command.
   */
  private boolean rebuild = false; 
  
  private String path = "";
  private String logicFileName = "";
  private String commentsFileName = "";

  /**
   * Constructor
   */
  public B390APARBuildOptions() 
  {
    this.setRebuild(MvsProperties.booleanValue(MvsProperties.rebuildApar));
    this.setShipTo(MvsProperties.shipTo);
  }

  /**
   * Constructor
   */
  public B390APARBuildOptions( String shipTo, boolean rebuild, 
                               String path,
                               String logicFileName, 
                               String commentsFileName ) 
    throws B390CommandException
  {
    this.setShipTo(shipTo);
    this.setRebuild(rebuild);
    this.setPath(path);
    this.setLogicFileName(logicFileName);
    this.setCommentsFileName(commentsFileName);
  }

  public boolean getRebuild() 
  {
    return rebuild;
  }
  public void setRebuild( boolean rebuild ) 
  {
    this.rebuild = rebuild;
  }

  public void setShipTo( String shipTo ) 
  {
    if (shipTo != null)
    {
      this.shipTo = shipTo;
    }
  }
  public String getShipTo()
  {
    return shipTo;
  }

  public void setPath( String path )
  {
    if (path != null)
    {
      this.path = path;
    } 
  }
  public String getPath() 
  {
    return path;
  }

  public void setLogicFileName( String logicFileName ) 
  {
    if (logicFileName != null)
    {
      this.logicFileName = logicFileName;
    }  
  }
  public String getLogicFileName() 
  {
    return logicFileName;
  }

  public void setCommentsFileName( String commentsFileName ) 
  {
    if (commentsFileName != null)
    {
      this.commentsFileName = commentsFileName;
    }  
  }
  public String getCommentsFileName() 
  {
    return commentsFileName;
  }
}
