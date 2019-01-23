using namespace std;
/**
 * PassNode (parsing)
 *
**/
using namespace std;
#define _ODE_BIN_MAKE_PASSNDP_CPP_

// The maximum number of nest includes that can occur within a makefile.
// This is used to detect infinite recursive includes.
#define MAX_INC_LEVEL 128

#include <base/binbase.hpp>

#include "bin/make/passnode.hpp"

#include "lib/string/strcon.hpp"
#include "lib/util/arch.hpp"

#include "bin/make/constant.hpp"
#include "bin/make/dir.hpp"
#include "bin/make/job.hpp"
#include "bin/make/mfstmnt.hpp"
#include "bin/make/targnode.hpp"
#include "bin/make/makefile.hpp"
#include "bin/make/sufftrfm.hpp"
#include "bin/make/makec.hpp"
#include "bin/make/cmdable.hpp"

/*******************************************************************************
 * Parse in a Makefile and create the Pass instance if necessary.
 */
void PassNode::parse( PassNode &curpass, Makefile &makefile )
   // throw (ParseException)
{
  // Reinitialize makefile and line pointer.
  //
  curpass.reinit(makefile);
  curpass.parse();
}


/*******************************************************************************
 */
void PassNode::parse()
  // throw (ParseException)
{
  boolean        parseskip = false;
  Commandable   *cmdnd;
  CondEvaluator  evaluator( mfs, this, getVarEval() );

  is_parsed = true;

  // Set the target list back to zero
  tgtlst.clear();

  // Interface::printVerbose("Parsing file "+mf.getPathname());
  VectorEnumeration< MakefileStatement > mf_enum( mf->getStatements() );
  while (mf_enum.hasMoreElements())
  {
    // Do a quick check to see if we've been interrupted.
    if (Signal::isInterrupted())
      Job::runInterruptHandler();

    mfs = mf_enum.nextElement();
    evaluator.changeFileInfo( mfs );
    linestr = *(mfs->getLineString());
    firstchar = mfs->getFirstChar();

    switch ( firstchar )
    {
      case '.':
      {
        // strip off comment for all line except for command line
        ConfigFile::stripComment( linestr );
        parseskip = parseDotDirectives( parseskip, evaluator, mf_enum );
        break;
      }
      case '\t':
      case ' ':
      {
        // Check to see if this is an indented conditional statement.
        if (startsWithDotKeyword( linestr.trimFront() ))
        {
          linestr.trimFrontThis();
          ConfigFile::stripComment( linestr );
          parseskip = parseDotDirectives( parseskip, evaluator, mf_enum );
          break;
        }

        if (parseskip || linestr.trim().length() == 0)
          continue;

        // tgtlst is not empty
        if (!tgtlst.isEmpty())
        {
          // If the -w flag wasn't specified and the line starts with a space,
          // then clear the target list, because it could be a variable assignment,
          // target/dependency line, or possibly an unintented shell command.
          if (!MkCmdLine::whiteSpace() && firstchar == ' ')
          {
            tgtlst.clear();

            // strip off comment for all line except for command line
            ConfigFile::stripComment( linestr );
            parseVarOrDep();
          }
          else
          {
            Vector< TargetNode * > processedLinks, tmpLinks;
            boolean addedCmdAlready = false;

            for (int tgtidx  = tgtlst.firstIndex();
                     tgtidx <= tgtlst.lastIndex();
                     tgtidx++ )
            {
              cmdnd = tgtlst[tgtidx];

              if (MkCmdLine::dTargs() &&
                  cmdnd->getCmdType() == Commandable::TARGET_NODE_CMD &&
                  ((TargetNode *)cmdnd)->getPrefix() == Constants::EMPTY_TARGET)
              {
                Interface::printAlways(
                      String( "Targ: '" ) + linestr + "' on line "
                      + mfs->getLineNumber() + " in " + mf->getPathname()
                      + " ignored" );
              }

              // Can't have commands associated with the .PASSES special source.
              //
              if (cmdnd->getCmdType() == Commandable::TARGET_NODE_CMD &&
                   ((TargetNode *)cmdnd)->isSet( Constants::OP_PASSES ))
                throw ParseException(mf->getPathname(),
                  mfs->getLineNumber(), " unassociated command");

              if (cmdnd->getCmdType() == Commandable::TARGET_NODE_CMD &&
                  ((TargetNode *)cmdnd)->getCmdsStates() ==
                    TargetNode::DONE_CMDS)
                 break;

              if (cmdnd->getCmdType() != Commandable::TARGET_NODE_CMD)
              {
                if (cmdnd->isSet( Constants::OP_PRECMDS ))
                {
                  if (cmdnd->isSet( Constants::OP_POSTCMDS ))
                    cmdnd->addPrePostCmd( Command( linestr, mfs ) );
                  else
                    cmdnd->addPreCmd( Command( linestr, mfs ) );
                }
                else
                  cmdnd->addCmd( Command( linestr, mfs ) );
              }
              else
              {
                TargetNode *tgtnd = (TargetNode *)cmdnd;

                // When a command is added below, it is added to all linked
                // targets as well.  So it is important that we check to see
                // if this target already had this command added.  We want to
                // avoid adding the same command multiple times to the same
                // object.
                {
                  VectorEnumeration< TargetNode * > enumlinks( &processedLinks );
                  while (enumlinks.hasMoreElements())
                  {
                    if ( tgtnd == *enumlinks.nextElement())
                    {
                      addedCmdAlready = true;
                      break;
                    }
                  }
                }

                if ( !addedCmdAlready )
                {
                  if (tgtnd->isSet( Constants::OP_PRECMDS ))
                  {
                    if (tgtnd->isSet( Constants::OP_POSTCMDS ))
                      tgtnd->addPrePostCmd( Command( linestr, mfs ) );
                    else
                      tgtnd->addPreCmd( Command( linestr, mfs ) );
                  }
                  else
                    tgtnd->addCmd( Command( linestr, mfs ) );
                }

                // Keep track of all targets that have been processed.
                tmpLinks = tgtnd->getLinks();
                VectorEnumeration< TargetNode * > enumTmpLinks( &tmpLinks );
                while (enumTmpLinks.hasMoreElements())
                {
                  processedLinks.addElement(*enumTmpLinks.nextElement());
                }
              }
            }
          }
        }
        else // tgtlst is empty
        {
          // strip off comment for all line except for command line
          ConfigFile::stripComment( linestr );
          parseVarOrDep();
        }
        break;
      }
      default:
      {
        if (parseskip || linestr.trim().length() == 0)
          continue;

        // strip off comment for all line except for command line
        ConfigFile::stripComment( linestr );
        parseVarOrDep();
        break;
      } /* end default */
    } /* end switch */
  } /* end while */

  // If there is a missing .endif report an error.
  if (!evaluator.allBlocksClosed())
    throw ParseException(mf->getPathname(),
        mf->getLastLineNumber(),
        "One or more .if blocks are unclosed (.endif expected)" );
}

