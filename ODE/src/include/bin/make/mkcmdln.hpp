/**
 * MkCmdLine
 *
**/
#ifndef _ODE_BIN_MAKE_MKCMDLN_HPP_
#define _ODE_BIN_MAKE_MKCMDLN_HPP_

#include "base/odebase.hpp"
#include "lib/io/cmdline.hpp"
#include "lib/io/ui.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/portable/env.hpp"
#include "lib/string/setvars.hpp"
#include "lib/portable/vector.hpp"

#include "bin/make/constant.hpp"
#include "bin/make/integer.hpp"



#define BITFLAG_force_build    0X00000001
#define BITFLAG_find_dirs      0X00000002
#define BITFLAG_debugAll       0X00000004
#define BITFLAG_debuga         0X00000008
#define BITFLAG_debugc         0X00000010
#define BITFLAG_debugd         0X00000020
#define BITFLAG_debugg1        0X00000040
#define BITFLAG_debugg2        0X00000080
#define BITFLAG_debugi         0X00000100
#define BITFLAG_debugj         0X00000200
#define BITFLAG_debugm         0X00000400
#define BITFLAG_debugp         0X00000800
#define BITFLAG_debugs         0X00001000
#define BITFLAG_debugt         0X00002000
#define BITFLAG_debugv         0X00004000
#define BITFLAG_debugpv        0X00008000
#define BITFLAG_ignore_errors  0X00010000
#define BITFLAG_keep_going     0X00020000
#define BITFLAG_no_exec        0X00040000
#define BITFLAG_query          0X00080000
#define BITFLAG_no_echo        0X00100000
#define BITFLAG_touch_targs    0X00200000
#define BITFLAG_whitespace     0X00400000



class MkCmdLine : public CommandLine
{
  public:
    // constructor and destructor
    MkCmdLine( const StringArray &arguments, const Tool &program ) :
        CommandLine( &states, &qualvars, true, arguments, program ), tool( program )
        {
          MkCmdLine::cmdline = this;
        };

    ~MkCmdLine()
    {};

    // access functions
    inline static boolean findDirs();
    inline static boolean forceRebuild();
    inline static boolean envOverride();
    inline static const String &userMakefile();
    inline static const String &userBOMfile();
    inline static boolean ignoreErrors();
           static boolean reportUpToDate();
    inline static boolean touchTarget();
    inline static boolean whiteSpace();
    inline static const Vector< String > &getTgts();
    inline static const StringArray &getIncs();
    inline static const Vector< String > &getCommandLineVars();
    inline static const Vector< String > &getGlobalVars();
    inline static boolean keepGoing();
           static boolean notKeepGoing();
    inline static const String &getRmtServer();
    inline static boolean noExec();
    inline static boolean noEcho();
    inline static int getMaxJobs();
    inline static int getMaxLocalJobs();

    // The following are debug access functions
    inline static boolean dAll();
    inline static boolean dArch();
    inline static boolean dCond();
    inline static boolean dDirs();
    inline static boolean dGraph1();
    inline static boolean dGraph2();
    inline static boolean dJobs();
    inline static boolean dIncs();
    inline static boolean dModTime();
    inline static boolean dPatterns();
    inline static boolean dSuffs();
    inline static boolean dTargs();
    inline static boolean dVars();
    inline static boolean dpVars();

    // misc functions
    String  setStates( boolean frommakefile, const StringArray &args,
                       boolean isMakeconf, boolean isVarsThere );
    void    clearStates();
    static String   appendFlags( const StringArray &newflags,
                                 boolean isMakeconf, boolean isVarsThere );
    static String   getRecursiveFlags();
    static int      getArguments( StringArray &arguments );
    static void     restoreArguments( const StringArray &newArgs,
                                      const int newStates );

  private:

    static MkCmdLine *cmdline;
    const  Tool &tool;
    static const char *sts[];       // used for StringArray states
    static const StringArray states;

