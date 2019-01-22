#define _ODE_LIB_PORTABLE_NATIVE_ARCH_C_
#include "lib/portable/native/arch.h"

ODEArchInfo *ODEopenArch( char *name )
{
  return 0;
}

void ODEcloseArch( ODEArchInfo *buf )
{
}

int ODEreadArchHdr( ODEArchInfo *buf, int reset_relative_members )
{
  return -1;
}

int ODEreadArchFirst( ODEArchInfo *buf )
{
  return -1;
}

int ODEreadArchNext( ODEArchInfo *buf )
{
  return -1;
}

int ODEreadArchCurrent( ODEArchInfo *buf )
{
  return -1;
}

int ODEsetMemberDate( ODEArchInfo *buf )
{
  return -1;
}
