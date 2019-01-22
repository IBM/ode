/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.2
 *
 * Date and Time File was last checked in: 11/14/03 15:00:36
 * Date and Time File was extracted/checked out: 04/10/04 09:13:27
 ****************************************************************************/
package com.ibm.ode.pkg.pkgMvs.service;

import com.ibm.ode.pkg.pkgMvs.MvsProperties;
import java.io.*;
import java.util.StringTokenizer;

/**
 *
 */
public class B390CommandInterface
{
  public static final int B390_DRIVER_NOT_FOUND_RC = 50;
  private static final String DEFAULT_TEMPDIR = "/tmp";
  private static final String odeFlag = "/odebuild";
  private static final String b390DebugFlag = "/DEBUG1";
  private static B390CommandHandler defaultExecHandler =
    new B390CommandHandler();
  private static String b390Path = "";
  private static boolean isB390Debug = MvsProperties.isB390Debug();

   /**
    * driverExists
    */
   public static boolean driverExists( String release, String driverName )
      throws B390CommandException, IOException, InterruptedException
   {
      boolean driverExists = false;
      try
      {
         getReport(release, driverName);
         driverExists = true;
      }
      catch (B390CommandException ex)
      {
         int returnCode = ex.getReturnCode();
         switch (returnCode)
         {
            case B390CommandException.mvsError:
               driverExists = false;
               break;
            default:
               throw ex;
         }
      }
      return driverExists;
   }

   /**
    * getB390ExecName
    */
   private static String getB390ExecName()
      throws B390CommandException
   {
      // Call via java -DMBGlobals.Build390_path=${B390_PATH} Build390.MBClient
      String execname = "java -DMBGlobals.Build390_path=" +
                        getB390Path() + " Build390.MBClient";
      return execname;
   }

   /**
    *
    */
   private static String getParmFile()
   {
      return MvsProperties.B390ParmFile;
   }

   /**
    *
    */
   public static String getB390Path() throws B390CommandException
   {
      if (b390Path == null || b390Path.length() == 0)
         throw new B390CommandException("Path to Build/390 client is undefined.");
      return b390Path;
   }

   /**
    *
    */
   public static void setB390Path( String b390Path )
      throws B390CommandException
   {
      if (b390Path == null)
      {
         throw new B390CommandException("Path to Build/390 client cannot be null.");
      }
      b390Path = b390Path.trim();
      if (b390Path.length() == 0)
      {
         throw new B390CommandException("Path to Build/390 client cannot be blank.");
      }
      B390CommandInterface.b390Path = b390Path.trim();
   }

   /**
    * getReport
    */
   public static void getReport( String mvsRelease, String driverName )
      throws B390CommandException, IOException, InterruptedException
   {
      StringBuffer args = new StringBuffer();
      String command = "DRVRRPT";
      args.append(" LIBRELEASE=").append(mvsRelease);
      args.append(" DRIVER=").append(driverName);
      args.append(" TYPE=STATUS");
      if (getParmFile() != null)
      {
         args.append(" PARMFILE=").append(getParmFile());
      }
      command = getB390ExecName() + " " + command + args.toString();
      if (isB390Debug) command += " " + b390DebugFlag;
      defaultExecHandler.executeCommand(command);
      args = null;
   }

   /**
    * deleteShadowRelease
    */
   public static void deleteShadowRelease( String mvsRelease,
                                           String mvsHighLevelQualifier )
      throws B390CommandException, IOException, InterruptedException
   {
      String args = "";
      String command = "DELETERELEASE";
      args += " LIBRELEASE=" + mvsRelease;
      args += " MVSRELEASE=" + mvsRelease;
      args += " CHILVL=" + mvsHighLevelQualifier;
      if (getParmFile() != null) args += " PARMFILE=" + getParmFile();
      args += " " + odeFlag;
      command = getB390ExecName() + " " + command + args;
      if (isB390Debug) command += " " + b390DebugFlag;
      defaultExecHandler.executeCommand(command);
   }

