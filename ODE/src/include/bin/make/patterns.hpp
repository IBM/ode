/**
 * Patterns
 *
**/
#ifndef _ODE_BIN_MAKE_PATTERNS_HPP_
#define _ODE_BIN_MAKE_PATTERNS_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/setvars.hpp"
#include "lib/portable/vector.hpp"
#include "lib/util/cachfile.hpp"
#include "lib/io/ui.hpp"

#include "bin/make/dir.hpp"
#include "bin/make/pattpair.hpp"
#include "bin/make/passnode.hpp"
#include "bin/make/passinst.hpp"
#include "lib/exceptn/parseexc.hpp"

class TargetNode;

class Patterns
{
  public:
    // constructors and destructor
    Patterns() :
      patterns( 10, elementsEqual )
    {};

    ~Patterns();

    /**
     * Insert a PatternPair pointer into the maintained pattern vector.
    **/
    void               insert(  PatternPair *pair );


    /**
     * Testing Vector patterns is empty.
     * Return true if it's empty.
    **/
    inline boolean     isEmpty() const;


    /**
     * caller should deallocate the returned TargetNode pointer
    **/
    TargetNode         *getImpSrcs( TargetNode         *tgtnode,
                                    PassInstance       &passnode,
                                    SetVars            &local_vars ) const;



    /**
     *  Add commands and explicit targest to given TargetNode
    **/
    boolean            processTarget( TargetNode       *tgtnode,
                                      PatternPair      *pattPair ) const;

    /**
     * Determine if the explict target (tgtname) fits the pattern indicated
     * by token, and if so determine and set the wildcard string.
    **/
    boolean isMatch( String   &tgtname,
                     String   &token,
                     String   &wildcardValue ) const;

    /**
     * Find the PatternPair in the maintained vector.
     * Both the implied targets and implied sources must match.
     * Return Found PatternPair Pointer. Zero not found
    **/
    PatternPair         *findPatternPair( const String &src,
                                          const String &tgt ) const;

  private:
    Vector< PatternPair * >               patterns;
};


/**
 * Testing Vector patterns is empty.
 * Return true if it's empty.
**/
inline boolean Patterns::isEmpty() const
{
  return (patterns.isEmpty());
}



#endif //_ODE_BIN_MAKE_PATTERNS_HPP_
