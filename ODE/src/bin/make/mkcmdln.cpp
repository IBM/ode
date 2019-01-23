/**
 * MkCmdLine
 *
 */

using namespace std;
#define _ODE_BIN_MAKE_MKCMDLINE_CPP_

#include "base/binbase.hpp"
#include "bin/make/mkcmdln.hpp"
#include "bin/make/job.hpp"
#include "bin/make/makec.hpp"

MkCmdLine *MkCmdLine::cmdline = 0;
const char *MkCmdLine::sts[] = {"-a","-b","-e","-i","-k","-n","-q",
    "-s","-t","-v","-w","-S","-?","-usage","-version","-rev", 0};

const StringArray MkCmdLine::states( MkCmdLine::sts );
boolean MkCmdLine::S = false;               // -S
boolean MkCmdLine::force_build = false;     // -a
boolean MkCmdLine::find_dirs = false;       // -b
boolean MkCmdLine::env_override = false;    // -e
boolean MkCmdLine::ignore_errors = false;   // -i
boolean MkCmdLine::keep_going = false;      // -k
boolean MkCmdLine::no_exec = false;         // -n
boolean MkCmdLine::query = false;           // -q
boolean MkCmdLine::no_echo = false;         // -s
boolean MkCmdLine::touch_targs = false;     // -t
boolean MkCmdLine::whitespace = false;      // -w
boolean MkCmdLine::debugAll = false;        // -dA
boolean MkCmdLine::debuga = false;          // -da
boolean MkCmdLine::debugc = false;          // -dc
boolean MkCmdLine::debugd = false;          // -dd
boolean MkCmdLine::debugg1 = false;         // -dg1
boolean MkCmdLine::debugg2 = false;         // -dg2
boolean MkCmdLine::debugi = false;          // -di
boolean MkCmdLine::debugj = false;          // -dj
boolean MkCmdLine::debugm = false;          // -dm
boolean MkCmdLine::debugp = false;          // -dp
boolean MkCmdLine::debugs = false;          // -ds
boolean MkCmdLine::debugt = false;          // -dt
boolean MkCmdLine::debugv = false;          // -dv
boolean MkCmdLine::debugpv = false;         // -dV

const char *MkCmdLine::qvs[] = {"-d","-f","-j","-B","-D","-I","-L",
                                "-O", "-R", 0};
const StringArray MkCmdLine::qualvars( MkCmdLine::qvs );
String MkCmdLine::bomfile;                   // -B <bomfile>
String MkCmdLine::makefile;                  // -f <makefile>
String MkCmdLine::rmtserver;                 // -R <rmtserver>
int MkCmdLine::total_jobs=1;                 // -j and NPROC <total_jobs>
int MkCmdLine::local_jobs=1;                 // -L and NPROC <local_jobs>
int MkCmdLine::j_value = 0;                  // -j <total_jobs>
int MkCmdLine::L_value = 0;                  // -L <local_jobs>
int MkCmdLine::opt_level = -1;               // -O <opt_level>
StringArray MkCmdLine::incs;                 // -I
Vector< String > MkCmdLine::tgts;            // everything left over
Vector< String > MkCmdLine::cmdline_vars;    // var=val
Vector< String > MkCmdLine::global_vars;     // -D

/************************************************************************
 **/
boolean MkCmdLine::reportUpToDate()
{
  Make::warning("The -q option is not supported in this version of mk");
  return query;
}

/************************************************************************
 **/
boolean MkCmdLine::notKeepGoing()
{
  Make::warning("The -S option is not supported in this version of mk");
  return S;
}