   /**
    * deleteDriver
    */
   public static void deleteDriver( String mvsRelease, String driverName,
                                    B390APARDriver driver )
      throws B390CommandException, IOException, InterruptedException
   {
      String args = "";
      String command = "DELETEDRIVER";
      args += " LIBRELEASE=" + mvsRelease;
      args += " MVSRELEASE=" + mvsRelease;
      args += " DRIVER=" + driverName;
      if (getParmFile() != null) args += " PARMFILE=" + getParmFile();
      args += " " + odeFlag;
      command = getB390ExecName() + " " + command + args;
      if (isB390Debug) command += " " + b390DebugFlag;
      driver.executeCommand(command);
   }

   /**
    * createShadowRelease
    */
   public static void createShadowRelease(
                    String mvsRelease, String mvsHighLevelQualifier,
                    String primaryDataSetSize, String secondaryDataSetSize,
                    String primaryUnibankSize, String maxCylinderSize,
                    String maxExtendSize, String collectors, String steps,
                    String volid, String stgcls, String mgtcls,
                    String cmvcFamily, String cmvcFamilyAddr )
      throws B390CommandException, IOException, InterruptedException
   {
      String command = "CREATESHADOW";
      String args = " CHILVL=" + mvsHighLevelQualifier;
      args += " NEWSHADOW=" + mvsRelease;

      args += " LIBFAMILY=" + cmvcFamily;
      args += " LIBADDR=" + cmvcFamilyAddr;
      if (primaryDataSetSize != null) args += " CBLKP=" + primaryDataSetSize;
      if (secondaryDataSetSize != null) args += " CBLKS=" + secondaryDataSetSize;
      if (primaryUnibankSize != null) args += " CUBKP=" + primaryUnibankSize;
      if (maxCylinderSize != null) args += " CMAXCYL=" + maxCylinderSize;
      if (maxExtendSize != null) args += " CMAXEXT=" + maxExtendSize;
      if (collectors != null) args += " CCLTR=" + collectors;
      if (steps != null) args += " CPRCS=" + steps;
      if (volid != null)
      {
         args += " CVOLID=" + volid;
      }
      else
      {
         if (stgcls != null) args += " CSTGCLS=" + stgcls;
         if (mgtcls != null) args += " CMGTCLS=" + mgtcls;
      }
      args += " " + odeFlag;
      if (getParmFile() != null) args += " PARMFILE=" + getParmFile();
      command = getB390ExecName() + " " + command + args;
      if (isB390Debug) command += " " + b390DebugFlag;
      defaultExecHandler.executeCommand( command );
   }

   /**
    * create B390AparDriver
    */
   public static void createDriver( String mvsRelease,
                    String driverName, String highLevelQualifier,
                    String primaryDataSetSize, String secondaryDataSetSize,
                    String primaryUnibankSize, String maxCylinderSize,
                    String maxExtendSize, String volid,
                    String stgcls, String mgtcls, int shippedFilesCount )
      throws B390CommandException, IOException, InterruptedException
   {
      String command = "CREATEDRIVER";
      StringBuffer args = new StringBuffer();
      args.append(" CHILVL=").append(highLevelQualifier);
      args.append(" MVSRELEASE=").append(mvsRelease);
      args.append(" NEWDRIVER=").append(driverName);
      args.append(" BASEDRIVER=COPYSENT");
      args.append(" CDRVRTYP=DELTA");

      // use NUMPARTS instead of the following three attributes if any
      // of them are not specified
      if (primaryDataSetSize == null ||
          secondaryDataSetSize == null ||
          primaryUnibankSize == null)
      {
        args.append(" NUMPARTS=").append(shippedFilesCount);
      }
      else
      {
        args.append(" CBLKP=").append(primaryDataSetSize);
        args.append(" CBLKS=").append(secondaryDataSetSize);
        args.append(" CUBKP=").append(primaryUnibankSize);
      }

      if (maxCylinderSize != null)
        args.append(" CMAXCYL=").append(maxCylinderSize);
      if (maxExtendSize != null)
        args.append(" CMAXEXT=").append(maxExtendSize);

      if (volid != null)
      {
        args.append(" VOLID=").append(volid);
      }
      else
      {
        if (stgcls != null) args.append(" STGCLS=").append(stgcls);
        if (mgtcls != null) args.append(" MGTCLS=").append(mgtcls);
      }
      if (getParmFile() != null)
        args.append(" PARMFILE=").append(getParmFile());
      args.append(" ").append(odeFlag);
      command = getB390ExecName() + " " + command + " " + args.toString();
      if (isB390Debug) command += " " + b390DebugFlag;
      defaultExecHandler.executeCommand(command);
   }

