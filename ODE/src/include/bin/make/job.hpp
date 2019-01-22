#ifndef _ODE_BIN_MAKE_JOB_HPP_
#define _ODE_BIN_MAKE_JOB_HPP_

#include <base/odebase.hpp>
#include "lib/portable/runcmd.hpp"
#include "lib/string/setvars.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/variable.hpp"

#include "bin/make/command.hpp"
#include "bin/make/passinst.hpp"
#include "bin/make/targnode.hpp"

class Job
{
  public:
    enum
    {
      ANYWHERE    = 0,
      REMOTE      = 1,
      LOCAL       = 2,
      UNAVAILABLE = 3
    };

    enum
    {
      NOTRUNNING  = 0,
      RUNNING     = 1,
      WAITING     = 2,
      DONE        = 3,
      ERROR       = 4
    };

    static void init( int maxJobs, int maxLocalJobs );

    static void setMaxJobs( int inMaxJobs, int inMaxLocalJobs );
    static int  execCmds( PassInstance *pn, TargetNode *tn );
    static int  addJobsWaiting( PassInstance *pn, TargetNode *tn, 
                                boolean add_last = true );
    static int  doWaitingJobs();
    static void waitForRunningJobs();
    static void runInterruptHandler();
    inline static int getMaxJobs();
    inline static int getMaxLocalJobs();

  private:
    static String  rmtserver;
    static Vector< Job * > jobs_running;
    static Vector< Job * > jobs_waiting;
    static int     maxJobs;
    static int     maxLocalJobs;
    static int     nJobs;
    static int     nLocalJobs;

    PassInstance  *pn;
    TargetNode    *tn;
    Variable       var_eval;
    int            whereis;
    String         current_cmdstr;
    int            state;
    Command       *cmdp;
    RunSystemCommand *running_cmd;
    VectorEnumeration< Command > enum_cmds;

    Job( PassInstance *pn, TargetNode *tn, int whereis );
    ~Job()
    {
      delete running_cmd;
    }
    static void startJobs();    // Take off waiting list and active jobs
    static void stopAll();      // Stop all running jobs and remove all being
                                // updated targets

    static int  incnJobs( int where );
    static void decnJobs( int where );
    static void print_njobs();
    static Job *findJob( ODEPROC_ID_TYPE child_pid );
    static Job *waitForJob();

    void exec();
    void start();
    void startNextCmd();
    void createRemoteCmd( String &buf ) const;
    boolean doRuntimeCommand();

#ifdef __WEBMAKE__
#ifdef __WEBDAV__
    void processPut();
    void processGet();
    void printBOM();
    String getCmdIncludePath();
#endif // __WEBDAV__
#endif // __WEBMAKE__
};

inline int Job::getMaxJobs()
{
  return (maxJobs);
}
inline int Job::getMaxLocalJobs()
{
  return (maxLocalJobs);
}

#endif //_ODE_BIN_MAKE_JOB_HPP_
