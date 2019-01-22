/*
 *************************************************************************
 *                                                                       *
 * Copyright 2000 Compaq Computer Corporation                            *
 *                                                                       *
 * COMPAQ Registered in U.S. Patent and Trademark Office.                *
 *                                                                       *
 *************************************************************************
 * IMPORTANT: Carefully read the License Terms below before              *
 * proceeding.  By use of these materials you agree to these terms.      *
 * If you do not agree to these terms, you may not use this software or  *
 * the accompanying documentation.                                       *
 *************************************************************************
 * LICENSE TERMS                                                         *
 * 1. GRANT                                                              *
 * Compaq Computer Corporation ("COMPAQ") grants you the right to use,   *
 * modify, and distribute the following source code (the "Software")     *
 * on any number of computers. You may use the Software as part of       *
 * creating a software program or product intended for commercial or     *
 * non-commercial distribution in machine-readable source code, binary,  *
 * or executable formats. You may distribute the Software as             *
 * machine-readable source code provided this license is not removed     *
 * from the Software and any modifications are conspicuously indicated.  *
 * 2. COPYRIGHT                                                          *
 * The Software is owned by COMPAQ and its suppliers and is protected by *
 * copyright laws and international treaties.  Your use of the Software  *
 * and associated documentation is subject to the applicable copyright   *
 * laws and the express rights and restrictions of these terms.          *
 * 3. RESTRICTIONS                                                       *
 * You may not remove any copyright, trademark, or other proprietary     *
 * notices from the Software or the associated  documentation.           *
 * You are responsible for compliance with all applicable export or      *
 * re-export control laws and regulations if you export the Software.    *
 * This license is governed by and is to be construed under the laws     *
 * of the State of Texas.                                                *
 *                                                                       *
 * DISCLAIMER OF WARRANTY AND LIABILITY                                  *
 * Compaq shall not be liable for technical or editorial errors or       *
 * omissions contained herein. The information contained herein is       *
 * subject to change without notice.                                     *
 *                                                                       *
 * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND.       *
 * THE ENTIRE RISK ARISING OUT OF THE USE OF THIS SOFTWARE REMAINS WITH  *
 * RECIPIENT.  IN NO EVENT SHALL COMPAQ BE LIABLE FOR ANY DIRECT,        *
 * CONSEQUENTIAL, INCIDENTAL, SPECIAL, PUNITIVE OR OTHER DAMAGES         *
 * WHATSOEVER (INCLUDING WITHOUT LIMITATION DAMAGES FOR LOSS OF BUSINESS *
 * PROFITS, BUSINESS INTERRUPTION, OR LOSS OF BUSINESS INFORMATION),     *
 * EVEN IF COMPAQ HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES    *
 * AND WHETHER IN AN ACTION OF CONTRACT OR TORT INCLUDING NEGLIGENCE.    *
 *                                                                       *
 * If you have any questions concerning this license, please contact:    *
 * Compaq Computer Corporation, Software Business Practices, ZKO1-2/D22, *
 * 110 Spit Brook Road, Nashua, NH. 03062-2698.                          *
 *                                                                       *
 *************************************************************************
 */

# ifndef __UTIME_LOADED
# define __UTIME_LOADED 1

#include <time.h>