   /**
    * driverBuild
    */
   public static void driverBuild( String driverName,
                                   String mvsRelease,
                                   String mvsHighLevelQualifier,
                                   B390CommandHandler driver,
                                   B390DriverBuildOptions driverOptions )
      throws B390CommandException, IOException, FileNotFoundException,
             InterruptedException
   {
      //copy( from => "${PKG_CONTROL_DIR}/<fmid>/B390.LST"
      //        to => "/<tempdir>/<hlq>_<fmid>_<apar>_B390.LST" );
      String inputFileName = "B390.LST";
      File odeLSTFile = getODEControlFile(inputFileName, mvsRelease);
      File mvsLSTFile = getMVSControlFile(inputFileName, driverName,
                                          mvsRelease, mvsHighLevelQualifier);
      System.out.println("copying file: " + odeLSTFile.toString() + "\n" +
                         "          to: " + mvsLSTFile.toString() );
      B390CommandInterface.copy(odeLSTFile, mvsLSTFile);

      inputFileName = "B390.UPD";
      File odeUPDFile = getODEControlFile(inputFileName, mvsRelease);
      File mvsUPDFile = getMVSControlFile(inputFileName, driverName,
                                          mvsRelease, mvsHighLevelQualifier);
      B390CommandInterface.copy(odeUPDFile, mvsUPDFile);
      System.out.println("copying file: " + odeUPDFile.toString() + "\n" +
                         "          to: " + mvsUPDFile.toString() );

      try
      {
         driver.executeCommand(getDriverBuildCommand(driverName, mvsRelease,
                                                     mvsHighLevelQualifier,
                                                     driverOptions.getRunScan(),
                                                     driverOptions.getShipTo(),
                                                     driverOptions.getShipType()));
      }
      finally
      {
         System.out.println("deleting file: " + mvsLSTFile.toString());
         if (mvsLSTFile.exists()) mvsLSTFile.delete();
         System.out.println("deleting file: " + mvsUPDFile.toString());
         if (mvsUPDFile.exists()) mvsUPDFile.delete();
      }
   }

   /**
    * copyControlFiles
    */
   public static void copyControlFiles( String driverName,
                                        String mvsRelease,
                                        String mvsHighLevelQualifier )
      throws B390CommandException, IOException, FileNotFoundException, InterruptedException
   {
      //copy( from => "${PKG_CONTROL_DIR}/<fmid>/B390.LST"
      //        to => "/<tempdir>/<hlq>_<fmid>_<apar/ptf>_B390.LST" );
      String inputFileName = "B390.LST";
      File odeLSTFile = getODEControlFile(inputFileName, mvsRelease);
      File mvsLSTFile = getMVSControlFile(inputFileName, driverName,
                                          mvsRelease, mvsHighLevelQualifier);
      System.out.println("copying file: " + odeLSTFile.toString() + "\n" +
                         "          to: " + mvsLSTFile.toString());
      B390CommandInterface.copy(odeLSTFile, mvsLSTFile);

      //copy( from => "${PKG_CONTROL_DIR}/<fmid>/B390.UPD"
      //        to => "/<tempdir>/<hlq>_<fmid>_<apar/ptf>_B390.UPD" );
      inputFileName = "B390.UPD";
      File odeUPDFile = getODEControlFile(inputFileName, mvsRelease);
      File mvsUPDFile = getMVSControlFile(inputFileName, driverName,
                                         mvsRelease, mvsHighLevelQualifier);
      B390CommandInterface.copy(odeUPDFile, mvsUPDFile);
      System.out.println("copying file: " + odeUPDFile.toString() + "\n" +
                         "          to: " + mvsUPDFile.toString());
   }

