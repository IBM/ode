/**
 * Test the functions in proc.c
 *
 * Usage: tproc <command> [args...]
 *
**/

#include <iostream.h>
#include <stdlib.h>

#include "lib/portable/native/proc.h"
#include <base/binbase.hpp>

int main( int argc, char **argv, char **envp )
{
  Tool::init( (const char **)envp );
  ODEPROC_ID_TYPE  pid;
  ODERET_CODE_TYPE  rc;

  if (argc < 2)
  {
    cout << "Usage: tproc <command> [args...]" << endl;
    exit( -1 );
  }
  cout << "My process ID is " << ODEgetpid() << endl;
  cout << "Forking child..." << endl;
  if ((pid = ODEfork( argv+1, envp )) < 0)
  {
    cout << "fork failed" << endl;
    exit( -1 );
  }
  cout << "Child is running." << endl;
  cout << "Child's process ID is " << (int)pid << endl;
  cout << "Waiting for child..." << endl;
  rc = ODEwait( pid );
  cout << "Child's return code was " << rc << endl;

  return 0;
}
