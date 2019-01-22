/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.3
 *
 * Date and Time File was last checked in: 8/28/03 17:29:26
 * Date and Time File was extracted/checked out: 04/10/04 09:13:22
 ****************************************************************************/
package com.ibm.ode.pkg.pkgMvs;

/**
 * Class to maintain the values of environment variables set as properties
 * on java -D flags.
 */
public class MvsProperties
{
  public static final String REQ_ENV_MSG =
    "The following required environment variables have not been set:";

  // Initialize all the properties.
  public static final String pkgControlDir  = getProperty("PKG_CONTROL_DIR");
  public static final String pkgOutputDir   = getProperty("PKG_OUTPUT_DIR");
  public static final String pkgTempDir     = getProperty("PKG_TEMP_DIR");
  public static final String toStage        = getProperty("TOSTAGE");

  // New MVS variables for bld and pkg (formerly PKG_MVS_*)   
  public static final String jobcardFile    = getProperty("MVS_JOBCARD");
  public static final String userid         = getProperty("MVS_USERID");
  public static final String password       = getProperty("MVS_PASSWORD");
  public static final String mvsExecDsn     = getProperty("MVS_EXEC_DATASET");
  public static final String jobMonitorTime = getProperty("MVS_JOBMONITOR_TIME");
  public static final String terseLoadDsn   = getProperty("MVS_TERSE_LOAD_DATASET");
  public static final String terseClistDsn  = getProperty("MVS_TERSE_CLIST_DATASET");
  public static final String funccntlDsn    = getProperty("MVS_FUNCCNTL_DATASET");
  public static final String subuildLoadlib = getProperty("MVS_RTG_LOADLIB");
  public static final String rtgAllocInfo   = getProperty("MVS_RTG_ALLOC_INFO");
  public static final String vsamVolumeInfo = getProperty("MVS_VSAM_VOLUME_INFO");
  public static final String displayOutput  = getProperty("MVS_DISPLAY_OUTPUT");  
  public static final String deleteOutput   = getProperty("MVS_DELETE_OUTPUT");
  public static final String saveOutputFile = getProperty("MVS_SAVE_OUTPUT_FILE");

  public static final String pkgClass       = getProperty("PKG_CLASS");
  public static final String pkgEvent       = getProperty("PKG_EVENT");
  public static final String pkgApiUrl      = getProperty("PKG_API_URL");
  public static final String cmvcLevel      = getProperty("CMVC_LEVEL");
  public static final String cmvcRelease    = getProperty("CMVC_RELEASE");
  public static final String cmvcFamily     = getProperty("CMVC_FAMILY");
  public static final String cmvcFamilyAddr = getProperty("CMVC_FAMILY_ADDR");
  public static final String supersedePTFs  = getProperty("SUPERSEDE_PTFS");
  public static final String apar           = getProperty("APAR");
  public static final String usermod        = getProperty("USERMOD");
  public static final String B390ParmFile   = getProperty("B390_PARM_FILE");
  public static final String B390Path       = getProperty("B390_PATH");
  public static final String B390Trace      = getProperty("B390_TRACE_ONLY");
  public static final String B390Debug      = getProperty("B390_DEBUG_ON");
  public static final String initPdt        = getProperty("B390_INIT_PDT");
  public static final String pdtName        = getProperty("B390_PDT");
  public static final String pdtStgcls      = getProperty("B390_PDT_STGCLS");
  public static final String pdtMgtcls      = getProperty("B390_PDT_MGTCLS");
  public static final String pdtUbkp        = getProperty("B390_PDT_UBKP");
  public static final String relBlkp        = getProperty("B390_REL_BLKP");
  public static final String relBlks        = getProperty("B390_REL_BLKS");
  public static final String relUbkp        = getProperty("B390_REL_UBKP");
  public static final String relMaxext      = getProperty("B390_REL_MAXEXT");
  public static final String relMaxcyl      = getProperty("B390_REL_MAXCYL");
  public static final String relCollectors  = getProperty("B390_REL_COLLECTORS");
  public static final String relSteps       = getProperty("B390_REL_STEPS");
  public static final String relVolid       = getProperty("B390_REL_VOLID");
  public static final String relStgcls      = getProperty("B390_REL_STGCLS");
  public static final String relMgtcls      = getProperty("B390_REL_MGTCLS");
  public static final String drvBlkp        = getProperty("B390_DRV_BLKP");
  public static final String drvBlks        = getProperty("B390_DRV_BLKS");
  public static final String drvUbkp        = getProperty("B390_DRV_UBKP");
  public static final String drvMaxext      = getProperty("B390_DRV_MAXEXT");
  public static final String drvMaxcyl      = getProperty("B390_DRV_MAXCYL");
  public static final String drvVolid       = getProperty("B390_DRV_VOLID");
  public static final String drvStgcls      = getProperty("B390_DRV_STGCLS");
  public static final String drvMgtcls      = getProperty("B390_DRV_MGTCLS");
  public static final String runScan        = getProperty("B390_RUNSCAN");
  public static final String shipTo         = getProperty("B390_SHIPTO");
  public static final String shipType       = getProperty("B390_SHIPTYPE");
  public static final String pkgType        = getProperty("PKG_TYPE");
  public static final String newFiles       = getProperty("B390_NEW_FILES");
  public static final String deletedFiles   = getProperty("B390_DELETED_FILES");
  public static final String ptfShipCode    = getProperty("PTF_SHIPCODE");
  public static final String retrieveApar   = getProperty("B390_RETRIEVE_APAR");
  public static final String retrievePtf    = getProperty("B390_RETRIEVE_PTF");
  public static final String rebuildApar    = getProperty("B390_APAR_REBUILD");
  public static final String deleteDriver   = getProperty("B390_AUTO_DELETE_DRIVER");
  public static final String workPath1      = getProperty("B390_WORKPATH1");
  
