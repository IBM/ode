//*********************************************************************
// Purpose:
//   This test programs test all methods in FileClass class.
// Usage:
//   Will prompt for input.
//*********************************************************************
#include <iostream.h>
#include <base/binbase.hpp>
#include "lib/util/filecach.hpp"
#include "lib/io/ui.hpp"

int main( int argc, char *argv[], const char **envp )
{
  Tool::init( envp );
  cout << "Testing FileCache class..." << endl;
  char filename[256];
  Interface::setState( "debug" );
  // FileCache::setCacheLevel( FileCache::CACHE_NOTHING );
  try
  {
    StringArray searchpath(3, 3);

    // Set up search path to be ". : .. : ../.. "
    searchpath[searchpath.firstIndex()]   = ".";
    searchpath[searchpath.firstIndex()+1] = "..";
    searchpath[searchpath.firstIndex()+2] = "../..";
    boolean done=false;
    while ( !done ) 
    {
      cout << endl << "Enter a file to test, or \"done\" :";
      cin >> filename;
      if (strcmp( filename, "done" ) == 0)
      {
        done = true;
        continue;
      }
      cout << "Getting " << filename << " from cache " << endl;
      CachedFile *cf = FileCache::get( filename, "", searchpath );
      if (cf != 0)
        cout << "*** Found " << cf->toString() << endl;
    }

    FileCache::printStats();
  }
  catch ( ... )
  {
    cerr << " Exception thrown" << endl;
  }
  return (0);
}
