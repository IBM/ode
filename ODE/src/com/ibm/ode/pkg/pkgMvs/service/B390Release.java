/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:45:06
 * Date and Time File was extracted/checked out: 04/10/04 09:13:28
 ****************************************************************************/
package com.ibm.ode.pkg.pkgMvs.service;

import java.io.*;
import java.util.Hashtable;
import com.ibm.ode.pkg.pkgMvs.MvsProperties;

/**
 * This class encapsulates B390 Release.
 *
 * @version 1.1
 * @see B390APARDriver
 */
public class B390Release
{
  private String base;            //The base release. The release this release is based on.
  private String name;            //The release name.
  private String component;       //The release component name.
  private String systemRelease;   //The system release name.
  private String retainRelease;   //The retain release name.
  private String mvsHighLevelQualifier;//The mvs high level qualifier containing the release data.
  private String changeTeam;      //The retain change team.
  private String featureFmids;    //The feature fmids for the base.

  private String primaryDataSetSize;
  private String secondaryDataSetSize;
  private String primaryUnibankSize;
  private String maxCylinderSize;
  private String maxExtendSize;
  private String collectors;
  private String steps;
  private String volid;
  private String stgcls;
  private String mgtcls;

  private String drvPrimaryDataSetSize;
  private String drvSecondaryDataSetSize;
  private String drvPrimaryUnibankSize;
  private String drvMaxCylinderSize;
  private String drvMaxExtendSize;
  private String drvVolid;
  private String drvStgcls;
  private String drvMgtcls;
  private int noShippedFiles = 0;  //number of shipped files in this release

  private String cmvcFamily;
  private String cmvcFamilyAddr;

  private Hashtable drivers = new Hashtable();

  /**
   * Constructor
   */
  public B390Release( String mvsRelease, String mvsHighLevelQualifier,
                      String component, String systemRelease, String base,
                      String retainRelease, String changeTeam, String featureFmids )
    throws B390CommandException
  {
    this.setBase(base);
    this.setName(mvsRelease);
    this.setMvsHighLevelQualifier(mvsHighLevelQualifier);
    this.setComponent(component);
    this.setSystemRelease(systemRelease);
    this.setRetainRelease(retainRelease);
    this.setChangeTeam(changeTeam);
    this.setFeatureFMIDs(featureFmids);
    initVars();
  }

  /**
   * This constructor is added to add the number of shipped files in the release
   * as an attribute to the B390Release object.
   * This attribute will be used while creating the copysentdriver.
   */
  public B390Release (String mvsRelease, String mvsHighLevelQualifier,
                      String component, String systemRelease, String base,
                      String retainRelease, String changeTeam, String featureFmids,
                      int noShippedFiles)
    throws B390CommandException
  {
    this(mvsRelease, mvsHighLevelQualifier, component,
         systemRelease, base, retainRelease, changeTeam, featureFmids );
    this.setNoShippedFiles(noShippedFiles);
  }

  /**
   * Constructor
   */
  public B390Release( String mvsRelease, String mvsHighLevelQualifier )
    throws B390CommandException
  {
    this.setName(mvsRelease);
    this.setMvsHighLevelQualifier(mvsHighLevelQualifier);
    initVars();
  }

  private void initVars()
  {
    // Initialize other parameters from MvsProperties
    primaryDataSetSize      = MvsProperties.relBlkp;
    secondaryDataSetSize    = MvsProperties.relBlks;
    primaryUnibankSize      = MvsProperties.relUbkp;
    maxCylinderSize         = MvsProperties.relMaxcyl;
    maxExtendSize           = MvsProperties.relMaxext;
    collectors              = MvsProperties.relCollectors;
    steps                   = MvsProperties.relSteps;
    volid                   = MvsProperties.relVolid;
    stgcls                  = MvsProperties.relStgcls;
    mgtcls                  = MvsProperties.relMgtcls;
    drvPrimaryDataSetSize   = MvsProperties.drvBlkp;
    drvSecondaryDataSetSize = MvsProperties.drvBlks;
    drvPrimaryUnibankSize   = MvsProperties.drvUbkp;
    drvMaxCylinderSize      = MvsProperties.drvMaxcyl;
    drvMaxExtendSize        = MvsProperties.drvMaxext;
    drvVolid                = MvsProperties.drvVolid;
    drvStgcls               = MvsProperties.drvStgcls;
    drvMgtcls               = MvsProperties.drvMgtcls;
    cmvcFamily              = MvsProperties.cmvcFamily;
    cmvcFamilyAddr          = MvsProperties.cmvcFamilyAddr;
  }