/* begin vms_jackets.h */
/*
 *************************************************************************
 *                                                                       *
 * Copyright 2000 Compaq Computer Corporation                            *
 *                                                                       *
 * COMPAQ Registered in U.S. Patent and Trademark Office.                *
 *                                                                       *
 *************************************************************************
 * IMPORTANT: Carefully read the License Terms below before              *
 * proceeding.  By use of these materials you agree to these terms.      *
 * If you do not agree to these terms, you may not use this software or  *
 * the accompanying documentation.                                       *
 *************************************************************************
 * LICENSE TERMS                                                         *
 * 1. GRANT                                                              *
 * Compaq Computer Corporation ("COMPAQ") grants you the right to use,   *
 * modify, and distribute the following source code (the "Software")     *
 * on any number of computers. You may use the Software as part of       *
 * creating a software program or product intended for commercial or     *
 * non-commercial distribution in machine-readable source code, binary,  *
 * or executable formats. You may distribute the Software as             *
 * machine-readable source code provided this license is not removed     *
 * from the Software and any modifications are conspicuously indicated.  *
 * 2. COPYRIGHT                                                          *
 * The Software is owned by COMPAQ and its suppliers and is protected by *
 * copyright laws and international treaties.  Your use of the Software  *
 * and associated documentation is subject to the applicable copyright   *
 * laws and the express rights and restrictions of these terms.          *
 * 3. RESTRICTIONS                                                       *
 * You may not remove any copyright, trademark, or other proprietary     *
 * notices from the Software or the associated  documentation.           *
 * You are responsible for compliance with all applicable export or      *
 * re-export control laws and regulations if you export the Software.    *
 * This license is governed by and is to be construed under the laws     *
 * of the State of Texas.                                                *
 *                                                                       *
 * DISCLAIMER OF WARRANTY AND LIABILITY                                  *
 * Compaq shall not be liable for technical or editorial errors or       *
 * omissions contained herein. The information contained herein is       *
 * subject to change without notice.                                     *
 *                                                                       *
 * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND.       *
 * THE ENTIRE RISK ARISING OUT OF THE USE OF THIS SOFTWARE REMAINS WITH  *
 * RECIPIENT.  IN NO EVENT SHALL COMPAQ BE LIABLE FOR ANY DIRECT,        *
 * CONSEQUENTIAL, INCIDENTAL, SPECIAL, PUNITIVE OR OTHER DAMAGES         *
 * WHATSOEVER (INCLUDING WITHOUT LIMITATION DAMAGES FOR LOSS OF BUSINESS *
 * PROFITS, BUSINESS INTERRUPTION, OR LOSS OF BUSINESS INFORMATION),     *
 * EVEN IF COMPAQ HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES    *
 * AND WHETHER IN AN ACTION OF CONTRACT OR TORT INCLUDING NEGLIGENCE.    *
 *                                                                       *
 * If you have any questions concerning this license, please contact:    *
 * Compaq Computer Corporation, Software Business Practices, ZKO1-2/D22, *
 * 110 Spit Brook Road, Nashua, NH. 03062-2698.                          *
 *                                                                       *
 *************************************************************************
 */

#ifndef vms_jackets_once
#define vms_jackets_once

/*
** Major include file for VMS Jacket routines.
** This .h file should be included by all VMS Jacket routines.
*/

#ifndef VMS_PREFIX
#define VMS_PREFIX "VMS_"
#endif

/*
** Some code uses VMS and a name (for a variable, for example). The default
** definition of VMS is "1" and so that makes the compiler choke.
*/

#ifdef VMS
#undef VMS
#endif
#define VMS VMS

#ifdef vms
#undef vms
#endif
#define vms vms

/*
** If VMS_MODULE_NAME is defined, then we must force the module name. If 
** the module name is specified, then the module version is optional.
*/
#if defined(VMS_MODULE_NAME)
#if defined(VMS_MODULE_VER)
#pragma module VMS_MODULE_NAME VMS_MODULE_VER
#else
#pragma module VMS_MODULE_NAME VMS_MODULE_NAME
#endif
#endif

/*
** Some code defines NULL to be 0. On earlier versions of C++ this was
** a problem since NULL is a void pointer. So there, we need to define
** NULL before including stdio.h or we get a MACROREDEF.
*/
#ifdef __cplusplus
#if (__DECCXX_VER < 60000000)
#ifndef NULL
#define NULL ((void *) 0)
#endif
#endif
#endif

