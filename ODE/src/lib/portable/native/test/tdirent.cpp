/**
 * Test the opendir/readdir/closedir functions in dirent.c
 *
 * Usage: tdirent <directory>
 *
**/

#include <iostream.h>

#include <base/binbase.hpp>
#include "lib/portable/native/dirent.h"

int main( int argc, char **argv, const char **envp )
{
  Tool::init( envp );
  ODEDIR *dirp;
  ODEDIRENT dep;

  if (argc == 2)
  {
    dirp = ODEopendir( argv[1] );
    while (ODEreaddir( dirp, &dep ) == 0)
      cout << dep.d_name << endl;
    ODEclosedir( dirp );
  }
  else
    cout << "Usage: tdirent <directory>" << endl;
  return 0;
}