/*******************************************************************************
 * Returns true if a conditional block has been encountered that
 * should be skipped.
 * Returns the previous/passed value of parseskip otherwise.
**/
boolean PassNode::parseDotDirectives( boolean parseskip,
    CondEvaluator &condeval,
    VectorEnumeration< MakefileStatement > &mf_enum )
   // throw (ParseException)
{
  const String *keyword;

  keyword = &StringConstants::EMPTY_STRING;

  if (!parseskip && linestr.startsWith( Constants::DOT_INCLUDE ))
    parseInclude( false );
  else if (!parseskip && linestr.startsWith( Constants::DOT_TRYINCLUDE ))
    parseInclude( true );
  else if (!parseskip && linestr.startsWith( Constants::DOT_UNDEF ))
    parseUnDef();
  else
  {
    boolean print_debug_msg = MkCmdLine::dCond();

    if (linestr.startsWith( Constants::DOT_IFNDEF ))
    {
      keyword = &Constants::DOT_IFNDEF;
      parseskip = !condeval.parseIfndef( linestr.substringThis(
         Constants::DOT_IFNDEF.length() + 1 ) );
    }
    else if (linestr.startsWith( Constants::DOT_IFDEF))
    {
      keyword = &Constants::DOT_IFDEF;
      parseskip = !condeval.parseIfdef( linestr.substringThis(
          Constants::DOT_IFDEF.length() + 1 ) );
    }
    else if (linestr.startsWith( Constants::DOT_IFNMAKE ))
    {
      keyword = &Constants::DOT_IFNMAKE;
      parseskip = !condeval.parseIfnmake( linestr.substringThis(
          Constants::DOT_IFNMAKE.length() + 1 ) );
    }
    else if (linestr.startsWith( Constants::DOT_IFMAKE ))
    {
      keyword = &Constants::DOT_IFMAKE;
      parseskip = !condeval.parseIfmake( linestr.substringThis(
          Constants::DOT_IFMAKE.length() + 1 ) );
    }
    else if (linestr.startsWith( Constants::DOT_IF ))
    {
      keyword = &Constants::DOT_IF;
      parseskip = !condeval.parseIf( linestr.substringThis(
          Constants::DOT_IF.length() + 1 ) );
    }
    else if (linestr.startsWith( Constants::DOT_ELSE ))
    {
      keyword = &Constants::DOT_ELSE;
      parseskip = !condeval.parseElse();
    }
    else if (linestr.startsWith( Constants::DOT_ELIFDEF ))
    {
      keyword = &Constants::DOT_ELIFDEF;
      parseskip = !condeval.parseElifdef( linestr.substringThis(
          Constants::DOT_ELIFDEF.length() + 1 ) );
    }
    else if (linestr.startsWith( Constants::DOT_ELIFNDEF ))
    {
      keyword = &Constants::DOT_ELIFNDEF;
      parseskip = !condeval.parseElifndef( linestr.substringThis(
          Constants::DOT_ELIFNDEF.length() + 1 ) );
    }
    else if (linestr.startsWith( Constants::DOT_ELIFMAKE ))
    {
      keyword = &Constants::DOT_ELIFMAKE;
      parseskip = !condeval.parseElifmake( linestr.substringThis(
          Constants::DOT_ELIFMAKE.length() + 1  ) );
    }
    else if (linestr.startsWith( Constants::DOT_ELIFNMAKE ))
    {
      keyword = &Constants::DOT_ELIFNMAKE;
      parseskip = !condeval.parseElifnmake( linestr.substring(
          Constants::DOT_ELIFNMAKE.length() + 1  ) );
    }
    else if (linestr.startsWith( Constants::DOT_ELIF ))
    {
      keyword = &Constants::DOT_ELIF;
      parseskip = !condeval.parseElif( linestr.substringThis(
          Constants::DOT_ELIF.length() + 1 ) );
    }
    else if (linestr.startsWith( Constants::DOT_ENDIF ))
    {
      keyword = &Constants::DOT_ENDIF;
      parseskip = !condeval.parseEndif();
    }
    else
    {
      // Parse something that starts with a ".". Could be suffix
      // transformation, special target, or ...  Just let parseVarOrDep
      // figure it out.
      if (!parseskip)
        parseVarOrDep();
      print_debug_msg = false;
    }

    if (print_debug_msg)
    {
      String cond_debug_msg( "Cond: \"" );
      cond_debug_msg += getPathName() + "\", line " +
                        String(getBeginLine()) + ": " + *keyword;

      if ((!parseskip || condeval.prevCondEvaluated()) &&
           !linestr.startsWith( Constants::DOT_ENDIF ))
      {
        if (!linestr.startsWith( Constants::DOT_ELSE ))
        {
          cond_debug_msg += linestr;
        }
        cond_debug_msg += " == ";
        cond_debug_msg += (!parseskip) ? "true" : "false";
      }
      else
      {
        // If conditional was not even considered (false block)
        // Indicate so, by adding a "skipped" comment to output
        if ( !condeval.prevCondEvaluated() )
        {
          if ( !linestr.startsWith( Constants::DOT_ELSE ) )
          {
            cond_debug_msg += linestr;
          }
          cond_debug_msg +=  " == skipped";
        }
      }

      Interface::printAlways( cond_debug_msg );
    }
  }

  return (parseskip);
}