/*
** Define some macros to make the conditional C++ extern stuff easier to read.
*/
#ifdef __cplusplus
#define VMS_BEGIN_C_PLUS_PLUS extern "C" {
#define VMS_END_C_PLUS_PLUS }
#else
#define VMS_BEGIN_C_PLUS_PLUS
#define VMS_END_C_PLUS_PLUS
#endif

/*
** Make sure FD_SETSIZE is defined here before anything. This is needed
** to make sure the select() in poll_Jacket.c support large enough
** descriptors to poll against. 
*/
#ifndef FD_SETSIZE
#define FD_SETSIZE  1024
#endif

/*
** MAXHOSTNAMELEN seems to be defined on UNIX systems, so we
** better define it too.
*/
#ifndef MAXHOSTNAMELEN
#define MAXHOSTNAMELEN 64
#endif

/*
** This isn't in the standard but some code uses it. The code should probably
** use PATH_MAX (from limits.h) instead.
*/
#ifndef MAXPATHLEN
#define MAXPATHLEN  256
#endif /*MAXPATHLEN*/

/*
** VMS doesn't get the endian definitions by default.
*/

/* begin endian.h */
/*
 *************************************************************************
 *                                                                       *
 * Copyright 2000 Compaq Computer Corporation                            *
 *                                                                       *
 * COMPAQ Registered in U.S. Patent and Trademark Office.                *
 *                                                                       *
 *************************************************************************
 * IMPORTANT: Carefully read the License Terms below before              *
 * proceeding.  By use of these materials you agree to these terms.      *
 * If you do not agree to these terms, you may not use this software or  *
 * the accompanying documentation.                                       *
 *************************************************************************
 * LICENSE TERMS                                                         *
 * 1. GRANT                                                              *
 * Compaq Computer Corporation ("COMPAQ") grants you the right to use,   *
 * modify, and distribute the following source code (the "Software")     *
 * on any number of computers. You may use the Software as part of       *
 * creating a software program or product intended for commercial or     *
 * non-commercial distribution in machine-readable source code, binary,  *
 * or executable formats. You may distribute the Software as             *
 * machine-readable source code provided this license is not removed     *
 * from the Software and any modifications are conspicuously indicated.  *
 * 2. COPYRIGHT                                                          *
 * The Software is owned by COMPAQ and its suppliers and is protected by *
 * copyright laws and international treaties.  Your use of the Software  *
 * and associated documentation is subject to the applicable copyright   *
 * laws and the express rights and restrictions of these terms.          *
 * 3. RESTRICTIONS                                                       *
 * You may not remove any copyright, trademark, or other proprietary     *
 * notices from the Software or the associated  documentation.           *
 * You are responsible for compliance with all applicable export or      *
 * re-export control laws and regulations if you export the Software.    *
 * This license is governed by and is to be construed under the laws     *
 * of the State of Texas.                                                *
 *                                                                       *
 * DISCLAIMER OF WARRANTY AND LIABILITY                                  *
 * Compaq shall not be liable for technical or editorial errors or       *
 * omissions contained herein. The information contained herein is       *
 * subject to change without notice.                                     *
 *                                                                       *
 * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND.       *
 * THE ENTIRE RISK ARISING OUT OF THE USE OF THIS SOFTWARE REMAINS WITH  *
 * RECIPIENT.  IN NO EVENT SHALL COMPAQ BE LIABLE FOR ANY DIRECT,        *
 * CONSEQUENTIAL, INCIDENTAL, SPECIAL, PUNITIVE OR OTHER DAMAGES         *
 * WHATSOEVER (INCLUDING WITHOUT LIMITATION DAMAGES FOR LOSS OF BUSINESS *
 * PROFITS, BUSINESS INTERRUPTION, OR LOSS OF BUSINESS INFORMATION),     *
 * EVEN IF COMPAQ HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES    *
 * AND WHETHER IN AN ACTION OF CONTRACT OR TORT INCLUDING NEGLIGENCE.    *
 *                                                                       *
 * If you have any questions concerning this license, please contact:    *
 * Compaq Computer Corporation, Software Business Practices, ZKO1-2/D22, *
 * 110 Spit Brook Road, Nashua, NH. 03062-2698.                          *
 *                                                                       *
 *************************************************************************
 */

