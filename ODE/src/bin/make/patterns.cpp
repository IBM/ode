/**
 * Patterns
 *
**/
#define _ODE_BIN_MAKE_PATTERNS_CPP_

#include <base/binbase.hpp>
#include "bin/make/patterns.hpp"
#include "bin/make/targnode.hpp"
#include "bin/make/passnode.hpp"
#include "bin/make/makec.hpp"
#include "bin/make/graph.hpp"
#include "bin/make/graphnd.hpp"




Patterns::~Patterns()
{
  // deallocate the PatternPairs memory
  VectorEnumeration< PatternPair * > enumpairs( &patterns );
  PatternPair *pattPair;

  while (enumpairs.hasMoreElements())
  {
    pattPair = *(enumpairs.nextElement());
    if (pattPair != 0)
      delete pattPair;
  }
}



/**
 * Insert a PatternPair pointer into the maintained vector patterns.
**/
void Patterns::insert( PatternPair *pair )
{
  if (pair == 0)
    return;

  if (MkCmdLine::dPatterns())
  {
    Interface::printAlways("Patterns: Adding " + (pair->getTgt()) +
                           " : "+ (pair->getSrc()));
  }

  // Make sure that this PatternPair is not already in the maintained vector.
  if (findPatternPair( pair->getSrc(), pair->getTgt() ) == 0)
  {
    patterns.addElement( pair );
  }

}




/**
 * Find the PatternPair in the maintained vector.
 * Both the implied targets and implied sources must match.
 * Return Found PatternPair Pointer. Zero not found
**/
PatternPair *Patterns::findPatternPair( const String &src,
                                        const String &tgt ) const
{

  PatternPair *const *pairnd;
  VectorEnumeration< PatternPair * > enumpairs( &patterns );
  while (enumpairs.hasMoreElements())
  {
    pairnd = enumpairs.nextElement();
    if (*pairnd == 0)
      continue;

#ifdef CASE_INSENSITIVE_OS
    if ((*pairnd)->getSrc().equalsIgnoreCase( src ) &&
        (*pairnd)->getTgt().equalsIgnoreCase( tgt ))
#else
    if ((*pairnd)->getSrc().equals( src ) &&
        (*pairnd)->getTgt().equals( tgt ))
#endif
      return ( *pairnd );
  }
  return ( 0 );
}



/**
 * See if the target (tgtname) fits the pattern specified by token.
 * If it does match, then set the value of the wildcard.
 * Example:  The target BillyBobJones will match token Billy%Jones
 *           with the wildcard=Bob
**/
boolean Patterns::isMatch(String   &tgtname,
                          String   &token,
                          String   &wildcardValue) const

{
  int idx = token.indexOf( StringConstants::PERCENT_SIGN );
  int tgtLength = tgtname.length();
  int tokLength = token.length();

  // Quit immediately if we cant find a percent sign
  // or if the target string is shorter than the token string.
  // We check tgtLength against (tokLength-1) because we are
  // allowing the percent sign the match an empty string.
  if ((idx == STRING_NOTFOUND) || (tgtLength < (tokLength - 1 )))
    return( false );

  // Make sure the strings match before the percent sign.
  if (idx > STRING_FIRST_INDEX)
  {
    if (token.substring( STRING_FIRST_INDEX, idx ) !=
        tgtname.substring( STRING_FIRST_INDEX, idx ))
      return( false );
  }

  // Make sure the strings match after the percent sign.
  if (idx < tokLength)
  {
    if (token.substring( idx + 1 ) !=
        tgtname.substring( tgtLength - (tokLength - idx) + 1 ))
      return( false );
  }

  // Determine the value of the wildcard.
  wildcardValue= tgtname.substring( idx );
  wildcardValue.substringThis( STRING_FIRST_INDEX,
             wildcardValue.length() - ( tokLength - idx ) + 1 );

  return( true );
}




