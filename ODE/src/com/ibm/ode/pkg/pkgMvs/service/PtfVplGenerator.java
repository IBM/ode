/*******************************************************************************
 * Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.2
 *
 * Date and Time File was last checked in: 5/10/03 15:30:50
 * Date and Time File was extracted/checked out: 06/04/13 16:47:06
 ******************************************************************************/
package com.ibm.ode.pkg.pkgMvs.service;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.string.StringTools;
import com.ibm.ode.pkg.pkgCommon.PkgApiUtil;
import com.ibm.ode.pkg.pkgMvs.*;
import com.ibm.sdwb.bps.api.servicepkg.*;

/**
 * Class to generate PTF VPL for MVS Service Packaging
 *
 * @author Wayne Mathis
 * @version 1.2
 */
public class PtfVplGenerator
{
  private String[] cmvcLevels;
  private String[] cmvcReleases;
  private ServicePackagingInterface servicePkgInterface = null;

  /**
   * Default constructor
   */
  public PtfVplGenerator()
  {
    cmvcLevels =
      StringTools.split(MvsProperties.cmvcLevel, ",");
    cmvcReleases =
      StringTools.split(MvsProperties.cmvcRelease, ",");
  }


  /**
   * Read package control file, call the methods for generating ptf vpl steps,
   * submitting & monitoring job(s) and printing job output for SP
   *
   * @return  int value to exit with
   * @exception MvsPkgError when an error occurs during Job Management phase
   */
  public int executeVplTarget()
    throws MvsPkgError
  {
    int errorCode = 0;
    long waitTime;
    try
    {
      // open the control file
      MvsControlFile controlFileObj = new MvsControlFile();
      MvsPackageData pkgData;
      ArrayList jobArray = new ArrayList();
      while ((pkgData = controlFileObj.nextPackage()) != null)
      {
        String function =
          (String)pkgData.getProductData().get("FUNCTION");

        String lstFile =
          MvsProperties.pkgControlDir + function + "/B390.LST";
        if (!Path.exists(lstFile) ||
            !isVplStepRequired(pkgData))
        {
          continue;
        }

        MvsJclGenerator jclGen =
          new MvsJclGenerator(pkgData,
                              controlFileObj.getPkgNum());

        MvsJobInfo jobInfo = null;
        jobInfo = jclGen.generatePtfVplStep(getPtfNumber(function));

        jobArray.add(jobInfo);

        // force garbage collection (package metadata objects
        // with many parts can consume a lot of storage)
        pkgData = null;
        jclGen  = null;
        System.gc();
      }

      // If there are no entries in the jobArray object then return. There are
      // jobs to process.
      if (jobArray.size() <= 0)
      {
        System.out.println("VPL generation is not required as there are no " +
                           "modified parts that are of type VPL. Exiting...");
        return 0;
      }

      // allow objects to be finalized/gc'ed
      controlFileObj = null;

      System.out.println("All JCL generation complete");
      System.out.println();

      // Job Management phase
      System.out.println("Entering job management phase");
      try
      {
        if (MvsProperties.jobMonitorTime == null)
          waitTime = 15;
        else
          waitTime = new Long(MvsProperties.jobMonitorTime).longValue();
      }
      catch (NumberFormatException e)
      {
        System.err.println("Error converting " + MvsProperties.jobMonitorTime +
                           " to an numeric value -- ");
        System.err.println("defaulting to 15 minute job monitor time.");
        waitTime = 15;
      }
      System.out.println("Job status will be checked every " + waitTime +
                         " minutes");

      // If an exception occured in execute mangage Jobs, return 1
      if (RunMvsPkg.executeAndManageJobs(jobArray, waitTime) == 1)
      {
        return 1;
      }
      errorCode = RunMvsPkg.scanJobsAndSetStatus(jobArray);

      System.out.println("Ending job management phase");
      System.out.println();

      System.out.println("Execution summary for all jobs:");
      System.out.println();
      RunMvsPkg.printJobExecutionSummary( jobArray );
      System.out.println();

      System.out.println("Output of all jobs:");
      System.out.println();
      RunMvsPkg.printJobOutput( jobArray );
    }
    catch (MvsPkgError e)
    {
      System.err.println(e.getMessage());
      return 1;
    }

    if (errorCode == 1)
      return 1;
    else
      return 0;
  }

  /**
   * Returns PTF number for the specified function. This is done by accessing
   * the BPS database using BPS remote interface.
   *
   * @param function a valid function number
   * @exception MvsPkgError is thrown when error occurs retrieving PTF number
   *                        from BPS.
   */
  public String getPtfNumber( String function )
    throws MvsPkgError
  {
    try
    {
      if (servicePkgInterface == null)
      {
        servicePkgInterface =
          PkgApiUtil.getServicePackagingInterface(MvsProperties.pkgApiUrl);
        PkgApiUtil.verifyPkgApiVersion(servicePkgInterface);
      }
      String[] functions = new String[1];
      functions[0] = function;
      String[] ptfNames =
        servicePkgInterface.getPTFsFromFMIDs(functions,
                                             cmvcLevels,
                                             cmvcReleases,
                                             MvsProperties.cmvcFamily);
      return ptfNames[0].substring(0, ptfNames[0].indexOf(","));
    }
    catch (Exception ex)
    {
      throw new MvsPkgError(
        "An error occurred while retrieving PTF number for the Function : " +
        function + "\nThe Exception message is as follows :\n" +
        ex.getLocalizedMessage());
    }
  }

  /**
   * If there is any VPL file that belongs to the specified PackageData object
   * and that is modified in the sandbox that is used for PTF creation then
   * return true.
   *
   * @param pkgData MvsPackageData object
   */
  private boolean isVplStepRequired( MvsPackageData pkgData )
  {
    HashSet vplFileSet = pkgData.getAllVplFiles();
    Iterator hsi = vplFileSet.iterator();
    while (hsi.hasNext())
    {
      if (Path.exists(MvsProperties.toStage + (String)hsi.next()))
        return true;
    }
    return false;
  }
}

