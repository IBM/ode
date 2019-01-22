/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:45:01
 * Date and Time File was extracted/checked out: 06/04/13 16:47:03
 ****************************************************************************/
package com.ibm.ode.pkg.pkgMvs.service;

import java.io.*;
import java.util.*;
import com.ibm.sdwb.bps.api.servicepkg.*;
import com.ibm.ode.pkg.pkgCommon.PkgApiUtil;
import com.ibm.ode.pkg.pkgMvs.MvsProperties;

/**
 * This class encapsulates information required to create a PTF driver and
 * a ++PTF. This class method provide interface for creating PTF drivers and
 * creating ++PTFs.
 *
 * @version 1.1
 * @see B390CommandHandler
 */
public class B390PTFDriver extends B390CommandHandler
{
  /**
   * The following are driver options.
   */
  private String bpsServerURL;
  private Hashtable b390releases;     //FMIDs
  private String cmvcLevels[];
  private String cmvcReleases[];
  private String cmvcFamily;
  private String ptfs2supersede = "";
  private String buildId = "";
  private String shipCode;
  private String ptfs[];
  private Hashtable ptfFmidPairs;
  private ServicePackagingInterface servicePackagingInterface_;
  private boolean useBPS_ = true;

  /**
   * Constructor
   *
   * @see B390APARInfo
   * @see B390CommandInterface
   */
  public B390PTFDriver( String bpsServerURL, Vector b390releases,
                        String cmvcLevels[], String cmvcReleases[],
                        String cmvcFamily, String shipCode )
    throws B390CommandException
  {
    super();
    this.setB390releases(b390releases);
    this.cmvcLevels = cmvcLevels;
    this.cmvcReleases = cmvcReleases;
    this.cmvcFamily = cmvcFamily;
    this.bpsServerURL = bpsServerURL;
    this.shipCode = shipCode;

    if (useBPS_)
      initializeServicePackagingInterface();
  }

  /**
   * get and set b390Releases
   */
  private void setB390releases( Vector b390releases )
  {
    this.b390releases = new Hashtable();
    Enumeration releases = b390releases.elements();
    while (releases.hasMoreElements())
    {
      B390Release release = (B390Release)releases.nextElement();
      this.b390releases.put(release.getName(), release);
    }
  }

  private B390Release getB390Release( String releaseName )
  {
    return (B390Release)this.b390releases.get(releaseName);
  }

  /**
   * Returns the names of the MVS Releases as an array that are contained in the
   * Hashtable b390releases.
   */
  private String[] getB390ReleaseNames()
  {
    String [] result = new String[this.b390releases.size()];
    Enumeration releaseNames = this.b390releases.keys();
    for (int idx = 0; releaseNames.hasMoreElements(); idx++)
    {
      result[idx] = (String)releaseNames.nextElement();
    }
    return result;
  }

  /**
   * Returns the HLQs of the MVS Releases as an array that are contained in the
   * Hashtable b390releases.
   */
  private String[] getB390ReleaseHlqs()
  {
    String [] result = new String[this.b390releases.size()];
    Enumeration releaseNames = this.b390releases.elements();
    for (int idx = 0; releaseNames.hasMoreElements(); idx++)
    {
      B390Release rel = (B390Release)releaseNames.nextElement();
      result[idx] = rel.getMvsHighLevelQualifier();
    }
    return result;
  }

  /**
   * Getter for shipCode attribute
   */
  public String getShipCode()
  {
    return this.shipCode;
  }

  /**
   * get and set buildId
   */
  public String getBuildId()
  {
    return buildId;
  }

  public void setBuildId( String buildId )
  {
     this.buildId = buildId;
  }

  /**
   * Run the commands to define the PTFs to Build/390
   */
  public void definePTF( String ptfs2supersede )
    throws B390CommandException, IOException, InterruptedException
  {
    String[] apars;
    if (useBPS_)
    {
      System.out.println(
        "Retrieving APARs that are associated with the following CMVC Levels: ");
      this.printArray(cmvcLevels, 0);
      try
      {
        apars = servicePackagingInterface_.getLevelMembersAsApars(cmvcLevels,
                                                                  cmvcReleases,
                                                                  cmvcFamily);
      }
      catch (ServicePackagingException ex)
      {
        throw new B390CommandException(ex.getMessage());
      }
    }
    else
    {
      apars =
        BPS_pkgapi.getLevelMembersAsAPARs(cmvcLevels, cmvcReleases, cmvcFamily);
    }

    if (apars != null)
    {
      System.out.println("Received the following APARs from BPS:");
      this.printArray(apars, 0);

      System.out.println("Running PTFDEFINE command");
      B390CommandInterface.definePTF(this, apars, ptfs2supersede);

      readPtfs();
    }
    else
    {
      throw new B390CommandException("No APARs found for the specified levels.");
    }
  }

