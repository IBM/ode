using namespace std;
using namespace std;
#define _ODE_BIN_MAKE_JOB_CPP_

#include "bin/make/job.hpp"

#include "base/binbase.hpp"
#include "lib/exceptn/exceptn.hpp"
#include "lib/exceptn/ioexcept.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/runcmd.hpp"
#include "lib/portable/native/sleep.h"
#include "lib/string/strcon.hpp"

#include "bin/make/linesrc.hpp"
#include "bin/make/mkcmdln.hpp"
#include "bin/make/targnode.hpp"
#include "bin/make/passinst.hpp"
#include "bin/make/makec.hpp"
#include "lib/intcmds/mkdep.hpp"
#include "lib/exceptn/rtimeexc.hpp"

#ifdef __WEBMAKE__
#include "lib/io/path.hpp"
#include "lib/io/file.hpp"
#include "bin/make/passnode.hpp"
#include "lib/portable/hashtabl.hpp"
#include <stdio.h>
#include <sys/stat.h>
#endif // __WEBMAKE__

String          Job::rmtserver;
Vector< Job * > Job::jobs_running;
Vector< Job * > Job::jobs_waiting;
int             Job::maxJobs      = 1;
int             Job::maxLocalJobs = 1;
int             Job::nJobs        = 0;
int             Job::nLocalJobs   = 0;

static unsigned long mksleepl = 0;

// how long to wait for a child to terminate (factor)
#define ODE_SLEEP_MSEC 100

// how long to wait for SIGINT when a child returns error
#define ODE_SIGNAL_WAIT_MSEC 500

Job::Job( PassInstance *pn, TargetNode *tn, int whereis )
  : pn( pn ), tn( tn ),
    var_eval( tn->getLocalVars(), pn->getSearchPath(), pn->getCwd(),
              pn->getEnvironVars(), true ),
    whereis( whereis ), state( NOTRUNNING ), cmdp( 0 ),
    running_cmd( 0 )
{
}

void Job::init( int maxJobs, int maxLocalJobs )
{
  const String *mksleepp = Env::getenv( Constants::MAKESLEEP );

  if (mksleepp == 0)           // top level mk
    Env::setenv( Constants::MAKESLEEP, String( ODE_SLEEP_MSEC*10 ), true );
  else                          // child mk
  {
    mksleepl = atol( mksleepp->toCharPtr() );
    if( mksleepl > ODE_SLEEP_MSEC*3 )
      mksleepl -= ODE_SLEEP_MSEC*3;
    else
      mksleepl = ODE_SLEEP_MSEC*3;
    Env::setenv( Constants::MAKESLEEP, String( mksleepl ), true );
  }

  setMaxJobs( maxJobs, maxLocalJobs );

  // If there are more total jobs then local jobs then we are to run remotely.
  if (maxJobs > maxLocalJobs)
  {
    rmtserver = MkCmdLine::getRmtServer();
    if (rmtserver.length() == 0)
    {
      const String *rmtserverptr = Env::getenv(
        StringConstants::ODEMAKE_RHOST_VAR );
      if (rmtserverptr == 0 || rmtserverptr->length() == 0)
        Make::quit( "Can't establish remote server name; use -R option.", 1 );
      rmtserver = *rmtserverptr;
    }
  }
}

void Job::setMaxJobs( int inMaxJobs, int inMaxLocalJobs )
{
  maxJobs = inMaxJobs;
  maxLocalJobs = inMaxLocalJobs;

  // Make sure we don't go below 1
  if (maxJobs < 1)
    maxJobs = 1;

  // We can't have more local jobs than total jobs.
  if (maxLocalJobs > maxJobs)
    maxLocalJobs = maxJobs;

  nJobs = 0;
  nLocalJobs = 0;
  if (MkCmdLine::dJobs())
  {
    Interface::printAlways( "Max total jobs = " + String( maxJobs ) );
    Interface::printAlways( "Max local jobs = " + String( maxLocalJobs ) );
  }
}

int Job::incnJobs( int where )
{
  // Wait for an available job.
  if (where == LOCAL)
  {
    if (nLocalJobs >= maxLocalJobs)
      return (UNAVAILABLE);

    nLocalJobs++;
    nJobs++;

    if (MkCmdLine::dJobs())
      print_njobs();

    return (LOCAL);
  }
  else if (where == REMOTE)
  {
    if (nJobs >= maxJobs)
      return (UNAVAILABLE);

    nJobs++;

    if (MkCmdLine::dJobs())
      print_njobs();

    return (REMOTE);
  }
  else // Either REMOTE or LOCAL
  {
    int dowhere = 0;
    if (nLocalJobs >= maxLocalJobs)
    {
      if (nJobs < maxJobs)
      {
        dowhere = REMOTE;
        nJobs++;
      }
      else
        return (UNAVAILABLE);
    }
    else
    {
      dowhere = LOCAL;
      nLocalJobs++;
      nJobs++;
    } // end if
    if (MkCmdLine::dJobs())
      print_njobs();

    return (dowhere);
  }
}

void Job::decnJobs(int where)
{
  // Free up a job
  if (where == LOCAL)
    nLocalJobs--;

  nJobs--;
  if (MkCmdLine::dJobs())
    print_njobs();
}

