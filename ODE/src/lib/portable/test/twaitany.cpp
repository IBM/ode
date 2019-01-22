/** 
 * Tests the RunSystemCommand class.
 *
 * Usage: truncmd <command> [args...]
**/

#include <iostream.h>
#include <base/binbase.hpp>
#include "lib/portable/runcmd.hpp"

int main(int argc, const char **argv, const char **envp )
{
  Tool::init(envp);

  const char* s1[] = { "sleep", "6", 0 };
  const char* s2[] = { "sleep", "2", 0 };
  const char* s3[] = { "sleep", "5", 0 };

  StringArray args1(s1);
  StringArray args2(s2);
  StringArray args3(s3);

  //When using pipes, the behavior is different.
  //We avoid using pipes here.
  RunSystemCommand cmd1( args1 );
  RunSystemCommand cmd2( args2 );
  RunSystemCommand cmd3( args3 );

  cmd1.start();
  cmd2.start();
  cmd3.start();

  cerr << " Running  17 child processes:" << endl;
  cerr << " child1: (" << (int)cmd1.getChildPID() << ")" << endl;
  cerr << " child2: (" << (int)cmd2.getChildPID() << ")" << endl;
  cerr << " child3: (" << (int)cmd3.getChildPID() << ")" << endl;
  cerr << " Waiting for child processes to terminate" << endl;

  int count = 0;

  ODERET_CODE_TYPE exit_code = ODEDEF_ERROR_CODE;
  ODEPROC_ID_TYPE ret;

  while ((ret = RunSystemCommand::waitForAny( exit_code )) != INVALID_PROCESS)
  {
    count++;
    cerr << " Just returned from process: (" << (int)ret <<
        ")  exit_code = " << exit_code << endl;
    if (count == 3)
      break;
  }

  return 0;
}
