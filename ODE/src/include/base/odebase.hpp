/**
 * Miscellaneous ODE types
 *
**/
#ifndef _ODE_ODEBASE_HPP_
#define _ODE_ODEBASE_HPP_

// On some platforms we must define boolean as a macro,
// since it is predefined elsewhere.  For example, on Windows,
// because of <windows.h>, we can't use a typedef for boolean.
#ifdef BOOLEAN_AS_MACRO
#define boolean int
#else
typedef int boolean;
#endif

#define false 0
#define true  1

// should we enforce the "explicit" keyword so constructors
// can't be used improperly?
#ifdef ENFORCE_EXPLICIT_CTRS
#define EXPLICIT_CTR explicit
#else
#define EXPLICIT_CTR
#endif

#include <signal.h>

// constants for collections
// WARNING: if ARRAY_FIRST_INDEX changes, both
// ODEListBase::lastIndex() and Array::lastIndex()
// must also change (they currently assume this value is 1 so that
// they need not perform calculations, which would hamper performance
// needlessly).  Best advice is just to leave FIRST_INDEX alone.
#define ARRAY_FIRST_INDEX 1
#define ELEMENT_NOTFOUND 0

// exit values for program termination
#define ODE_EXIT_OK        0
#define ODE_EXIT_INTERRUPT 1

#ifdef __cplusplus
extern "C"
{
#endif

#if defined(SIGFUNC_SIGFUNC)
typedef _SigFunc SignalFunctionPtr;
#elif defined(SIGFUNC_VOIDFCN)
typedef __void_fcn SignalFunctionPtr;
#else /* void (*)(int) */
typedef void (*SignalFunctionPtr)(int);
#endif

#ifdef __cplusplus
}
#endif

#endif /* _ODE_ODEBASE_HPP_ */
