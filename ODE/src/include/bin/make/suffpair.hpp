/**
 * SuffixPair
 *
**/
#ifndef _ODE_BIN_MAKE_SUFFPAIR_HPP_
#define _ODE_BIN_MAKE_SUFFPAIR_HPP_

#include "base/odebase.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/portable/vector.hpp"

#include "bin/make/linesrc.hpp"
#include "bin/make/cmdable.hpp"
#include "bin/make/suffix.hpp"
#include "bin/make/mfstmnt.hpp"

class SuffixPair : public LineSource, public Commandable
{
  public:
    // constructors and destructor
    SuffixPair() {};
    SuffixPair( const Suffix &initsuffsrc, const Suffix &initsufftgt,
                const MakefileStatement *mfs,
                const String &initsrcs,
                const String &initsrcdir,
                const String &inittgtdir ) :
        LineSource( mfs ), src( initsuffsrc ), tgt( initsufftgt ),
        explicit_srcs( initsrcs ), src_dir( initsrcdir ),
        tgt_dir( inittgtdir )
    {};
    SuffixPair( const SuffixPair &rhs ) :
        LineSource( rhs.mfs), src( rhs.src ), tgt( rhs.tgt ),
        explicit_srcs( rhs.explicit_srcs ), src_dir( "" ), tgt_dir( "" )
    {};
    ~SuffixPair(){};

    inline boolean operator==( const Commandable &rhs )const;
    inline boolean operator==( const SuffixPair &rhs ) const;
    inline int getCmdType() const;
    inline const Suffix &getSrc() const;
    inline const Suffix &getTgt() const;
    inline const String &getSrcDir() const;
    inline const String &getTgtDir() const;
    inline const String &getExplicitSrcs() const;
    inline void  addExplicitSrcs( const String &more_srcs);
    inline void  removeExplicitSrcs();
    inline boolean equals(const SuffixPair &obj) const;
    inline void setSrcDir( String &newSrcDir );
    inline void setTgtDir( String &newTgtDir );

  private:
    Suffix src;
    Suffix tgt;
    String src_dir;
    String tgt_dir;
    String explicit_srcs;
};

inline boolean SuffixPair::operator==( const Commandable &rhs ) const
{
  return false;
}

inline boolean SuffixPair::operator==( const SuffixPair &rhs ) const
{
  return equals(rhs);
}

inline int SuffixPair::getCmdType() const
{
  return SUFFIX_PAIR_CMD;
}

inline const Suffix &SuffixPair::getSrc() const
{
  return src;
}

inline const Suffix &SuffixPair::getTgt() const
{
  return tgt;
}

inline const String &SuffixPair::getSrcDir() const
{
  return src_dir;
}

inline const String &SuffixPair::getTgtDir() const
{
  return tgt_dir;
}

inline const String &SuffixPair::getExplicitSrcs() const
{
  return (explicit_srcs);
}

inline void SuffixPair::addExplicitSrcs( const String &more_srcs )
{
  explicit_srcs += StringConstants::SPACE;
  explicit_srcs += more_srcs;
}

inline void SuffixPair::removeExplicitSrcs( )
{
  explicit_srcs = StringConstants::EMPTY_STRING;
}

inline void SuffixPair::setSrcDir( String &newsrcdir )
{
  src_dir = newsrcdir;
}

inline void SuffixPair::setTgtDir( String &newtgtdir )
{
  tgt_dir = newtgtdir;
}

/**
 * Define equals to be the same as String tgt equals.
 */
inline boolean SuffixPair::equals( const SuffixPair &obj ) const
{
  return tgt.equals(obj.tgt);
}

#endif //_ODE_BIN_MAKE_SUFFPAIR_HPP_

