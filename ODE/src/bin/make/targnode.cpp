using namespace std;
using namespace std;
#define _ODE_BIN_MAKE_TARGETNODE_CPP

#include <time.h>
#include "base/binbase.hpp"

#include "bin/make/passnode.hpp"
#include "bin/make/targnode.hpp"

#include "lib/exceptn/mfvarexc.hpp"
#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/string/variable.hpp"
#include "lib/util/arch.hpp"
#include "lib/util/signal.hpp"

#include "bin/make/graph.hpp"
#include "bin/make/job.hpp"
#include "bin/make/keyword.hpp"
#include "bin/make/makec.hpp"
#include "bin/make/mkcmdln.hpp"
#include "bin/make/suffix.hpp"
#include "bin/make/sufftrfm.hpp"

#ifdef __WEBMAKE__
#include <stdio.h>
#include <sys/stat.h>
#endif // __WEBMAKE__

/******************************************************************************
 * Since removeTargetFile is called from a signal handler we don't want to
 * access the heap.  That's why there some lower level calls to things like
 * fprintf and remove here.
 */
boolean TargetNode::removeTargetFile()
{
  // don't remove PRECIOUS Target(s)
  if (isSet( Constants::OP_PRECIOUS ) ||
       (passnode != 0 && ((PassNode *)passnode)->isPrecious()))
    return (true);
  String path;
  if (passnode == 0  || Path::absolute( nameOf() ))
    path = nameOf();
  else
  {
    path  = passnode->getCwd();
    path += Path::DIR_SEPARATOR;
    path += nameOf();
  }

  if (Path::deletePath( path ))
    Interface::printAlways( "*** " + nameOf() + " was removed" );

  return (true);
}

/******************************************************************************
 *
 */
void TargetNode::determineSuffix()
{
  static StringArray results( 3 );
  results.clear();
  Path::fileRootSuffixThis( id, results );

  if (results.length() >= 3)
  {
    id     = results[results.firstIndex()];
    suffix = results[results.firstIndex() + 1];
    prefix = results[results.firstIndex() + 2];
  }

  if (MkCmdLine::dTargs() && (prefix != Constants::EMPTY_TARGET))
    Interface::printAlways( "Targ: Target=" + id + " : pre=" + prefix +
                            " : suf=" + suffix );
}

/******************************************************************************
 *
 */
TargetNode *TargetNode::createAndInsertChild(
  const String &srcname, const PassInstance *passnd,
  const MakefileStatement *mfs,
  GraphNode *srcnd )
    // throws ParseException
{
  GraphNode *tmpnd = 0;
  if (srcnd == 0)
    srcnd = passnd->getTgtGraph()->find( srcname );
  else
    tmpnd = passnd->getTgtGraph()->insert( srcnd );

  if (srcnd == 0)
  {
    srcnd = new TargetNode( srcname, mfs );
    tmpnd = passnd->getTgtGraph()->insert( srcnd );

    // If the srcnd already existed then free the allocated TargetNode.
    if (tmpnd != srcnd)
    {
      delete srcnd;
      srcnd = tmpnd;
    }
  }

  // Dont allow a target to have itself as a dependent.
  if ( this ==  srcnd )
  {
    throw ParseException( getPathName(), getBeginLine(),
      String( "Graph cycles through `" ) + nameOf() + "'" );
  }

  this->addChild( srcnd );
  srcnd->addParent( this );

  return ((TargetNode *)srcnd);
}

/******************************************************************************
 */
int TargetNode::markOODate( GraphNode *parentnode )
{
  // Increment the num_running_children count.
  if (parentnode != 0 && (cmdsLength() > 0 || getNumRunningChildren() > 0))
    parentnode->incNumRunningChildren();

  // Finally mark this target as out of date and return;
  setState( OUTOFDATE );

  // If we are a leaf node of the out-of-date graph then start the job off
  // running now
  if (getNumRunningChildren() == 0)
    if (cmdsLength() > 0)
    {
      if (execCmds() == GraphNode::ERROR)
        setState( GraphNode::ERROR );
    }
    else
      setState( MADE );

  return (getState());
}

/******************************************************************************
 */
int TargetNode::execCmds( boolean add_last )
{
  if (passnode == 0 || getState() == GraphNode::ERROR)
    return (GraphNode::ERROR);

  // If we just need to touch the target then do so.
  if (MkCmdLine::touchTarget() &&
      !isSet( Constants::OP_MAKE ) && !isSet( Constants::OP_PMAKE ))
    return (touchTarget());

  if (cmdsLength() == 0)
  {
    // Since this target is considered made, make a call to
    // finish to notify parents that this child is done.
    finish();
    freeLocalVars();
    return (MADE);
  }

  // Set variables local to the node.  Things like .ALLSRC.
  setLocalVars( );

  int return_status = Job::addJobsWaiting( passnode, this, add_last );

  if (return_status == Job::ERROR)
    return (GraphNode::ERROR);

  return (MADE);
}

/************************************************
  *  --- void TargetNode::setLocalVars ---
  *
  ************************************************/
void TargetNode::setLocalVars( )
{
  static String     tmpstr;
  TargetNode *childnd;

  // Set the .IMPSRC/< variables
  //   This is the implied child to the node.
  if (impsrc != 0)
  {
    if (impsrc->getCF() == 0)
      tmpstr = impsrc->nameOf();
    else
      tmpstr = impsrc->getCF()->getUnixPath();
#ifdef __WEBMAKE__
      tmpstr=Path::normalize(tmpstr);
#endif // __WEBMAKE__
    local_vars->set( Constants::DOT_IMPSRC, tmpstr, true );
    local_vars->set( StringConstants::LESS_THAN, tmpstr, true );
  } /* end if */

  // Set the .ALLSRC/> variables
  //   This is a list of all immediate children nodes of this node.
  tmpstr = StringConstants::EMPTY_STRING;
  VectorEnumeration< GraphNode *> enumchildren( &children );
  while (enumchildren.hasMoreElements())
  {
    childnd = (TargetNode *)*enumchildren.nextElement();

    if (childnd == 0 || childnd->getNodeType() == PASS_NODE)
      continue;

    if (childnd->isSet( Constants::OP_INVISIBLE ))
      continue;

    if (childnd->getCF() == 0)
      tmpstr += childnd->nameOf();
    else
      tmpstr += childnd->getCF()->getUnixPath();

    // Only add a space if this isn't the last child.
    if (enumchildren.hasMoreElements())
      tmpstr += StringConstants::SPACE;
  }
#ifdef __WEBMAKE__
  tmpstr = Path::normalize(tmpstr);
  arch   = Path::normalize(arch);
  memb   = Path::normalize(memb);
#endif // __WEBMAKE__
  local_vars->set( Constants::DOT_ALLSRC, tmpstr, true );
  local_vars->set( StringConstants::BIGGER_THAN, tmpstr, true );

  // Set the .OODATE/? variables
  //   This is a list of all the immediate children that were out-of-date with
  //   respect to this node
  // Only need to build the OODATE string if oodate list is not empty, else
  // reuse .ALLSRC
  if (oodate.length() > 0)
  {
    tmpstr = StringConstants::EMPTY_STRING;
    VectorEnumeration< GraphNode * > enumoodate( &oodate );
    while (enumoodate.hasMoreElements())
    {
      childnd = (TargetNode *)*enumoodate.nextElement();

      if (childnd == 0 || childnd->getNodeType() == PASS_NODE)
        continue;

      if (childnd->isSet(Constants::OP_INVISIBLE))
        continue;

      if (childnd->getCF() == 0)
        tmpstr += childnd->nameOf();
      else
        tmpstr += childnd->getCF()->getUnixPath();

      // Only add a space if this isn't the last oodate child.
      if (enumoodate.hasMoreElements())
       tmpstr += StringConstants::SPACE;
    } /* end while */
  }
#ifdef __WEBMAKE__
  tmpstr = Path::normalize(tmpstr);
#endif // __WEBMAKE__
  local_vars->set( Constants::DOT_OODATE, tmpstr, true );
  local_vars->set( StringConstants::QUESTION_MARK, tmpstr, true );

}


/******************************************************************************
 */
