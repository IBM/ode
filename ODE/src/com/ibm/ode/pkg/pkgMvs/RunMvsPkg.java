/*****************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.3
 *
 * Date and Time File was last checked in: 5/5/04 11:32:59
 * Date and Time File was extracted/checked out: 06/04/13 16:46:55
 ****************************************************************************/
package com.ibm.ode.pkg.pkgMvs;

import java.io.File;
import java.io.IOException;
import java.lang.InterruptedException;
import java.util.*;
import com.ibm.ode.pkg.pkgMvs.service.B390CommandException;
import com.ibm.ode.pkg.pkgMvs.service.B390APARInfo;
import com.ibm.ode.pkg.pkgMvs.service.B390Release;

/**
 * Main driver for MVS IPP/Service gather and packaging.
 */
public class RunMvsPkg
{
  public static void main(String[] args)
  {
    // Validate PKG_CLASS
    if ((MvsProperties.pkgClass == null) ||
        (!MvsProperties.pkgClass.equalsIgnoreCase("ipp")))
    {
      System.err.println(
         "The PKG_CLASS variable must be set to \"IPP\"");
      System.exit(1);
    }

    // Validate PKG_EVENT
    if ((MvsProperties.pkgEvent == null) ||
        (!MvsProperties.pkgEvent.equalsIgnoreCase("gather")
         && !MvsProperties.pkgEvent.equalsIgnoreCase("package")))
    {
      System.err.println(
          "The PKG_EVENT variable must be set to \"gather\" or \"package\"");
      System.exit(1);
    }

    // make sure all required properties are set
    if (!MvsProperties.checkValues())
    {
      System.exit(1);
    }

    int exitValue = 0;
    exitValue = doMvsWork( MvsProperties.pkgEvent, MvsProperties.pkgClass);
    System.exit(exitValue);

  } //main()

  /**
   * Read package control file, call the methods for generating jcl,
   * submitting & monitoring job(s) and printing job output for IPP
   * gather and package actions and for service gather action.
   *
   * @param pkgAction the package action: gather or package
   * @param pkgClass the package class: ipp or sp
   * @return  int value to exit with
   **/
  private static int doMvsWork( String pkgAction, String pkgClass )
  {
    String packageControlFile = MvsProperties.pkgControlDir + "pcd.mvs";

    MvsControlFile controlFile;
    ArrayList jobArray = new ArrayList();
    ArrayList linkEditJobArray = new ArrayList();
    ArrayList runnableLinkEditJobArray = new ArrayList();
    ArrayList abortedLinkEditJobArray = new ArrayList();

    int errorCode = 0;
    int linkEditErrorCode = 0;
    int index = 0;
    long waitTime;

    ListIterator ai;
    try
    {
      // open the control file
      controlFile = new MvsControlFile(packageControlFile);
      System.out.println("Package control file is " + packageControlFile);

      // create reference to MvsValidation object to prevent it from being gc'd
      MvsValidation validation = new MvsValidation();

      // JCL generation phase
      generateIppJcl(pkgAction, controlFile, jobArray, linkEditJobArray);

      // allow objects to be finalized/gc'ed
      controlFile = null;
      validation  = null;

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
      // If the pkgAction is GATHER, jobArray contains the jobs performing
      // only the GATHER action, all the jobs performing LINKEDIT will be
      // stored in linkEditJobArray

      // If an exception occured in executeAndManagementJobs, return 1
      if (executeAndManageJobs( jobArray, waitTime ) == 1)
        return 1;

      errorCode = scanJobsAndSetStatus( jobArray );

      // At this point all the jobs in jobArray have already been run
      // We will run only those LinkEdit jobs whose corresponding
      // GATHER steps have completed successfully
      // Now, look at each job in linkEditJobArray and put it in
      // runnableLinkEditJobArray if the status of the corresponding job
      // in jobArray is neither ERROR nor FAILED else put it in
      // abortedLinkEditJobArray
      // Finally, run all the jobs in runnableLinkEditJobArray by submitting
      // it to executeAndManagementJobs
      ai = linkEditJobArray.listIterator();
      while (ai.hasNext())
      {
        MvsJobInfo jobInfo = (MvsJobInfo) ai.next();
        MvsJobInfo matchingJob = getMatchingJobFromJobArray( jobInfo,jobArray );
        if (matchingJob != null)
        {
          //check its status
          int status = matchingJob.getStatus();
          if ((status != MvsJobInfo.ERROR) && (status != MvsJobInfo.FAILED))
          {
            // This LINKEDIT job can be run as the corresponding GATHER job
            // has run successfully
            runnableLinkEditJobArray.add( jobInfo );
          }
          else
          {
            // This LINKEDIT job cannot be run as the corresponding GATHER job
            // has failed or caused an error
            // store this information to be displayed later
            abortedLinkEditJobArray.add( jobInfo );
            abortedLinkEditJobArray.add( matchingJob );
          }
        }
      }
      for (index = 0; index < abortedLinkEditJobArray.size()-1; index++)
      {
        System.out.println("Warning: " +
            ((MvsJobInfo)abortedLinkEditJobArray.get(index)).getJclFileName() +
             " will not be submitted as job " +
            ((MvsJobInfo)abortedLinkEditJobArray.get(index+1)).getJobNumber() +
            " failed");
        index++;
      }

      // submit the LINKEDIT jobs in runnableLinkEditJobArray if it is not empty
      // If an exception occured in executeAndManageJobs, return 1
      if (!runnableLinkEditJobArray.isEmpty() &&
          (executeAndManageJobs( runnableLinkEditJobArray, waitTime ) == 1))
        return 1;
      linkEditErrorCode = scanJobsAndSetStatus( runnableLinkEditJobArray );
      System.out.println("Ending job management phase");
      System.out.println();

      System.out.println("Execution summary for all jobs:");
      System.out.println();
      printJobExecutionSummary(jobArray);
      printJobExecutionSummary(runnableLinkEditJobArray);
      System.out.println();
      System.out.println("Output of all jobs:");
      System.out.println();
      printJobOutput(jobArray);
      printJobOutput(runnableLinkEditJobArray);
    }
    catch (MvsPkgError e)
    {
      System.err.println(e.getMessage());
      return 1;
    }
    if ((errorCode == 1) || (linkEditErrorCode == 1))
      return 1;
    else
      return 0;
  }

