#ifndef _LIB_STRING_STRING_HPP_
#define _LIB_STRING_STRING_HPP_

#include <limits.h>
#include <ctype.h>
#include <stdlib.h>
#include <iostream.h>
#include <string.h>
#include "base/odebase.hpp"
#include "lib/portable/hashable.hpp"

#define STRING_FIRST_INDEX 1
#define STRING_NOTFOUND 0
#define STATIC_STR_BUFSIZE 15
#define DYNAMIC_STR_OVERFLOW 32
#define MAX_HASH_CHARS 16

class StringArray;

/**
 * This String is case-aware, but only derived classes
 * can override the default (case sensitive, of course).
 * Hence, constructors which take an extra boolean are
 * decalared in the protected section.
 * This rule applies even to the copy constructor, to
 * prevent Strings from becoming SmartCaseStrings if
 * they are constructed as a copy of them.
 *
 * THEREFORE, whenever String itself uses the copy
 * constructor, it may need to use the two-parameter version
 * (and pass case_sensitive) if that temporary String
 * is used as part of case-dependent operations (indexOf,
 * compareTo, equals, etc., etc.).
 *
**/
class String // : public Hashable< String >
{
  public:

    // Constructors and destructors
    //
    inline String();
    inline String( const String &str );
    inline String( const char *cPtr );
    inline EXPLICIT_CTR String( char aChar );
    EXPLICIT_CTR String( int aInt );
    EXPLICIT_CTR String( double aDbl );
    EXPLICIT_CTR String( unsigned long aUlong );

    inline ~String();

#ifdef CASE_INSENSITIVE_OS
    unsigned long ODEHashFunction( unsigned long hash_max ) const;
#else
    inline unsigned long ODEHashFunction( unsigned long hash_max ) const;
#endif

    // Assignment and addition
    //
    String &operator=( const String &str );
    String &operator=( const char *str );
    inline String &operator+=( const String &str );
    inline String &operator+=( const char *aCharPtr );
    inline String &operator+=( char aChar );
    inline String &operator+=( int aInt );
    inline String &operator+=( unsigned long aUlong );
           String operator+( const String &str ) const;
    inline String operator+( const char *aCharPtr ) const;
    inline String operator+( char aChar ) const;
    inline String operator+( int aInt ) const;
    inline String operator+( unsigned long aUlong ) const;
    inline String concat( const String &str );
    inline String &concatThis( const String &str );
    inline static String concat( const String &orig, const String &str );

    // Comparison
    //
    inline boolean operator==( const String &str ) const;
    inline boolean operator==( const char *aCharPtr ) const;
    inline boolean operator!=( const String &str ) const;
    inline boolean operator!=( const char *aCharPtr ) const;
    inline boolean operator>( const String &str ) const;
    inline boolean operator>=( const String &str ) const;
    inline boolean operator<( const String &str ) const;
    inline boolean operator<=( const String &str ) const;
#ifdef CASE_INSENSITIVE_OS
    boolean equals( const String &str ) const;
    boolean startsWith( const String &prefix,
      unsigned long start_index = STRING_FIRST_INDEX ) const;
    boolean endsWith( const String &suffix ) const;
    int compareTo( const String &cmp ) const;
#else
    inline boolean equals( const String &str ) const;
    inline boolean startsWith( const String &prefix,
      unsigned long start_index = STRING_FIRST_INDEX ) const;
    inline boolean endsWith( const String &suffix ) const;
    inline int compareTo( const String &cmp ) const;
#endif
    inline boolean equalsIgnoreCase( const String &str ) const;
    boolean isDigits() const;
    inline boolean isEmpty() const;

    // Conversion
    //
    inline char   *toCharPtr() const;
    inline String  trim() const;
           String &trimThis();
    inline static  String trim( const String &orig );
    inline String  trimFront() const;
           String &trimFrontThis();
    inline static  String trimFront( const String &orig );
    inline String  reduceWhitespace() const;
           String &reduceWhitespaceThis();
    inline String  toLowerCase() const;
           String &toLowerCaseThis();
    inline static  String toLowerCase( const String &orig );
    inline String  toUpperCase() const;
           String &toUpperCaseThis();
    inline static  String toUpperCase( const String &orig );
    inline         int asInt() const;
    inline String &desensitizeThis();
    inline static String desensitize( const String &str );
    inline static char desensitize( char ch );
    inline static int desensitize( int ch );
    inline const String &toString() const;
    String &dequoteThis( boolean enforce_closure = false );
    inline String dequote( boolean enforce_closure = false ) const;
#ifdef FILENAME_BLANKS
    String &doubleQuoteThis();
    inline String doubleQuote() const;
#endif // FILENAME_BLANKS