int TargetNode::update( GraphNode *parentnode,
                        PassInstance *pass_reference,
                        Vector< String > *tgts )
                                    // throws ParseException
{
  int tgtstate = UNMADE, tmpstate = UNMADE;
  boolean tgthaserrors = false;
  TargetNode *tgtnode;

  // Preserve the reference to the associated PassNode.
  this->passnode = pass_reference;

  if (Signal::isInterrupted() && nameOf() != Constants::DOT_INTERRUPT &&
      nameOf() != Constants::DOT_EXIT)
    Job::runInterruptHandler();

  switch (getState())
  {
    // If this target has the state of: ERROR or MADE
    // then return
    case GraphNode::ERROR: // Fall through
    case MADE:
      return (getState());

    // If this target has the states of: OUTOFDATE
    // then return
    case OUTOFDATE:
    {
      // If this child node has been evaluated and will be updated then
      // increment the parent dependency count
      if (parentnode != 0 && !(parentnode->isSet( Constants::OP_DOUBLEDEP)) &&
         (cmdsLength() > 0 || getNumRunningChildren() > 0))
        parentnode->incNumRunningChildren();

      return (getState());
    } /* end case OUTOFDATE */

    // If this target is up-to-date then do the time comparison needed for
    // the parent then exit
    case UPTODATE:
    {
      if (parentnode != 0 && parentnode->getNodeType() == TARGET_NODE)
      {
        if (!isSet( Constants::OP_SPECTARG ) && cf == 0)
        {
          cf = getCachedFile( passnode );
        }
        if (cf != 0)
        {
          ((TargetNode *)parentnode)->setChildsModTime( this );
        }
      }
      return (getState());
    } /* end case UPTODATE */

    case UNMADE:
    {
      setState( MAKING );
      // Mark all linked targets as Made, so that we will not attempt
      // to update them simultaneously.
      setLinkedTargsState( MADE );
      break;
    } /* end case UNMADE */

    case MAKING:
    {
      throw ParseException( getPathName(), getBeginLine(),
        String( "Graph cycles through `" ) + nameOf() + "'" );
    } /* end case MAKING */

    default:
      // Do nothing
      break;
  } /* end switch */


  if (passnode == 0)
  {
    setState( GraphNode::ERROR );
    return (GraphNode::ERROR);
  }

  // If this target has the `::' operator, then update the other
  // double colon targets with the same name (the siblings).
  // We'll not update these `::' targets in parallel, since
  // this isn't used that often and has potential dangers by
  // updating the same target many different ways at the same time.
  if (isSet( Constants::OP_DOUBLEDEP ))
  {
    // First update the first double colon target
    tmpstate = this->updateTarget( parentnode, tgts );
    if (tmpstate == MADE)
      tgtstate = MADE;
    else if (tmpstate == OUTOFDATE)
      tgtstate = OUTOFDATE;
    else if (tmpstate == GraphNode::ERROR)
    {
      tgthaserrors = true;
      if (!MkCmdLine::keepGoing())
      {
        setState( GraphNode::ERROR );
        return (GraphNode::ERROR);
      }
    }

    // Now update all the siblings.
    VectorEnumeration< GraphNode * > enumsiblings( &siblings );
    while (enumsiblings.hasMoreElements())
    {
      // We know that siblings are TargetNode's so we cast them.
      tgtnode = ((TargetNode *)(*enumsiblings.nextElement()));

      // Preserve the reference to the associated PassNode.
      tgtnode->passnode = pass_reference;

      // Inherit parents from first sibling, hi mom
      tgtnode->parents = parents;

      tmpstate = tgtnode->updateTarget( parentnode, tgts );
      if (tmpstate == MADE)
        tgtnode->setState( MADE );
      else if (tmpstate == OUTOFDATE)
        tgtstate = OUTOFDATE;
      else if (tmpstate == GraphNode::ERROR)
      {
        tgthaserrors = true;
        if (!MkCmdLine::keepGoing())
        {
          tgtnode->setState( GraphNode::ERROR );
          return (GraphNode::ERROR);
        }
      }
    } /* end while */

    if (tgthaserrors)
    {
      setState( GraphNode::ERROR );
      return (GraphNode::ERROR);
    }
    else
    {
      setState( tgtstate );
      // if the the current target's state is UPTODATE, mark linked targets
      // UPTODATE so the linked targets' ancestors aren't marked OUTOFDATE
      if ( tgtstate == UPTODATE )
      {
        setLinkedTargsState( UPTODATE );
      }
      return ( tgtstate );
    }
  }
  else
  {
    tgtstate = updateTarget( parentnode, tgts );
    // if the the current target's state is UPTODATE, mark linked targets
    // UPTODATE so the linked targets' ancestors aren't marked OUTOFDATE
    if ( tgtstate == UPTODATE )
    {
      setLinkedTargsState( UPTODATE );
    }
    return tgtstate;
  }
}

/******************************************************************************
 */