/**
  There need to be a "guaranteed" ordered of how the jobs_waiting queue is
  handled.  This will allow targets to be updated from left to right as they
  are defined in the makefile.  For example,
   a b: ; echo ${.TARGET}
   a : a1
   a1 ; echo ${.TARGET}
  We need to make sure 'a' is updated (and all its children) before 'b' is
  processed.  Of course, this is NOT true when NPROC > 1.  The above example
  should yield:
    mk -j1
    a1
    a
    b
 */
int Job::addJobsWaiting( PassInstance *pn, TargetNode *tn, boolean add_last )
{
  Job *newjob = new Job( pn, tn, UNAVAILABLE );
  if (add_last)
    jobs_waiting.addAsLast( newjob );
  else
    // Typically only add at the front of the queue when a dependent child
    // has been updated and its parent is now ready to be updated.
    //   See TargetNode::finish()
    jobs_waiting.addAsFirst( newjob );
  newjob->state = WAITING;

  // Format commands for running
  newjob->tn->parseCmds( newjob->var_eval, pn );
  newjob->tn->setFirstParsedCmd(); // reset parsed commands enum

  return (newjob->state);
}

void Job::start()
{
  boolean no_exec = (MkCmdLine::noExec() && !tn->isSet( Constants::OP_MAKE )
                                         && !tn->isSet( Constants::OP_PMAKE ));
  int last_state = GraphNode::UNMADE;

  // Get command and verify it isn't last
  cmdp = (Command *) tn->getNextParsedCmd();  // Explicity cast off the const
  if (!cmdp)
  {
    state = NOTRUNNING;
    return;
  }

  // We need to format and parse command now.
  last_state = cmdp->formatCommand( var_eval, current_cmdstr );

  if (last_state == GraphNode::ERROR)       // can't format the command
    Make::quit( 2 );

  while (cmdp)
  {
    if (no_exec)
    {
      if (MkCmdLine::dModTime()) tn->printModTimeInfo();
      if (last_state != GraphNode::UNMADE)
        Interface::printAlways( current_cmdstr );
    }
    else if (last_state != GraphNode::UNMADE)
    {
      break;
    }

    cmdp = (Command *) tn->getNextParsedCmd();
    if ( cmdp )
    {
      last_state = cmdp->formatCommand( var_eval, current_cmdstr );
      if (last_state == GraphNode::ERROR)       // can't format the command
        Make::quit( 2 );
    }
  }

  if (no_exec)
    state = DONE; // we've printed all commands, so we're DONE
  else
  {
    if (last_state != GraphNode::UNMADE)
    {
      if (MkCmdLine::dModTime()) tn->printModTimeInfo();
      exec();  // Run the command without waiting
    }
    else
      state = NOTRUNNING; //Make sure the state is properly set.
  }
}


/**
 * Create the remote command string.
 *
 * LIMITATIONS:
 *
 * Target platform and shell must be of the same type as the
 * local platform/shell.
 *
 * Untested/unsupported on Windows 95/98 and MVS/OE.
 *
**/
void Job::createRemoteCmd( String &buf ) const
{
  const String &ruser = StringConstants::getODEMAKE_RUSER();
  const String &dce_login = StringConstants::getODEMAKE_RDCELOGIN();
  const String &rmttmpdir = StringConstants::getODEMAKE_RTMPDIR();
  String tmpenvfile; // script containing envvars
  int shell_format;

  if (rmttmpdir == StringConstants::EMPTY_STRING)
    tmpenvfile = Path::tempFilename( pn->getCwd() );
  else
    tmpenvfile = Path::tempFilename( rmttmpdir );

  Path::userizeThis( tmpenvfile );

  if (StringConstants::getODEMAKE_RSHELLTYPE() ==
      StringConstants::ODEMAKE_RSHELLTYPE_CMD)
  {
    shell_format = Env::CMD_FORMAT;
    tmpenvfile += ".cmd"; // so it can be run as a script
  }
  else if (StringConstants::getODEMAKE_RSHELLTYPE() ==
      StringConstants::ODEMAKE_RSHELLTYPE_CSH)
    shell_format = Env::CSH_FORMAT;
  else if (StringConstants::getODEMAKE_RSHELLTYPE() ==
      StringConstants::ODEMAKE_RSHELLTYPE_KSH)
    shell_format = Env::KSH_FORMAT;
  else
    shell_format = Env::SH_FORMAT;

  // generate a file with all environment variables ready to go
  Env::createEnvDumpFile( tmpenvfile, *pn->getEnvironVarsArr(), shell_format );

  // the first command is run from the remote machine from the script
  String scrcmd;
  if (dce_login != StringConstants::EMPTY_STRING)
  {
    scrcmd += dce_login.dequote();
    scrcmd += " -e ";

    if (!StringConstants::getODEMAKE_RDCECMD_QUOTED().isEmpty())
      scrcmd += "\\\"";
    else
    {
#ifndef DEFAULT_SHELL_IS_CMD
      // get shell...guaranteed by SandboxConstants::init to exist
      scrcmd += *Env::getenv( StringConstants::ODEMAKE_SHELL_VAR );
      scrcmd += " \"";
#endif
    }
  }
#ifdef DEFAULT_SHELL_IS_CMD
  // change drives
  scrcmd += pn->getCwd().substring( STRING_FIRST_INDEX,
      STRING_FIRST_INDEX + 2 );
  scrcmd += " & ";
#endif
  scrcmd += "cd ";
  scrcmd += Path::userize( pn->getCwd() );
#ifdef DEFAULT_SHELL_IS_CMD
  scrcmd += " & ";
#else
  scrcmd += " ; ";
#endif
  scrcmd += StringConstants::getODEMAKE_RCMDPREPEND().dequote();
  scrcmd += " ";
  scrcmd += current_cmdstr;
  scrcmd += " ";
  scrcmd += StringConstants::getODEMAKE_RCMDAPPEND().dequote();
  if (dce_login != StringConstants::EMPTY_STRING)
  {
    if (!StringConstants::getODEMAKE_RDCECMD_QUOTED().isEmpty())
      scrcmd += "\\\"";
    else
    {
#ifndef DEFAULT_SHELL_IS_CMD
      scrcmd += "\"";
#endif
    }
  }

  try
  {
    fstream *scrfile = Path::openFileWriter( tmpenvfile, true, true );
    Path::putLine( *scrfile, scrcmd );
    Path::closeFileWriter( scrfile );
  }
  catch (IOException &e)
  {
    Make::error( "Failure while creating remote make script file:" );
    Make::quit( e.getMessage(), 1 );
  }

  // the second command is what is run locally to invoke the above script
  buf += StringConstants::getODEMAKE_RSHELL();
  buf += " ";
  buf += rmtserver;
  buf += " ";
  if (ruser != StringConstants::EMPTY_STRING)
  {
    buf += "-l ";
    buf += ruser;
    buf += " ";
  }
  if (shell_format == Env::KSH_FORMAT || shell_format == Env::SH_FORMAT)
    buf += ". ";
  else if (shell_format == Env::CSH_FORMAT)
    buf += "source ";
  buf += tmpenvfile;

#ifdef DEFAULT_SHELL_IS_CMD
  buf += " & del ";
#else
  buf += " ; rm ";
#endif
  buf += tmpenvfile;
}