/*******************************************************************************
**/
void PassNode::parseUnDef()
  // throw (ParseException)
{
  String &var = linestr.substringThis(STRING_FIRST_INDEX+6).trimThis();
  if (var.length() == 0)
    return;

  // Unset the variable in all the global SetVars objects.
  //
  SetVars *pnd = getGlobalVars();
  SetVars *envs= getEnvironVars();
  while ( pnd != 0 && pnd != envs )
  {
    pnd->unset( var );
    pnd = (SetVars *)pnd->getParent();
  }

  // If VPATH is being undefined, then update all the search paths
  if (var == Constants::VPATH)
  {
    updateSearchPathsbyVPATH();
  }
}

/*******************************************************************************
**/
void PassNode::parseInclude( boolean tryinclude )
  // throw (ParseException)
{
  static short inclevel;
  static StringArray incarr( 2 );

  Makefile *incmf;
  boolean sys_search=true;

  // Initialize the static array
  incarr.clear();

  // Save previous values for restoring when returning from included makefile
  // processing.
  Makefile *prev_mf=mf;
  const MakefileStatement *prev_mfs = mfs;

  if (tryinclude)
    linestr.substringThis(STRING_FIRST_INDEX+11).trimThis();
  else
    linestr.substringThis(STRING_FIRST_INDEX+8).trimThis();

  if (linestr.startsWith( StringConstants::LESS_THAN ))
  {
    if (linestr.endsWith( StringConstants::BIGGER_THAN ))
      linestr.replaceThis('<',' ').replaceThis('>',' ').trimThis();
    else
      throw ParseException(mf->getPathname(),
        mfs->getLineNumber(),
        "Unclosed .include filename. '>' expected");
  }
  else if (linestr.startsWith( StringConstants::DOUBLE_QUOTE ))
  {
    sys_search = false;
    if (linestr.endsWith( StringConstants::DOUBLE_QUOTE ))
      linestr.replaceThis( '\"', ' ' ).trimThis();
    else
      throw ParseException(mf->getPathname(),
        mfs->getLineNumber(),
        "Unclosed .include filename. '\"' expected");
  }
  else
  {
    throw ParseException(mf->getPathname(),
      mfs->getLineNumber(),
      ".include filename must be delimited by '\"' or '<'");
  }
  try
  {
    tgtlst.clear();            // Empty the target node list

    // Eliminate any variables in the included filename
    parseUntil( linestr, StringConstants::EMPTY_STRING, false, &incarr );

    if (MkCmdLine::dIncs())
      Interface::printAlways(
       "Inc: Including makefile " + incarr[ARRAY_FIRST_INDEX]);

    if (inclevel++ > MAX_INC_LEVEL)
      throw ParseException(mf->getPathname(),
        mfs->getLineNumber(),
        "Exceeded max level of nested .include statements "
        + String( MAX_INC_LEVEL));

#ifdef __WEBMAKE__
    // if extractFlag is on , we will extract the inclueMakefile
    // if the makefile is in TC, then we will smartExtract the includeMakeFile
    const String *noExtractFlag=Env::getenv("noExtract");
    if (noExtractFlag==0 ||
       (noExtractFlag!=0 && (*noExtractFlag).startsWith("0")
))
       extractIncludeMakeFile(incarr[ARRAY_FIRST_INDEX]);
#endif // __WEBMAKE__

    // If the included makefile was enclosed in <> then search in the
    // sysSearchPath or the environment variable MAKEINCLUDECOMPAT was defined.
    //
    if (sys_search && !makeincludecompat)
    {
      Dir *syssearchpath = getSysSearchPath();
      incmf = Makefile::load( incarr[ARRAY_FIRST_INDEX], getCwd(), *syssearchpath);
      if (incmf != 0)
        incmf->instantiate(*this);
      else
      {
        if (!tryinclude)
          throw ParseException(prev_mf->getPathname(),
            prev_mfs->getLineNumber(),
            "Could not find included file " + incarr[ARRAY_FIRST_INDEX]);
      }
    }
    else
    {
      //add this part for the problem of if block embedded after continuation
      //line, since the include makefile might have the above problem

#ifdef __WEBMAKE__
         CondEvaluator condeval(mfs, this, getVarEval());
         incmf = Makefile::load( incarr[ARRAY_FIRST_INDEX], getCwd(),
                                 *searchpath, &condeval );
#else
         incmf = Makefile::load( incarr[ARRAY_FIRST_INDEX], getCwd(),
                                 *searchpath );
#endif // __WEBMAKE__
      if (incmf != 0)
        incmf->instantiate( *this );
      else
      {
        // If we are in backwards compatibility mode then search
        // also the system search path after the normal search path
        //
        if (makeincludecompat)
        {
          Dir *syssearchpath = getSysSearchPath();
#ifdef __WEBMAKE__
          incmf = Makefile::load( incarr[ARRAY_FIRST_INDEX], getCwd(),
                                  *searchpath, &condeval );
#else
          incmf = Makefile::load( incarr[ARRAY_FIRST_INDEX], getCwd(),
                                  *syssearchpath );
#endif // __WEBMAKE__
          if (incmf != 0)
            incmf->instantiate( *this );
          else
          {
            if (!tryinclude)
              throw ParseException(prev_mf->getPathname(),
               prev_mfs->getLineNumber(),
                "Could not find included file " + incarr[ARRAY_FIRST_INDEX]);
          }
        }
        else if (!tryinclude)
          throw ParseException(prev_mf->getPathname(),
            prev_mfs->getLineNumber(),
            "Could not find included file " + incarr[ARRAY_FIRST_INDEX]);
      }
    }
    tgtlst.clear(); // Empty the target node list.
    mf = prev_mf;               // Restore previous makefile name.
    mfs = prev_mfs;             // Restore previous makefile statement.
    inclevel--;                 // we returned back, so one level less
  }
  catch ( MalformedVariable &ev )
  {
    throw ParseException(prev_mf->getPathname(),
      prev_mfs->getLineNumber(),
      incarr[ARRAY_FIRST_INDEX] + ": "+ev.getMessage());
  }
}