  /**
   * Generate JCL for IPP gather/package actions
   * A separate job will be generated for each package in the control file.
   *
   * @param pkgAction the package action: gather or package
   * @param controlFile the control file to be read
   * @param jobArray An array to be filled with jobs with each job
   *                 corresponding to a jcl created
   *                 If the pkgAction is gather, and a package contains both
   *                 the GATHER and LINKEDIT steps, then this array will
   *                 contain the jobs performing only the GATHER action
   * @param linkEditJobArray An array to be filled with LINKEDIT jobs with each job
   *                    corresponding to a linkEdit jcl created
   * @exception MvsPkgError if FileNotFoundException or I/O error occurs
   **/
  private static void generateIppJcl(String pkgAction,
                                     MvsControlFile controlFile,
                                     ArrayList jobArray,
                                     ArrayList linkEditJobArray)
    throws MvsPkgError
  {

    // read each package from the control file and generate the JCL
    MvsPackageData pkgData;
    while ( (pkgData = controlFile.nextPackage()) != null )
    {

      MvsJclGenerator jclGen = new MvsJclGenerator(pkgData,
                                                   controlFile.getPkgNum());
      MvsJobInfo jobInfo = null;
      MvsJobInfo linkJobInfo = null;

      if (pkgAction.equalsIgnoreCase("gather"))
      {
        jobInfo = jclGen.generateIppGather();
        //If this package contains LINKEDITING, generate a different jcl
        //for this step and add it to a different job array
        //Both the GATHER and LINKEDIT jobs will have the same name though
        // they will have different job numbers
        // This common name will serve to co-relate them
        linkJobInfo = jclGen.generateIppLinkEdit();
      }
      else
      {
        jobInfo = jclGen.generateIppPackage();
      }
      jobArray.add(jobInfo);
      if (linkJobInfo != null)
        linkEditJobArray.add(linkJobInfo);

      System.out.println();

      // force garbage collection (package metadata objects
      // with many parts can consume a lot of storage)
      pkgData = null;
      jclGen  = null;
      System.gc();

    } // while

    return;

  } // generateIppJcl()

  /**
   * Submit all the jobs in the jobArray and retreive their output after
   * the time interval specified by waitTime.
   * @param ArrayList jobArray, long waitTime
   * jobArray - An array of jobs to be run
   * waitTime - A long value which specifies the time interval for
   *            checking the job status after it is submitted
   * @return int 0 or 1
   **/
  public static int executeAndManageJobs(ArrayList jobArray, long waitTime)
  {
    // Call MvsJobManager every PKG_MVS_JOBMONITOR_TIME minutes
    try
    {
      MvsJobManager jobMgr = new MvsJobManager(jobArray);

      waitTime = waitTime * 60000;    // multiply by # of millisec in a second
                                    // to get correct sleep time
      boolean done = false;
      while (!done)
      {
        // call job manager to submit/check/retrieve output as necessary
        jobMgr.manageJobs();

        // go thru list of jobs and get status - if any have status of
        // DEFINED or SUBMITTED, then we are not done managing jobs
        done = true;
        ListIterator ai = jobArray.listIterator();
        while (ai.hasNext())
        {
          MvsJobInfo jobInfo = (MvsJobInfo) ai.next();

          int jobStatus = jobInfo.getStatus();
          if (jobStatus == MvsJobInfo.DEFINED ||
              jobStatus == MvsJobInfo.SUBMITTED )
          {
            done = false;     // there are still running jobs
          }
          else if (jobStatus == MvsJobInfo.OUTPUT)
          {
            // we retreived the job output - check it for completion
            try
            {
              MvsJobOutputScanner.getReturnCodes(jobInfo);
              MvsJobOutputScanner.setErrorMessages(jobInfo);
              jobInfo.setStatus(MvsJobInfo.CHECKED);
            }
            catch (MvsPkgError e)
            {
              System.err.println("Failure trying to check output of job " +
                                 jobInfo.getJobNumber() + ":");
              System.err.println(e.getMessage());
              jobInfo.setStatus(MvsJobInfo.ERROR);
            }
          }
        } //while

        System.out.println();

        // if there are more jobs to be managed, sleep for a while....
        if (!done)
        {
          try
          {
            Thread.sleep(waitTime);
          }
          catch (InterruptedException e)
          {
          }
        }
      } //while
    }
    catch( Exception ex )
    {
      System.err.println("An error occurred while managing jobs. The exception message is as follows:");
      System.err.println(ex.getLocalizedMessage());
      return 1;
    }
    return 0;
  } //executeAndManagementJobs

