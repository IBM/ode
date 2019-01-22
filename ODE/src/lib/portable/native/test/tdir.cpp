/**
 * Test the functions in dir.c
 *
 * Usage: tdir < <<m|r|c> <directory>> | <g> >
 *   m = mkdir, r = rmdir, c = chdir, g = getcwd
 *
**/

#include <stdlib.h>
#include <iostream.h>

#include "lib/portable/native/dir.h"
#include <base/binbase.hpp>

int main( int argc, char **argv, const char **envp )
{
  Tool::init( envp );
  if (argc < 2 || argc > 3 ||
      (argc == 2 && *(argv[1]) != 'g'))
  {
    cout << "Usage: tdir < <<m|r|c> <directory>> | <g> >" << endl;
    cout << "  m = mkdir, r = rmdir, c = chdir, g = getcwd" << endl;
    exit( -1 );
  }

  switch (*(argv[1]))
  {
    case 'm':
      if (ODEmkdir( argv[2] ) == 0)
        cout << "Created directory successfully" << endl;
      else
        cout << "Couldn't create directory" << endl;
      break;
    case 'r':
      if (ODErmdir( argv[2] ) == 0)
        cout << "Removed directory successfully" << endl;
      else
        cout << "Couldn't remove directory" << endl;
      break;
    case 'c':
      ODEsetcwd( argv[2] );
      // fall through to see if directory did change
    case 'g':
    {
      char buf[512];
      ODEgetcwd( buf, 512 );
      cout << "Current directory is " << buf << endl;
      break;
    }
    default:
      cout << "Invalid command line" << endl;
  }
  return 0;
}
