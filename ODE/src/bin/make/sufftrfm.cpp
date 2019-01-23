/**
 * SuffixTransforms
 *
**/
using namespace std;
#define _ODE_BIN_MAKE_SUFFTRFM_CPP_

#include <base/binbase.hpp>
#include "bin/make/sufftrfm.hpp"
#include "bin/make/targnode.hpp"
#include "bin/make/passnode.hpp"
#include "bin/make/makec.hpp"
#include "bin/make/graph.hpp"

/**
 * This method sets all variables of the form .PATH[.suffix]
 * @param vars The SetVars object to set the variables.
**/
void SuffixTransforms::setAllDotPathVars( SetVars &vars )
{
  VectorEnumeration< Suffix *> enumsuffixes( &suffixes );
  while (enumsuffixes.hasMoreElements())
    (*enumsuffixes.nextElement())->setDotPathVars(vars);
}

/**
 * Append this Dir path to all Dir paths of all suffixes.
**/
void SuffixTransforms::addDirToAllSuffs( Dir &dirs )
{
  VectorEnumeration< Suffix *> enumsuffixes( &suffixes );
  while (enumsuffixes.hasMoreElements())
    (*enumsuffixes.nextElement())->setPath(dirs);
}


/**
 * Update with possible VPATH to all Dir paths of all suffixes.
**/
void SuffixTransforms::updateAllSuffPaths( PassNode *pass )
{
  Suffix    *suff;
  Dir       temppath;
  VectorEnumeration< Suffix *> enumsuffixes( &suffixes );

  while (enumsuffixes.hasMoreElements())
  {
    suff = *enumsuffixes.nextElement();

    if (suff->getCorePath().getPath())
      pass->buildNewSearchPath( *(suff->getCorePath().getPath()), &temppath );
    else
      pass->buildNewSearchPath( StringArray( (unsigned long)0 ), &temppath );

    suff->clearPath();
    suff->setPath( temppath );
  }
}


/**
 * Clear all suffix in Vector suffixes and deallocate memory.
**/
void SuffixTransforms::clearSuffs()
{
  // deallocate the Suffixes memory in
  // Vector<Suffix *>
  Suffix *suffix;
  VectorEnumeration< Suffix *> enumsuffixes( &suffixes );
  while (enumsuffixes.hasMoreElements())
  {
    suffix = *enumsuffixes.nextElement();
    if (suffix != 0)
      delete suffix;
  }
  suffixes.removeAllElements();
}

/**
 * Find all SuffixPair those have the same Target Suffix name which is suff.
**/
Vector< SuffixPair *> *SuffixTransforms::find( const String &suff ) const
{
  Vector<SuffixPair *> * const *sppptr = transforms.get(suff);
  if (sppptr == 0)
    return (0);
  else
    return (*sppptr);
}

SuffixTransforms::~SuffixTransforms()
{
  // deallocate the SuffixPairs memory in
  // Hashtable<Suffix, Vector<SuffixPair *> *>
  HashElementEnumeration< SmartCaseString, Vector< SuffixPair* >* >
      enumer( &transforms );
  Vector<SuffixPair *> * const *suffpairs;
  SuffixPair *suffpair;
  while (enumer.hasMoreElements())
  {
    suffpairs = enumer.nextElement();
    if (suffpairs != 0 || (*suffpairs) != 0)
    {
       VectorEnumeration< SuffixPair * > enumpairs( *suffpairs );
      while (enumpairs.hasMoreElements())
      {
        suffpair = *(enumpairs.nextElement());
        if (suffpair != 0)
          delete suffpair;
      }
    }
  }

  // deallocate the Suffixes memory in
  // Vector<Suffix *>
  clearSuffs();
}

