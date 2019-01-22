/**
 * odeecho
 *
 * This program functions as an alternative for the built-in
 * shell command 'echo'.  It strips quotes (single and double)
 * and allows escaped characters (using the backslash).  The
 * backslash can be used to print quotes, tabs, newlines,
 * carriage returns, and the backslash itself.
 *
 * Example:
 *
 *   odeecho Hello\t"World"
 *
 * this prints:
 *
 * Hello       World
 *
**/

#include <stdio.h>
#include <stdlib.h>

#define FLAG_CHAR   '-'  /* flags are introduced with this character */
#define USAGE_FLAG  '?'  /* show usage */
#define ESCAPE_FLAG 'e'  /* don't escape backslashes */

#define USAGE_TEXT "Usage: odeecho [-e[char]] [-?] [text]"

struct
{
  int escape_backslashes;
  char escape_char;
} options;


char getEscapedChar( char ch )
{
  switch (ch)
  {
    case 't': /* tab */
      return ('\t');
    case 'n': /* newline */
      return ('\n');
    case 'r': /* carriage return */
      return ('\r');
    default: /* not special, just print normally */
      return (ch);
  }
}

void initOpts( void )
{
  options.escape_backslashes = 1;
  options.escape_char = '\\';
}

void processOpts( const char *ptr )
{
  while (*ptr != '\0')
  {
    switch (*ptr)
    {
      case ESCAPE_FLAG:
        if (*(ptr + 1) == '\0')
          options.escape_backslashes = 0;
        else
        {
          options.escape_backslashes = 1;
          options.escape_char = *(++ptr);
        }
        break;
      case USAGE_FLAG:
        puts( USAGE_TEXT );
        exit( 1 );
      default:
        putchar( *ptr );
        puts( ": invalid flag" );
        puts( USAGE_TEXT );
        exit( 1 );
    }
    ++ptr;
  }
}

int main( int argc, const char **argv )
{
  const char *ptr;
  int escaped = 0, i;

  initOpts();

  for (i = 1; i < argc; ++i)
  {
    ptr = *(argv + i);

    if (*ptr == FLAG_CHAR)
      processOpts( ptr + 1 );
    else
    {
      while (*ptr != '\0')
      {
        if (options.escape_backslashes &&
            *ptr == options.escape_char && !escaped)
          escaped = 1;
        else if ((*ptr == '\'' || *ptr == '\"') && !escaped)
          escaped = 0;
        else
        {
          if (escaped)
            putchar( getEscapedChar( *ptr ) );
          else
            putchar( *ptr );
          escaped = 0;
        }
        ++ptr;
      }
      putchar( ' ' );
    }
  }
  puts( "" );
  return (0);
}