  // The following are used by the runpkgtool_all when PKG_TYPE is retrieveApar/retrievePtf
  public static final String copyTo = getProperty("B390_COPYTO");
  public static final String function = getProperty("B390_FUNCTION");
  public static final String shipAparName = getProperty("B390_++APAR_NAME");


  // The following are used during TSO build process
  public static final String bldHLQ         = getProperty("HLQ");  
  public static final String objectDir      = getProperty("TGTPATH");  
  public static final String srcBaseDir     = getProperty("SRCBASE");    
  public static final String sBox           = getProperty("SBOX");
  public static final String srcPath        = getProperty("SRCPATH");
  public static final String srcFull        = getProperty("SRCFULL");  
  public static final String cFlags         = getProperty("CFLAGS");
  public static final String numOfFiles     = getProperty("BLD_NUM_OF_FILES");
  public static final String exportPath     = getProperty("EXPPATH");  
  public static final String spaceType      = getProperty("BLD_SPACE_TYPE");  
  public static final String primaryQty     = getProperty("BLD_PRIMARY_QTY");
  public static final String secondaryQty   = getProperty("BLD_SECONDARY_QTY");
  public static final String directoryBlks  = getProperty("BLD_DIRECTORY_BLKS");
  public static final String blockSize      = getProperty("BLD_BLK_SIZE");
  public static final String lstSpaceType   = getProperty("BLD_LST_SPACE_TYPE");  
  public static final String lstPrimaryQty  = getProperty("BLD_LST_PRIMARY_QTY");
  public static final String lstSecondQty   = getProperty("BLD_LST_SECONDARY_QTY");
  public static final String lstDirBlks     = getProperty("BLD_LST_DIR_BLKS");
  public static final String lstBlockSize   = getProperty("BLD_LST_BLK_SIZE");  
  public static final String remoteHost     = getProperty("BLD_REMOTEHOST");
  public static final String netrcFile      = getProperty("NETRC_FILE");
  public static final String asmProgram     = getProperty("BLD_ASMPGM");
  public static final String cblProgram     = getProperty("BLD_CBLPGM");  
  public static final String plsProgram     = getProperty("BLD_PLSPGM");
  public static final String plxProgram     = getProperty("BLD_PLXPGM");
  public static final String region         = getProperty("BLD_REGION");  
  public static final String asmFlags       = getProperty("ASMFLAGS");
  public static final String cblFlags       = getProperty("CBLFLAGS");  
  public static final String plsFlags       = getProperty("PLSFLAGS");
  public static final String plxFlags       = getProperty("PLXFLAGS");
  public static final String asmStepLib     = getProperty("ASM_STEPLIB");
  public static final String cblStepLib     = getProperty("CBL_STEPLIB");  
  public static final String plsStepLib     = getProperty("PLS_STEPLIB");
  public static final String plxStepLib     = getProperty("PLX_STEPLIB");
  public static final String prodAsmSysLib  = getProperty("PROD_ASM_SYSLIB");
  public static final String prodCblSysLib  = getProperty("PROD_CBL_SYSLIB");
  public static final String prodPlsSysLib  = getProperty("PROD_PLS_SYSLIB");  
  public static final String prodPlxSysLib  = getProperty("PROD_PLX_SYSLIB");
  public static final String stndSysLib     = getProperty("STANDARD_SYSLIB");
  public static final String useRexxScript  = getProperty("BLD_WITH_REXXEXEC");
  public static final String useOdeTemplates= getProperty("USE_ODE_TEMPLATES");
  public static final String useMyTemplates = getProperty("USE_MY_TEMPLATES");
  public static final String languages2use  = getProperty("LANGS_FOR_MY_TEMPLATES");