  private void setName( String name )
    throws B390CommandException
  {
    if (name == null)
      throw new B390CommandException(
          "Release name is required. It can not be null" );
    name = name.trim();
    if (name.length() == 0)
      throw new B390CommandException( "Release name is required. " );
    this.name = name;
  }
  public String getName()
  {
    return name;
  }

  public String getBase()
  {
    return base;
  }
  private void setBase(String base)
  {
    this.base=base.trim();
  }

  public String getChangeTeam() throws B390CommandException
  {
    if (changeTeam == null)
      throw new B390CommandException( "Retain change team not set" );
    return changeTeam;
  }
  private void setChangeTeam( String changeTeam ) throws B390CommandException
  {
    if (changeTeam == null)
      throw new B390CommandException(
          "Retain change team is required. It can not be null" );
    changeTeam = changeTeam.trim();
    if (changeTeam.length() == 0)
      throw new B390CommandException( "Retain changeTeam is required. ");
    this.changeTeam = changeTeam;
  }

  public int getNoShippedFiles()
  {
    return noShippedFiles;
  }

  private void setNoShippedFiles( int noShippedFiles )
  {
    this.noShippedFiles = noShippedFiles;
  }

  private void setComponent(String component)
    throws B390CommandException
  {
    if (component == null)
      throw new B390CommandException(
          "Release component is required. It can not be null" );
    component = component.trim();
    if (component.length() == 0)
      throw new B390CommandException( "Release component is required. ");
    this.component = component;
  }

  public String getComponent() throws B390CommandException
  {
    if (component == null)
      throw new B390CommandException( "Retain release component not set" );
    if (component.length() != 9)
      throw new B390CommandException(
                          "Retain release component must be 9 characters" );
    return component;
  }

  public String getHighLevelComponent() throws B390CommandException
  {
    return getComponent().substring(0, 4);
  }

  public String getComponentId() throws B390CommandException
  {
    return getComponent().substring(4);
  }

  private void setSystemRelease( String systemRelease )
    throws B390CommandException
  {
    if (systemRelease == null)
      throw new B390CommandException(
          "System release is required. It can not be null.");
    systemRelease = systemRelease.trim();
    if (systemRelease.length() == 0)
      throw new B390CommandException("System release component is required. ");
    this.systemRelease = systemRelease;
  }

  public String getSystemRelease() throws B390CommandException
  {
    if (systemRelease == null)
      throw new B390CommandException("System release component not set.");
    return systemRelease;
  }

  private void setRetainRelease( String retainRelease )
    throws B390CommandException
  {
    if (retainRelease == null)
      throw new B390CommandException(
        "Retain Release is required. It can not be null.");
    retainRelease = retainRelease.trim();
    if (retainRelease.length() == 0)
      throw new B390CommandException("Retain Release component is required. ");
    this.retainRelease = retainRelease;
  }

  public String getRetainRelease() throws B390CommandException
  {
    if (retainRelease == null)
      throw new B390CommandException("Retain Release component not set.");
    return retainRelease;
  }

