/**
 * PassNode (misc)
 *
**/
#define _ODE_BIN_MAKE_PASSNDC_CPP_

#include "base/binbase.hpp"

#include "bin/make/passnode.hpp"

#include "bin/make/cmdable.hpp"
#include "bin/make/constant.hpp"
#include "bin/make/dir.hpp"
#include "bin/make/job.hpp"
#include "bin/make/mfstmnt.hpp"
#include "bin/make/targnode.hpp"
#include "bin/make/makefile.hpp"
#include "bin/make/sufftrfm.hpp"
#include "bin/make/makec.hpp"

#ifdef __WEBMAKE__
#include <stdio.h>
#include <sys/stat.h>
#endif // __WEBMAKE__
const String &PassNode::hashName() const
{
  return (passndid);
}

/***************************************************************
 *  The constructor
 *    This constructor is used for full PassNode construction.
 */
PassNode::PassNode(const String &initname, const String &initactionname, const String &initcwd,
                   const String &initmakedir, const String &initmaketop,
                   Makefile *initmakefile, Makefile *initparentmakefile,
                   PassNode *initparentdirpass, const StringArray *initsearchpath)
    :
    GraphNode( initname ),
    virtual_cwd( initcwd ),
    make_top( initmaketop ),
    make_dir( initmakedir ),
    mf( initmakefile ),
    parentmf( initparentmakefile ),
    tgtgraph( new Graph() ),
    parentdirpass( initparentdirpass ),
    subdirpasses( new Vector<PassNode *>(5, elementsEqual) ),
    actionname( initactionname ),
    makeincludecompat( false ),
    passndid( actionname + id ),
    is_parsed( false ),
    is_precious( false ),
    tgtlst( 10 )
{
  // Add this pass to the parent directory pass node
  if (parentdirpass != 0)
  {
    parentdirpass->addSubDirPass(this);
  }

  firstchar = '\0';
  transforms = new SuffixTransforms();
  patterns = new Patterns();

  if (initsearchpath == 0)
  {
    defaultsearchpath = 0;
    coresearchpath = 0;
  }
  else
  {
    defaultsearchpath = new StringArray(*initsearchpath);
    coresearchpath = new StringArray(*initsearchpath);
  }

  searchpath = new Dir( initsearchpath, this);

  // Set the global variables for the action and pass name so
  // that they can be test against during parsing of the Makefiles.
  initVars();

  var_eval = new Variable(root_vars, searchpath, virtual_cwd, environ_vars, true);

  // Update with VPATH if set in the User's Environment.
  updateSearchPathsbyVPATH();
}

/***************************************************************
 *  The constructor
 *    This constructor is used for partial PassNode construction.
 *    This is an attempt to save memory since most PassNodes are
 *    never executed.
 */
PassNode::PassNode(const String &initname, const String &initactionname, const String &initcwd,
                   const String &initmakedir, const String &initmaketop,
                   Makefile *initmakefile, Makefile *initparentmakefile,
                   const StringArray *initsearchpath) :
    GraphNode( initname ),
    virtual_cwd( initcwd ),
    make_top( initmaketop ),
    make_dir( initmakedir ),
    mf( initmakefile ),
    parentdirpass( 0 ),
    parentmf( initparentmakefile ),
    tgtgraph( 0 ),
    subdirpasses( 0 ),
    actionname( initactionname ),
    makeincludecompat( false ),
    passndid( actionname + id ),
    environ_varsarr( 0 ),
    environ_charstararr( 0 ),
    environ_vars( 0 ),
    global_vars( 0 ),
    cmdline_vars( 0 ),
    root_vars( 0 ),
    firstchar( '\0' ),
    tgtlst( 10 ),
    transforms( 0 ),
    patterns( 0 ),
    var_eval( 0 ),
    is_precious( false ),
    is_parsed( false )
{
  // If Makeconf has been read then clone its environment variables,
  // otherwise get it straight from the environment.
  if (initsearchpath == 0)
  {
    defaultsearchpath = 0;
    coresearchpath=0;
  }
  else
  {
    defaultsearchpath = new StringArray(*initsearchpath);
    coresearchpath = new StringArray(*initsearchpath);
  }

  searchpath = new Dir( initsearchpath, this);
}

PassNode::~PassNode()
{
  delete tgtgraph;
  delete searchpath;
  delete environ_vars;
  delete global_vars;
  delete cmdline_vars;
  delete transforms;
  delete patterns;
  delete var_eval;
  delete defaultsearchpath;
  delete coresearchpath;
  delete environ_varsarr;
  delete subdirpasses;
  delete[] environ_charstararr;
}

