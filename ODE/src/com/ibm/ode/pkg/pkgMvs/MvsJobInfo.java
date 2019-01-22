//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* Version: 1.3
//*
//* Date and Time File was last checked in:       97/09/30 10:31:49
//* Date and Time File was extracted/checked out: 99/04/25 09:14:39
//*
//*
//*****************************************************************************
package com.ibm.ode.pkg.pkgMvs;

import java.util.*;

/**
 * Holds status information for generated job streams.
 * Includes information such as file name containing generated jcl,
 * job name, information about each step in the job (expected/actual 
 * return codes), job output file name and job status.
 * @version  1.3 97/09/30
 * @author   Mark DeBiase
 * @see      MvsJclGenerator
 * @see      MvsJobManager
 * @see      MvsJobOutputScanner
**/

public class MvsJobInfo
{
  //***************************************************************************
  // Construct a new MvsJobInfo object.
  // 
  // Parameters: jclFileName - name of the file that contains the JCL
  //             jobName     - the job name
  //***************************************************************************
  public MvsJobInfo(String jclFileName, String jobName)  
  {
    jclFileName_       = jclFileName;
    jobName_           = jobName;
    stepNames_         = new ArrayList();
    expectedRc_        = new Hashtable();
    actualRc_          = new Hashtable();
    stepDescription_   = new Hashtable();
    errorMessages_     = new Vector();
    jobNumber_         = null;
    jobTermination_    = null;
    jobOutputFileName_ = null;
    status_            = DEFINED;
    errors_            = 0;
  }

  /**
   * Add information about a step in the job.
   *
   * @param String stepName    - the step name
   *        int    expectedRc  - the maximum allowable return code for the step
   *        String description - a description of the step's purpose
  **/
  public void addStep(String stepName, int expectedRc, String description)
  {
    stepName = stepName.trim();
    if ( stepNames_.contains(stepName) )
    {
      throw new RuntimeException( "Attempt to add duplicate step " + stepName );
    }
    stepNames_.add(stepName);
    expectedRc_.put(stepName, new Integer(expectedRc).toString());
    actualRc_.put(stepName, "?");
    stepDescription_.put(stepName, description); 
  }

  /**
   * Sets the job number after the job has been submitted.
   * The name of the job output file is also generated relative 
   * to PKG_TEMP_DIR as "JOBNUMBER.out". If the TSO build process
   * is running, then the job output file will be generated relative
   * to TGTPATH (i.e. obj/machinename/plxExample).
   *
   * @param String jobNumber   
  **/
  public void setJobNumber(String jobNumber)
  {
    jobNumber_         = jobNumber;
    
    if (MvsProperties.objectDir != null) 
      jobOutputFileName_ = MvsProperties.objectDir + jobNumber_ + ".out";
    else
      jobOutputFileName_ = MvsProperties.pkgTempDir + jobNumber_ + ".out";
  }

  /**
   * Sets the actual return code for a step after execution.
   *
   * @param  String stepName - the step name
   *         String actualRc - the step return code
  **/
  public void setActualRc(String stepName, String actualRc)
  {
    stepName = stepName.trim();
    actualRc_.put(stepName, actualRc);
  }

  /**
  * Sets a message indicitaing why a job failed (i.e., JCL error, cancelled).
  *
  * @param String message
  **/
  public void setJobTerminationMsg(String message)
  {
    jobTermination_ = message;
  }

  /**
  * @return the name of the file containing the jcl.
  **/
  public String getJclFileName()
  {
    return jclFileName_;
  }

  /**
  * @return the job number.
  **/
  public String getJobNumber()
  {
    return jobNumber_;
  }

  /**
  * @returns the job name.
  **/
  public String getJobName()
  {
    return jobName_;
  }

  /**
  * Sets the job status.
  *
  * @param int jobStatus
  **/
  public void setStatus(int jobStatus)
  {
    status_ = jobStatus;
  }

  /**
  * @return the job status.
  **/
  public int getStatus()
  {
    return status_;
  }

  /**
  * Increment the error count indicating the number of errors that occured
  * attempting to check job status or retrieve job output.  If the error
  * count exceeds the maximum number of allowable errors, set the job status
  * to ERROR.
  **/
  public void setErrorOccured()
  {
    errors_++;
    if ( errors_ >= MAX_ERRORS ) status_ = ERROR;
  }

  /**
  * @return the current count of the number of errors that have occured when
  * attempting to check job status or retrieve job output.
  **/
  public int getCurrentErrors()
  {
    return errors_;
  }

  /**
  * @return the job output file name (which is generated internally based
  * on the job number).
  **/
  public String getJobOutputFileName()
  {
    return jobOutputFileName_;
  }

  /**  
   * Adds the specified error message to the list of error messages.
   * 
   * @param errorMsg a String object containing the error
   */
  public void addErrorMessage( String errorMsg )
  {
    if (errorMsg != null)
      errorMessages_.addElement(errorMsg);
  }