  /**
   * This represents a list of parts that need an entry in the JCLIN. The
   * parts are delimited by a ',' or a space. This variable is only used in
   * the Service Packaging.
   */
  public static final String jclinFiles = System.getProperty("B390_JCLIN_FILES");

  public static String NL = System.getProperty("line.separator");
  public static String fileSeparator = System.getProperty("file.separator");

  /**
   * Return a string containing all the properties and their values.
   */
  public static String ToString()
  {
    // This method is called ToString and not toString because
    // Object.toString() can't be overridden with a static method
    StringBuffer sb = new StringBuffer(600);

    sb.append("pkgControlDir=")  .append(pkgControlDir)  .append(NL);
    sb.append("pkgOutputDir=")   .append(pkgOutputDir)   .append(NL);
    sb.append("pkgTempDir=")     .append(pkgTempDir)     .append(NL);
    sb.append("toStage=")        .append(toStage)        .append(NL);
    sb.append("mvsJobcardFile=") .append(jobcardFile)    .append(NL);
    sb.append("userid=")         .append(userid)         .append(NL);
    sb.append("password=")       .append(password)       .append(NL);
    sb.append("mvsExecDsn=")     .append(mvsExecDsn)     .append(NL);
    sb.append("jobMonitorTime=") .append(jobMonitorTime) .append(NL);
    sb.append("terseLoadDsn=")   .append(terseLoadDsn)   .append(NL);
    sb.append("terseClistDsn=")  .append(terseClistDsn)  .append(NL);
    sb.append("funccntlDsn=")    .append(funccntlDsn)    .append(NL);
    sb.append("subuildLoadlib=") .append(subuildLoadlib) .append(NL);
    sb.append("rtgAllocInfo=")   .append(rtgAllocInfo)   .append(NL);
    sb.append("vsamVolumeInfo=") .append(vsamVolumeInfo) .append(NL);
    sb.append("pkgClass=")       .append(pkgClass)       .append(NL);
    sb.append("pkgEvent=")       .append(pkgEvent)       .append(NL);
    sb.append("deleteOutput=")   .append(deleteOutput)   .append(NL);
    sb.append("displayOutput=")  .append(displayOutput)  .append(NL);
    sb.append("saveOutputFile=") .append(saveOutputFile) .append(NL);
    sb.append("pkgApiUrl=")      .append(pkgApiUrl)      .append(NL);
    sb.append("cmvcLevel=")      .append(cmvcLevel)      .append(NL);
    sb.append("cmvcRelease=")    .append(cmvcRelease)    .append(NL);
    sb.append("cmvcFamily=")     .append(cmvcFamily)     .append(NL);
    sb.append("supersedePTFs=")  .append(supersedePTFs)  .append(NL);
    sb.append("apar=")           .append(apar)           .append(NL);
    sb.append("usermod=")        .append(usermod)        .append(NL);
    sb.append("B390_PDT=")       .append(pdtName)        .append(NL);
    sb.append("pkgType=")        .append(pkgType)        .append(NL);
    // used by the TSO build process
    sb.append("bldHLQ=")         .append(bldHLQ)         .append(NL);
    sb.append("objectDir=")      .append(objectDir)      .append(NL);
    sb.append("srcBaseDir=")     .append(srcBaseDir)     .append(NL);    
    sb.append("sBox=")           .append(sBox)           .append(NL);
    sb.append("srcPath=")        .append(srcPath)        .append(NL);
    sb.append("srcFull=")        .append(srcFull)        .append(NL);    
    sb.append("cFlags=")         .append(cFlags)         .append(NL);
    sb.append("numOfFiles=")     .append(numOfFiles)     .append(NL);
    sb.append("exportPath=")     .append(exportPath)     .append(NL);    
    sb.append("spaceType=")      .append(spaceType)      .append(NL);       
    sb.append("primaryQty=")     .append(primaryQty)     .append(NL);    
    sb.append("secondaryQty=")   .append(secondaryQty)   .append(NL);
    sb.append("directoryBlks=")  .append(directoryBlks)  .append(NL);
    sb.append("blockSize=")      .append(blockSize)      .append(NL);
    sb.append("lstSpaceType=")   .append(lstSpaceType)   .append(NL);       
    sb.append("lstPrimaryQty=")  .append(lstPrimaryQty)  .append(NL);    
    sb.append("lstSecondQty=")   .append(lstSecondQty)   .append(NL);
    sb.append("lstDirBlks=")     .append(lstDirBlks)     .append(NL);
    sb.append("lstBlockSize=")   .append(lstBlockSize)   .append(NL);    
    sb.append("remoteHost=")     .append(remoteHost)     .append(NL);
    sb.append("netrcFile=")      .append(netrcFile)      .append(NL);    
    sb.append("asmProgram=")     .append(asmProgram)     .append(NL);
    sb.append("cblProgram=")     .append(cblProgram)     .append(NL);    
    sb.append("plsProgram=")     .append(plsProgram)     .append(NL);
    sb.append("plxProgram=")     .append(plxProgram)     .append(NL);
    sb.append("region=")         .append(region)         .append(NL);
    sb.append("asmFlags=")       .append(asmFlags)       .append(NL);
    sb.append("cblFlags=")       .append(cblFlags)       .append(NL);
    sb.append("plsFlags=")       .append(plsFlags)       .append(NL);
    sb.append("plxFlags=")       .append(plxFlags)       .append(NL);
    sb.append("asmStepLib=")     .append(asmStepLib)     .append(NL);
    sb.append("cblStepLib=")     .append(cblStepLib)     .append(NL);
    sb.append("plsStepLib=")     .append(plsStepLib)     .append(NL);
    sb.append("plxStepLib=")     .append(plxStepLib)     .append(NL);    
    sb.append("prodAsmSysLib=")  .append(prodAsmSysLib)  .append(NL);
    sb.append("prodCblSysLib=")  .append(prodCblSysLib)  .append(NL);    
    sb.append("prodPlsSysLib=")  .append(prodPlsSysLib)  .append(NL);
    sb.append("prodPlxSysLib=")  .append(prodPlxSysLib)  .append(NL);    
    sb.append("stndSysLib=")     .append(stndSysLib)     .append(NL);
    sb.append("useRexxScript=")  .append(useRexxScript)  .append(NL);
    sb.append("useOdeTemplates=").append(useOdeTemplates).append(NL);
    sb.append("useMyTemplates=") .append(useMyTemplates) .append(NL);
    sb.append("languages2use=")  .append(languages2use)  .append(NL);

    return sb.toString();
  }

