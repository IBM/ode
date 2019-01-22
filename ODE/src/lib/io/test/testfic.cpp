// Test program to exercise findFilesInChain() and getDirContentsCanonical().

#include <iostream.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/io/path.hpp"
#include "lib/exceptn/ioexcept.hpp"
#include <base/binbase.hpp>

void printUsage()
{
  cout << "Usage: testfic [-debug] pattern directory..." << endl;
  cout << "   For each directory, list the files that match the" << endl;
  cout << "   pattern, showing the files in canonical form." << endl;
  cout << "   The pattern matching is done after following symlinks" << endl;
  cout << "   for the files.  Matching is only done on the file name" << endl;
  cout << "   and not on the path." << endl;
  cout << "   -debug causes extra debugging messages to be output." << endl;
}


/* ****************************
*
*  Main()
*
*
* *****************************
*/
int main( int argc, const char **argv, const char **envp )
{
  try
  {

    int i = 1;
    StringArray chain;
    StringArray output;

    if(argc < 3)
    {
      printUsage();
      return(1);
    }

    if (String( argv[1] ).equals( "-debug" ))
    {
      ++i;
      Interface::setState( "debug" );
    }
    if (i + 1 > argc) // not enough args after -debug
    {
      printUsage();
      return(1);
    }

    String pattern = argv[i++];
    for (; i < argc; i++)
      chain.add( argv[i] );
    Interface::print( "Searching the following:" );
    Interface::printArray( chain, " " );

    Interface::print( ">>>>>>>>>>>>" );

    Path::findFilesInChain( pattern, chain, &output );

    for (i = output.firstIndex(); i <= output.lastIndex(); i++)
      Interface::print( ">> " + output[i] );
  }
  catch(IOException &ex)
  {
    cout << "Caught IOException: " << ex.getMessage() << endl;
  }
  catch(FileNotFoundException &mesg)
  { 
    cout << "Caught FileNotFoundException: " << mesg.getMessage() <<endl; 
  }
  catch(...)
  {
   cout<<"Caught unknown error"<<endl;
  }

  return (0);
}