void Job::exec()
  // throws (Exception())
{
  StringArray newcmd( 4 );

  if (!(cmdp->isSilent()))
  {
#ifdef __WEBMAKE__
    if (Interface::writeXML())
       Interface::printAlways( "<WebMakeBldScript>" );
#endif // __WEBMAKE__
    Interface::printAlways( current_cmdstr );
#ifdef __WEBMAKE__
    if (Interface::writeXML())
    {
       Interface::printAlways( "</WebMakeBldScript>" );
       Interface::printAlways( "<WebMakeBldScriptOutput>" );
    }
#endif // __WEBMAKE__
  }

  // Look for runtime commands first
  if (current_cmdstr.charAt(STRING_FIRST_INDEX) == '.' && doRuntimeCommand())
  {
    state = DONE;
    return;
  }
  if (whereis == REMOTE)
  {
    String rmtcmd;
    createRemoteCmd( rmtcmd );

    if (MkCmdLine::dJobs())
      Interface::printAlways( "Job: running remote command: " +
        rmtcmd );

    // Add a shell command processor to the command.
    RunSystemCommand::buildShellCmdArray( rmtcmd,
        StringConstants::EMPTY_STRING, &newcmd );
  }
  else // Running locally
  {
    if (MkCmdLine::dJobs())
      Interface::printAlways("Job: running local command: " + current_cmdstr );

#ifdef __WEBMAKE__
#ifdef __WEBDAV__
    if(cmdp->getCmdName().startsWith("put"))
       processPut();
    if(cmdp->getCmdName().startsWith("get"))
       processGet();
#endif // __WEBDAV__
#endif // __WEBMAKE__

    // Add a shell command processor to the command.
    RunSystemCommand::buildShellCmdArray( current_cmdstr,
        StringConstants::EMPTY_STRING, &newcmd );
  }

  delete running_cmd;
  if (pn->getEnvironVarsArr() != 0)
    running_cmd = new RunSystemCommand( newcmd, pn->getEnvironVarsCharStarArr(),
      pn->getEnvironVarsArr()->size(),
      true, true, false, false );
  else
    running_cmd = new RunSystemCommand( newcmd,
      true, true, false, false );

  // Before we start the command, make sure we haven't been interrupted
  if (Signal::isInterrupted() &&
      tn->nameOf() != Constants::DOT_INTERRUPT && tn->nameOf() != Constants::DOT_EXIT)
  {
    runInterruptHandler();
    state = NOTRUNNING;
  }
  else
  {
    running_cmd->start();
    state = RUNNING;
  }
}

