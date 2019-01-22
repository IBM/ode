//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/pkgMvs/MvsJobOutputScanner.java, pkgMvs, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.5
//*
//* Date and Time File was last checked in:       98/02/25 17:38:52
//* Date and Time File was extracted/checked out: 99/04/25 09:14:39
//*
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* MAD      F 1463  Initial creation
//* MAD      F 1680  Made class public for MVS media creation
//* MAD      D 3986  Remove nulls chars from job output in printJobOutput()
//* WM       D 12195 Checking for >80 line lengths 
//*
//*****************************************************************************
package com.ibm.ode.pkg.pkgMvs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;
import java.util.Vector;
import com.ibm.ode.lib.io.Interface;

/**
 * This class contains various methods for processing the job output file.
 * @version 1.5 98/02/25
 * @author  Mark DeBiase
 * @see     MvsJobInfo
 */
public class MvsJobOutputScanner
{
  /**
   * This method scans the job output file looking for the following messages:
   *   $HASP106 JOB DELETED BY JES2 OR CANCELLED BY OPERATOR BEFORE EXECUTION
   *   IEFC452I jobname - JOB NOT RUN - JCL ERROR
   *   IEF453I jobname - JOB FAILED - JCL ERROR
   *   IEF142I jobname stepname STEP WAS EXECUTED - COND CODE code
   *   IEF272I jobname stepname - STEP WAS NOT EXECUTED
   *   IEF472I jobname stepname - COMPLETION CODE - SYSTEM=scode USER=ucode  
   *
   * @param MvsJobInfo jobInfo object
   *   The MvsJobInfo object will be updated with step return codes and any
   *   termination messages encountered.  
   * @exception MvsPkgError, if FileNotFoundException or I/O error occurs.
   */  
  public static void getReturnCodes( MvsJobInfo jobInfo ) 
    throws MvsPkgError
  {
    boolean foundJesMsgs = false;
    int     index;
    String  endSpoolFile = "!! END OF JES SPOOL FILE !!";
    String  jobOutputFile = jobInfo.getJobOutputFileName();
    BufferedReader br = null;

    try
    {
      br = new BufferedReader(new FileReader(jobOutputFile));
      String line = br.readLine();
      while (line != null)
      {
        String word1 = null;
        StringTokenizer st = new StringTokenizer(line, " ");
   
        // get first word on the line
        if (st.hasMoreTokens())
        {
          word1 = st.nextToken();
        }
  
        if (foundJesMsgs && line.indexOf(endSpoolFile) != -1)
        {
          // when we get to the end of the Jes messages stop scanning
          // because there is nothing else we are interested in.
          break;
        }
  
        // processes the messages:
        if ((index = line.indexOf("IEFC452I")) != -1)        // job log
        {
          //IEFC452I jobname - JOB NOT RUN - JCL ERROR
          jobInfo.setJobTerminationMsg(line.substring(index).trim());
        }
        else if ((index = line.indexOf("IEF453I")) != -1)    // job log
        {
          //IEF453I jobname - JOB FAILED - JCL ERROR
          jobInfo.setJobTerminationMsg(line.substring(index).trim());
        }
        else if (line.indexOf("$HASP106") != -1)   // job log
        {
          //$HASP106 JOB DELETED BY JES2 OR CANCELLED BY OPERATOR BEFORE...
          // this msg probably due to invalid jobcard

          // remove print control
          if (line.charAt(0) == '-')
          {
            line = line.substring(1);     
          }
          jobInfo.setJobTerminationMsg(line.trim());
        }
        // make sure line is not blank....
        else if (word1 != null)
        {
          if (word1.equals("IEF142I"))                // jes msgs
          {
            //IEF142I jobname stepname STEP WAS EXECUTED - COND CODE code
            // stepname is 3rd word, step rc is 9th word
            // we are already at 1st word:
            st.nextToken(" -");         // skip 2nd word
            String stepname = st.nextToken();
            for (int i = 4; i <= 8; i++)  // skip words 4-8
            { 
              st.nextToken(); 
            }
            
	    // remove leading zeros from step rc:
            Integer I = new Integer(st.nextToken());
            jobInfo.setActualRc(stepname, I.toString());
            foundJesMsgs = true;
          }
          else if (word1.equals("IEF272I"))                // jes msgs
          {
            //IEF272I jobname stepname - STEP WAS NOT EXECUTED
            // stepname is 3rd word; currently at 1st word
            st.nextToken();           // skip 2nd word
            jobInfo.setActualRc(st.nextToken(), "*");
            foundJesMsgs = true;
          }
          else if (word1.equals("IEF472I"))                // jes msgs
          {
            //IEF472I jobname stepname - COMPLETION CODE - SYSTEM=s USER=u
            // get stepname at word 3 (now at word 1)
            st.nextToken(" -");       // skip 2nd word
            String stepname = st.nextToken();
            for (int i = 4; i <= 5; i++)
            { 
              st.nextToken(" -"); 
            }
           
            // get system/user abend code starting word 6
            String sCode = st.nextToken();
            String uCode = st.nextToken();
            int sindex = sCode.indexOf("=");
            int uindex = uCode.indexOf("=");
            sCode = sCode.substring(sindex + 1);
            uCode = uCode.substring(uindex + 1);
            if (sCode.equals("000"))
            {
              jobInfo.setActualRc(stepname, "U" + uCode);
            }
            else
            {
              jobInfo.setActualRc(stepname, "S" + sCode);
            }
            foundJesMsgs = true;
          }
        }
        line = br.readLine();
      } //while
    } //try    
    catch (FileNotFoundException ex)
    {
      throw new MvsPkgError(MvsPkgError.fileNotFound2, 
                            new Object[] {"job output", jobOutputFile});
    }
    catch (IOException ex)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"reading", jobOutputFile, ex.getMessage()});
    }
    finally
    {
      try
      {
        if (br != null) br.close();
      }
      catch (IOException ex)
      {
        System.err.println("Warning: Caught IOException closing file" + 
                           jobOutputFile + ":");
        System.err.println(ex.getMessage());
      }
    }
  }

  /**
   * This method sets the record truncation message or the copy failed message
   * for later use.
   *
   * @param jobInfo MvsJobInfo object. The MvsJobInfo contains information about
   *   the job - mainly the job output file, the job name and job numbe
   */
  public static void setErrorMessages( MvsJobInfo jobInfo )
    throws MvsPkgError
  {
    BufferedReader br = null;
    String jobOutputFile = jobInfo.getJobOutputFileName();
    boolean setRC = false;
    try
    {
      br = new BufferedReader(new FileReader(jobOutputFile));
      String line = null;
      while ((line = br.readLine()) != null)
      {
        // Look for any cp command error messages. Most of the cp command 
        // error messages start with FSUM. We use "OSHELL cp" to copy files 
        // from HFS to TSO datasets.
        // Error EDC5129I occurs when "OSHELL cp" tries to copy a 
        // non-existent file
        // This will happen only while running GATHER
        if ((line.indexOf("FSUM") != -1) || (line.indexOf("EDC5129I") != -1))
        {
          // strip null chars from job output line
          int nullIndex = line.indexOf(0);
          if (nullIndex != -1)
          {
             line = line.substring(0, nullIndex);
          }
            
          // Found a error message, so add the line to jobInfo object
          jobInfo.addErrorMessage(line);
          // set the return code to 8 only once
          if (!setRC)
          {
            jobInfo.setActualRc("GATHER", "8");
            setRC = true;
          }
        }
        else if ((line.trim().startsWith("OSHELL Exit Status")) &&
                 (line.trim().endsWith("1")))
        {
          // An error occured but we are not sure what the error is,
          // because OSHELL returned 1.
          // Don't set the error message in this case
          // set the return code to 8 only once
          if (!setRC)
          {
            jobInfo.setActualRc("GATHER", "8");
            setRC = true;
          }
        }
      }//while
    }
    catch (FileNotFoundException ex)
    {
      throw new MvsPkgError(MvsPkgError.fileNotFound2, 
                            new Object[] {"job output", jobOutputFile});
    }
    catch (IOException ex)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"reading", jobOutputFile, ex.getMessage()});
    }
    finally
    {
      try
      {
        if (br != null) br.close();
      }
      catch (IOException ex)
      {
        System.err.println("Warning: Caught IOException closing file" + 
                           jobOutputFile + ":");
        System.err.println(ex.getMessage());
      }
    }
  }
  
  /**
   * Prints the job output to standard out.
   * 
   * @param MvsJobInfo jobInfo object
   *        The MvsJobInfo contains information about the job whose output
   *        is to be printed - mainly the job output file, 
   *        but also the job name and job number.    
   */
  public static void printJobOutput( MvsJobInfo jobInfo )
  {
    System.out.print("========================================");
    System.out.println("========================================");
    System.out.println("Output for job " + jobInfo.getJobName() + " - " +
                       jobInfo.getJobNumber() + ":");
    System.out.print("========================================");
    System.out.println("========================================");

    BufferedReader br = null;
    try
    {     
      FileReader fr = new FileReader(jobInfo.getJobOutputFileName());
      br = new BufferedReader(fr);
      String line;
      boolean foundTruncMsg = false;

      while ((line = br.readLine()) != null)
      {
        // strip null chars from job output & then print to stdout
        int nullIndex = line.indexOf(0);
        if (nullIndex != -1)
        {
           line = line.substring(0, nullIndex);
        }          
        System.out.println(line);
      }
      br.close();
    }
    catch (FileNotFoundException ex)
    {
      System.err.println("Could not find file: " + jobInfo.getJobOutputFileName());
      System.err.println("Could not print job output");
    }
    catch (IOException ex)
    {
      System.err.println("Caught IOException while printing job output:");
      System.err.println(ex.getMessage());
    }
    finally
    {
      try
      {
        if (br != null) br.close();
      }
      catch (IOException ex)
      {
        System.err.println("Warning: Unable to close the file '" + 
                           jobInfo.getJobOutputFileName() + 
			   "' due to the following error:");
        System.err.println(ex.getMessage());
      }
    }
  }

  /**
   * Reads job output to get the names of the relfiles from the RTG FUNCOUT   
   * spool file.  Example of output we are searching for:
   *
   *      STORAGE REQUIREMENTS FOR DATA SETS
   *
   *      DATASET NAME                                 1600 6250 3480      TRKS
   *
   *      IBMVS9.HMAD310.SMPMCS                           1    1    1         1
   *      IBMVS9.HMAD310.F1                              26    7    1
   *      IBMVS9.HMAD310.F2                              18    5    1         6
   *      IBMVS9.HMAD310.F3                              11    3    1         5
   *      IBMVS9.HMAD310.F4                               1    1    1         1
   *      --- SUBTOTALS FOR FUNCTION ---                 57   17    5
   * @param String jobOutputFile
   * @return Vector containing the name of the SMPMCS data set and
   *         all the relfile data sets.  
   * @exception MvsPkgError, if FileNotFoundException or I/O error occurs.
   */
  public static Vector getPackageFiles( String jobOutputFile ) 
    throws MvsPkgError
  {
    String  header1 = "STORAGE REQUIREMENTS FOR DATA SETS";
    String  header2 = "DATASET NAME";
    String  trailer = "--- SUBTOTALS FOR FUNCTION ---";
    String  line;
    boolean done = false;
    boolean foundHeader1 = false, foundHeader2 = false;
    Vector  v = new Vector();

    BufferedReader br = null;

    try 
    {
      br = new BufferedReader( new FileReader(jobOutputFile) );

      line = br.readLine();
      while ( line != null && !done )
      {
        if ( foundHeader1 && foundHeader2 && line.indexOf(trailer) != -1 )
        {
           done = true;
        }
        else if ( foundHeader1 && foundHeader2 ) 
        {
          line = line.substring(1);     // possible print control char in col 1

          StringTokenizer st = new StringTokenizer(line, " ");
          if ( st.hasMoreTokens() )     // skip blank lines
          {
            String packageFile = st.nextToken();
            v.addElement(packageFile);      
          }
        }
        else if ( foundHeader1 && line.indexOf(header2) != -1 )
        {
          foundHeader2 = true;
        }
        else if ( line.indexOf(header1) != -1 )
        {
          foundHeader1 = true;
        }

        line = br.readLine();
      } //while
    } //try
    catch (FileNotFoundException ex)
    {
      throw new MvsPkgError(MvsPkgError.fileNotFound2, 
                            new Object[] {"job output", jobOutputFile});
    }
    catch (IOException ex)
    {
      throw new MvsPkgError(MvsPkgError.ioException3,
                new Object[] {"reading", jobOutputFile, ex.getMessage()});
    }
    finally
    {
      try 
      { 
        if (br != null) br.close();
      }
      catch (IOException ex)
      {
        System.err.println("Warning: Unable to close the file '" + 
                           jobOutputFile + "' due to the following error:");
        System.err.println(ex.getMessage());
      }
    }
    return v;
  }
}
