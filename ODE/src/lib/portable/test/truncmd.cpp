/** 
 * Tests the RunSystemCommand class.
 *
 * Usage: truncmd <command> [args...]
**/

#include <iostream.h>
#include <base/binbase.hpp>
#include "lib/portable/runcmd.hpp"

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  if (argc < 2)
  {
    cout << "Usage: " << argv[0] << " <command> [args...]. " <<
        "example '" << argv[0] << " ls -l'" << endl;
    exit( -1 );
  }

  StringArray args( argv+1 );
  RunSystemCommand cmd( args, true, true, true );
  cmd.start();
  cmd.waitFor();
  cout << endl << "STDOUT:" << endl << cmd.getOutputText() << endl;
  cout << endl << "STDERR: (Not implemented)" << endl <<
      cmd.getErrorText() << endl;

  return 0;
}
