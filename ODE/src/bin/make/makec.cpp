#define _ODE_BIN_MAKE_MAKEC_CPP_

#include <stdio.h>

#include "base/binbase.hpp"
#include "lib/util/signal.hpp"
#include "lib/exceptn/ioexcept.hpp"
#include "lib/exceptn/mfvarexc.hpp"
#include "lib/exceptn/parseexc.hpp"
#include "lib/io/version.hpp"
#include "lib/string/strcon.hpp"
#include "lib/portable/env.hpp"
#include "lib/util/filecach.hpp"

#include "bin/make/makec.hpp"
#include "bin/make/passnode.hpp"
#include "bin/make/integer.hpp"
#include "bin/make/job.hpp"
#include "bin/make/mkcmdln.hpp"
#include "bin/make/targnode.hpp"
#include "bin/make/sufftrfm.hpp"
#include "bin/make/makefile.hpp"
#ifdef __WEBMAKE__
#include "lib/util/date.hpp"
#endif // __WEBMAKE

Make      *Make::mk = 0;
MkCmdLine *Make::cmdline = 0;
fstream   *Make::bf = 0;

volatile int        Make::interrupt_cnt = 0;
#ifdef __WEBMAKE__
#ifdef WIN32
#include   <windows.h>
#endif
#include <sys/stat.h>
#include "lib/io/path.hpp"
#include "lib/portable/runcmd.hpp"
#ifdef __WEBDAV__
Hashtable< String, WebResource *> Make::WebDavFilesTable( 200 );
#endif //end of WEBDAV
StringArray *Make::extractFiles=new StringArray();
#endif //end of WEBMAKE

/************************************************************************
 **/
Make::~Make()
{
  delete mainpass;
  delete mkconfpass;
  maintgt = 0;
  mk = 0;
}

/************************************************************************
 * This is the public interface to the build command/class.
 * @param args The arguments to pass to mk
 * @return Returns 0 on success, and non-zero on failure.
 */
int Make::classMain( const char **argv, const char **envp )
{
#ifdef __WEBMAKE__
#ifdef WIN32
// If the server is created without a console on NT, each child
// daemon will create their own console, so create one, and hide it.

   if (AllocConsole())
   {
      HWND      parent;
      HWND      cmdWindow;
      WINDOWPLACEMENT placement;
      placement.length = sizeof(WINDOWPLACEMENT);
      Interface::printAlways("allocate console window");

// Hide the command session window that we have to have to allow
// stdout and stdin to work in the command line dll

      cmdWindow = GetForegroundWindow();
      if (cmdWindow)
      {
         char cmdName[256];

         GetWindowPlacement(cmdWindow, &placement);
         GetWindowText(cmdWindow,cmdName,sizeof(cmdName));
         strupr(cmdName);
         if (strstr(cmdName,"WEBMAKE"))
         {
            UINT saveCmd = placement.showCmd;
            placement.showCmd = SW_HIDE;
            SetWindowPlacement(cmdWindow, &placement);
            placement.showCmd = saveCmd;
         }
      }
   }
#endif // WIN32
#endif
  Make tmpmk( argv, envp );
#ifdef __WEBMAKE__
  //* Should output be tagged with XML
  if (Env::getenv("CONTENT-TYPE") && (*Env::getenv("CONTENT-TYPE")).equals("text/xml"))
  {
     // Should the header be printed when imbedded by Build Servlet
       Interface::setXMLState(true);
       Interface::printAlways( "<?xml version=\"1.0\"?>" );
       Interface::printAlways( "<!DOCTYPE SIMPLE [" );
       Interface::printAlways( "<!ELEMENT WebMake (#PCDATA)>" );
       Interface::printAlways( "]>" );
       Interface::printAlways( "<WebMake>" );  // beginning tag
  }
  else
       Interface::setXMLState(false);

#ifdef __WEBDAV__
  webDav_FileTable();
  String usermakefile=MkCmdLine::userMakefile();
  int index=usermakefile.indexOf(".");
  String bomfile=usermakefile.substring(STRING_FIRST_INDEX,index)+".bom";
  const String *destDir=Env::getenv("BOMDIR");
  if (destDir==0)
  {
    Interface::printAlways("BOMDIR environment variable is not set");
    exit(-1);

  }
  FILE *trace=fopen("Trace.out","r");
  if (trace!=0)
  {
    fclose(trace);
    /* int rc=system("del Trace.out"); */
    if(!Path::deletePath("Trace.out"))
    {
      Interface::printAlways("can't remove old Trace.out");
      exit(-1);
    }
  }
  //clean up the old bom file in the user specified directory
  struct stat buf;
  String delfile;
  int result=stat((*destDir+Path::DIR_SEPARATOR+bomfile).toCharPtr(),&buf);
  if(result==0)
  {
    /* result=system((String("del ")+(*destDir)+
                   Path::DIR_SEPARATOR+bomfile).toCharPtr()); */
    delfile = (*destDir) + Path::DIR_SEPARATOR + bomfile;
    Path::deletePath(delfile);
  }
#endif // __WEBDAV__
#endif // __WEBMAKE__
  try
  {
    mk->determineMainTargets();

    // Start updating the targets.
    //
    mk->findOutOfDate();
    if ( MkCmdLine::dDirs() )
      FileCache::printStats();

  }
  catch( Exception &e ) // base class for all ODE exceptions
  {
    error( "Fatal error." );
    error( e.getMessage() ); // exception details
    quit( 1 );
  }
  catch(...)
  {
    error("Fatal internal error.");
#ifdef __WEBMAKE__
    if (Interface::writeXML())
       Interface::printAlways( "</WebMake>" );  // end tag
    //remove the bom file if any error occurs
    /* int rc=system((String("del ")+bomfile).toCharPtr()); */
    if (!Path::deletePath(bomfile))
       Interface::printAlways("can't remove bomfile");
#endif // __WEBMAKE__
    quit( 1 );
  }
#ifdef __WEBMAKE__
   if (Interface::writeXML())
      Interface::printAlways( "</WebMake>" );  // end tag

   FILE *bfile=fopen(bomfile.toCharPtr(),"r");
   if (bfile != 0) {
      fclose(bfile);
     // make the bomfile name unique
     String oldbomfile=bomfile;
     index=usermakefile.indexOf(".");
     String tstr=Date::getCurrentDateAndTime();
     tstr.replaceThis(':','_').replaceThis(
     StringConstants::SPACE,StringConstants::EMPTY_STRING);
     bomfile=usermakefile.substring(STRING_FIRST_INDEX,index)+tstr;
     bomfile=bomfile+".bom";
     system((String("rename ")+oldbomfile+" "+bomfile).toCharPtr());

     //move the bom file to the user specified directory
     Interface::printAlways("move BOM file "+bomfile+" to "+(*destDir));
     int rc=system((String("move ")+bomfile+" "+(*destDir)).toCharPtr());
     if(rc!=0)
     {
       Interface::printAlways("can't move "+bomfile+" to "+(*destDir));
       exit(-1);
     }
   }
#endif  // __WEBMAKE__
  return 0;
}