void PassNode::initVars( )
{
  // If Makeconf has been read then clone its environment variables,
  // otherwise get it straight from the environment.
  environ_varsarr = 0;
  environ_charstararr = 0;
  if (Make::mk->mkconfpass == 0)
  {
    environ_vars = (SetVars *)Env().clone();
    global_vars  = new SetVars( environ_vars );
    cmdline_vars = new SetVars( global_vars );

    // If -e flag was given, then swap the search order for variables.
    if (MkCmdLine::envOverride())
    {
      cmdline_vars->setParent( environ_vars );
      environ_vars->setParent( global_vars );
      global_vars->setParent( 0 );
    }
  }
  else
  {

    // If -e flag was given, then swap the search order for variables.
    if (MkCmdLine::envOverride())
    {
      environ_vars = (SetVars *)Make::mk->mkconfpass->getEnvironVars()->clone();
      global_vars  = (SetVars *)Make::mk->mkconfpass->getGlobalVars()->clone();
      cmdline_vars = new SetVars( environ_vars );
      environ_vars->setParent( global_vars );
      global_vars->setParent( 0 );
    }
    else
    {
      environ_vars = (SetVars *)Make::mk->mkconfpass->getEnvironVars()->clone();
      global_vars  = (SetVars *)Make::mk->mkconfpass->getGlobalVars()->clone();
      cmdline_vars = new SetVars( global_vars );
      global_vars->setParent( environ_vars );
      environ_vars->setParent( 0 );
    }

  } /* end if */

  // Command Line variables are always the first in the search list.
  root_vars = cmdline_vars;

  global_vars->set( Constants::MAKEACTION, actionname, true );
  global_vars->set( Constants::MAKEPASS, nameOf(), true );
  global_vars->set( Constants::CURDIR, virtual_cwd, true );

  // obsolete
  global_vars->set( Constants::DOT_CURDIR, virtual_cwd, true );

  global_vars->set( Constants::MAKETOP, make_top, true );
  global_vars->set( Constants::POUND, StringConstants::POUND_SIGN, true );
  environ_vars->set( Constants::MAKEDIR, make_dir, true );
  SandboxConstants::setMAKEDIR( make_dir );
  if (make_dir.length() > 1)
    global_vars->set( Constants::MAKESUB,
      make_dir.substring( ARRAY_FIRST_INDEX + 1 ) +
      StringConstants::FORW_SLASH, true );
  else
    // makedir must by just "/"
    global_vars->set( Constants::MAKESUB, make_dir, true );

  // Set the variables from the command line.
  Vector< String > cmdline_varsvec = MkCmdLine::getCommandLineVars();
  if (!cmdline_varsvec.isEmpty())
  {
    for (int varidx=cmdline_varsvec.firstIndex();
         varidx <= cmdline_varsvec.lastIndex();
         varidx++ )
    {
      cmdline_vars->put( *cmdline_varsvec.elementAt(varidx) );
      if ( MkCmdLine::dVars() )
        Interface::printAlways("Var: Setting command line variable " +
          (*cmdline_varsvec.elementAt(varidx)));
    }
  }

  // Set the global variables from the command line.
  Vector< String > global_varsvec=MkCmdLine::getGlobalVars();
  if (!global_varsvec.isEmpty())
  {
    for (int varidx=global_varsvec.firstIndex();
         varidx <= global_varsvec.lastIndex();
         varidx++ )
    {
      global_vars->put( *global_varsvec.elementAt(varidx) );
      if ( MkCmdLine::dVars() )
        Interface::printAlways("Var: Setting global variable " +
          (*global_varsvec.elementAt(varidx)));
    }
  }

  // Initialize the search variable .PATH corresponding to searchpath.
  setDotPathVar();

  // Initialize .TARGETS with command-line targets
  static String tgtsstr;
  tgtsstr = StringConstants::EMPTY_STRING;
  VectorEnumeration< String > tgt_enum( &MkCmdLine::getTgts() );
  while (tgt_enum.hasMoreElements())
  {
    tgtsstr += *tgt_enum.nextElement();
    if (tgt_enum.hasMoreElements())
      tgtsstr += StringConstants::SPACE;
  }
  global_vars->set( Constants::DOT_TARGETS, tgtsstr, true );
  if (MkCmdLine::dVars())
    Interface::printAlways("Var: Setting global variable .TARGETS= " + tgtsstr);

  // Test for MAKEINCLUDECOMPAT.
  if (getEnvironVars()->find(Constants::MAKEINCLUDECOMPAT) != 0)
  {
    if (MkCmdLine::dIncs())
      Interface::printAlways("Inc: MAKEINCLUDECOMPAT is set");
    makeincludecompat = true;
  }
}

