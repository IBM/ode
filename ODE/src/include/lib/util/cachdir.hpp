/**
 * String
 *
**/
#ifndef _ODE_LIB_UTIL_CACHDIR_HPP_
#define _ODE_LIB_UTIL_CACHDIR_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/util/cachfile.hpp"
#include "lib/portable/hashtabl.hpp"

class FileCache;

class CachedDir : public CachedFile
{
  friend class FileCache;

  public:

    CachedDir() :
        cached_some( false ) {};
    CachedDir( const CachedDir &cd ) :
        CachedFile( cd ), files( cd.files ), cached_some( false ) {};
    CachedDir( const String &filename, boolean statnow ) :
        CachedFile( filename, statnow ), cached_some( false ) {};
    CachedDir( const String &dirname, const String &filename ) :
        CachedFile( dirname, filename ), cached_some( false ) {};
    inline void    setCachedSome( boolean new_state = true );
    inline boolean isCachedSome() const;

  private:

    Hashtable< SmartCaseString, CachedFile* > files;
    boolean   cached_some; // If true, directory on partially cached
};

inline void CachedDir::setCachedSome( boolean new_state )
{
  cached_some = new_state;
}

inline boolean CachedDir::isCachedSome() const
{
  return (cached_some);
}

#endif /* _ODE_LIB_UTIL_CACHDIR_HPP_ */