#ifdef __WEBMAKE__
#ifdef __WEBDAV__
void Make::webDav_FileTable()
{
   /*-----------------------------------------------------------------+
   | get the -Dvariable=val for all the webDAV macro                  |
   | We will get all the variable and set it as environment variable  |
   | so that the WebDAV macros can be processed from any place        |
   | The command line -D will have higher precedence than the env     |
   | set by the buildServlet                                          |
   +-----------------------------------------------------------------*/
   Vector< String > global_varsvec=MkCmdLine::getCommandLineVars();
   if (!global_varsvec.isEmpty())
   {
     boolean replace=true;
     for (int varidx=global_varsvec.firstIndex();
         varidx <= global_varsvec.lastIndex();
         varidx++ )
     {
       StringArray webDavVars;
       String var=*global_varsvec.elementAt(varidx);
       var.split("=",UINT_MAX,&webDavVars);
       if(webDavVars[ARRAY_FIRST_INDEX].equals("WEBDAV_SERVER"))
          Env::setenv("WEBDAV_SERVER",webDavVars[ARRAY_FIRST_INDEX +1],replace);
       if(webDavVars[ARRAY_FIRST_INDEX].equals("BUILDRESOURCE_ROOT"))
          Env::setenv("BUILDRESOURCE_ROOT",webDavVars[ARRAY_FIRST_INDEX + 1],replace);
       if(webDavVars[ARRAY_FIRST_INDEX].equals("noExtract"))
          Env::setenv("noExtract",webDavVars[ARRAY_FIRST_INDEX + 1],replace);
       if(webDavVars[ARRAY_FIRST_INDEX].equals("TCTime"))
          Env::setenv("TCTime",webDavVars[ARRAY_FIRST_INDEX + 1],replace);
       if(webDavVars[ARRAY_FIRST_INDEX].equals("BOMDIR"))
          Env::setenv("BOMDIR",webDavVars[ARRAY_FIRST_INDEX + 1],replace);
     }
   }
   const String *server=Env::getenv("WEBDAV_SERVER");
   const String *rootDir=Env::getenv("BUILDRESOURCE_ROOT");
   StringArray  newcmds(4);
   if (Interface::writeXML())
      Interface::printAlways( "<WebDAVPROPFIND>" );
   if(server==0)
   {
      Interface::printAlways("WEBDAV_SERVER environment is not set");
      if (Interface::writeXML())
         Interface::printAlways( "</WebDAVPROPFIND>" );
      exit(-1);
   }
   if(rootDir==0)
   {
      Interface::printAlways("BUILDRESOURCE_ROOT environment is not set");
      if (Interface::writeXML())
         Interface::printAlways( "</WebDAVPROPFIND>" );
      exit(-1);
   }
   const String cmd="java com.ibm.etools.webmake.make.ALLMEMPROPFIND "+(*server);
   int rc=system(cmd.toCharPtr());
   if (rc!=0)
   {
     if (Interface::writeXML())
        Interface::printAlways( "</WebDAVPROPFIND>" );
     exit(-1);
   }
   const String files="WebResource.tmp";
   String webDavFiles=(*rootDir)+Path::DIR_SEPARATOR+files;
   //Path::unixizeThis(tcfiles);
   FILE *fp=fopen(webDavFiles.toCharPtr(),"r+");
   if (!fp)
   {
     Interface::printAlways("can't open webDavfiles:"+webDavFiles);
     if (Interface::writeXML())
        Interface::printAlways( "</WebDAVPROPFIND>" );
     return;
   }
   char buffer[1024];
   char href[512];
   char time_string[512];
   time_t file_time;
   int file_size;
   while (fp && !feof(fp))
   {
     if (fgets(buffer,1024,fp)==NULL) break;
     int i=0;
     while (buffer[i]==' ') i++;

     sscanf(buffer+i, "%s%ld%ld%s",href, &file_size,&file_time,time_string);
     WebDavFilesTable.put( String(href),new WebResource(String(href),file_size,
file_time,time_string )  );

   }
   if (Interface::writeXML())
      Interface::printAlways( "</WebDAVPROPFIND>" );
}
#endif // __WEBDAV__
#endif // __WEBMAKE__