/***************************************************************
 *  This method is executed each time an included makefile is parsed.
 *  The lines pointers and current working files are reinitialized to
 *  allow proper parsing
 */
void PassNode::reinit( Makefile &initmakefile )
{
  mf        = &initmakefile;
  mfs       = 0;
  firstchar = '\0';
  linestr   = StringConstants::EMPTY_STRING;
  tgtlst.clear();
}

/***************************************************************
 *  This method is used to save time and space.  There may be many
 * passes references in makefiles, but only a few are actually used.
 */
void PassNode::doDelayedConstruction()
{
  // If everything is in order then return.
  if (environ_vars != 0 && global_vars != 0 && cmdline_vars != 0)
    return;

  tgtgraph = new Graph();
  subdirpasses = new Vector<PassNode *>(5, elementsEqual);

  transforms = new SuffixTransforms();

  patterns = new Patterns();

  // Set the global variables for the action and pass name so
  // that they can be test against during parsing of the Makefiles.
  initVars();

  var_eval = new Variable(root_vars, searchpath, virtual_cwd, environ_vars, true);

  // Update with VPATH if set in the User's Environment.
  updateSearchPathsbyVPATH();
}

/***************************************************************
 * This private method is used to determine what subdirectories that
 * should be traversed for the current pass.
 */
StringArray *PassNode::getPassSubDirs( StringArray *buf )
  // throw (ParseException)
{
  // Look for the subdirectories defined in _<action>_<pass>_SUBDIRS_ then
  // _<pass>_SUBDIRS_
  boolean actionsubdir = true;
  String subdirvarstr = StringConstants::UNDERSCORE;
  subdirvarstr += nameOf();
  subdirvarstr += "_SUBDIRS_";
  String actsubdirvarstr = StringConstants::UNDERSCORE;
  actsubdirvarstr += actionname;
  actsubdirvarstr += subdirvarstr;

  try
  {
    const String *varval = findVar( actsubdirvarstr );

    if (varval == 0)
    {
      varval = findVar( subdirvarstr );
      if (varval == 0)
        return 0;
      actionsubdir = false;
    }
    if (actionsubdir) subdirvarstr = actsubdirvarstr; // for all debug prints

    StringArray result;
    parseUntil(*varval, StringConstants::EMPTY_STRING, false, &result);

    if ( MkCmdLine::dVars() )
      Interface::printAlways("Var: Getting "+subdirvarstr);

    if ( result[ARRAY_FIRST_INDEX].length() > 0 )
    {
      if ( MkCmdLine::dVars() )
        Interface::printAlways( "Var: Got " + subdirvarstr + StringConstants::EQUAL_SIGN +
            result[ARRAY_FIRST_INDEX] );

      if (buf != 0)
        return (result[ARRAY_FIRST_INDEX].split( StringConstants::SPACE_TAB, UINT_MAX, buf ));
      else
        return (result[ARRAY_FIRST_INDEX].split( StringConstants::SPACE_TAB ));
    }
  }
  catch ( MalformedVariable &e )
  {
    throw ParseException( String(0), 0, "Error processing variable \"" +
      subdirvarstr + "\" : " + e.getMessage());
  }
  return 0;
}

/***************************************************************
 * This private method is used to determine what targets should be updated
 * for the current action/pass combination.
 */
Vector<String> *PassNode::getPassTargets( GraphNode *parentnode )
  // throw (ParseException)
{

  // Look for the targets defined in _<action>_<pass>_TARGETS_
  //
  String tgtvarstr = StringConstants::UNDERSCORE;
  tgtvarstr += actionname;
  tgtvarstr += StringConstants::UNDERSCORE;
  tgtvarstr += nameOf();
  tgtvarstr += "_TARGETS_";

  try
  {
    const String *varval=findVar(tgtvarstr);

    if (varval == 0)
      return 0;

    StringArray result;
    parseUntil( *varval, StringConstants::EMPTY_STRING, false, &result );
    Vector<String> *tgtlist = new Vector<String>( 5 );

    if (result[ARRAY_FIRST_INDEX].length() != 0)
    {
      result[ARRAY_FIRST_INDEX].trimThis();
      if (result[ARRAY_FIRST_INDEX].length() == 0)
       tgtlist->addElement(StringConstants::EMPTY_STRING);
      else
      {
        StringArray tgtstrs;
        result[ARRAY_FIRST_INDEX].split( StringConstants::SPACE_TAB, UINT_MAX, &tgtstrs );
        String tgtname;
        for (int tgtidx=ARRAY_FIRST_INDEX; tgtidx<=tgtstrs.lastIndex(); tgtidx++)
        {
          tgtname = tgtstrs[tgtidx].trim();
          if (tgtname.length() > 0)
            tgtlist->addElement(tgtname);
        }
      }
      return tgtlist;
    }
  }
  catch ( MalformedVariable &e )
  {
    throw ParseException( String(0), 0, "Error processing variable \"" +
      tgtvarstr + "\" : " + e.getMessage() );
  }
  return 0;
}