    static boolean S;               // -S
    static boolean find_dirs;       // -b
    static boolean force_build;     // -a
    static boolean env_override;    // -e
    static boolean ignore_errors;   // -i
    static boolean keep_going;      // -k
    static boolean no_exec;         // -n
    static boolean query;           // -q
    static boolean no_echo;         // -s
    static boolean touch_targs;     // -t
    static boolean whitespace;      // -w
    static boolean debugAll;        // -dA
    static boolean debuga;          // -da
    static boolean debugc;          // -dc
    static boolean debugd;          // -dd
    static boolean debugg1;         // -dg1
    static boolean debugg2;         // -dg2
    static boolean debugi;          // -di
    static boolean debugj;          // -dj
    static boolean debugm;          // -dm
    static boolean debugp;          // -dp
    static boolean debugs;          // -ds
    static boolean debugt;          // -dt
    static boolean debugv;          // -dv
    static boolean debugpv;         // -dV

    static const char *qvs[];       // used for StringArray qualvars
    static const StringArray qualvars;
    static String makefile;         // -f <makefile>
    static String bomfile;          // -B <bomfile>
    static String rmtserver;        // -R <rmtserver>
    static int total_jobs;          // -j and NPROC <total_jobs>
    static int local_jobs;          // -L and NPROC <local_jobs>
    static int j_value;             // -j <total_jobs>
    static int L_value;             // -L <local_jobs>
    static int opt_level;           // -O <opt level>
    static StringArray incs;        // -I
    static Vector< String > tgts;   // everything left over
    static Vector< String > cmdline_vars; // var=val
    static Vector< String > global_vars;  // -D
};

inline boolean MkCmdLine::findDirs()
{
  return find_dirs;
}

inline boolean MkCmdLine::forceRebuild()
{
  return force_build;
}

inline boolean MkCmdLine::envOverride()
{
  return env_override;
}

inline const String &MkCmdLine::userMakefile()
{
  return makefile;
}

/************************************************
  * --- inline const String &MkCmdLine::userBOMfile ---
  *
  ************************************************/
inline const String &MkCmdLine::userBOMfile()
{
  return bomfile;
}

inline boolean MkCmdLine::ignoreErrors()
{
  return ignore_errors;
}

inline  const Vector< String > &MkCmdLine::getTgts()
{
  return tgts;
}

inline  const StringArray &MkCmdLine::getIncs()
{
  return incs;
}

inline  const Vector< String > &MkCmdLine::getCommandLineVars()
{
  return cmdline_vars;
}

inline  const Vector< String > &MkCmdLine::getGlobalVars()
{
  return global_vars;
}

inline boolean MkCmdLine::keepGoing()
{
  return keep_going;
}

inline boolean MkCmdLine::touchTarget()
{
  return touch_targs;
}

inline boolean MkCmdLine::whiteSpace()
{
  return whitespace;
}

inline  const String &MkCmdLine::getRmtServer()
{
  return rmtserver;
}

inline boolean MkCmdLine::noExec()
{
  return no_exec;
}

inline boolean MkCmdLine::noEcho()
{
  return no_echo;
}

inline int MkCmdLine::getMaxJobs()
{
  return total_jobs;
}

inline int MkCmdLine::getMaxLocalJobs()
{
  return local_jobs;
}

inline boolean MkCmdLine::dAll()
{
  return debugAll;
}

inline boolean MkCmdLine::dArch()
{
  return debuga;
}

inline boolean MkCmdLine::dCond()
{
  return debugc;
}

inline boolean MkCmdLine::dDirs()
{
  return debugd;
}

inline boolean MkCmdLine::dGraph1()
{
  return debugg1;
}

inline boolean MkCmdLine::dGraph2()
{
  return debugg2;
}

inline boolean MkCmdLine::dJobs()
{
  return debugj;
}

inline boolean MkCmdLine::dIncs()
{
  return debugi;
}

inline boolean MkCmdLine::dModTime()
{
  return debugm;
}

inline boolean MkCmdLine::dPatterns()
{
  return debugp;
}

inline boolean MkCmdLine::dSuffs()
{
  return debugs;
}

inline boolean MkCmdLine::dTargs()
{
  return debugt;
}

inline boolean MkCmdLine::dVars()
{
  return debugv;
}
inline boolean MkCmdLine::dpVars()
{
  return debugpv;
}
#endif //_ODE_BIN_MAKE_MKCMDLN_HPP_

