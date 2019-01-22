/**
 * Test the functions in file.c
 *
 * Usage: tfile < <<s|S|e|t> <file>> | <<l|i|m> <file1> <file2>> >
 *   s = stat, S = lstat, e = exists, t = touch
 *   l = symlink, i = clonetime, m = clonemode
 *     arguments should be in function call order
 *
**/

#include <stdlib.h>
#include <time.h>
#include <iostream.h>

#include "lib/portable/native/file.h"
#include <base/binbase.hpp>

int main( int argc, char **argv, const char **envp )
{
  Tool::init( envp );
  if (argc < 3 || argc > 4 ||
      (argc == 3 && *(argv[1]) != 's' && *(argv[1]) != 'S' &&
      *(argv[1]) != 'e' && *(argv[1]) != 't'))
  {
    cout << "Usage: tfile < <<s|S|e|t> <file>> |"
        "<<l|i|m> <file1> <file2>> >" << endl;
    cout << "  s = stat, S = lstat, e = exists, t = touch" << endl;
    cout << "  l = symlink, i = clonetime, m = clonemode" << endl;
    cout << "    arguments should be in function call order" << endl;
    exit( -1 );
  }

  switch (*(argv[1]))
  {
    case 's':
    {
      struct ODEstat buf;
      if (ODEstat( argv[2], &buf, OFFILE_ODEMODE, 1 ) == 0)
      {
        cout << "stat info:" << endl << endl;
        cout << "is_link: " << buf.is_link << endl;
        cout << "is_dir : " << buf.is_dir << endl;
        cout << "is_file: " << buf.is_file << endl;
        cout << "is_reg : " << buf.is_reg << endl;
        cout << "size   : " << buf.size << endl;
        cout << "atime  : " << ctime( &buf.atime );
        cout << "mtime  : " << ctime( &buf.mtime );
        cout << "ctime  : " << ctime( &buf.ctime );
        cout << "is_readable : " << buf.is_readable << endl;
        cout << "is_writable : " << buf.is_writable << endl;
      }
      else
        cout << "stat failed" << endl;
      break;
    }
    case 'S':
    {
      struct ODEstat buf;
      if (ODEstat( argv[2], &buf, OFLINK_ODEMODE, 1 ) == 0)
      {
        cout << "lstat info:" << endl << endl;
        cout << "is_link: " << buf.is_link << endl;
        cout << "is_dir : " << buf.is_dir << endl;
        cout << "is_file: " << buf.is_file << endl;
        cout << "is_reg : " << buf.is_reg << endl;
        cout << "size   : " << buf.size << endl;
        cout << "atime  : " << ctime( &buf.atime );
        cout << "mtime  : " << ctime( &buf.mtime );
        cout << "ctime  : " << ctime( &buf.ctime );
        cout << "is_readable : " << buf.is_readable << endl;
        cout << "is_writable : " << buf.is_writable << endl;
      }
      else
        cout << "lstat failed" << endl;
      break;
    }
    case 'e':
      cout << ((ODEstat( argv[2], 0, OFFILE_ODEMODE, 0 ) == 0) ?
          "File exists." : "File does not exist.") << endl;
      cout << ((ODEstat( argv[2], 0, OFLINK_ODEMODE, 0 ) == 0) ?
          "Link exists." : "Link does not exist.") << endl;
      break;
    case 't':
      if (ODEtouch( argv[2] ) == 0)
        cout << "Touched successfully" << endl;
      else
        cout << "Touch was unsuccessful" << endl;
      break;
    case 'l':
      if (ODEsymlink( argv[2], argv[3] ) == 0)
        cout << "Linked successfully" << endl;
      else
        cout << "Link was unsuccessful" << endl;
      break;
    case 'i':
      if (ODEclonetime( argv[2], argv[3] ) == 0)
        cout << "clonetime successfully" << endl;
      else
        cout << "clonetime was unsuccessful" << endl;
      break;
    case 'm':
      if (ODEclonemode( argv[2], argv[3] ) == 0)
        cout << "clonemode successfully" << endl;
      else
        cout << "clonemode was unsuccessful" << endl;
      break;
    default:
      cout << "Invalid command line" << endl;
  }
  return 0;
}
