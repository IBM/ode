/**
 * This program is linked dynamically with the C library, so it
 * should NOT crash whereas the one linked statically SHOULD crash.
 *
**/

#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/env.hpp"

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );

  // these strings are allocated on the heap...when linking
  // statically, the DLL and EXE have separate heaps...when
  // linking dynamically, the heap is shared by both.
  String var( "MYVAR" ), val( "MYVAL" ), argv0( argv[0] );

  // Env stores the strings in its own hashtable, which
  // are deallocated when the DLL unloads...if the C library
  // heap is not shared, BLAMMO!
  Env::setenv( var, val, true );

  // Tell the user what to expect
  if (argv0.indexOf( "tshlib" ) != STRING_NOTFOUND)
    Interface::print( "Upon exiting, this program should NOT crash." );
  else
    Interface::print( "Upon exiting, this program SHOULD crash." );

  return (0);
}