/************************************************************************
 * set states
**/
String MkCmdLine::setStates( boolean frommakefile,
    const StringArray &args, boolean isMakeconf, boolean isVarsThere )
{
  // Just in case we need to, reset the messaging state to normal.
  //
  Interface::setState("normal");

  if (!cmdline->checkSyntax(args) ||
       cmdline->isState("-usage", args) ||
       cmdline->isState("-?", args))
  {
    tool.printUsage();
    Make::quit(0);
  }
  else if ( cmdline->isState("-version", args) ||
            cmdline->isState("-rev", args) ||
            cmdline->isState("-v", args) )
  {
    printVersion( Constants::MAKE_NAME );
    Make::quit(0);
  }
  else
  {
    StringArray strarr;

    // Set all state type variables
    //  We need to test the value of the flags so subsequent calls don't
    // reset them to false.
    //
    if (!force_build)
      force_build = cmdline->isState( "-a", args );
    if (!find_dirs)
      find_dirs = cmdline->isState( "-b", args );
    if (!env_override)
    {
      env_override = cmdline->isState( "-e", args );
      if (env_override && frommakefile && !isMakeconf)
        return String( "Special target .MAKEFLAGS can be used to "
            "override macros by environment variables only in "
            "Makeconf (-e)." );
    }
    if (!ignore_errors)
      ignore_errors = cmdline->isState( "-i", args );
    if (!keep_going)
      keep_going = cmdline->isState( "-k", args );
    if ( !no_exec )
      no_exec = cmdline->isState("-n", args);
    if ( !query )
      query = cmdline->isState("-q", args);
    if ( !touch_targs )
      touch_targs = cmdline->isState("-t", args);
    if ( !whitespace )
      whitespace = cmdline->isState("-w", args);
    if ( !no_echo )
      no_echo = cmdline->isState("-s", args);

    Interface::setState("normal");   // by default, -dA will do it verbose

    S = cmdline->isState("-S", args);

    if ( query )
      Make::warning("The -q option is not supported in this version of mk");
    if ( S )
      Make::warning("The -S option is not supported in this version of mk");

    // Determine debugging
    //
    strarr.clear();
    cmdline->getQualifiedVariables("-d", args, &strarr);
    if ( strarr.length() != 0 )
    {
      String line;
      int cidx, cline;
      for ( cline=strarr.firstIndex(); cline <= strarr.lastIndex(); cline++ )
      {
        line = strarr[cline];
        for ( cidx=line.firstIndex(); cidx <= line.lastIndex(); cidx++)
        {
          switch (line.charAt(cidx))
          {
            case 'A':
              debuga = true;
              debugc = true;
              debugd = true;
              debugg1 = false; // these options are not included into -dA case
              debugg2 = false; // because it makes lots of duplicated output
              debugi = true;
              debugj = true;
              debugm = true;
              debugp = true;
              debugs = true;
              debugt = true;
              debugv = true;
              debugpv = true;
              Interface::print( Constants::MAKE_NAME +
                ": All debugging options on, -dg1/-dg2 "
                "should be used separately");
              Interface::setState( "verbose" );
              Interface::print( Constants::MAKE_NAME +
                ": Setting state to verbose");
              break;
            case 'a':
              debuga = true;
              break;
            case 'c':
              debugc = true;
              break;
            case 'd':
              debugd = true;
              break;
            case 'g':
            {
              if ( cidx == line.lastIndex() )
              {
                Make::error("Unknown debug option -g");
                tool.printUsage();
                Make::quit(1);
              }
              switch (line.charAt(++cidx))
              {
                case '1':
                  debugg1 = true;
                  break;
                case '2':
                  debugg2 = true;
                  break;
                default:
                  Make::error("Unknown debug option -g"+line.charAt(cidx));
                  tool.printUsage();
                  Make::quit(1);
              } /* end switch */
              break;
            }
            case 'i':
              debugi = true;
              break;
            case 'j':
              debugj = true;
              break;
            case 'm':
              debugm = true;
              break;
            case 'p':
              debugp = true;
              break;
            case 's':
              debugs = true;
              break;
            case 't':
              debugt = true;
              break;
            case 'v':
              debugv = true;
              break;
            case 'V':
              debugpv = true;
              debugv = true;
              break;
            default:
              Make::error("Unknown debug option "+line);
              tool.printUsage();
              Make::quit(1);
          } /* end switch */
        } /* end for */
      } /* end for */
    } /* end if */

    // Determine job limitations
    //
    strarr.clear();
    cmdline->getQualifiedVariables( "-j", args, &strarr );
    if (strarr.length() != 0)
    {
      if (frommakefile)
        return String( "Special target .MAKEFLAGS cannot be used to set "
            "max jobs (-j)." );
      try
      {
        local_jobs = total_jobs = Integer::parseInt(strarr[strarr.lastIndex()]);
        if (local_jobs == 0) local_jobs = total_jobs = 1;
        // Preserve j flag for recursive mk's
        j_value = local_jobs;

        if ( dJobs() )
          Interface::printAlways( "Setting Maximum Total Jobs=" +
            String( total_jobs ) );
      }
      catch ( MalformedVariable &e )
      {
        Make::error("argument \""+strarr[strarr.lastIndex()]+
                    "\" to -j flag was not an integer");
        tool.printUsage();
        Make::quit(1);
      }
    }
    else
    {
      // Look for NPROC.
      //
      const String *nproc=Env::getenv("NPROC");
      if ( nproc != 0 && !frommakefile )
      {
        try
        {
          local_jobs = total_jobs = Integer::parseInt(*nproc);
          if ( dJobs() )
            Interface::printAlways( "Setting Maximum Jobs=" +
              String( total_jobs ) );
        }
        catch ( MalformedVariable &e )
        {
          Make::quit("NPROC value \""+*nproc+
                     "\" was not an integer", 1);
        }
      }
    }

    strarr.clear();
    cmdline->getQualifiedVariables("-L", args, &strarr);
    if ( strarr.length() != 0 )
    {
      if ( frommakefile )
        return String( "Special target .MAKEFLAGS cannot be used to set "
            "local max jobs (-L)." );
      try
      {
        local_jobs = Integer::parseInt(strarr[strarr.lastIndex()]);

        // Preserve L flag for recursive mk's
        L_value = local_jobs;

        if (dJobs())
          Interface::printAlways( "Setting Maximum Local Jobs=" +
            String( local_jobs ) );
      }
      catch ( MalformedVariable &e )
      {
        Make::error("argument \""+strarr[strarr.lastIndex()]+
                    "\" to -L flag was not an integer");
        tool.printUsage();
        Make::quit(1);
      }
    }

    strarr.clear();
    cmdline->getQualifiedVariables("-O", args, &strarr);
    if (strarr.length() != 0)
    {
      if (frommakefile || isMakeconf)
        return String( "The -O flag can only be used from the command "
            "line or the MAKEFLAGS environment variable." );

      String tmpstr = strarr[strarr.lastIndex()];
      tmpstr.split( ',', UINT_MAX, &strarr );
      try
      {
        opt_level = Integer::parseInt(strarr[strarr.firstIndex()]);
      }
      catch ( MalformedVariable &e )
      {
        Make::error("argument \""+strarr[strarr.firstIndex()]+
                    "\" to -O flag was not an integer");
        tool.printUsage();
        Make::quit(1);
      }
      if (opt_level > 3 || opt_level < 0)
      {
        opt_level = 2; // CacheFiles
        Make::warning( String( "Defaulting -O flag to " ) + opt_level +
                       " , entered " + strarr[strarr.lastIndex()] );
      }
      // Set the appropriate cache levels based on optimization levels.
      switch (opt_level)
      {
        case 0:
          FileCache::setCacheLevel( FileCache::CACHE_NOTHING );
          if (strarr.length() > 1)
            Make::warning( String( "Ignoring additional parameters \"," ) +
              strarr[strarr.firstIndex() + 1] +
              "\" given to the -O0 flag" );
          break;
        case 1:
          FileCache::setCacheLevel( FileCache::CACHE_FILES );
          if (strarr.length() > 1)
            Make::warning( String( "Ignoring additional parameters \"," ) +
              strarr[strarr.firstIndex() + 1] +
              "\" given to the -O1 flag" );
          break;
        case 2:
          FileCache::setCacheLevel( FileCache::CACHE_DIR_CONTENTS );
          if (strarr.length() > 1)
            Make::warning( String( "Ignoring additional parameters \"," ) +
              strarr[strarr.firstIndex() + 1] +
              "\" given to the -O1 flag" );
          break;
        case 3:
          FileCache::setCacheLevel( FileCache::CACHE_SOME_FILES );
          if (strarr.length() > 1)
          {
            String str=strarr[strarr.lastIndex()];
            str.split( '=', UINT_MAX, &strarr );
            if (strarr.length() != 2 )
            {
              Make::error("argument \""+strarr[strarr.firstIndex()+1]+
                    "\" is not a valid argument to the -O3 flag");
              tool.printUsage();
              Make::quit(1);
            }
            if (strarr[strarr.firstIndex()].equalsIgnoreCase( "cache_limit" ))
            {
              if (strarr.length() > 1)
              {
                int cache_limit;
                try
                {
                  cache_limit = Integer::parseInt(strarr[strarr.firstIndex()+1]);
                  // Let FileCache do the limit boundary testing.
                  FileCache::setCacheSomeFilesLimit( cache_limit );
                }
                catch (MalformedVariable &e)
                {
                  Make::error("argument \""+strarr[strarr.firstIndex()+1]+
                    "\" to -O3,cache_limit= flag was not an integer");
                  tool.printUsage();
                  Make::quit(1);
                }
              }
              else
              {
                Make::warning( "Option -O3,cache_level= has no effect "
                  "since it has no value");
              }
            }
            else
            {
              Make::warning( "Ignoring additional options \"" +
                strarr[strarr.firstIndex()] + "\" to -O3");
            }
          }
          break;
      }
    }

    // Test for user specified makefile.
    //
    strarr.clear();
    cmdline->getQualifiedVariables("-f", args, &strarr);
    if ( strarr.length() != 0 )
    {
      if (frommakefile && (makefile != StringConstants::EMPTY_STRING))
        return String( "Special target .MAKEFLAGS cannot be used to "
            "specify makefile (-f)." );
      makefile = strarr[strarr.lastIndex()];
#ifdef DEFAULT_SHELL_IS_CMD
      // in case makefile path is in the form "d:makefile"
      if (!Path::absolute( makefile ) && makefile.length() > 1 &&
          isalpha( makefile[STRING_FIRST_INDEX] ) &&
          makefile[STRING_FIRST_INDEX + 1] == ':')
        Path::canonicalizeThis( makefile );
#endif
    }

    // Test for user specified BOM file.
    //
    strarr.clear();
    cmdline->getQualifiedVariables( "-B", args, &strarr );
    if (strarr.length() != 0)
    {
      if (frommakefile && !isMakeconf)
        return String( "The -B flag can only be used with .MAKEFLAGS "
            "in Makeconf." );

      bomfile = strarr[strarr.lastIndex()];
    }

    // Test for system makefile search paths.
    //
    strarr.clear();
    cmdline->getQualifiedVariables("-I", args, &strarr);
    if ( strarr.length() != 0 )
    {
      if ( frommakefile )
        return String( "Special target .MAKEFLAGS cannot be used to set "
            "search paths (-I)." );
      incs = strarr;
#ifdef DEFAULT_SHELL_IS_CMD
      for (int incidx = incs.firstIndex(); incidx <= incs.lastIndex(); incidx++)
      {
        if (!Path::absolute( incs[incidx] ) && incs[incidx].length() > 1 &&
            isalpha( incs[incidx][STRING_FIRST_INDEX] ) &&
            incs[incidx][STRING_FIRST_INDEX + 1] == ':')
          Path::canonicalizeThis( incs[incidx] );
      }
#endif
    }

    // Test for rmtserver name
    //
    strarr.clear();
    if ( (cmdline->getQualifiedVariables("-R", args, &strarr)) != NULL )
    {
      if ( frommakefile )
        return String( "Special target .MAKEFLAGS cannot be used to reset "
            "the RMS server (-R)." );
      rmtserver = strarr[strarr.lastIndex()];
      if ( cmdline->getMaxJobs() <= cmdline->getMaxLocalJobs() )
        Make::warning("Remote server "+rmtserver+" is ignored.");
    }

    // Test for variable definitions.
    //
    strarr.clear();
    global_vars.removeAllElements();
    cmdline->getQualifiedVariables("-D", args, &strarr);
    if ( strarr.length() != 0 )
    {
      if ( frommakefile && isVarsThere )
        return String( "Special target .MAKEFLAGS cannot be used to set "
            "global variables (-D)." );
      for (int idx=strarr.firstIndex(); idx<=strarr.lastIndex(); idx++ )
      {
        if (strarr[idx].indexOf( StringConstants::VAR_SEP_STRING ) == 0)
          // Add a trailing '=1'
          global_vars.addElement( strarr[idx] +
              StringConstants::VAR_SEP_STRING + '1' );
        else
          global_vars.addElement(strarr[idx]);
      }
    } /* end if */

    // Only thing left are targets or variable definitions.
    //
    tgts.removeAllElements();
    strarr.clear();
    cmdline_vars.removeAllElements();
    cmdline->getUnqualifiedVariables( &strarr );
    if (strarr.length() != 0)
    {
      String tgtorvar;
      for ( int cnt=strarr.firstIndex(); cnt<=strarr.lastIndex(); cnt++ )
      {
        tgtorvar = strarr[cnt];
        if (tgtorvar.indexOf( StringConstants::VAR_SEP_STRING ) == 0)
          tgts.addElement(tgtorvar);
        else
        {
          if ( frommakefile && isVarsThere )
            return String( "Special target .MAKEFLAGS cannot be used to set "
                "command line variables ( " )
                            + tgtorvar + " ).";
          if (tgtorvar.charAt(tgtorvar.firstIndex()) == '\"')
            tgtorvar = tgtorvar.substring( tgtorvar.firstIndex()+1 );

          if (tgtorvar.charAt(tgtorvar.lastIndex()) == '\"')
            tgtorvar = tgtorvar.substring( tgtorvar.firstIndex(),
              tgtorvar.lastIndex() );

          cmdline_vars.addElement(tgtorvar);
        }
      } /* end for */
    } /* end if */
  } /* end if */
  return String();
} /* end setStates */


