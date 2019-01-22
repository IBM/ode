/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.2
 *
 * Date and Time File was last checked in: 8/28/03 17:29:28
 * Date and Time File was extracted/checked out: 06/04/13 16:58:07
 *******************************************************************************/
package com.ibm.ode.bin.bldMvs;

import java.io.*;
import java.util.*;
import com.ibm.ode.pkg.pkgMvs.*;

/** 
 * Main driver for the TSO Build process.
 */
public class RunMvsBld 
{
  public static void main(String[] args) throws FileNotFoundException, IOException
  {
    String buildControlFile = null;

    try
    {
      Vector asmFiles  = new Vector();     
      Vector cblFiles  = new Vector(); 
      Vector plsFiles  = new Vector();     
      Vector plxFiles  = new Vector();            
      Vector srcFiles  = new Vector();     
      
      Vector srcPathNames   = new Vector();
      Vector localMacs  = new Vector();
      Vector exportMacs = new Vector();
      
      // Check if MVS_JOBCARD, MVS_USERID, MVS_PASSWORD and
      // MVS_EXEC_DATASET and REMOTEHOST properties are set
      if (!MvsProperties.checkReqPropForSubmittingJobAndFTP())
      {
        System.out.println();
        System.exit(1);
      }

      int lastDot = MvsProperties.remoteHost.lastIndexOf(".");

      // IP Address is not allowed, must be RemoteHost Name
      if ( Character.isDigit(MvsProperties.remoteHost.charAt(lastDot + 1)) )
      {
         System.out.println("-----------------------------------------");
         System.out.println("Hostname for " + MvsProperties.remoteHost + 
                            " must be used instead of the IP address.");
         System.out.println("-----------------------------------------");
         System.exit(1);
      }

      String listDir = MvsProperties.objectDir;
      buildControlFile = listDir + "mvsbldlist.rsp";
  
      System.out.println();
      System.out.println("Build control file is " + buildControlFile);    
      System.out.println();
  
      BufferedReader reader = new BufferedReader(new FileReader(buildControlFile));
     
      String nextLine = null;
      String srcPart  = null;
      String asmType  = null;
      String cblType  = null;
      String plsType  = null;
      String plxType  = null;
      String langType = null;
      
      boolean asmFileFound = false;
      boolean cblFileFound = false;
      boolean plsFileFound = false;      
      boolean plxFileFound = false;
      boolean langNotSupported = false;
      boolean isFileTooLong = false;

      int langCnt    = 0;
      int numOfParts = 0;
      int asmCount   = 0;
      int cblCount   = 0;
      int plsCount   = 0;
      int plxCount   = 0;

      String localFile = null;
      String remoteFile = null;

      // read the mvsbldlist file
      while ((nextLine = reader.readLine()) != null)       
      { 
        if (!isFileTooLong) 
        {
          // check the length of line
          if (nextLine.length() > 70)
          {
            isFileTooLong = true;
          }
        }

        if (nextLine.length() == 0)   // skip blank line
        {
          continue;
        }   
        else if (nextLine.endsWith(".mac") || nextLine.endsWith(".MAC")
                 || nextLine.endsWith(".Mac"))
        {
          String dirsep = System.getProperty("file.separator");
          String exportDir = dirsep + "export" + dirsep;

          int indxOfExportDir = nextLine.indexOf(exportDir);
            
          if (indxOfExportDir >= 0)
             exportMacs.addElement(nextLine);
          else
              localMacs.addElement(nextLine);   
        }
        else 
        {
          int lineLength = nextLine.length();             
          int indxOfDot = nextLine.lastIndexOf(".");

          String srcExt = nextLine.substring((indxOfDot + 1), lineLength);
                                      
          // check file extensions of source files 
          if (srcExt.equalsIgnoreCase( "asm" ))
            asmCount++;
          else if (srcExt.equalsIgnoreCase( "cbl" ) ||
                   srcExt.equalsIgnoreCase( "cob" ) ||
                   srcExt.equalsIgnoreCase( "cobol" ))
          {
            cblCount++;
          }
          else if (srcExt.equalsIgnoreCase( "pls" ))
            plsCount++;
          else if (srcExt.equalsIgnoreCase( "plx" ))
            plxCount++;
          else
          {
            System.out.println("*******************************");
            System.out.println("Language for file extension: " + 
                               srcExt.toUpperCase() + " not supported.");

            langNotSupported = true;
          }

          // check if the language is supported
          if (langNotSupported) 
          {
            System.out.println("The following file was skipped: ");  
            System.out.println(nextLine);
            System.out.println();
            langNotSupported = false;
          }
          else
          {
            srcPart = createSrcData(nextLine);   
            srcFiles.addElement(srcPart);   
            srcPathNames.addElement(nextLine);                   
            numOfParts++;
          }  // langNotSupported
        }   
      }  // end while
 
      // verify that one of the template variables is set to YES
      if ((MvsProperties.useOdeTemplates.equalsIgnoreCase("NO")) &&
          (MvsProperties.useMyTemplates.equalsIgnoreCase("NO")))
      {
        System.out.println("CURRENT SETTINGS FOR THE FOLLOWING VARIABLES:");
        System.out.println("USE_ODE_TEMPLATES = " + MvsProperties.useOdeTemplates);
        System.out.println("USE_MY_TEMPLATES  = " + MvsProperties.useMyTemplates);
        System.out.println("WHICH TEMPLATES DO YOU WANT TO UTILIZE?");
        System.exit(1);
      }

      // If ASM files found
      if (asmCount != 0)
      {
        asmFileFound = true;
        langType = "ASM";
      
        System.out.println("-----------------------------------------");
        System.out.println("*** " + asmCount + " ASM File(s) will be compiled ***");
     
        if (MvsProperties.useOdeTemplates.equalsIgnoreCase("YES"))
        {
          if ( MvsProperties.useMyTemplates.equalsIgnoreCase("NO") ||
               (MvsProperties.useMyTemplates.equalsIgnoreCase("YES") &&
                !isLanguageFound(langType)) )
          {
            // Make sure ASMPGM, ASMCFLAGS, AND ASM_STEPLIB properties are 
            // set because they are required for this language.
            if (!MvsProperties.checkLanguageProperties(langType))
            {
              System.exit(1);
            }  
            System.out.println("    using the ODE ASM template");            
          }
          else
             System.out.println("    using the USER's ASM template");            
        } // end useOdeTemplates
        else
        {
          System.out.println("    using the USER's ASM template");            
        }
      }

      // If COBOL files found
      if (cblCount != 0)
      {
        cblFileFound = true;
        langType = "COBOL";

        System.out.println("-----------------------------------------");
        System.out.println("*** " + cblCount + " COBOL File(s) will be compiled ***");
     
        if (MvsProperties.useOdeTemplates.equalsIgnoreCase("YES"))
        {
          if ( MvsProperties.useMyTemplates.equalsIgnoreCase("NO") ||
               (MvsProperties.useMyTemplates.equalsIgnoreCase("YES") &&
                !isLanguageFound(langType)) )
          {
            // Make sure CBLPGM, CBLCFLAGS, AND CBL_STEPLIB properties are 
            // set because they are required for this language.
            if (!MvsProperties.checkLanguageProperties(langType))
            {
              System.exit(1);
            }
            System.out.println("    using the ODE COBOL template");            
          }
          else
            System.out.println("    using the USER's COBOL template");            
        } // end useOdeTemplates
        else
        {
          System.out.println("    using the USER's COBOL template");            
        }
      }

      // If PLS files found
      if (plsCount != 0)
      {
        plsFileFound = true;
        langType = "PLS";
        
        System.out.println("-----------------------------------------");
        System.out.println("*** " + plsCount + " PLS File(s) will be compiled ***");


        if (MvsProperties.useOdeTemplates.equalsIgnoreCase("YES"))
        {
          if ( MvsProperties.useMyTemplates.equalsIgnoreCase("NO") ||
               (MvsProperties.useMyTemplates.equalsIgnoreCase("YES") &&
                !isLanguageFound(langType)) )
          {
            // Make sure PLSPGM, PLSCFLAGS, AND PLS_STEPLIB properties are 
            // set because they are required for this language.
            if (!MvsProperties.checkLanguageProperties(langType))
            {
              System.exit(1);
            }   
            System.out.println("    using the ODE PLS template");            
          }
          else
            System.out.println("    using the USER's PLS template");            
        } // end useOdeTemplates
        else
        {
          System.out.println("    using the USER's PLS template");            
        }
      }

      // If PLX files found
      if (plxCount != 0)
      {
        plxFileFound = true;
        langType = "PLX";

        System.out.println("-----------------------------------------");
        System.out.println("*** " + plxCount + " PLX File(s) will be compiled ***");         

        if (MvsProperties.useOdeTemplates.equalsIgnoreCase("YES"))
        {
          
          if ( MvsProperties.useMyTemplates.equalsIgnoreCase("NO") ||
               (MvsProperties.useMyTemplates.equalsIgnoreCase("YES") &&
                !isLanguageFound(langType)) )
          {
            // Make sure PLXPGM, PLXCFLAGS, AND PLX_STEPLIB properties are 
            // set because they are required for this language.
            if (!MvsProperties.checkLanguageProperties(langType))
            {
              System.exit(1);
            }  
            System.out.println("    using the ODE PLX template");             
          }
          else
            System.out.println("    using the USER's PLX template");            
        } // end useOdeTemplates
        else
        {
          System.out.println("    using the USER's PLX template");            
        }
      }

      // If no ASM, CBL, PLS, or PLX files found in interim file
      if ( (asmCount == 0 ) && (cblCount == 0) && 
           (plsCount == 0) &&  (plxCount == 0) ) 
      {
        System.out.println("The files are up_to_date.");
        System.exit(0);
      }

      nextLine = null;
   
      // store source files and macs from Interim file       
      String[] srcArray = vector2StringArray(srcFiles);
      String[] srcPathArray = vector2StringArray(srcPathNames);       
      String[] lmacArray = vector2StringArray(localMacs);
      String[] emacArray = vector2StringArray(exportMacs);
      
      int rc = 0;
    
      // Start TSO Build process....
      rc = doTSOBuild(srcArray, srcPathArray, lmacArray, emacArray,
                      numOfParts, asmFileFound, cblFileFound, 
                      plsFileFound, plxFileFound, listDir, isFileTooLong);

      if (rc == 0)
      {
        System.out.println("BUILD process....................completed successfully");
        System.out.println("-----------------------------------------");         
        System.out.println("Generating File Dependencies.....");

        generateFileDependency(srcArray, srcPathArray, lmacArray,
                               emacArray, numOfParts, listDir);
        
        if ((asmCount > 0) || (plsCount > 0) || (plxCount > 0)) 
          System.out.println("File Dependencies................done");
        System.out.println("-----------------------------------------");       
      }
      else
      { 
        System.out.println("BUILD process....................done");
        System.out.println("JOB(s)...........................failed");
        System.out.println("-----------------------------------------");         
        System.out.println("Checking File Dependencies.......");

        generateFileDependency(srcArray, srcPathArray, lmacArray,
                               emacArray, numOfParts, listDir);

        System.out.println("-----------------------------------------");         
      }
    }
    catch (FileNotFoundException ex)
    {                                                            
      System.err.println("Build Control File: " + buildControlFile + " not found.");
      System.err.println(ex);
      System.exit(-1);          
    }
    //this is placed to catch any exceptions uncaught else where, hence
    //it is made generic
    catch( Exception e )
    {
      System.out.println("Build Error - An exception occurred" );
      System.exit(1);
    }

  } // main ()