/***************************************************************
 */
void PassNode::updateLocalTargets( GraphNode *parentnode,
                                   Vector< String > *tgts )
 // throw (ParseException)
{
  Vector<String> *localtgts = 0;

  // If the tgts list is null look up the current pass targets.
  if (tgts == 0 || tgts->isEmpty())
    localtgts = getPassTargets( parentnode );
  else
    localtgts = tgts;

  if (localtgts == 0 || localtgts->isEmpty())
  {
    GraphNode *tgtnd=getTgtGraph()->find(Constants::DOT_MAIN);

    // Delete the empty tgts list
    if (localtgts != 0)
      delete localtgts;

    // No targets to make.
    if (tgtnd == 0 || tgtnd->getChildren()->size() == 0)
      return;

    // FIXME: Clean this up later.  No need to take a list of targets, convert to list of
    // names, then convert back to list of names.
    localtgts = new Vector<String>( tgtnd->getChildren()->size() );
    for (int idx = localtgts->firstIndex(); idx <= localtgts->lastIndex(); idx++)
      localtgts->addElement( (*(tgtnd->getChildren()->elementAt(idx)))->nameOf() );
  }
  // If there is one target returned and it is the empty string, then
  // the _<action>_<pass>_TARGETS_ variable was defined but not equal to
  // anything.
  else if (localtgts->size() == 1 &&
           localtgts->elementAt( ARRAY_FIRST_INDEX )->length() == 0)
  {
    delete localtgts;
    return;
  }

  // Set the .TARGETS global variable.
  String tgtsstr;
  for (int i = localtgts->firstIndex(); i <= localtgts->lastIndex(); i++)
    tgtsstr += *localtgts->elementAt( i ) + StringConstants::SPACE;
  global_vars->set(Constants::DOT_TARGETS, tgtsstr, true);
  if (MkCmdLine::dVars())
    Interface::printAlways("Var: Setting global variable .TARGETS= " + tgtsstr);

  // Update the main targets for this node.
  TargetNode *tgt = 0, *tmptgt = 0;
  const String *tgtname;
  StringArray suffres(3);
  int tmpstate = UNMADE;

  for (int tgtidx=localtgts->firstIndex(); tgtidx <= localtgts->lastIndex();
       tgtidx++)
  {
    tgtname = localtgts->elementAt( tgtidx );
    tgt = (TargetNode *)getTgtGraph()->find(*tgtname);
    if (tgt == 0)
    {
      // See if there is a suffix transformation that exists
      // for the target.
      suffres.clear();
      Path::fileRootSuffixThis( *tgtname, suffres );
//      if (suffres[ARRAY_FIRST_INDEX].length() == 0 ||
//          getSuffTransforms()->findSuff(suffres[ARRAY_FIRST_INDEX+1]) == 0)
//      {
//        // First print the graph before exiting for additional debug info
//        if (MkCmdLine::dGraph2())
//        {
//          Interface::printAlways( "Graph: *** after the build" );
//          getTgtGraph()->printGraph( false );
//        }
//        throw ParseException( mfs->getPathname(), mfs->getLineNumber(),
//          "don't know how to make `" + *tgtname + "'." );
//      }
      tgt = (TargetNode *) getTgtGraph()->find( *tgtname );
      if (tgt == 0)
      {
        // Don't know which statement this TargetNode is coming from so
        // we'll create a dummy MakefileStatement
        tgt = new TargetNode( *tgtname, new MakefileStatement() );
        tmptgt = (TargetNode *) getTgtGraph()->insert(tgt);

        // If the tgt already existed then free the allocated TargetNode.
        if (tmptgt != tgt)
        {
          delete tgt;
          tgt = tmptgt;
        }
      }
    }
    // Now traverse the graph with this main node and mark nodes to be
    // made and add to the waiting list.
    tmpstate = tgt->update( 0, this, 0 );

    if (tmpstate == MADE || tmpstate == OUTOFDATE)
    {
      if (getState() != ERROR)
        setState( MADE );
    }
    else if (tmpstate == ERROR)
    {
      setState( ERROR );
      if (!MkCmdLine::keepGoing())
      {
        // Free up localtgts if allocated.
        if (localtgts != tgts)
          delete localtgts;

        return;
      }
    } /* end if */
  } /* end for */

  // Free up localtgts if allocated.
  if (localtgts != tgts)
    delete localtgts;
#ifdef __WEBMAKE__
  const String *noExtractFlag=Env::getenv("noExtract");
  if (noExtractFlag==0 ||
     (noExtractFlag!=0 && (*noExtractFlag).startsWith("0")))
#ifdef __WEBDAV__
  {
      FILE *trace=fopen("Trace.out","a+");
      fputs("Inside updateLocalTarget ,start to extract the input files\n",
            trace);
      fclose(trace);
      WEBDAV_autoExtract_dependency();
      trace=fopen("Trace.out","a+");
      fputs("End of updateLocalTarget\n",trace);
      fclose(trace);
  }
#endif // __WEBDAV__
#endif // __WEBMAKE__

  // Need to wait for the target node's commands to finish running.
  tmpstate = Job::doWaitingJobs();

  if (getState() == GraphNode::ERROR || tmpstate == Job::ERROR)
    setState( GraphNode::ERROR );
}

