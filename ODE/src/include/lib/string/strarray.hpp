/**
 * StringArray
 *
**/
#ifndef _ODE_LIB_STRING_STRARRAY_HPP_
#define _ODE_LIB_STRING_STRARRAY_HPP_

#define DEFAULT_STRARRAY_MAXSIZE 2
#define DEFAULT_STRARRAY_INITSIZE 0
#define DEFAULT_STRARRAY_JOINSTR ""

#include "lib/string/string.hpp"
#include "lib/portable/array.hpp"

#ifdef __cplusplus
extern "C"
{
#endif
// pass a negative value in "elements" if the number is unknown,
// and it will be calculated on-the-fly.
char **sortCharStarArray( char **array, int elements );
#ifdef __cplusplus
}
#endif

/**
 * A specialized array just for strings.  See
 * also the Array class for other member functions.
**/
class StringArray : public Array< String >
{
  public:

    // constructors
    // max_size - initial maximum capacity
    // init_size - number of empty strings inserted during initialization
    //             (so that direct assignment may be used afterward)
    StringArray( unsigned long max_size = DEFAULT_STRARRAY_MAXSIZE,
        unsigned long init_size = DEFAULT_STRARRAY_INITSIZE ) :
        Array< String >( max_size ) { extendTo( init_size ); }
    StringArray( const char **array,
        unsigned long max_size = DEFAULT_STRARRAY_MAXSIZE );
    StringArray( const StringArray &array ) : Array< String >( array ) {}

    // char ** conversion
    // ONLY use delete[] to deallocate the returned pointer.
    // DO NOT deallocate or alter the contents of the pointer.
    char **toCharStarArray() const;
    inline char **toSortedCharStarArray() const;

    // join functions
    inline String join( char join_with ) const;
    inline String join( const String &join_with ) const;
    String join( const char *join_with = DEFAULT_STRARRAY_JOINSTR ) const;
    inline StringArray &extendTo( unsigned int newsize );
    inline StringArray *extendToAsPtr( unsigned int newsize );
};

/**
 * Concatenates together all elements of the array, with
 * the join_with character in between each element.
**/
inline String StringArray::join( char join_with ) const
{
  char chstr[2] = { join_with, '\0' };
  return (join( chstr ));
}

/**
 * Concatenates together all elements of the array, with
 * the join_with string in between each element.
**/
inline String StringArray::join( const String &join_with ) const
{
  return (join( join_with.toCharPtr() ));
}

inline StringArray &StringArray::extendTo( unsigned int newsize )
{
  Array< String >::extendTo( newsize );
  return (*this);
}

inline StringArray *StringArray::extendToAsPtr( unsigned int newsize )
{
  Array< String >::extendTo( newsize );
  return (this);
}

/**
 * A way to convert a StringArray to a sorted char**.
 *
 * Use delete[] to deallocate the returned pointer.
 * DO NOT deallocate or alter the contents of the pointer.
**/
inline char **StringArray::toSortedCharStarArray() const
{
  return (::sortCharStarArray( toCharStarArray(), this->size() ));
}

// printing operator
ostream &operator<<( ostream &os, const StringArray &sa );

#endif /* _ODE_LIB_STRING_STRARRAY_HPP_ */
