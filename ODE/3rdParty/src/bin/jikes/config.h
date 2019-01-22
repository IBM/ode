#if defined(AIX) || defined(MVSOE) || defined(SCO)
#define HAVE_STRINGS_H 1
#else
#define HAVE_STRING_H 1
#endif


#if defined(SCO)
#undef _XOPEN_SOURCE
#undef _XOPEN_SOURCE_EXTENDED
#endif


#if defined(WIN32)
#define HAVE_WINDOWS_H 1
#define WIN32_FILE_SYSTEM 1
#endif


#if defined(OS2)
#define HAVE_OS2_H 1
#define OS2_FILE_SYSTEM 1
#endif


#if !defined(BEOS) && !defined(FREEBSD) && !defined(NETBSD) && \
    !defined(OPENBSD)
#define HAVE_WCHAR_H 1
#define HAVE_WCSCAT 1
#define HAVE_WCSCMP 1
#define HAVE_WCSCPY 1
#define HAVE_WCSLEN 1
#define HAVE_WCSNCMP 1
#define HAVE_WCSNCPY 1
#endif


#if defined(HPUX) || defined(WIN32) || defined(MVSOE) || defined(TRU64) || \
    defined(IRIX) || defined(SCO) || defined(LINUX_IA64) || \
    defined(LINUX_ZSERIES) || defined(LINUX_PPC) || defined(LINUX_S390)
#define HAVE_ERROR_CALL_ICONV_CONST 1
#endif


#if defined(SOLARIS) || defined(HPUX) || defined(LINUX) || defined(TRU64) || \
    defined(IRIX) || defined(_MSC_VER) || defined(SCO) || defined(BEOS) || \
    defined(DYNIXPTX) || defined(FREEBSD) || defined(NETBSD) || \
    defined(OPENBSD) || defined(AIX_IA64)
#define HAVE_BOOL 1
#endif


#if defined(MVSOE) || defined(OS400)
/* define EBCDIC when conversion functions exist */
/* #define EBCDIC 1 */
#endif


#if !defined(MVSOE) && !defined(_MSC_VER)
#define HAVE_UNSIGNED_LONG_LONG 1
#endif


#if !defined(_MSC_VER) && !defined(BEOS) && !defined(FREEBSD) && \
    !defined(NETBSD) && !defined(OPENBSD)
#define HAVE_ICONV_H 1
#define HAVE_LIBICONV 1
#define HAVE_LIBICU_UC 1
#endif


#if defined(_MSC_VER)
#define HAVE_VCPP_SET_NEW_HANDLER 1
#endif


/**
 * all Intel and Alpha machines are little endian
 * the rest define WORDS_BIGENDIAN here...
**/
#if defined(MVSOE) || defined(HPUX) ||  defined(OS400) || defined(IRIX)
#define WORDS_BIGENDIAN 1
#elif defined(AIX) && !defined(AIX_IA64)
#define WORDS_BIGENDIAN 1
#elif defined(SOLARIS) && !defined(SOLARIS_X86)
#define WORDS_BIGENDIAN 1
#elif defined(LINUX) && !defined(LINUX_X86) && !defined(LINUX_ALPHA) && \
                        !defined(LINUX_IA64)
#define WORDS_BIGENDIAN 1
#endif


#ifdef UNIX

#define HAVE_DIRENT_H 1
#define HAVE_GLIBC_MKDIR 1
#define PATH_SEPARATOR ':'
#define UNIX_FILE_SYSTEM 1
/* #define HAVE_LIBC5_MKDIR 1 */

#else /* Windows and OS/2 */

#define HAVE_DIRECT_H 1
#define HAVE_PATHNAME_STYLE_DOS 1
#define PATH_SEPARATOR ';'
#define HAVE_WIN32_MKDIR 1
/* #define HAVE_ICC_FP_BUGS 1 */
/* #define HAVE_SYS_CYGWIN_H 1 */

#endif


#define HAVE_32BIT_TYPES 1
#define HAVE_ASSERT_H 1
#define HAVE_CTYPE_H 1
#define HAVE_FLOAT_H 1
#define HAVE_IOSTREAM_H 1
#define HAVE_LIBM 1
#define HAVE_LIMITS_H 1
#define HAVE_MATH_H 1
#define HAVE_MEMORY_H 1
#define HAVE_MKDIR 1
#define HAVE_NEW_H 1
#define HAVE_STDIO_H 1
#define HAVE_TIME_H 1
#define STDC_HEADERS 1

/* #define HAVE_STD 1 */
/* #define HAVE_MAC_MKDIR 1 */
/* #define HAVE_NAMESPACES 1 */

#define HAVE_WINT_T 1

#define JIKES_STAT_S_IFDIR S_IFDIR
#define JIKES_VERSION_STRING "Version 1.12o 12/04/2000"
#define PACKAGE "jikes"
#define VERSION "1.12o"
