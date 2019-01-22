/** 
 * Tests the SbconfReader class.
 *
 * Usage: tsbrdr <sandboxdir> [b]
 *   b = read backward
 *   Example: tsbrdr c:/sandbox1 b
**/

#include <iostream.h>
#include <stdlib.h>
#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/portable/env.hpp"
#include "lib/io/sbcnfrdr.hpp"


void printSCFInfo( SbconfReader &scf )
{
  cout << endl << "BACKING CHAIN: " << scf.getBackingChain() << endl;
  cout << "BACKING CHAIN ARRAY:" << endl;
  const StringArray &clist=scf.getBackingChainArray();
  for (int i=clist.firstIndex(); i <= clist.lastIndex(); ++i)
    cout << "  " << clist[i] << endl;
  StringArray slist;
  scf.getLocalVars().get( false, &slist );
  cout << "LOCAL VARS:" << endl;
  for (int j=slist.firstIndex(); j <= slist.lastIndex(); ++j)
    cout << "  " << slist[j] << endl;
  slist.clear();
  scf.getGlobalVars().get( true, &slist );
  cout << "GLOBAL VARS:" << endl;
  for (int k=slist.firstIndex(); k <= slist.lastIndex(); ++k)
    cout << "  " << slist[k] << endl;
}

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  if (argc < 2 || argc > 3)
  {
    cout << "Usage: tsbrdr <sandboxdir> [b]" << endl;
    cout << "  b = read backward" << endl;
    cout << "  Example: tsbrdr c:/sandbox1 b" << endl;
    exit( -1 );
  }

  SbconfReader scf( argv[1], argc==3 );
  printSCFInfo( scf );
  return 0;
}