/***************************************************************************
 *  Clear only the states that can be set via the special target .MAKEFLAGS
**/
void MkCmdLine::clearStates()
{
  // Just in case we need to, reset the messaging state to normal.
  Interface::setState("normal");


  // Clear all the states that can be set via .MAKEFLAGS special target
  force_build   = false;     // -a
  find_dirs     = false;     // -b
  debugAll      = false;     // -dA
  debuga        = false;     // -da
  debugc        = false;     // -dc
  debugd        = false;     // -dd
  debugg1       = false;     // -dg1
  debugg2       = false;     // -dg2
  debugi        = false;     // -di
  debugj        = false;     // -dj
  debugm        = false;     // -dm
  debugp        = false;     // -dp
  debugs        = false;     // -ds
  debugt        = false;     // -dt
  debugv        = false;     // -dv
  debugpv       = false;     // -dV
  ignore_errors = false;     // -i
  keep_going    = false;     // -k
  no_exec       = false;     // -n
  query         = false;     // -q
  no_echo       = false;     // -s
  touch_targs   = false;     // -t
  whitespace    = false;     // -w

} /* end clearStates */




/************************************************************************
 **/
String MkCmdLine::appendFlags( const StringArray &newflags,
    boolean isMakeconf, boolean isVarsThere )
{
  cmdline->appendArgs( newflags );
  return cmdline->setStates( true, newflags, isMakeconf, isVarsThere );
}