TargetNode *Patterns::getImpSrcs(
  TargetNode         *tgtnode,
  PassInstance       &passnode,
  SetVars            &local_vars ) const
     // throw ParseException()
{
  int        idx = 0;
  String     src;
  String     srcname;
  String     strImpSrcs = "";
  TargetNode *srcnode = 0;
  CachedFile *srcfile = 0;
  boolean  bPatternWorks = true;
  Vector < TargetNode * >    impSrcs;
  Vector < CachedFile * >    impCFs;
  Vector < String     * >    cachedFnames;


  if (tgtnode == 0)
    return ( 0 );


  PatternPair *pattPair;
  VectorEnumeration< PatternPair * > enumpairs( &patterns );
  while (enumpairs.hasMoreElements())
  {
    pattPair = *(enumpairs.nextElement());

    String wildcardValue="";
    String tgtname = tgtnode->getPrefix() + tgtnode->getSuff();

    StringArray pattern;
    pattPair->getTgt().split(
                     StringConstants::SPACE_TAB, UINT_MAX, &pattern );


    for ( idx=pattern.firstIndex();
          idx<=pattern.lastIndex(); idx++ )
    {
      if (this->isMatch(tgtname, pattern[idx], wildcardValue))
      {
        // First construct the source name and see if it exists in the graph.
        srcname = pattPair->getSrc();
        strImpSrcs += StringConstants::SPACE + srcname;
        srcname.replaceThis( "%", wildcardValue );

        static  StringArray   srcnames( 10 );
        srcname.split( StringConstants::SPACE_TAB, UINT_MAX, &srcnames );
        bPatternWorks = true;

        // Loop thru all the implied sources.
        // First, see if it is already a targetnode.
        // Second, see if the file can be found.
        // Third, see if another pattern rule or suffix transform will work.
        // If none of the above work, then this pattern rule wont work.
        for ( int i=ARRAY_FIRST_INDEX;
              (i<=srcnames.lastIndex()) && bPatternWorks;
              i++ )
        {
          srcnode  = (TargetNode *)passnode.getTgtGraph()->find(srcnames[i]);

          if (srcnode != 0)
          {
            impSrcs.addElement( srcnode );
          }
          else
          {
            // Here we need to search for the file.
            srcfile = passnode.getSearchPath()->findFile(srcnames[i]);

            if (srcfile != 0)
            {
              impCFs.addElement( srcfile );
              cachedFnames.addElement( &srcnames[i] );
            }
            else
            {
              srcnode = new TargetNode( srcnames[i], pattPair->mfs, &passnode );

              if (passnode.getRootVars()->
                            find(StringConstants::ODEMAKE_TFMFIRST_VAR ) != 0 )
              {
                if (!passnode.getSuffTransforms()->getImpSrcs( srcnode,
                                                               passnode,
                                                               local_vars ) &&
                    !getImpSrcs( srcnode, passnode, local_vars ))
                {
                  // End of the line.  No way to make srcnode.
                  bPatternWorks = false;
                }
              }
              else if (!getImpSrcs( srcnode, passnode, local_vars) &&
                       !passnode.getSuffTransforms()->getImpSrcs( srcnode,
                                                                   passnode,
                                                                   local_vars ))
              {
                // End of the line.  No way to make srcnode.
                bPatternWorks = false;
              }

              if (bPatternWorks)
              {
                impSrcs.addElement( srcnode );
              }
              else
              {
                // Clear out the vectors we were using for this pattern.
                impSrcs.removeAllElements();
                impCFs.removeAllElements();
                cachedFnames.removeAllElements();
              }
            }
          }
        }


        if (bPatternWorks)
        {
          //Need to process all the implied sources we saved above here.
          VectorEnumeration< TargetNode * > enumImpSrcs( &impSrcs );
          while (enumImpSrcs.hasMoreElements())
          {
            srcnode = *(enumImpSrcs.nextElement());
            tgtnode->addChild( srcnode );
            srcnode->addParent(tgtnode);
          }

          if (MkCmdLine::dPatterns())
          {
            String strImpTargs = "";
            for ( idx=pattern.firstIndex();
                  idx<=pattern.lastIndex(); idx++ )
            {
              strImpTargs += pattern[idx] + StringConstants::SPACE;
            }

            Interface::printAlways("Patterns: Using  " + strImpTargs +
                                   ":" + strImpSrcs);
          }

          VectorEnumeration< CachedFile * > enumCFs    ( &impCFs );
          VectorEnumeration< String     * > enumCFnames( &cachedFnames );
          while (enumCFs.hasMoreElements())
          {
            srcfile = *(enumCFs.nextElement());

            // Since we found a source, create a new target node and stick it
            // in the graph.
            srcnode = tgtnode->createAndInsertChild(
                                                **(enumCFnames.nextElement()),
                                                &passnode,
                                                pattPair->mfs );

            // Since we have a cached copy then assign it to the node so we don't
            // have to go for it again.
            srcnode->setCF( srcfile );
          }


          // Add the commands for this target.
          if (!processTarget( tgtnode, pattPair ))
            continue;


          //Need to process other potential targets here.
          if (pattPair->isSet( Constants::OP_LINKTARGS ))
          {
            for ( int j=pattern.firstIndex();
                  j<=pattern.lastIndex(); j++ )
            {
              // Replace the %
              // Check to see if this target is already in graph
              // If not, create it and add it to the graph.
              if (j != idx)
              {
                srcname = pattern[j];
                srcname.replaceThis( "%", wildcardValue );

                srcnode  = (TargetNode *)passnode.getTgtGraph()->find(srcname);
                if ( srcnode == 0 )
                {
                  srcnode = new TargetNode( srcname, pattPair->mfs );
                  passnode.getTgtGraph()->insert(srcnode);
                }

                srcnode->setTarget();
                srcnode->setState(GraphNode::MADE);
              }
            }
          }

          // srcnode can be null when a pattern matched the target,
          // but there are no pattern-sources (i.e., either no
          // sources or only explicit sources).  Still need to make
          // sure non-null is returned (note, the actual returned
          // pointer is only used to check for success, its value
          // is never used).
          if (srcnode == 0)
            srcnode = tgtnode;

          // All finished, exit now.
          return (srcnode);
        }
      }
    }
  }

  // Couldn't find a match.
  return (0);
}



boolean  Patterns::processTarget( TargetNode     *tgtnode,
                               PatternPair    *pattPair ) const
{
  // Add the Pattern Pair's commands now that all
  // implied srcs have been approved.
  if (!tgtnode->hasCmds())
  {
    tgtnode->setTarget();

    if (!pattPair->hasCmds())
      return( false );

    tgtnode->addCmds( pattPair->getCmds() );

    if (pattPair->isSet( Constants::OP_FORCEDEP ))
      tgtnode->setType( Constants::OP_FORCEDEP );
  }


  // If the pattern pair has explicit srcs, add them now.
  if (pattPair->getExplicitSrcs().length() > 0)
    tgtnode->parseSources( pattPair->getExplicitSrcs() );

  return( true );
}