int TargetNode::updateTarget( GraphNode *parentnode,
                        Vector< String > *tgts )
                                    // throws ParseException
{
  int childstate = UNMADE;
  boolean childhaserrors = false;

  // Local variable associated only to this node.  Allocated here freed by
  // Job waitFor or by uptodate condition
  if (local_vars == 0)
    local_vars = new SetVars( passnode->getRootVars() );
  if (local_runtime_vars == 0)
    local_runtime_vars = new SetVars( local_vars );
#ifdef __WEBMAKE__
  //put the target name into an Array which later will be processed and
  //compared with timestamp regard to TC's timestamp and extracted if
  //necessary
  boolean smartBuild=false;
  String cwd=passnode->getCwd();
#ifdef __WEBDAV__
  const String *root=Env::getenv("BUILDRESOURCE_ROOT");
#else
  const String *root=Env::getenv("TC_RESOURCE_ROOT");
#endif // __WEBDAV__
  String build_top=(*root);
  int index=cwd.toUpperCase().indexOf((Path::unixize(build_top)).toUpperCase());
  int octo_index=(nameOf()).toUpperCase().indexOf((Path::unixize(build_top)).toUpperCase());
  /*------------------------------------------------------------------+
  |  we have the following case where namOf() value might be          |
  |  1. with full path: like e:\v303\src\tcrs\client\tcextract.cpp    |
  |  2. with relative path: the file resides in the makefile directory|
  |  The first case need to truncate octo_top                         |
  |  The second case needs to prepend the current working directory,  |
  |  and then truncate the octo_top                                   |
  +------------------------------------------------------------------*/
  String name=Path::unixize(nameOf());
  if(octo_index!=STRING_NOTFOUND )
     (Make::extractFiles)->add(name.substring((*root).length()+1,name.length()+1));
  else if (index!=STRING_NOTFOUND && octo_index==STRING_NOTFOUND && (*root).length()==cwd.length())
     (Make::extractFiles)->add(name);
  else
     (Make::extractFiles)->add(Path::unixize(cwd.substring((*root).length()+1,cwd.length()+1))+
     Path::DIR_SEPARATOR+name);

#endif // __WEBMAKE__

  // Set the .TARGET/@ variables
  //   This is the name of the current node.
  if (isSet( Constants::OP_ARCHV ))
  {
    local_vars->set( Constants::DOT_TARGET, memb, true );
    local_vars->set( StringConstants::AT_SIGN, memb, true );
  }
  else
  {
#ifdef __WEBMAKE__
    local_vars->set( Constants::DOT_TARGET, Path::normalize(nameOf()), true );
    local_vars->set( StringConstants::AT_SIGN, Path::normalize(nameOf()),
                     true );
#else
    local_vars->set( Constants::DOT_TARGET, nameOf(), true );
    local_vars->set( StringConstants::AT_SIGN, nameOf(), true );
#endif // __WEBMAKE__
  }

  // Set the .PREFIX/* variables
  //   This is the prefix of this node.  It is .TARGET without the suffix
#ifdef __WEBMAKE__
  local_vars->set( Constants::DOT_PREFIX, Path::normalize(prefix), true );
  local_vars->set( StringConstants::STAR_SIGN, Path::normalize(prefix), true );
#else
  local_vars->set( Constants::DOT_PREFIX, prefix, true );
  local_vars->set( StringConstants::STAR_SIGN, prefix, true );
#endif // __WEBMAKE__

  // Set the .ARCHIVE/! variables
  //   This is the archive name (if it exists) of this node.
#ifdef __WEBMAKE__
  local_vars->set( Constants::DOT_ARCHIVE, Path::normalize(arch), true );
  local_vars->set( StringConstants::EXCLAMATION, Path::normalize(arch), true);
#else
  local_vars->set( Constants::DOT_ARCHIVE, arch, true );
  local_vars->set( StringConstants::EXCLAMATION, arch, true);
#endif // __WEBMAKE__

  // Set the .MEMBER/% variables
  //   This is the archive member name (if it exists) of this node.
#ifdef __WEBMAKE__
  local_vars->set(Constants::DOT_MEMBER, Path::normalize(memb), true);
  local_vars->set(StringConstants::PERCENT_SIGN, Path::normalize(memb), true);
#else
  local_vars->set(Constants::DOT_MEMBER, memb, true);
  local_vars->set(StringConstants::PERCENT_SIGN, memb, true);
#endif // __WEBMAKE__

  if (hasChildren())      // has not parsed children
    parseSources( delayed_children );


  // If the target hasn't been looked up in the cache do so now.
  if (!isSet( Constants::OP_SPECTARG ) && cf == 0)
  {
    cf = getCachedFile( passnode );
  }

#ifdef __WEBMAKE__
  //if target's file doesn't exist and it is source only, it means that
  //we have reach the leaves of the tree branch. ODE code check whether
  //the source file exists in the file system or not.  We need to extract
  //now to satisfy the requirement
  if (cf==0 && isSet( Constants::OP_SOURCEONLY ))
  {
     const String *noExtractFlag=Env::getenv("noExtract");
     if(noExtractFlag==0 || (noExtractFlag!=0 && (*noExtractFlag).startsWith("0"
)))
#ifdef __WEBDAV__
     {
        FILE *trace=fopen("Trace.out","a+");
        fputs("Inside targnode.cpp updateTarget\n",trace);
        fputs("Begin to extract \"source only\" files\n",trace);
        fclose(trace);
        WEBDAV_autoExtract_dependency();
        smartBuild=true;
     }
#endif // __WEBDAV__
  }

  // Look up the file again to make sure that we extract files from TC.
  if (!isSet( Constants::OP_SPECTARG ) && cf == 0)
  {
    if (isSet( Constants::OP_ARCHV ))
      cf = passnode->getSearchPath()->findArchMemb( arch, memb );
    else
    {
      cf = passnode->getSearchPath()->findFile( nameOf() );

      // If the file wasn't found then look to the file system directly
      // in case this file was updated by a different target.
      if (cf == 0)
      {
        if (Path::absolute( nameOf() ))
          cf = passnode->getSearchPath()->findFile( nameOf(), true );
        else
          cf = passnode->getSearchPath()->findFile( passnode->getCwd() +
                 StringConstants::FORW_SLASH + nameOf(), true );
      }
    }
  }
#endif // __WEBMAKE__

  if (parentnode != 0 && cf != 0 && parentnode->getNodeType() == TARGET_NODE)
  {
    ((TargetNode *)parentnode)->setChildsModTime( this );
  }


  // Determine any "implied" children via pattern rules
  // The result of the getImpSrcs will be added children to this
  // node when appropriate.  So any work on the kids should take
  // place after this call. Don't go looking for implied sources
  // if one has already been assigned.
  // but for archive target, don't get implied children.
  if ((impsrc == 0) && !hasCmds() &&
      !isSet( Constants::OP_SPECTARG ) &&
      !isSet( Constants::OP_ARCHV ))
  {
    try
    {
      if (passnode->getRootVars()->
                          find(StringConstants::ODEMAKE_TFMFIRST_VAR ) != 0 )
      {
        if (!passnode->getSuffTransforms()->getImpSrcs( this, *passnode,
                                                        *local_vars))
        {
          passnode->getPatterns()->getImpSrcs( this, *passnode, *local_vars );
        }
      }
      else if (!passnode->getPatterns()->getImpSrcs( this, *passnode,
                                                     *local_vars))
      {
        passnode->getSuffTransforms()->getImpSrcs( this, *passnode,
                                                   *local_vars );
      }
    }
    catch ( ParseException &e )
    {
      Make::error( e.toString() );
      setState( ERROR );
      return (ERROR);
    }
  }

  if (MkCmdLine::dModTime())
  {
    if (children.length() == 0)
      Interface::printAlways(
        "Mod: Evaluating `" + id + "' with no sources" );
    else
    {
      VectorEnumeration< GraphNode *> enum_children( &children );
      String children_str;
      while (enum_children.hasMoreElements())
      {
        children_str += (*enum_children.nextElement())->nameOf();
        children_str += StringConstants::SPACE;
      }
      Interface::printAlways(
        "Mod: Evaluating `" + id + "' with sources: " +
        children_str );
    }
  }

  // Time to update the children
  if (children.length() > 0)
    childstate = updateChildren( parentnode, passnode, tgts );

  if (childstate == GraphNode::ERROR)
    childhaserrors = true;

  // If a child has returned with an error then return.
  if (childhaserrors)
  {
    setState( GraphNode::ERROR );
    return (GraphNode::ERROR);
  }

  // First see if we don't care if its out-of-date.  This is the
  // implementation of the '!' dependency character.
  if (MkCmdLine::forceRebuild() || isSet( Constants::OP_FORCEDEP ) ||
      (isSet(Constants::OP_DOUBLEDEP) && children.length() == 0) ||
      linkedTargNeedsUpdate( cmtime ))
  {
    setTargetmkType( FORCED );

    if (cf == 0)
    {
      if (isSet( Constants::OP_SOURCEONLY ))
      {
        // First print the graph before exiting for additional debug info
        if (MkCmdLine::dGraph2())
        {
          Interface::printAlways( "Graph: *** after the build" );
          passnode->getTgtGraph()->printGraph( false );
        }
        throw ParseException( mfs->getPathname(), mfs->getLineNumber(),
            "don't know how to make `" + nameOf() + "'" +
            StringConstants::PERIOD );
      }
    }

    markOODate( parentnode );
  }
  else if (childstate == MADE || childstate == OUTOFDATE)
  {
    setTargetmkType( MOD );

    markOODate( parentnode );
  }
  // If the child doesn't exist (this node) then check to see if it
  // is only a source, if so report an error.
  else if (cf == 0)
  {
    if (!isSet( Constants::OP_MEMBER))
    {
      setTargetmkType( NOTEXIST );
      markOODate( parentnode );
    }
    if (isSet( Constants::OP_SOURCEONLY ))
    {
      // First print the graph before exiting for additional debug info
      if (MkCmdLine::dGraph2())
      {
        Interface::printAlways( "Graph: *** after the build" );
        passnode->getTgtGraph()->printGraph( false );
      }
      throw ParseException( mfs->getPathname(), mfs->getLineNumber(),
        "don't know how to make `" + nameOf() + "'" + StringConstants::PERIOD );
    }
  }
  // This is the (parent < child) test to see if this node is really
  // up-to-date.  The comparison is done with the youngest child's
  // modification time
#ifdef __WEBMAKE__
#ifdef __WEBDAV__
  //add smartBuild is set, mark the target out of date
  else if (getModTime() < cmtime || smartBuild)
#endif // __WEBDAV__
#else
  else if (getModTime() < cmtime)
#endif // __WEBMAKE__
  {
    if (MkCmdLine::dModTime())
    {
      TargetNode * childnd;
      VectorEnumeration< GraphNode * > enumoodate( &oodate );
      while (enumoodate.hasMoreElements())
      {
        childnd = (TargetNode*)*enumoodate.nextElement();
        Interface::printAlways(
          String( "Mod: `" ) + nameOf() +
          "' is an out-of-date wrt `" + childnd->nameOf() + String("'") );
        Interface::printAlways(
          String( "Mod: ModTime of `" ) + nameOf() +
          "' is " + getModTimeString() );
        Interface::printAlways(
          String( "Mod: ModTime of `" ) + childnd->nameOf() +
          "' is " + childnd->getModTimeString() );
      }
    }
#ifdef __WEBMAKE__
    //For the extracted source-only file, we need to mark it UPTODATE
    //otherwise it will cause graph cycles if the source files shows up in
    //multiple dependent lists
    if (smartBuild && (getModTime() > cmtime))
      setState(UPTODATE);
#endif // __WEBMAKE__
    setTargetmkType( OODWRT );
    if (hasCmds()) markOODate( parentnode );
  }
  // Check archive member source against object in archive,
  // if object file doesn't exist.
  else if( parentnode != 0 && parentnode->isSet( Constants::OP_MEMBER ))
  {
    if( !(((TargetNode *)parentnode)->getCF()) )
    {
      CachedArchMember *scf = passnode->getSearchPath()->findArchMemb(
        ((TargetNode *)parentnode)->getArchName(),
        ((TargetNode *)parentnode)->getMembName() );
      // create or update archive member, if it's older than source
      if (scf == 0)
      {
        if (MkCmdLine::dModTime())
          Interface::printAlways( String( "Mod: Archive member `" )
            + ((TargetNode *)parentnode)->getMembName() +
            "' does not exist in `" + ((TargetNode *)parentnode)->getArchName() +
            "' and will be created" );
        markOODate( parentnode );
      }
      else
      {
        time_t membmodtime = scf->getModTime();
        if(membmodtime < getModTime())
        {
          String tmpstr( ctime( &membmodtime ) );
          tmpstr.replaceThis( StringConstants::NEWLINE,
            StringConstants::EMPTY_STRING );
          if (MkCmdLine::dModTime())
            Interface::printAlways( String( "Mod: ModTime of archive member `" )
              + ((TargetNode *)parentnode)->getMembName() +
              "' in `" + ((TargetNode *)parentnode)->getArchName() +
              "' is " + tmpstr );
          markOODate( parentnode );
        }
        else
        {
          setState( UPTODATE );             // source is up-to-date
          setTargetmkType( UTD );
          parentnode->setState( UPTODATE ); // implied object is up-to-date
          ((TargetNode *)parentnode)->setTargetmkType( UTD );
        }
      }
    }
    else
    {
      setState( UPTODATE );               // source is up-to-date
      setTargetmkType( UTD );
    }
  }
  // Otherwise the target is up-to-date.
  else
  {
    if (MkCmdLine::dModTime())
      Interface::printAlways( String( "Mod: Target `" ) + nameOf() +
                              "' is up-to-date" );
    setTargetmkType( UTD );
    setState( UPTODATE );
    freeLocalVars();
  }

  if (isOrdered() || isSet( Constants::OP_MAKE ))
  {
    if (getState() == OUTOFDATE)
    {
      // Need to wait for the target node's commands to finish running.
      if (Job::doWaitingJobs() == Job::ERROR)
        setState( GraphNode::ERROR );
      else
        setState( MADE );
    }

    // Take the placeholder back off
    if (parentnode != 0)
      parentnode->decNumRunningChildren();

  } /* end if */

  return (getState());
} /* end updateTarget */

