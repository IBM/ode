/*****************************************************************************
 *                    Licensed Materials - Property of XXXX
 *
 * IBM-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.3
 *
 * Date and Time File was last checked in: 8/28/03 17:29:25
 * Date and Time File was extracted/checked out: 06/04/13 16:46:47
 *
 *****************************************************************************/
package com.ibm.ode.pkg.pkgMvs;

/**
 * Class to submit jobs, monitor job execution and retrieve job output.
 * Jobs are submitted/monitored/retrieved via FTP via the MvsFtp class.
 *
 * @version 1.3
 * @author  Mark DeBiase
 */

import java.util.*;
import java.text.SimpleDateFormat;
import java.net.*;

public class MvsJobManager
{
  /**
   * Create a new MvsJobManager object to manage the jobs contained in
   * the Array of MvsJobInfo objects.
   */
  public MvsJobManager(ArrayList jobArray) throws UnknownHostException
  {
    jobArray_ = jobArray;

    try
    {
      String pkgClass = MvsProperties.pkgClass;

      if (pkgClass != null) 
      {
        String localHostName = java.net.InetAddress.getLocalHost().getHostName();

        ftp_ = new MvsFtp(java.net.InetAddress.getLocalHost().getHostName());        
      }
      else
      {
        String remoteHostName = MvsProperties.remoteHost;

        ftp_ = new MvsFtp(remoteHostName);
      }
    }
    catch(UnknownHostException e)
    {
      System.out.println
        ("  ERROR: Job manager is unable to get a valid hostname for job submission");
      throw new UnknownHostException();
    }
    // initialize timestamp format
    String tz = System.getProperty("user.timezone");
    dateFormat_ = new SimpleDateFormat("HH:mm:ss zzz EEE MMM dd");
    dateFormat_.setTimeZone( java.util.TimeZone.getTimeZone(tz) );
  }

  /**
   * Go through the array of MvsJobInfo objects.  Sumbit any jobs whose status
   * is DEFINED.  Check the status of any jobs whose status is SUBMITTED.  If
   * status check returns true (job done), retrieve the job output.
   */
  public void manageJobs()
  {
    ListIterator ai = jobArray_.listIterator();

    System.out.println("Performing job management at " +
                       dateFormat_.format(new Date()) );
    while (ai.hasNext())
    {
      MvsJobInfo jobInfo = (MvsJobInfo) ai.next();

      if (jobInfo.getStatus() == MvsJobInfo.DEFINED)
      {
        submitJob(jobInfo);
      }
      else if (jobInfo.getStatus() == MvsJobInfo.SUBMITTED)
      {
        if (checkJob(jobInfo))
        {
          // Job is done
          getJobOutput(jobInfo);
          if (MvsProperties.isDeleteOutput())
            deleteJobOutput(jobInfo);
        }
      }
    }
  }

  /**
   * Submit job from file specified in MvsJobInfo object.
   */
  private void submitJob( MvsJobInfo jobInfo )
  {
    StringBuffer command = new StringBuffer(80);

    System.out.println("Submitting " + jobInfo.getJobName() + " from " +
                        jobInfo.getJclFileName());

    try
    {
      String jobnumber = ftp_.submitJob(MvsProperties.userid,
                                        MvsProperties.password,
                                        jobInfo.getJclFileName());
      jobInfo.setJobNumber(jobnumber);
      jobInfo.setStatus(MvsJobInfo.SUBMITTED);
      System.out.println("  Submit successful: job number is " + jobnumber);
    }
    catch (MvsFtpException x)
    {
      jobInfo.setStatus(MvsJobInfo.ERROR);
      System.out.println("  Submit failed");

      System.err.println("Submit for job " + jobInfo.getJobName() + " failed:");
      System.err.println(x.getMessage());
      if (x.hasNestedException())
        x.printNestedStackTrace();
      System.err.println("FTP output:");
      System.err.println(ftp_.getOutput());
    }
  }

