/** 
 * Tests the SetVarConfigFile class.
 *
 * Usage: tsvarcf <filename> [replace] [setenv] <var> <val> 
 *   Example: tsvarcf sb.conf replace build_env true
**/

#include <iostream.h>
#include <stdlib.h>
#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/portable/env.hpp"
#include "lib/io/setvarcf.hpp"


void printSCFInfo( SetVarConfigFile &scf )
{
  cout << endl << "CONTENTS:" << endl;
  StringArray slist;
  SetVars local_vars, global_vars;
  scf.getLocalVars( local_vars );
  scf.getGlobalVars( global_vars );
  local_vars.get( false, &slist );
  cout << "LOCALS:" << endl;
  for (int i=slist.firstIndex(); i <= slist.lastIndex(); ++i)
  {
    cout << "  " << slist[i] << endl;
  }
  slist.clear();
  global_vars.get( !PlatformConstants::onCaseSensitiveOS(), &slist );
  cout << endl << "GLOBALS:" << endl;
  for (int j=slist.firstIndex(); j <= slist.lastIndex(); ++j)
  {
    cout << "  " << slist[j] << endl;
  }
}

void doSCFAdd( SetVarConfigFile &scf, const String &var, const String &val,
    boolean is_env = false, boolean replace = false )
{
  SetVarConfigFileData data( val, is_env, replace );
  cout << "Add rc = " << scf.change( var, data ) << endl;
  printSCFInfo( scf );
}

void showUsage()
{
  cout << "Usage: tsvarcf <filename> [replace] [setenv] <var> <val>" << endl; 
  cout << "  Example: tsvarcf sb.conf build_env true" << endl;
  exit( -1 );
}

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  SetVars vars( new Env(), true, true );
  if (argc < 4 || argc > 5)
    showUsage();

  SetVarConfigFile scf( argv[1], vars );
  printSCFInfo( scf );
  switch (argc)
  {
    case 4:
      doSCFAdd( scf, argv[2], argv[3] );
      break;
    case 5:
      if (strcmp( argv[2], "replace" ) == 0)
        doSCFAdd( scf, argv[3], argv[4], false, true );
      else if (strcmp( argv[2], "setenv" ) == 0)
        doSCFAdd( scf, argv[3], argv[4], true );
      else
        showUsage();
      break;
    case 6:
      if ((strcmp( argv[2], "replace" ) == 0 &&
          strcmp( argv[3], "setenv" ) == 0) ||
          (strcmp( argv[3], "replace" ) == 0 &&
          strcmp( argv[2], "setenv" ) == 0))
        doSCFAdd( scf, argv[4], argv[5], true, true );
      else
        showUsage();
      break;
    default:
      showUsage();
      break;
  }
  return 0;
}

