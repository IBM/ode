#ifndef _ODE_LIB_PORTABLE_NATIVE_SLEEP_H_
#define _ODE_LIB_PORTABLE_NATIVE_SLEEP_H_

#ifdef _ODE_LIB_PORTABLE_NATIVE_SLEEP_C_
#ifdef WIN32
#include <windows.h>
#else
#ifdef OS2
#define INCL_DOSPROCESS
#include <os2.h>
#endif /* OS2 */
#endif /* WIN32 */
#endif /* _ODE_LIB_PORTABLE_NATIVE_SLEEP_C_ */

#ifdef __cplusplus
extern "C"
{
#endif

  void ODEsleep( unsigned long msec );

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* _ODE_LIB_PORTABLE_NATIVE_SLEEP_H_ */
