#include <sys/types.h>
#include <time.h>

using namespace std;
#define _ODE_LIB_UTIL_CACHAMEM_CPP_
#include "lib/io/path.hpp"
#include "lib/util/cachamem.hpp"
#include "lib/portable/native/arch.h"

long CachedArchMember::getModTime()
{
  return (modtime);
}

boolean CachedArchMember::setModTime( long modtime )
{
  boolean rc = false;
  ODEArchInfo *archptr;

  if (modtime < 0)
    modtime = time( 0 );
  if ((archptr = ODEopenArch( Path::canonicalize( archname,
      false ).toCharPtr() )) != 0)
  {
    archptr->current_member = file_offset;
    archptr->date = modtime;
    if (ODEsetMemberDate( archptr ) == 0)
    {
      this->modtime = modtime;
      rc = true;
    }
    ODEcloseArch( archptr );
  }

  return (rc);
}