/*
** Added this to keep the compilation of dbm/src/h_page.c happy. Maybe
** there's other modules that need it too, I guess I'll never know!
*/
#define BYTE_ORDER LITTLE_ENDIAN
#define BIG_ENDIAN      4321
#define LITTLE_ENDIAN   1234
/* end endian.h */

/*
** If we are building /NAMES=AS_IS then we have to worry about fixing up
** some broken prototypes and things.
*/
#ifdef VMS_AS_IS

#define lib$get_current_invo_context LIB$GET_CURRENT_INVO_CONTEXT

#endif /* VMS_AS_IS */

/*
** General stuff used by the jackets and users of the jackets.
*/

#define fork vfork

#define SI_ARCHITECTURE 100 /* needed for the sysinfo routine */

#define USYNC_THREAD 1

#ifdef USE_SOME_STUFF
typedef struct siginfo {
                int si_signo;
                int si_errno;
                int si_code;
                int si_pid;
                int si_status;
        } siginfo_t;

/*
** Support for vms_lockf.
*/
#define F_LOCK  1
#define F_TLOCK 2
#define F_ULOCK 3
#define F_TEST  4

/*
** The following defines are all for fcntl. I thought these were going to be
** present in 7.2, but they're not (at least not on VAX). So for now, only
** skip for Alpha 7.2 or higher.
*/
#if (__CRTL_VER < 70200000) || !defined __ALPHA

extern int fcntl(int,int,...);

/* file segment locking types */
#define F_RDLCK 1       /* Read (shared) lock */
#define F_WRLCK 2       /* Write (exclusive) lock */
#define F_UNLCK 8       /* Remove lock(s) */

/* File descriptor flags used for fcntl() */
/* POSIX REQUIRED */
#define FD_CLOEXEC      1       /* Close this file during exec */

/* fcntl() requests */
#define  F_DUPFD        0       /* Duplicate fildes             */
#define  F_GETFD        1       /* Get fildes flags             */
#define  F_SETFD        2       /* Set fildes flags             */
#define  F_GETFL        3       /* Get file flags               */
#define  F_SETFL        4       /* Set file flags               */
#define  F_GETLK        7       /* Get file lock        POSIX REQUIRED  */
#define  F_SETLK        8       /* Set file lock        POSIX REQUIRED  */
#define  F_SETLKW       9       /* Set file lock and waitPOSIX REQUIRED */

#endif  /* (__CRTL_VER < 70200000)  || !defined __ALPHA */

#endif /* USE_SOME_STUFF */

/*===========================================================================*/
#ifdef THE_JACKETS
/*
** Definitions that are only for the jackets.
*/

/*
** Define a temporary string size variable.
*/
#define TEMP_STRING_SIZE 2048

/*
** Constants passed to GENERIC_VMS_Filename_parser to
** provide hint about what kind of string it is
** suppposed to map.
*/
#define GENERIC_K_STRING_TYPE_FILE      0
#define GENERIC_K_STRING_TYPE_DIRECTORY 1
#define GENERIC_K_STRING_TYPE_UNKNOWN   2

/*
** Maximum size we ever try to transfer in a write, writev, send or recv.
*/
#define GENERIC_K_MAXBUF 32768

#endif /* THE_JACKETS */
/*===========================================================================*/

/*===========================================================================*/
#ifdef THE_JACKETS

