/** 
 * Tests the BuildListConfigFile class.
 *
 * Usage: tblstcf <filename> <a[rd]|d> <build> [builddir]
 *   a = add (r=replace, d=default), d = delete
 *   builddir is only needed for the 'a' command
 *   Example: tblstcf build_list ar latest o:\olestra
**/

#include <iostream.h>
#include <stdlib.h>
#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/env.hpp"
#include "lib/io/bldlstcf.hpp"


void printBLCFInfo( BuildListConfigFile &blcf )
{
  cout << "CONTENTS:" << endl;
  StringArray blist;
  blcf.getBuildList( &blist );
  for (int i=blist.firstIndex(); i <= blist.lastIndex(); ++i)
    cout << blist[i] << " " << blcf.getBuildDir( blist[i] ) << endl;
}

void doBLCFAdd( BuildListConfigFile &blcf, const String &build,
    const String &builddir, boolean replace, boolean is_default )
{
  cout << "Add rc = " << blcf.add( build, builddir, replace, is_default ) <<
      endl;
  printBLCFInfo( blcf );
}

void doBLCFDel( BuildListConfigFile &blcf, const String &build )
{
  cout << "Del rc = " << blcf.del( build ) << endl;
  printBLCFInfo( blcf );
}

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  if (argc < 4 || argc > 5)
  {
    cout << "Usage: tblstcf <filename> <a[rd]|d> <build> [builddir]" << endl;
    cout << "  a = add (r=replace, d=default), d = delete" << endl;
    cout << "  builddir is only needed for the 'a' command" << endl;
    cout << "  Example: tblstcf build_list ar latest o:\\olestra" << endl;
    exit( -1 );
  }

  BuildListConfigFile blcf( argv[1] );
  printBLCFInfo( blcf );
  if (argv[2][0] == 'a')
  {
    boolean replace = false, is_default = false;

    if (argv[2][1] == 'r' && argv[2][2] == 'd')
      replace = is_default = true;
    else if (argv[2][1] == 'r')
      replace = true;
    else if (argv[2][1] == 'd')
      is_default = true;
    doBLCFAdd( blcf, argv[3], argv[4], replace, is_default );
  }
  else if (argv[2][0] == 'd')
  {
    doBLCFDel( blcf, argv[3] );
  }
  return 0;
}