#ifdef __WEBMAKE__
#ifdef __WEBDAV__
void TargetNode::WEBDAV_autoExtract_dependency()
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
   fputs((String("BUILDRESOURCE_ROOT="+(*root)+"\n")).toCharPtr(),trace);
   StringArray *array=Make::extractFiles;
   String fileName;
   String href;
   WebResource *const *f=0;
   String buildroot=Path::unixize(*root);
   for(int i=array->firstIndex();i<=array->lastIndex();i++)
   {
      fputs((String("array[i]="+(*array)[i]+"\n")).toCharPtr(),trace);
      if (((*array)[i].toUpperCase()).indexOf(buildroot.toUpperCase())!=STRING_NOTFOUND)
         fileName=(*array)[i];
      else if (((*array)[i]).startsWith("/") || ((*array)[i]).startsWith("\\"))
         fileName=(buildroot)+(*array)[i];
      else
         fileName=(buildroot)+Path::DIR_SEPARATOR+(*array)[i];
      href=prefix+fileName.substring((buildroot).length()+1,fileName.length()+1);
      fileName=Path::normalize(fileName);
      href=Path::unixize(href);
      fputs((String("href="+href+"\n")).toCharPtr(),trace);
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
          //write the timestamp to change the file's timestamp to match
          // server's
           fputs(fileName.toCharPtr(),fp);
           fputs("   ",fp);
           fputs((*f)->getLastModified().toCharPtr(),fp);
           fputs("\n",fp);
           String current_cmdstr=" java com.ibm.etools.webmake.make.GetFile "
             "-url http://"+hostAndPort+href+" -rootDir "+(*root)+
             " -webdavServer "+(*server)+" -changeTS 0";
           fputs((String("java command="+current_cmdstr)).toCharPtr(), trace);
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
#endif
#endif
int TargetNode::updateChildren(
  GraphNode *parentnode, PassInstance *passnode, Vector< String > *tgts )
{
  GraphNode *childnd;
  int childstate = UNMADE;
  boolean childhaserrors = false;
  Vector< GraphNode* > ordered_children, unordered_children;
  orderChildren( unordered_children, ordered_children );
  // We now do the "ordered" children.  The ordered children will wait
  // until they are updated before continuing.
  VectorEnumeration< GraphNode * > enumSomeChildren( &ordered_children );
  while (enumSomeChildren.hasMoreElements())
  {
    childnd = *enumSomeChildren.nextElement();
    if (childnd == 0)
      continue;

    switch (childnd->update( this, passnode, tgts ))
    {
      case OUTOFDATE: // FALL THROUGH
      case MADE:
        childstate = MADE;
        oodate.addElement( childnd );
        break;

      case GraphNode::ERROR:
        childhaserrors = true;
        if (!MkCmdLine::keepGoing())
          return (GraphNode::ERROR);
        break;

      default:
        break;

    } /* end switch */
  } /* end for */

  // We now do the "unordered" children.  The unordered children will NOT wait
  // until they are updated before continuing.
  enumSomeChildren.setObject( &unordered_children );
  while (enumSomeChildren.hasMoreElements())
  {
    childnd = *enumSomeChildren.nextElement();
    if (childnd == 0)
      continue;

    switch (childnd->update( this, passnode, tgts ))
    {
      case OUTOFDATE: // FALL THROUGH
      case MADE:
        childstate = MADE;
        oodate.addElement( childnd );
        break;

      case GraphNode::ERROR:
        childhaserrors = true;
        if (!MkCmdLine::keepGoing())
          return (GraphNode::ERROR);
        break;

      default:
        break;
    } /* end switch */
  } /* end for */

  if (childhaserrors)
    return (GraphNode::ERROR);
  else
    return (childstate);
}

int TargetNode::touchTarget()
{
  boolean success;
  String name_of = nameOf();

  // Since we may be running commands (those marked by .MAKE) then we need
  // to properly handle the 'running_children' counts
  GraphNode * const * parent;
  VectorEnumeration< GraphNode *> parent_enum( &parents );
  setState( MADE );

  while (parent_enum.hasMoreElements())
  {
    parent = parent_enum.nextElement();

    // Only update those parents that need to be, we could have parents
    // that are up-to-date, we hope not to have any that are MADE.
    switch((*parent)->getState())
    {
      case OUTOFDATE:
      case MAKING:
      case GraphNode::ERROR:
      {
        // Decrement parent num_runnning_children count.
        (*parent)->decNumRunningChildren();
        break;
      }
      case MADE:
      {
        // If we notice that the parent has already been made, then this is
        // a problem that should be reported.  This should never happen (if
        // this code is written properly)
        Make::quit( "Internal: Illegally made parent `" +
                     (*parent)->nameOf() + "' before all children were made",
                    1 );

        break;
      } /* end case MADE */
      // Ignore other cases
    } /* end switch */
  } /* end while */


  if (Keyword::isSpecialTarget( name_of ) != 0 ||
      isSet( Constants::OP_SPECTARG ))
  {
    Interface::print( Constants::MAKE_NAME + String( ": Not touching `" ) +
        name_of + "' (special target)" );
    return (MADE);
  }

  if (isSet( Constants::OP_ARCHV ) && cf == 0)
    name_of = arch;

  Interface::printAlways( Constants::MAKE_NAME + String( ": Touching `" ) +
      name_of + String( "'" ) );

  if (cf == 0)
  {
    if (passnode == 0  || Path::absolute( name_of ))
      success = Path::touch( name_of );
    else
      success = Path::touch( passnode->getCwd() +
          Path::DIR_SEPARATOR + name_of );
  }
  else
    success = cf->File::touch( nameOf() );

  if (!success)
    Interface::printWarning( Constants::MAKE_NAME +
        String( ": Unable to touch `" ) + name_of +
        "' (may not exist)" );

  return (MADE);
}

void TargetNode::reloadCachedFile()
{
  if (!isSet( Constants::OP_SPECTARG ))
  {
    // If the cached file existed before then we need to look up the new
    // location or restat
    CachedFile *ncf;
    boolean dirs_only = isSet( Constants::OP_DIRS );

    if (isSet( Constants::OP_ARCHV ))
      ncf = passnode->getSearchPath()->findArchMemb( arch, memb );
    else
    {
      if (Path::absolute( nameOf() ))
        ncf = passnode->getSearchPath()->findFile( nameOf(), true, dirs_only );
      else
      {
        ncf = passnode->getSearchPath()->findFile( passnode->getCwd() +
                StringConstants::FORW_SLASH + nameOf(), true, dirs_only);
        if (ncf == 0)   // Absolute lookup failed, search in relative path
          ncf = passnode->getSearchPath()->findFile( nameOf(), true, dirs_only );
      }
    }

    if (cf != 0)
    {
      if (cf == ncf)
        cf->restat();
      else if (ncf != 0)
        cf = ncf;
    }
    else
      cf = ncf;

  }

}

int TargetNode::finish( int job_state, const MakefileStatement *errmfs )
{
  int return_status = MADE;
  GraphNode * const * parent;
  VectorEnumeration< GraphNode *> parent_enum( &parents );
  setState( job_state );

  if (job_state == MADE)
    // Restat/Recache file if need
    reloadCachedFile();

  if (getState() == GraphNode::ERROR)
    if (errmfs)
      Make::error( "\"" + errmfs->getPathname() + "\", line " +
                   errmfs->getLineNumber() +
                   ": shell command failed for target `" + nameOf() + "'" );
    else
      Make::error( "\"Unknown Makefile\": shell command failed for target `"
                   + nameOf() + "'" );

  while (parent_enum.hasMoreElements())
  {
    parent = parent_enum.nextElement();

    // In order to ensure the parent doesn't update if the
    // child fails, we mark the parent as failed here.
    if (getState() == GraphNode::ERROR)
      (*parent)->setState( GraphNode::ERROR );

    // Only update those parents that need to be, we could have parents
    // that are up-to-date, we hope not to have any that are MADE.
    switch((*parent)->getState())
    {
      case MAKING: // If we are brought up-to-date while still determining
                   // dependencies.  For example, usage of .ORDER

        // Only decrement parent num_runnning_children count.
        // Don't start any jobs since parent is not ready
        (*parent)->decNumRunningChildren();
        break;

      case OUTOFDATE:
      case GraphNode::ERROR:
      {
        // First decrement parent num_runnning_children count.
        (*parent)->decNumRunningChildren();

        // If any parents are ready to be made then add them to the waiting
        // list.
        if ((*parent)->getNumRunningChildren() == 0)
          return_status = (*parent)->execCmds( false );

        // Verify return status from Job
        if (return_status == GraphNode::ERROR)
        {
          setState( GraphNode::ERROR );
          (*parent)->setState( GraphNode::ERROR );
          if (!MkCmdLine::keepGoing())
            return (GraphNode::ERROR);
        }
        break;
      } /* end case OUTOFDATE */

      case MADE:
      {
        // If we notice that the parent has already been made, then this is
        // a problem that should be reported.  This should never happen (if
        // this code is written properly)
        Make::quit( "Internal: Illegally made parent `" +
                     (*parent)->nameOf() + "' before all children were made",
                    1 );

        break;
      } /* end case MADE */
    } /* end switch */
  } /* end while */

  // Free up the local variables hashtable
  freeLocalVars();

  return (return_status);
}

/************************************************
  *--- void TargetNode::printModTimeInfo ---
  *
  ************************************************/
void TargetNode::printModTimeInfo()
{
  switch (getTargetmkType())
  {
    case FORCED:
      Interface::printAlways( "Mod: Making `" + nameOf()
        + "' since it is a 'force' target");
      break;
    case MOD:
      Interface::printAlways( "Mod: Making `" + nameOf()
        + "' since source was modified");
      break;
    case NOTEXIST:
      Interface::printAlways( "Mod: Making `" + nameOf()
        + "' since it doesn't exist");
      break;
    case OODWRT:
      Interface::printAlways( "Mod: Making `" + nameOf()
        + "' since it is out-of-date");
      break;
    case UTD:
      Interface::printAlways( "Mod: ERROR: target `" + nameOf()
        + "' is up-to-date!");
      break;
    default:
      Interface::printAlways( "Mod: ERROR: target `" + nameOf()
        + "' has a wrong mktype!");
  }
}

/************************************************
  * --- void TargetNode::printNodeInfo ---
  *
  ************************************************/
void TargetNode::printNodeInfo( boolean print_delayed_children )
{
  String depspec = StringConstants::EMPTY_STRING;
  String liststr = StringConstants::EMPTY_STRING;
  String specsrc = StringConstants::EMPTY_STRING;

  Interface::printAlways( "# Target node `" + nameOf() + "'" );
  Interface::printAlways( "#   assigned in "
    + defnmfs->getPathname() + ", line " + defnmfs->getLineNumber() );

  if (cf != 0)
  {
    liststr = "#   last modified ";
    liststr += getModTimeString();
    Interface::printAlways( liststr );
  }

  switch (getState())
  {
    case UNMADE:
      Interface::printAlways( String( "# State: UNMADE" ) );
      break;
    case MAKING:
      Interface::printAlways( String( "# State: MAKING" ) );
      break;
    case MADE:
      Interface::printAlways( String( "# State: MADE" ) );
      break;
    case UPTODATE:
      Interface::printAlways( String( "# State: UPTODATE ") );
      break;
    case OUTOFDATE:
      Interface::printAlways( String( "# State: OUTOFDATE ") );
      break;
    case ERROR:
      Interface::printAlways( String( "# State: ERROR ") );
      break;
    default:
      Interface::printAlways( String( "# Error: Undefined state! ") );
      break;
  }

  if (isSourceOnly())
    Interface::printAlways( "# This target is only a source.");

  liststr = "# Parents: ";
  liststr += getListOf( getParents() );
  Interface::printAlways( liststr );

  if (isSet( Constants::OP_DOUBLEDEP ))
  {
    depspec = " :: ";
  }
  else if (isSet( Constants::OP_FORCEDEP ))
  {
    depspec = " ! ";
  }
  else
  {
    depspec = " : ";
  }

  if (isSet( Constants::OP_DIRS ))
  {
    specsrc += ".DIRS ";
  }
  if (isSet( Constants::OP_FORCEBLD ))
  {
    specsrc += ".FORCEBLD ";
  }
  if (isSet( Constants::OP_INVISIBLE ))
  {
    specsrc += ".INVISIBLE ";
  }
  if (isSet( Constants::OP_LINK ))
  {
    specsrc += ".LINKS ";
  }
  if (isSet( Constants::OP_MAKE ))
  {
    specsrc += ".MAKE ";
  }
  if (isSet( Constants::OP_NOREMOTE ))
  {
    specsrc += ".NOREMOTE ";
  }
  if (isSet( Constants::OP_NORMTARG ))
  {
    specsrc += ".NORMTARG ";
  }
  if (isSet( Constants::OP_NOTMAIN ))
  {
    specsrc += ".NOTMAIN ";
  }
  if (isSet( Constants::OP_PASSES ))
  {
    specsrc += ".PASSES ";
  }
  if (isSet( Constants::OP_PMAKE ))
  {
    specsrc += ".PMAKE ";
  }
  if (isSet( Constants::OP_PRECIOUS ) ||
       (passnode != 0 && ((PassNode *)passnode)->isPrecious()))
  {
    specsrc += ".PRECIOUS ";
  }
  if (isSet( Constants::OP_PRECMDS ))
  {
    specsrc += ".PRECMDS ";
  }
  if (isSet( Constants::OP_POSTCMDS ))
  {
    specsrc += ".POSTCMDS ";
  }
  if (isSet( Constants::OP_REPLCMDS ))
  {
    specsrc += ".REPLCMDS ";
  }
  if (isSet( Constants::OP_REPLSRCS ))
  {
    specsrc += ".REPLSRCS ";
  }
  if (isSet( Constants::OP_SPECTARG ))
  {
    specsrc += ".SPECTARG ";
  }

  liststr = StringConstants::EMPTY_STRING;
  if (hasChildren())
  {
    liststr += getListOf( getChildren() );
    if (print_delayed_children)
    {
      liststr += StringConstants::SPACE;
      liststr += getDelayedChildren();
    }
  }

  Interface::printAlways( nameOf() + depspec + specsrc + liststr );

  if (hasCmds())
  {
    liststr = StringConstants::EMPTY_STRING;
    setFirstCmd();
    const Command *cmd = getNextCmd();
    while (cmd)
    {
      liststr += cmd->getCmdName();
      cmd = getNextCmd();
      if (cmd)
        liststr += StringConstants::NEWLINE;
    }
    Interface::printAlways( liststr );
  }
  if (isSet( Constants::OP_DOUBLEDEP ))
  {
    // now get siblings and print all of them as targets
    VectorEnumeration< GraphNode *> siblenum( getSiblings() );
    while (siblenum.hasMoreElements())
      ((GraphNode *) *siblenum.nextElement())->printNodeInfo( print_delayed_children );
  }
  Interface::printAlways( StringConstants::EMPTY_STRING );
}

/************************************************
  * --- String TargetNode::getModTimeString ---
  *
  ************************************************/
String TargetNode::getModTimeString()
{
  time_t modtime;

  if (cf)
  {
    if (isSet( Constants::OP_ARCHV ))
      modtime = ((CachedArchMember *) getCF())->getModTime();
    else
      modtime = getModTime();
    String tmpstr( ctime( &modtime ) );
    return ( tmpstr.replaceThis( StringConstants::NEWLINE,
                                 StringConstants::EMPTY_STRING ) );
  }

  return ( StringConstants::EMPTY_STRING );
}

/************************************************
  * --- void TargetNode::addLinkedTargs ---
  *
  ************************************************/
void  TargetNode::addLinkedTargs( Vector< TargetNode * > *tgts )
{
  TargetNode *tmptgt = 0;
  VectorEnumeration< TargetNode * > enumtgts( tgts );

  while (enumtgts.hasMoreElements())
  {
    tmptgt = *enumtgts.nextElement();
    if ( tmptgt != this )         //dont add self to list of linked targets
      linkedTargs.addWithoutDup( tmptgt );
  }
}

/************************************************
  *   --- void TargetNode::parseSources ---
  *
  ************************************************/
void TargetNode::parseSources( const String &srcs )
  // throw (ParseException)
{
  static StringArray temp, srcnames;
  GraphNode *srcnd = 0, *tmpnd = 0, *prev_passnd = 0;
  const Keyword *specsrc=0;
  Variable    var_eval( local_vars, passnode->getSearchPath(),
                          passnode->getCwd(), passnode->getEnvironVars(), true );

  try
  {
    if (MkCmdLine::dpVars())
    {
      VarInfoPrinter ip;
      var_eval.parseUntil( srcs, StringConstants::EMPTY_STRING, false, false, &temp, &ip );
    }
  else
      var_eval.parseUntil( srcs, StringConstants::EMPTY_STRING, false, false, &temp );
  }
  catch ( const MalformedVariable &e )
  {
    if (mfs->isNULL())
      throw ParseException( StringConstants::EMPTY_STRING, 0,
        srcs + StringConstants::COLON + e.getMessage() );
    else
      throw ParseException( mfs->getPathname(), mfs->getLineNumber(),
        srcs + StringConstants::COLON + e.getMessage() );
  }

  if (temp[temp.firstIndex()].length() == 0) return;  // no children
  srcnames.clear();
  temp[temp.firstIndex()].split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );

  VectorEnumeration< const MakefileStatement * > enum_child_mfs( &child_mfs );

  for (int srcidx=srcnames.firstIndex(); srcidx<=srcnames.lastIndex(); srcidx++)
  {
    String &srcname = srcnames[srcidx];
#ifdef FILENAME_BLANKS
    srcname.dequoteThis();
#endif
    if (srcname.length() == 0) continue; // empty variable/target

    // First test for special sources.
    if ((specsrc=Keyword::isSpecialSource( srcname )) != 0)
    {
      if (MkCmdLine::dTargs())
        Interface::printAlways( "Targ: Found special source `"+srcname+"'" );

      setType( specsrc->getVal() );

      // Look for .PASSES special source.
      switch (specsrc->getVal())
      {
        case Constants::OP_DIRS:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_DIRS");
          break;
        } /* case OP_DIRS */

        case Constants::OP_FORCEBLD:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_FORCEBLD");
          break;
        } /* case OP_FORCEBLD */

        case Constants::OP_INVISIBLE:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
              ": Evaluating special source OP_INVISIBLE");
          continue;
        } /* case OP_INVISIBLE */

        case Constants::OP_LINK:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_LINK");
          break;
        } /* case OP_LINK */

        case Constants::OP_MAKE:
        {
          if (srcname.equals( Constants::DOT_RECURSIVE ))
            Make::quit( "Special source .RECURSIVE is no longer supported. Use .MAKE instead.", 1 );
          else if (srcname.equals( Constants::DOT_MAKE ))
          {
            if (MkCmdLine::dTargs())
              Interface::printAlways("Targ: "+ nameOf() +
                ": Evaluating special source OP_MAKE");
          } /* end if */

          if (isSet( Constants::OP_PMAKE ))
          {
            clearType( Constants::OP_MAKE );
            if (MkCmdLine::dTargs())
              Interface::printAlways("Targ: "+ nameOf() +
                               ": .PMAKE is already set so .MAKE is ignored.");
          }
          break;
        } /* case OP_MAKE */

        case Constants::OP_NOREMOTE:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_NOREMOTE");
          break;
        } /* case OP_NOREMOTE */

        case Constants::OP_NORMTARG:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_NORMTARG");
          break;
        } /* case OP_NORMTARG */

        case Constants::OP_NOTMAIN:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_NOTMAIN");
          break;
        } /* case OP_NOTMAIN */

        case Constants::OP_PASSES:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: " + nameOf() +
              ": Evaluating special source OP_PASSES");

          Make::mk->setSpecialPasses();

          if (srcidx > ARRAY_FIRST_INDEX)
            throw ParseException(mfs->getPathname(), mfs->getLineNumber(),
              ".PASSES special source needs to be the first source on the line");
          else if (srcnames.length() == 1)
            throw ParseException(mfs->getPathname(), mfs->getLineNumber(),
              ".PASSES special source needs to be followed by pass names");
          break;
        } /* case OP_PASSES */

        case Constants::OP_PMAKE:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_PMAKE");

          if (isSet( Constants::OP_MAKE ))
          {
            clearType( Constants::OP_PMAKE );
            if (MkCmdLine::dTargs())
              Interface::printAlways("Targ: "+ nameOf() +
                               ": .MAKE is already set so .PMAKE is ignored.");
          }
          break;
        } /* case OP_PMAKE */

        case Constants::OP_PRECIOUS:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways( "Targ: " + nameOf() +
              ": Evaluating special source OP_PRECIOUS" );
          continue;
        } /* case OP_PRECIOUS */

        case Constants::OP_PRECMDS:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_PRECMDS");
          break;
        } /* case OP_PRECMDS */

        case Constants::OP_POSTCMDS:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_POSTCMDS");
          break;
        } /* case OP_POSTCMDS */

        case Constants::OP_REPLCMDS:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_REPLCMDS");
          break;
        } /* case OP_REPLCMDS */

        case Constants::OP_REPLSRCS:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_REPLSRCS");
          break;
        } /* case OP_REPLSRCS */

        case Constants::OP_SPECTARG:
        {
          if (MkCmdLine::dTargs())
            Interface::printAlways("Targ: "+ nameOf() +
                                   ": Evaluating special source OP_SPECTARG");
          break;
        } /* case OP_SPECTARG */


        // Look for unsupported special sources and report errors
        // appropriately.
        case Constants::OP_EXEC:
          Make::quit( "Special source .EXEC is no longer supported.", 1 );
        case Constants::OP_IGNORE:
          Make::quit(
            String( "Special source .IGNORE is no longer supported. ") +
              "Prepend commands with `-' or use the -i flag.", 1 );
        case Constants::OP_JOIN:
          Make::quit( "Special source .JOIN is no longer supported.", 1 );
        case Constants::OP_OPTIONAL:
          Make::quit( "Special source .OPTIONAL is no longer supported.", 1 );
        case Constants::OP_SILENT:
          Make::quit( "Special source .SILENT is no longer supported.", 1 );
        case Constants::OP_USE:
          Make::quit( "Special source .USE is no longer supported.", 1 );
        default:
          break;
      } /* end switch */
      continue;
    } /* end if */

    // Look for .PASSES special source.
    if (isSet( Constants::OP_PASSES ))
    {
      if (MkCmdLine::dTargs())
        Interface::printAlways( "Targ: Evaluating special source OP_PASSES" );

      // Create a PassNode to be parsed later.
      srcnd = new PassNode( srcname, nameOf(), passnode->getCwd(),
        passnode->getMakeDir(), passnode->getMakeTop(), passnode->getParentMF(),
        passnode->getParentMF(), passnode->getDefaultSearchPath() );

      // Chain this PassNode with the previous one to get proper ordering.
      if (prev_passnd != 0)
      {
        srcnd->addChild( prev_passnd );
        prev_passnd->addParent( srcnd );
      }

      // Reset the previous PassNode
      prev_passnd = srcnd;

      // Add the PassNode to the target graph.
      tmpnd = passnode->getTgtGraph()->insert( srcnd );

      // If the srcnd already existed then free the allocated PassNode.
      if (tmpnd != srcnd)
      {
        delete srcnd;
        srcnd = (PassNode *)tmpnd;
      }

      addChild( srcnd );
      srcnd->addParent( this );
      continue;
    } /* end if */

    // Add the source to the target dependency graph.
    if ( enum_child_mfs.hasMoreElements() )
    {
      srcnd = createAndInsertChild( srcname, passnode,
                                    *enum_child_mfs.nextElement() );
    }
    else    // Use Target (parents) line# if childs is missing.
    {
      srcnd = createAndInsertChild( srcname, passnode, mfs );
    }
    if (srcnd != 0)
    {
      // if the target is an archive target, we should split its name
      // and set the archive name, member name and set target type to
      // be OP_ARCHV
      if (srcnd->getNodeType() == TARGET_NODE &&
          !srcnd->isSet( Constants::OP_ARCHV ) &&
          srcname.indexOf( '(' ) != STRING_NOTFOUND)
      {
        ((TargetNode *)srcnd)->setArchName(
          Archive::extractArchName( srcname ) );
        ((TargetNode *)srcnd)->setMembName(
          Archive::extractMembName( srcname ) );
        srcnd->setType( Constants::OP_ARCHV );
      }
    }
  } /* end for srcnames */
}

