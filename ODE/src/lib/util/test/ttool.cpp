#include <iostream.h>
#include <base/binbase.hpp>
#include <lib/portable/runcmd.hpp>
#include <lib/util/toolcnt.hpp>
#include <lib/string/env.hpp>
#include <lib/string/sboxcon.hpp>

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  if (argc != 2)
  {
    cerr << "Usage: ttool <toolname>" << endl;
    cerr << "       toolname should be an ODE tool, e.g. workon" << endl;
    exit( 0 );
  }
  Tool::init( envp );
  RunSystemCommand *toolcnt = ToolCounter::toolCountNoWait( argv[1] );
  toolcnt->waitFor();
  delete toolcnt;
  return 0;
}