/*******************************************************************************
 */
void PassNode::orderSources( StringArray &srcnames )
{
  GraphNode *prevnd = 0, *curnd = 0, *tmpnd = 0;

  for (int srcidx=srcnames.firstIndex();
       srcidx <= srcnames.lastIndex(); srcidx++)
  {
    curnd = getTgtGraph()->find( srcnames[srcidx] );
    if (curnd == 0)
    {
      curnd = new TargetNode( srcnames[srcidx], mfs );
      tmpnd = getTgtGraph()->insert( curnd );

      // If the tgt already existed then free the allocated TargetNode.
      if (tmpnd != curnd)
      {
        delete curnd;
        curnd = tmpnd;
      }
    }
    if (prevnd != 0)
    {
      prevnd->addNext( curnd );
      curnd->addPrev( prevnd );
    }
    prevnd = curnd;
  }
}

/*******************************************************************************
 */
void PassNode::linkSources( StringArray &srcnames )
{
  GraphNode *curnd = 0, *tmpnd = 0, *cmdnd = 0;
  TargetNode *tmptgt = 0;
  Vector< TargetNode * > links, tmpLinks;
  Vector< const MakefileStatement * > child_mfs, tmp_child_mfs;
  String  strChildren="";
  StringArray  children;

  // Process each source listed with the .LINKTARGS: special target.
  // Collect a complete set of linked targets.
  // Collect a complete set of these targets' children.
  for (int srcidx=srcnames.firstIndex();
       srcidx <= srcnames.lastIndex(); srcidx++)
  {
    curnd = getTgtGraph()->find( srcnames[srcidx] );
    // Create TargetNode since this one is not already in the graph.
    if (curnd == 0)
    {
      curnd = new TargetNode( srcnames[srcidx], mfs );
      tmpnd = getTgtGraph()->insert( curnd );

      // If the tgt already existed then free the allocated TargetNode.
      if (tmpnd != curnd)
      {
        delete curnd;
        curnd = tmpnd;
      }

      // Mark the target node as trully being a target not just a source.
      ((TargetNode *)curnd)->setTarget();
    }

    // Collect all linked targets.  Avoid duplicates.
    links.addWithoutDup( (TargetNode *)curnd );

    // The first linked target listed that has commands needs to be saved.
    // These commands will be applied to all linked targets later.
    if ( !cmdnd && curnd->hasCmds() )
      cmdnd = curnd;

    // Collect all children of all the linked targets.
    // Also collect each child's corresponding MakefileStatement.
    if ( ((TargetNode *)curnd)->hasChildren() )
    {
      strChildren += ((TargetNode * )curnd)->getDelayedChildren();
      tmp_child_mfs = ((TargetNode * )curnd)->getDelayedMFS();
      VectorEnumeration< const MakefileStatement * > enumTmpMFS( &tmp_child_mfs );
      while (enumTmpMFS.hasMoreElements())
      {
        child_mfs.addElement( *enumTmpMFS.nextElement() );
      }
    }

    // Any of the linked targets listed may already have links.
    // If so,  then add these also to the list.
    tmpLinks = ((TargetNode *)curnd)->getLinks();
    VectorEnumeration< TargetNode * > enumTmpLinks( &tmpLinks );
    while (enumTmpLinks.hasMoreElements())
    {
      tmptgt = *enumTmpLinks.nextElement();
      links.addWithoutDup( tmptgt );
    }
  }


  strChildren.split( StringConstants::SPACE_TAB, UINT_MAX, &children );

  // Enumerate our complete set of linked targets.
  // Each linked target must maintain a vector of the other targets.
  // Also, each target must have a dependency on all the children.
  VectorEnumeration< TargetNode * > enumlinks( &links );
  while (enumlinks.hasMoreElements())
  {
    tmptgt = *enumlinks.nextElement();
    tmptgt->addLinkedTargs( &links );
    if ( cmdnd && (tmptgt != (TargetNode *)cmdnd) )
    {
      // Replace any previous commands this node may have had.
      tmptgt->addCmds( cmdnd->getCmds() );
    }

    // We need to add all the children one at a time.
    // We need to maintain the corresponding MakefileStatement for each child.
    VectorEnumeration< const MakefileStatement * > enumTmpMFS( &child_mfs );
    for ( int idx= ARRAY_FIRST_INDEX;
          idx <= children.lastIndex();
          idx++ )
    {
      if (enumTmpMFS.hasMoreElements())
        tmptgt->addDelayedChildren( children[idx],
                                    *enumTmpMFS.nextElement(), false );
      else
        tmptgt->addDelayedChildren( children[idx], mfs, false );
    }
  }
}

