/**
 *  Makefile
 *
**/
#ifndef _ODE_BIN_MAKE_MAKEFILE_HPP_
#define _ODE_BIN_MAKE_MAKEFILE_HPP_

#include <base/odebase.hpp>
#include "lib/io/cfgf.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/portable/vector.hpp"

#include "lib/exceptn/parseexc.hpp"
#include "bin/make/mfstmnt.hpp"

class Dir;
class MakefileStatement;
class PassNode;
class MakefileCache;

class Makefile : public ConfigFile
{
  public:
    // constructors and destructor
    Makefile( const String &filename ) :
      ConfigFile( filename ),
      lines( Vector<MakefileStatement>(hashsize) ) {};

    ~Makefile() {};

    // operators overload
    inline boolean   operator==( const Makefile &rhs ) const;

    /**
     * This method loads a makefile but doesn't parse it into a target/dep graph.
     * @return *Makefile, the caller shouldn't deallocate the returned pointer
    **/
#ifdef __WEBMAKE__
    static Makefile *load( const String &filename, const String &cwd,
                           const Dir &searchpath, CondEvaluator *condeval=0 );
#else
    static Makefile *load( const String &filename, const String &cwd,
                           const Dir &searchpath, boolean replContin = true );
#endif // __WEBMAKE__
    inline void      addStatement( const MakefileStatement &stmt );
    inline const     Vector< MakefileStatement > *getStatements() const;
           void      instantiate( PassNode &pass );

  private:
#ifdef __WEBMAKE__
    Makefile *loadInCache( CondEvaluator *condeval );
#else
    Makefile *loadInCache( boolean replContin = true );
#endif // __WEBMAKE__

    static const int hashsize; // Initial hash table size
    Vector< MakefileStatement > lines; // Vector of MakefileStatement

};

inline boolean Makefile::operator==( const Makefile &rhs ) const
{
  return (getPathname().equals(rhs.getPathname()));
}
inline void Makefile::addStatement( const MakefileStatement &stmt )
{
  lines.addElement( stmt );
}

inline const Vector< MakefileStatement > *Makefile::getStatements() const
{
  return &lines;
}

#endif //_ODE_BIN_MAKE_MAKEFILE_HPP_