/***************************************************************
 *  Append the subdir to the end of the default search strings.
 *  the caller should deallocate the returned pointer or pass *buf
 */
StringArray *PassNode::createDefaultSearchPath( const String &subdir,
                                             StringArray *buf )
{
  if (buf == 0)
    return searchpath->appendSubDirToDefaultSearchPath(subdir);
  else
    return searchpath->appendSubDirToDefaultSearchPath(subdir, buf);
}

/****************************************************************
 * Create pass nodes for subdirs
 *
 */
int PassNode::updateSubDirTargets( StringArray subdirs )
  // throw (ParseException)
{
  if (getState() == ERROR) return ERROR;

  // Do recursion
  int childstate = UNMADE, tmpstate = UNMADE;
  boolean childhaserrors = false;
  if (subdirs.length() > 0)
  {
    int dirdepth;
    String cwd, subdir, newmake_dir, newmake_top;
    PassNode *subdirpassnd = 0;
    StringArray tmpdefaultpaths;

    for (int stridx = subdirs.firstIndex(); stridx <= subdirs.lastIndex();
         stridx++)
    {
      subdir = subdirs[stridx].trim();
      Path::unixizeThis( subdir );

      if (subdir.startsWith( StringConstants::FORW_SLASH ))
        subdir.substringThis( STRING_FIRST_INDEX + 1 );

      if (subdir.endsWith( StringConstants::FORW_SLASH ))
        subdir.substringThis( STRING_FIRST_INDEX, subdir.lastIndex() );

      if (subdir.length() == 0)
        continue;

      // Determine the Current Working Directory for the new PassNode
      cwd  = this->virtual_cwd;
      cwd += StringConstants::FORW_SLASH;
      cwd += subdir;
      if (this->make_dir.equals(StringConstants::FORW_SLASH))
      {
        newmake_dir  = StringConstants::FORW_SLASH;
        newmake_dir += subdir;
      }
      else
      {
        newmake_dir  = this->make_dir;
        newmake_dir += StringConstants::FORW_SLASH;
        newmake_dir += subdir;
      }

      // Determine the new make_top for the new passnode
      newmake_top = this->make_top;
      dirdepth = Make::getDirDepth( subdir ) + 1;
      for (int i = 0; i < dirdepth; i++)
        newmake_top += "../";

      tmpdefaultpaths.clear();
      createDefaultSearchPath( subdir, &tmpdefaultpaths );
      subdirpassnd = new PassNode(nameOf(), this->actionname,
           cwd, newmake_dir, newmake_top, 0, 0, this,
           &tmpdefaultpaths);

      // Create the path we are going to.
      Path::createPath( cwd );
      Interface::print("[ " + subdirpassnd->make_dir + " ] (entering)");

      // Save current MAKEFLAGS, restore after subdir is processed.
      static StringArray saveArgs = StringArray(10);
      saveArgs.clear();
      static int states = MkCmdLine::getArguments( saveArgs );

      // Determine the targets to be updated by parse the makefile in
      // in the subdirectory.
      Make::determineSubTargets( *subdirpassnd, *subdirpassnd->getSearchPath(),
        *subdirpassnd->getSysSearchPath() );

      tmpstate = subdirpassnd->update( this, subdirpassnd, 0 );
      if (tmpstate == MADE)
        childstate = MADE;
      else if (tmpstate == ERROR)
      {
        childhaserrors = true;

        if (!MkCmdLine::keepGoing( ))
        {
          // In order to save some memory, free this subdirectory PassNode.
          //
          delete subdirpassnd;

          break;
        }
      } /* end if */

      Interface::print("[ " + subdirpassnd->make_dir + " ] (returned from)");

      // In order to save some memory, free this subdirectory PassNode.
      delete subdirpassnd;

      // Restore the MAKEFLAGS to previous state.
      MkCmdLine::restoreArguments( saveArgs, states );
      Path::setcwd( this->virtual_cwd );

    } /* end for */

  } /* end if */

  // In order to save some memory, free the subdirectory PassNode's list.
  delete subdirpasses;
  subdirpasses = 0;

  if (childhaserrors)
    childstate = ERROR;

  return childstate;
}