/************************************************
  *        --- void printNodeBOM ---
  *
  ************************************************/
void TargetNode::printNodeBOM( )
{
  Vector< GraphNode * > bomChildren;
  boolean print_date = false;
  boolean show_all   = false;

  if (passnode)
  {
    print_date = (passnode->getRootVars()->
                        find(StringConstants::ODEMAKE_BOMSHOWTIME_VAR) != 0);

    show_all   = (passnode->getRootVars()->
                        find(StringConstants::ODEMAKE_BOMSHOWALL_VAR) != 0);
  }

  int tmktype = getTargetmkType();
  if ((tmktype == FORCED || tmktype == MOD || tmktype == NOTEXIST || tmktype == OODWRT)
       && getState() != UNMADE && !isSet( Constants::OP_INVISIBLE ) && !isSourceOnly())
  {
    String liststr = StringConstants::EMPTY_STRING;

    VectorEnumeration< GraphNode *> enumer( &oodate );
    if (show_all)
      enumer.setObject( &children );

    while (enumer.hasMoreElements())
    {
      bomChildren.addWithoutDup( *enumer.nextElement() );
    }

    enumer.setObject( &bomChildren );

    while (enumer.hasMoreElements())
    {
      TargetNode *childnd = (TargetNode *) *enumer.nextElement();

      // check if we really need to include this child to BOM
      if (childnd == 0 || childnd->getNodeType() == PASS_NODE)
        continue;
      if (!show_all)
      {
        if (oodate == 0 && (childnd->getState() != MADE))
          continue;
        if (childnd->isSet( Constants::OP_INVISIBLE ))
          continue;
      }

      liststr += StringConstants::TAB;
      liststr += Path::unixize( childnd->getPathname() );

      if (print_date && childnd->getCF())
      {
        liststr += StringConstants::SPACE;
        liststr += StringConstants::OPEN_PAREN;
        liststr += childnd->getModTimeString();
        liststr += StringConstants::CLOSE_PAREN;
      }

      if (enumer.hasMoreElements())
      {
        liststr += StringConstants::SPACE;
        liststr += StringConstants::BACK_SLASH;
        liststr += StringConstants::NEWLINE;
      }
    }

    if (liststr.charAt( liststr.lastIndex() - 1) == '\\')
      liststr.substringThis( STRING_FIRST_INDEX, liststr.lastIndex() - 1 );

    // Print TargetNode's BOM info.
    // If ODEMAKE_BOMSHOWTIME, then add the time string also.
    // If children (sources) existed, then add them to the end.
    Path::putLine( *Make::bf,
      Path::unixize( getPathname() )
      + ((print_date && cf)
      ? StringConstants::SPACE
      + StringConstants::OPEN_PAREN
      + getModTimeString()
      + StringConstants::CLOSE_PAREN
      + StringConstants::SPACE
      : StringConstants::SPACE)
      + StringConstants::COLON
      + (liststr.length() == 0
      ? StringConstants::NEWLINE
      : StringConstants::SPACE + StringConstants::BACK_SLASH
      + StringConstants::NEWLINE + liststr + StringConstants::NEWLINE) );
  }
}