  private void setMvsHighLevelQualifier( String mvsHighLevelQualifier )
    throws B390CommandException
  {
    if (mvsHighLevelQualifier == null)
      throw new B390CommandException(
          "MVS High level qualifier is required. It can not be null");
    mvsHighLevelQualifier = mvsHighLevelQualifier.trim();
    if (mvsHighLevelQualifier.length() == 0)
      throw new B390CommandException("MVS High level qualifier is required. ");
    this.mvsHighLevelQualifier = mvsHighLevelQualifier;
  }
  public String getMvsHighLevelQualifier()
  {
    return mvsHighLevelQualifier;
  }
  public String getSystemName()
  {
    return name.substring(1, 4);
  }
  public String getFeatureFMIDs()
  {
    return featureFmids;
  }
  public void setFeatureFMIDs( String featureFmids )
  {
    this.featureFmids = featureFmids;
  }
  public void setPrimaryDataSetSize( String primaryDataSetSize )
  {
    this.primaryDataSetSize = primaryDataSetSize;
  }
  public String getPrimaryDataSetSize()
  {
    return primaryDataSetSize;
  }
  public void setSecondaryDataSetSize( String secondaryDataSetSize )
  {
    this.secondaryDataSetSize = secondaryDataSetSize;
  }
  public String getSecondaryDataSetSize()
  {
    return secondaryDataSetSize;
  }
  public void setPrimaryUnibankSize( String primaryUnibankSize )
  {
    this.primaryUnibankSize = primaryUnibankSize;
  }
  public String getPrimaryUnibankSize()
  {
    return primaryUnibankSize;
  }
  public void setMaxCylinderSize( String maxCylinderSize )
  {
    this.maxCylinderSize = maxCylinderSize;
  }
  public String getMaxCylinderSize()
  {
    return maxCylinderSize;
  }
  public void setMaxExtendSize( String maxExtendSize )
  {
    this.maxExtendSize = maxExtendSize;
  }
  public String getMaxExtendSize()
  {
    return maxExtendSize;
  }
  public void setCollectors( String collectors )
  {
    this.collectors = collectors;
  }
  public String getCollectors()
  {
    return collectors;
  }
  public void setSteps( String steps )
  {
    this.steps = steps;
  }
  public String getSteps()
  {
    return steps;
  }
  public void setVolid( String volid )
  {
    this.volid = volid;
  }
  public String getVolid()
  {
    return volid;
  }
  public void setStgcls( String stgcls )
  {
    this.stgcls = stgcls;
  }
  public String getStgcls()
  {
    return stgcls;
  }
  public void setMgtcls( String mgtcls )
  {
    this.mgtcls = mgtcls;
  }
  public String getMgtcls()
  {
    return mgtcls;
  }

  /**
   *
   */
  public void create()
    throws B390CommandException, IOException, InterruptedException
  {
    B390CommandInterface.createShadowRelease(getName(),
            getMvsHighLevelQualifier(),
            getPrimaryDataSetSize(), getSecondaryDataSetSize(),
            getPrimaryUnibankSize(), getMaxCylinderSize(),
            getMaxExtendSize(), getCollectors(), getSteps(), getVolid(),
            getStgcls(), getMgtcls(), cmvcFamily, cmvcFamilyAddr);
  }

  /**
   *
   */
  public void delete()
    throws B390CommandException, IOException, InterruptedException
  {
    B390CommandInterface.deleteShadowRelease(getName(),
                                             getMvsHighLevelQualifier());
  }

  /**
   * Returns number of changed files by reading the B390.lst file that was
   * created by parse_all target for this release (FMID)
   *
   * @return number of changed files; returns 0 if B390.LST file does not exist
   *         for this release.
   */
  private int getNoOfChangedFiles()
  {
    String lstFileName = MvsProperties.pkgControlDir + this.name + "/B390.LST";
    int noFiles = 0;
    BufferedReader lstFileReader = null;
    try
    {
      lstFileReader = new BufferedReader(new FileReader(lstFileName));
      while (lstFileReader.readLine() != null)
      {
        noFiles++;
      }
    }
    catch (Exception ex)
    {
      // ignoring this exception purposely
    }
    finally
    {
      if (lstFileReader != null)
      {
        try
        {
          lstFileReader.close();
        }
        catch (IOException ex)
        {
          // ignoring this exception purposely
        }
      }
    }
    return noFiles;
  }

