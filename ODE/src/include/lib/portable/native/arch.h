#ifndef _ODE_LIB_PORTABLE_NATIVE_ARCH_H_
#define _ODE_LIB_PORTABLE_NATIVE_ARCH_H_

#include <stdio.h> /* FILE */

#ifdef __cplusplus
extern "C"
{
#endif

#ifdef AIX
#include <sys/types.h>
typedef int ARCH_FILE_TYPE;
typedef offset_t ODE_ARCH_OFFSET_SIZE; /* for 64-bit AIX 4.3 */
#else
typedef FILE* ARCH_FILE_TYPE;
typedef long ODE_ARCH_OFFSET_SIZE;
#endif

typedef struct ODEArchInfo
{
  ARCH_FILE_TYPE fp;
  char *filename;
  char *name;
  char *longnames; /* block of newline-delimited long names */
  long longnames_size; /* size of block */
  long date;
#ifdef AIX
  int is64Bit; /* is this a BIG (64-bit) archive? */
#endif

  /**
   * The following are file offsets of various members.  Note that
   * last_member is only used on AIX.
  **/
  ODE_ARCH_OFFSET_SIZE current_member, next_member;
  ODE_ARCH_OFFSET_SIZE first_member, last_member;
} ODEArchInfo;

ODEArchInfo *ODEopenArch( char *name );
void ODEcloseArch( ODEArchInfo *buf );
int ODEreadArchHdr( ODEArchInfo *buf, int reset_relative_members );
int ODEreadArchFirst( ODEArchInfo *buf );
int ODEreadArchNext( ODEArchInfo *buf );
int ODEreadArchCurrent( ODEArchInfo *buf );
int ODEsetMemberDate( ODEArchInfo *buf );

#ifdef __cplusplus
}
#endif

#endif /* _ODE_LIB_PORTABLE_NATIVE_ARCH_H_ */
