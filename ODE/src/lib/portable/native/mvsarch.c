#include <sys/types.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "lib/portable/native/mvs_ar.h"

#define _ODE_LIB_PORTABLE_NATIVE_ARCH_C_
#include "lib/portable/native/arch.h"

#define ODE_AR_NAME_LEN 16
#define ODE_AR_DATE_LEN 12
#define SYMBOL_TABLE_NAME "__.SYMDEF"
#define SYMBOL_TABLE_NAME_LEN 9
#define AR_EFMT_LEN 3

void cleanNames( char *names, int len );
void skipLinefeeds( ODEArchInfo *buf );

ODEArchInfo *ODEopenArch( char *name )
{
  FILE *fp;
  ODEArchInfo *buf = 0;

  if ((fp = fopen( name, "rb" )) != 0)
  {
    buf = (ODEArchInfo *)malloc( sizeof( ODEArchInfo ) );
    buf->fp = fp;
    buf->filename = (char *)malloc( strlen( name ) + 1 );
    strcpy( buf->filename, name );
    buf->name = 0;
    buf->longnames = 0;
    buf->longnames_size = 0;
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
      fclose( buf->fp );
    if (buf->filename != 0)
      free( buf->filename );
    if (buf->name != 0)
      free( buf->name );
    if (buf->longnames != 0)
      free( buf->longnames );
    buf->fp = 0;
    buf->filename = 0;
    buf->name = 0;
    buf->longnames = 0;
    free( buf );
  }
}

int ODEreadArchHdr( ODEArchInfo *buf, int reset_relative_members )
{
  char magic[SARMAG];

  fseek( buf->fp, 0, SEEK_SET );
  if (fread( magic, 1, SARMAG, buf->fp ) != SARMAG ||
      strncmp( magic, ARMAG, SARMAG ) != 0)
    return 1;
  buf->first_member = SARMAG;
  buf->last_member = -1; /* not used */
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
  struct ar_hdr arhdr;
  char *name_ptr;
  int done = 0; /* until we read an non-special member entry */
  int mem_size, name_size;

  if (buf->current_member < 0)
    return -1;
  while (!done) /* some members are special, and are silently skipped */
  {
    if (fseek( buf->fp, buf->current_member, SEEK_SET ) != 0)
      return -1;
    if (fread( &arhdr, sizeof( struct ar_hdr ), 1, buf->fp ) != 1)
      return -1;
    mem_size = atol( arhdr.ar_size );
    buf->next_member += sizeof( struct ar_hdr ) + mem_size;
    if (strncmp( arhdr.ar_name, SYMBOL_TABLE_NAME, SYMBOL_TABLE_NAME_LEN ) == 0)
    {
      skipLinefeeds( buf );
      buf->current_member = buf->next_member;
      continue; /* keep looking for a non-special member */
    }
    else if (strncmp( arhdr.ar_name, AR_EFMT1, AR_EFMT_LEN ) == 0)
    { /* long name */
      if (buf->longnames != 0)
        free( buf->longnames );
      name_size = atol( &(arhdr.ar_name[AR_EFMT_LEN]) );
      buf->longnames = (char *)malloc( name_size + 1 );
      buf->longnames_size = name_size;
      if (fread( buf->longnames, 1, name_size, buf->fp ) != name_size)
        return -1;
      buf->longnames[name_size] = '\0';
      name_ptr = buf->longnames;
    }
    else
    {
      cleanNames( arhdr.ar_name, ODE_AR_NAME_LEN ); /* remove trailing spaces */
      name_ptr = arhdr.ar_name;
      name_size = ODE_AR_NAME_LEN;
    }
    if (buf->name != 0)
      free( buf->name );
    buf->name = (char *)malloc( name_size + 1 );
    strncpy( buf->name, name_ptr, name_size );
    buf->name[name_size] = '\0';
    buf->date = atol( arhdr.ar_date );
    skipLinefeeds( buf );
    done = 1;
  }
  return 0;
}

int ODEsetMemberDate( ODEArchInfo *buf )
{
  int i;
  struct ar_hdr arhdr;

  if (buf->current_member < 0)
    return -1;
  if (buf->fp != 0 && fclose( buf->fp ) != 0)
    return -1;
  if ((buf->fp = fopen( buf->filename, "rb+" )) == 0)
  {
    buf->fp = fopen( buf->filename, "rb" );
    return -1;
  }
  if (fseek( buf->fp, buf->current_member, SEEK_SET ) != 0)
    return -1;
  if (fread( &arhdr, sizeof( struct ar_hdr ), 1, buf->fp ) != 1)
    return -1;
  sprintf( arhdr.ar_date, "%ld", buf->date );
  /* make sure string is padded with spaces with no null terminator */
  for (i = strlen( arhdr.ar_date ); i < ODE_AR_DATE_LEN; ++i)
    arhdr.ar_date[i] = ' ';
  if (fseek( buf->fp, buf->current_member, SEEK_SET ) != 0)
    return -1;
  if (fwrite( &arhdr, sizeof( struct ar_hdr ), 1, buf->fp ) != 1)
    return -1;
  if (fflush( buf->fp ) != 0)
    return -1;
  if (fclose( buf->fp ) != 0)
    return -1;
  if ((buf->fp = fopen( buf->filename, "rb" )) == 0)
    return -1;
  return 0;
}

void cleanNames( char *names, int len )
{
  int i, remove_spaces = 1;
  char *ptr;

  if (names == 0 || len <= 0)
    return;

  ptr = names + (len - 1); /* we parse the string backwards */

  for (i = 0; i < len; ++i, --ptr)
  {
    if (*ptr == '\n' || *ptr == '\0')
    {
      *ptr = '\0';
      remove_spaces = 1;
    }
    else if (*ptr == '/')
    {
      *ptr = '\0';
      remove_spaces = 1;
    }
    else if (*ptr == ' ' && remove_spaces)
      *ptr = '\0';
    else
      remove_spaces = 0;
  }
}

void skipLinefeeds( ODEArchInfo *buf )
{
  char ch;

  if (fseek( buf->fp, buf->next_member, SEEK_SET ) == 0)
  {
    do
    {
      if (fread( &ch, 1, 1, buf->fp ) != 1)
        break;
      else if (ch == '\n')
        ++(buf->next_member);
    } while (ch == '\n');
  }
}
