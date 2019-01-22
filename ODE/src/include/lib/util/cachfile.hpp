/**
 * CachedFile
 *
**/
#ifndef _ODE_LIB_UTIL_CACHFILE_HPP_
#define _ODE_LIB_UTIL_CACHFILE_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/io/file.hpp"
#include "lib/io/path.hpp"

class CachedFile : public File
{
  public:

    inline CachedFile();
    inline CachedFile( const File &cf );
    inline CachedFile( const CachedFile &cf );
    inline CachedFile( const String &filename, boolean statnow );
    inline CachedFile( const String &dirname, const String &filename );
    // this last one is optimized for speed for the FileCache
    inline CachedFile( const String &dirname, char *filename );

    inline void restat();
    inline const SmartCaseString &getUnixPath() const;
    inline CachedFile &operator=( const SmartCaseString &file );
    inline CachedFile &operator=( const File &file );
    inline CachedFile &operator=( const CachedFile &file );


  private:

#ifdef DEFAULT_SHELL_IS_CMD
    SmartCaseString unixized;
#endif
    inline void createUnixizedPath();
};

inline CachedFile::CachedFile()
{
}

inline CachedFile::CachedFile( const File &cf ) :
    File( cf )
{
  createUnixizedPath();
}

inline CachedFile::CachedFile( const CachedFile &cf ) :
    File( cf )
#ifdef DEFAULT_SHELL_IS_CMD
    , unixized( cf.unixized )
#endif
{
}

inline CachedFile::CachedFile( const String &filename, boolean statnow ) :
    File( filename, statnow )
{
  createUnixizedPath();
}

inline CachedFile::CachedFile( const String &dirname, const String &filename ) :
    File( dirname, filename )
{
  createUnixizedPath();
}

inline CachedFile::CachedFile( const String &dirname, char *filename ) :
    File( dirname, filename )
{
  createUnixizedPath();
}

inline void CachedFile::createUnixizedPath()
{
#ifdef DEFAULT_SHELL_IS_CMD
  unixized = *this;
  Path::unixizeThis( unixized );
#endif
}

inline void CachedFile::restat()
{
  if (!isLink())
    File::setModTime();   // just update mtime in cache
  else
  {
    stat();
    // If it was a link and had been "stat'd" before then restat.
    if (getLinkModTime() > 0)
      lstat();
  }
}

inline const SmartCaseString &CachedFile::getUnixPath() const
{
#ifdef DEFAULT_SHELL_IS_CMD
  return (unixized);
#else
  return (*this);
#endif
}

inline CachedFile &CachedFile::operator=( const SmartCaseString &file )
{
  File::operator=( file );
  createUnixizedPath();
  return (*this);
}

inline CachedFile &CachedFile::operator=( const File &file )
{
  File::operator=( file );
  createUnixizedPath();
  return (*this);
}

inline CachedFile &CachedFile::operator=( const CachedFile &file )
{
  File::operator=( file );
#ifdef DEFAULT_SHELL_IS_CMD
  unixized = file.unixized;
#endif
  return (*this);
}

#endif /* _ODE_LIB_UTIL_CACHFILE_HPP_ */
