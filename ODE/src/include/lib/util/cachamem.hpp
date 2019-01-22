/**
 * CachedArchMember
**/
#ifndef _ODE_LIB_UTIL_CACHAMEM_HPP_
#define _ODE_LIB_UTIL_CACHAMEM_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/util/cachfile.hpp"

class CachedArchMember : public CachedFile
{
  public:

    CachedArchMember() :
        CachedFile(), modtime( 0 ), file_offset( 0 ), archname( "" ) {};
    CachedArchMember( const CachedArchMember &rhs ) :
        CachedFile( rhs ), modtime( rhs.modtime ),
        file_offset( rhs.file_offset ), archname( rhs.archname ) {};
    CachedArchMember( long modtime, const String &archname,
        const String &membname, long file_offset ) :
        CachedFile( membname, false ), modtime( modtime ),
        file_offset( file_offset ), archname( archname ) {};

    virtual long getModTime();
    virtual boolean setModTime( long modtime = -1 ); // <0 == now


  private:

    long modtime, file_offset;
    SmartCaseString archname;
};

#endif //_ODE_LIB_UTIL_CACHAMEM_HPP_