/************************************************************************
 * Private constructor
 * Only allow 1 instance from the Make class
 */
Make::Make( const char **argv, const char **envp ) :
      Tool( envp ), args( argv ), envs( envp ),
      mainpass(0), maintgt(0), mkconfpass(0),
      not_parallel( false ), special_passes( false )
{
  Keyword::initialize();

  if (mk != 0)
    warning("Unwanted reinitialization occured");

  mk = this;
  Signal::registerInterruptHandler( this );

  static StringArray allargs;
  allargs = args;

  // Retrieve all the flags set in the MAKEFLAGS environment variable.
  //
  const String *makeflags=Env::getenv( Constants::MAKEFLAGS );
  if (makeflags != 0)
  {
    StringArray makeflagsarr;
    makeflags->split( StringConstants::SPACE, UINT_MAX, &makeflagsarr );
    allargs += makeflagsarr;
  }

  // Set the environment variable MAKE to the program name.
#ifdef ARGV0_IS_UNUSABLE_PATH
  Env::setenv( Constants::MAKE, Constants::MAKE_NAME, false);
#else
  Env::setenv( Constants::MAKE, argv[0], false);
#endif

  // Initialize the MACHINE variable to the machine that we are running on
  // if it hasn't already been set.
  if (Env::getenv( Constants::MACHINE ) == 0)
    Env::setenv( Constants::MACHINE, PlatformConstants::CURRENT_MACHINE, true);

  // Set version numbering information to help conditionally test for different
  // syntaxes.
  Env::setenv( Constants::ODERELEASE, Version::RELEASE, false );

  // Initialize the command line Object
  cmdline = new MkCmdLine(allargs, *this);
  cmdline->setStates( false, allargs, false, false );

  // Set MAKEFLAGS to the minimal set of recursive flags.
  Env::setenv( Constants::MAKEFLAGS, MkCmdLine::getRecursiveFlags(), true );

  // Initialize the job module with what is defined in the commmand line.
  Job::init(MkCmdLine::getMaxJobs(), MkCmdLine::getMaxLocalJobs());
}

/************************************************************************
 * Read in usermakefile if defined or search the normal paths
 * for makefile.ode, Makefile.ode, makefile, Makefile.
 */