  /**
   * Read the build control file, call the methods to generate the jcl,
   * submit and monitor the job(s).  The job output is saved if requested. 
   *
   * @param srcName: Source names (test) 
   * @param srcPathName: Full source names (/u/userid/sandbox/src/plx/test.plx)
   * @param lmacName: Local macros (/u/userid/sandbox/src/plx/a.mac)
   * @param emacName: Export macros(/u/userid/sandbox/export/mvs390_oe_2/usr/incl390/b.mac)
   * @numParts: Total number of source files to compile 
   * @asmFound Found ASM parts: true or false
   * @plsFound Found PLS parts: true or false
   * @plxFound Found PLX parts: true or false
   * @bldPath Directory to store generated JCL files (/u/userid/sandbox/obj/mvs390_oe_2/plx)
   *
   * @exception I/O error occurs   
   * @return  int value to exit with
   */
  private static int doTSOBuild(String srcName[], String srcPathName[], String lmacName[],
                                String emacName[], int numParts, boolean asmFound,
                                boolean cblFound, boolean plsFound, boolean plxFound,
                                String bldPath, boolean isAnyFileTooLong)
                                throws IOException   
  {
    MvsJclGenerator jclGen = new MvsJclGenerator(null, 2);
    
    ArrayList jobArray = new ArrayList();
    int errorCode = 0;
    long waitTime;

    try
    {
      // JCL generation phase
      generateTSOBuildJcl(jobArray, srcName, srcPathName, lmacName, emacName,
                          numParts, asmFound, cblFound, plsFound, plxFound,
                          bldPath, isAnyFileTooLong);
     
      System.out.println("All JCL generation complete");
      System.out.println();

      // Job Management phase
      System.out.println("Entering job management phase");
      try
      {
        if (MvsProperties.jobMonitorTime == null)
          waitTime = 1;
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

      if (RunMvsPkg.executeAndManageJobs(jobArray, waitTime) == 1)
        return 1;

      errorCode = RunMvsPkg.scanJobsAndSetStatus( jobArray );

      System.out.println("Ending job management phase");
      System.out.println();

      System.out.println("Execution summary for all jobs:");
      System.out.println();
      RunMvsPkg.printJobExecutionSummary(jobArray);

      System.out.println();
      saveJobOutput(jobArray);

    }
    catch (MvsPkgError e)
    {
      System.err.println(e.getMessage());
      return 1;
    }
    if ((errorCode == 1))
    {
       return 1;
    }
    else
      return 0;

  }  // doTSOBuild

  /**
   * Generate JCL for TSO Build process
   * A separate job will be generated:
   *     copy2tso.jcl - ALLOCLIB and FTPFILE Steps
   *     compile1.jcl - The step name is the name of the src file (TEST)
   *     copy2hfs.jcl - FTPFILE and DELPDS Steps
   *
   * @param jobArray An array to be filled with jobs with each job
   * @param srcFile: Source Files (test) 
   * @param srcFilePath: Full source names (/u/userid/sandbox/src/plx/test.plx)
   * @param lmacFile: Local macros (/u/userid/sandbox/src/plx/a.mac)
   * @param emacFile: Export macros(/u/userid/sandbox/export/mvs390_oe_2/usr/incl390/b.mac)
   * @numOfParts: Total number of source files to compile 
   * @asmExist Found ASM parts: true or false
   * @plsExist Found PLS parts: true or false
   * @plxExist Found PLX parts: true or false
   * @bldPath_ Directory to store generated JCL files (/u/userid/sandbox/obj/mvs390_oe_2/plx)
   *
   * @exception MvsPkgError I/O error occurs
   */
  private static void generateTSOBuildJcl(ArrayList jobArray, String srcFile[],
                                          String srcFilePath[], String lmacFile[], String emacFile[],
                                          int numOfParts, boolean asmExist, boolean cblExist,
                                          boolean plsExist, boolean plxExist,
                                          String bldPath_, boolean fileTooLong)
                                          throws MvsPkgError, IOException
  {  
    MvsJclGenerator jclGen = new MvsJclGenerator(null, 1);        
    MvsJobInfo allocJobInfo = null;
    MvsJobInfo compileJobInfo = null;
    MvsJobInfo copyJobInfo = null;

    boolean oneCompileJob = false;
    boolean needJobForRemainingFiles = false;
    
    int jobCount = 0;
    int totalCount = 0;
    int firstFileIndex = 0;
    int endIndex = 0;        
    int lastFileIndex =  0;
    int numOfJobs_ = 0;
    int fileRemainder = 0;
    int numOfFilesPerJob = 0;
    int bldCount = 1;
    int typeCnt = 0;
    
    String dirsep = System.getProperty("file.separator");

    String sBox_ = MvsProperties.sBox;
    String objectDir_ = MvsProperties.objectDir;
    String srcPath_ = MvsProperties.srcPath;

    // sandbox name will be trim to 8 characters for TSO dataset
    if (sBox_.length() > 8)
    {
      System.out.println("Sandbox Name " + sBox_ +
        " has more than 8 characters. Truncated to " +
        sBox_.substring(0, 8));
      sBox_ = MvsProperties.sBox.substring(0, 8);
    }

    if (objectDir_.endsWith(dirsep))  // strip backslash
    {
      objectDir_ = objectDir_.substring(0, objectDir_.length() - 1);
    }

    String hlqString     = MvsProperties.bldHLQ;    
    String pdsString     = null;
    String dsnString     = MvsProperties.userid + "." + sBox_;

    // if HLQ not defined or equals userid, use userid
    if ( (hlqString == null) || (hlqString.equalsIgnoreCase(MvsProperties.userid)) )
      pdsString     = dsnString;     
    else
      pdsString     = hlqString + "." + dsnString;
    
    String pdsLangName   = pdsString + ".SRC";     
    String pdsLMacName   = pdsString + ".MACLOCAL";
    String pdsEMacName   = pdsString + ".MACEXPT";     
    String pdsObject     = pdsString + ".O";
    String pdsListing    = pdsString + ".LISTING";
    String pdsClrPrnt    = pdsString + ".CLRPRINT";
    String pdsUnidata    = pdsString + ".UNIDATA";
        
    // generate the copy2tso.jcl 
    allocJobInfo = jclGen.generateTsoBldGather(pdsLangName, pdsLMacName, pdsEMacName,
                                               pdsObject, pdsListing, pdsClrPrnt, pdsUnidata,
                                               srcFile, srcFilePath, lmacFile, emacFile, numOfParts,
                                               bldPath_, fileTooLong);
       
    jobArray.add(allocJobInfo);

    allocJobInfo = null;

    int filesInJob;

    // if number of files not defined, all files in one job (default)
    if (MvsProperties.numOfFiles == null)
    {
      filesInJob = 0; 
    }
    else
    { 
      Integer filesPerJob = new Integer(MvsProperties.numOfFiles); 
      int filesPerJob_ = filesPerJob.intValue();
      filesInJob = filesPerJob_;
    }
           
    if (filesInJob == 0) 
    {
      jobCount = 1;   // default
      oneCompileJob = true;
    }
    else 
    {
      // compute the number of compile Jobs needed
      numOfJobs_ = computeNumOfJobs(numOfParts);
               
      // if the total number of files to compile less than or equal 
      // to the number of files defined to a job, all files will be in one job 
      if ((numOfJobs_ == 1) && (numOfParts <= filesInJob))
      {
        numOfFilesPerJob = numOfParts;

        jobCount = 1;   // default
        oneCompileJob = true;
      }
      else
      {
        // put x number of files in a job
        numOfFilesPerJob = filesInJob; 
        // Get the remaining files
        fileRemainder = remainingFiles(numOfParts);
     
        // if no files remaining, get job count
        if (fileRemainder == 0)
        {
          jobCount = numOfJobs_ ;
        }
        else
        {
          // add one more job for remaining files
          jobCount = numOfJobs_ + 1;
          needJobForRemainingFiles = true;
        }

        oneCompileJob = false;
      }     
    } // filesInJob

    // if more than one compile job needed,
    // set first and last index for array
    if (!oneCompileJob)
    {
      firstFileIndex = 0;          
      lastFileIndex  = numOfFilesPerJob - 1;
    }

    while (bldCount <= jobCount) 
    {
      // create compile JCL(s)
      if (oneCompileJob)    // all compiles in one Job
      {
        // generate compile1.jcl
        compileJobInfo = jclGen.generateTsoBldCompile(bldCount, pdsLangName, pdsLMacName, pdsEMacName,
                                               pdsObject, pdsListing, pdsClrPrnt, pdsUnidata,
                                               srcFile, srcFilePath, 0, 0, oneCompileJob, asmExist,
                                               cblExist, plsExist, plxExist, bldPath_);
            
        jobArray.add(compileJobInfo);   
            
        compileJobInfo = null;

        bldCount++;             
      }
      else
      {
        // generate compile1.jcl, compile2.jcl, etc. 
        compileJobInfo = jclGen.generateTsoBldCompile(bldCount, pdsLangName, pdsLMacName, pdsEMacName,
                                                      pdsObject, pdsListing, pdsClrPrnt, pdsUnidata,
                                                      srcFile, srcFilePath, firstFileIndex, lastFileIndex,                                                   
                                                      oneCompileJob, asmExist, cblExist, 
                                                      plsExist, plxExist, bldPath_); 
            
        jobArray.add(compileJobInfo);   

        compileJobInfo = null;

        bldCount++;

        if (needJobForRemainingFiles)   // last job to have remaining files
        {
          int currentIndexPosition = lastFileIndex;              
          firstFileIndex = currentIndexPosition + 1;

          if (bldCount == jobCount) 
          {
            lastFileIndex =  currentIndexPosition + fileRemainder;
          }
          else
          {
            lastFileIndex =  currentIndexPosition + numOfFilesPerJob;
          }
        }
        else 
        {
          if (bldCount <= jobCount) 
          { 
            int currentIndexPosition = lastFileIndex;
 
            firstFileIndex = currentIndexPosition + 1;
            lastFileIndex =  currentIndexPosition + numOfFilesPerJob;
          }
        } // needJobForRemainingFiles             
      } // oneCompileJob
    } // end while
              
    // generate copy2hfs.jcl 
    copyJobInfo = jclGen.generateTsoBldCopyToHFS(pdsLangName, pdsLMacName, pdsEMacName,
                                                 pdsObject, pdsListing, pdsClrPrnt,
                                                 pdsUnidata, srcFile, srcFilePath, bldPath_);

    jobArray.add(copyJobInfo);
    copyJobInfo = null;
    
    return;
  }  // generateTSOBuild

  /**
   * Returns true if the specified language is found in the variable 
   * language2use
   *
   * @param String langStr
   */
  private static boolean isLanguageFound( String langStr )
  {
    StringTokenizer wordTokens =
           new StringTokenizer(MvsProperties.languages2use);
    
    String lang2Use;
    int numOfWordsInString = wordTokens.countTokens();

    while (wordTokens.hasMoreTokens())
    {
      lang2Use = (String) wordTokens.nextToken();
      
      if (lang2Use.equalsIgnoreCase(langStr))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Generate dependencies for compiled files 
   *
   * @param srcArray: Source names (test) 
   * @param srcPathArray: Full source names (/u/userid/sandbox/src/plx/test.plx)
   * @param lmacArray: Local macros (/u/userid/sandbox/src/plx/a.mac)
   * @param emacArray: Export macros(/u/userid/sandbox/export/mvs390_oe_2/usr/incl390/b.mac)
   * @numOfParts: Total number of source files to compile 
   * @asmFound Found ASM parts: true or false
   * @plsFound Found PLS parts: true or false
   * @plxFound Found PLX parts: true or false
   * @bldLstDir Directory to store generated JCL files (/u/userid/sandbox/obj/mvs390_oe_2/plx)
   *
   * @exception I/O error occurs
   */
  private static void generateFileDependency(String srcArray[], String srcPathArray[], String lmacArray[], 
                                             String emacArray[], int numOfParts, String bldLstDir ) throws IOException
  {
    BufferedWriter bw = null;
    String uFile = null;

    String dirsep = System.getProperty("file.separator");     
    String srcFull_ = MvsProperties.srcFull;
     
    String nextLine = null;     
    String fileType = null;
    String oExt    = null;
    int    numOfCobolFiles = 0;

    for (int i=0;i<srcPathArray.length;i++) 
    {
      String fileName = srcArray[i];
      String includeFile = srcArray[i] + ".tmp";
      String inclFile = bldLstDir + includeFile;

      File file = new File( inclFile );

      // verify that temp file (.tmp) with the dependency 
      // info was copied from TSO to hfs 
      if (!file.exists())  
      { 
        if (srcPathArray[i].endsWith(".cbl") ||
            srcPathArray[i].endsWith(".cob") ||
            srcPathArray[i].endsWith(".cobol"))
        {
          numOfCobolFiles++;
        }
        else
        {
          System.out.println("No dependencies generated for : ");
          System.out.println(srcPathArray[i]);
        }  
      }
      else
      { 
        // create .u file
        uFile = fileName + ".u";
          
        try
        { 
          if (srcPathArray[i].endsWith(".asm"))
          {
             fileType = "asm";
             oExt = ".asm";
          }
          else if (srcPathArray[i].endsWith(".cbl") ||
                   srcPathArray[i].endsWith(".cob") ||
                   srcPathArray[i].endsWith(".cobol"))
          {
             fileType = "cbl";
             if (srcPathArray[i].endsWith(".cbl")) 
               oExt = ".cbl";
             else if (srcPathArray[i].endsWith(".cob")) 
               oExt = ".cob";
             else  
               oExt = ".cobol";

          }
          else if (srcPathArray[i].endsWith(".pls"))
          {
             fileType = "pls";
             oExt = ".pls";
          }
          else if (srcPathArray[i].endsWith(".plx"))
          {
             fileType = "plx";
             oExt = ".plx";
          }

          // read .tmp file
          BufferedReader freader = new BufferedReader(new FileReader(inclFile));
        
          bw = new BufferedWriter( new FileWriter(uFile));
                    
          if (fileType == "asm") 
          {               
             bw.write(fileName + ".o:  " + srcFull_ + dirsep + fileName + oExt); bw.newLine(); 
          }
          else if (fileType == "cbl") 
          {
             bw.write(fileName + ".o:  " + srcFull_ + dirsep + fileName + oExt); bw.newLine(); 
          }
          else if (fileType == "pls") 
          {
             bw.write(fileName + ".o:  " + srcFull_ + dirsep + fileName + oExt); bw.newLine(); 
          }
          else if (fileType == "plx") 
          {               
             bw.write(fileName + ".o:  " + srcFull_ + dirsep + fileName + oExt); bw.newLine(); 
          }

          String incStr = "MACLST=(";
          String syslibStr = "INCLST=(";
          String attribStr = "";
          String inclWord = null;
          String includeLine = null;
          String lineStr = null;

          boolean macFlag = false;
          boolean inclFlag = false;
          boolean addInclude = true;
          boolean foundIncl = false;
          boolean foundIncludedMacs = false;
          boolean isEndOfLine = false;
          boolean foundLastComma = false;
          boolean isIncludeNextLine = false;
          boolean startMacroSearch = false;

          int inclPosition;          
          int startPosition;
          int endLPosition;
          int lastComma;
          int count = 0;
          
          if (fileType == "asm")   // ASM dependency search
          {
            while ((nextLine = freader.readLine()) != null)       
            {

              if ( ((inclPosition = nextLine.indexOf("MACLST=(")) != -1) ||
                   ((inclPosition = nextLine.indexOf("INCLST=(")) != -1) )
              {
                startMacroSearch = true;
              }

              // start seaching for macros
              if (startMacroSearch)
              {
                if ( (endLPosition = nextLine.indexOf(")")) != -1 )
                { 
                  isEndOfLine = true;                       
                  startMacroSearch = false;
                }

                if (isEndOfLine)
                {
                  if ( (startPosition = nextLine.indexOf("(")) != -1 )
                  {
                    lineStr =  nextLine.substring((startPosition + 1) , endLPosition);
                  }
                  else
                  { 
                    lineStr =  nextLine.substring(0 , endLPosition);
                  }
                } 
                else 
                {
                  isEndOfLine = false;                       

                  if ( (startPosition = nextLine.indexOf("(")) != -1 )
                  {
                    lastComma = nextLine.lastIndexOf(',');
                    lineStr =  nextLine.substring((startPosition + 1) , lastComma);
                  }
                  else
                  {
                    lastComma = nextLine.lastIndexOf(',');
                    lineStr =  nextLine.substring(0 , lastComma);                      
                  }
                } // isEndOfLine

                StringTokenizer wordTokens = new StringTokenizer( lineStr, "," );
                int numOfWordsInString = wordTokens.countTokens();

                if (numOfWordsInString == 0)
                {
                  continue;
                }
                else
                {
                  while (wordTokens.hasMoreTokens())
                  {
                    count++;
                     
                    inclWord = wordTokens.nextToken();
                    inclWord = inclWord.trim();
                    int wordLength = inclWord.length();

                    String word = inclWord.substring(9, wordLength);

                    checkFileDependency(fileName, lmacArray, emacArray, word.toLowerCase(), bw);                                                            
                  } // end while
                } // numOfWordsInString              
              } // startMacroSearch                
            } // end of read line
          }
          else if (fileType == "pls" || fileType == "plx")  // PLS or PLX dependency search
          {
            while ((nextLine = freader.readLine()) != null)       
            {
              int wordLen = nextLine.length();
                 
              StringTokenizer wordTokens = new StringTokenizer( nextLine );                   
              int numOfWordsInString = wordTokens.countTokens();
              if (numOfWordsInString == 0)
              {
                continue; 
              }
              else
              {
                String word1 = null;
                String word2 = null;
                String word3 = null;
                String word4 = null;

                word1 = (String) wordTokens.nextToken();
                word1 = word1.trim();                     

                if ( word1.startsWith("1") || word1.startsWith("-") )
                {
                   if (word1.startsWith("-END")) 
                     word1 = "END";
                   else
                     word1 = (String) wordTokens.nextToken();
                }

                word2 = (String) wordTokens.nextToken();
                word2 = word2.trim();

                if ( (word1.equals("LEVEL")) && (word2.equals("OF")) ||
                     (word1.equals("INCLUDE")) && (word2.equals("LEVEL")) ||
                     (word1.equals("MACRO")) && (word2.equals("LEVEL")) ||
                     (word1.equals("END")) && (word2.equals("OF")) )     
                {  
                  continue;
                }
                else
                {
                   // check if the macro the from search is valid  
                  checkFileDependency(fileName, lmacArray, emacArray, word1.toLowerCase(), bw);                                                            
                  
                  addInclude = true;

                  while ( wordTokens.hasMoreTokens() )
                  {
                    if (addInclude)
                    {
                      word3 = (String) wordTokens.nextToken();
                      word3 = word3.trim();
                      
                      checkFileDependency(fileName, lmacArray, emacArray, word3.toLowerCase(), bw);                                                            

                      addInclude = false;
                    }
                    else
                    {
                      word4 = (String) wordTokens.nextToken();
                      word4 = word4.trim();
                      
                      addInclude = true;
                    }
                  } // end while

                } // word1 and word2

              } // numOfWordsInString

            }  // end while nextLine
          }
          else
          {
            System.out.println("Dependency for this language - not supported");
          } 

          // delete the .tmp file
          deleteMvsFiles(inclFile);

        }
        catch (IOException ex)
        {
           System.out.println("Error Writing.... " + uFile);
        }
        finally
        {
          try
          { 
            if (bw != null)
              bw.close();
          }
          catch (IOException ex)
          {
             System.out.println("Error Closing.... " + uFile);      
          }
        }

        nextLine = null;
      }  // end if file
    } // end for loop
    
    if (numOfCobolFiles > 0) 
    {
      System.out.println();
      System.out.println("****************************************************");
      System.out.println("* Dependencies for Cobol - not currently supported *");
      System.out.println("****************************************************");    
    }
  }

  /**  
   *  Check File Dependency
   *
   * @param srcFileName: Source File Names (/u/userid/sandbox/src/plx/test.plx)
   * @param lmacArray: Local macros (/u/userid/sandbox/src/plx/a.mac)
   * @param emacArray: Export macros(/u/userid/sandbox/export/mvs390_oe_2/usr/incl390/b.mac)
   *
   * @exception I/O error occurs   
   */
  private static void checkFileDependency(String srcFileName,
                                          String lmacArray[],
                                          String emacArray[], 
                                          String macFile, 
                                          BufferedWriter bw) throws IOException 
  {    
    String dirsep = System.getProperty("file.separator");
    String srcPath_ = MvsProperties.srcPath;

    boolean inLocalInclude = false;

    try
    {
      for (int i=0;i<lmacArray.length;i++) 
      {
        int val1 = lmacArray[i].lastIndexOf( "/" );
        int val2 = lmacArray[i].lastIndexOf( "." );
  
        String srcMacFile = lmacArray[i].toLowerCase().substring( (val1+1), val2 );
          
        if (srcMacFile.equals(macFile))
        {
          inLocalInclude = true;
  
          bw.write(srcFileName + ".o:  ${MAKETOP}" + srcPath_ + dirsep +
                   macFile + ".mac"); bw.newLine();        
          break;
        }
      }  // end for loop
  
      // If not a local macro, search export macro
      if (!inLocalInclude) 
      {
        for (int i=0;i<emacArray.length;i++) 
        {
          int val1 = emacArray[i].lastIndexOf( "/" );
          int val2 = emacArray[i].lastIndexOf( "." );
          
          String srcMacFile = emacArray[i].toLowerCase().substring( (val1+1), val2 );
          
          if (srcMacFile.equals(macFile))
          {
            bw.write(srcFileName + ".o:  ${MAKETOP}" + MvsProperties.exportPath +
                     macFile + ".mac"); bw.newLine();                
            break;
          }  
        }  // end for loop  
      }  // inLocalInclude
    }
    catch( IOException e )
    {
      System.out.println("IO Exception writing ");
    }
    return;
  }  // checkFileDependency

  /**
   * converts a vector into a string array
   */
  private static String[] vector2StringArray( Vector vector )
  {
    String result[] = new String[vector.size()];
    Enumeration enumeration = vector.elements();
    for (int idx = 0; enumeration.hasMoreElements(); idx++)
    {
       result[idx] = (String)enumeration.nextElement();
    }
    return result;
  }

  /**  
   * Delete the temporary dependency file copied from TSO
   *
   * @param part: File with the included dependencies (test.tmp)   
   */
  private static void deleteMvsFiles(String part) 
  {
    File file = new File( part );

    file.delete();
  }

  /**  
   * Get the source name from input line
   *
   * @param line: Input line (/u/userid/sandbox/src/plx/test.plx)  
   *
   * @return  fileName  (test)       
   */
  private static String createSrcData( String line )
  {    
    // Create the file string.
    String fileName = "";

    String dirsep = System.getProperty("file.separator");

    int val1 = line.lastIndexOf( dirsep );
    int val2 = line.lastIndexOf( "." );
    
    String srcfile = line.substring( (val1+1), val2 );

    if (srcfile != null)
      fileName = srcfile;

    return fileName;

  }  // createSrcData

  /**  
   * Compute the number of Jobs needed to handle the compiles   
   *
   * @param totalFileCnt:  Total number of files to be compiled
   *   
   * @exception I/O error occurs   
   * @return  numOfJobs   
   */
  private static int computeNumOfJobs(int totalFileCnt ) throws IOException
  {
    int filesInJob;
    int numOfJobs = 0;
    boolean isFilePerJobGreater = false;

    if (MvsProperties.numOfFiles == null)
    {
      filesInJob = 0; 
    }
    else
    { 
      Integer filesPerJob = new Integer(MvsProperties.numOfFiles); 
      int filesPerJob_ = filesPerJob.intValue();
      filesInJob = filesPerJob_;
    }

    if (totalFileCnt < filesInJob)
       isFilePerJobGreater = true;

    if (isFilePerJobGreater)
       numOfJobs = 1;
    else
       numOfJobs = totalFileCnt / filesInJob;

    return numOfJobs;

  }  // computeNumOfJobs

  /**  
   * If the number of files in a job don't divide evenly into the 
   * total number of files to compile, then get the remaining files  
   *
   * @param totalFileCnt:  Total number of files to be compiled
   * @param jobs:  Number of jobs needed   
   *   
   * @exception I/O error occurs   
   * @return  remainderCnt   
   */
  private static int remainingFiles(int totalFileCnt) throws IOException
  {
    int filesInJob;
    int remainderCnt;
 
    if (MvsProperties.numOfFiles == null)
    {
      filesInJob = 0; 
    }
    else
    { 
      Integer filesPerJob = new Integer(MvsProperties.numOfFiles); 
      int filesPerJob_ = filesPerJob.intValue();
      filesInJob = filesPerJob_;
    }
     
    remainderCnt = totalFileCnt % filesInJob;

    return remainderCnt;

  }  // remainingFiles

  /**
   * Save the output of all the jobs in an array whose output was
   * successfully retreived.  If not specifed, the job output 
   * is deleted.
   *
   * @param ArrayList jobArray An array of jobs
   */
  private static void saveJobOutput( ArrayList jobArray )
  {
    MvsJobInfo jobInfo;
    ListIterator ai = jobArray.listIterator();

    while (ai.hasNext())
    {
      jobInfo = (MvsJobInfo) ai.next();
      if (jobInfo.getStatus() != MvsJobInfo.ERROR)
      {
        if (MvsProperties.isSaveOutputFile())
        {
          System.out.println("Saving job output file at: " 
                             + jobInfo.getJobOutputFileName());
          System.out.println();
        }
        else
        {
          File f = new File(jobInfo.getJobOutputFileName());
          f.delete();
        }
      }
    }
  }  // saveJobOutput

}
