#ifndef _ODE_LIB_PORTABLE_NATIVE_FILE_H_
#define _ODE_LIB_PORTABLE_NATIVE_FILE_H_

#include <time.h>

#ifdef __cplusplus
extern "C"
{
#endif

  struct ODEstat
  {
    int is_file;
    int is_reg;
    int is_dir;
    int is_link;
    int is_readable;
    int is_writable;
    unsigned long size;
    time_t atime;
    time_t mtime;
    time_t ctime;
  };

  enum ODEstatmode
  {
    OFFILE_ODEMODE,  /* stat of the file (stat)  */
    OFLINK_ODEMODE   /* stat of the link (lstat) */
  };

  /* pass buf as NULL if you just want to check existence. */
  /* if getrwinfo is nonzero, ODEstat will retrieve is_readable and */
  /* is_writable information (slight performance hit). */
  int ODEstat( char *name, struct ODEstat *buf, enum ODEstatmode mode,
      int getrwinfo );

  int ODEclonetime( char *srcfile, char *dstfile );
  int ODEclonemode( char *srcfile, char *dstfile );
  int ODEtouch( char *name );
  int ODEsymlink( char *pathname, char *linkname );
  int ODEremove( const char *filename );

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* _ODE_LIB_PORTABLE_NATIVE_FILE_H_ */