void Make::readUserMakefile( const String &usermakefile, PassNode &curpass,
  Dir &searchpath )
{
  Makefile *usrmf = 0;

  // The following boolean is used to determine if we should replace
  // the continuation character with a space or just remove it.
  boolean noSpace = (curpass.getRootVars()->
                          find(StringConstants::ODEMAKE_NOCONTSPC_VAR ) != 0);


  // Read user makefile
  if ( usermakefile != StringConstants::EMPTY_STRING )
  {
#ifdef __WEBMAKE__

    //clean up the old BOM file for this makefile
    int index=usermakefile.indexOf(".");
    String bomfile=usermakefile.substring(STRING_FIRST_INDEX,index)+".bom";
    FILE *bom=fopen(bomfile.toCharPtr(),"r");
    if (bom!=0)
    {
      fclose(bom);
      /* int rc=system((String("del ")+bomfile).toCharPtr()); */
      if(!Path::deletePath(bomfile))
      {
        Interface::printAlways("can't remove old"+bomfile);
        exit(-1);
      }
    }
    // if extractFlag is on , we will extract the UserMakefile
    const String *noExtractFlag=Env::getenv("noExtract");
    if(noExtractFlag==0 || (noExtractFlag!=0 && (*noExtractFlag).startsWith("0")))
    {
       FILE *trace=fopen("Trace.out","a+");
       fputs("Begin to extract userMakeFile.....:\n",trace);
       fclose(trace);
       curpass.extractIncludeMakeFile(usermakefile);

    }
    CondEvaluator  evaluator( curpass.mfs, &curpass, curpass.getVarEval() );

    //add this for solving the if block embedded after continuatio line
    //we need to pass all the global and command line variables to be
    //evaluated and thus te makefile only contains the final correct stmts
    //we only do this for user makefile, and skip the default makefile
    //since we expect the default to be in the ode format
    usrmf = Makefile::load(usermakefile, curpass.getCwd(), searchpath,&evaluator);
#else
    usrmf = Makefile::load(usermakefile, curpass.getCwd(), searchpath, !noSpace);
#endif // __WEBMAKE__
    if (usrmf == 0)
      quit("File "+usermakefile+" not found", 1);
  }
  else
  {
    usrmf = Makefile::load("makefile.ode", curpass.getCwd(),
                           searchpath, !noSpace);
    if (usrmf == 0)
    {
// For case sensitive platforms, check uppercase version
#ifndef CASE_INSENSITIVE_OS
      usrmf = Makefile::load("Makefile.ode", curpass.getCwd(),
                             searchpath, !noSpace);
      if (usrmf == 0)
#endif
      {
        usrmf = Makefile::load("makefile", curpass.getCwd(),
                               searchpath, !noSpace);
        if (usrmf == 0)
        {
// For case sensitive platforms, check uppercase version
#ifndef CASE_INSENSITIVE_OS
          usrmf = Makefile::load("Makefile", curpass.getCwd(), searchpath);
          if (usrmf == 0)
#endif
            quit("could not find a makefile", 1);
        }
      }
    }
  }
  // Set the MAKEFILE variable
  curpass.setMakefile( usrmf );

  // Instantiate the user Makefile
  usrmf->instantiate( curpass );
}

/************************************************************************
 **/
void Make::determineMainTargets()
{
  // Initialize the system search paths.  The system search paths are defined
  // in order by -I flags, then MAKESYSPATH.
  initSysSearchPath();

  readMakeconf();

  mainpass = new PassNode( "root", Constants::DEFAULT, cur_dir, make_dir,
      make_top, 0, 0, 0, dirSearchPath.getPath() );

  // Initialize the system search paths.  The system search paths are defined in
  // order by -I flags, then MAKESYSPATH.
  initSysSearchPath();

  readUserMakefile( MkCmdLine::userMakefile(), *mainpass, dirSearchPath );
}

/************************************************************************
 **/
void Make::findOutOfDate( )
{
  Vector< String > tgts=MkCmdLine::getTgts();
  if (tgts.isEmpty())
  {
    GraphNode *tgtnd=mainpass->getTgtGraph()->find( Constants::DOT_MAIN );

    // First find the .MAIN target if no command line targets were
    // specified.
    if (tgtnd != 0)
    {
      if (((TargetNode *)tgtnd)->hasChildren())
        tgts.addElement( ((TargetNode*)tgtnd)->nameOf() );
    }
    else if (maintgt != 0)
    {
      tgts.addElement( maintgt->nameOf() );
    }
    else
    {
      quit( "No targets found to update", 1 );
    }
  }

  if (mainpass == 0 ||
      (mainpass->getTgtGraph()->isEmpty() &&
       mainpass->getSuffTransforms()->isEmpty() &&
       mainpass->getPatterns()->isEmpty()))
  {
    quit( "Nothing to make", 1 );
  }

  try
  {
    // Determine if .NOTPARALLEL was set and .PASSES was not used, then
    // turn off parallelism.  Set max jobs equal to 1.
    if (getNotParallel() == true)
    {
      if (getSpecialPasses() == true)
        warning( "ignoring .NOTPARALLEL since the .PASSES special source is being used" );
      else
        Job::setMaxJobs( 1, 1 );
    }

    int childstate = mainpass->update( 0, mkconfpass, &tgts );

    // We'll say the target is up-to-date if it is unmade or uptodate.
    boolean has_errors = false;
    GraphNode *gn;
    int tstate;
    for (int tgtidx=tgts.firstIndex(); tgtidx<=tgts.lastIndex(); tgtidx++)
    {
      gn = mainpass->getTgtGraph()->
        find( *(String *)tgts.elementAt( tgtidx ) );
      if (gn == 0)
        continue;

      tstate = gn->getState();
      if (tstate == GraphNode::UNMADE || tstate == GraphNode::UPTODATE)
      {
        Interface::printAlways( Constants::MAKE_NAME + ": `" +
          (*(String *)tgts.elementAt( tgtidx )) + "' is up to date" );
      }
      else if ((tstate == GraphNode::ERROR) ||
               (tstate != GraphNode::MADE && childstate == GraphNode::ERROR))
      {
        error( "`" +(*(String *)tgts.elementAt( tgtidx )) +
               "' not made because of errors" );
        has_errors = true;
      }
    }
    if (has_errors || childstate == GraphNode::ERROR)
      quit( 1 );
  }
  catch ( ParseException &e )
  {
    error( e.toString() );
    quit( "Fatal errors encountered -- cannot continue", 1 );
  }
}