/*******************************************************************************
 *  Valid format for a Suffix Transformation is:
 *      <srcdir>.srcsuff|<tgtdir>.tgtsuff:
 *  The separater (|)  and directories <srcdir> and <tgtdir> are optional.
 *  Return 0(null) if not a suffix transformation.
 *  Else return lhs suff in result[ARRAY_FIRST_INDEX]
 *              rhs suff in result[ARRAY_FIRST_INDEX+1]
 *              lhs dir  in result[ARRAY_FIRST_INDEX+2]
 *              rhs dir  in result[ARRAY_FIRST_INDEX+3]
 */
StringArray *PassNode::parseSuffix( const String &tgtname, StringArray *buf )
{
  static String sufflhs;
  static String suffrhs;
  static String dirlhs;
  static String dirrhs;

  StringArray *result = buf;
  dirlhs = dirrhs = StringConstants::EMPTY_STRING;

  int fstidx = tgtname.firstIndex();
  int tgtdiridx = tgtname.lastIndexOf( StringConstants::LESS_THAN );

  // Suffix transformations must start with a '.' or a '<'
  if (tgtname.charAt(fstidx) == '.' || tgtname.charAt(fstidx) == '<')
  {
    // Test for a special character '|' to divide double suffixes.
    int sepidx = tgtname.indexOf(Constants::SUFF_SEP);
    if (sepidx > fstidx)
    {
      if ((sepidx < tgtname.lastIndex()-1) &&
          (tgtname.charAt(sepidx+1) == '.' || tgtname.charAt(sepidx+1) == '<'))
      {
        sufflhs = tgtname.substring( fstidx, sepidx );
        suffrhs = tgtname.substring( sepidx + 1 );
      }
      else if (sepidx == tgtname.lastIndex())
      {
        sufflhs = tgtname.substring( fstidx, sepidx );
        suffrhs = StringConstants::EMPTY_STRING;
      }
      else
        return (0);
    }
    else if (tgtdiridx > fstidx)
    {
      sufflhs = tgtname.substring( fstidx, tgtdiridx );
      suffrhs = tgtname.substring( tgtdiridx );
    }
    else
    {
      // Finally test for the old suffix transformations.
      sepidx = tgtname.lastIndexOf( StringConstants::PERIOD );
      if (sepidx > fstidx && sepidx != tgtname.lastIndex())
      {
        sufflhs = tgtname.substring( fstidx, sepidx );
        suffrhs = tgtname.substring( sepidx );
      }
      // Could be single suffix rule.
      else if ((sepidx == fstidx)  &&  (sepidx != tgtname.lastIndex()) &&
               (sepidx == STRING_FIRST_INDEX))
      {
        sufflhs = tgtname;
        suffrhs = StringConstants::EMPTY_STRING;
      }
      else
        return (0);
    }

    // Separate the directories from the suffixes.
    if (!parseSuffixDir(&sufflhs, &dirlhs) ||
        !parseSuffixDir(&suffrhs, &dirrhs, true))
      return (0);
  }
  else
    return (0);

  if ((transforms->findSuff( sufflhs ) == 0) ||
      ((suffrhs != StringConstants::EMPTY_STRING) &&
       transforms->findSuff( suffrhs ) == 0))
    return (0);

  if (result == 0)
    result = new StringArray(4);

  result->append( sufflhs );
  result->append( suffrhs );
  result->append( dirlhs );
  result->append( dirrhs );
  return (result);
}


