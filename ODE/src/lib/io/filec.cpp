using namespace std;
#define _ODE_LIB_IO_FILE_CPP_
#include "lib/io/file.hpp"
#include "lib/io/path.hpp"

File::File( const String &filename, boolean statnow ) :
    SmartCaseString( filename ), exists( false )
{
  Path::canonicalizeThis( *this, false );
  initStatStruct();
  if (statnow)
    stat();
}

File::File( const String &dirname, const String &filename ) :
    SmartCaseString( dirname ), exists( false )
{
  append( filename );
  initStatStruct();
  stat( filename.toCharPtr() ); // stat with just the filename
}

File::File( const String &dirname, char *filename ) :
    SmartCaseString( dirname ), exists( false )
{
  append( filename );
  initStatStruct();
  stat( filename ); // stat with just the filename
}

File &File::operator=( const SmartCaseString &cf )
{
  this->SmartCaseString::operator=( cf );
  Path::canonicalizeThis( *this, false );
  initStatStruct(); // in case the stat fails
  stat();
  return (*this);
}

void File::lstat()
{
#ifdef NO_SYMLINKS
  lmtime = statstruct.mtime;
#else
  struct ODEstat lstatstruct;
  if (ODEstat( this->toCharPtr(), &lstatstruct, OFLINK_ODEMODE, 0 ) != 0)
    return;

  statstruct.is_link = lstatstruct.is_link;
  lmtime = lstatstruct.mtime;
#endif
}

boolean File::setModTime( long modtime )
{
  this->statstruct.mtime = time( 0 );
  return (true);
}