/************************************************************************
 **/
void Make::determineTop( const String &srcbase )
{
  static String basedir;
  basedir  = srcbase;
  Path::unixizeThis( Path::canonicalizeThis( basedir ) );
  int topdepth = getDirDepth(cur_dir) - getDirDepth(basedir);
  String top;
  int subidx=cur_dir.lastIndex();
  for ( ; topdepth > 0; topdepth-- )
  {
    top += "../";
    subidx = cur_dir.lastIndexOf(StringConstants::FORW_SLASH, subidx-1);
  }
  if (top.length() == 0)
  {
    make_dir = StringConstants::FORW_SLASH;
    make_top = StringConstants::EMPTY_STRING;
  }
  else
  {
    make_dir = cur_dir.substring(subidx);
    Path::unixizeThis( make_dir );
    make_top = top;
    Path::unixizeThis( make_top );
  }
}


void Make::determineObjDir( String &obj_dir, PassNode *mkconfpass )
{
  if (mkconfpass != 0)
  {
    try
    {
      StringArray obj_dirs;
      mkconfpass->parseUntil("$(MAKEOBJDIR)", StringConstants::EMPTY_STRING,
        false, &obj_dirs);
      obj_dir = obj_dirs[ARRAY_FIRST_INDEX];
    }
    catch ( MalformedVariable &e )
    {
      quit( "MAKEOBJDIR is malformed", 1 );
    }
  }

  if (obj_dir.isEmpty())
    obj_dir = SandboxConstants::getOBJECTDIR();
  else
    SandboxConstants::setOBJECTDIR( obj_dir );
}

/************************************************************************
 **/