/***************************************************************
 * Update the given target name.  If it doesn't exist then don't complain.
 */
int PassNode::updateTarget( const String &tgt )
  // throw (ParseException)
{
  GraphNode *gn = getTgtGraph()->find( tgt );
  if (gn != 0)
  {
    if (gn->update( this, this, 0 ) == GraphNode::ERROR)
      return (GraphNode::ERROR);

    if (Job::doWaitingJobs() == Job::ERROR)
      return (GraphNode::ERROR);
  }
  return (MADE);
}


/***************************************************************
 * Build new search path whether this is for general path or
 * a specific suffix path.
 */
void PassNode::buildNewSearchPath( const StringArray &suffcorepath, Dir *path )
{
  int i, j;
  static String      tmpstring;
  static StringArray tmparray;
  tmparray.clear();

  // Build new path by appending corepaths

  // reset path to 0 and rebuild it from scratch.
  path->setPath( (StringArray *)0 );

  // Create new searchpath by building it from the coresearchpath
  // and sequentially adding any relative vpaths. So that the resulting
  // searchpath should like something like the following.
  // 1. corepath1
  // 2. corepath1\vpath1
  // 3. corepath1\vpath2
  // 4. corepath2
  // 5. corepath2\vpath1
  // 6. corepath2\vpath2
  //     ...
  // n.   absolutevpath1
  // n+1. absolutevpath2
  //

  tmparray = suffcorepath;

  if (coresearchpath)
    tmparray.append( *coresearchpath );

  for ( i = tmparray.firstIndex();
        i <= tmparray.lastIndex(); i++ )
  {
    path->append( tmparray[i] );

    for ( j = vpaths_rel.firstIndex(); j <= vpaths_rel.lastIndex(); j++ )
    {
      tmpstring =  tmparray[i];
      tmpstring += vpaths_rel[j];
      path->append( Path::unixizeThis(
                           Path::canonicalizeThis( tmpstring, false ) ) );
    }
  }

  // Put all absolute vpaths at the end.
  path->append( vpaths_abs );
}


/***************************************************************
 * Update search path(s) with both relative and absolute VPATH's
 */
void PassNode::updateSearchPathsbyVPATH( Suffix *suff )
  // throw (ParseException)
{
  try
  {
    // VPATH should be added to all directories if it is a relative path.
    const String *vpathptr=getRootVars()->find( Constants::VPATH );

    vpaths_abs.clear();
    vpaths_rel.clear();

    if (vpathptr != 0)
    {
      StringArray vpaths;
      String vpath = *vpathptr;

      parseUntil( vpath, StringConstants::EMPTY_STRING, false, &vpaths );
      if (vpaths.length() != 0)
      {
        int i;                      // For loop variable.

        vpath = vpaths[ARRAY_FIRST_INDEX];
        vpaths.clear();
        Path::unixize( vpath ).split( Path::PATH_SEPARATOR, UINT_MAX, &vpaths );

        // Separate the relative vpaths from the absolute vpaths
        for ( i = vpaths.firstIndex(); i <= vpaths.lastIndex(); i++ )
        {
          vpaths[i].trimThis();
          if (Path::absolute( vpaths[i] ))
          {
            vpaths_abs.append( vpaths[i] );
          }
          else
          {
            vpaths_rel.append( StringConstants::FORW_SLASH + vpaths[i] );
          }
        }
      }
    }

    // Process only single .Path.suffix if parameter suff is non-zero.
    if (suff != 0)
    {
      Dir          temppath;

      if (suff->getCorePath().getPath())
        buildNewSearchPath( *(suff->getCorePath().getPath()), &temppath );
      else
        buildNewSearchPath( StringArray( (unsigned long)0 ), &temppath );

      suff->clearPath();
      suff->setPath( temppath );
    }
    else
    {
      // Update the general path AND all suffix paths.

      buildNewSearchPath( StringArray( (unsigned long)0 ), searchpath );

      transforms->updateAllSuffPaths( this );

    } /* end else */
  }
  catch ( MalformedVariable &e )
  {
    throw ParseException(mf->getPathname(), mfs->getLineNumber(),
      String( "Error in VPATH: " ) + e.getMessage());
  }
}

