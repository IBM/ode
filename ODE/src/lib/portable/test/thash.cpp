#include <iostream.h>

#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/portable/hashtabl.hpp"
#include "lib/portable/hashtabl.cxx"
#include "lib/portable/nilist.hpp"
#include "lib/portable/nilist.cxx"


int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  Hashtable< String, int > table( 2 );

  cout << "Empty? rc: " << table.isEmpty() << endl;
  cout << "Inserted one,1 rc: " << table.put( "one", 1 ) << endl;
  cout << "Inserted two,2 rc: " << table.put( "two", 2 ) << endl;
  cout << "Inserted three,3 rc: " << table.put( "three", 3 ) << endl;
  cout << "Getting value of three: " << *table.get( "three" ) << endl;
  cout << "Inserted three,5 rc: " << table.put( "three", 5 ) << endl;
  cout << "Size? rc: " << table.size() << endl;
  cout << "Getting value of two: " << *table.get( "two" ) << endl;
  cout << "Getting value of three: " << *table.get( "three" ) << endl;
  cout << "Contains element 1? rc: " << table.contains( 1 ) << endl;
  cout << "Contains element 3? rc: " << table.contains( 3 ) << endl;
  cout << "Contains key one? rc: " << table.containsKey( "one" ) << endl;
  cout << "Contains key four? rc: " << table.containsKey( "four" ) << endl;

  cout << "Key Enumeration:" << endl;
  HashKeyEnumeration< String, int > enume( &table );
  while (enume.hasMoreElements())
    cout << *enume.nextElement() << endl;

  cout << "Element Enumeration:" << endl;
  HashElementEnumeration< String, int > enumerate( &table );
  while (enumerate.hasMoreElements())
	  cout << *enumerate.nextElement() << endl;

  cout << "Removing key three rc: " << table.remove( "three" ) << endl;
  cout << "Size? rc: " << table.size() << endl;
  table.clear();

  return 0;
}