    // Accessors
    //
    inline unsigned long length() const;
    inline unsigned long size() const;
    inline unsigned long firstIndex() const;
    inline unsigned long lastIndex() const;
    inline char charAt( unsigned long idx ) const;
    inline char &refCharAt( unsigned long idx );
#ifdef CASE_INSENSITIVE_OS
    unsigned long indexOf( char ch,
      unsigned long start_index = STRING_FIRST_INDEX ) const;
#else
    inline unsigned long indexOf( char ch,
      unsigned long start_index = STRING_FIRST_INDEX ) const;
#endif
    unsigned long indexOf( const String &str,
      unsigned long start_index = STRING_FIRST_INDEX ) const;
    unsigned long indexOfAny( const String &chars,
      unsigned long start_index = STRING_FIRST_INDEX ) const;
    unsigned long lastIndexOf( char ch,
      unsigned long start_index = STRING_NOTFOUND ) const;
    unsigned long lastIndexOf( const String &str,
      unsigned long start_index = STRING_NOTFOUND ) const;
    unsigned long lastIndexOfAny( const String &chars,
      unsigned long start_index = STRING_NOTFOUND ) const;
    inline char operator[]( int index ) const;
    inline char &operator[]( int index );
#ifdef CASE_INSENSITIVE_OS
    inline boolean isCaseSensitive() const;
    inline void setCaseSensitivity( boolean case_sensitive );
#endif

    // Modifiers
    //
    inline String substring( unsigned long start_index,
      long end_index = -1 ) const;
    String &substringThis( unsigned long start_index,
      long end_index = -1 );
    inline String replace( const String &find,
      const String &replace, unsigned int times = UINT_MAX,
      unsigned int start_index = STRING_FIRST_INDEX ) const;
    String &replaceThis( const String &find,
      const String &replace, unsigned int times = UINT_MAX,
      unsigned int start_index = STRING_FIRST_INDEX );
    inline static String replace( const String &orig,
      char find, char replace,
      unsigned int times = UINT_MAX,
      unsigned int start_index = STRING_FIRST_INDEX );
    inline String replace( char find,
      char replace, unsigned int times = UINT_MAX,
      unsigned int start_index = STRING_FIRST_INDEX ) const;
    String &replaceThis( char find,
      char replace, unsigned int times = UINT_MAX,
      unsigned int start_index = STRING_FIRST_INDEX );
    inline static String replace( const String &orig,
      const String &find, const String &replace,
      unsigned int times = UINT_MAX,
      unsigned int start_index = STRING_FIRST_INDEX );
    String &remove( unsigned int start_index, unsigned int num_chars );
    String &rightJustify( unsigned int length, char padChar = ' ' );
    String &append( const String &str );
    String &append( const char *str );
    String &append( char aChar );
    String &prepend( const String &str );
    String &prepend( const char *str );
    String &prepend( char aChar );
    inline void extendTo( unsigned long newlen );  // Extends bufsize to newlen

    // Split functions
    //
    inline StringArray *split( char split_by,
        unsigned int max_strings = UINT_MAX,
        StringArray *buf = 0 ) const;
    inline StringArray *split( const String &split_by,
        unsigned int max_strings = UINT_MAX,
        StringArray *buf = 0 ) const;
    StringArray *split( const char *split_by,
        unsigned int max_strings = UINT_MAX,
        StringArray *buf = 0 ) const;


  protected:

#ifdef CASE_INSENSITIVE_OS
    boolean case_sensitive; // be case sensitive for comparisons?
    // constructors for SmartCaseString
    inline String( const String &str, boolean case_sensitive );
    inline String( const char *cPtr, boolean case_sensitive );
#endif

    inline char *getCP() const;
    inline void setCP( char *newCP, boolean dofree = true );
    inline void setLength( unsigned long newlen );
    inline static  char *allocate( int length );
    inline static  char *reallocate( char *ptr, int length );
    inline static  void deallocate( char *ptr );


  private:

    unsigned long len; // Actual length of string
    unsigned long bufsize; // Size of entire string buffer (not incl. null)
    // the static buffer is used when the requested string size
    // is small enough to fit (for performance), otherwise the
    // dynamic buffer is used.
    char scp[STATIC_STR_BUFSIZE + 1]; // static char buffer
    char *cp;                         // dynamic char buffer, may point to scp

