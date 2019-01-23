#include <fstream>
using namespace std ;

#include "lib/intcmds/body.hpp"
#include "lib/io/ui.hpp"

/* **********************************************************************
 *
 * Constructor:
 *
 * Create a vector and add line to it.
 *
 * *********************************************************************/
Body::Body( const String& line )
{
  content.addElement( line );
}


/* **********************************************************************
 *
 * Destructor:
 *
 * 
 *
 * *********************************************************************/

Body::~Body( )
{

  content.removeAllElements();
}


/* ********************************************************************
 *
 * addElements:
 *
 *  Add the 'subline' to the vector.
 *
 * *******************************************************************/
void Body::addElement(const String& subline)
{
  content.addElement( subline );
}

/* ********************************************************************
 *
 * print:
 *
 * print the content of the vector.
 *
 * *******************************************************************/
void Body::print()
{
  for (int i=content.firstIndex(); i<=content.lastIndex(); i++)
    Interface::printAlways( *content.elementAt(i) );
}

/* ********************************************************************
 *
 * write:
 *
 * write self to a file
 *
 * *******************************************************************/
void Body::write(fstream* fileptr )
{
#ifdef FILENAME_BLANKS
  String breakString(":  ");
  int breakStringLen = breakString.length();
  for (int i=content.firstIndex(); i<=content.lastIndex(); i++)
  {
    int index = (content.elementAt(i))->indexOf( breakString );
    *fileptr << (content.elementAt(i))->substring( 1, index ).doubleQuote()
       << breakString
       << (content.elementAt(i))->substring( index + breakStringLen ).doubleQuote()
       << endl;
  }
#else
  for (int i=content.firstIndex(); i<=content.lastIndex(); i++)
    *fileptr << *content.elementAt(i) << endl; 
#endif

}