   /**
    * Deletes .UPD and .LST files from the temp directory.
    */
   public static void deleteControlFiles()
   {
     File tempDirectory = getTempDirectory();
     if (!tempDirectory.exists())
       return;
     String[] files = tempDirectory.list();
     for (int idx = 0; idx < files.length; idx++)
     {
       if (files[idx].endsWith("B390.LST") || files[idx].endsWith("B390.UPD") ||
           files[idx].endsWith("b390.lst") || files[idx].endsWith("b390.upd"))
       {
         File file = new File(tempDirectory.getPath(), files[idx]);
         try
         {
           file.delete();
         }
         catch (SecurityException ex)
         {
           // Ignore the securityException that is thrown when deleting control
           // files from the temp directory.
         }
       }
     }
   }

   /**
    * copy - why is this here, move to com.ibm.ode.lib.io.Path?
    */
   public static void copy( File src, File dest )
      throws IOException, FileNotFoundException
   {
      BufferedReader reader = new BufferedReader(new FileReader(src));
      BufferedWriter writer = new BufferedWriter(new FileWriter(dest));
      String nextLine;
      while ((nextLine = reader.readLine()) != null)
      {
         writer.write(nextLine);
         writer.newLine();
      }
      writer.flush();
      writer.close();
   }

   /**
    * Build the APAR driver build
    */
   private static String getDriverBuildCommand( String  driverName,
                                                String  mvsRelease,
                                                String  mvsHighLevelQualifier,
                                                boolean runScanFlag,
                                                String  shipTo,
                                                String  shipType )
      throws B390CommandException
   {
      String args = "";
      String command = "DRIVERBUILD";
      args += " LIBRELEASE=" + mvsRelease;
      args += " DRIVER=" + driverName;
      args += " BUILDTYPE=ODE";
      args += " DESCRIPTION=\"BUILD " + driverName + "\"";
      args += " DELTABUILD=YES";
      if (runScanFlag)
      {
         args += " RUNSCAN=YES";
      }
      else
      {
         args += " RUNSCAN=NO";
      }
      if (shipTo == null) shipTo = "";
      if (shipType == null) shipType = "";
      if (shipTo != "" && shipType != "")
      {
         args += " XMITTO=" + shipTo;
         args += " XMITTYPE=" + shipType;
      }
      args += " " + odeFlag;
      if (getParmFile() != null) args += " PARMFILE=" + getParmFile();
      command = getB390ExecName() + " " + command + " " + args;
      if (isB390Debug) command += " " + b390DebugFlag;
      return command;
   }

   /**
    * aparCheck
    */
   public static void aparCheck( String  driverName,
                                 String  mvsRelease,
                                 String  mvsHighLevelQualifier,
                                 B390APARDriver driver,
                                 boolean retainHasAPAR )
      throws B390CommandException, IOException, InterruptedException
   {
      String args = "";
      String command = "APARCHECK";
      args += " LIBRELEASE=" + mvsRelease;
      args += " APARNAME=" + driverName;
      String usermod = "";
      if (!retainHasAPAR) args += " BUILDUSERMOD=YES USERMODNAME=" + driverName;
      if (getParmFile() != null) args += " PARMFILE=" + getParmFile();
      args += " " + odeFlag;
      command = getB390ExecName() + " " + command + args;
      if (isB390Debug) command += " " + b390DebugFlag;
      driver.executeCommand(command);
   }

   /**
    * driver cleanup
    */
   public static void driverCleanup( String buildId )
      throws B390CommandException, IOException, InterruptedException
   {
      String args = "";
      String command = "CLEANUP";
      args += " BUILD1=" + buildId;
      args += " /HOSTDS /JOBS /UNLOCK /EXTRACT /LOCAL";
      if (getParmFile() != null) args += " PARMFILE=" + getParmFile();
      command = getB390ExecName() + " " + command + args;
      if (isB390Debug) command += " " + b390DebugFlag;
      defaultExecHandler.executeCommand(command);
   }