boolean Job::doRuntimeCommand()
// throw Exception
{
  int i=0;
  static StringArray args;
  args.clear();
  current_cmdstr.split( MkDep::WORD_SPLIT_STRING, 0, &args );
  int  firstIdx = args.firstIndex();
  int  lastIdx  = args.lastIndex();

  if (current_cmdstr.startsWith( ".rmkdep" ))
  {
    const SetVars *const envs = 0;
    if (args[firstIdx] != ".rmkdep")
      return false;
    for (i = firstIdx + 1; i <= lastIdx; ++i)
      args[i].dequoteThis();

    if (!MkDep::run( &args, envs ))
      throw (RunTimeException( String(
          "invalid arguments: " ) + args.join( StringConstants::SPACE ) ));

    return true;
  }
  else if (current_cmdstr.startsWith( ".rcp" ) ||
           current_cmdstr.startsWith( ".rmv" ))
  {
    if ((args[firstIdx] != ".rcp") && (args[firstIdx] != ".rmv"))
      return false;

    if (lastIdx < (firstIdx + 2))
    {
      throw (RunTimeException( String(
          "Incorrect number of arguments: " ) + args.join( StringConstants::SPACE ) ));
    }

    for (i = firstIdx + 1; i <= lastIdx; ++i)
      args[i].dequoteThis();

    if (lastIdx > (firstIdx + 2))
    {
      // Last argument must be directory.
      if (!Path::isDirectory( args[lastIdx] ))
      {
        throw (RunTimeException( args[lastIdx] + " must be a directory." ));
      }
    }

    for (i = firstIdx + 1; i < lastIdx; ++i)
    {
      if (!Path::isFile( args[i] ))
      {
        throw (RunTimeException( args[i] + " must be a file." ));
      }

      if (!Path::copyFile( args[i], args[lastIdx], true, 0, true ) ||
          ((args[firstIdx] == ".rmv") && !Path::deletePath( args[i] )))
      {
        throw (RunTimeException( String(
            "Command returned error code: " ) + args.join( StringConstants::SPACE ) ));
      }
    }

    return true;
  }
  else if (current_cmdstr.startsWith( ".rrm" ))
  {
    if (args[firstIdx] != ".rrm")
      return false;

    if (lastIdx < (firstIdx + 1))
    {
      throw (RunTimeException( String(
          "Incorrect number of arguments: " ) + args.join( StringConstants::SPACE ) ));
    }

    for (i = firstIdx + 1; i <= lastIdx; ++i)
    {
      args[i].dequoteThis();
      if (!Path::deletePath( args[i] ))
        throw (RunTimeException( args[i] + " could not be deleted." ));
    }

    return true;
  }

  return false;
}

void Job::startJobs()
{
  Job *child_job;
  int wheretorun;
  while (!jobs_waiting.isEmpty())
  {
    child_job = *jobs_waiting.firstElement();

    wheretorun = ANYWHERE;
    if (child_job->tn->isSet( Constants::OP_NOREMOTE ))
      wheretorun = LOCAL;

    // Start the jobs if jobs are available, else exit
    int whereis = incnJobs( wheretorun );
    if (whereis != UNAVAILABLE)
    {
      jobs_waiting.removeElement( child_job );
      child_job->whereis = whereis;
      child_job->start();
      while (child_job->state != RUNNING && child_job->state != NOTRUNNING)
        child_job->start();
      if (child_job->state == NOTRUNNING)
      {
        child_job->tn->finish();
        decnJobs( child_job->whereis );
        continue;
      }

      jobs_running.addElement( child_job );
    }
    else
      break;
  } /* end while */
}

/**
 * Waits for a running job to finish.  It updates the Job* information
 * but does not start any new jobs.
 *
 * Returns:
 *   0     If mk has been interrupted or it can't locate the returned
 *         pid in the running_job list
 *   Job*  A valid pointer to the Job object that is associated with
 *         the command that finished.
 */
Job* Job::waitForJob()
{
  Job *child_job;
  ODEPROC_ID_TYPE child_pid;
  ODERET_CODE_TYPE return_code = 0;

  child_pid = RunSystemCommand::waitForAny( return_code );

  child_job = findJob( child_pid );

  // If we don't know about this job then just forget about it and on to
  // the next
  if (child_job == 0)
  {
    if (Signal::isInterrupted())
      runInterruptHandler();        // remove updated targets
    return (0);
  }

#ifdef __WEBMAKE__
// print end tag unless there was an error then print after error msg.
  if (Interface::writeXML() && !return_code)
     Interface::printAlways( "</WebMakeBldScriptOutput>" );
#endif // __WEBMAKE__

  child_job->state = DONE;

  // Update the child return code for running command
  if (child_pid == INVALID_PROCESS)
    child_job->running_cmd->setChildRC( ODEDEF_ERROR_CODE );
  else
    child_job->running_cmd->setChildRC( return_code );

#ifdef SLOW_SIGNALS
  /**
   * Some platforms have a signal latency problem.  The SIGINT
   * signal (et al.) may not reach the parent process before it
   * already processes the child's error and exits.
   * So, sleep a bit to give it a chance to arrive (unless
   * we're ignoring error exit codes).
  **/
  if (return_code != 0 && child_job->cmdp != 0 &&
       !(child_job->cmdp->isIgnoreErrors()) && !MkCmdLine::keepGoing())
    ODEsleep( ODE_SIGNAL_WAIT_MSEC );
#endif

  // Before evaluate the exit code and start more command, check to
  // see if we have been interrupted and we are not updating the
  // special targets that run when interrupted.
  if (Signal::isInterrupted() &&
      child_job->tn->nameOf() != Constants::DOT_INTERRUPT &&
      child_job->tn->nameOf() != Constants::DOT_EXIT)
  {
    runInterruptHandler();
    return (0);
  }
  return (child_job);
}

/**
 * This sits in a loop until both waiting and running job lists are empty.
 * It is possible that the waiting and running job lists will grow
 * because calls to TargetNode::finish() adds elements to the waiting
 * list.
 *
 * Returns:
 *   DONE    All command completed successfully
 *   ERROR   At least 1 command exited with a non-zero return code
 *
 */