  /**
   * Make sure all required properties are set for the given event/class.
   * Prints a message if any of the required properties are not set before
   * returning false.
   *
   * @return true if all required properties were set else returns false
   */
  public static boolean checkValues()
  {
    StringBuffer notSet = new StringBuffer();

    if (pkgControlDir == null)
      notSet.append("  PKG_CONTROL_DIR").append(NL);
    if (pkgClass == null)
      notSet.append("  PKG_CLASS").append(NL);

    if (pkgClass.equalsIgnoreCase("ipp") &&
        (pkgEvent.equalsIgnoreCase("gather") ||
         pkgEvent.equalsIgnoreCase("package")))
    {
      if (jobcardFile == null)
        notSet.append("  MVS_JOBCARD").append(NL);
      if (userid == null)
        notSet.append("  MVS_USERID").append(NL);
      if (password == null)
        notSet.append("  MVS_PASSWORD").append(NL);
      if (mvsExecDsn == null)
        notSet.append("  MVS_EXEC_DATASET").append(NL);
      if (pkgOutputDir == null)
        notSet.append("  PKG_OUTPUT_DIR").append(NL);
      if (pkgTempDir == null)
        notSet.append("  PKG_TEMP_DIR").append(NL);
       if (toStage == null)
         notSet.append("  TOSTAGE").append(NL);
    }

    if (pkgEvent.equalsIgnoreCase("package"))
    {
      if (pkgClass.equalsIgnoreCase("sp"))
      {
        if (pkgType.equalsIgnoreCase("ptf") ||
            pkgType.equalsIgnoreCase("++ptf") ||
            pkgType.equalsIgnoreCase("ptfvpl") ||
            pkgType.equalsIgnoreCase("retrievePtf"))
        {
          if (cmvcLevel == null || cmvcLevel.length() == 0)
            notSet.append("  CMVC_LEVEL").append(NL);
          if (cmvcRelease == null)
            notSet.append("  CMVC_RELEASE").append(NL);
          if (cmvcFamily == null)
            notSet.append("  CMVC_FAMILY").append(NL);
          if (pkgApiUrl == null)
            notSet.append("  PKG_API_URL").append(NL);
        }

        if (pkgType.equalsIgnoreCase("retrievePtf") ||
            pkgType.equalsIgnoreCase("retrieveApar") ||
            pkgType.equalsIgnoreCase("retrieveUsermod"))
        {
          if (MvsProperties.function == null)
            notSet.append("  B390_FUNCTION").append(NL);
        }

        if (pkgType.equalsIgnoreCase("retrieveApar") ||
            pkgType.equalsIgnoreCase("retrieveUsermod"))
        {
          if (MvsProperties.shipAparName == null)
            notSet.append("  B390_++APAR_NAME").append(NL);
          if (MvsProperties.apar == null)
            notSet.append("  APAR").append(NL);
        }
        else if (pkgType.equalsIgnoreCase("++APAR") ||
                 pkgType.equalsIgnoreCase("APAR"))
        {
          if (MvsProperties.apar == null)
            notSet.append("  APAR").append(NL);
        }
        else if (pkgType.equalsIgnoreCase("USERMOD") ||
                 pkgType.equalsIgnoreCase("++USERMOD"))
        {
          if (MvsProperties.usermod == null)
            notSet.append("  USERMOD").append(NL);
        }
        else if (pkgType.equalsIgnoreCase("ptfvpl"))
        {
          if (MvsProperties.terseLoadDsn == null &&
              MvsProperties.terseClistDsn == null)
            notSet.append("  MVS_TERSE_CLIST_DATASET/MVS_TERSE_LOAD_DATASET").append(NL);
          if (toStage == null)
            notSet.append("  TOSTAGE").append(NL);
          if (jobcardFile == null)
            notSet.append("  MVS_JOBCARD").append(NL);
          if (userid == null)
            notSet.append("  MVS_USERID").append(NL);
          if (password == null)
            notSet.append("  MVS_PASSWORD").append(NL);
          if (mvsExecDsn == null)
            notSet.append("  MVS_EXEC_DATASET").append(NL);
        }
      }
      else if (pkgClass.equalsIgnoreCase("st"))
      {
        if (pdtName == null)
          notSet.append("  B390_PDT").append(NL);
      }
      else if (pkgClass.equalsIgnoreCase("ipp"))
      {
        if (MvsProperties.funccntlDsn == null)
          notSet.append("  MVS_FUNCCNTL_DATASET").append(NL);
        if (MvsProperties.subuildLoadlib == null)
          notSet.append("  MVS_RTG_LOADLIB").append(NL);
      }
    }
    else if (pkgEvent.equalsIgnoreCase("getptfnums"))
    {
      if (cmvcLevel == null || cmvcLevel.length() == 0)
        notSet.append("  CMVC_LEVEL").append(NL);
      if (cmvcRelease == null)
        notSet.append("  CMVC_RELEASE").append(NL);
      if (cmvcFamily == null)
        notSet.append("  CMVC_FAMILY").append(NL);
      if (pkgApiUrl == null)
        notSet.append("  PKG_API_URL").append(NL);
    }

    if (notSet.length() == 0)
      return true;
    else
    {
      System.out.println(MvsProperties.REQ_ENV_MSG);
      System.out.println(notSet);
      return false;
    }
  }