/************************************************************************
 **/
int  MkCmdLine::getArguments( StringArray &arguments )
{
  cmdline->getArgs( arguments );


  // Save the current states so we dont have to re-process the arguments
  // with the cmdline->setStates() method again.
  // We will use bit flags to simplify the storage.
  int  bitStates = 0;

  if (force_build)
    bitStates += BITFLAG_force_build;

  if (find_dirs)
    bitStates += BITFLAG_find_dirs;

  if (debugAll)
    bitStates += BITFLAG_debugAll;

  if (debuga)
    bitStates += BITFLAG_debuga;

  if (debugc)
    bitStates += BITFLAG_debugc;

  if (debugd)
    bitStates += BITFLAG_debugd;

  if (debugg1)
    bitStates += BITFLAG_debugg1;

  if (debugg2)
    bitStates += BITFLAG_debugg2;

  if (debugi)
    bitStates += BITFLAG_debugi;

  if (debugj)
    bitStates += BITFLAG_debugj;

  if (debugm)
    bitStates += BITFLAG_debugm;

  if (debugp)
    bitStates += BITFLAG_debugp;

  if (debugs)
    bitStates += BITFLAG_debugs;

  if (debugt)
    bitStates += BITFLAG_debugt;

  if (debugv)
    bitStates += BITFLAG_debugv;

  if (debugpv)
    bitStates += BITFLAG_debugpv;

  if (ignore_errors)
    bitStates += BITFLAG_ignore_errors;

  if (keep_going)
    bitStates += BITFLAG_keep_going;

  if (no_exec)
    bitStates += BITFLAG_no_exec;

  if (query)
    bitStates += BITFLAG_query;

  if (no_echo)
    bitStates += BITFLAG_no_echo;

  if (touch_targs)
    bitStates += BITFLAG_touch_targs;

  if (whitespace)
    bitStates += BITFLAG_whitespace;


  return( bitStates );
}


