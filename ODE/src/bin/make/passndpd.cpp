/**
 * PassNode (parseDep function)
 *
**/
#define _ODE_BIN_MAKE_PASSNDP_CPP_

#include "base/binbase.hpp"

#include "lib/string/strcon.hpp"
#include "lib/util/arch.hpp"

#include "bin/make/dir.hpp"
#include "bin/make/mfstmnt.hpp"
#include "bin/make/targnode.hpp"
#include "bin/make/makefile.hpp"
#include "bin/make/sufftrfm.hpp"
#include "bin/make/makec.hpp"
#include "bin/make/cmdable.hpp"
#include "bin/make/passnode.hpp"

/************************************************
 *     --- void PassNode::parseDep ---
  * Assumptions:
  *  parameter tgts, has been Variable::parseUntil() and trimmed
  ************************************************/
void PassNode::parseDep( const String &tgts, const String &srcnms, int deptype )
  // throw (ParseException)
{
  static String cmds;
  static String srcs;

  TargetNode *tgtnd=0;
  GraphNode  *tmpnd=0;
  const Keyword *spectgt=0;
  int start_idx = STRING_FIRST_INDEX;

  // Define these as "static" so we don't have to keep reallocating the stuff.
  static StringArray tgtnames( 10 ), srcnames( 10 ), tmparr( 10 ), suffixes;

  // Clear the static arrays to be safe
  tgtnames.clear();
  srcnames.clear();
  suffixes.clear();
  tmparr.clear();
  cmds = StringConstants::EMPTY_STRING;
  srcs = srcnms;

  try
  {
    tgtlst.clear();
    srcs.trimThis();
    if (srcs.length() > 0)
    {
      // Look for commands on the dependency line.
      parseUntil( srcs, StringConstants::SEMICOLON, true, &srcnames );

      // Check for a command at the end of the line.  For example,
      // foo.o: foo.h;cc -c foo.c
      if (srcnames[ARRAY_FIRST_INDEX+1].length() > 0)
        cmds = srcnames[ARRAY_FIRST_INDEX+1].substring(STRING_FIRST_INDEX+1);
      if (srcnames[ARRAY_FIRST_INDEX].length() == 0)
      {
        srcs = StringConstants::EMPTY_STRING;
        srcnames.clear();
      }
      else
      {
        // check for archive specification. For example,
        //   tgt: arch( memb1 memb2 ) src
        // build a new dependent line
        //   tgt: arch(memb1) arch(memb2) src
        // split it and put them into a string array
        //   [arch(memb1), arch(memb2), src]
        if (srcnames[ARRAY_FIRST_INDEX].indexOf( StringConstants::OPEN_PAREN ) != STRING_NOTFOUND)
        {
          Archive::parse( srcnames[ARRAY_FIRST_INDEX].trim(), &tmparr );
          srcs = tmparr.join( StringConstants::SPACE );
        }
        else
          srcs = srcnames[ARRAY_FIRST_INDEX]; // sources with $, vars expanded
      }
    }

    if (tgts.length() == 0)
    {
      // Create an Empty Target as a placeholder
      tgtnd = new TargetNode( Constants::EMPTY_TARGET, mfs );
      tmpnd = getTgtGraph()->insert( tgtnd );
      // If the tgtnd already existed then free the allocated TargetNode.
      if (tmpnd != tgtnd)
      {
        delete tgtnd;
        tgtnd = (TargetNode *)tmpnd;
      }
      else
      {
        // Make sure this node does not get commands or is
        // accidentally used as a regular target.
        tgtnd->setCmdsStates( TargetNode::DONE_CMDS );
        tgtnd->setType( Constants::OP_SPECTARG );
        tgtnd->setType( Constants::OP_NOTMAIN );
      }
      tgtlst.add( tgtnd );

      if (MkCmdLine::dTargs())
          Interface::printAlways(
            String( "Targ: No targets specified on line " )
            + mfs->getLineNumber() + " in " + mf->getPathname() );
    }

    tmparr.clear();

    // check for archive specification. For example,
    //   arch( memb1 memb2 ) tgt: src
    // build new dependent line
    //   arch(memb1) arch(memb2) tgt: src
    // split it and put them into a string array
    //   [arch(memb1), arch(memb2), tgt]
    if (tgts.indexOf( StringConstants::OPEN_PAREN ) !=
        STRING_NOTFOUND)
    {
      try
      {
        Archive::parse( tgts, &tgtnames );
      }
      catch ( ParseException &e )
      {
        throw ParseException( mf->getPathname(), mfs->getLineNumber(),
            e.getMessage() );
      }
    }
    else
    {
      tgts.split( StringConstants::SPACE_TAB, UINT_MAX, &tgtnames );
    }

    boolean duplicate=false;

    for ( int tgtidx=tgtnames.firstIndex();
          tgtidx<=tgtnames.lastIndex(); tgtidx++ )
    {
      String &tgtname = tgtnames[tgtidx];
#ifdef FILENAME_BLANKS
      tgtname.dequoteThis();
#endif
      tgtname.trimThis();

      if (tgtname.length() == 0)
        continue;

      Path::unixizeThis( tgtname );
      tgtnames[tgtidx] = tgtname;

      if (MkCmdLine::dTargs())
      {
        if (srcs.length() == 0)
          Interface::printAlways(
            "Targ: Found target " + tgtname + " with no sources" );
        else
          Interface::printAlways(
            "Targ: Found target " + tgtname + " with sources: " +
             srcs );
      }

      // Check to see if this target was already defined via this same
      // makefile statement.  If so, then give user a warning and ignore it
      // so we do not duplicate the targets commands.
      // Ex:   targA  targA: src1 src2
      duplicate=false;
      for ( int i=tgtnames.firstIndex(); (i < tgtidx) && (!duplicate); i++ )
      {
        if (tgtname == tgtnames[i])
        {
          if (MkCmdLine::dTargs())
          {
            Interface::printAlways( "Targ: Duplicate target " + tgtname +
                                    " on line " + mfs->getLineNumber() +
                                    " in " + mf->getPathname() +
                                    " is ignored.");
          }
          duplicate=true;
        }
      }
      if (duplicate)
        continue;

      // First check for special targets
      if ((spectgt=Keyword::isSpecialTarget( tgtname )) != 0)
      {
        if (tgtnames.length() > 1)
          throw ParseException( mf->getPathname(),
            mfs->getLineNumber(),
            "special target `" + spectgt->nameOf() + "' must be the only target" );

        if (MkCmdLine::dTargs())
          Interface::printAlways("Found special target "+spectgt->nameOf());

        switch (spectgt->getVal())
        {
          case Constants::TGT_SUFFIXES:
          {
            // If there are no sources then reinitialize the suffix
            // list.
            if (srcs.length() == 0)
            {
              transforms->clearSuffs();
            }
            else
            {
              Suffix  *newSuff;
              srcs.split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );

              for ( int sufidx=ARRAY_FIRST_INDEX;
                    sufidx<=srcnames.lastIndex();
                    sufidx++ )
              {
                // If suffix begins with "|" then ignore this special char.
                if (srcnames[sufidx].startsWith( StringConstants::BAR ))
                  srcnames[sufidx].substringThis( STRING_FIRST_INDEX + 1 );

                // Add if suffix starts with Period "."
                // If it doesn't, ignore it and notify user.
                if (srcnames[sufidx].startsWith( StringConstants::PERIOD ))
                {
                  newSuff = transforms->insertSuff( srcnames[sufidx], *this );
                  if (newSuff)
                    updateSearchPathsbyVPATH( newSuff );
                }
                else if (MkCmdLine::dTargs())
                {
                  Interface::printAlways( "Targ: Suffix " + srcnames[sufidx] +
                                          " on line " + mfs->getLineNumber() +
                                          " in " + mf->getPathname() +
                   " is ignored because it is not preceded with a period(.)");
                }
              }
            }
            continue;
          }
          case Constants::TGT_PATH:
          {
            if (tgtnames.length() > 1)
              throw ParseException(mf->getPathname(),
                mfs->getLineNumber(),
                ".PATH special target must be only target");

            // If .PATH is used without a suffix
            if (tgtname.equals(Constants::DOT_PATH))
            {
              if (srcs.length() == 0)
              {
                // Clear the search path.
                //
                searchpath->setPath((StringArray *)0);
                coresearchpath->clear();
                // Might have absolute VPATH's, so update searchpath.
                updateSearchPathsbyVPATH();
              }
              else
              {
                srcs.split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );
                // Append the specified search paths.
                for ( int srcidx=srcnames.firstIndex();
                      srcidx<=srcnames.lastIndex();
                      srcidx++ )
                {
                  tmparr.clear();
                  searchpath->appendSubDirToDefaultSearchPath(
                    Path::unixize( srcnames[srcidx]), &tmparr );
                  // We do not need to update searchpath at this point.
                  // updateSearchPathsbyVPATH() will update it.
                  searchpath->append( tmparr );
                  coresearchpath->append( tmparr );
                }

                // Since the coresearchpath was updated, we need to update
                // the searchpath with any specified vpath(s)
                updateSearchPathsbyVPATH();
              }
              //  Set the corresponding .PATH variable to match.
              setDotPathVar();
            }
            else
            {
              String suffname = tgtname.substring( STRING_FIRST_INDEX+5 );

              // If Path extension begins with "|" then ignore this special char
              if (suffname.startsWith( StringConstants::BAR ))
                suffname.substringThis( STRING_FIRST_INDEX + 1 );

              Suffix *suff = transforms->findSuff( suffname );
              if (suff == 0)
                throw ParseException( mf->getPathname(),
                  mfs->getLineNumber(),
                  "Suffix `" + suffname + "' not defined (yet)");

              if (srcs.length() == 0)
              {
                // Clear the search path.
                suff->clearPath();
                suff->clearCorePath();
                updateSearchPathsbyVPATH( suff );
              }
              else
              {
                srcs.split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );
                // Append the specified search paths.
                for ( int srcidx=srcnames.firstIndex();
                      srcidx<=srcnames.lastIndex(); srcidx++ )
                {
                  tmparr.clear();
                  searchpath->appendSubDirToDefaultSearchPath(
                    Path::unixize(srcnames[srcidx]), &tmparr);
                  suff->setPath( tmparr );
                  suff->setCorePath( tmparr );
                }
                updateSearchPathsbyVPATH( suff );
              }
              //  Set the corresponding .PATH.suffix variable to match.
              suff->setDotPathVars( *getGlobalVars() );
            }
            continue;
          }
          case Constants::TGT_MAKEFLAGS:
          {
            if (tgtname.equals(Constants::DOT_MFLAGS))
              throw ParseException( mf->getPathname(), mfs->getLineNumber(),
                ".MFLAGS special target is no longer support, "
                "use .MAKEFLAGS instead." );

            String errormsg;
            srcs.split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );
            errormsg = MkCmdLine::appendFlags( srcnames, isMakeconf(),
                        srcs.indexOf( StringConstants::VAR_SEP_STRING ) != 0 );
            if (errormsg.length() != 0)
              throw ParseException( mf->getPathname(), mfs->getLineNumber(),
                  errormsg);

            continue;
          }
          case Constants::TGT_ORDER:
          {
            if (MkCmdLine::dTargs())
              Interface::printAlways(
                "Targ: ordering: " + srcs );
            srcs.split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );
            orderSources( srcnames );
            continue;
          }
          case Constants::TGT_LINKTARGS:
          {
            if (MkCmdLine::dTargs())
              Interface::printAlways(
                "Targ: Linking: " + srcs );
            srcs.split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );
            linkSources( srcnames );
            continue;
          }
          case Constants::TGT_NOTPARALLEL:
            // Only allow NOTPARALLEL when .PASSES is NOT used.
            Make::mk->setNotParallel();
            continue;
          case Constants::TGT_PRECIOUS:
            if (srcs.length() == 0)
              is_precious = true;     // all nodes in this pass will be .PRECIOUS
            else
            {
              srcs.split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );
              markAsPrecious( srcnames );
            }
            continue;

          // The long list of targets no longer supported
          //  In a future version we won't even test for them.
          //
          case Constants::TGT_DEFAULT:
            Make::quit(".DEFAULT special target is no longer support.", 1);
          case Constants::TGT_IGNORE:
            Make::quit(".IGNORE special target is no longer support.", 1);
          case Constants::TGT_INCLUDES:
            Make::quit(".INCLUDES special target is no longer support.", 1);
          case Constants::TGT_LIBS:
            Make::quit(".LIBS special target is no longer support.", 1);
          case Constants::TGT_NULL:
            Make::quit(".NULL special target is no longer support.", 1);
          case Constants::TGT_SILENT:
            Make::quit(".SILENT special target is no longer support.", 1);
          default:
          {
            // Just add special targets, .BEGIN, .END, .MAIN, .INTERRUPT to the graph.
            break;
          }
        }
      } /* end if SpecialTarget */


      // Determine if the target is definitely NOT a suffix transform
      // This is the case when the .NORMAL_TARG special source is used.
      // While we are at it, decide if the .FORCEBLD special source is used.
      start_idx = STRING_FIRST_INDEX;
      boolean potentialImpliedRule=true;
      boolean forceBld     = false;
      boolean linkPatTargs = false;
      boolean replaceSrcs  = false;
      while ((start_idx = findDot( srcs, start_idx )) != STRING_NOTFOUND)
      {
        if (isSpecialSource( srcs, Constants::DOT_NORMTARG, start_idx ))
          potentialImpliedRule=false;
        else if (isSpecialSource( srcs, Constants::DOT_LINKTARGS, start_idx ))
          linkPatTargs = true;
        else if (isSpecialSource( srcs, Constants::DOT_REPLSRCS, start_idx ))
          replaceSrcs = true;
        else if (isSpecialSource( srcs, Constants::DOT_FORCEBLD, start_idx ))
        {
          deptype  = Constants::OP_FORCEDEP;
          forceBld = true;
        }
        start_idx++;
      }


      // If pattern character (%) is in the target, then process as a pattern
      int pidx = tgts.indexOf( StringConstants::PERCENT_SIGN );
      int idx  = 0,
          bidx = 0,
          last_pidx = 0;

      if (potentialImpliedRule && (pidx != STRING_NOTFOUND))
      {
        // We need to make sure that all tgts have exactly one %
        for ( idx=tgtnames.firstIndex();
              idx<=tgtnames.lastIndex();
              idx++ )
        {
          pidx = tgtnames[idx].indexOf( StringConstants::PERCENT_SIGN );
          last_pidx = tgtnames[idx].lastIndexOf( StringConstants::PERCENT_SIGN );
          if ((pidx == STRING_NOTFOUND) || (pidx != last_pidx))
          {
            //Throw some invalid syntax error.
            throw ParseException( mf->getPathname(), mfs->getLineNumber(),
                       "Pattern rules error, all targets must have exactly one \"%\" character");
          }

          bidx = tgtnames[idx].indexOf( StringConstants::BAR );
          while (bidx != STRING_NOTFOUND)
          {
            tgtnames[idx].remove( bidx, 1 );
            bidx = tgtnames[idx].indexOf( StringConstants::BAR );
          }
        }

        // We need to separate the srcs to explicit and implicit sources
        String expSrcs="";
        String impSrcs="";
        srcs.split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );

        for ( idx=ARRAY_FIRST_INDEX;
              idx<=srcnames.lastIndex();
              idx++ )
        {
          // If source contains "%" than it must be implicit source.
          if (srcnames[idx].indexOf( StringConstants::PERCENT_SIGN ) !=
                                   STRING_NOTFOUND)
            impSrcs += srcnames[idx] + StringConstants::SPACE;
          else
            expSrcs += srcnames[idx] + StringConstants::SPACE;
        }
        impSrcs.trimThis();
        expSrcs.trimThis();


        //Need to sort before we go further!
        //Both impSrcs and tgts   tgtnames
        StringArray arrImpSrcs(10);
        impSrcs.split( StringConstants::SPACE_TAB, UINT_MAX, &arrImpSrcs );
        char **patImpSrc = arrImpSrcs.toSortedCharStarArray();
        char **patImpTgt = tgtnames.toSortedCharStarArray();

        String patTgt = "";
        String patSrc = "";
        for ( idx = 0; idx < tgtnames.lastIndex(); idx++ )
        {
          patTgt += patImpTgt[idx] + StringConstants::SPACE;
        }
        patTgt.trimThis();

        for ( idx = 0; idx < arrImpSrcs.lastIndex(); idx++ )
        {
          patSrc += patImpSrc[idx] + StringConstants::SPACE;
        }
        patSrc.trimThis();


        PatternPair *patternPair = patterns->findPatternPair( patSrc, patTgt );
        if (patternPair == 0)
        {
          if (tgtname == impSrcs)
            throw ParseException( mfs->getPathname(), mfs->getLineNumber(),
                                  "Illegal circular pattern rule "
                                  + tgtname + " can not depend on itself" );

          patternPair = new PatternPair( patTgt, patSrc, expSrcs, mfs );

          patterns->insert( patternPair );
        }
        else
        {
          // Append or replace the new explicit sources to the list
          if (expSrcs.length() > 0)
          {
            if (replaceSrcs)
            {
              // .REPLSRCS is used -> delete existing sources first.
              patternPair->removeExplicitSrcs();
            }
            patternPair->addExplicitSrcs( expSrcs );
          }
        }

        patternPair->setType( deptype );
        if (linkPatTargs)
          patternPair->setType( Constants::OP_LINKTARGS );

        patternPair->clearType( Constants::OP_PRECMDS );
        patternPair->clearType( Constants::OP_POSTCMDS );

        if (srcs.length() > 0)
        {
          // For .PRE/.POST, don't forget to clear the option
          // each time (so the options don't "accumulate").
          start_idx = STRING_FIRST_INDEX;

          while ((start_idx = findDot( expSrcs, start_idx )) != STRING_NOTFOUND)
          {
            if (isSpecialSource( expSrcs, Constants::DOT_PRECMDS, start_idx ))
              patternPair->setType( Constants::OP_PRECMDS );
            else if (isSpecialSource( expSrcs, Constants::DOT_POSTCMDS, start_idx ))
              patternPair->setType( Constants::OP_POSTCMDS );
            start_idx++;
          } /* end while */
        } /* end if */

        // If the pattern pair has already been defined with commands, then
        // clear them out (unless, of course, if we are supposed to append/prepend
        // them then we don't clear out).
        if (patternPair->hasCmds() &&
            !patternPair->isSet( Constants::OP_POSTCMDS ) &&
            !patternPair->isSet( Constants::OP_PRECMDS ))
          patternPair->removeCmds();

        patternPair->prepareForCmds(); // must be called before adding commands

        // Add the commands if they exist
        if (cmds.length() > 0)
        {
          if (patternPair->isSet( Constants::OP_PRECMDS ) &&
            patternPair->isSet( Constants::OP_POSTCMDS ))
            patternPair->addPrePostCmd( Command( cmds, mfs ) );
          else if (patternPair->isSet( Constants::OP_PRECMDS ))
            patternPair->addPreCmd( Command( cmds, mfs ) );
          else // Either post commands or normal appending
            patternPair->addCmd( Command( cmds, mfs ) );
        }

        // Add to list of targets so we can gather up the commands.
        tgtlst.add( patternPair );
        tgtidx =1000;  // Done with this makefile statement
        continue;
      }


      // Determine if the target is a suffix transformation rule.
      suffixes.clear();
      parseSuffix( tgtname, &suffixes );


      if ((suffixes.length() != 0) && potentialImpliedRule )
      {
        SuffixPair *pair = transforms->findSuffixPair(
          suffixes[ARRAY_FIRST_INDEX],
          suffixes[ARRAY_FIRST_INDEX+1],
          suffixes[ARRAY_FIRST_INDEX+2],
          suffixes[ARRAY_FIRST_INDEX+3] );
        if (pair == 0)
        {
          if (suffixes[ARRAY_FIRST_INDEX] == suffixes[ARRAY_FIRST_INDEX+1])
            throw ParseException( mfs->getPathname(), mfs->getLineNumber(),
                    "Illegal circular suffix transformation rule "
                    + suffixes[ARRAY_FIRST_INDEX] + "->" +
                    suffixes[ARRAY_FIRST_INDEX+1] );

          pair = new SuffixPair( Suffix( suffixes[ARRAY_FIRST_INDEX],
                                         Dir( (StringArray *)0, this ),
                                         *getGlobalVars() ),
                                 Suffix( suffixes[ARRAY_FIRST_INDEX+1],
                                         Dir( (StringArray *)0, this ),
                                         *getGlobalVars() ),
                                 mfs, srcs,
                                 suffixes[ARRAY_FIRST_INDEX+2],
                                 suffixes[ARRAY_FIRST_INDEX+3] );

          transforms->insert( pair );
        }
        else
        {
          // Append or replace the new explicit sources to the list
          if (srcs.length() > 0)
          {
            if (replaceSrcs)
            {
              // .REPLSRCS is used -> delete existing sources first.
              pair->removeExplicitSrcs();
            }
            pair->addExplicitSrcs( srcs );
          }
        }

        pair->setType( deptype );

        pair->clearType( Constants::OP_PRECMDS );
        pair->clearType( Constants::OP_POSTCMDS );

        if (srcs.length() > 0)
        {
          // For .PRE/.POST, don't forget to clear the option
          // each time (so the options don't "accumulate").
          start_idx = STRING_FIRST_INDEX;

          while ((start_idx = findDot( srcs, start_idx )) != STRING_NOTFOUND)
          {
            if (isSpecialSource( srcs, Constants::DOT_PRECMDS, start_idx ))
              pair->setType( Constants::OP_PRECMDS );
            else if (isSpecialSource( srcs, Constants::DOT_POSTCMDS, start_idx ))
              pair->setType( Constants::OP_POSTCMDS );
            start_idx++;
          } /* end while */
        } /* end if */

        // If the suffix pair has already been defined with commands, then
        // clear them out (unless, of course, if we are supposed to append/prepend
        // them then we don't clear out).
        if (pair->hasCmds() && !pair->isSet( Constants::OP_POSTCMDS ) &&
          !pair->isSet( Constants::OP_PRECMDS ))
          pair->removeCmds();

        pair->prepareForCmds(); // must be called before adding commands

        // Add the commands if they exist
        if (cmds.length() > 0)
        {
          if (pair->isSet( Constants::OP_PRECMDS ) &&
            pair->isSet( Constants::OP_POSTCMDS ))
            pair->addPrePostCmd( Command( cmds, mfs ) );
          else if (pair->isSet( Constants::OP_PRECMDS ))
            pair->addPreCmd( Command( cmds, mfs ) );
          else // Either post commands or normal appending
            pair->addCmd( Command( cmds, mfs ) );
        }

        // Add to list of targets so we can gather up the commands.
        tgtlst.add( pair );
        continue; // Skip to next target

      }

      // Attempt to see if the target node already exists.
      bidx = tgtname.lastIndexOf( StringConstants::BAR );
      if (bidx != STRING_NOTFOUND)
      {
        tgtname.remove( bidx, 1 );
      }

      tgtnd = (TargetNode *)getTgtGraph()->find( tgtname );

      if (tgtnd == 0)
      {
        tgtnd = new TargetNode( tgtname, mfs );
        tmpnd = getTgtGraph()->insert( tgtnd );

        // If the tgtnd already existed then free the allocated TargetNode.
        if (tmpnd != tgtnd)
        {
          delete tgtnd;
          tgtnd = (TargetNode *)tmpnd;
        }

        // if the target is an archive target, we should split its name
        // and set the archive name, member name and set target type to
        // be OP_ARCHV
        if (tgtname.indexOf( '(' ) != STRING_NOTFOUND)
        {
          tgtnd->setArchName( Archive::extractArchName( tgtname ) );
          tgtnd->setMembName( Archive::extractMembName( tgtname ) );
          tgtnd->setType( Constants::OP_ARCHV );
        }

        if (forceBld)
          tgtnd->setType( Constants::OP_FORCEBLD );
      }
      else
      {
        if (forceBld)
          tgtnd->setType( Constants::OP_FORCEBLD );

        if (!tgtnd->isSet( Constants::OP_FORCEBLD ) &&  //we can change if .FORCEBLD is used
            ((tgtnd->isSet( Constants::OP_DEPEND ) &&
              deptype != Constants::OP_DEPEND ) ||
             (tgtnd->isSet( Constants::OP_FORCEDEP ) &&
              deptype != Constants::OP_FORCEDEP ) ||
             (tgtnd->isSet( Constants::OP_DOUBLEDEP ) &&
              deptype != Constants::OP_DOUBLEDEP )))
          throw ParseException(mf->getPathname(),
            mfs->getLineNumber(),
            "Illegal dependency redefinition for `" + tgtname +
            "', orginally defined in \"" + tgtnd->getPathName() +
            "\", line " + String(tgtnd->getBeginLine()));

        // If the TargetNode was only used as a source before, then
        // set its dependency type now that it is a target.
        boolean is_source_only = tgtnd->isSourceOnly();
        if (is_source_only)
        {
          // Set the type of dependency. Could be :, :: or !.
          tgtnd->setType( deptype );

          // Change the type to target.
          tgtnd->setTarget();
        }

        tgtnd->clearType( Constants::OP_REPLCMDS );
        tgtnd->clearType( Constants::OP_PRECMDS );
        tgtnd->clearType( Constants::OP_POSTCMDS );

        if (srcs.length() > 0)
        {
          start_idx = STRING_FIRST_INDEX;

          while ((start_idx = findDot( srcs, start_idx )) != STRING_NOTFOUND)
          {
            if (isSpecialSource( srcs, Constants::DOT_REPLCMDS, start_idx ))
            {
              tgtnd->setType( Constants::OP_REPLCMDS );
              // PRECMDS and POSTCMDS are ignored when REPLCMDS is used
              tgtnd->clearType( Constants::OP_PRECMDS );
              tgtnd->clearType( Constants::OP_POSTCMDS );
              break;
            }
            else if (isSpecialSource( srcs, Constants::DOT_PRECMDS, start_idx ))
              tgtnd->setType( Constants::OP_PRECMDS );
            else if (isSpecialSource( srcs, Constants::DOT_POSTCMDS, start_idx ))
              tgtnd->setType( Constants::OP_POSTCMDS );
            start_idx++;
          } /* end while */
        } /* end if */

        // We need to verify that if the the TargetNode existed before,
        // if it was only a source and is of type DOUBLEDEP (::),
        // then any new TargetNodes that have the same name must also be
        // DOUBLEDEP (::).
        if (tgtnd->isSet( Constants::OP_DOUBLEDEP ) && !is_source_only)
        {
          if (deptype == Constants::OP_DOUBLEDEP)
          {
            TargetNode *newtgtnd = new TargetNode( tgtname, mfs );
            tgtnd->addSibling( newtgtnd );

            // Set the current target node to the new sibling.
            tgtnd = newtgtnd;
          }
          else
          {
            throw ParseException(mf->getPathname(),
              mfs->getLineNumber(),
              "Inconsistent operator for \""+tgtname+
              "\", orginally defined in \""+tgtnd->getPathName()+
              "\", line "+String(tgtnd->getBeginLine()));
          }
        }
        else
        {
          if (deptype == Constants::OP_DOUBLEDEP && !is_source_only)
            throw ParseException(mf->getPathname(),
              mfs->getLineNumber(),
              "Inconsistent operator `::' for `"+tgtname+
              "', orginally defined in \""+tgtnd->getPathName()+
              "\", line "+String(tgtnd->getBeginLine()));
        }
        if (tgtnd->isSet( Constants::OP_REPLCMDS ))
        {
          tgtnd->setCmdsStates( TargetNode::NEEDS_CMDS );
          tgtnd->removeCmds();
        }
        else if (!tgtnd->hasCmds() ||
            tgtnd->isSet( Constants::OP_PRECMDS ) ||
            tgtnd->isSet( Constants::OP_POSTCMDS ))
        {
          tgtnd->setCmdsStates( TargetNode::NEEDS_CMDS );
          tgtnd->prepareForCmds(); // must be called before adding commands
        }
        else
          tgtnd->setCmdsStates( TargetNode::DONE_CMDS );
      }

      if (tgtnd->isSourceOnly())
      {
        // Mark the target node as trully being a target not just a source.
        tgtnd->setTarget();

        // Keep target definition makefile name and line
        tgtnd->setDefnLineSrc( mfs );
      }

      // Set the type of dependency. Could be :, :: or !.
      // This block must be executed even if tgtnd was previously defined
      // as a target.....because of .FORCEBLD
      // We should not change the dependency type to non-ForceDep if
      // this target was associated with .FORCEBLD
      if (!tgtnd->isSet( Constants::OP_FORCEBLD ) ||
          deptype == Constants::OP_FORCEDEP )
      {
        // To avoid mult. types being set, clear all types before setting it.
        tgtnd->clearType( Constants::OP_DEPEND );
        tgtnd->clearType( Constants::OP_DOUBLEDEP );
        tgtnd->clearType( Constants::OP_FORCEDEP );
        tgtnd->setType( deptype );
      }

      if (Keyword::isSpecialTarget( tgtname ))
        tgtnd->setType( Constants::OP_SPECTARG );

      // Since all the double colon dependencies generate the same
      // target we don't remove it.
      if (tgtnd->isSet( Constants::OP_DOUBLEDEP ))
        tgtnd->setType( Constants::OP_PRECIOUS );

      // Only add commands to the target node if it
      // doesn't have any associated commands, or if
      // PRECMDS and/or POSTCMDS is used.
      if (cmds.length() != 0)
      {
        if (!tgtnd->hasCmds()) // ignore PRECMDS/POSTCMDS if first time
          tgtnd->addCmd( Command( cmds, mfs ) );
        else if (tgtnd->isSet( Constants::OP_PRECMDS ) &&
            tgtnd->isSet( Constants::OP_POSTCMDS ))
          tgtnd->addPrePostCmd( Command( cmds, mfs ) );
        else if (tgtnd->isSet( Constants::OP_PRECMDS ))
          tgtnd->addPreCmd( Command( cmds, mfs ) );
        else if (tgtnd->isSet( Constants::OP_POSTCMDS ))
          tgtnd->addCmd( Command( cmds, mfs ) );
      }

      tgtlst.add( tgtnd );

      if (replaceSrcs)
      {
        tgtnd->removeChildren();
      }

      // If we have sources let's create them after
      // we'll decide to build their parent
      if (srcs.length() > 0)
        tgtnd->addDelayedChildren( srcs, mfs );

      // Set the main target to first target in makefile.
      if (Make::mk->maintgt == 0 && !tgtnd->isSet(Constants::OP_NOTMAIN) &&
          !tgtnd->isSet(Constants::OP_SPECTARG))
      {
        // check for .NOTMAIN and .SPECTARG special source
        boolean is_main = true;
        if (srcs.length() > 0)
        {
          start_idx = STRING_FIRST_INDEX;
          while ((start_idx = findDot( srcs, start_idx )) != STRING_NOTFOUND &&
                 is_main)
          {
            if (isSpecialSource( srcs, Constants::DOT_SPECTARG, start_idx ) ||
                isSpecialSource( srcs, Constants::DOT_NOTMAIN, start_idx ))
            {
              is_main = false;
            }
            start_idx++;
          } /* end while */
        } /* end if */
        if (is_main)
          Make::mk->maintgt = tgtnd;
      }

    } /* end for tgts */
  } /* end try */
  catch ( MalformedVariable &e )
  {
    throw ParseException( mf->getPathname(), mfs->getLineNumber(),
        e.getMessage() );
  }
} /* end parseDep */

