using namespace std;
#include <ssdef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main( int argc, char **argv )
{
  int cmdlen = 0, i;

  for (i = 1; i < argc; ++i)
    cmdlen += strlen( argv[i] ) + 1;
  if (cmdlen)
  {
    char *buf;
    ++cmdlen; // needed for ending null char
    buf = (char *)malloc( cmdlen * sizeof( char ) );
    if (buf)
    {
      *buf = '\0';
      for (i = 1; i < argc; ++i)
      {
        strcat( buf, argv[i] );
        strcat( buf, " " );
      }
      int rc = system( buf );
      return (rc);
    }
    fprintf( stderr, "out of memory\n" );
    return (SS$_ABORT);
  }
  return (SS$_NORMAL);
}
