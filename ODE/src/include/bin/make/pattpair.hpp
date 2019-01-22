/**
 * PatternPair
 *
**/
#ifndef _ODE_BIN_MAKE_PATTPAIR_HPP_
#define _ODE_BIN_MAKE_PATTPAIR_HPP_

#include "base/odebase.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/portable/vector.hpp"

#include "bin/make/linesrc.hpp"
#include "bin/make/cmdable.hpp"
#include "bin/make/mfstmnt.hpp"

class PatternPair : public LineSource, public Commandable
{
  public:
    // constructors and destructor
    PatternPair() {};
    PatternPair( const String &targs,
                 const String &impliedSrcs,
                 const String &explicitSrcs,
                 const MakefileStatement *mfs ) :
        LineSource( mfs ),  tgt( targs ), src( impliedSrcs ),
        explicit_srcs( explicitSrcs )
    {};
    PatternPair( const PatternPair &rhs ) :
        LineSource( rhs.mfs), src( rhs.src ), tgt( rhs.tgt ),
        explicit_srcs( rhs.explicit_srcs )
    {};
    ~PatternPair(){};

    inline boolean operator==( const Commandable &rhs )const;
    inline boolean operator==( const PatternPair &rhs ) const;
    inline int getCmdType() const;
    inline const String &getSrc() const;
    inline const String &getTgt() const;
    inline const String &getExplicitSrcs() const;
    inline void  addExplicitSrcs( const String &more_srcs);
    inline void  removeExplicitSrcs();
    inline boolean equals(const PatternPair &obj) const;

  private:
    String tgt;              // Target(s).
    String src;              // Implied source(s).
    String explicit_srcs;    // Explicit source(s).
};

inline boolean PatternPair::operator==( const Commandable &rhs ) const
{
  return false;
}

inline boolean PatternPair::operator==( const PatternPair &rhs ) const
{
  return equals(rhs);
}

inline int PatternPair::getCmdType() const
{
  return PATTERN_PAIR_CMD;
}

inline const String &PatternPair::getSrc() const
{
  return src;
}

inline const String &PatternPair::getTgt() const
{
  return tgt;
}

inline const String &PatternPair::getExplicitSrcs() const
{
  return (explicit_srcs);
}

inline void PatternPair::addExplicitSrcs( const String &more_srcs )
{
  explicit_srcs += StringConstants::SPACE;
  explicit_srcs += more_srcs;
}

inline void PatternPair::removeExplicitSrcs( )
{
  explicit_srcs = StringConstants::EMPTY_STRING;
}

/**
 * Equals if both target (tgt) and implied sources (src) are the same.
 */
inline boolean PatternPair::equals( const PatternPair &obj ) const
{
  return( tgt.equals(obj.tgt) && src.equals(obj.src) );
}

#endif //_ODE_BIN_MAKE_PATTPAIR_HPP_
