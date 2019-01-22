/**
 * Test the ODE regex functions ODEregcomp, ODEregexec, ODEregfree, ODEregerror
**/
#include <iostream.h>

#include <stdio.h>

#include "lib/portable/native/regex.h"
#include <base/binbase.hpp>

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
  Tool::init( envp );
  int i, j, retcode;
  int ecflags = 0;
  int icflags = 0;
  int ncflags = 0;
  int scflags = 0;
  int Beflags = 0;
  int Eeflags = 0;
  int msgSize, msgSize2;
  ODEregex compiledExpression;
  ODEregex *preg = &compiledExpression;
  char *errorMsg = 0;
  unsigned long beginOffset = 0;
  unsigned long endOffset = 0;

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
        ecflags = 1;
        break;
      case 'i':
        icflags = 1; 
        break;
      case 'n':
        ncflags = 1; 
        break;
      case 's':
        scflags = 1; 
        break;
      case 'B':
        Beflags = 1; 
        break;
      case 'E':
        Eeflags = 1; 
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
  retcode = ODEregcomp( preg, argv[PATTERN_IX],
                        ecflags, icflags, ncflags, scflags );
  if (retcode != 0)
  {
    printf( "Compile error code %d\n", retcode );
    /* get the size of the error message */
    msgSize = ODEregerror( retcode, preg, 0, 0);
    errorMsg = (char *)malloc( msgSize );
    if (errorMsg != 0)
    {
      msgSize2 = ODEregerror( retcode, preg, errorMsg, msgSize);
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

  for (i = STRING_IX; i < argc; ++i)
    printf( "Target[%d] = '%s'\n", i, argv[i] );

  printf( "Output match is:\n" );

  /*
    Do one string for now.
  */
  retcode = ODEregexec( preg,
                        argv[STRING_IX], /* the thingy to scan */
                        Beflags,
                        Eeflags,
                        &beginOffset,
                        &endOffset
                        );
  if (retcode != 0)
  {
    printf( "Execute error code %d\n", retcode );
    /* get the size of the error message */
    msgSize = ODEregerror( retcode, preg, 0, 0);
    errorMsg = (char *)malloc( msgSize );
    if (errorMsg != 0)
    {
      msgSize2 = ODEregerror( retcode, preg, errorMsg, msgSize);
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
          beginOffset, endOffset );

  /*
    Now free whatever regcomp/regexec got.
  */
  ODEregfree( preg );

  /**
   * NOT tested yet: Where pointers to offset integers are 0.
   * NOT tested yet: Where error message space is smaller than message.
  **/

}