/*******************************************************************************
 *  Valid format for a suffix transformation target/suffix is
 *      <dir>.suffix       where <dir> is optional
 *  Input suffix -> String as described above.
 *  Input rhs -> true if evaluating the right hand side of suffix transform.
 *  Return true if valid syntax
 *  Output   suffix ->string that follows the brackets
 *  Output   suffdir ->string contained in brackets <>
 *  Return false if not valid syntax.
 */
boolean PassNode::parseSuffixDir( String *suffix, String *suffdir, boolean rhs )
{
  int fstidx = suffix->firstIndex();
  if (suffix->charAt( fstidx ) == '<')
  {
    int direndidx = suffix->indexOf( StringConstants::BIGGER_THAN );
    if (direndidx > fstidx)
    {
      if ((direndidx < suffix->lastIndex()-1) &&
          (suffix->charAt(direndidx+1) == '.'))
      {
        *suffdir = suffix->substring( fstidx + 1, direndidx );
        suffix->substringThis( direndidx + 1 );
      }
      // If we are evaluating the right hand side, then the suffix is
      // optional because of single suffix rules support.
      else if (rhs && (direndidx == suffix->lastIndex()))
      {
        *suffdir = suffix->substring( fstidx + 1, direndidx );
        *suffix = StringConstants::EMPTY_STRING;
      }
      else
        return (false);
    }
  }
  Path::unixizeThis( *suffdir );
  if ((suffdir->lastIndex() > fstidx) &&
      (suffdir->charAt(suffdir->lastIndex()) != '/'))
  {
    *suffdir += StringConstants::FORW_SLASH;
  }
  return (true);
}