int Job::doWaitingJobs()
{
  boolean haserror = false;
  int return_status = DONE;
  Job *child_job;
  ODERET_CODE_TYPE return_code = 0;

  startJobs();

  // Wait for any jobs to finish, when found see if it is the last command for
  // the target node.  If it is the last, pull it off the running list and get
  // a new one off the waiting list.
  while (true)
  {
    // Terminate the loop once the waiting and running list are empty.
    if (jobs_running.isEmpty() && jobs_waiting.isEmpty())
      break;

    child_job = waitForJob();

    // Reset in case of errors.
    return_status = DONE;

    if (child_job == 0)
    {
      // Before evaluate the exit code and start more command, check to
      // see if we have been interrupted and we are not updating the
      // special targets that run when interrupted.
      if (Signal::isInterrupted())
        return (DONE);
      else
        continue;
    }

    return_code = child_job->running_cmd->getExitCode();

    if (return_code != 0)
    {
      if (child_job->cmdp != 0 && child_job->cmdp->isIgnoreErrors())
      {
        Make::error( "*** Error code " + String( return_code ), true );
#ifdef __WEBMAKE__
        if (Interface::writeXML())
           Interface::printAlways( "</WebMakeBldScriptOutput>" );
#endif // __WEBMAKE__
      }
      else
      {
        Make::error( "*** Error code " + String( return_code ) );
#ifdef __WEBMAKE__
        if (Interface::writeXML())
           Interface::printAlways( "</WebMakeBldScriptOutput>" );
#endif // __WEBMAKE__

        haserror = true;
        return_status = Job::ERROR;

        // If there was an error and we don't want to keep going
        // the clear the waiting list
        if (!MkCmdLine::keepGoing())
        {
          // Do appropriate cleanup
          child_job->tn->finish( GraphNode::ERROR,
                                 child_job->cmdp->mfs );
          jobs_running.removeElement( child_job );
          decnJobs( child_job->whereis );
          delete child_job;
          jobs_waiting.removeAllElements();

          // We need to return abruptly
          return (Job::ERROR);
        }
        else
          // Set the state to NOTRUNNING and let it fall through
          // to be handled in the following loop.
          child_job->state = Job::NOTRUNNING;
      }
    }

    // Now that we have an available job, start the next command
    // if there is one.
    while (child_job->state == DONE)
        child_job->start();

    if (child_job && (child_job->state == NOTRUNNING))
    {
      // We need to communicate to the target node that there was
      // an error
      if (return_status == Job::ERROR)
        child_job->tn->finish( GraphNode::ERROR,
                               child_job->cmdp->mfs );
      else
        child_job->tn->finish();

#ifdef __WEBMAKE__
#ifdef __WEBDAV__
/*   if (child_job->running_cmd->getFullCmdString().indexOf(
         "java com.ibm.etools.webmake.make.GetFile"))
   {
     const String *TCTIME=Env::getenv("TCTime");
     if(TCTIME==0 || (*TCTIME).startsWith("1") )
     {
       String cmdstr="Fhcutil timestamp.tmp  -TCTime";
       int rc=system(cmdstr);
       if (rc!=0)
       exit(-1);
     }
   }*/
      child_job->printBOM();
#endif // __WEBDAV__
#endif // __WEBMAKE__

      jobs_running.removeElement( child_job );
      decnJobs( child_job->whereis );
      delete child_job;
      child_job = 0;

      startJobs();
    }
  }

  // Return with error if ANY of the commands failed
  if (haserror)
    return_status = Job::ERROR;
  return( return_status );
}

/**
 * This is only called by Make::quit() to wait for all child
 * processes to finish before mk exits.  No command are started
 * from this method.
 */
void Job::waitForRunningJobs()
{
  Job *child_job;

  // Loop until all child jobs have finished
  while (!jobs_running.isEmpty())
  {
    child_job = waitForJob();

    if (child_job == 0)
    {
      // If we've been interrupted then return since children should have
      // received the signal and exited
      if (Signal::isInterrupted())
        return;
      else
        continue;
    }

    if (child_job->running_cmd->getExitCode() != 0)
      Make::error( String( "*** Error code " ) +
        String( child_job->running_cmd->getExitCode() ) );

    // Do appropriate cleanup
    jobs_running.removeElement( child_job );
    decnJobs( child_job->whereis );
    delete child_job;
  } /* end while */
}

/**
**/
void Job::stopAll()
{
  int idx;
  static boolean donesleep = false;

  if( donesleep )   // already have been here
    return;

  if( MkCmdLine::dJobs() )
    Interface::printAlways( "Job: signal " + String(Signal::getSignalType()) +
      " received" );

  if( !jobs_running.isEmpty() )
  {
    ODEsleep( mksleepl );
    donesleep = true;
  }

  // Remove targets that are being updated targets
  for( idx  = jobs_running.firstIndex();
       idx <= jobs_running.lastIndex(); idx++ )
    if( (*jobs_running.elementAt( idx ))->tn->nameOf() != Constants::DOT_INTERRUPT
      && (*jobs_running.elementAt( idx ))->tn->nameOf() != Constants::DOT_EXIT )
    {
      jobs_running[idx]->running_cmd->stopChild();
      if( MkCmdLine::dJobs() )
        Interface::printAlways( "Job: removing target '" +
          (*jobs_running.elementAt( idx ))->tn->nameOf() + '\'' );
      (*jobs_running.elementAt( idx ))->tn->removeTargetFile();
    }

  jobs_running.removeAllElements();
  jobs_waiting.removeAllElements();

  // Don't wait for child jobs running.  The handler assumes that
  // the jobs will properly finish.

  // Reset the counts incase anyone dreams of trying to start a job after
  // mk has been interrupted
  nJobs      = 0;
  nLocalJobs = 0;

}