/***************************************************************
 * This defines the parent GraphNode's abstract update method.
 */
int PassNode::update( GraphNode *parentnode, PassInstance *passnode,
                         Vector<String> *tgts )
  // throw (ParseException)
{
  if (Signal::isInterrupted())
    Job::runInterruptHandler();

  int childstate = UNMADE;

  // If the PassNode was only constructed, but not parsed then parse it
  // here.  This could happen when the PassNode was constructed because
  // of the special source .PASSES
  doDelayedConstruction();

  // change directory to current working directory
  Path::setcwd( getCwd() );

  if (!is_parsed)
    parse();

  // Set the evironment variable array
  setEnvironVarsArr();

  if (MkCmdLine::dGraph1())
  {
    Interface::printAlways( "Graph: *** before the build" );
    getTgtGraph()->printGraph( true );
  }

  if (updateTarget( Constants::DOT_BEGIN ) == GraphNode::ERROR)
    setState( GraphNode::ERROR );
  else
  {
    // Only print out Pass and Action when this PassNode is the parent
    // directory PassNode or the root node of the graph.
    if (parentdirpass == 0 && !nameOf().equals( "root" ) &&
        !actionname.equals( "DEFAULT" ))
      Interface::print("Pass: "+nameOf()+", Action: "+actionname);

    StringArray subdirs;
    if (getPassSubDirs( &subdirs ))
      childstate = updateSubDirTargets( subdirs );

    updateLocalTargets( parentnode, tgts );
  }

  if (getState() == GraphNode::ERROR)
    childstate = GraphNode::ERROR;

  if (getState() == UNMADE)
  {
    // If both the PassNode and its child are "unmade" which implies
    // the PassNode doesn't have any children or associated targets.  So
    // Just mark the PassNode as "up-to-date".
    if (childstate == UNMADE)
      setState( UPTODATE );
    else
      setState( childstate );
  }

  // Run .END on normal error-free ending
  if (getState() != ERROR)
    if (updateTarget( Constants::DOT_END ) == GraphNode::ERROR)
    {
      Interface::printError( "mk: `.END' not made because of errors" );
      setState( GraphNode::ERROR );
    }

  // and .ERROR when an error occurs.
  if (getState() == ERROR)
    updateTarget( Constants::DOT_ERROR );

  // Run .EXIT targets no matter what.
  if (updateTarget( Constants::DOT_EXIT ) == GraphNode::ERROR)
  {
    Interface::printError( "mk: `.EXIT' not made because of errors" );
    setState( GraphNode::ERROR );
  }

  if (MkCmdLine::dGraph2())
  {
    Interface::printAlways( "Graph: *** after the build" );
    getTgtGraph()->printGraph( false );
  }

  if (!MkCmdLine::userBOMfile().isEmpty())
  {
    // As of Version 3.0, always append to the BOM regardless if the given
    // BOMfile is absolute or relative.
    Make::bf = Path::openFileWriter( MkCmdLine::userBOMfile(),
                                     true, true, false );

    time_t tme;
    time( &tme );
    String tmpstr = ctime( &tme );
    tmpstr.replaceThis( StringConstants::NEWLINE,
        StringConstants::EMPTY_STRING );

    Path::putLine( *(Make::bf), String( "# generated by mk version " ) +
        Version::VERSION + " on " + tmpstr );
    Path::putLine( *(Make::bf), String( "# MAKEFLAGS =")
      + ( MkCmdLine::userMakefile().length() == 0
          ?  StringConstants::EMPTY_STRING
          : String( " -f" ) + MkCmdLine::userMakefile() )
      + StringConstants::SPACE + *Env::getenv( Constants::MAKEFLAGS ) );
    Path::putLine( *(Make::bf), String( "# MAKEDIR = ")
      + getMakeDir()
      + StringConstants::NEWLINE + "#" );
    getTgtGraph()->writeBOM();
    Path::putLine( *(Make::bf), String( "# END OF MAKEDIR = " )
                   + getMakeDir() + StringConstants::NEWLINE );
    Path::closeFileWriter( Make::bf );
    Make::bf = 0;
  }

  return ( getState() );
}