  /**
   * Establish arrays of PTF numbers and corresponding FMIDs, as
   * returned by the PTFDEFINE command
   */
  private void readPtfs() throws B390CommandException
  {
    String dirsep = System.getProperty("file.separator");
    String ptffilename = B390CommandInterface.getB390Path();
    if (!ptffilename.endsWith(dirsep))
    {
      ptffilename += dirsep;
    }
    ptffilename += "ptf" + dirsep + this.getBuildId() + dirsep + "ptfSet.lst";
    Vector fmids = new Vector();
    Vector ptfNumbers = new Vector();

    try
    {
      System.out.println("Reading PTF set file: " + ptffilename);
      File ptfFile = new File(ptffilename);
      BufferedReader ptfFileReader = new BufferedReader(new FileReader(ptfFile));
      String nextLine;
      for (int i = 0; (nextLine = ptfFileReader.readLine()) != null; i++)
      {
        StringTokenizer st = new StringTokenizer(nextLine, "|");
        if (st.countTokens() == 3)
        {
          String ptfNumber = st.nextToken();
          String fmid = st.nextToken();
          String comprel = st.nextToken();  // not using this for now

          // Here is where we can temporarily kludge in the buildid to the API
          // We'll add buildid as a comma separated filed in the ptf, and parse
          // it back out when getPTFsFromFMIDS is called
          String tmpPtf = ptfNumber + "," + buildId;
          ptfNumbers.addElement(tmpPtf);
          fmids.addElement(fmid);
        }
        else
        {
          throw new B390CommandException(
            "Format of PTF set file is not correct.");
        }
      }
      ptfFileReader.close();

      // store ptf/fmid pairs in BPS
      String[] ptfNumbersArray = vector2StringArray(ptfNumbers);
      String[] fmidsArray = vector2StringArray(fmids);

      if (useBPS_)
      {
        try
        {
          System.out.println("Storing PTF/FMID pairs in BPS");
          servicePackagingInterface_.addFMID_PTFPair(ptfNumbersArray, fmidsArray,
                                                     this.cmvcLevels,
                                                     this.cmvcReleases,
                                                     this.cmvcFamily);
        }
        catch (ServicePackagingException ex)
        {
          throw new B390CommandException(ex.getMessage());
        }
      }
      else
      {
        BPS_pkgapi.addFMID_PTFPair(ptfNumbersArray, fmidsArray,
                                   this.cmvcLevels, this.cmvcReleases,
                                   this.cmvcFamily);
      }
    }
    catch (FileNotFoundException ex)
    {
      throw new B390CommandException("PTF set file " + ptffilename + " not found:\n"
                                      + ex.getMessage());
    }
    catch (IOException ex)
    {
      throw new B390CommandException("Error reading PTF set file: " + ptffilename
                                      + "\n" + ex.getMessage());
    }
  }

  /**
   * Create and build PTFs via Build/390 commands
   */
  public void createPTFs()
    throws B390CommandException, IOException, InterruptedException
  {
    String[] ptfNames;
    String[] cmvcLevel = new String[1];
    String[] cmvcRelease = new String[1];
    cmvcLevel[0] = this.cmvcLevels[0];
    cmvcRelease[0] = this.cmvcReleases[0];
    String[] fmids = getB390ReleaseNames();

    System.out.println("Retrieving PTF numbers from BPS");
    if (useBPS_)
    {
      try
      {
        ptfNames = servicePackagingInterface_.getPTFsFromFMIDs(fmids, cmvcLevel,
                                                               cmvcRelease,
                                                               cmvcFamily);
      }
      catch (ServicePackagingException ex)
      {
        throw new B390CommandException(ex.getMessage());
      }
    }
    else
    {
      ptfNames =
        BPS_pkgapi.getPTFsFromFMIDs(fmids, cmvcLevel, cmvcRelease, cmvcFamily);
    }

    // Can use any PTF num, as they will all have the same buildId
    if (ptfNames.length > 0)
    {
      this.buildId = ptfNames[0].substring(ptfNames[0].indexOf(",") + 1);

      System.out.println("Running PTFBUILD command");
      B390CommandInterface.createPTF(this);

      // This deletes the control files from the /tmp directory. These control
      // files are copied to temp directory when copyControlFiles() is called
      // from prepForPtf method.
      B390CommandInterface.deleteControlFiles();

      // Retrieve the ++PTF from the driver. We only retrieve if the
      // B390_RETRIEVE_PTF property is set.
      if (MvsProperties.isRetrievePtf())
      {
        for (int idx = 0; idx < fmids.length; idx++)
        {
          String ptfNumber =
            ptfNames[idx].substring(0, ptfNames[idx].indexOf(","));
          B390Release relObj = this.getB390Release(fmids[idx]);
          RunMvsSvc.retrievePtf(relObj.getMvsHighLevelQualifier(),
                                fmids[idx], ptfNumber);
        }
      }
    }
  }