Job *Job::findJob( ODEPROC_ID_TYPE child_pid )
{
  VectorEnumeration< Job * > enum_running( &jobs_running );
  Job *return_job;
  while (enum_running.hasMoreElements())
  {
    return_job = *enum_running.nextElement();
    if (return_job->running_cmd->getChildPID() == child_pid)
      return (return_job);
  }
  // If we can't find the job, just return null and don't report error
  return (0);
}

/************************************************************************
 * Handle ctrl-C, ctrl-Break interrupts. Kill all running jobs.
 * Remove all being updated targets.
 * Don't register the handler with Signal since we need to do things
 * that use malloc and delete and can't leave the heap in an unknow state.
**/
void Job::runInterruptHandler()
{
  // If someone called it too soon
  if (!Signal::isInterrupted())
    return;

  stopAll();

  // Run .EXIT or .INTERRUPT
  Make::mk->runInterruptTargets();

  Make::quit( 1 );

}

void Job::print_njobs()
{
  if (MkCmdLine::dJobs())
    Interface::printAlways(
      "Job: nJobs = " + String(nJobs) + ", nLocalJobs = " +
      String( nLocalJobs ) );
}

#ifdef __WEBMAKE__
#ifdef __WEBDAV__
void Job::processPut()
{
   const String *server=Env::getenv("WEBDAV_SERVER");
   const String *root=Env::getenv("BUILDRESOURCE_ROOT");
   String urlString=(*server).substring(8,(*server).length()+1);
   int slash=urlString.indexOf('/');
   String hostAndPort=urlString.substring(STRING_FIRST_INDEX,slash);
   String prefix=urlString.substring(slash,urlString.length()+1);
   //get the full href for the webserver
   StringArray cmds;
   current_cmdstr.split(StringConstants::SPACE,UINT_MAX,&cmds);
   String fileName=cmds[ARRAY_FIRST_INDEX + 1];;
   String href;
   if(fileName.indexOf(*root)!=STRING_NOTFOUND)
      href=prefix+fileName.substring((*root).length()+1,fileName.length()+1);
   else
   {
      href=prefix+"/"+fileName;
      fileName=*root+"/"+fileName;
      fileName=Path::normalize(fileName);
   }
   href=Path::unixize(href);
   String webDAV_cmdstr="java com.ibm.etools.webmake.make.PutFile http://"+hostAndPort+href+"  "+fileName;
   current_cmdstr=webDAV_cmdstr;
}