  /**
   * Searches in an array of jobs for a particular job and returns it.
   * The name of the job is used as the matching criteria.
   * @param MvsJobInfo targetJob, ArrayList jobArray
   * targetJob - The job whose name is used in the search criteria
   * jobArray - The array of jobs used for searching
   * @return MvsJobInfo or null
   **/
  private static MvsJobInfo getMatchingJobFromJobArray( MvsJobInfo targetJob,
                                                         ArrayList jobArray )
  {
    ListIterator ai = jobArray.listIterator();
    String jobName = targetJob.getJobName();
    while (ai.hasNext())
    {
      MvsJobInfo job = (MvsJobInfo)ai.next();
      if(job.getJobName().equals(jobName))
      {
        return job;
      }
    }
    return null;
  }

  /**
   * Scans all the jobs in an array to find their completion status and
   * returns the appropriate error code. Also, sets the status of the job to
   * FAILED if the job did not successfully run.
   * @param ArrayList jobArray
   * jobArray - An array of jobs
   * @return int 1 if any of the jobs didn't run successfully and 0 otherwise
   **/
  public static int scanJobsAndSetStatus( ArrayList jobArray )
  {
    int exitValue = 0;
    ListIterator ai = jobArray.listIterator();
    while (ai.hasNext())
    {
      MvsJobInfo jobInfo = (MvsJobInfo) ai.next();

      if (jobInfo.getStatus() == MvsJobInfo.ERROR)
      {
        exitValue = 1;
      }
      else   // status == CHECKED
      {
        if (!jobInfo.isJobSuccessful())
        {
          exitValue = 1;
          jobInfo.setStatus( MvsJobInfo.FAILED );
        }
      }
    }
    return exitValue;
  }

  /**
   * Print execution summary of all the jobs in the job array.
   *
   * @param ArrayList jobArray
   * jobArray - An array of jobs
   * @return void
   **/
  public static void printJobExecutionSummary( ArrayList jobArray )
  {
    MvsJobInfo jobInfo;
    ListIterator ai = jobArray.listIterator();

    while (ai.hasNext())
    {
      jobInfo = (MvsJobInfo) ai.next();

      // At this point, all jobs should be in CHECKED, ERROR or FAILED state.
      if (jobInfo.getStatus() == MvsJobInfo.ERROR)
      {
        System.out.println("Job " + jobInfo.getJobName() +
                           " was not successfully run");
        System.out.println("See previous error messages for details");
      }
      else   // status = CHECKED or FAILED
      {
        System.out.println(jobInfo);     // prints execution summary
        if (jobInfo.getStatus() == MvsJobInfo.FAILED)
        {
          System.out.println(">>> Job FAILED");
        }
        else
        {
          System.out.println(">>> Job was successful");
        }
        System.out.println();
      }
    } //while
  }

  /**
   * Print the output of all the jobs in an array whose output was
   * successfully retreived and deletes them.
   *
   * @param ArrayList jobArray An array of jobs
   */
  public static void printJobOutput( ArrayList jobArray )
  {
    MvsJobInfo jobInfo;
    ListIterator ai = jobArray.listIterator();

    while (ai.hasNext())
    {
      jobInfo = (MvsJobInfo) ai.next();
      if (jobInfo.getStatus() != MvsJobInfo.ERROR)
      {
        if (MvsProperties.isDisplayOutput())
        {
          MvsJobOutputScanner.printJobOutput(jobInfo);
        }
        if (MvsProperties.isSaveOutputFile())
        {
          System.out.println("Saving job output file at: "
                             + jobInfo.getJobOutputFileName());
        }
        else
        {
          File f = new File(jobInfo.getJobOutputFileName());
          f.delete();
        }
      }
    }
  }
}