  /**
   * Check status of job whose job number is stored in MvsJobInfo object.
   * Returns true if the job is complete.  Job status will be set to ERROR
   * in the following cases:
   * - status returned is UNKNOWN (output is missing)
   * - a MvsFtpException is thrown & MvsJobInfo.MAX_ERRORS has been reached
   * Otherwise, job status is not changed.
   */
  private boolean checkJob(MvsJobInfo jobInfo)
  {
    boolean jobDone = false;

    System.out.println("Checking status of job " + jobInfo.getJobNumber());

    try
    {
      int jobStatus = ftp_.checkJob(MvsProperties.userid,
                                    MvsProperties.password,
                                    jobInfo.getJobNumber());
      System.out.println("  Job status is " +
                         MvsFtp.JOB_STATUS_TEXT[jobStatus]);

      if (jobStatus == MvsFtp.JOB_STATUS_INPUT  ||
          jobStatus == MvsFtp.JOB_STATUS_HELD   ||
          jobStatus == MvsFtp.JOB_STATUS_ACTIVE)
      {
        // job is waiting/running
        jobDone = false;
      }
      else if ( jobStatus == MvsFtp.JOB_STATUS_UNKNOWN )
      {
        // job output is gone - failure
        jobInfo.setStatus(MvsJobInfo.ERROR);
        jobDone = false;

        System.err.println("Job " + jobInfo.getJobNumber() + " output is " +
                           "missing");
        System.err.println("Was held output class specified on job card?");
      }
      else
      {
        // MvsFtp.JOB_STATUS_OUTPUT - job is done
        jobDone = true;
      }
    }
    catch (MvsFtpException x)
    {
      // could not get job status via ftp
      jobInfo.setErrorOccured();
      jobDone = false;
      int retries = MvsJobInfo.MAX_ERRORS - jobInfo.getCurrentErrors();
      System.out.println("  Error checking job status");
      if (retries > 0)
      {
        System.out.println("  Will retry " + retries + " more times");
      }

      System.err.println("Error getting status for job " +
                         jobInfo.getJobNumber());
      System.err.println(x.getMessage());
      if (x.hasNestedException())
        x.printNestedStackTrace();
      System.err.println("FTP output:");
      System.err.println(ftp_.getOutput());
    }

    return jobDone;
  }

  /**
   * Retrieve output of job whose job number is stored in MvsJobInfo object.
   * The name of the file that the output will be written to is also taken
   * from the MvsJobInfo object.
   * If output is not reteived and MvsJobInfo.MAX_ERRORS has been reached,
   * job status will be set to ERROR. Otherwise job status is not changed.
   */
  private void getJobOutput(MvsJobInfo jobInfo)
  {
    System.out.println("Retrieving output for job " + jobInfo.getJobNumber());

    try
    {
      ftp_.getJob(MvsProperties.userid, MvsProperties.password,
                  jobInfo.getJobNumber(), jobInfo.getJobOutputFileName());
      jobInfo.setStatus(MvsJobInfo.OUTPUT);
      System.out.println("  Successfully retrieved job output");
    }
    catch (MvsFtpException x)
    {
      jobInfo.setErrorOccured();
      System.out.println("  Failed to get job output");

      System.err.println("Failed to get output for job " +
                         jobInfo.getJobNumber());
      int retries = MvsJobInfo.MAX_ERRORS - jobInfo.getCurrentErrors();
      if (retries > 0)
      {
        System.err.println("Will retry " + retries + " more times");
      }
      System.err.println(x.getMessage());
      if (x.hasNestedException())
        x.printNestedStackTrace();
      System.err.println("FTP output:");
      System.err.println(ftp_.getOutput());
    }
  }

  /**
   * Delete output of job whose job number is stored in MvsJobInfo object.
   * If output is not deleted and MvsJobInfo.MAX_ERRORS has been reached,
   * job status will be set to ERROR.  Otherwise job status is not changed.
   */
  private void deleteJobOutput( MvsJobInfo jobInfo )
  {
    System.out.println("Deleting output for job " + jobInfo.getJobNumber());

    try
    {
      ftp_.deleteJobOutput(MvsProperties.userid, MvsProperties.password,
                  jobInfo.getJobNumber());
      jobInfo.setStatus(MvsJobInfo.OUTPUT);
      System.out.println("  Successfully deleted job output");
    }
    catch (MvsFtpException x)
    {
      jobInfo.setErrorOccured();
      System.out.println("  Failed to delete job output");

      System.err.println("Failed to delete output for job " +
                         jobInfo.getJobNumber());
      int retries = MvsJobInfo.MAX_ERRORS - jobInfo.getCurrentErrors();
      if (retries > 0)
      {
        System.err.println("Will retry " + retries + " more times");
      }
      System.err.println(x.getMessage());
      if (x.hasNestedException())
        x.printNestedStackTrace();
      System.err.println("FTP output:");
      System.err.println(ftp_.getOutput());
    }
  }

  private ArrayList        jobArray_;
  private MvsFtp           ftp_;
  private SimpleDateFormat dateFormat_;
}