   /**
    * create ++APAR
    */
   public static void createTestPackage(B390APARDriver driver, B390APARBuildOptions options)
      throws B390CommandException, IOException, InterruptedException
   {
      String command = "APARBUILD";
      String args = "";
      args += " BUILDID=" + driver.getBuildId();
      args += " REBUILD=";
      if (options.getRebuild())
        args += "YES";
      else
         args += "NO";

      if (options.getShipTo() != "")
        args += " SHIPTO=" + options.getShipTo();
      if (options.getPath() != "")
        args += " PATH=" + options.getPath();
      if (options.getLogicFileName() != "")
        args += " LOGIC=" + options.getLogicFileName();
      if (options.getCommentsFileName() != "")
        args += " COMMENTS=" + options.getCommentsFileName();

      if (getParmFile() != null) args += " PARMFILE=" + getParmFile();
      args += " " + odeFlag;
      command = getB390ExecName() + " " + command + args;
      if (isB390Debug) command += " " + b390DebugFlag;
      driver.executeCommand(command);
   }

  /**
   * Retrieves a ++PTF or ++APAR from the specified driver. The driver could
   * be either PTF or APAR driver. It is specified by the parameter 'aparOrPtf'.
   * The retrieved information is copied to the directory or PDS specified by
   * parameter 'copyTo'.
   *
   * @param aparOrPtf String that contains either 'PTF' or 'APAR'
   * @param releaseName a valid CMVC release name
   * @param name a valid ++APAR or ++PTF name
   * @param driverName a valid PTF/APAR driver name
   * @param copyTo a valid HFS path or pre allocated partition dataset name.
   *               This parameter could be null.
   * @param sendTo VM node.userid where the extracted ++PTF or ++APAR is sent.
   *               This parameter could be null.
   * @throws B390CommandException
   * @throws IOException
   * @throws InterruptedException
   */
  public static void logRetrieve( String aparOrPtf, String releaseName,
                                  String name, String driverName,
                                  String copyTo, String sendTo )
    throws B390CommandException, IOException, InterruptedException
  {
    String command = "LOGRETRIEVE";
    StringBuffer args = new StringBuffer();
    args.append(" LIBRELEASE=").append(releaseName);
    args.append(" DRIVER=").append(driverName);
    args.append(" LOGNAME1=").append(name);
    args.append(" LOGTYPE1=OBJ");
    args.append(" LOGCLASS1=").append(aparOrPtf);
    if (copyTo != null)
    {
      if (copyTo.indexOf("/") >= 0)
        args.append(" HFSPATH=").append(copyTo);
      else
        args.append(" DSNPATH=").append(copyTo);
    }
    if (sendTo != null)
    {
      args.append(" SENDTO=").append(sendTo);
    }
    if (getParmFile() != null)
      args.append(" PARMFILE=").append(getParmFile());
    args.append(" ").append(odeFlag);
    command = getB390ExecName() + " " + command + " " + args.toString();
    if (isB390Debug) command += " " + b390DebugFlag;
    defaultExecHandler.executeCommand(command);
  }

  /**
    * getODEControlFile
    */
  private static File getODEControlFile( String inputFileName,
                                         String mvsRelease )
    throws B390CommandException
  {
    String controlDir = MvsProperties.pkgControlDir;
    String fileSeparator = System.getProperty("file.separator");
    if (!controlDir.endsWith(fileSeparator))
      controlDir += fileSeparator;
    mvsRelease = mvsRelease.toUpperCase();
    return new File(controlDir + mvsRelease + fileSeparator + inputFileName);
  }

