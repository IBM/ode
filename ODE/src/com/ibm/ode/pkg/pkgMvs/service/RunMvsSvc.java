/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.3
 *
 * Date and Time File was last checked in: 5/5/04 11:33:02
 * Date and Time File was extracted/checked out: 04/10/04 09:13:30
 ****************************************************************************/
package com.ibm.ode.pkg.pkgMvs.service;

import java.io.IOException;
import java.util.*;
import com.ibm.ode.pkg.pkgMvs.*;
import com.ibm.ode.lib.io.Path;

/**
 * Main driver for MVS SP/Service gather and packaging.
 */
public class RunMvsSvc
{
  public static void main(String[] args)
    throws B390CommandException, MvsPkgError, InterruptedException, IOException
  {
    // make sure all required properties are set
    if (!MvsProperties.checkValues())
    {
      System.exit(1);
    }

    // Set Build390 Client path, if the PTFVPL process is not running
    if (MvsProperties.pkgType != null &&
        MvsProperties.pkgType.equalsIgnoreCase("ptfvpl"))
    {
      // do not set the path to the B390 client if the pkg type is ptfvpl.
      // we are not using B390 client for generating vpl
    }
    else
    {
      try
      {
        B390CommandInterface.setB390Path(MvsProperties.B390Path);
      }
      catch (B390CommandException ex)
      {
        System.err.println(ex.getMessage());
      }
    }

    if (MvsProperties.pkgEvent.equalsIgnoreCase("getptfnums"))
    {
      definePTFs();
    }
    else if (MvsProperties.pkgEvent.equalsIgnoreCase("package"))
    {
      servicePackage();
    }
    else
    {
      System.err.println("The PKG_EVENT ("+MvsProperties.pkgEvent+") variable "
                         + "must be set to \"package\" or \"getptfnums\"");
      System.exit(1);
    }
  } //main()

