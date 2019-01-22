/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:44:30
 * Date and Time File was extracted/checked out: 04/10/04 09:13:25
 ****************************************************************************/
package com.ibm.ode.pkg.pkgMvs.service;

import java.io.*;
import java.util.*;
import com.ibm.ode.pkg.pkgMvs.MvsProperties;

/**
 * This class encapsulates information required to create a APAR/USERMOD driver
 * and a ++APAR/++USERMOD. This class method provide interface for creating
 * APAR/USERMOD drivers and for creating ++APAR/++USERMODs.
 *
 * @version 1.1
 * @see B390CommandHandler
 */
public class B390APARDriver extends B390CommandHandler
{
  /**
   * The following are driver options.
   */
  private boolean built = false;
  private String  buildId = "";

  /**
   * Used to hold the ++APAR or ++USERMOD name. This is set when handleNextLine
   * method is called by the createTestPackage method.
   */
  private String  shipAparName = null;

  private String  mvsRelease = "";
  private String  highLevelQualifier = "";
  private String  retainHasAPAR = "";
  private String  runningCommand = "";
  private boolean checkLocked = false;

  /**
   * Constructor
   * @see B390APARInfo, B390CommandInterface
   */
  public B390APARDriver()
  {
    super();
  }
  public B390APARDriver( boolean checkLocked )
  {
    super();
    this.checkLocked = checkLocked;
  }
  public String getBuildId()
  {
    return buildId;
  }
  public void setBuildId( String buildId )
  {
    this.buildId = buildId;
  }
  public String getMvsRelease()
  {
    return mvsRelease;
  }
  public void setMvsRelease( String mvsRelease )
  {
    this.mvsRelease = mvsRelease;
  }
  public String getHighLevelQualifier()
  {
    return highLevelQualifier;
  }
  public void setHighLevelQualifier( String highLevelQualifier )
  {
    this.highLevelQualifier = highLevelQualifier;
  }
  public String getRetainHasAPAR()
  {
    return retainHasAPAR;
  }
  public void setRetainHasAPAR( String retainHasAPAR )
  {
    this.retainHasAPAR = retainHasAPAR;
  }

  /**
   * Builds the apar delta driver for the specified APAR.
   *
   * @param aparName
   * @param mvsRelease
   * @param highLevelQualifier
   */
  public void build( String aparName,
                     String mvsRelease,
                     String highLevelQualifier )
    throws B390CommandException, IOException, InterruptedException
  {
    if (built) return;
    this.runningCommand = "driverBuild";
    B390CommandInterface.driverBuild(
             aparName, mvsRelease, highLevelQualifier,
             this, new B390DriverBuildOptions());
    this.runningCommand = "";
    built = true;
  }

  /**
   * Creates ++APAR or ++USERMOD for the specified APAR or USERMOD.
   *
   * @param aparName
   * @param mvsRelease
   * @param highLevelQualifier
   * @param retainHasAPAR
   */
  public void createTestPackage( String aparName,
                                 String mvsRelease,
                                 String highLevelQualifier,
                                 boolean retainHasAPAR )
    throws B390CommandException, IOException, InterruptedException
  {
    // if (!built) build(aparName, mvsRelease,highLevelQualifier);
    this.runningCommand = "aparCheck";
    B390CommandInterface.aparCheck(aparName,
                                   mvsRelease,
                                   highLevelQualifier,
                                   this,
                                   retainHasAPAR);
    this.runningCommand = "createTestPackage";
    B390CommandInterface.createTestPackage(this, new B390APARBuildOptions());

    // Retrieve the ++APAR/++USERMOD from the driver. We only retrieve if the
    // B390_RETRIEVE_APAR property is set.
    if (MvsProperties.isRetrieveApar())
    {
      this.runningCommand = "retrievingApar";
      RunMvsSvc.retrieveApar(highLevelQualifier, mvsRelease,
                             aparName, this.shipAparName, !retainHasAPAR);
    }
    this.runningCommand = "";
  }

  /**
   * Overrides the <code>handleNextOutputLine</code> method of B390CommandHandler
   */
  public void handleNextOutputLine( String nextLine )
    throws B390CommandException
  {
    super.handleNextOutputLine(nextLine);
    nextLine = nextLine.trim();
    if (!checkLocked)  // used by APARCHECK
    {
      String buildId = "Running build";
      if (nextLine.startsWith(buildId))
        setBuildId(nextLine.substring(buildId.length() + 1));
    }
    else // used by DELETEDRIVER
    {
      String buildId = "locked by Build";
      int idx = nextLine.indexOf(buildId);
      if (idx != -1)
        setBuildId(nextLine.substring(idx + buildId.length() + 1));
    }

    // Extract the name of the ++APAR or ++USERMOD built. This will be
    // used during the extracting phase.
    if (this.runningCommand.equals("createTestPackage"))
    {
      if (nextLine.startsWith("++APAR") ||
          nextLine.startsWith("++USERMOD"))
      {
        StringTokenizer st = new StringTokenizer(nextLine);
        st.nextToken();
        this.shipAparName = st.nextToken();
      }
    }
  }

  /**
   * Overrides the <code>handleReturnCode</code> method of B390CommandHandler
   */
  public void handleReturnCode( int returnCode )
    throws B390CommandException
  {
    super.handleReturnCode(returnCode);
    String buildId = getBuildId();
    if (buildId == null || buildId.length() == 0)
      throw new B390CommandException(
        "Build Id not returned by Build/390 build process.");
  }

  /**
   *
   */
  public static void usage()
  {
    String usage = "B390APARDriver(String aparName, String aparName, String highLevelQualifier)";
    usage += "releaseName is the name of the release on MVS which must be "+
      "less than 8 characters long.";
    usage += "highLevelQualifier is the MVS high level qualifier containing"+
      "the release which must be less than 8 characters long.";
    System.out.println(usage);
  }
}