  /**
   * @return record truncation message
   */
  public Vector getErrorMessages()
  {
    return errorMessages_;
  }

  /**
   * @return a summary report of each step in the job.
  **/
  public String toString()
  {
    String NL    = System.getProperty("line.separator");
    String sumry = "Execution summary for job ";
    String title = "Step      Max RC  Actual RC  Step Description";
    String uline = "----      ------  ---------  ----------------";

    StringBuffer report = new StringBuffer(1000);

    report.append(sumry).append(jobName_);
    if ( jobNumber_ != null )
    {
      report.append(" (").append(jobNumber_).append(")");
    }
    report.append(":").append(NL);

    if ( jobTermination_ != null )
    {
      report.append(jobTermination_).append(NL);
    }
    report.append(title).append(NL);
    report.append(uline).append(NL);

    ListIterator ai = stepNames_.listIterator();

    while ( ai.hasNext() )
    {
      String step = (String)ai.next();

      StringBuffer stepName    = new StringBuffer(8);
      StringBuffer expectedRc  = new StringBuffer(5);
      StringBuffer actualRc    = new StringBuffer(5);
  
      stepName.append(step);
      for (int i = step.length(); i < 8; i++)
        stepName.append(" ");
  
      expectedRc.append( (String)expectedRc_.get(step) );
      for (int i = expectedRc.length(); i < 5; i++)
        expectedRc.append(" ");
  
      actualRc.append( (String)actualRc_.get(step) );
      for (int i = actualRc.length(); i < 5; i++)
        actualRc.append(" ");
  
      report.append(stepName).append("  ");
      report.append(expectedRc).append("   ");
      report.append(actualRc).append("      ");
      report.append( (String)stepDescription_.get(step) ).append(NL);

    }

    Enumeration enumer = getErrorMessages().elements();
    while (enumer.hasMoreElements())
    {
      report.append("ERROR: ").append((String)enumer.nextElement()).append(NL);      
    }
    
    // change trailing newline to blank....
    report.setCharAt(report.length() - 1, ' ');

    return report.toString();
  }

  /**
  * Determine if the job was successful based on the expected vs. actual
  * step return codes.
  **/
  public boolean isJobSuccessful()
  {
    boolean successful = true;
    ListIterator ai = stepNames_.listIterator();
   
    while ( ai.hasNext() && successful )
    {
      String stepName = (String) ai.next();
      String expected = (String) expectedRc_.get(stepName);
      String actual   = (String) actualRc_.get(stepName);
      
      // all expected RC's should be convertible to integer
      // (only integer allowed for expectedRc on addStep())
      // actual rc can be:
      //   ?     - could not be determined from job output
      //   *     - step was not executed
      //   Sxxx  - system abend code
      //   Uxxxx - user abend code
      //   n     - integer return code
      if ( actual.equals("?") || 
           actual.equals("*") ||
           actual.charAt(0) == 'S' ||
           actual.charAt(0) == 'U'    )
      {
        successful = false;
      }
      else
      {
        // compare actual vs. expected rc
        int expectedInt = new Integer(expected).intValue();
        int actualInt   = new Integer(actual).intValue();

        if ( actualInt > expectedInt )
        {
          successful = false;
        }
      }
    } //while
    
    return successful;
  }

  //***************************************************************************  
  // job status codes:
  //***************************************************************************  
  public static final int DEFINED   = 0;  // job has been defined 
  public static final int SUBMITTED = 1;  // job has been submitted
  public static final int OUTPUT    = 2;  // job output has been retrieved
  public static final int CHECKED   = 3;  // job output has been checked
  public static final int ERROR     = 4;  // error - could be during submit,
                                          // checking status, getting output, 
                                          // or checking output
  public static final int FAILED    = 5;  // failed - job failed

  //***************************************************************************
  // Maximum number of errors allowed.
  //***************************************************************************
  public static final int MAX_ERRORS = 4; 
  

  //***************************************************************************
  // Private data.
  //***************************************************************************
  private ArrayList  stepNames_;          // list of all the steps in the job

                                          // hash tables indexed by step name:
  private Hashtable  expectedRc_;         // expected step return code
  private Hashtable  actualRc_;           // actual step return code
  private Hashtable  stepDescription_;    // text description of step function

  private String     jobTermination_;     // string containing job failure msg
  private String     jclFileName_;        // name of file containing JCL
  private String     jobName_;            // job name
  private String     jobNumber_;          // job number
  private String     jobOutputFileName_;  // job output file name as
                                          // ${PKG_TEMP_DIR}/JOBNUMBER.out
  /**
   * vector containing error msgs. Right now it contains the error messages that
   * are from OSHELL cp command.
   */
  private Vector     errorMessages_;
  
  private int        status_;             // job status - see above
  private int        errors_;             // # of errors that occured checking
                                          // job status or getting job output
}