/*
** Protypes and static storage for the jacket routines themselves.
** At the moment we don't prototype all of them, just the ones which
** are called from other jackets.
*/
VMS_BEGIN_C_PLUS_PLUS
    char * GENERIC_VMS_Filename_parser(char *, char *, int, int *);
    int GENERIC_FILE_SHARE_INIT();
    int GENERIC_FILE_SHARE_REG(int file_handle, int mode );
    int GENERIC_FILE_SHARE_UNREG(int file_handle);
    int GENERIC_FILE_SHARE_CHECK(int file_handle);
    char * GENERIC_EXTERNAL_NAME (const char *unixPtr, char *optBuffer);
    char *GENERIC_GETCWD_JACKET (char *buf, unsigned int size);

    void generic_tidy_spec(char *to, const char *from);
    int generic_absone(const char *spec);
VMS_END_C_PLUS_PLUS

/*
** Static storage for the jacket routines.
*/

static int GENERIC_trace_first_time = 1;
static int GENERIC_trace_indicator  = 1;
static char GENERIC_trace_logical[]= VMS_PREFIX "TRACE_FILENAMES";


/*
** Common macro definitions for the jacket routines.
*/

#define GENERIC_TRACE(string1, string2, string3, string4)               \
  { char *GENERIC_trace_flag;                                           \
    extern int GENERIC_trace_first_time;                                \
    extern int GENERIC_trace_indicator;                                 \
    extern char GENERIC_trace_logical[];                                \
    if (GENERIC_trace_first_time)                                       \
    {                                                                   \
        GENERIC_trace_first_time = 0;                                   \
        /* Check once if logical exists */                              \
        if (!(GENERIC_trace_flag= getenv(GENERIC_trace_logical)))       \
            GENERIC_trace_indicator = 0;                                \
    }                                                                   \
    if (GENERIC_trace_indicator )                                       \
        printf("%s %s %s %s \n",string1 ? string1 : "",                 \
                             string2 ? string2 : "",                    \
                             string3 ? string3 : "",                    \
                             string4 ? string4 : "");                   \
  }


/*---------------------------------------------------------------------------*/
#else /* THE_JACKETS */
/*
** DEFINE's for all of our new/jacketted entry points.
*/
#ifdef USE_ALL_JACKETS
#define access      GENERIC_ACCESS_JACKET
#define chdir       GENERIC_CHDIR_JACKET
#define chmod       GENERIC_CHMOD_JACKET
#define chown       GENERIC_CHOWN_JACKET
#define closedir    GENERIC_CLOSEDIR_JACKET
#define closelog    GENERIC_CLOSELOG
#define creat       GENERIC_CREAT_JACKET
#ifndef __cplusplus
#define delete      GENERIC_DELETE_JACKET
#endif
#define dlclose     GENERIC_DLCLOSE
#define dlerror     GENERIC_DLERROR
#define dlopen      GENERIC_DLOPEN
#define dlsym       GENERIC_DLSYM
#define endpwent    GENERIC_ENDPWENT
#define fchmod      GENERIC_FCHMOD
#define fcntl       GENERIC_FCNTL
#define fopen       GENERIC_FOPEN_JACKET
#define fread       GENERIC_FREAD_JACKET
#define fstat       GENERIC_FSTAT_JACKET
#define getcwd      GENERIC_GETCWD_JACKET
#define getpwent    GENERIC_GETPWENT
#define getpwnam    GENERIC_GETPWNAM_JACKET
#define getpwuid    GENERIC_GETPWUID_JACKET
#define gmtime_r    GENERIC_GMTIME_R
#define localtime_r GENERIC_LOCALTIME_R
#define lockf       GENERIC_LOCKF
#define lstat       GENERIC_LSTAT
#define mkdir       GENERIC_MKDIR_JACKET
#define mmap        GENERIC_MMAP_JACKET
#define munmap      GENERIC_MUNMAP_JACKET
#define opendir     GENERIC_OPENDIR_JACKET
#define open        GENERIC_OPEN_JACKET
#define poll        GENERIC_POLL
#define readdir     GENERIC_READDIR_JACKET
#define readlink    GENERIC_READLINK
#define realpath    GENERIC_REALPATH
#define recv        GENERIC_RECV
#define remove      GENERIC_REMOVE_JACKET
#define rename      GENERIC_RENAME_JACKET
#define rewinddir   GENERIC_REWINDDIR_JACKET
#define rmdir       GENERIC_RMDIR_JACKET
#define seekdir     GENERIC_SEEKDIR_JACKET
#define send        GENERIC_SEND
#define setpwent    GENERIC_SETPWENT
#define setsid      GENERIC_SETSID
#define socketpair  GENERIC_SOCKETPAIR
#define stat        GENERIC_STAT_JACKET
#define statfs      GENERIC_STATFS
#define syslog      GENERIC_SYSLOG
#define telldir     GENERIC_TELLDIR_JACKET
#define tempnam     GENERIC_TEMPNAM_JACKET
#define unlink      GENERIC_UNLINK_JACKET
#define write       GENERIC_WRITE
#define writev      GENERIC_WRITEV

