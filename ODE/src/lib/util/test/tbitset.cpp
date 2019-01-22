#include <iostream.h>

#include <base/binbase.hpp>
#include "lib/util/bitset.hpp"

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  BitSet bs(65), bs2;

  bs.set( 1 );
  bs.set( 3 );
  bs.set( 5 );
  bs2.set( 2 );
  bs2.set( 4 );
  bs2.set( 5 );
  cout << (char*)bs << endl;
  cout << (char*)bs2 << endl;
  bs.Xor( bs2 );
  cout << (char*)bs << endl;
  cout << bs.equals( bs2 ) << endl;
  cout << bs.equals( bs ) << endl;
  cout << bs.get( 2 ) << endl;
  cout << bs.get( 0 ) << endl;
  return 0;
}
