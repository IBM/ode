/** 
 * Tests the SetsConfigFile class.
 *
 * Usage: tsetscf <directory> <a[rd]|d> <setname> [username] [setdir]
 *   a = add (r=replace, d=default), d = delete
 *   setdir is only needed for the 'a' command
 *   Example: tsetscf . ar myset crvich c:/temp
**/

#include <iostream.h>
#include <stdlib.h>
#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/env.hpp"
#include "lib/io/setscf.hpp"


void printSCFInfo( SetsConfigFile &scf )
{
  cout << endl << "CONTENTS:" << endl;
  StringArray slist, set_and_user;
  scf.getSets( &slist );
  for (int i=slist.firstIndex(); i <= slist.lastIndex(); ++i)
  {
    scf.separateUserNameFromSetName( slist[i], &set_and_user );
    cout << "  " << slist[i] << " " <<
      scf.getSetDir( set_and_user[set_and_user.firstIndex() + 1],
          set_and_user[set_and_user.firstIndex()] ) << endl;
    set_and_user.clear();
  }
  cout << "DEFAULT: " << scf.getDefaultSet() << " " <<
      scf.getDefaultSetDir() << endl << endl;
}

void doSCFAdd( SetsConfigFile &scf, const String &setname,
    const String &setdir, const String &username = "",
    boolean replace = true, boolean is_default = false )
{
  if (username == "")
  {
    cout << "Add rc = " << scf.add( setname, setdir, replace ) <<
        endl;
    if (is_default)
      scf.changeDefaultSet( setname );
  }
  else
  {
    cout << "Add rc = " << scf.add( setname, setdir, username, replace ) <<
        endl;
    if (is_default)
      scf.changeDefaultSet( setname, username );
  }
  printSCFInfo( scf );
}

void doSCFDel( SetsConfigFile &scf, const String &setname,
    const String &username = "" )
{
  if (username == "")
    cout << "Del rc = " << scf.del( setname ) << endl;
  else
    cout << "Del rc = " << scf.del( setname, username ) << endl;
  printSCFInfo( scf );
}

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  if (argc < 4 || argc > 6)
  {
    cout << "Usage: tsetscf <directory> <a[rd]|d> <setname> [username]"
        " [setdir]" << endl;
    cout << "  a = add (r=replace, d=default), d = delete" << endl;
    cout << "  setdir is only needed for the 'a' command" << endl;
    cout << "  Example: tsetscf sets ar myset crvich c:/temp" << endl;
    exit( -1 );
  }

  SetsConfigFile scf( argv[1] );
  printSCFInfo( scf );
  if (argv[2][0] == 'a')
  {
    boolean replace = false, is_default = false;

    if (argv[2][1] == 'r' && argv[2][2] == 'd')
      replace = is_default = true;
    else if (argv[2][1] == 'r')
      replace = true;
    else if (argv[2][1] == 'd')
      is_default = true;
    if (argc == 6)
      doSCFAdd( scf, argv[3], argv[5], argv[4], replace, is_default );
    else
      doSCFAdd( scf, argv[3], argv[4], "", replace, is_default );
  }
  else if (argv[2][0] == 'd')
  {
    if (argc > 4)
      doSCFDel( scf, argv[3], argv[4] );
    else
      doSCFDel( scf, argv[3] );
  }
  return 0;
}