#define pthread_attr_getscope GENERIC_PTHREAD_ATTR_GETSCOPE
#define pthread_attr_setscope GENERIC_PTHREAD_ATTR_SETSCOPE
#endif

#define utime       GENERIC_UTIME

/*
** Prototypes for all of our new entry points.
** We don't prototype the jacketted routines because they get prototyped
** when the user includes the appropriate system header file.
*/

/*
** These includes are needed because the prototypes use things like
** off_t, size_t, struct tm, etc.
*/
#include <stat.h>
#include <time.h>
#include <types.h>
#include <uio.h>

VMS_BEGIN_C_PLUS_PLUS
    void GENERIC_CLOSELOG(void);
#if 0
/* These are now in DLFCN.H */
    void GENERIC_DLCLOSE(dl_struct *in_handle);
    char *GENERIC_DLERROR();
    dl_struct *GENERIC_DLOPEN(char *fn,int mode);
    void *GENERIC_DLSYM(dl_struct *in_handle,char *name);
#endif
    void GENERIC_ENDPWENT (void);
    char * GENERIC_EXTERNAL_NAME (const char *unixPtr, char *optBuffer);
    char * GENERIC_EXTERNAL_DIR_NAME (const char *unixPtr, char *optBuffer);
    int GENERIC_FCHMOD(int fildes, mode_t mode);
    int GENERIC_FILE_SHARE_CHECK(int file_handle);
    int GENERIC_FILE_SHARE_INIT();
    int GENERIC_FILE_SHARE_REG(int file_handle, int mode );
    int GENERIC_FILE_SHARE_UNREG(int file_handle);
    struct passwd *GENERIC_GETPWENT (void);
    struct tm *GENERIC_GMTIME_R (const time_t *timer, struct tm *result);
    int GENERIC_IS_IMAGE (char *file_name);
    int GENERIC_IS_VALID_VMS_FILE_SPEC(char *file_name);
    struct tm *GENERIC_LOCALTIME_R (const time_t *timer, struct tm *result);
    int GENERIC_LOCKF(int fildes, int function, off_t size);
    int GENERIC_LSTAT(const char *path, struct stat *buf);
    int GENERIC_MARK_SHARE_WRITE(int file_handle);
#if 0
/* This is a special, rarely used, routine. We don't proto it here */
/* otherwise we need to include stat.h */
    void *GENERIC_MMAP_JACKET8 (void *addr, size_t len, int prot, int flags,
                       int filedes, off_t off, stat_t *finfo, int gflags);