void Make::readMakeconf()
{
  static String mkconf_subdir;
  static String mkconf;
  static String top;
  static String srcbase;
  cur_dir = Path::getCwdFromEnviron();
  Path::unixizeThis( cur_dir );
  int         dirdepth = getDirDepth( cur_dir );
  int         depth = 0;
  boolean     found = false;
  StringArray searchpath_arr( 4 ); // Initially 4 elements.

  srcbase = StringConstants::EMPTY_STRING;
  top = StringConstants::EMPTY_STRING;
  mkconf = Constants::MAKECONF;
  mkconf_subdir = SandboxConstants::getSRCNAME();

  if (MkCmdLine::dDirs())
    Interface::printAlways( Constants::MAKE_NAME + ": cwd: " + cur_dir );

  // If we need to look in the rc_files directory for Makeconf.
  if (Env::getenv( "_ODE_CONFS_IN_RCFILES" ) != 0)
  {
    mkconf_subdir = SandboxConstants::getRCFILES_DIR();
    if ((srcbase = SandboxConstants::getSANDBOXBASE()) !=
        StringConstants::EMPTY_STRING)
    {
      if (MkCmdLine::dVars())
        Interface::printAlways( "Var: SANDBOXBASE=" + srcbase );
      srcbase += StringConstants::FORW_SLASH;
      srcbase += SandboxConstants::getSRCNAME();
    }
    Path::unixizeThis( srcbase );
  }

  const String *mkconfptr = Env::getenv( "_MAKECONF" );
  // If _MAKECONF is set, then we must be recursive
  if (mkconfptr != 0)
  {
    mkconf = *mkconfptr;
    if ((srcbase = SandboxConstants::getSANDBOXBASE()) !=
        StringConstants::EMPTY_STRING)
    {
      if (MkCmdLine::dVars())
        Interface::printAlways( "Var: SANDBOXBASE=" + srcbase );
      srcbase += StringConstants::FORW_SLASH;
      srcbase += SandboxConstants::getSRCNAME();
    }
    Path::unixizeThis( srcbase );
    determineObjDir( obj_dir, mkconfpass );
    if (obj_dir.length() > 0)
    {
      if (Path::absolute( obj_dir ))
        determineTop( obj_dir );
      else
        determineTop( srcbase + StringConstants::FORW_SLASH + obj_dir );
    }
    else
      // We must be in the
      determineTop( srcbase );

    if (MkCmdLine::dDirs())
      Interface::printAlways( "    found " + mkconf );
    found = true;
  }
  else
  {
    determineDirSearchPath( mkconf_subdir, searchpath_arr );
    if (searchpath_arr.length() != 0)
    {
      mkconf = Path::findFileInChain( Constants::MAKECONF, searchpath_arr );
      Path::unixizeThis( mkconf );
      if (MkCmdLine::dDirs())
      {
        Interface::printAlways("Dir: Searching for Makeconf in ...");
        for (int i = searchpath_arr.firstIndex();
            i <= searchpath_arr.lastIndex(); i++)
          Interface::printAlways("\t"+searchpath_arr[i]);
      }
      if (mkconf.length() != 0)
      {
        if (MkCmdLine::dDirs())
          Interface::printAlways("    found " + mkconf);
        if (srcbase.length() != 0)
          determineTop( srcbase );
        else
        {
          srcbase = searchpath_arr[ARRAY_FIRST_INDEX];
          determineTop( srcbase );
        }
        found = true;
      }
      else if (srcbase.length() != 0)
      {
        determineTop( srcbase );
      }
    }
  }

  // If we couldn't find it through environment variables, start searching
  // up the directory tree.
  if (!found)
  {
    int makediridx = cur_dir.lastIndex();
    mkconf  = Constants::MAKECONF;
    while (!(found = Path::exists( mkconf )) && depth <= dirdepth)
    {
      top += "../";
      mkconf = top;
      mkconf += Constants::MAKECONF;
      makediridx = cur_dir.lastIndexOf( StringConstants::FORW_SLASH,
          makediridx - 1 );
      depth++;
    }
    if (found)
    {
      make_top = top;
      // In the case Makeconf was found in the current directory
      if (makediridx == cur_dir.lastIndex())
        make_dir = StringConstants::FORW_SLASH;
      else
        make_dir = cur_dir.substring( makediridx );
    }
  }

  // If the search path hasn't been determined, do it now
  if (searchpath_arr.length() == 0)
  {
    determineDirSearchPath( mkconf_subdir, searchpath_arr );
  }

  // If we don't have any paths, we'll use the current directory for now
  if (searchpath_arr.length() == 0)
    searchpath_arr.add( cur_dir );
  else
  {
    // First append MAKEDIR so the current directory is searched and
    // corresponding backed directories are searched.
    if (make_dir.length() > 1)
    {
      for (int pathidx = searchpath_arr.firstIndex();
          pathidx <= searchpath_arr.lastIndex(); pathidx++)
        searchpath_arr[pathidx] += make_dir;
    }
  }

  // Set up the search path so Makeconf can find "" included files.
  // Those include lines that look like:
  //   .include "foo.mk"
  dirSearchPath.setPath( &searchpath_arr );

  if (found)
  {
    if (MkCmdLine::dDirs())
      Interface::printAlways( "Dir: Found Makeconf as: " + mkconf );

    boolean reinitmaintgt=false;
    Makefile *Makeconf = Makefile::load( mkconf, cur_dir, dirSearchPath );
    if (Makeconf != 0) // The Makeconf was found
    {
      // Don't allow Makeconf to set the main target.
      if (maintgt == 0)
        reinitmaintgt = true;

      mkconfpass = new PassNode( "Makeconf", "DEFAULT", cur_dir, make_dir,
        make_top, Makeconf, Makeconf, 0, &searchpath_arr);
      Makeconf->instantiate( *mkconfpass );
      if (reinitmaintgt)
        maintgt = 0;

      if (mkconfptr == 0)
      {
        String unix_mkconf = mkconf;
        Path::unixizeThis( unix_mkconf );
        mkconfpass->getEnvironVars()->set( "_MAKECONF", unix_mkconf, true );

        // set env variables for recursive make
        Env::setenv( "_MAKECONF", unix_mkconf, true );
        if (MkCmdLine::dVars())
          Interface::printAlways( "Var: _MAKECONF=" + unix_mkconf );
      }
      else
      {
        // Need to determine MAKEDIR and MAKETOP now for recursive mk
        determineObjDir( obj_dir, mkconfpass );
        if (obj_dir.length() > 0)
        {
          if (Path::absolute( obj_dir ))
            determineTop( obj_dir );
          else
            determineTop( srcbase + StringConstants::FORW_SLASH + obj_dir );
        }
        else
          // We must be in the
          determineTop( srcbase );

        // Hammer in the new values
        mkconfpass->environ_vars->set( Constants::MAKEDIR, make_dir, true );
        SandboxConstants::setMAKEDIR( make_dir );
        if (make_dir.length() > 1)
          mkconfpass->global_vars->set( Constants::MAKESUB,
            make_dir.substring( ARRAY_FIRST_INDEX + 1 ) +
            StringConstants::FORW_SLASH, true );
        else
          // makedir must by just "/"
          mkconfpass->global_vars->set( Constants::MAKESUB, make_dir, true );
      }
    }
    else
    {
      mkconfpass = 0;
      make_top = StringConstants::EMPTY_STRING;
      make_dir = StringConstants::FORW_SLASH;
      if (MkCmdLine::dDirs())
        Interface::printAlways( Constants::MAKE_NAME + ": No Makeconf Found" );
    }
  }
  else
  {
    mkconfpass = 0;
    if (srcbase.length() == 0)
    {
      make_top = StringConstants::EMPTY_STRING;
      make_dir = StringConstants::FORW_SLASH;
    }
    if (MkCmdLine::dDirs())
      Interface::printAlways( Constants::MAKE_NAME + ": No Makeconf Found" );
  }

  // Reset the search path for "" type includes
  // The following sections will set it to its appropriate values
  dirSearchPath.setPath( 0 );

  if (mkconfpass != 0)
  {
    String srcdirpath=StringConstants::EMPTY_STRING;
    try
    {
      StringArray src_dirs;
      mkconfpass->parseUntil( "$(MAKESRCDIRPATH)",
          StringConstants::EMPTY_STRING, false, &src_dirs );
      srcdirpath = src_dirs[ARRAY_FIRST_INDEX];
    } catch ( MalformedVariable &e )
    {
      quit("MAKESRCDIRPATH is malformed", 1);
    }
    if (srcdirpath.length() > 0)
    {
      StringArray src_dirs;
      String unix_srcdirpath = srcdirpath;
      Path::unixizeThis( unix_srcdirpath );
      unix_srcdirpath.split( Path::PATH_SEPARATOR, UINT_MAX, &src_dirs );
      StringArray tmparr;
      Dir::appendSubDirToAll( &src_dirs, make_dir.substring(
          make_dir.firstIndex() + 1 ), &tmparr );
      dirSearchPath.setPath( &tmparr );

      if (MkCmdLine::dVars())
        Interface::printAlways( "Var: MAKESRCDIRPATH=" + unix_srcdirpath );
    }
  }
  if (srcbase.length() > 0)
    dirSearchPath.prepend( srcbase + make_dir );

  if (obj_dir.length() == 0)
    determineObjDir( obj_dir, mkconfpass );

  // If we are NOT a recursive mk and object directory setting exists then
  // change the directory to the obj tree
  if (obj_dir.length() > 0 && mkconfptr == 0)
  {
    // Set the MAKEOBJDIR variable as an environment variable so genpath
    // can use what mk sees.
    Env::setenv( "MAKEOBJDIR", obj_dir, false );
    if (mkconfpass != 0)
      mkconfpass->getEnvironVars()->set( "MAKEOBJDIR", obj_dir, false );

    if (MkCmdLine::dVars())
      Interface::printAlways( "Var: MAKEOBJDIR=" + obj_dir );

    String newobjdir;
    if (!Path::absolute( obj_dir ))
      newobjdir = make_top;
    newobjdir += obj_dir;
    // Don't add make_dir if it is not equal to '/'
    if (make_dir.length() > 1)
      newobjdir += make_dir;

    Interface::printAlways( "cd " + Path::userize( newobjdir ) );

    if (!Path::absolute( obj_dir ))
    {
      cur_dir += StringConstants::FORW_SLASH;
      cur_dir += make_top;
      cur_dir += obj_dir;
    }
    else
      cur_dir = obj_dir;
    cur_dir += make_dir;
    Path::canonicalizeThis( cur_dir, false );
    Path::createPath( cur_dir );
    Path::setcwd( cur_dir );
    if (mkconfpass != 0)
      mkconfpass->setVirtualCwd( cur_dir );
  }

  dirSearchPath.prepend( Path::unixizeThis( cur_dir ) );
}

