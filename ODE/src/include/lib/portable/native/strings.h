#ifndef _ODE_LIB_PORTABLE_NATIVE_STRINGS_H_
#define _ODE_LIB_PORTABLE_NATIVE_STRINGS_H_

#ifdef __cplusplus
extern "C"
{
#endif

int ODEstrcasecmp( const char *str1, const char *str2 );
int ODEstrncasecmp( const char *str1, const char *str2, int n );

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* _ODE_LIB_PORTABLE_NATIVE_STRINGS_H_ */