  /**
   * getAPAR
   */
  private static B390APARInfo getAPAR(String aparName, boolean retainHasAPAR)
  {
    try
    {
      // open the control file
      Vector releases = getReleases();
      if (!releases.isEmpty())
      {
        B390APARInfo apar = new B390APARInfo(aparName, releases,
                                             retainHasAPAR);
        return apar;
      }
    }
    catch (MvsPkgError e)
    {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    catch (B390CommandException e)
    {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    return null;
  }

  /**
   * getReleases
   */
  private static Vector getReleases()
    throws MvsPkgError, B390CommandException
  {
    Vector releases = new Vector();

    // open the control file
    MvsControlFile controlFile = new MvsControlFile();
    MvsPackageData pkgData;
    while ((pkgData = controlFile.nextPackage()) != null)
    {
      Hashtable pd = pkgData.getProductData();
      String mvsHLQ = (String)pd.get("APPLID");     // MVS High level qualifier
      String mvsRelease = (String)pd.get("FUNCTION");   // function, FMID, entityName
      B390Release release = new B390Release(mvsRelease, mvsHLQ);

      // Only add release if it has affected files
      // if the PKG_CONTROL_DIR/<fmid>/LST file exists, then assume the fmid
      //   has affected files
      if (Path.exists(MvsProperties.pkgControlDir + mvsRelease + "/B390.LST"))
        releases.addElement(release);
    }
    return releases;
  }

  /**
   * parseMultiEntryString
   */
  private static String[] parseMultEntryString( String string )
  {
    StringTokenizer st = new StringTokenizer(string, ",");
    String[] result = new String[st.countTokens()];
    for (int i = 0; i<result.length; i++)
    {
      result[i] = st.nextToken();
    }
    return result;
  }

  /**
   * Run PTF definition commands
   */
  public static void definePTFs()
    throws B390CommandException, MvsPkgError,InterruptedException, IOException
  {
    B390PTFDriver ptfDriver = getPTFDriver();
    ptfDriver.definePTF(MvsProperties.supersedePTFs);
  }

  /**
   * Run APAR/PTF build commands
   */
  public static void servicePackage()
    throws B390CommandException, MvsPkgError,InterruptedException, IOException
  {
    int exitValue = 0;
    String aparName = MvsProperties.apar;
    String usermodName = MvsProperties.usermod;
    String pkgClass = MvsProperties.pkgClass;
    String pkgType = MvsProperties.pkgType;
    boolean retainHasAPAR = true;

    // Validate PKG_CLASS
    if (pkgClass == null || pkgClass.length() == 0)
    {
      pkgClass = "SP";
    }
    if (!pkgClass.equalsIgnoreCase("sp"))
    {
      System.err.println("The PKG_CLASS variable must be set to \"SP\"");
      System.exit(1);
    }

    // Validate PKG_TYPE
    if (pkgType.equalsIgnoreCase( "apar" ) ||
        pkgType.equalsIgnoreCase( "usermod" ) ||
        pkgType.equalsIgnoreCase( "++apar" ) ||
        pkgType.equalsIgnoreCase( "++usermod" ))
    {
      // Usermod settings
      if (pkgType.equalsIgnoreCase("usermod") || pkgType.equalsIgnoreCase("++usermod"))
      {
        if (usermodName != null && usermodName.length() != 0)
          aparName = usermodName;
        retainHasAPAR = false;
      }

      // APAR/USERMOD must be defined
      if (aparName == null || aparName.length() == 0)
      {
        System.err.println("Must set either APAR or USERMOD for MVS service packaging.");
        System.exit(1);
      }

      // Run Packaging commands
      try
      {
        B390APARInfo apar = getAPAR(aparName, retainHasAPAR);
        if (apar != null)
        {
          if (pkgType.equalsIgnoreCase("apar")||
              pkgType.equalsIgnoreCase("usermod"))
          {
            apar.define();
          }
          else if (pkgType.equalsIgnoreCase("++apar")||
                   pkgType.equalsIgnoreCase("++usermod"))
          {
            apar.createTestPackages(retainHasAPAR);
          }
        }
      }
      catch (B390CommandException ex)
      {
        System.err.println(ex.getMessage());
        System.exit(1);
      }
      catch (IOException ex)
      {
        System.err.println(ex.getMessage());
        System.exit(1);
      }
      catch (InterruptedException ex)
      {
        System.err.println(ex.getMessage());
        System.exit(1);
      }
    }
    else if (pkgType.equalsIgnoreCase("ptf"))
    {
      B390PTFDriver ptfDriver = getPTFDriver();
      ptfDriver.prepForPtf();
    }
    else if (pkgType.equalsIgnoreCase("++ptf"))
    {
      B390PTFDriver ptfDriver = getPTFDriver();
      ptfDriver.createPTFs();
    }
    else if (pkgType.equalsIgnoreCase("ptfvpl"))
    {
      int exitVal = 0;
      PtfVplGenerator ptfVpl = new PtfVplGenerator();
      exitVal = ptfVpl.executeVplTarget();
      System.exit(exitVal);
    }
    else if (pkgType.equalsIgnoreCase("retrieveApar") ||
             pkgType.equalsIgnoreCase("retrieveUsermod"))
    {
      MvsControlFile controlFile = new MvsControlFile();
      MvsPackageData pkgData;
      String hlq = null;
      while ((pkgData = controlFile.nextPackage()) != null)
      {
        Hashtable pd = pkgData.getProductData();
        String mvsRelease = (String)pd.get("FUNCTION");
        if (mvsRelease.equalsIgnoreCase(MvsProperties.function))
          hlq = (String)pd.get("APPLID");
      }
      RunMvsSvc.retrieveApar(hlq, MvsProperties.function,
                             MvsProperties.apar,
                             MvsProperties.shipAparName,
									  pkgType.equalsIgnoreCase("retrieveUsermod"));
    }
    else if (pkgType.equalsIgnoreCase("retrievePtf"))
    {
      MvsControlFile controlFile = new MvsControlFile();
      MvsPackageData pkgData;
      String hlq = null;
      while ((pkgData = controlFile.nextPackage()) != null)
      {
        Hashtable pd = pkgData.getProductData();
        String mvsRelease = (String)pd.get("FUNCTION");
        if (mvsRelease.equalsIgnoreCase(MvsProperties.function))
          hlq = (String)pd.get("APPLID");
      }

      System.out.print("Getting PTF number for Function '" +
                       MvsProperties.function + "'...");
      PtfVplGenerator ptfVplGenerator = new PtfVplGenerator();
      String ptfNumber = ptfVplGenerator.getPtfNumber(MvsProperties.function);
      System.out.println("done");

      RunMvsSvc.retrievePtf(hlq, MvsProperties.function, ptfNumber);
    }
    else
    {
      System.err.println("Invalid PKG_TYPE specified: " + pkgType);
      System.exit(1);
    }
  }

  /**
   * Returns B390PTFDriver object
   */
  private static B390PTFDriver getPTFDriver()
    throws B390CommandException, MvsPkgError
  {
    String [] cmvcLevels = parseMultEntryString( MvsProperties.cmvcLevel );
    String [] cmvcReleases = parseMultEntryString( MvsProperties.cmvcRelease );
    String cmvcFamily = MvsProperties.cmvcFamily;
    String bpsServerUrl = MvsProperties.pkgApiUrl;
    String shipCode = MvsProperties.ptfShipCode;
    return new B390PTFDriver( bpsServerUrl, getReleases(), cmvcLevels,
                              cmvcReleases, cmvcFamily, shipCode );
  }

  /**
   * Creates a dataset of type PDS with the specified name.
   *
   * @param function a valid function name
   * @param dsnName dataset name
   */
  public static int createPDSDataSet( String function, String dsnName )
  {
    // Check if PKG_MVS_JOBCARD, PKG_MVS_USERID, PKG_MVS_PASSWORD and
    // PKG_MVS_EXEC_DATASET properties are set
    if (!MvsProperties.checkReqPropForSubmittingJob())
    {
      return 1;
    }

    MvsJclGenerator jclGen = new MvsJclGenerator(null, 1);
    int errorCode = 0;
    try
    {
      System.out.println("Generating JCL file '" +
                         function + ".createPDS.jcl'");
      MvsJobInfo jobInfo =
        jclGen.generateAllocLibForLogRetrieve(function, dsnName);
      ArrayList jobArray = new ArrayList();
      jobArray.add(jobInfo);

      // If an exception occured in execute mangage Jobs, return 1
      if (RunMvsPkg.executeAndManageJobs(jobArray, 1) == 1)
      {
        errorCode = 1;
      }
      if (errorCode != 1)
      {
        errorCode = RunMvsPkg.scanJobsAndSetStatus(jobArray);
        if (errorCode == 1)
        {
          System.out.println("Execution summary for the job:");
          RunMvsPkg.printJobExecutionSummary(jobArray);
          System.out.println();
          System.out.println("Output of the job:");
          RunMvsPkg.printJobOutput(jobArray);
        }
      }
    }
    catch (MvsPkgError ex)
    {
      errorCode = 1;
    }

    if (errorCode == 1)
    {
      System.err.println("Unable to create PDS '" + dsnName + "'");
      return 1;
    }
    return 0;
  }

  /**
   * Retrieves ++PTF from its driver and copies it into the dataset(PDS)
   * specified by the B390_COPYTO property.
   *
   * @param hlq high level qualifier
   * @param function a valid function name
   * @param ptfNumber a valid PTF number
   * @exception IOException
   * @exception B390CommandException
   * @exception InterruptedException
   */
  public static void retrievePtf( String hlq,
                                  String function,
                                  String ptfNumber )
    throws IOException, B390CommandException, InterruptedException
  {
    String dsnName = MvsProperties.copyTo;
    int rc = 0;
    if (dsnName == null)
    {
      StringBuffer sb = new StringBuffer(hlq);
      sb.append(".").append(function).append(".");
      sb.append(ptfNumber).append(".PTF");
      dsnName = sb.toString();
      System.out.println("Creating PDS '" + dsnName + "'");
      rc = RunMvsSvc.createPDSDataSet(function, dsnName);
    }
    if (rc == 0)
    {
      System.out.println("Done creating PDS '" + dsnName + "'");
      System.out.println("Retrieving ++PTF '" + ptfNumber +
                         "' from PTF driver '" + ptfNumber + "'");
      B390CommandInterface.logRetrieve("PTF", function,
                                       ptfNumber, ptfNumber,
                                       dsnName, MvsProperties.shipTo);
    }
    else
    {
      System.exit(1);
    }
  }

  /**
   * Retrieves specified ++APAR from the specified driver and copies it into
   * the dataset(PDS) specified by the B390_COPYTO property. If B390_COPYTO
   * is not set then a PDS is created.
   *
   * @param hlq high level qualifier
   * @param function a valid function name
   * @param aparName a valid APAR name
   * @param shipAparName a valid ++APAR name
	* @param getUsermod if true get a ++USERMOD, otherwise get a ++APAR
   * @exception IOException
   * @exception B390CommandException
   * @exception InterruptedException
   */
  public static void retrieveApar( String hlq,
                                   String function,
                                   String aparName,
                                   String shipAparName,
											  boolean getUsermod )
    throws IOException, B390CommandException, InterruptedException
  {
    String dsnName = MvsProperties.copyTo;
    int rc = 0;
    if (dsnName == null)
    {
      StringBuffer sb = new StringBuffer(hlq);
      sb.append(".").append(function).append(".");
      sb.append(MvsProperties.apar).append(".APAR.").append(shipAparName);
      dsnName = sb.toString();
      System.out.println("Creating PDS '" + dsnName + "'");
      rc = RunMvsSvc.createPDSDataSet(function, dsnName);
    }
    if (rc == 0)
    {
      System.out.println("Done creating PDS '" + dsnName + "'");
      System.out.println("Retrieving ++" + (getUsermod ? "USERMOD" : "APAR") +
                         " '" + shipAparName +
                         "' from APAR driver '" + aparName + "'");
      B390CommandInterface.logRetrieve((getUsermod ? "USERMOD" : "APAR"),
		                                 function, shipAparName, aparName,
                                       dsnName, MvsProperties.shipTo);
    }
    else
    {
      System.exit(1);
    }
  }
}