/************************************************************************
 **/
void MkCmdLine::restoreArguments( const StringArray &newArgs,
                                  const int          newStates)
{
  // First, we must restore the command line arguments that are maintained
  // in the inherited class CommandLine.
  cmdline->restoreArgs( newArgs );

  // Second, clear out the states that we maintain in this class MkCmdLine.
  // We only consider those arguments that can be changed via
  // the .MAKEFLAGS special target.
  cmdline->clearStates();

  // Instead of calling cmdline->setStates() to re-process the newly
  // restored arguments, just restore them directly since we saved the
  // states from before.

  if (newStates & BITFLAG_force_build)
    force_build = true;

  if (newStates & BITFLAG_find_dirs)
    find_dirs = true;

  if (newStates & BITFLAG_debugAll)
    debugAll = true;

  if (newStates & BITFLAG_debuga)
    debuga = true;

  if (newStates & BITFLAG_debugc)
    debugc = true;

  if (newStates & BITFLAG_debugd)
    debugd = true;

  if (newStates & BITFLAG_debugg1)
    debugg1 = true;

  if (newStates & BITFLAG_debugg2)
    debugg2 = true;

  if (newStates & BITFLAG_debugi)
    debugi = true;

  if (newStates & BITFLAG_debugj)
    debugj = true;

  if (newStates & BITFLAG_debugm)
    debugm = true;

  if (newStates & BITFLAG_debugp)
    debugp = true;

  if (newStates & BITFLAG_debugs)
    debugs = true;

  if (newStates & BITFLAG_debugt)
    debugt = true;

  if (newStates & BITFLAG_debugv)
    debugv = true;

  if (newStates & BITFLAG_debugpv)
    debugpv = true;

  if (newStates & BITFLAG_ignore_errors)
    ignore_errors = true;

  if (newStates & BITFLAG_keep_going)
    keep_going = true;

  if (newStates & BITFLAG_no_exec)
    no_exec = true;

  if (newStates & BITFLAG_query)
    query = true;

  if (newStates & BITFLAG_no_echo)
    no_echo = true;

  if (newStates & BITFLAG_touch_targs)
    touch_targs = true;

  if (newStates & BITFLAG_whitespace)
    whitespace = true;
}



