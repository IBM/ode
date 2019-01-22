/**
 * File
 *
**/
#ifndef _ODE_LIB_IO_FILE_HPP_
#define _ODE_LIB_IO_FILE_HPP_

#include <base/odebase.hpp>
#include "lib/string/smartstr.hpp"
#include "lib/portable/native/file.h"
#include "lib/portable/platcon.hpp"

class File : public SmartCaseString
{
  public:

    File() :
        exists( false )
    {
      initStatStruct();
    }
    File( const File &cf ) :
        SmartCaseString( cf ), exists( cf.exists ),
        statstruct( cf.statstruct ), lmtime( cf.lmtime )
    {
    }
    File( const String &filename, boolean statnow );
    File( const String &dirname, const String &filename );
    File( const String &dirname, char *filename );

    File &operator=( const SmartCaseString &cf );
    inline File &operator=( const File &cf );

    inline long getModTime();
    inline long getLinkModTime();
    inline long getChangeTime();
    inline long getAccessTime();
    virtual boolean setModTime( long modtime = -1 ); // <0 == now
    inline boolean touch( const String &filename );
    inline boolean doesExist() const;
    inline boolean isDir();
    inline boolean isFile();
    inline boolean isLink();
    inline unsigned long getSize() const;
    inline void setDir();
    inline void stat();
    void lstat();


  private:

    boolean exists;
    struct ODEstat statstruct;
    time_t lmtime; // Link mod time
    inline void initStatStruct();
    inline void stat( char *pathname );
};


inline void File::initStatStruct()
{
  statstruct.is_file = statstruct.is_reg =
      statstruct.is_dir = statstruct.is_link = 0;
  statstruct.size = 0;
  statstruct.atime = statstruct.mtime = statstruct.ctime = 0;
  lmtime = 0;
}

inline File &File::operator=( const File &cf )
{
  this->SmartCaseString::operator=( cf );
  this->statstruct = cf.statstruct;
  this->exists     = cf.exists;
  this->lmtime     = cf.lmtime;
  return (*this);
}

inline void File::setDir()
{
  statstruct.is_dir = true;
}

inline long File::getModTime()
{
  if (statstruct.mtime == 0)
    stat();
  return (statstruct.mtime);
}

inline long File::getChangeTime()
{
  if (statstruct.ctime == 0)
    stat();
  return (statstruct.ctime);
}

inline long File::getAccessTime()
{
  if (statstruct.atime == 0)
    stat();
  return (statstruct.atime);
}

inline unsigned long File::getSize() const
{
  return (statstruct.size);
}

inline long File::getLinkModTime()
{
  if (lmtime == 0)
    lstat();
  return (lmtime);
}

inline boolean File::doesExist() const
{
  return (exists);
}

inline boolean File::isDir()
{
  if (!exists)
    stat();
  return (statstruct.is_dir);
}

inline boolean File::isFile()
{
  if (!exists)
    stat();
  return (statstruct.is_file);
}

inline boolean File::isLink()
{
#ifdef NO_SYMLINKS
  return (false);
#else
  if (!exists)
    stat();
  if (lmtime == 0)
    lstat();
  return (statstruct.is_link);
#endif /* UNIX */
}

inline void File::stat()
{
  stat( this->toCharPtr() );
}

inline void File::stat( char *pathname )
{
  exists = (ODEstat( pathname, &statstruct, OFFILE_ODEMODE, 0 ) == 0);
}

inline boolean File::touch( const String &filename )
{
  if (ODEtouch( filename.toCharPtr() ) == 0)
  {
    setModTime();
    return( true );
  }
  else
    return( false );
}
#endif /* _ODE_LIB_UTIL_CACHFILE_HPP_ */
