/**
 *  Keyword
 *
**/
#ifndef _ODE_BIN_MAKE_KEYWORD_HPP_
#define _ODE_BIN_MAKE_KEYWORD_HPP_

#include <base/odebase.hpp>
#include "lib/string/strcon.hpp"
#include "lib/string/string.hpp"
#include "lib/portable/hashtabl.hpp"
#include "bin/make/constant.hpp"

class Keyword
{
  friend class Make;

  public:

    /**
     *  Can't use StringConstants::EMPTY_STRING in this constructor
     *  since it is used by a static initializer (no guarantee the
     *  StringConstants module has been initialized yet).
    **/
    Keyword( const String &name = "",
             int val = 0, const boolean source = false ) :
         name( name ), val( val ), source( source ) {};

    Keyword( const Keyword &rhs ) :
         name( rhs.name ), val( rhs.val), source( rhs.source) {};

    ~Keyword() {};

    // operators overload
    inline Keyword &operator=( const Keyword &rhs );
    inline boolean operator==( const Keyword &rhs ) const;
    inline char *toCharPtr() const;

    // access functions
    inline boolean isSource() const;
    inline const String &nameOf() const;
    inline boolean equals( const Keyword &rhs ) const;
    inline boolean equals( const String &rhs ) const;
                    // no compareing with other class type
    inline int getVal() const;
    inline static const Keyword *isSpecialSource( const String &str );
    inline static const Keyword *isSpecialTarget( const String &str );
    inline static boolean isKeyword( const String &str );

  private:
    // static initialization for the special sources and targets
    static boolean initialize();

    // static variables.
    static Hashtable< String, Keyword *>  special_sources;
    static Hashtable< String, Keyword *>  special_targets;
    static Keyword pathKeyword;

    // Instance variables
    String  name;
    int     val;
    boolean source;
};

inline Keyword &Keyword::operator=( const Keyword &rhs )
{
  if (this == &rhs) return *this;

  name = rhs.name;
  val = rhs.val;
  source = rhs.source;

  return *this;
}

inline boolean Keyword::operator==( const Keyword &rhs ) const
{
  return equals( rhs );
}

inline char *Keyword::toCharPtr() const
{
  return (name.toCharPtr());
}

inline boolean Keyword::isSource() const
{
  return source;
}

inline const String &Keyword::nameOf() const
{
  return name;
}

inline boolean Keyword::equals( const Keyword &rhs ) const
{
  if (name.equals(Constants::DOT_PATH) && rhs.name.startsWith(Constants::DOT_PATH))
    return true;

  return name.equals(rhs.name);
}

inline boolean Keyword::equals( const String &rhs ) const
{
  if (name.equals(Constants::DOT_PATH) && rhs.startsWith(Constants::DOT_PATH))
    return true;

  return name.equals(rhs);
}

inline int Keyword::getVal() const
{
  return val;
}

inline const Keyword *Keyword::isSpecialSource( const String &str )
{
  Keyword * const * specsrc = special_sources.get( str );
  if (specsrc == 0 || *specsrc == 0)
    return 0;
  else
    return (*specsrc);
}

inline const Keyword *Keyword::isSpecialTarget( const String &str )
{
  if ( pathKeyword.equals(str) )
    return &pathKeyword;

  Keyword * const *spectgt = special_targets.get( str );
  if (spectgt == 0 || *spectgt == 0)
    return 0;
  else
    return (*spectgt);
}

inline boolean Keyword::isKeyword( const String &str )
{
  return ( isSpecialTarget(str) != 0 || isSpecialSource(str) != 0 );
}

#endif //_ODE_BIN_MAKE_KEYWORD_HPP_

