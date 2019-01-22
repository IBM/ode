/**
 * StringArray
 *
 * Extends OCL's ISequence for the <String> template,
 * and adds some new functionality.  Namely the ability
 * to use [] and length() instead of elementAtPosition()
 * and numberOfElements().  Also added join() function.
 *
**/

#include <iostream.h>
#include <stdlib.h>
#include <string.h>

#define _ODE_LIB_STRING_STRARRAY_CPP_
#include "lib/string/strarray.hpp"
#include "lib/string/string.hpp"

#ifdef __cplusplus
extern "C"
{
#endif
int sortComparer( const void *elem1, const void *elem2 );
#ifdef __cplusplus
}
#endif


StringArray::StringArray( const char **array,
    unsigned long max_size ) :
    Array< String >( max_size )
{
  while (array && *array)
    this->append( String( *(array++) ) );
}

/**
 * A way to convert a StringArray to a char**.
 *
 * Use delete[] to deallocate the returned pointer.
 * DO NOT deallocate or alter the contents of the pointer.
**/
char **StringArray::toCharStarArray() const
{
  int ci = 0; // index for char pointer
  char **rc = new char*[this->length() + 1];
  for (int i = firstIndex(); i <= lastIndex(); ++i, ++ci)
    *(rc + ci) = elementAtPosition( i ).toCharPtr();
  *(rc + ci) = 0;
  return (rc);
}

/**
 * The join functions simply concatenate strings
 * of an array into a single string (in order).  An
 * optional character or string (join_with) may be
 * automatically placed in between each string.
 * Example:
 *
 * assume array = { "foo", "bar", "again" }
 * array.join( '-' )
 * produces the string "foo-bar-again"
 *
**/
String StringArray::join( const char *join_with ) const
{
  String rc;
  
  if (this->length() > 0)
  {
    rc = elementAtPosition( firstIndex() );
    for (int i = firstIndex() + 1; i <= lastIndex(); ++i)
    {
      rc += join_with;
      rc += elementAtPosition( i );
    }
  }
  return (rc);
}

#ifdef __cplusplus
extern "C"
{
#endif

int sortComparer( const void *elem1, const void *elem2 )
{
  return (strcmp( *(const char**)elem1, *(const char**)elem2 ));
}

char **sortCharStarArray( char **array, int elements )
{
  if (elements < 0)
  {
    char **tmp = array;
    elements = 0;
    while (*(tmp++))
      ++elements;
  }

  if (elements > 0) // make sure there's something to sort
    qsort( array, elements, sizeof( char* ), sortComparer );

  return (array);
}

#ifdef __cplusplus
}
#endif

/**
 * Print operator for StringArray.
 * Allows "cout << string_array;"
**/
ostream &operator<<( ostream &os, const StringArray &sa )
{
  if (sa.length() > 0)
  {
    os << "[" << sa[sa.firstIndex()];
    for (int i = sa.firstIndex() + 1; i <= sa.lastIndex(); ++i)
      os << ", " << sa[i];
    os << "]";
  }
  return (os);
}