/**
 * Find Suffix by suffix name from Vector suffixes.
 * return Suffix pointer the found suffix pointer.
 * zero not found.
**/
Suffix *SuffixTransforms::findSuff( const String &name ) const
{
  String trimname( name.trim() );
  if (trimname.length() == 0)
    return 0;

  Suffix * const *res;
  VectorEnumeration< Suffix *> enumsuffixes( &suffixes );
  while (enumsuffixes.hasMoreElements())
  {
    res = enumsuffixes.nextElement();
#ifdef CASE_INSENSITIVE_OS
    if ((*res)->getSuff().equalsIgnoreCase( trimname ))
#else
    if ((*res)->getSuff().equals( trimname ))
#endif
      return ( *res );
  }
  return ( 0 );
}


/**
 * Insert a SuffixPair pointer into Hashtable transforms. First try to find
 * this Suffixpair vector with the Target Suffix of this suffixpair as key.
 * If found, then add this suffixpair into this vector. If not, Create
 * a new SuffixPair Vector and put this suffixpair into this Vector and
 * add this vector as value and the Target
 * Suffix as key into the Hashtable tansforms.
**/
void SuffixTransforms::insert( SuffixPair *pair )
{
  if (pair == 0)
    return;

  Vector<SuffixPair *> *const *lst = transforms.get( pair->getTgt().getSuff() );
  if (MkCmdLine::dSuffs())
  {
    Interface::printAlways("Suff: Adding "+(pair->getSrc().getSuff())+
      "->"+(pair->getTgt().getSuff()));
  }
  if (lst == 0)
  {
    Vector<SuffixPair *> *newlst = new Vector<SuffixPair *>(10, elementsEqual);
    newlst->addElement( pair );
    transforms.put( pair->getTgt().getSuff(), newlst);
  }
  else
  {
    // Add in the order defined by .SUFFIXES.
    //
    VectorEnumeration< SuffixPair * > enumlst( *lst );
    VectorEnumeration< Suffix * > enumsuffixes( &suffixes );
    SuffixPair ** findpair;
    const Suffix *suffix;
    int idx, insertidx = ARRAY_FIRST_INDEX;
    StringArray lastsuffs;

    // First build a list of .SUFFIXES that follow the given source
    //
    while (enumsuffixes.hasMoreElements())
    {
      suffix = *enumsuffixes.nextElement();
      if (suffix->getSuff().equals( pair->getSrc().getSuff() ))
      {
        // Now that we have the position of the desired suffix, add
        // all the following ones so we can better determine the position
        // for inserting.
        //
        while (enumsuffixes.hasMoreElements())
        {
          suffix = *enumsuffixes.nextElement();
          lastsuffs.add( suffix->getSuff() );
        }
      }
    }

    // Now we go through the list of known transformations and find the
    // appropriate place to insert the new pair.  If one exists, we replace
    // it.  If transforms exist, we position it in the order defined by
    // .SUFFIXES.
    //
    while (enumlst.hasMoreElements())
    {
      // Cast off the const
      findpair = (SuffixPair **)enumlst.nextElement();

      for (idx = lastsuffs.firstIndex(); idx <= lastsuffs.lastIndex(); idx++)
      {
        // Since we're past where the pair needs to be inserted then insert
        // the pair before the current element.
        //
#ifdef CASE_INSENSITIVE_OS
        if ((*findpair)->getSrc().getSuff().equalsIgnoreCase(
            lastsuffs[idx] ))
#else
        if ((*findpair)->getSrc().getSuff().equals(
            lastsuffs[idx] ))
#endif
        {
          (*lst)->insertElementAt( pair, insertidx );
          return;
        }
      }
      insertidx++;
    }

    // Last resort, add to end of list.
    //
    (*lst)->addAsLast( pair );
  }
}

