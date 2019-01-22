#ifndef _ODE_BIN_MAKE_SUFFIX_HPP_
#define _ODE_BIN_MAKE_SUFFIX_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/string/setvars.hpp"
#include "lib/portable/hashable.hpp"

class Dir;

class Suffix
{
  public:
    // constructors and destructor
    Suffix(){};
    Suffix( const String &initsuff , const Dir &initpath) :
        suff( initsuff ), path( initpath ) {};
    Suffix( const String &initsuff, const Dir &initpath, SetVars &vars) :
        suff( initsuff ), path( initpath )
        { setDotPathVars( vars ); };
    Suffix( const Suffix &rhs ):
        suff( rhs.suff ), path( rhs.path ){};

    ~Suffix(){};

    inline boolean operator==( const Suffix &rhs ) const;
    inline char *toCharPtr() const;

    inline const String &getSuff() const;
    inline const Dir    &getPath() const;
    inline const Dir    &getCorePath() const;
    /**
     *  Append path to current Suffix path( not set, we don't change function
     *  name so that it is compatible with old version)
    **/
                 void          setPath( const Dir &path );
    /**
     *  Append path to current Suffix path( not set, we don't change function
     *  name so that it is compatible with old version)
    **/
                 void          setPath( const StringArray *path );

                 void          clearPath( );
                 void          clearCorePath( );
                 void          setCorePath( const StringArray *path );
                 void          setCorePath( const Dir &path );

    /**
     * Set .PATH variable into SetVar vars by path array in Dir path
    **/
                 void          setDotPathVars( SetVars &vars );
    inline       boolean       equals( const Suffix &obj ) const;

  // Data Members
  //
  private:
    SmartCaseString suff;
    Dir             path;
    Dir             corepath;
};

inline boolean Suffix::operator==( const Suffix &rhs ) const
{
  return (equals( rhs ));
}

inline char *Suffix::toCharPtr() const
{
  return (suff.toCharPtr());
}

inline const String &Suffix::getSuff() const
{
  return (suff);
}

inline const Dir &Suffix::getPath() const
{
  return (path);
}

inline const Dir &Suffix::getCorePath() const
{
  return (corepath);
}

/**
 * Define equals to be the same as SmartCaseString tgt equals.
 */
inline boolean Suffix::equals( const Suffix &obj) const
{
  return (suff.equals( obj.suff ));
}

#endif //_ODE_BIN_MAKE_SUFFIX_HPP_