    inline void expandBuffer( int newlen );
    int findIndexToSplit( const char *split_by, int start_index ) const;
#ifdef CASE_INSENSITIVE_OS
    void copyConstructor( const String &str, boolean case_sensitive );
    void charPtrConstructor( const char *cptr, boolean case_sensitive );
#else
    void copyConstructor( const String &str );
    void charPtrConstructor( const char *cptr );
#endif
};

inline String::String() :
    len( 0 ), bufsize( 0 ), cp( scp )
{
#ifdef CASE_INSENSITIVE_OS
  setCaseSensitivity( true );
#endif
  scp[0] = '\0';
}

inline String::String( const String &str ) :
    len( str.len ), bufsize( 0 ), cp( scp )
{
#ifdef CASE_INSENSITIVE_OS
  copyConstructor( str, true );
#else
  copyConstructor( str );
#endif
}

inline String::String( const char *cptr ) :
    len( 0 ), bufsize( 0 ), cp( scp )
{
#ifdef CASE_INSENSITIVE_OS
  charPtrConstructor( cptr, true );
#else
  charPtrConstructor( cptr );
#endif
}

#ifdef CASE_INSENSITIVE_OS
inline String::String( const String &str, boolean case_sensitive ) :
    len( str.len ), bufsize( 0 ), cp( scp )
{
  copyConstructor( str, case_sensitive );
}

inline String::String( const char *cptr, boolean case_sensitive ) :
    len( 0 ), bufsize( 0 ), cp( scp )
{
  charPtrConstructor( cptr, case_sensitive );
}
#endif

inline String::String( char aChar ) :
    len( 1 ), bufsize( 0 ), cp( scp )
{
#ifdef CASE_INSENSITIVE_OS
  setCaseSensitivity( true );
#endif
  scp[0] = aChar;
  scp[1] = '\0';
}

inline String::~String()
{
  if (cp != scp)
    deallocate( cp );
}

inline String String::toUpperCase() const
{
  String result( *this );
  return (result.toUpperCaseThis());
}

inline String String::toLowerCase() const
{
  String result( *this );
  return (result.toLowerCaseThis());
}

inline String String::trim() const
{
  String result( *this );
  return (result.trimThis());
}

inline String String::reduceWhitespace() const
{
  String result( *this );
  return (result.reduceWhitespaceThis());
}

inline String String::trimFront() const
{
  String result( *this );
  return (result.trimFrontThis());
}

inline String String::substring( unsigned long start_index,
    long end_index ) const
{
  String result( *this );
  return (result.substringThis( start_index, end_index ));
}

/**
 * This SHOULD NOT be used if the current string should
 * be preserved (for speed, it doesn't copy the string
 * to the new buffer). Use extendTo() if you wish to
 * retain the current string.
**/
inline void String::expandBuffer( int newlen )
{
  if (len > STATIC_STR_BUFSIZE && bufsize < newlen)
  {
    bufsize = newlen + DYNAMIC_STR_OVERFLOW;
    this->setCP( allocate( bufsize ) );
  }
}

inline void String::extendTo( unsigned long newlen )
{
  if (newlen > STATIC_STR_BUFSIZE && bufsize < newlen)
  {
    bufsize = newlen + DYNAMIC_STR_OVERFLOW;
    if (getCP() != scp)
      setCP( reallocate( getCP(), bufsize ), false );
    else
    {
      setCP( allocate( bufsize ), false );
      memcpy( getCP(), this->scp, this->length() + 1 );
    }
  }
}

inline String &String::operator+=( const String &str )
{
  return (append( str ));
}

inline String &String::operator+=( char aChar )
{
  return (append( aChar ));
}

inline String &String::operator+=( int aInt )
{
  return (append( String( aInt ) ));
}

inline String &String::operator+=( unsigned long aUlong )
{
  return (append( String( aUlong ) ));
}

inline String &String::operator+=( const char *aCharPtr )
{
  return (append( aCharPtr ));
}

inline String String::operator+( const char *aCharPtr ) const
{
  return ((*this) + String( aCharPtr ));
}

inline String String::operator+( char aChar ) const
{
  return ((*this) + String( aChar ));
}

inline String String::operator+( int aInt ) const
{
  return ((*this) + String( aInt ));
}

inline String String::operator+( unsigned long aUlong ) const
{
  return ((*this) + String( aUlong ));
}

inline String String::concat( const String &str )
{
  return ((*this) + str);
}