  /**
   * Get the value of the given property via System.getProperty().  Empty
   * string values will be returned as null values.  Perform any other
   * needed operations on the value before returning it (adding path
   * separator, conversion to uppercase).
   */
  private static String getProperty( String property )
  {
    String value = System.getProperty(property);

    // MVS_DELETE_OUTPUT defaults to "YES" if not set to "NO"
    if (property.equals("MVS_DELETE_OUTPUT") ||
        property.equals("MVS_DISPLAY_OUTPUT") || 
        property.equals("B390_AUTO_DELETE_DRIVER") )
    {
      if (value != null)
      {
        value = value.toUpperCase();
        if ((value.equals("NO")) || (value.equals("N")))
          value = "NO";
        else
          value = "YES";
      }
      else
        value = "YES";
    }
    else if (property.equals("MVS_SAVE_OUTPUT_FILE") ||
             property.equals("BLD_WITH_REXXEXEC") )
    {
      if (value != null)
      {
        value = value.toUpperCase();
        if (value.equals("YES") || value.equals("Y"))
          value = "YES";
        else
          value = "NO";
      }
      else
        value = "NO";
    }

    // make empty values null
    if (value == null ||
        (value != null && value.length() == 0))
    {
      return null;
    }

    // make sure directory names end in path separator
    if ( property.equals("PKG_CONTROL_DIR") ||
         property.equals("PKG_OUTPUT_DIR")  ||
         property.equals("PKG_TEMP_DIR")    ||
         property.equals("TOSTAGE")||
         property.equals("SRCBASE")||
         property.equals("TGTPATH") )
    {
      char fileSeparator = System.getProperty("file.separator").charAt(0);
      if (value.charAt(value.length() - 1) != fileSeparator)
        value = value + fileSeparator;
    }

    // uppercase propeties where needed....
    else if ( property.equals("MVS_USERID")         ||
              property.equals("MVS_EXEC_DATASET")     ||
              property.equals("MVS_TERSE_LOAD_DATASET")  ||
              property.equals("MVS_TERSE_CLIST_DATASET") ||
              property.equals("MVS_FUNCCNTL_DATASET")    ||
              property.equals("MVS_RTG_LOADLIB") ||
              property.equals("MVS_RTG_ALLOC_INFO")   ||
              property.equals("MVS_VSAM_VOLUME_INFO") ||
              property.equals("PKG_TYPE")   ||
              property.equals("USERMOD")   ||
              property.equals("B390_PDT") ||
              property.equals("PTF") ||
              property.equals("APAR") )
    {
      value = value.toUpperCase();
    }

    // uppercase properties for TSO Build process...
    else if ( property.equals("MVS_USERID") ||
              property.equals("MVS_EXEC_DATASET") ||
              property.equals("HLQ")   ||
              property.equals("SBOX")  ||
              property.equals("BLD_SPACE_TYPE") || 
              property.equals("BLD_LST_SPACE_TYPE") ||               
              property.equals("BLD_REMOTEHOST") ||
              property.equals("NETRC_FILE")     ||
              property.equals("BLD_ASMPGM")     ||
              property.equals("BLD_CBLPGM")     ||
              property.equals("BLD_PLSPGM")     ||
              property.equals("BLD_PLXPGM")     ||
              property.equals("BLD_REGION")     ||
              property.equals("ASMFLAGS")       ||
              property.equals("CBLFLAGS")       ||
              property.equals("PLSFLAGS")       ||
              property.equals("PLXFLAGS")       ||
              property.equals("ASM_STEPLIB")    ||
              property.equals("CBL_STEPLIB")    ||
              property.equals("PLS_STEPLIB")    ||              
              property.equals("PLX_STEPLIB")    ||
              property.equals("PROD_ASM_SYSLIB") ||
              property.equals("PROD_CBL_SYSLIB") ||              
              property.equals("PROD_PLS_SYSLIB") ||
              property.equals("PROD_PLX_SYSLIB") ||
              property.equals("STANDARD_SYSLIB") ) 
    {
       value = value.toUpperCase();
    }

    return value;
  }

