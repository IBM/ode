//*********************************************************************
// Purpose:
//   This test programs test all methods in CachedFile class.
// Usage:
//   Will prompt for input.
//*********************************************************************
#include <iostream.h>
#include <base/binbase.hpp>
#include "lib/util/cachfile.hpp"

int main( int argc, char *argv[], const char **envp )
{
  Tool::init( envp );
  cout << "Testing CachedFile class..." << endl;
  char filename[256];
  cout << "Enter a file to test:";
  cin >> filename;
  CachedFile *cf = new CachedFile( filename, true );

  cout << filename << " mod time  = " << cf->getModTime() << endl;
  cout << filename << " lmod time = " << cf->getLinkModTime() << endl;
  cout << filename << " isDir     = " << cf->isDir() << endl;
  cout << filename << " doesExist = " << cf->doesExist() << endl;
  cout << filename << " isLink    = " << cf->isLink() << endl;

  delete cf;
  return (0);
}