/************************************************
  *  --- void PassNode::printNodeInfo ---
  *
  ************************************************/
void PassNode::printNodeInfo( boolean print_delayed_children )
{
  Interface::printAlways( "# Pass node `" + hashName() + "'");
  if (mfs)
  {
    Interface::printAlways( "# assigned in "
      + mfs->getPathname() + ", line " + mfs->getLineNumber() );
  }
}

/************************************************
  *   --- void PassNode::printNodeBOM ---
  *
  ************************************************/
void PassNode::printNodeBOM()
{
}

/*******************************************************************************
**/
boolean PassNode::isMainTarget( const String &tgt ) const
{
  const Vector<String> tgts=MkCmdLine::getTgts();
  VectorEnumeration< String > enumtgts( &tgts );
  while (enumtgts.hasMoreElements())
  {
    if (((String *)enumtgts.nextElement())->equals(tgt))
      return true;
  }
  return false;
}

#ifdef __WEBMAKE__
#ifdef __WEBDAV__
void PassNode::WEBDAV_autoExtract_dependency()
{
  //process the list of files in extractFiles and compare them
  //with the files in the WebDAVFilesTable, if the file exist in the
  //list, then we need to extract it.
   const String *server=Env::getenv("WEBDAV_SERVER");
   const String *root=Env::getenv("BUILDRESOURCE_ROOT");
   String urlString=(*server).substring(8,(*server).length()+1);
   int slash=urlString.indexOf('/');
   String hostAndPort=urlString.substring(STRING_FIRST_INDEX,slash);
   String prefix=urlString.substring(slash,urlString.length()+1);

   //process the list of files in extractList
   FILE *fp=fopen("timestamp.tmp","w+");
   FILE *trace=fopen("Trace.out","a+");
   fputs((String("WEBDAV_SERVER="+(*server)+"\n")).toCharPtr(),trace);
   fputs((String("BUILDRESOURCE_ROOT=")+(*root)+"\n").toCharPtr(),trace);
   StringArray *array=Make::extractFiles;
   String fileName;
   String href;
   WebResource *const *f=0;
   String buildroot=Path::unixize(*root);
   for(int i=array->firstIndex();i<=array->lastIndex();i++)
   {
      fputs((String("array[i]=")+(*array)[i]+"\n").toCharPtr(),trace);
      if (((*array)[i].toUpperCase()).indexOf(buildroot.toUpperCase())!=STRING_NOTFOUND)
         fileName=(*array)[i];
      else if (((*array)[i]).startsWith("/") || ((*array)[i]).startsWith("\\"))
         fileName=(buildroot)+(*array)[i];
      else
         fileName=(buildroot)+Path::DIR_SEPARATOR+(*array)[i];
      href=prefix+fileName.substring((buildroot).length()+1,fileName.length()+1);
      fileName=Path::normalize(fileName);
      href=Path::unixize(href);
      fputs((String("href=")+href+"\n").toCharPtr(),trace);
      if((f=(Make::WebDavFilesTable).get(href))!=0)
      {
       //compare the timeStamp,if timeStamp is different, write it to files
       //which will be passed to change timestamp

       int slash=fileName.lastIndexOf(Path::DIR_SEPARATOR);
       String localDir=fileName.substring(STRING_FIRST_INDEX,slash);
       struct stat statbuf;
       int rc;
       //put info into trace.out
       fputs((String("Local fileName=")+fileName).toCharPtr(), trace);
       fputs("\n",trace);
       //prepare for getting stat information
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
           String current_cmdstr=" java com.ibm.etools.webmake.make.GetFile -url http://"+hostAndPort+href+" -rootDir "+(*root)+" -webdavServer "+(*server)+" -changeTS 0";
           fputs((String("java command=")+current_cmdstr).toCharPtr(), trace);
           fputs("\n",trace);
           int rc=system(current_cmdstr.toCharPtr());
           if (rc!=0)
               exit(-1);
           //set the flag for smartBuild later
           (*f)->extracted=true;
       }

      }


   }
   fclose(fp);
   fclose(trace);
   //reset the extractFiles to null
   (*Make::extractFiles).clear();
   //change the timestamp
   const String *TCTIME=Env::getenv("TCTime");
   if(TCTIME==0 || (*TCTIME).startsWith("1"))
   {
      String current_cmdstr="Fhcutil timestamp.tmp  -TCTime";
      int rc=system(current_cmdstr.toCharPtr());
      if (rc!=0)
      exit(-1);
   }
}
#endif // __WEBDAV__
#endif // __WEBMAKE__
