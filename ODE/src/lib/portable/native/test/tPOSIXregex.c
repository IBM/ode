#include <stdlib.h>
#include <stdio.h>
#include <regex.h>

/* 
   Quick and dirty C program to see if I can get at (POSIX or GNU) regex
   routines.
*/
#define FLAG_IX 1
#define PATTERN_IX 2
#define STRING_IX 3

void printUsage()
{
  printf( "Usage: tregex flags reg_expr string_to_scan\n" );
  printf( " compile flags: 0 (noop flag), e (extended), i (ignore case)\n" );
  printf( "                n (newline), s (set NO substring flag)\n" );
  printf( " execute flags: B (not begin line), E (not end line)\n" );
  printf( "WARNING! Some characters cause problems or disappear unless\n" );
  printf( "double quoted or escaped, depending upon the command processor,\n" );
  printf( "for example:  ^ | &\n" );
}

int main( int argc, char **argv, const char **envp )
{
  int i, j, retcode;
  int cflags = 0;
  int eflags = 0;
  size_t msgSize, msgSize2;
  regex_t compiledExpression;
  regex_t *preg = &compiledExpression;
  regmatch_t matchStruct;
  regmatch_t *pmatch = &matchStruct;
  int nmatch = 1; /* number of regmatch_t structs */
  char *errorMsg = 0;

  if (argc < STRING_IX + 1)
  {
    printf( "Not enought args!!!\n" );
    printUsage();
    exit(1);
  }

  /*
    check out the flags
  */
  for (j = 0; argv[FLAG_IX][j] != 0; ++j)
  {
    switch (argv[FLAG_IX][j])
    {
      case '0':
        break;
      case 'e':
        cflags |= REG_EXTENDED;
        break;
      case 'i':
        cflags |= REG_ICASE;
        break;
      case 'n':
        cflags |= REG_NEWLINE;
        break;
      case 's':
        cflags |= REG_NOSUB;
        break;
      case 'B':
        eflags |= REG_NOTBOL;
        break;
      case 'E':
        eflags |= REG_NOTEOL;
        break;
      default:
        printf( "Illegal flag char '%c'!\n", argv[FLAG_IX][j] );
        printUsage();
        exit(2);
    }
  }

  /*
    Get and compile the regular expression
  */
  printf( "Regular expression = '%s'\n", argv[PATTERN_IX] );
  retcode = regcomp( preg, argv[PATTERN_IX], cflags );
  if (retcode != 0)
  {
    printf( "Compile error code %d\n", retcode );
    /* get the size of the error message */
    msgSize = regerror( retcode, preg, 0, 0);
    errorMsg = (char *)malloc( msgSize );
    if (errorMsg != 0)
    {
      msgSize2 = regerror( retcode, preg, errorMsg, msgSize);
      printf( "Error message '%s'\n", errorMsg ); 
      if (msgSize != msgSize2)
        printf( "!!! msgSize = %d msgSize2 = %d\n", msgSize, msgSize2);
      free( errorMsg );
      exit(3);
    }
    else
    {
      printf( "Could not get space for error regcomp message!\n" );
      exit(4);
    }
  }

  printf( "Number of parenthesized subexpressions = %d\n", preg->re_nsub );

  for (i = STRING_IX; i < argc; ++i)
    printf( "Target[%d] = '%s'\n", i, argv[i] );

  printf( "Output match is:\n" );

  /*
    Do one string for now.
  */
  retcode = regexec( preg,
                     argv[STRING_IX], /* the thingy to scan */
                     nmatch,  /* # of match structures */
                     pmatch,  /* pointer to match structures */
                     eflags);
  if (retcode != 0)
  {
    printf( "Execute error code %d\n", retcode );
    /* get the size of the error message */
    msgSize = regerror( retcode, preg, 0, 0);
    errorMsg = (char *)malloc( msgSize );
    if (errorMsg != 0)
    {
      msgSize2 = regerror( retcode, preg, errorMsg, msgSize);
      printf( "Error message '%s'\n", errorMsg ); 
      if (msgSize != msgSize2)
        printf( "!!! msgSize = %d msgSize2 = %d\n", msgSize, msgSize2);
      free( errorMsg );
      exit(5);
    }
    else
    {
      printf( "Could not get space for regexec error message!\n" );
      exit(6);
    }
  }

  printf( "Offset to start = %d; offset to after end = %d\n",
          pmatch->rm_so, pmatch->rm_eo );

  /*
    Now free whatever regcomp/regexec got.
  */
  regfree( preg );

}