/**
 * Find ths SuffixPair whose Source Suffix name is src, Target Suffix
 * name is tgt.
 * Return Found SuffixPair Pointer. Zero not found
**/
SuffixPair *SuffixTransforms::findSuffixPair( const String &src,
    const String &tgt, const String &srcdir, const String &tgtdir ) const
{
  Vector< SuffixPair *> *pairs=find(tgt);
  if (pairs == 0) return 0;

  SuffixPair *const *pairnd;
  VectorEnumeration< SuffixPair * > enumpairs( pairs );
  while (enumpairs.hasMoreElements())
  {
    pairnd = enumpairs.nextElement();
    if (*pairnd == 0)
      continue;

#ifdef CASE_INSENSITIVE_OS
    if ((*pairnd)->getSrc().getSuff().equalsIgnoreCase( src ) &&
        (*pairnd)->getSrcDir().equalsIgnoreCase( srcdir ) &&
        (*pairnd)->getTgtDir().equalsIgnoreCase( tgtdir ))
#else
    if ((*pairnd)->getSrc().getSuff().equals( src ) &&
        (*pairnd)->getSrcDir().equals( srcdir ) &&
        (*pairnd)->getTgtDir().equals( tgtdir ))
#endif
      return ( *pairnd );
  }
  return ( 0 );
}