/*******************************************************************************
**/
void PassNode::parseVar( const String &varname, String val /* val only writable for speed */,
  char op )
  // throw (ParseException)
{
  static String resultval;
  static StringArray results( 2 );

  if (varname.length() == 0)
    throw ParseException( mf->getPathname(),
      mfs->getLineNumber(),
      "no variable name given" );

  const String *resultvalptr=0;

  // Reinitialize static variables
  results.clear();
  resultval = StringConstants::EMPTY_STRING;

  if (MkCmdLine::dVars())
  {
    if (op == '=')
      Interface::printAlways( "Var: " + varname + " = " + val );
    else
      Interface::printAlways( "Var: " + varname + " " + String(op) + "= " + val);
      Interface::printAlways( "Var:   assigned in "
        + mfs->getPathname() + ", line " + mfs->getLineNumber() );
  }
  try
  {
    switch (op)
    {
      case '?': // Conditional assignment
        if (findVar( varname ) != 0)
          break;
        // Else fall through and do a normal assignment.
      case '=': // Normal assignment
        global_vars->set( varname, val.trimFrontThis(), true );
        break;
      case ':': // Assign with immediate expansion (global)
        parseUntil( val, StringConstants::EMPTY_STRING, true, &results );
        global_vars->set( varname, results[ARRAY_FIRST_INDEX].trimFrontThis(),
            true );
        break;
      case '%': // Assign with immediate expansion (environ)
        parseUntil( val, StringConstants::EMPTY_STRING, true, &results );
        environ_vars->set( varname, results[ARRAY_FIRST_INDEX].trimFrontThis(),
            true );
        Env::setenv( varname, results[ARRAY_FIRST_INDEX], true );
        break;
      case '+': // Append
        resultvalptr = findVar( varname );
        // If the variable wasn't already set then do a normal
        // assignment
        if (resultvalptr == 0)
        {
          global_vars->set( varname, val.trimFrontThis(), true );
        }
        else
        {
          resultval = *resultvalptr;

          // Append on the new variable val.
          val.trimFrontThis();
          if (val.length() > 0)
          {
            resultval += StringConstants::SPACE;
            resultval += val;
            global_vars->set( varname, resultval, true );
          }
        }
        break;
      case '!': // Assign from shell output.
        parseUntil( val, StringConstants::EMPTY_STRING, true, &results );
        if (results.length() == 0 ||
            results[ARRAY_FIRST_INDEX].trimThis().length() == 0)
          global_vars->set( varname, StringConstants::EMPTY_STRING, true );
        else
        {
          StringArray words;
          Variable::runShellCmd( results[ARRAY_FIRST_INDEX], getEnvironVars(),
                                 getGlobalVars(),
                                 String( "!= " ) + results[ARRAY_FIRST_INDEX],
                                 true
                               ).split( Variable::EOL_CHARS_STRING,
                                        UINT_MAX, &words );
          resultval = words.join( StringConstants::SPACE );
          global_vars->set( varname, resultval, true );
        }
        break;
      default:
        // No need to complain here, we check enough before we got here
        break;
    } /* end switch */
    // to keep search paths current
    if (varname == Constants::VPATH) updateSearchPathsbyVPATH();
  }
  catch ( MalformedVariable &e )
  {
    throw ParseException( mf->getPathname(),
      mfs->getLineNumber(),
      varname + ": " + e.getMessage() );
  }
}

/*******************************************************************************
**/
void PassNode::parseVarOrDep()
  // throw (ParseException)
{
  static StringArray result( 2 );

  // Reinitialize static data
  result.clear();

  try
  {
    parseUntil( linestr, Constants::UNTIL_CHARS, false, &result );

    String &lhs = result[ARRAY_FIRST_INDEX];
    String &rhs = result[ARRAY_FIRST_INDEX+1];

    // If there isn't a special character like '=', ':', etc.
    // complain about it.
    if (rhs.length() == 0)
      throw ParseException(mf->getPathname(),
        mfs->getLineNumber(), StringConstants::DOUBLE_QUOTE + lhs +
        "\" unrecognized input. Expected indented shell command, "
        "variable assignment or target/dependency" );

    switch (rhs.charAt( rhs.firstIndex() ))
    {
      case ':':   // Maybe ":", "::", ":="
      {
        if (rhs.length() == 1) // Dependency line with no sources.
        {
          parseDep( lhs.trimThis(), StringConstants::EMPTY_STRING, Constants::OP_DEPEND );
        }
        else if (rhs.charAt(rhs.firstIndex()+1) == ':')
        // Double dependency line.
        {
          // Empty the target node list;
          tgtlst.clear();
          parseDep( lhs.trimThis(),
                    rhs.substringThis(rhs.firstIndex()+2).trimThis(),
                    Constants::OP_DOUBLEDEP );
        }
        else if (rhs.charAt(rhs.firstIndex()+1) == '=')
        // Immediate assignment line
        {
          setVars( lhs.trimThis(), rhs.substringThis( rhs.firstIndex()+2 ), ':' );
        }
        else // Normal dependency line
        {
          // Empty the target node list
          tgtlst.clear();
          parseDep( lhs.trimThis(),
                    rhs.substringThis( rhs.firstIndex()+1 ).trimThis(),
                    Constants::OP_DEPEND );
        } /* end if */
        break;
      }
      case '!': // Shell assignment or dependency
      {
        if (rhs.length() == 1 || // Dependency line with no sources.
            rhs.charAt(rhs.firstIndex()+1) != '=')
        {
          // Empty the target node list
          tgtlst.clear();
          parseDep( lhs.trimThis(),
                    rhs.substringThis( rhs.firstIndex()+1 ).trimThis(),
                    Constants::OP_FORCEDEP );
        }
        else // shell assignment
        {
          setVars( lhs.trimThis(), rhs.substringThis( rhs.firstIndex()+2 ), '!' );
        }
        break;
      }
      case '=': // Assignment
      {
        char op = lhs.charAt( lhs.lastIndex() );

        // Test last char in lhs to find operators: +=,?=,%=
        if (op == '+' || op == '?' || op == '%')
        {
          lhs.substringThis( lhs.firstIndex(), lhs.lastIndex() ).trimThis();
          setVars( lhs, rhs.substringThis( rhs.firstIndex()+1 ), op );
        }
        else // Normal assignment
        {
          setVars( lhs.trimThis(), rhs.substringThis( rhs.firstIndex()+1 ), '=' );
        }
        break;
      } // end case '='
      default:
      {
        throw ParseException(mf->getPathname(),
         mfs->getLineNumber(), StringConstants::DOUBLE_QUOTE + lhs +
         "\" unrecognized input. Expected indented shell command, "
         "variable assignment or target/dependency" );
      }
    } /* end switch */
  } /* end try */
  catch ( MalformedVariable &e )
  {
    throw ParseException(mf->getPathname(),
      mfs->getLineNumber(),
      e.getMessage());
  }
  catch ( ParseException &e )
  {
    throw;
  }
  catch ( Exception &e )
  {
    throw ParseException(mf->getPathname(),
      mfs->getLineNumber(), "premature end of line" );
  }
}

