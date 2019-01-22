#include "lib/portable/native/regex.h"

/**
 * NOTE:
 * Some features of the POSIX/whatever regcomp() and regexec() are not used.
 * We do not anticipate caring about parenthesized subexpressions in the
 * regular expression, and what substrings they match.
**/

/**
 * The pattern is compiled into a ODEregex. If 0 is returned, the compile 
 * was successful; otherwise the returned int can be used by ODEregerror
 * to produce a printable error message.
 * noSubstring != 0 is useful if we only want to check for a match, but
 * do not care about substituting anything for the matched substring.
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
        )
{
  int cflags = 0;
  if (extended)
    cflags |=REG_EXTENDED;
  if (ignoreCase)
    cflags |=REG_ICASE;
  if (newline)
    cflags |=REG_NEWLINE;
  if (noSubstring)
    cflags |=REG_NOSUB;
  return regcomp( preg, pattern, cflags );
}


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
        )
{
  int eflags = 0;
  regmatch_t matchInfo;
  int retcode;

  if (notBeginLine)
    eflags |= REG_NOTBOL;
  if (notEndLine)
    eflags |= REG_NOTEOL;
  retcode = regexec( preg, string, 1, &matchInfo, eflags);
  if (retcode == 0)
  {
    if (startOffset)
      *startOffset = matchInfo.rm_so;
    if (endOffset)
      *endOffset = matchInfo.rm_eo;
  }
  return retcode;
}


/**
 * Frees any storage that ODEregcomp may have used to compile a pattern.
**/
void ODEregfree( ODEregex *preg )
{
  regfree( preg );
}

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
        )
{
  return regerror( errcode, preg, errorBuffer, errorBufferSize );
}