  /**
   * Calls BPS to get fmid/ptf mappings and rename and create levels
   */
  public void prepForPtf()
    throws B390CommandException, IOException, InterruptedException
  {
    // Only use one-element arrays for now - minor discrepancy with API design
    String[] cmvcLevel = new String[1];
    String[] cmvcRelease = new String[1];
    cmvcLevel[0] = this.cmvcLevels[0];
    cmvcRelease[0] = this.cmvcReleases[0];
    String[] fmids = getB390ReleaseNames();
    String[] ptfNames;
    String[] hlqs = getB390ReleaseHlqs();

    // Retrieve FMID/PTF mappings from BPS
    System.out.println("Retrieving PTF numbers from BPS");
    if (useBPS_)
    {
      try
      {
        ptfNames = servicePackagingInterface_.getPTFsFromFMIDs(fmids, cmvcLevel,
                                                               cmvcRelease,
                                                               cmvcFamily);
      }
      catch (ServicePackagingException ex)
      {
        throw new B390CommandException(ex.getMessage());
      }
    }
    else
    {
      ptfNames =
        BPS_pkgapi.getPTFsFromFMIDs(fmids, cmvcLevel, cmvcRelease, cmvcFamily);
    }

    if (ptfNames.length != fmids.length)
    {
      String errorMessage = "Unable to find the PTFs for \n";
      for (int idx = 0; idx < fmids.length; idx++)
      {
        errorMessage += fmids[idx] + "\n";
      }
      throw new B390CommandException(errorMessage);
    }

    ptfFmidPairs = new Hashtable();
    for (int idx = 0; idx < fmids.length; idx++)
    {
      // need to strip out buildid from ptfs
      if (ptfNames[idx].indexOf(",") != -1)
      {
        this.buildId = ptfNames[idx].substring(ptfNames[idx].indexOf(",") + 1);
        ptfNames[idx] = ptfNames[idx].substring(0, ptfNames[idx].indexOf(","));
      }
      System.out.println("Retrieved following PTFs from BPS for FMID "
                          + fmids[idx] + ": " + ptfNames[idx]);
      ptfFmidPairs.put(fmids[idx], ptfNames[idx]);
      B390CommandInterface.copyControlFiles(ptfNames[idx], fmids[idx], hlqs[idx]);
    }

    // Create duplicate levels in CMVC to represent the PTF numbers
    if (useBPS_)
    {
      try
      {
        servicePackagingInterface_.makeDuplicateLevels(cmvcFamily, cmvcRelease[0],
                                                       cmvcLevel[0], ptfNames);
        System.out.print("Renamed the CMVC Level '" + cmvcLevel[0] + "' to '" +
                         ptfNames[0] + "' and created duplicate Levels : ");
        this.printArray(ptfNames, 1);
      }
      catch (ServicePackagingException ex)
      {
        throw new B390CommandException(ex.getMessage());
      }
    }
    else
    {
      BPS_pkgapi.makeDuplicateLevels(cmvcFamily, cmvcRelease[0],
                                     cmvcLevel[0], ptfNames);
    }
  }

  /**
   * Overrides the <code>handleNextOutputLine</code> method of B390CommandHandler
   */
  public void handleNextOutputLine( String nextLine )
    throws B390CommandException
  {
    super.handleNextOutputLine(nextLine);
    String buildId = "Running build";
    nextLine = nextLine.trim();
    if (nextLine.startsWith(buildId))
    {
      setBuildId(nextLine.substring(buildId.length() + 1));
    }
  }

  /**
   * Get a reference to the remote interface and verify the version
   * is compatible
   */
  private void initializeServicePackagingInterface()
    throws B390CommandException
  {
    try
    {
      // get the PkgApiInterface Object from the rmi registry
      servicePackagingInterface_ =
        PkgApiUtil.getServicePackagingInterface(bpsServerURL);
      PkgApiUtil.verifyPkgApiVersion(servicePackagingInterface_);
      System.out.println("Got remote reference to the BPS server at URL : " +
                         bpsServerURL);
    }
    catch (Exception ex)
    {
      throw new B390CommandException(ex.getMessage());
    }
  }

  /**
   * Prints the contents of the specified array from the specified
   * index.
   */
  private void printArray( Object[] list, int startIdx )
  {
    for (int idx = startIdx; idx < list.length; idx++)
    {
       System.out.print(list[idx].toString());
       if (idx < (list.length - 1))
         System.out.print(", ");
    }
    System.out.println();
  }

  /**
   * converts a vector into a string array
   */
  public String[] vector2StringArray( Vector vector )
    throws B390CommandException
  {
    String result[] = new String[vector.size()];
    Enumeration enumeration = vector.elements();
    for (int idx = 0; enumeration.hasMoreElements(); idx++)
    {
       result[idx] = (String)enumeration.nextElement();
    }
    return result;
  }
}