#endif
    void *GENERIC_MMAP_JACKET (void *addr, size_t len, int prot, int flags,
                        int filedes, off_t off);
    int  GENERIC_MUNMAP_JACKET( void *addr, size_t len );
    int GENERIC_NOECHO(long chan, int flag);
    int GENERIC_OPEN_SHARED_JACKET(const char *file_spec, int flags, mode_t mode);
    int GENERIC_OPEN_JAVA_JACKET(const char *file_spec, int flags, mode_t mode);
    /* poll is not included here since its in poll.h
    int GENERIC_POLL(struct pollfd pfds[], nfds_t npfds, int timeout); */
    int GENERIC_READLINK(const char *path, char *buf, size_t bufsize);
    int GENERIC_REMOVE_IMAGE_PRIVS ();
    int GENERIC_SET_PRIVS (unsigned long m1, unsigned long m2);
    int GENERIC_RESTORE_PRIVS ();
    char *GENERIC_REALPATH(const char *filename, char *resolved_name);
    void GENERIC_SETPWENT (void);
    pid_t setsid(void);
    int GENERIC_SOCKETPAIR ( int fam, int type, int proto, int sd[2] );
    void GENERIC_SYSLOG (int priority, char *msgp, ...);
    /* utime is not included here since its in utime.h
    int GENERIC_UTIME (const char *file_spec, const struct utimbuf *times); */
    int GENERIC_WRITEV(int fd, const struct iovec *iov, int count);

#if 0
/* These are prototyped in pthread.h and so we don't need to do so here */
    int GENERIC_PTHREAD_ATTR_SETSCOPE (pthread_attr_t *attr, int scope);
    int GENERIC_PTHREAD_ATTR_GETSCOPE (pthread_attr_t *attr, int *scope);
#endif /* 0 */
VMS_END_C_PLUS_PLUS

#endif /* THE_JACKETS */
/*===========================================================================*/

#endif  /* vms_jackets_once */
/* Add nothing beyond this point in file. */
/* end vms_jackets.h */

# if __DECC_VER<50000000 && __DECCXX_VER<50000000   /* PROLOGUE version X-7 */
#   error POSIX for OpenVMS V3.0 requires DEC C or DEC C++ V5.0 or later
# endif
# if __64BITS_A || __64BITS_B
#   error The /INTS compile time option is not supported
# endif
# pragma __environment __save
# pragma __environment __header_defaults
# pragma __extern_model __strict_refdef
# if __cplusplus
extern "C" {
# endif
# ifndef __CHAR_SP
#   define __CHAR_SP 1
#   ifdef __INITIAL_POINTER_SIZE
#   pragma __required_pointer_size __long
#   endif
    typedef char *	__char_lp;	/* 64-bit pointer */
    typedef void *	__void_lp;	/* 64-bit pointer */
    typedef int *	__int_lp;	/* 64-bit pointer */
    typedef const char *__kchar_lp;	/* 64-bit pointer */
    typedef const void *__kvoid_lp;	/* 64-bit pointer */
    typedef const int  *__kint_lp;	/* 64-bit pointer */
#   ifdef __INITIAL_POINTER_SIZE
#   pragma __required_pointer_size __short
#   endif
    typedef char *	__char_sp;	/* 32-bit pointer */
    typedef void *	__void_sp;	/* 32-bit pointer */
    typedef int *	__int_sp;	/* 32-bit pointer */
    typedef const char *__kchar_sp;	/* 32-bit pointer */
    typedef const void *__kvoid_sp;	/* 32-bit pointer */
    typedef const int  *__kint_sp;	/* 64-bit pointer */
# endif

/* I don't think we need this any more */
#if 0
# ifndef __TIME_T					    /* version X-3  */
#   define __TIME_T 1
/* Changed following definition of time_t from int to unsigned long int. */
/*  typedef int			time_t; */
    typedef unsigned long int	time_t;
# endif
#endif /* 0 */

struct utimbuf
{
    time_t	actime;
    time_t	modtime;
} ;

# if __INITIAL_POINTER_SIZE > 0
#   pragma __pointer_size __long
# endif

int utime (const char * __path, const struct utimbuf * __times);

# if __cplusplus				    /* EPILOGUE version X-5 */
}
# endif
# pragma __environment __restore

# endif		/* __UTIME_LOADED  */