  /**
   * Returns true if B390_RETRIEVE_APAR property is set and is set to
   * either YES or Y
   */
  public static boolean isRetrieveApar()
  {
    return booleanValue(MvsProperties.retrieveApar);
  }

  /**
   * Returns true if B390_RETRIEVE_PTF property is set and is set to
   * either YES or Y else false.
   */
  public static boolean isRetrievePtf()
  {
    return booleanValue(MvsProperties.retrievePtf);
  }

  /**
   * Returns false if MVS_DELETE_OUTPUT property is set and is set to
   * NO else returns true.
   */
  public static boolean isDeleteOutput()
  {
    return booleanValue(MvsProperties.deleteOutput);
  }

  /**
   * Returns false if MVS_DISPLAY_OUTPUT property is set and is set to
   * NO else returns true.
   */
  public static boolean isDisplayOutput()
  {
    return booleanValue(MvsProperties.displayOutput);
  }

  /**
   * Returns true if MVS_SAVE_OUTPUT_FILE property is set and is set to
   * YES else returns false.
   */
  public static boolean isSaveOutputFile()
  {
    return booleanValue(MvsProperties.saveOutputFile);
  }

  /**
   * Returns true if B390_TRACE_ONLY property is set and is set to
   * either YES or Y else returns false.
   */
  public static boolean isB390TraceOnly()
  {
    return booleanValue(MvsProperties.B390Trace);
  }