/*******************************************************************************
 * This takes a the String 'lhs' and splits it into multiple variable names
 * and calls parseVar() on each variable name.
 *
**/
void PassNode::setVars( const String &lhs, const String &rhs, char op)
{
  static StringArray lhsarray( 4 );
  lhsarray.clear();

  // Empty the target node list
  tgtlst.clear();

  lhs.split( StringConstants::SPACE_TAB, UINT_MAX, &lhsarray );
  for (int lhsidx  = lhsarray.firstIndex();
           lhsidx <= lhsarray.lastIndex(); lhsidx++)
  {
    parseVar( lhsarray[lhsidx], rhs, op );
  }
}

/*******************************************************************************
**/
void PassNode::markAsPrecious( const StringArray &srcs )
  // throw (ParseException)
{
  GraphNode *srcnd = 0, *tmpnd = 0;

  // For each source of .PRECIOUS, create the TargetNode if it doesn't exist
  // and mark it as precious.
  for (int srcidx=srcs.firstIndex(); srcidx<=srcs.lastIndex(); srcidx++)
  {
    srcnd = getTgtGraph()->find( srcs[srcidx] );
    if (srcnd == 0)
    {
      srcnd = new TargetNode( srcs[srcidx], mfs );
      tmpnd = getTgtGraph()->insert( srcnd );

      // If the srcnd already existed then free the allocated TargetNode.
      if (tmpnd != srcnd)
      {
        delete srcnd;
        srcnd = tmpnd;
      }
    }
    srcnd->setType( Constants::OP_PRECIOUS );
  }
}


/*******************************************************************************
 * Checks to see if the given string starts with a conditional keyword.
 * This does not include runtime conditionals.
 *
**/
boolean PassNode::startsWithDotKeyword( const String &str )
{
  // Exit quickly for typical non-dot case.
  if (str.startsWith( "." ) &&
      (str.startsWith( Constants::DOT_INCLUDE    ) ||
       str.startsWith( Constants::DOT_TRYINCLUDE ) ||
       str.startsWith( Constants::DOT_UNDEF      ) ||
       str.startsWith( Constants::DOT_IFNDEF     ) ||
       str.startsWith( Constants::DOT_IFDEF      ) ||
       str.startsWith( Constants::DOT_IFNMAKE    ) ||
       str.startsWith( Constants::DOT_IFMAKE     ) ||
       str.startsWith( Constants::DOT_IF         ) ||
       str.startsWith( Constants::DOT_ELSE       ) ||
       str.startsWith( Constants::DOT_ELIFDEF    ) ||
       str.startsWith( Constants::DOT_ELIFNDEF   ) ||
       str.startsWith( Constants::DOT_ELIFMAKE   ) ||
       str.startsWith( Constants::DOT_ELIFNMAKE  ) ||
       str.startsWith( Constants::DOT_ELIF       ) ||
       str.startsWith( Constants::DOT_ENDIF      )))
  {
    return true;
  }
  else
  {
    return false;
  }
}


#ifdef __WEBMAKE__
/*******************************************************************************
 */
void PassNode::extractIncludeMakeFile(String includeMakeFile)
{
   //extract the include file from TC
   includeMakeFile=Path::unixize(includeMakeFile);
   (Make::extractFiles)->add(includeMakeFile);

#ifdef __WEBDAV__
   WEBDAV_autoExtract_dependency();
#else
   TC_autoExtract_dependency();
#endif // __WEBDAV__

}
#endif // __WEBMAKE__