/************************************************************************
 **/
String MkCmdLine::getRecursiveFlags()
{
  String newflags("");

  if (forceRebuild())
    newflags += " -a";

  if (findDirs())
    newflags += " -b";

  if (!userBOMfile().isEmpty())
    newflags += " -B " + userBOMfile();

  // Debug flags
  //
  if (dAll( ))
    newflags += " -dA";
  else
  {
    String dflags("");
    if (dArch( ))
      dflags += "a";
    if (dCond( ))
      dflags += "c";
    if (dDirs( ))
      dflags += "d";
    if (dGraph1( ))
      dflags += "g1";
    if (dGraph2( ))
      dflags += "g2";
    if (dJobs( ))
      dflags += "j";
    if (dIncs( ))
      dflags += "i";
    if (dModTime( ))
      dflags += "m";
    if (dPatterns( ))
      dflags += "p";
    if (dSuffs( ))
      dflags += "s";
    if (dTargs( ))
      dflags += "t";
    if (dVars( ))
      dflags += "v";
    if (dpVars( ))
      dflags += "V";

    // If any of the debug flags were set, then prepend the "-d"
    //
    if (dflags.length() > 0)
      newflags += " -d"+dflags;
  }

  if (envOverride())
    newflags += " -e";

  if (ignoreErrors())
    newflags += " -i";

  if (keepGoing())
    newflags += " -k";

  if (noExec())
    newflags += " -n";

  if (noEcho())
    newflags += " -s";

  if (touchTarget())
    newflags += " -t";

  if (whiteSpace())
    newflags += " -w";

  if (j_value > 0)
    newflags += " -j"+String( j_value );

  if (L_value > 0)
    newflags += " -L"+String( L_value );

  if (getMaxJobs() > getMaxLocalJobs())
    newflags += " -R"+getRmtServer();

  if (opt_level != -1)
  {
    switch (FileCache::cache_level)
    {
      case FileCache::CACHE_NOTHING:
        newflags += " -O0";
        break;
      case FileCache::CACHE_FILES:
        newflags += " -O1";
        break;
      case FileCache::CACHE_DIR_CONTENTS:
        newflags += " -O2";
        break;
      case FileCache::CACHE_SOME_FILES:
        newflags += " -O3,cache_limit=" + String( FileCache::getCacheSomeFilesLimit() );
        break;
    }
  }


  // Variables

  const String *varp;
  int equalsidx, varidx;

  // Command Line Variables
  //
  for (varidx=cmdline_vars.firstIndex(); varidx <= cmdline_vars.lastIndex(); varidx++ )
  {
    varp = cmdline_vars.elementAt(varidx);
    // Wrap the variable in quotes if it doesn't start with
    if (varp->charAt( varp->firstIndex() ) == '\"')
      newflags += " " + *varp;
    else
      newflags += " \"" + *varp + "\"";
  }

  // Global Variables
  //
  for (varidx=global_vars.firstIndex(); varidx <= global_vars.lastIndex(); varidx++ )
  {
    varp = global_vars.elementAt(varidx);

    equalsidx = varp->indexOf("=");
    if (equalsidx == STRING_NOTFOUND)
    {
      // Just add the -D flag as is.
      //
      newflags += " -D" + *varp;
    }
    else
    {
      // If the -D flag is of the form -Dvar="val1 val2" or -Dvar=val then
      // quote the string.
      //
      if (varp->charAt( equalsidx+1 ) == '\"')
        newflags += " -D" + varp->substring( varp->firstIndex(), equalsidx+1 ) +
          varp->substring( equalsidx+1 );
      else
        newflags += " -D" + varp->substring( varp->firstIndex(), equalsidx+1 ) +
          "\"" + varp->substring( equalsidx+1 ) + "\"";
    }
  }

  // Include flags
  for ( int incidx=incs.firstIndex(); incidx <= incs.lastIndex(); incidx++ )
  {
    newflags += " -I" + incs[incidx];
  }

  if (dVars())
    Interface::printAlways("Var: MAKEFLAGS=" + newflags);

  return ( newflags );
}