  /**
   * This method creates an APAR driver for a release.
   * If the variable "B390_AUTO_DELETE_DRIVER" is set to YES or left blank, then
   * the existing APAR driver will be deleted and the driver is created again. 
   * If the variable "B390_AUTO_DELETE_DRIVER" is set to NO, then driver report
   * checks for the existance of the APAR driver and if the driver already exist,
   * then the driver will not be deleted.  In either instance (YES or NO), if no
   * APAR driver exist, then the driver will be created.  
   * Default: (variable "B390_AUTO_DELETE_DRIVER" not defined)
   *   
   * @return APAR driver for this release.   
   */
  private B390APARDriver createDriver( String driverName )
    throws B390CommandException, IOException, InterruptedException
  {
    boolean driverExist = true;
        
    if (MvsProperties.isDeleteDriver())
    {
       System.out.println("Deleting existing APAR driver '" + driverName + "'");

       deleteDriver(driverName);
       driverExist = false;
    }
    else
    {
       System.out.println("Checking driver report for APAR driver '" + driverName + "'");
       
       if(B390CommandInterface.driverExists(this.getName(), driverName))
       {
          System.out.println("APAR driver '" + driverName + "' already exist");
          driverExist = true;
       }
       else
       { 
          System.out.println("APAR driver '" + driverName + "' does not exist");
          System.out.println("Driver '" + driverName +
                             "' is not defined for Build/390 Release '" +
                             this.name + "'");
          driverExist = false;
       }       
    }

    if(!driverExist)
    {
       System.out.println("Creating APAR driver '" + driverName +
                          "' in Build/390 Release '" + this.getName() +"'");
       int noShippedFiles = getNoOfChangedFiles();
       B390CommandInterface.createDriver(this.getName(), driverName,
               getMvsHighLevelQualifier(), drvPrimaryDataSetSize,
               drvSecondaryDataSetSize, drvPrimaryUnibankSize,
               drvMaxCylinderSize, drvMaxExtendSize,
               drvVolid, drvStgcls, drvMgtcls, noShippedFiles);
    }

    B390APARDriver driver = new B390APARDriver();
    driver.setMvsRelease(this.name);
    this.drivers.put(driverName, driver);
    return driver;
  }

  /**
   *
   */
  private void deleteDriver( String driverName )
    throws B390CommandException, IOException, InterruptedException
  {
    B390APARDriver driver = new B390APARDriver(true);
	 boolean do_cleanup = false;
    try
    {
      B390CommandInterface.deleteDriver(this.getName(), driverName, driver);
    }
    catch (B390CommandException ex)
    {
      // RC of 50 means the driver does not exist
      if (ex.getReturnCode() == B390CommandInterface.B390_DRIVER_NOT_FOUND_RC)
      {
        System.out.println("Driver '" + driverName +
                           "' is not defined for Build/390 Release '" +
                           this.name + "'");
      }
      else if (ex.getReturnCode() != 0)
      {
			do_cleanup = true;
      }
    }
	 if (do_cleanup)
	 {
	   try
		{
        B390CommandInterface.driverCleanup(driver.getBuildId());
        B390CommandInterface.deleteDriver(this.getName(), driverName, driver);
	   }
		catch (B390CommandException ex)
		{
		}
	 }
    if (this.drivers.containsKey(driverName))
    {
      this.drivers.remove(driverName);
    }
  }

  /**
   *
   */
  public void createTestPackage( String driverName, boolean retainHasAPAR )
    throws B390CommandException, IOException, InterruptedException
  {
    B390APARDriver driver = new B390APARDriver();
    driver.setMvsRelease(this.name);
    driver.createTestPackage(driverName, this.getName(),
                             getMvsHighLevelQualifier(), retainHasAPAR);
  }

  /**
   *
   */
  public void build( String driverName )
    throws B390CommandException, IOException, InterruptedException
  {
     B390APARDriver driver = createDriver(driverName);
     driver.build(driverName, this.getName(), getMvsHighLevelQualifier());

  }

  /**
   *
   */
  public void createCopySentDriver( String pdt )
    throws B390CommandException, IOException, InterruptedException
  {
    B390CommandInterface.createCopySentDriver(getName(),
             getMvsHighLevelQualifier(),
             getHighLevelComponent(), pdt, drvPrimaryDataSetSize,
             drvSecondaryDataSetSize, drvPrimaryUnibankSize, drvMaxCylinderSize,
             drvMaxExtendSize, drvVolid, drvStgcls, drvMgtcls,
             this.getNoShippedFiles());
  }

  /**
   *
   */
  public void defineVersion( String pdt )
    throws B390CommandException, IOException, InterruptedException
  {
    String changeTeam = getChangeTeam();
    String copyright = "2001";  // Hard-code this for now
    B390CommandInterface.defineVersion(getComponentId(),
                    getRetainRelease(), pdt, getName(),
                    changeTeam, copyright,
                    getBase(), getSystemRelease(), cmvcFamily,
                    cmvcFamilyAddr, getSystemName(), getFeatureFMIDs());
  }

  /**
   *
   */
  public static void usage()
  {
    String usage = "B390Relase(String releaseName, String highLevelQualifier)\n";
    usage += "releaseName is the name of the release on MVS which must ";
    usage += "be less than 8 characters long. \n";
    usage += "highLevelQualifier is the MVS high level qualifier containing";
    usage += " the release which must be less than 8 characters long.\n";
    System.out.println(usage);
  }
}
