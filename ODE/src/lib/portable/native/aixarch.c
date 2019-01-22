#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>

#define _ODE_LIB_PORTABLE_NATIVE_ARCH_C_
#include "lib/portable/native/arch.h"

#define __AR_BIG__
#define __AR_SMALL__
#include "lib/portable/native/aix_ar.h" /* obtained from an AIX 4.3 machine */

#define ODE_AR_DATE_LEN 12

ODEArchInfo *ODEopenArch( char *name )
{
  ARCH_FILE_TYPE fp;
  ODEArchInfo *buf = 0;

  if ((fp = open( name, O_RDONLY )) >= 0)
  {
    buf = (ODEArchInfo *)malloc( sizeof( ODEArchInfo ) );
    buf->fp = fp;
    buf->filename = (char *)malloc( strlen( name ) + 1 );
    strcpy( buf->filename, name );
    buf->name = 0;
    buf->longnames = 0; /* not used */
    buf->date = 0;
    if (ODEreadArchHdr( buf, 1 ) != 0)
    {
      ODEcloseArch( buf );
      buf = 0;
    }
  }

  return (buf);
}

void ODEcloseArch( ODEArchInfo *buf )
{
  if (buf != 0)
  {
    if (buf->fp != 0)
      close( buf->fp );
    if (buf->filename != 0)
      free( buf->filename );
    if (buf->name != 0)
      free( buf->name );
    buf->fp = 0;
    buf->filename = 0;
    buf->name = 0;
    free( buf );
  }
}

int ODEreadArchHdr( ODEArchInfo *buf, int reset_relative_members )
{
  union
  {
    FL_HDR flhdr;
    FL_HDR_BIG flhdr_big;
  } hdr;
  char fl_magic[SAIAMAG];
  int fl_hsz;

  /* read magic string, determine if 32-bit or 64-bit (or invalid) */
  if (llseek( buf->fp, 0, SEEK_SET ) != 0)
    return -1;
  if (read( buf->fp, fl_magic, SAIAMAG ) != SAIAMAG)
    return -1;
  if (strncmp( fl_magic, AIAMAG, SAIAMAG ) == 0)
    buf->is64Bit = 0;
  else if (strncmp( fl_magic, AIAMAGBIG, SAIAMAG ) == 0)
    buf->is64Bit = 1;
  else /* magic string didn't match either of the valid values */
    return -1;
  fl_hsz = (buf->is64Bit) ? FL_HSZ_BIG : FL_HSZ;

  /* read entire header now that size is known and save the    */
  /* member offset info.  Note that we can treat the           */
  /* *addresses* of flhdr and flhdr_big as identical, but when */
  /* we access their members we have to know which one to use. */
  if (llseek( buf->fp, 0, SEEK_SET ) != 0)
    return -1;
  if (read( buf->fp, &hdr.flhdr, fl_hsz ) != fl_hsz)
    return -1;
  buf->first_member = strtoll(
      (buf->is64Bit) ? hdr.flhdr_big.fl_fstmoff : hdr.flhdr.fl_fstmoff, 0, 10 );
  buf->last_member = strtoll(
      (buf->is64Bit) ? hdr.flhdr_big.fl_lstmoff : hdr.flhdr.fl_lstmoff, 0, 10 );
  if (reset_relative_members)
  {
    buf->current_member = buf->first_member;
    buf->next_member = buf->first_member;
  }
  return 0;
}

int ODEreadArchFirst( ODEArchInfo *buf )
{
  buf->current_member = buf->first_member;
  return (ODEreadArchCurrent( buf )); /* resets next_member */
}

int ODEreadArchNext( ODEArchInfo *buf )
{
  buf->current_member = buf->next_member;
  return (ODEreadArchCurrent( buf )); /* resets next_member */
}

int ODEreadArchCurrent( ODEArchInfo *buf )
{
  union
  {
    AR_HDR arhdr;
    AR_HDR_BIG arhdr_big;
  } hdr;
  long len;
  int ar_hsz = (buf->is64Bit) ? AR_HSZ_BIG : AR_HSZ;

  if (buf->current_member < 0)
    return -1;
  if (llseek( buf->fp, buf->current_member, SEEK_SET ) != buf->current_member)
    return -1;
  if (read( buf->fp, &hdr.arhdr, ar_hsz ) != ar_hsz)
    return -1;
  if (buf->current_member == buf->last_member)
    buf->next_member = -1;
  else
    buf->next_member = strtoll(
        (buf->is64Bit) ? hdr.arhdr_big.ar_nxtmem : hdr.arhdr.ar_nxtmem, 0, 10 );
  buf->date = atol( (buf->is64Bit) ? hdr.arhdr_big.ar_date :
      hdr.arhdr.ar_date );
  len = atol( (buf->is64Bit) ? hdr.arhdr_big.ar_namlen : hdr.arhdr.ar_namlen );
  if (buf->name != 0)
    free( buf->name );
  buf->name = (char *)malloc( len + 1 );
  buf->name[0] = (buf->is64Bit) ? hdr.arhdr_big._ar_name.ar_name[0] :
      hdr.arhdr._ar_name.ar_name[0];
  buf->name[1] = (buf->is64Bit) ? hdr.arhdr_big._ar_name.ar_name[1] :
      hdr.arhdr._ar_name.ar_name[1];
  read( buf->fp, &(buf->name[2]), len - 2 );
  buf->name[len] = '\0';
  return 0;
}

int ODEsetMemberDate( ODEArchInfo *buf )
{
  int i;
  union
  {
    AR_HDR arhdr;
    AR_HDR_BIG arhdr_big;
  } hdr;
  int ar_hsz = (buf->is64Bit) ? AR_HSZ_BIG : AR_HSZ;

  if (buf->current_member < 0)
    return -1;
  if (buf->fp != 0 && close( buf->fp ) != 0)
    return -1;
  if ((buf->fp = open( buf->filename, O_RDWR | O_SYNC )) == 0)
  {
    buf->fp = open( buf->filename, O_RDONLY );
    return -1;
  }
  if (llseek( buf->fp, buf->current_member, SEEK_SET ) != buf->current_member)
    return -1;
  if (read( buf->fp, &hdr.arhdr, ar_hsz ) != ar_hsz)
    return -1;
  sprintf( (buf->is64Bit) ? hdr.arhdr_big.ar_date : hdr.arhdr.ar_date,
      "%ld", buf->date );
  /* make sure string is padded with spaces with no null terminator */
  for (i = strlen( (buf->is64Bit) ? hdr.arhdr_big.ar_date : hdr.arhdr.ar_date );
      i < ODE_AR_DATE_LEN; ++i)
  {
    if (buf->is64Bit)
      hdr.arhdr_big.ar_date[i] = ' ';
    else
      hdr.arhdr.ar_date[i] = ' ';
  }
  if (llseek( buf->fp, buf->current_member, SEEK_SET ) != buf->current_member)
    return -1;
  if (write( buf->fp, &hdr.arhdr, ar_hsz ) != ar_hsz)
    return -1;
  if (close( buf->fp ) != 0)
    return -1;
  if ((buf->fp = open( buf->filename, O_RDONLY )) == 0)
    return -1;
  return 0;
}