/************************************************
  * --- void TargetNode::addDelayedChildren ---
  *
  ************************************************/
void TargetNode::addDelayedChildren( String more_children,
                                     const MakefileStatement *mfs,
                                     boolean allLinks )
{
  static  StringArray srcnames( 10 );
  srcnames.clear();

  // Add the new children and associated MakefileStatement.
  // Note: It is possible to have duplication here, but the duplication
  //       will eventually be eliminated via TargetNode::parseSources()
  delayed_children += StringConstants::SPACE;
  delayed_children += more_children;

  // In the case that multiple children are being added, we will add
  // the MakefileStatement* the same number of times to preserve a 1-1 mapping.
  // This is needed so we can report any potential error with the
  // appropriate line number.
  more_children.split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );
  for ( int idx= ARRAY_FIRST_INDEX;
        idx <= srcnames.lastIndex();
        idx++ )
  {
    child_mfs.addElement( mfs );   // line # the child node was specified
  }

  // Make sure all linked targets are updated as well.
  if ( allLinks )
  {
    VectorEnumeration< TargetNode * > enumlinks( &linkedTargs );
    while (enumlinks.hasMoreElements())
    {
      (*enumlinks.nextElement())->addDelayedChildren( more_children,
                                                      mfs, false );
    }
  }
}