/************************************************
 * Purpose of this method is to improve performance of the search of some
 * special sources through the source String.  It is used in conjunction with
 * isSpecialSource()
 * Parameters:
 *   srcs      The String of sources
 *   index     Where to start the search and is updated with the next index to
 *             start the next search
 * Return Value:
 *   int       Index of the '.' or STRING_NOTFOUND
 **/
int PassNode::findDot( const String &srcs, int index ) const
{
  // First find the first src that starts with a '.'
  for (;index <= srcs.lastIndex(); index++)
  {
    if (srcs.charAt( index ) == '.')
    {
      // If we are at the beginning of the string then it is okay
      if (index == srcs.firstIndex())
        return (index);
      // If there is whitespace before the dot it is okay
      else if (srcs.charAt( index-1 ) == ' ' || srcs.charAt( index-1 ) == '\t')
        return (index);
    }
  }
  return (STRING_NOTFOUND);
}

/************************************************
 * Purpose of this method is to improve performance of the search of some
 * special sources through the source String.
 * Parameters:
 *   srcs      The String of sources
 *   spec_src  The special source to test for
 *   index     Where to start the search and is updated with the next index to
 *             start the next search
 * Return Value:
 *   true      If the special source was found and was surrounded by whitespaces
 *             Assumption: findDot() determines if the start of srcs is valid, ie
 *                         either start of string or whitespace
 *   false     Otherwise
 * Examples
 *   srcs = foo.PRECMDS
 *   spec_src = .PRECMDS
 *   Should return false since it isn't surrounded by whitespace
 **/
boolean PassNode::isSpecialSource( const String &srcs, const String &spec_src, int &index ) const
{
  if (srcs.startsWith( spec_src, index ))
  {
    // Need to see if special source was at the end of the string
    if (index + spec_src.length() - 1 == srcs.lastIndex())
    {
      index = srcs.lastIndex();
      return (true);
    }
    else
    {
      index += spec_src.length();
      // See if there is whitespace at the end of the special source
      if (srcs.charAt( index ) == ' ' || srcs.charAt( index ) == '\t')
        return (true);
    }
  }
  return (false);
}