void Job::processGet()
{
   const String *server=Env::getenv("WEBDAV_SERVER");
   const String *root=Env::getenv("BUILDRESOURCE_ROOT");
   String urlString=(*server).substring(8,(*server).length()+1);
   int slash=urlString.indexOf('/');
   String hostAndPort=urlString.substring(STRING_FIRST_INDEX,slash);
   String prefix=urlString.substring(slash,urlString.length()+1);
   StringArray cmds;
   current_cmdstr.split(StringConstants::SPACE,UINT_MAX,&cmds);
   String webDAV_cmdstr="java com.ibm.etools.webmake.make.GetFile -url ";
   //this file is used to change timestamp
   FILE *fp=fopen("timestamp.tmp","w+");
   //this file is used to pass url to the dav4j client
   FILE *urlfp=fopen("url.tmp","w+");
   const String *noExtract=Env::getenv("noExtract");
   String href;
   if(noExtract==0 || (*noExtract).startsWith("0"))
   {
   for(int i=cmds.firstIndex()+1;i<=cmds.lastIndex();i++)
   {
      StringArray NameArray(10);
      int index=ARRAY_FIRST_INDEX;
      String fileName;
      boolean truncateNeed=false;
      if ((cmds[i].toUpperCase()).indexOf((*root).toUpperCase())!=STRING_NOTFOUND)
      {
        truncateNeed=true;
        href=prefix+Path::DIR_SEPARATOR+cmds[i].substring((*root).length()+2);
      }
      else
        href=prefix+Path::DIR_SEPARATOR+cmds[i];
      href=Path::unixize(href);
      if(cmds[i].indexOf("*")!=STRING_NOTFOUND)
      {
         int firstStar=cmds[i].indexOf("*");
         int lastStar=cmds[i].lastIndexOf("*");
         int dot=cmds[i].indexOf(".");
         String path;
         String ext;
         //case 1 src\*.hpp
         if(firstStar==lastStar && firstStar<dot)
         {
            if (truncateNeed==true)
               path=Path::unixize(cmds[i].substring(STRING_FIRST_INDEX,firstStar).substring((*root).length()+1));
            else
               path=Path::unixize(cmds[i].substring(STRING_FIRST_INDEX,firstStar));
            ext=cmds[i].substring(dot+1);
         }
         //case 2 hello.*
         else if(firstStar==lastStar && dot<firstStar)
         {
            if (truncateNeed==true)
               path=Path::unixize(cmds[i].substring(STRING_FIRST_INDEX,dot).substring((*root).length()+1));
            else
               path=Path::unixize(cmds[i].substring(STRING_FIRST_INDEX,dot));
         }
         //case 3 *.*
         else if(firstStar<lastStar && firstStar<dot && dot<lastStar)
         {
            if (truncateNeed==true)
               path=Path::unixize(cmds[i].substring(STRING_FIRST_INDEX,firstStar).substring((*root).length()+1));
            else
               path=Path::unixize(cmds[i].substring(STRING_FIRST_INDEX,firstStar));
         }
         HashKeyEnumeration < String, WebResource* > my_enum(&Make::WebDavFilesTable);
         if(path.startsWith("/")!=true)
           path="/"+path;

         //now check in the repository and find out all the files which match
         //the wildcard specification
         while(my_enum.hasMoreElements())
         {
            String name=*my_enum.nextElement();
            int thisDot=name.indexOf(".");
            if(ext.length()!=0)
            {
               if(name.startsWith(prefix+path) &&
                  name.substring(thisDot+1).equals(ext))
                {
                NameArray.add(name.substring(prefix.length()+1));
                }
            }
            else
            {
               if(name.startsWith(prefix+path))
                  NameArray.add(name.substring(prefix.length()+1));
            }
         }
      }
      else
          NameArray.add(cmds[i]);
      //for all the files found in the get command, now compare the timestamp
      for(int j=NameArray.firstIndex();j<=NameArray.lastIndex();j++)
      {
         if ((NameArray[j].toUpperCase()).indexOf((*root).toUpperCase())!=STRING_NOTFOUND)
            fileName=NameArray[j];
         else if ((NameArray[j]).startsWith("/") || NameArray[j].startsWith("\\"))
            fileName=(*root)+NameArray[j];
         else
            fileName=(*root)+Path::DIR_SEPARATOR+NameArray[j];
         href=prefix+fileName.substring((*root).length()+1,fileName.length()+1)
;
         href=Path::unixize(href);
         fileName=Path::normalize(fileName);
         WebResource *const *f=0;
         if((f=(Make::WebDavFilesTable).get(href))!=0)
         {
            //compare the timeStamp,if timeStamp is different, write it to files
            //which will be passed to change timestamp
            int slash=fileName.lastIndexOf(Path::DIR_SEPARATOR);
            String localDir=fileName.substring(STRING_FIRST_INDEX,slash);
            struct stat statbuf;
            int rc;
            //prepare to get stat information
            fileName=Path::unixize(fileName);
            rc=stat(fileName.toCharPtr(),&statbuf);
            if(rc==-1||(rc==0 && (*f)->compareTimeStamp(statbuf.st_mtime)!=0))
            {
               //call webdav to extract files immediately
               //write the timestamp to change the file's timestamp to match server's
                fputs(fileName.toCharPtr(),fp);
                fputs("   ",fp);
                fputs((*f)->getLastModified().toCharPtr(),fp);
                fputs("\n",fp);
                fputs((String("http://")+hostAndPort+href+"\n").toCharPtr(),
                      urlfp);
                //if the server crashes and file is not extracted, then the flag
                //might not reflect the consistent state of the file
                (*f)->extracted=true;
            }
         }
      }
   }
   //use the wildcard method, so the dav4j client will process the urlfile
   //which contains only the files needed to be extracted
   webDAV_cmdstr+=" http://"+hostAndPort+"/*.*";
   webDAV_cmdstr+=" -rootDir "+(*root)+" -webdavServer "+(*server)+" -changeTS 1";
   current_cmdstr=webDAV_cmdstr;
   }
   fclose(fp);
   fclose(urlfp);

}
void Job::printBOM()
{
   const String *root=Env::getenv("BUILDRESOURCE_ROOT");
   GraphNode *childnd;
   Vector <GraphNode*> ordered_children, unordered_children;
   tn->orderChildren(unordered_children,ordered_children);
   VectorEnumeration <GraphNode * > enumSomeChildren( &unordered_children);
   const MakefileStatement *mfs=tn->defnmfs;
   String makefile=mfs->getPathname();
   int    index=makefile.indexOf(".");
   String bomFile=makefile.substring(STRING_FIRST_INDEX,index)+".bom";
   FILE *fp=fopen(bomFile.toCharPtr(),"a+");
   String build_top=Path::unixize(*root).toUpperCase();
   /*---get the directory for the compiler's searching include path------+
   |   rule 1: current directory                                         |
   |   rule 2: Directories specified with the /I option                  |
   |   rule 3: Directories specified in the INCLUDE environment variable |
   +--------------------------------------------------------------------*/
   String cwd=pn->getCwd();
   String Ipath=getCmdIncludePath();
   const String *include=Env::getenv("INCLUDE");
   String includeEnv=String(*include).replace(';','#');
   while(enumSomeChildren.hasMoreElements())
   {
     //need to get relative path for OUTPUT, MAKEFILE, INPUT
     if(tn->nameOf().toUpperCase().indexOf(build_top)!=STRING_NOTFOUND)
        fputs((String("BOM ")+tn->nameOf().substring((*root).length()+2)+
              "  {\n").toCharPtr(),fp);
     else
        fputs((String("BOM ")+tn->nameOf()+"  {\n").toCharPtr(),fp);
     if(mfs->getPathname().toUpperCase().indexOf(build_top)!=STRING_NOTFOUND)
         fputs((String("Makefile=")+
               mfs->getPathname().substring((*root).length()+2)+
               ";\n").toCharPtr(),fp);
     else
         fputs((String("Makefile=")+mfs->getPathname()+";\n").toCharPtr(),fp);
     fputs((String("LineNo=")+String(mfs->getLineNumber())+
           ";\n").toCharPtr(),fp);
     childnd= *enumSomeChildren.nextElement();
     if (childnd==0)
        continue;
     if(childnd->nameOf().toUpperCase().indexOf(build_top)!=STRING_NOTFOUND)
        fputs((String("Input=")+childnd->nameOf().substring((*root).length()+2)+
              ";\n").toCharPtr(),fp);
     else
        fputs((String("Input=")+childnd->nameOf()+";\n").toCharPtr(),fp);
     fputs("Cmd=",fp);
     Vector<Command> cmds = tn->getCmds();  // Explicity cast off the const
     for(int i=cmds.firstIndex();i<=cmds.lastIndex();i++)
     {
        String cmdstr=cmds[i].getCmdName();
        fputs(cmdstr.toCharPtr(),fp);
        if(i!=cmds.lastIndex())
          fputs("#",fp);
     }
     fputs(";\n",fp);
     fputs((String("Machine=")+PlatformConstants::CURRENT_MACHINE+
           ";\n").toCharPtr(),fp);
     fputs((String("BuildResourceRoot=")+(*root)+";\n").toCharPtr(),fp);
     fputs((String("Cwd=")+cwd+";\n").toCharPtr(),fp);
     fputs((String("Ipath=")+Ipath+";\n").toCharPtr(),fp);
     fputs((String("Include=")+includeEnv+";\n").toCharPtr(),fp);
     fputs("}\n",fp);
   }
   enumSomeChildren.setObject(&ordered_children);
   while(enumSomeChildren.hasMoreElements())
   {
     //need to get relative path for OUTPUT, MAKEFILE, INPUT
     if(tn->nameOf().toUpperCase().indexOf(build_top)!=STRING_NOTFOUND)
        fputs((String("BOM ")+tn->nameOf().substring((*root).length()+2)+
              "  {\n").toCharPtr(),fp);
     else
        fputs((String("BOM ")+tn->nameOf()+"  {\n").toCharPtr(),fp);
     if(mfs->getPathname().toUpperCase().indexOf(build_top)!=STRING_NOTFOUND)
         fputs((String("Makefile=")+
               mfs->getPathname().substring((*root).length()+2)+
               ";\n").toCharPtr(),fp);
     else
         fputs((String("Makefile=")+mfs->getPathname()+";\n").toCharPtr(),fp);
     fputs((String("LineNo=")+
           String(mfs->getLineNumber())+";\n").toCharPtr(),fp);
     childnd= *enumSomeChildren.nextElement();
     if (childnd==0)
        continue;
     if(childnd->nameOf().toUpperCase().indexOf(build_top)!=STRING_NOTFOUND)
        fputs((String("Input=")+childnd->nameOf().substring((*root).length()+2)+
              ";\n").toCharPtr(),fp);
     else
        fputs((String("Input=")+childnd->nameOf()+";\n").toCharPtr(),fp);
     fputs("Cmd=",fp);
     Vector<Command> cmds = tn->getCmds();  // Explicity cast off the const
     for(int i=cmds.firstIndex();i<=cmds.lastIndex();i++)
     {
        String cmdstr=cmds[i].getCmdName();
        fputs(cmdstr.toCharPtr(),fp);
        if(i!=cmds.lastIndex())
          fputs("#",fp);
     }
     fputs(";\n",fp);
     fputs((String("Machine=")+PlatformConstants::CURRENT_MACHINE+
           ";\n").toCharPtr(),fp);
     fputs((String("BuildResourceRoot=")+(*root)+";\n").toCharPtr(),fp);
     fputs((String("Cwd=")+cwd+";\n").toCharPtr(),fp);
     fputs((String("Ipath=")+Ipath+";\n").toCharPtr(),fp);
     fputs((String("Include=")+includeEnv+";\n").toCharPtr(),fp);
     fputs("}\n",fp);
   }
   fclose(fp);
}
String Job::getCmdIncludePath()
{
   StringArray buf;
   String Ipath;
   String cmdstr;
   Command *cmd;
   boolean be_silent = false;

   if(current_cmdstr.indexOf("/I")!=STRING_NOTFOUND|| current_cmdstr.indexOf("-I"))
   {
      current_cmdstr.split(StringConstants::SPACE,UINT_MAX,&buf);
      for(int i=buf.firstIndex()+1;i<=buf.lastIndex();i++)
      {
          String segment=buf[i];
          if(buf[i].startsWith("/I")!=STRING_NOTFOUND|| buf[i].startsWith("-I"))
             Ipath+=buf[i].substring(STRING_FIRST_INDEX+2)+"#";
       }

   }

   return Ipath;
}
#endif // __WEBDAV__
#endif // __WEBMAKE__