/************************************************
  *  --- void Make::initSysSearchPath ---
  *
  ************************************************/
void Make::initSysSearchPath()
{
  // System search paths are those used to search for the sys.mk file or
  // makefiles included with <>'s.
  //  Search order for <> is -I's then MAKESYSPATH
  int syslen=0;
  static StringArray tmparr;

  tmparr.clear();
  // Clear any previous settings
  sysSearchPath.setPath( 0 );

  if (MkCmdLine::getIncs().length() > 0)
  {
    expandSysDirs( MkCmdLine::getIncs(), &tmparr );
    sysSearchPath.setPath( &tmparr );
    syslen = sysSearchPath.getPath()->length();
  }

  const String *syspaths = Env::getenv( Constants::MAKESYSPATH );
  if (syspaths != 0)
  {
    tmparr.clear();
    Path::unixize( *syspaths ).split( Path::PATH_SEPARATOR,
        UINT_MAX, &tmparr );

    StringArray tmpsyspaths;
    expandSysDirs( tmparr, &tmpsyspaths );
    if (syslen == 0)
    {
      sysSearchPath.setPath( &tmpsyspaths );
    }
    else
    {
      // We need to concat the two arrays.
      tmpsyspaths += *sysSearchPath.getPath();
      sysSearchPath.setPath( &tmpsyspaths );
    }
  } /* end if */
}

/************************************************************************
 *  Add all relative paths specified to the dirSearchPath.
**/
StringArray *Make::expandSysDirs( const StringArray &arr, StringArray *buf )
{
  StringArray *res = 0;
  const StringArray *dirsearchpathsptr = dirSearchPath.getPath();
  static String unix_param;

  if (buf == 0)
    res = new StringArray();
  else
    res = buf;

  for (int i=arr.firstIndex(); i<=arr.lastIndex(); i++ )
  {
    if (Path::absolute(arr[i]))
      res->append(arr[i]);
    else
    {
      if (dirsearchpathsptr != 0)
      {
        for (int j = dirsearchpathsptr->firstIndex();
             j <= dirsearchpathsptr->lastIndex(); j++)
        {
          unix_param = (*dirsearchpathsptr)[j];
          unix_param += Path::DIR_SEPARATOR;
          unix_param += arr[i];
          Path::unixizeThis( Path::canonicalizeThis( unix_param, false, false,
              cur_dir ));
          res->append( unix_param );
        } /* end for */
      } /* end if */
      else
        res->append(arr[i]);
    } /* end if */
  }
  return (res);
}