  /**
   * Returns true if B390_DEBUG_ON property is set and is set to
   * either YES or Y else returns false.
   */
  public static boolean isB390Debug()
  {
    return booleanValue(MvsProperties.B390Debug);
  }

  /**
   * Returns true if B390_AUTO_DELETE_DRIVER property is set and is set to
   * either NO or N else returns true.
   */
  public static boolean isDeleteDriver()
  {
    return booleanValue(MvsProperties.deleteDriver);
  }

  /**
   * Returns the boolean value of the specified string. Returns true if
   * the value is either YES/Y or yes/y. Returns false if the value is
   * either NO/N or no/n. If the values is something else returns false.
   *
   * @param val string containing either YES/Y, yes/y, NO/N, no/n
   */
  public static boolean booleanValue( String val )
  {
    boolean boolVal = false;
    if (val != null)
    {
      if (val.equalsIgnoreCase("YES") ||
          val.equalsIgnoreCase("Y"))
        boolVal = true;
      else if (val.equalsIgnoreCase("NO") ||
               val.equalsIgnoreCase("N"))
        boolVal = false;
    }
    return boolVal;
  }

  /**
   * Checks if MVS_JOBCARD, MVS_USERID and MVS_PASSWORD and
   * MVS_EXEC_DATASET properties, that are required for submitting
   * a job, are set. If any one of them is not set then an error
   * message is printed and false is returned. 
   *
   * @return true if all the required properties are set
   */
  public static boolean checkReqPropForSubmittingJob()
  {
    StringBuffer notSet = new StringBuffer();
    if (jobcardFile == null)
      notSet.append("  MVS_JOBCARD").append(NL);
    if (userid == null)
      notSet.append("  MVS_USERID").append(NL);
    if (password == null)
      notSet.append("  MVS_PASSWORD").append(NL);    
    if (mvsExecDsn == null)
      notSet.append("  MVS_EXEC_DATASET").append(NL);

    if (notSet.length() == 0)
    {
      return true;
    }
    else
    {
      System.out.println(MvsProperties.REQ_ENV_MSG);
      System.out.println(notSet);
      return false;
    }
  }