inline String &String::concatThis( const String &str )
{
  return ((*this) += str);
}

inline String String::concat( const String &orig, const String &str )
{
  return (orig + str);
}

inline boolean String::equalsIgnoreCase( const String &str ) const
{
  return (this->toUpperCase().equals( str.toUpperCase() ));
}

inline String String::trimFront( const String &orig )
{
  return (orig.trimFront());
}

inline String String::toLowerCase( const String &orig )
{
  return (orig.toLowerCase());
}

inline String String::toUpperCase( const String &orig )
{
  return (orig.toUpperCase());
}

inline int String::asInt() const
{
  return (strtol( this->getCP(), 0, 10 ));
}

inline unsigned long String::length() const
{
  return (len);
}

inline unsigned long String::size() const
{
  return (this->length());
}

inline unsigned long String::firstIndex() const
{
  return (STRING_FIRST_INDEX);
}

inline unsigned long String::lastIndex() const
{
  return (STRING_FIRST_INDEX + this->length() - 1);
}

inline char &String::refCharAt( unsigned long idx )
{
  return (this->getCP()[idx - STRING_FIRST_INDEX]);
}

inline char String::charAt( unsigned long idx ) const
{
  return (this->getCP()[idx - STRING_FIRST_INDEX]);
}

inline String String::replace( const String &orig, const String &find,
  const String &replace, unsigned int times, unsigned int start_index )
{
  return (orig.replace( find, replace, times, start_index ));
}

inline String String::replace( const String &orig, char find,
  char replace, unsigned int times, unsigned int start_index )
{
  return (orig.replace( find, replace, times, start_index ));
}

/**
 * MUST use the two-parameter copy constructor here in
 * case we are actually a SmartCaseString.
**/
inline String String::replace( const String &find, const String &replace,
    unsigned int times, unsigned int start_index ) const
{
#ifdef CASE_INSENSITIVE_OS
  String tempstr( *this, case_sensitive );
#else
  String tempstr( *this );
#endif
  return (tempstr.replaceThis( find, replace, times, start_index ));
}

/**
 * MUST use the two-parameter copy constructor here in
 * case we are actually a SmartCaseString.
**/
inline String String::replace( char find, char replace,
    unsigned int times, unsigned int start_index ) const
{
#ifdef CASE_INSENSITIVE_OS
  String tempstr( *this, case_sensitive );
#else
  String tempstr( *this );
#endif
  return (tempstr.replaceThis( find, replace, times, start_index ));
}

inline void String::setCP( char *newCP, boolean dofree )
{
  if (dofree && cp != scp)
    deallocate( cp );
  cp = newCP;
}

inline char *String::getCP() const
{
  return (cp);
}

inline void String::setLength( unsigned long newlen )
{
  this->len = newlen;
}

inline boolean String::isEmpty() const
{
  return (this->len == 0);
}

inline char *String::allocate( int length )
{
  return ((char*)malloc( length + 1 ));
}

inline char *String::reallocate( char *ptr, int length )
{
  return ((char*)realloc( ptr, length + 1 ));
}

inline void String::deallocate( char *ptr )
{
  free( (void*)ptr );
}

/**
 * Split a string into an array of strings.
**/
inline StringArray *String::split( char split_by,
    unsigned int max_strings, StringArray *buf ) const
{
  char chstr[2] = { split_by, '\0' };
  return (split( chstr, max_strings, buf ));
}

/**
 * Split a string into an array of strings.
**/
inline StringArray *String::split( const String &split_by,
    unsigned int max_strings, StringArray *buf ) const
{
  return (split( split_by.toCharPtr(), max_strings, buf ));
}

inline boolean String::operator!=( const String &str ) const
{
  return (!this->equals( str ));
}

inline boolean String::operator!=( const char *aCharPtr ) const
{
  return (!this->equals( aCharPtr ));
}

inline boolean String::operator>( const String &str ) const
{
  return (compareTo( str ) > 0);
}

inline boolean String::operator>=( const String &str ) const
{
  return (compareTo( str ) >= 0);
}

inline boolean String::operator<( const String &str ) const
{
  return (compareTo( str ) < 0);
}

inline boolean String::operator<=( const String &str ) const
{
  return (compareTo( str ) <= 0);
}

// Non-String class functions
inline String operator+( const char *cPtr, const String &str )
{
  return (String( cPtr ) + str);
}

inline boolean operator==( const char *cPtr, const String &str )
{
  return (str.equals( cPtr ));
}