/************************************************
  * --- void TargetNode::removeChildren ---
  *
  * This function is called when .REPLSRCS special source is used.
  * Each TargetNode maintains its own set of children (delayed_children).
  * This string maintains a complete list of the targets children and it
  * includes both explicit sources and special sources.
  * This functions goal is to remove all explicit sources, while keeping
  * all the special sources.
  *
  ************************************************/
void TargetNode::removeChildren( boolean allLinks )
{
  String delayed_sp_srcs = StringConstants::EMPTY_STRING;
  Vector< const MakefileStatement * >  sp_src_mfs;
  const MakefileStatement *tmp_mfs =0;
  static  StringArray srcnames( 10 );
  srcnames.clear();
  VectorEnumeration< const MakefileStatement * > enum_child_mfs( &child_mfs );

  //Save all special sources first.
  delayed_children.split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );
  for ( int idx= ARRAY_FIRST_INDEX;
        idx <= srcnames.lastIndex();
        idx++ )
  {
    if ( enum_child_mfs.hasMoreElements() )
    {
      tmp_mfs = *enum_child_mfs.nextElement();
    }
    if ( srcnames[idx] == Constants::DOT_DIRS      ||
         srcnames[idx] == Constants::DOT_EXEC      ||
         srcnames[idx] == Constants::DOT_IGNORE    ||
         srcnames[idx] == Constants::DOT_INVISIBLE ||
         srcnames[idx] == Constants::DOT_JOIN      ||
         srcnames[idx] == Constants::DOT_LINKS     ||
         srcnames[idx] == Constants::DOT_MAKE      ||
         srcnames[idx] == Constants::DOT_NOREMOTE  ||
         srcnames[idx] == Constants::DOT_NOTMAIN   ||
         srcnames[idx] == Constants::DOT_OPTIONAL  ||
         srcnames[idx] == Constants::DOT_PASSES    ||
         srcnames[idx] == Constants::DOT_PMAKE     ||
         srcnames[idx] == Constants::DOT_PRECIOUS  ||
         srcnames[idx] == Constants::DOT_RECURSIVE ||
         srcnames[idx] == Constants::DOT_REPLSRCS  ||
         srcnames[idx] == Constants::DOT_SILENT    ||
         srcnames[idx] == Constants::DOT_SPECTARG  ||
         srcnames[idx] == Constants::DOT_USE       ||
         srcnames[idx] == Constants::DOT_PRECMDS   ||
         srcnames[idx] == Constants::DOT_POSTCMDS  ||
         srcnames[idx] == Constants::DOT_REPLCMDS  ||
         srcnames[idx] == Constants::DOT_NORMTARG  ||
         srcnames[idx] == Constants::DOT_FORCEBLD )
    {
      sp_src_mfs.addElement( tmp_mfs );
      delayed_sp_srcs += StringConstants::SPACE;
      delayed_sp_srcs += srcnames[idx];
    }
  }

  // We need to reset the chilren (sources) for this target.
  // If any special sources were present, they will be saved. Otherwise
  // these fields will be cleared out.
  delayed_children = delayed_sp_srcs;
  child_mfs.removeAllElements();

  VectorEnumeration< const MakefileStatement * > enum_sp_src_mfs( &sp_src_mfs );
  while ( enum_sp_src_mfs.hasMoreElements() )
  {
    child_mfs.addElement( *enum_sp_src_mfs.nextElement() );
  }

  //GraphNode::children
  children.removeAllElements();

  // Make sure all linked targets are updated as well.
  if ( allLinks )
  {
    VectorEnumeration< TargetNode * > enumlinks( &linkedTargs );
    while (enumlinks.hasMoreElements())
    {
      (*enumlinks.nextElement())->removeChildren( false );
    }
  }
}