  /**
    * getMVSLSTFile
    */
  private static File getMVSControlFile( String inputFileName,
                                         String driverName,
                                         String mvsRelease,
                                         String mvsHighLevelQualifier )
    throws B390CommandException
  {
    String attributeSeparator = "_";
    String outputFileName = mvsHighLevelQualifier + attributeSeparator +
      mvsRelease + attributeSeparator + driverName +
      attributeSeparator + inputFileName;
    File outputDir = getTempDirectory();
    if (!outputDir.exists())
      if (!outputDir.mkdir())
        throw new B390CommandException(
              "Unable to make temporary directory " + outputDir );
    return new File(outputDir, outputFileName);
  }

  /**
    * Returns java.io.File object that represents temp directory.
	 * This function guarantees never to return a null or empty value.
    *
    * @return File object that represents the temp directory.
    */
  private static File getTempDirectory()
  {
    String temp = MvsProperties.workPath1;
    if (temp == null)
      temp = "";
    else
      temp = temp.trim();

    if (temp.length() > 0)
      return (new File( temp ));
    else
      return (new File( DEFAULT_TEMPDIR ));
  }

  //=================Service Transfer========================
   /**
    * initB390DB - create PDT
    */
   public static void initB390DB( String  pdtName )
      throws B390CommandException, IOException, InterruptedException
   {
      String command = "INITPDTDB";
      String args, temparg;
      args = " CPDT=" + pdtName;
      temparg = MvsProperties.pdtStgcls;
      if ( MvsProperties.pdtStgcls != null )
         args += " STGCLS=" + MvsProperties.pdtStgcls;
      temparg = MvsProperties.pdtMgtcls;
      if ( temparg != null ) args += " MGTCLS=" + temparg;
      temparg = MvsProperties.pdtUbkp;
      if ( temparg != null ) args += " UBKP=" + temparg;
      temparg = getParmFile();
      if ( temparg != null) args += " PARMFILE=" + temparg;
      command = getB390ExecName() + " " + command + args;
      if (isB390Debug) command += " " + b390DebugFlag;
      defaultExecHandler.executeCommand( command );
   }

   /**
    * createCopySentDriver
    */
   public static void createCopySentDriver(
                    String mvsRelease, String highLevelQualifier,
                    String component, String pdt,
                    String primaryDataSetSize, String secondaryDataSetSize,
                    String primaryUnibankSize, String maxCylinderSize,
                    String maxExtendSize, String volid,
                    String stgcls, String mgtcls, int shippedFilesCount )
      throws B390CommandException, IOException, InterruptedException
   {
      String command = "CREATEDRIVER";
      String args = "";
      if (highLevelQualifier != null) args += " CHILVL=" + highLevelQualifier;
      if (mvsRelease != null) args += " MVSRELEASE=" + mvsRelease;
      args += " NEWDRIVER=COPYSENT";
      if (component != null) args += " HLCOMP=" + component.substring(0, 4);
      if (pdt != null) args += " CPDT=" + pdt;

      // use NUMPARTS instead of the following three attributes if any
      // of them are not specified
      if ((primaryDataSetSize == null) || (secondaryDataSetSize == null) ||
          (primaryUnibankSize == null))
      {
        args += " NUMPARTS=" + shippedFilesCount;
      }
      else
      {
        args += " CBLKP=" + primaryDataSetSize;
        args += " CBLKS=" + secondaryDataSetSize;
        args += " CUBKP=" + primaryUnibankSize;
      }

      if (maxCylinderSize != null)  args += " CMAXCYL=" + maxCylinderSize;
      if (maxExtendSize != null) args += " CMAXEXT=" + maxExtendSize;

      String workPath = getTempDirectory().toString();
      if (!workPath.equals( DEFAULT_TEMPDIR ))
         args += " WORKPATH1=" + workPath;

      if (volid != null)
         args += " CVOLID=" + volid;
      else
      {
         if (stgcls != null) args += " CSTGCLS=" + stgcls;
         if (mgtcls != null) args += " CMGTCLS=" + mgtcls;
      }
      args += " " + odeFlag;
      if (getParmFile() != null) args += " PARMFILE=" + getParmFile();
      command = getB390ExecName() + " " + command + args;
      if (isB390Debug) command += " " + b390DebugFlag;
      defaultExecHandler.executeCommand( command );
   }