TargetNode *SuffixTransforms::getImpSrcs(
  TargetNode         *tgtnode,
  PassInstance       &passnode,
  SetVars            &local_vars ) const
     // throw ParseException()
{
  String suff;

  if (tgtnode == 0)
    return ( 0 );

  // First check and see if the suffix is on the list of known and
  // approved suffixes.  The list specified by the .SUFFIXES special
  // target.
  Suffix *tgtsuff = findSuff( tgtnode->getSuff() );
  if (tgtsuff == 0)
    suff = StringConstants::EMPTY_STRING;
  else
    suff = tgtnode->getSuff();

  // Get all transformations that match the given suffix.
  Vector<SuffixPair *> *trans = find( suff );

  // If no transformations found, then nothing to do.
  if (trans == 0)
    return 0;

  SuffixPair * const *pair=0;
  Suffix src;
  String srcname;
  String tgtprefix;

  // Find which transformation actually works.  This is defined by
  // first searching transforms like .c.o: and if the target is .o
  // then look for a file that exists like .c.

  // First make sure that none of the children are explicitly defined.
  // We could have:
  //  f.o: ../../dir1/f.c
  // So we'll try to find it now.
  const GraphNode *tmpgn = 0;
  TargetNode *srcnode = 0;
  VectorEnumeration< GraphNode * >  enumchildren( &tgtnode->children );
  VectorEnumeration< SuffixPair * > enumtrans( trans );

  // Start looping with the children since the explicity defined "implied" sources
  // will take precedence over the order in which the suffix transformations were
  // defined in the makefile.
  while (enumchildren.hasMoreElements())
  {
    tmpgn = *(enumchildren.nextElement());
    if (tmpgn->getNodeType() != GraphNode::TARGET_NODE)
      continue;

    srcnode = (TargetNode *)tmpgn;

    enumtrans.setObject( trans );
    while (enumtrans.hasMoreElements())
    {
      pair = enumtrans.nextElement();
      if (*pair == 0)
        continue;

      src = (*pair)->getSrc();

      if (MkCmdLine::dSuffs())
        Interface::printAlways("Suff: Trying "+src.getSuff()+
          "->"+tgtnode->getSuff()+" for "+tgtnode->nameOf());

      // First construct the source name and see if it exists in the graph.
      srcname  = tgtnode->getPrefix();
      srcname += src.getSuff();

#ifdef CASE_INSENSITIVE_OS
      if (srcname.equalsIgnoreCase( Path::fileName( srcnode->nameOf() ) ))
#else
      if (srcname.equals( Path::fileName( srcnode->nameOf() ) ))
#endif
      {
        if (!tgtnode->hasCmds())
        {
          tgtnode->setTarget();

          if (!(*pair)->hasCmds())
            continue;

          tgtnode->addCmds( (*pair)->getCmds() );
          if (srcnode->getCF() == 0)
            srcnode->setCF( src.getPath().findFile( srcnode->nameOf() ) );
        }
        // If the suffix pair has explicit srcs, add them now.
        if ((*pair)->getExplicitSrcs().length() > 0)
          tgtnode->parseSources( (*pair)->getExplicitSrcs() );
        tgtnode->setImpSrc( srcnode );

        if (MkCmdLine::dSuffs())
          Interface::printAlways( "Suff: Using  " + srcnode->nameOf() +
                                  "->" + tgtnode->nameOf() );

        return (srcnode);
      }
    }
  }

  // Enumerate the list of possible transformations (transforms with a
  // matching target suffix).  Stop when we find a transform that satisfies
  // the requirements of this target.
  CachedFile *srcfile = 0;
  enumtrans.setObject( trans );
  while (enumtrans.hasMoreElements())
  {
    pair = enumtrans.nextElement();
    if (*pair == 0)
      continue;

    tgtprefix = tgtnode->getPrefix();
    int tgtidx = tgtprefix.lastIndexOf( StringConstants::FORW_SLASH );
    String tgtdir = tgtprefix.substring( STRING_FIRST_INDEX, tgtidx + 1 );

    // Skip this suffix transformation if our target has a specific target
    // subdirectory and it doesn't match the transformation.
    if (((*pair)->getTgtDir() != StringConstants::EMPTY_STRING) &&
        (Path::unixize(tgtdir) != (*pair)->getTgtDir()))
      continue;

    src = (*pair)->getSrc();

    if (MkCmdLine::dSuffs())
      Interface::printAlways("Suff: Trying "+src.getSuff()+
        "->"+tgtnode->getSuff()+" for "+tgtnode->nameOf());

    // First construct the source name and see if it exists in the graph.
    if ((*pair)->getSrcDir() == StringConstants::EMPTY_STRING &&
        (*pair)->getTgtDir() == StringConstants::EMPTY_STRING )
    {
      // We need to use the transformation's target directory to build the
      // implied source.
      srcname = tgtnode->getPrefix();
      srcname += src.getSuff();
    }
    else
    {
      // We need to use the transformation's source directory to build the
      // implied source.
      tgtprefix.substringThis( tgtidx + 1 );
      srcname  = (*pair)->getSrcDir();
      srcname += tgtprefix;
      srcname += src.getSuff();
    }

    srcnode  = (TargetNode *)passnode.getTgtGraph()->find(srcname);

    // If the source was found and the parent doesn't have any way to
    // update the node, then give the parent node the suffix tranformation
    // commands.
    if (srcnode != 0)
    {
      tgtnode->addChild( srcnode );
      srcnode->addParent(tgtnode);
      if (!tgtnode->hasCmds())
      {
        tgtnode->setTarget();
        if (!(*pair)->hasCmds())
          continue;

        tgtnode->addCmds( (*pair)->getCmds() );
        tgtnode->setImpSrc( srcnode );

        if ((*pair)->isSet( Constants::OP_FORCEDEP ))
          tgtnode->setType( Constants::OP_FORCEDEP );
      }
      // If the suffix pair has explicit srcs, add them now.
      if ((*pair)->getExplicitSrcs().length() > 0)
        tgtnode->parseSources( (*pair)->getExplicitSrcs() );

      if (MkCmdLine::dSuffs())
        Interface::printAlways("Suff: Using  " + srcnode->nameOf() + "->" +
                               tgtnode->nameOf());
      return (srcnode);
    }

    // Construct the source name again and put the separator bar in
    // so we know with certainty where to split it later.
    if ((*pair)->getSrcDir() == StringConstants::EMPTY_STRING &&
        (*pair)->getTgtDir() == StringConstants::EMPTY_STRING)
    {
      srcname = tgtnode->getPrefix();
      srcname += StringConstants::BAR;
      srcname += src.getSuff();
    }
    else
    {
      srcname  = (*pair)->getSrcDir();
      srcname += tgtprefix;
      srcname += StringConstants::BAR;
      srcname += src.getSuff();
    }

    // If the source wasn't in the graph then lets do the sources, by looking
    // for the source.
    srcfile = src.getPath().findFile( srcname );

    // If we haven't found it then lets look at the next source.
    if (srcfile != 0)
    {
      // Since we found a source, create a new target node and stick it
      // in the graph.
      srcnode = tgtnode->createAndInsertChild( srcname, &passnode,
                                               (*pair)->mfs );

      // Since we have a cached copy then assign it to the node so we don't
      // have to go for it again.
      srcnode->setCF( srcfile );
      tgtnode->setImpSrc( srcnode );
      if (!tgtnode->hasCmds())
      {
        tgtnode->setTarget();
        if (!(*pair)->hasCmds())
          continue;

        tgtnode->addCmds( (*pair)->getCmds() );

        if ((*pair)->isSet( Constants::OP_FORCEDEP ))
          tgtnode->setType( Constants::OP_FORCEDEP );
      }

      // If the suffix pair has explicit srcs, add them now.
      if ((*pair)->getExplicitSrcs().length() > 0)
        tgtnode->parseSources( (*pair)->getExplicitSrcs() );

      if (MkCmdLine::dSuffs())
        Interface::printAlways( "Suff: Using  " + srcnode->nameOf() + "->"
                                + tgtnode->nameOf() );

      return (srcnode);
    }
  } // end while

  // If we made it this far then a source was not found in the first level.
  // Go searching another level. For example, .a->.o->.h->.y
  TargetNode *newsrcnd=0;
  boolean bTransformWorks;
  enumtrans.setObject( trans );
  while (enumtrans.hasMoreElements())
  {
    pair = enumtrans.nextElement();
    if (*pair == 0)
      continue;

    src = (*pair)->getSrc();

    // We know that the source doesn't exist so try to find
    // the source's source.
    newsrcnd = new TargetNode( tgtnode->getPrefix() + StringConstants::BAR +
                               src.getSuff(),
                               (*pair)->mfs,
                               &passnode );

    bTransformWorks = true; // assume we will find the transformation

    // If an implied source is found then we can add it to the target
    // and return
    if (passnode.getRootVars()->
                  find(StringConstants::ODEMAKE_TFMFIRST_VAR ) != 0 )
    {
      if (!getImpSrcs( newsrcnd, passnode, local_vars ) &&
          !passnode.getPatterns()->getImpSrcs( newsrcnd, passnode, local_vars ))
      {
        // End of the line.  No way to make srcnode.
        bTransformWorks = false;
      }
    }
    else if (!passnode.getPatterns()->getImpSrcs( newsrcnd, passnode,
                                                  local_vars) &&
             !getImpSrcs( newsrcnd, passnode, local_vars ))
    {
      // End of the line.  No way to make srcnode.
      bTransformWorks = false;
    }

    if (bTransformWorks)
    {
      tgtnode->createAndInsertChild( newsrcnd->nameOf(), &passnode,
        (*pair)->mfs, newsrcnd );

      if (!tgtnode->hasCmds())
      {
        tgtnode->setTarget();
        if (!(*pair)->hasCmds())
          continue;

        tgtnode->addCmds((*pair)->getCmds());
        tgtnode->setImpSrc( newsrcnd );
      }

      // If the suffix pair has explicit srcs, add them now.
      if ((*pair)->getExplicitSrcs().length() > 0)
        tgtnode->parseSources( (*pair)->getExplicitSrcs() );

      if (MkCmdLine::dSuffs())
        Interface::printAlways( "Suff: Using  " + newsrcnd->nameOf() +
                                "->" + tgtnode->nameOf() );

      return (newsrcnd);
    }
    // @@@ Need to look for levels deeper then 1, but not too far.
    // The old version of make will take the shortest route to the source,
    // I guess the new one should, too.
    delete newsrcnd;
    newsrcnd = 0;

  } // end while

  // Couldn't find a match.
  return (0);
}
