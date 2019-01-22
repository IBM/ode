#ifndef _ODE_LIB_PORTABLE_NATIVE_REGEX_H_
#define _ODE_LIB_PORTABLE_NATIVE_REGEX_H_

#include <sys/types.h>

#if defined(WIN32) || defined(VMS)
#include <stddef.h>
#include "lib/portable/native/gregex.h"
#else
#include <regex.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#define ODE_REGEX_NOMATCH  REG_NOMATCH  /*  1 RE pattern not found         */
#define ODE_REGEX_BADPAT   REG_BADPAT   /*  2 Invalid Regular Expression   */
#define ODE_REGEX_ECOLLATE REG_ECOLLATE /*  3 Invalid collating element    */
#define ODE_REGEX_ECTYPE   REG_ECTYPE   /*  4 Invalid character class      */
#define ODE_REGEX_EESCAPE  REG_EESCAPE  /*  5 Last character is \          */
#define ODE_REGEX_ESUBREG  REG_ESUBREG  /*  6 Invalid number in \digit     */
#define ODE_REGEX_EBRACK   REG_EBRACK   /*  7 [] imbalance                 */
#define ODE_REGEX_EPAREN   REG_EPAREN   /*  8 \( \) or () imbalance        */
#define ODE_REGEX_EBRACE   REG_EBRACE   /*  9 \{ \} or { } imbalance       */
#define ODE_REGEX_BADBR    REG_BADBR    /* 10 Invalid \{ \} range exp      */
#define ODE_REGEX_ERANGE   REG_ERANGE   /* 11 Invalid range exp endpoint   */
#define ODE_REGEX_ESPACE   REG_ESPACE   /* 12 Out of memory                */
#define ODE_REGEX_BADRPT   REG_BADRPT   /* 13 ?*+ not preceded by valid RE */
#define ODE_REGEX_ECHAR    REG_ECHAR    /* 14 invalid multibyte character  */
#define ODE_REGEX_EBOL     REG_EBOL     /* 15 ª anchor and not BOL         */
#define ODE_REGEX_EEOL     REG_EEOL     /* 16 $ anchor and not EOL         */

typedef regex_t ODEregex;

/**
 * The pattern is compiled into a ODEregex. If 0 is returned, the compile 
 * was successful; otherwise the returned int can be used by ODEregerror
 * to produce a printable error message.
**/
int ODEregcomp(
        ODEregex *preg,       /* where the compiled pattern is put */
        const char *pattern,  /* pattern to compile */
        int extended,         /* use extended syntax if non-0 */
        int ignoreCase,       /* ignore case if non-0 */
        int newline,          /* treat newlines as special if non-0 */
        int noSubstring       /* if non-0, compile so that no offsets to the */
                              /* beginning and end of a matching substring */
                              /* will be returned by ODEregex */
        );

/**
 * The compiled pattern is used to scan the string. If 0 is returned, then
 * a match was found in the string. If the return value is ODE_REGEX_NOMATCH
 * then no match was found. Any other returned value indicates an error.
 * A non-zero return code can be used by ODEregerror to produce a printable
 * error message.
**/
int ODEregexec(
        const ODEregex *preg, /* the compiled pattern to use */
        const char *string,   /* the characters to do a match in */
        int notBeginLine,     /* if non-0, the first character of string */
                              /* is not the beginning of the line */
        int notEndLine,       /* if non-0, the last character of the string */
                              /* is not the end of the line */
        unsigned long *startOffset, /* if startOffset is a non-0 pointer and */
                              /* if there is a match and if the preg was */
                              /* compiled with noSubstring == 0, then the */
                              /* byte offset to the matching substring is */
                              /* returned in the unsigned int. */
                              /* If the regular expression was compiled with */
                              /* nosubstitute != 0, the value is undefined. */
        unsigned long *endOffset /* if endOffset is a non-0 pointer and if */
                              /* there is a match and if the preg was */
                              /* compiled with noSubstring == 0, then the */
                              /* byte offset to the first character after */
                              /* the end of the matching substring is */
                              /* returned in the unsigned int. */
                              /* If the regular expression was compiled with */
                              /* noSubstring != 0, the value is undefined. */
        );

/**
 * Frees any storage that ODEregcomp may have used to compile a pattern.
**/
void ODEregfree( ODEregex *preg );

/**
 * The errcode is examined and the full size of the associated error message
 * is returned, including space for a null at the end. If errorBuffer is
 * not 0, the message is copied to the space pointed to. If the message,
 * including the null at the end, is longer than errorBufferSize then only
 * the first errorBufferSize - 1 characters are copied, and a null character
 * is copied to terminate the string. An easy way to request the message
 * size before allocating space for it, is to call ODEregerror first with
 * errorBufferSize == 0. The errorBuffer pointer is then ignored.
**/
int ODEregerror(
        int errcode,
        const ODEregex *preg,
        char *errorBuffer,
        int errorBufferSize
        );


#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* _ODE_LIB_PORTABLE_NATIVE_REGEX_H_ */