/************************************************************************
 * Purpose:
 *   This method will get a list of paths for mk to search for the
 *   Makeconf.  This method will also be used to determine what a
 *   search path should be for double quoted ("") include.  The
 *   example is:
 *     .include "somefile"
 *   from the Makeconf file.
**/
void Make::determineDirSearchPath( const String &mkconf_subdir,
  StringArray &searchpath_arr )
{
  // First look for Makeconf through BACKED_SANDBOXDIR
  String backeddir, backedsrcdir;
  const String *backeddirptr=0;
  if ((backeddirptr=Env::getenv("BACKED_SANDBOXDIR")) != 0)
  {
    // Add the appropriate subdirectory to the sandbox search directory.
    // If BACKED_SANDBOXDIR=/tmp/bb:/tmp/sb1 and _ODE_CONFS_IN_RCFILES is not
    // set then the search path becomes:
    //   (/tmp/bb/${ODESRCNAME}:/tmp/sb1/${ODESRCNAME}
    // If _ODE_CONFS_IN_RCFILES is set then the search path becomes:
    //   (/tmp/bb/${ODERCFNAME}:/tmp/sb1/${ODERCFNAME}
    backeddir = *backeddirptr;

    if (MkCmdLine::dVars())
      Interface::printAlways( "Var: BACKED_SANDBOXDIR="+backeddir );

    backeddir.String::replaceThis( Path::PATH_SEPARATOR,
      String( StringConstants::FORW_SLASH +  mkconf_subdir +
              Path::PATH_SEPARATOR ) );
    backedsrcdir  =  backeddir;
    backedsrcdir +=  StringConstants::FORW_SLASH;
    backedsrcdir +=  mkconf_subdir;
    Path::unixizeThis( backedsrcdir );
  }
  backedsrcdir.split( Path::PATH_SEPARATOR, UINT_MAX,  &searchpath_arr );
}

/************************************************************************
 * Handle ctrl-C, ctrl-Break type interrupts.
**/
void Make::handleInterrupt()
{
  if (interrupt_cnt == 0)
    fprintf( stderr, ">> ERROR: mk: Interrupt received.  Cleaning up...\n" );
  else if (interrupt_cnt > 2)
  {
    fprintf( stderr, ">> ERROR: mk: Interrupt received, again.  Giving up...\n" );
    quit( 1 );
  }
  else
    fprintf( stderr, ">> ERROR: mk: Interrupt received, again.  Please wait...\n" );

  interrupt_cnt++;
}

/************************************************************************
 * Run the .INTERRUPT and .EXIT targets from the main PassNode
**/
void Make::runInterruptTargets()
{
  if (mainpass == 0)
    return;
  mainpass->updateTarget(Constants::DOT_INTERRUPT);
  mainpass->updateTarget(Constants::DOT_EXIT);
}

int Make::getDirDepth(const String &dir)
{
  int depth=0;
  for (int i=dir.firstIndex(); i<=dir.lastIndex(); i++)
    if (dir.charAt(i) == '/') depth++;

  // Count occurences of "../"  and make adjustment for them.
  int idx=STRING_FIRST_INDEX;
  int count=0;
  String backdir =  ".." + StringConstants::FORW_SLASH;

  while (idx != STRING_NOTFOUND)
  {
    idx = dir.indexOf( backdir, idx );
    if (idx != STRING_NOTFOUND)
    {
      count++;
      idx++;
    }
  }
  // We need to subtract 2 for each "../" found in the path.
  depth -= (2 * count);

  return (depth);
}


void Make::quit( const int errcode )
{
  if (errcode != 0)
    error( "Aborting" );
  else
    Job::waitForRunningJobs();
  Interface::quit( errcode );
}

void Make::quit( const String &msg, const int errcode )
{
  error( msg );
  quit( errcode );
}

void Make::error( const String &msg, boolean ignored )
{
  if (ignored)
    Interface::printAlways( String( ">> ERROR: " ) + Constants::MAKE_NAME +
      ": " + msg + " (ignored)" );
  else
    Interface::printError( Constants::MAKE_NAME + ": "+msg );
}

/**
 * Print usage information.
 *
**/
void Make::printUsage() const
{
  Interface::printAlways( String( "Usage: " ) + Constants::MAKE_NAME +
      " [-abeiknstw] [-Bfilename] "
      "[-Dvariable] [-d[Aacdg1g2ijmpstVv]]" );
  Interface::printAlways( "          [-fmakefile] [-Idirectory] "
      "[-Lmax_local_jobs] [-jmax_jobs]" );
  Interface::printAlways( "          [-Rremote_host] "
      "[-O{0|1|2|3[,cache_limit=limit]}] " );
  Interface::printAlways( "          [variable=value] [target ...]" );
  Interface::printAlways( "          [-usage | -?] [-version | -rev | -v]" );
}
