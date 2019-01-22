
/**
 * suffixTransforms
 *
**/
#ifndef _ODE_BIN_MAKE_SUFFTRFM_HPP_
#define _ODE_BIN_MAKE_SUFFTRFM_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/setvars.hpp"
#include "lib/portable/hashtabl.hpp"
#include "lib/portable/vector.hpp"
#include "lib/util/cachfile.hpp"
#include "lib/io/ui.hpp"

#include "bin/make/dir.hpp"
#include "bin/make/suffix.hpp"
#include "bin/make/suffpair.hpp"
#include "bin/make/passnode.hpp"
#include "bin/make/passinst.hpp"
#include "lib/exceptn/parseexc.hpp"

class TargetNode;

class SuffixTransforms
{
  public:
    // constructors and destructor
    SuffixTransforms() :
      suffixes( 10, elementsEqual )
    {};

    ~SuffixTransforms();

    /**
     * Testing Vector suffixes is empty.
     * Return true it's empty.
    **/
    inline boolean     isEmpty() const;

    /**
     * Clear all suffix in Vector suffixes and deallocate memory.
    **/
    void               clearSuffs();

    /**
     * Insert a Suffix into Vector suffixes. First try to find this suffix
     * by name suff from suffixes. If it could be found. Then Create a new
     * Suffix and insert into suffixes.
    **/
    inline Suffix     *insertSuff( const String &suff,  PassInstance &passnode );

    /**
     * Find Suffix by suffix name from Vector suffixes.
     * return Suffix pointer the found suffix pointer.
     * zero not found.
    **/
    Suffix            *findSuff( const String &name ) const;

     /**
     * Insert a SuffixPair pointer into Hashtable transforms. First try to find
     * this Suffixpair vector with the Target Suffix of this suffixpair as key.
     * If found, then add this suffixpair int this vector. If not, Create
     * a new SuffixPair Vector and put this suffixpair into this Vector and
     * add this vector as value and the Target
     * Suffix as key into the Hashtable tansforms.
    **/
    void               insert(  SuffixPair *pair );

    /**
     * Append this Dir path to all Dir paths of all suffixes.
    **/
           void        addDirToAllSuffs( Dir &dirs );


           void        updateAllSuffPaths( PassNode *pass );

    /**
     * This method sets all variables of the form .PATH[.suffix]
     * @param vars The SetVars object to set the variables.
    **/
           void        setAllDotPathVars( SetVars &vars );

    /**
     * First Try to find the Suffix from vector suffixes whose name is suff.
     * Then append this Dir path to the Dir path of that found suffix.
    **/
    inline void        addDirToSuff( const String &suff,  Dir &dirs );

    /**
     *
     * caller should deallocate the returned TargetNode pointer
    **/
    TargetNode         *getImpSrcs( TargetNode         *tgtnode,
                                    PassInstance       &passnode,
                                    SetVars            &local_vars ) const;
      // throw ParseException()

    /**
     * Find all SuffixPair those have the same Target Suffix name which is suff.
    **/
    Vector< SuffixPair *> *find( const String &suff ) const;

    /**
     * Find ths SuffixPair whose Source Suffix name is src, Target Suffix
     * name is tgt.
     * Return Found SuffixPair Pointer. Zero not found
    **/
    SuffixPair         *findSuffixPair( const String &src,
      const String &tgt, const String &srcdir, const String &tgtdir ) const;

  private:
    Hashtable< SmartCaseString, Vector<SuffixPair * > * >  transforms;
    Vector<Suffix *>                                      suffixes;
};

/**
 * Testing Vector suffixes is empty.
 * Return true it's empty.
**/
inline boolean SuffixTransforms::isEmpty() const
{
  return (suffixes.isEmpty());
}

/**
 * Insert a Suffix into Vector suffixes. First try to find this suffix
 * by name suff from suffixes. If it could be found. Then Create a new
 * Suffix and insert into suffixes.
 * Return new Suffix if created and inserted successfully.
 * Return 0  if suffix already exists.
**/
inline Suffix *SuffixTransforms::insertSuff( const String &suff,
  PassInstance &passnode )
{
  if (findSuff( suff ) == 0)
  {
    Suffix  *newSuff;
    SetVars *vars = passnode.getGlobalVars();
    if (vars == 0)
      newSuff = new Suffix( suff, Dir( 0, &passnode ) );
    else
      newSuff = new Suffix( suff, Dir( 0, &passnode ), *vars );

    suffixes.addElement( newSuff );
    return ( newSuff );
  }
  return ( 0 );
}

/**
 * First Try to find the Suffix from vector suffixes whose name is suff.
 * Then append this Dir path to the Dir path of that found suffix.
**/
inline void SuffixTransforms::addDirToSuff( const String &suff,
                                            Dir &dirs )
{
  Suffix *tmpsuff = findSuff( suff );
  if (tmpsuff == 0)
    // Suffix not found.
    return;
  else
    tmpsuff->setPath( dirs );
}

#endif //_ODE_BIN_MAKE_SUFFTRFM_HPP_



