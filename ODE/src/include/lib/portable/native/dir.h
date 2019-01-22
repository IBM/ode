#ifndef _ODE_LIB_PORTABLE_NATIVE_DIR_H_
#define _ODE_LIB_PORTABLE_NATIVE_DIR_H_

#ifdef __cplusplus
extern "C"
{
#endif

  char *ODEgetcwd( char *buf, int buflen );
  int ODEsetcwd( char *path );
  int ODEmkdir( char *path );
  int ODErmdir( char *path );

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* _ODE_LIB_PORTABLE_NATIVE_DIR_H_ */