  /**
   * Checks if MVS_JOBCARD, MVS_USERID and MVS_PASSWORD
   * properties are set, because they are required for
   * submitting a job. 
   * Checks if REMOTEHOST and NETRC_FILE properties are
   * set, because they are required for the FTP job step  
   * If any one of them is not set then an error message is
   * printed and false is returned. If BLD_WITH_REXXEXEC is   
   * null or no, MVS_EXEC_DATASET will not be needed.   
   *
   * @return true if all the required properties are set
   */
  public static boolean checkReqPropForSubmittingJobAndFTP()
  {
    StringBuffer notSet = new StringBuffer();
    
    if (jobcardFile == null)
      notSet.append("  MVS_JOBCARD").append(NL);
    if (userid == null)
      notSet.append("  MVS_USERID").append(NL);
    if (password == null)
      notSet.append("  MVS_PASSWORD").append(NL);
    
    if (useRexxScript.equalsIgnoreCase("YES"))
    {
      if (mvsExecDsn == null)
        notSet.append("  MVS_EXEC_DATASET").append(NL);
    }

    if (remoteHost == null)
      notSet.append("  REMOTEHOST").append(NL);

    if (notSet.length() == 0)
    {
      return true;
    }
    else
    {
      System.out.println(MvsProperties.REQ_ENV_MSG);
      System.out.println(notSet);
      return false;
    }
  }

  /**
   * Verify that the PGM, CFLAGS, STEPLIB and STANDARD_SYSLIB
   * properties are set for each language because they are 
   * required. If any one of them is not set then an error message
   * is printed and false is returned.
   *
   * @return true if all the required properties are set
   */
  public static boolean checkLanguageProperties(String lang )
  {
    StringBuffer notSet = new StringBuffer();
    
    if (lang.equalsIgnoreCase("ASM"))
    {
      if (asmProgram == null)
        notSet.append("  ASMPGM").append(NL);
      if (asmFlags == null)
        notSet.append("  ASMCFLAGS").append(NL);
      if (asmStepLib == null)
        notSet.append("  ASM_STEBLIB").append(NL);
    }
    if (lang.equalsIgnoreCase("COBOL"))
    {
      if (cblProgram == null)
        notSet.append("  CBLPGM").append(NL);
      if (cblFlags == null)
        notSet.append("  CBLCFLAGS").append(NL);
      if (cblStepLib == null)
        notSet.append("  CBL_STEBLIB").append(NL);
    }
    else if (lang.equalsIgnoreCase("PLS"))
    {
      if (plsProgram == null)
        notSet.append("  PLSPGM").append(NL);
      if (plsFlags == null)
        notSet.append("  PLSCFLAGS").append(NL);
      if (plsStepLib == null)
        notSet.append("  PLS_STEPLIB").append(NL);    
    }
    else if (lang.equalsIgnoreCase("PLX"))
    {
      if (plxProgram == null)
        notSet.append("  PLXPGM").append(NL);
      if (plxFlags == null)
        notSet.append("  PLXCFLAGS").append(NL);
      if (plxStepLib == null)
        notSet.append("  PLX_STEPLIB").append(NL);
    }
    
    if (stndSysLib == null)
      notSet.append("  STANDARD_SYSLIB").append(NL);

    if (notSet.length() == 0)
    {
      return true;
    }
    else
    {
      System.out.println(MvsProperties.REQ_ENV_MSG);
      System.out.println(notSet);
      return false;
    }
  }

}
