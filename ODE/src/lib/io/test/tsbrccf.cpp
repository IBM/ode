/** 
 * Tests the SandboxRCConfigFile class.
 *
 * Usage: tsbrccf
 *   Example: tsbrccf
**/

#include <iostream.h>
#include <stdlib.h>
#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/portable/env.hpp"
#include "lib/io/sbrccf.hpp"


void printSCFInfo( SandboxRCConfigFile &scf )
{
  cout << "rc filename: " << scf.getPathname() << endl;
  cout << "default sb: " << scf.getDefaultSandbox() << endl;
  StringArray slist;
  scf.getSandboxList( &slist );
  cout << "Sandboxes:" << endl << slist << endl;
}

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  SandboxRCConfigFile scf;
  printSCFInfo( scf );
  return 0;
}