   /**
    * DEFVER
    */
   public static void defineVersion( String component,
                    String retainRelease, String pdt, String mvsRelease,
                    String changeTeam, String copyright,
                    String base, String systemRelease, String cmvcFamily,
                    String cmvcFamilyAddr, String systemName, String featureInfo )
      throws B390CommandException, IOException, InterruptedException
   {
      String args = "";
      String command = "DEFVER";
      args += " COMPID=" + component;
      args += " RETREL=" + retainRelease;
      args += " PDT=" + pdt;
      args += " VERSION=" + mvsRelease;
      args += " LIBRELEASE=" + mvsRelease;
      args += " SREL=" + systemRelease;
      args += " LIBFAMILY=" + cmvcFamily;
      args += " LIBFAMADR=" + cmvcFamilyAddr;
      if (changeTeam != null) args += " TEAM=" + changeTeam.trim();

      // Copyright should default to the current year
      // if (copyright!= null ) args += " COPYRT=" +copyright;
      if (base != null && !base.equalsIgnoreCase(mvsRelease))
      {
         args += " BASE=" + base;
         args += " VFB1=" + base;
      }
      else
      {
        args += gatherFeatures(featureInfo);
      }

      /* Hard-code these options for now*/
      args += " FSUPER=N PREFIX=A ADDSUP=OFF PRICED=ON SITE=B25";
      args += " SYS=" + systemName;
      args += " " + odeFlag;
      if (getParmFile() != null) args += " PARMFILE=" + getParmFile();
      command = getB390ExecName() + " " + command + args;
      if (isB390Debug) command += " " + b390DebugFlag;
      defaultExecHandler.executeCommand( command );
   }

  // PTF commands
  /**
    * definePtf
    */
  public static void definePTF( B390PTFDriver driver, String[] apars,
                                String supersede )
    throws B390CommandException, IOException, InterruptedException
  {
    String aparargs = "";
    for (int idx = 0; idx < apars.length; idx++)
    {
      int val = idx + 1;
      aparargs += " APAR" + val + "=" + apars[idx];
    }

    String args = "";
    String command = "PTFDEFINE";
    args += aparargs;
    if (supersede != null) args += " SUPERCEDE1=" + supersede;
    args += " " + odeFlag;
    if (getParmFile() != null) args += " PARMFILE=" + getParmFile();
    command = getB390ExecName() + " " + command + args;
    if (isB390Debug) command += " " + b390DebugFlag;
    driver.executeCommand( command );
  }

  /**
   * createPtf
   */
  public static void createPTF( B390PTFDriver ptfDriver )
    throws B390CommandException, IOException, InterruptedException
  {
    String command = "PTFBUILD";
    StringBuffer args = new StringBuffer("");
    args.append(" BUILDID=").append(ptfDriver.getBuildId());
    String shipcode = ptfDriver.getShipCode();
    if (shipcode != null && shipcode.length() != 0)
    {
      args.append(" SHIPCODE=").append(shipcode);
    }
    if (getParmFile() != null)
    {
      args.append(" PARMFILE=").append(getParmFile());
    }
    args.append(" ").append(odeFlag);
    command = getB390ExecName() + " " + command + args.toString();
    if (isB390Debug) command += " " + b390DebugFlag;
    ptfDriver.executeCommand(command);
    args = null;
  }


  /**
   * This method returns FPI parameters for DEFVER command
   *
   * @param inputString a space separated string of feature FMIDs
   *                    example:  JBPS112 JBPS113
   * @return a String of form "FPI1=xxx .. .. FPIn=yyyy"
   *         example:  FPI1=JBPS112 FPI2=JBPS113
   */
  public static String gatherFeatures( String inputString )
  {
    StringTokenizer wordTokens = new StringTokenizer( inputString );
    int wordCnt = 1;
    String argString = "";

    while (wordTokens.hasMoreTokens())
    {
      String newWord = (String) wordTokens.nextToken();
      argString += " FPI" + wordCnt + "=" + newWord;
      wordCnt++;
    }
    return argString;
  }
}