/******************************************************************
  * --- boolean TargetNode::linkedTargNeedsUpdate( youngChildTime )
  *
  * This function is called to determine if any linked targets
  * need to be updated.  If so, true is returned, so that we force
  * the targets commands to be executed.
  *
  ******************************************************************/
boolean TargetNode::linkedTargNeedsUpdate( long youngChildTime )
{
  TargetNode *linkedTarg = 0;
  CachedFile *lf;               // The associated cached file

  VectorEnumeration< TargetNode * > enumlinks( &linkedTargs );
  while (enumlinks.hasMoreElements())
  {
    linkedTarg = *enumlinks.nextElement();

    // Quit now if any linked target has ! or .FORCEBLD
    if (linkedTarg->isSet( Constants::OP_FORCEDEP ))
      return true;

    lf = linkedTarg->getCachedFile( passnode );

    // Quit now if any linked target file doesnt exist or is out of date.
    if (linkedTarg->isSymbolicLink())
    {
      if (!lf || (lf->getLinkModTime() < youngChildTime))
        return true;
    }
    else
    {
      if (!lf || (lf->getModTime() < youngChildTime))
        return true;
    }
  }

  return false;
}


/******************************************************************
  * --- CachedFile *TargetNode::getCachedFile()
  *
  * This function is called to retrieve this TargetNode's
  * cached file.  Moved to separate routine to avoid duplication.
  *
  ******************************************************************/
CachedFile *TargetNode::getCachedFile( PassInstance *pass )
{
  CachedFile *cf=0;
  boolean dirs_only = isSet( Constants::OP_DIRS );

  if (isSet( Constants::OP_ARCHV ))
  {
    cf = pass->getSearchPath()->findArchMemb( arch, memb );
    // create implied source object
    TargetNode *srcnode = createAndInsertChild( memb, pass, mfs );
    srcnode->setType( Constants::OP_MEMBER );
    srcnode->setArchName( arch );
    srcnode->setMembName( memb );
  }
  else
  {
    // Need to add BAR so that we are sure what the suffix really is.
    cf = pass->getSearchPath()->findFile(
                         prefix + StringConstants::BAR + suffix,
                         false, dirs_only );

    // If the file wasn't found then look to the file system directly
    // in case this file was updated by a different target.
    if (cf == 0)
    {
      if (Path::absolute( nameOf() ))
        cf = pass->getSearchPath()->findFile( nameOf(), true, dirs_only );
      else
        cf = pass->getSearchPath()->findFile( pass->getCwd() +
               StringConstants::FORW_SLASH + nameOf(), true, dirs_only );
    }
   //to ensure the extract file will get rebuild again,if the timestamp
   //of its children is different, we will do the smartBuild, so set the flag
#ifdef __WEBMAKE__
#ifdef __WEBDAV__

   if(cf!=0)
   {
     WebResource *const *f=0;
     const String *server=Env::getenv("WEBDAV_SERVER");
     String urlString=(*server).substring(8,(*server).length()+1);
     int slash=urlString.indexOf('/');
     String hostAndPort=urlString.substring(STRING_FIRST_INDEX,slash);
     String prefix=urlString.substring(slash,urlString.length()+1);
     String href;
     String childName;
     TargetNode * childNode;
     VectorEnumeration< GraphNode *> enum_children( &children );
     struct stat statbuf;
     int rc;
     //prepare to get stat information
     while (enum_children.hasMoreElements())
     {
       childNode=(TargetNode*)*enum_children.nextElement();
       childName= childNode->nameOf();
       if(childName.toUpperCase().indexOf(Path::unixize((*root).toUpperCase()))!=STRING_NOTFOUND)
         href=prefix+childName.substring((*root).length()+1,childName.length()
         +1);
         else
           href=prefix+Path::DIR_SEPARATOR+childName;
         href=Path::unixize(href);
         childName=Path::unixize(childName);
         rc=stat(childName.toCharPtr(),&statbuf);
         if((f=(Make::WebDavFilesTable).get(href))!=0)
         {
           if(rc==0 && ((*f)->compareTimeStamp(statbuf.st_mtime)!=0 ||
              (*f)->extracted==true))
           {
              smartBuild=true;
              break;
           }
         }
       }
     }
#endif // __WEBDAV__
#endif // __WEBMAKE__
  }

  return( cf );
}

/******************************************************
 * This functions set the state of all linked targets.
 ******************************************************/
void TargetNode::setLinkedTargsState( int instate )
{
  VectorEnumeration< TargetNode * > enumtgts( &linkedTargs );
  TargetNode *tmptgt;
  while (enumtgts.hasMoreElements())
  {
    tmptgt = *enumtgts.nextElement();
    tmptgt->setState( instate );
  }
}

/**************************************************
  * - const Command *TargetNode::getNextParsedCmd -
  *
  *************************************************/
const Command *TargetNode::getNextParsedCmd()
{
  const Command *nextCmd;
  nextCmd = GraphNode::getNextParsedCmd();

  // Need to update local vars.
  StringArray  variables = getLocalVar();
  StringArray  values = getLocalVarValue();

  for (int i = variables.firstIndex(); i <= variables.lastIndex(); i++)
  {
    if (values[i] == StringConstants::EMPTY_STRING)
    {
      if (variables[i] != StringConstants::EMPTY_STRING)
        local_runtime_vars->unset( variables[i] );
    }
    else
      local_runtime_vars->set( variables[i], values[i], true );
  }

  return( nextCmd );
}