inline boolean operator!=( const char *cPtr, const String &str )
{
  return (!str.equals( cPtr ));
}

inline boolean operator<( const char *cPtr, const String &str )
{
  return (str.operator>=( cPtr ));
}

inline boolean operator<=( const char *cPtr, const String &str )
{
  return (str.operator>( cPtr ));
}

inline boolean operator>( const char *cPtr, const String &str )
{
  return (str.operator<=( cPtr ));
}

inline boolean operator>=( const char *cPtr, const String &str )
{
  return (str.operator<( cPtr ));
}

inline char String::operator[]( int index ) const
{
  return (charAt( index ));
}

inline char &String::operator[]( int index )
{
  return (refCharAt( index ));
}

inline char *String::toCharPtr() const
{
  return (this->getCP());
}

/**
 * Convert this string to some uniform case (upper or lower).
 *
 * @return The internal insensitive string after conversion
 * to upper or lower case.
 */
inline String &String::desensitizeThis()
{
  return (toLowerCaseThis());
}

/**
 * Convert this string to some uniform case (upper or lower).
 *
 * @return The internal insensitive string after conversion
 * to upper or lower case.
 */
inline String String::desensitize( const String &str )
{
  return (str.toLowerCase());
}

/**
 * Convert this string to some uniform case (upper or lower).
 *
 * @param ch The character to convert the case of.
 * @return The character after conversion to upper or lower case.
 */
inline char String::desensitize( char ch )
{
  return ((char)tolower( ch ));
}

/**
 * Convert this string to some uniform case (upper or lower).
 *
 * @param ch The character to convert the case of.
 * @return The character after conversion to upper or lower case.
 */
inline int String::desensitize( int ch )
{
  return (tolower( ch ));
}

inline const String &String::toString() const
{
  return (*this);
}

#ifdef CASE_INSENSITIVE_OS
inline boolean String::isCaseSensitive() const
{
  return (this->case_sensitive);
}

inline void String::setCaseSensitivity( boolean case_sensitive )
{
  this->case_sensitive = case_sensitive;
}
#endif

/**
 * Throws an Exception if enforce_closure is true and
 * a quotation is unclosed.
**/
String String::dequote( boolean enforce_closure ) const
{
  String result( *this );
  return (result.dequoteThis( enforce_closure ));
}

#ifdef FILENAME_BLANKS
String String::doubleQuote() const
{
  String result( *this );
  return (result.doubleQuoteThis());
}
#endif // FILENAME_BLANKS

inline ostream &operator<<( ostream &os, const String &str )
{
  os << str.toCharPtr();
  return (os);
}

inline boolean String::operator==( const String &str ) const
{
  return (this->equals( str ));
}

inline boolean String::operator==( const char *aCharPtr ) const
{
  return (this->equals( aCharPtr ));
}

#ifndef CASE_INSENSITIVE_OS
inline boolean String::startsWith( const String &prefix,
  unsigned long start_index ) const
{
  if ((length() - start_index + STRING_FIRST_INDEX) < prefix.length())
    return (false);
  else
    return (memcmp( getCP() + start_index - STRING_FIRST_INDEX,
        prefix.getCP(), prefix.length() ) == 0);
}

inline boolean String::endsWith( const String &suffix ) const
{
  if (length() < suffix.length())
    return (false);
  else
    return (memcmp( getCP() + this->length() - suffix.length(),
        suffix.getCP(), suffix.length() ) == 0);
}

inline boolean String::equals( const String &str ) const
{
  if (this->length() != str.length())
    return ( false );
  else
    return (memcmp( this->getCP(), str.getCP(), this->length() + 1 ) == 0);
}

inline int String::compareTo( const String &cmp ) const
{
  return (strcoll( this->getCP(), cmp.getCP() ));
}

inline unsigned long String::indexOf( char ch,
    unsigned long start_index ) const
{
  const char *ptr = strchr( getCP() + (start_index - STRING_FIRST_INDEX), ch );
  return ((ptr == 0) ? STRING_NOTFOUND : (ptr - getCP() + STRING_FIRST_INDEX));
}

inline unsigned long String::ODEHashFunction( unsigned long hash_max ) const
{
  int offset = (this->length() <= MAX_HASH_CHARS) ? 0 :
      (this->length() - MAX_HASH_CHARS);
  char *ptr = this->getCP() + offset;

  return (::ODEHashFunction( ptr, hash_max ));
}
#endif // CASE_INSENSITIVE_OS

#endif // _LIB_STRING_STRING_HPP_
