/** 
 * Tests the Sandbox class.
 *
 * Usage: tsandbox
 *   Example: tsandbox
**/

#include <iostream.h>
#include <stdlib.h>
#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/portable/env.hpp"
#include "lib/io/sandbox.hpp"
#include "lib/io/ui.hpp"
#include "lib/exceptn/sboxexc.hpp"


int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  Sandbox *sbp;

  Interface::setState( "verbose" );
  if (argc < 1 || argc > 6)
  {
    exit( -1 );
  }

  try
  {
    sbp = new Sandbox();
  }
  catch (SandboxException &e)
  {
    cerr << "Caught sandbox exception!" << endl << e.getMessage() << endl;
    exit( -1 );
  }
  Sandbox &sb = *sbp;
  cerr << sb.getSandboxName() << endl;
  cerr << sb.getSandboxBaseDir() << endl;
  cerr << sb.getSandboxBase() << endl;
  cerr << sb.getSandboxRCName() << endl;
  cerr << sb.getSandboxRCBase() << endl;
  cerr << sb.getBackingChain() << endl;
  cerr << sb.getMachineList() << endl;
  StringArray bchain, sblocals, bclocals, envs;
  sb.getBackingChainArray( &bchain );
  cerr << bchain << endl;
  sb.getSbconfLocals().get( false, &sblocals );
  cerr << sblocals << endl;
  sb.getBuildconfLocals().get( false, &bclocals );
  cerr << bclocals << endl;
  sb.getEnvs().get( false, &envs );
  cerr << envs << endl;
  cerr << endl <<
      "Machines: " << sb.getMachineList( sb.getSandboxBase() ) << endl;
  delete sbp;
  return 0;
}
