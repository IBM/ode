#define _LIB_STRING_STRING_CPP_

#include <ctype.h>
#include <stdio.h>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/exceptn/exceptn.hpp"
#include "lib/portable/native/strings.h"


String::String( int aInt ) :
    len( 0 ), bufsize( 0 ), cp( scp )
{
#ifdef CASE_INSENSITIVE_OS
  setCaseSensitivity( true );
#endif
  char buf[33];
  sprintf( buf, "%d", aInt );
  this->len = strlen( buf );
  expandBuffer( this->len );
  memcpy( this->getCP(), buf, len+1 );
}

String::String( unsigned long aUlong ) :
    len( 0 ), bufsize( 0 ), cp( scp )
{
#ifdef CASE_INSENSITIVE_OS
  setCaseSensitivity( true );
#endif
  char buf[33];
  sprintf( buf, "%ld", aUlong );
  this->len = strlen( buf );
  expandBuffer( this->len );
  memcpy( this->getCP(), buf, len+1 );
}

String::String( double aDbl ) :
    len( 0 ), bufsize( 0 ), cp( scp )
{
#ifdef CASE_INSENSITIVE_OS
  setCaseSensitivity( true );
#endif
  char buf[65];
  sprintf( buf, "%lf", aDbl );
  this->len = strlen( buf );
  expandBuffer( this->len );
  memcpy( cp, buf, len+1 );
}

#ifdef CASE_INSENSITIVE_OS
void String::copyConstructor( const String &str, boolean case_sensitive )
#else
void String::copyConstructor( const String &str )
#endif
{
#ifdef CASE_INSENSITIVE_OS
  setCaseSensitivity( case_sensitive );
#endif
  expandBuffer( str.bufsize );
  memcpy( cp, str.getCP(), len + 1 );
}

#ifdef CASE_INSENSITIVE_OS
void String::charPtrConstructor( const char *cptr, boolean case_sensitive )
#else
void String::charPtrConstructor( const char *cptr )
#endif
{
#ifdef CASE_INSENSITIVE_OS
  setCaseSensitivity( case_sensitive );
#endif
  len = strlen( cptr );
  expandBuffer( len );
  memcpy( this->getCP(), cptr, len + 1 );
}

/**
 * Assignment operator
 *
 * DO NOT change the value of case_sensitive here.  Imagine:
 *
 * String str1;
 * SmartCaseString str2( "value", false );
 * str1 = str2;
 *
 * It would not be appropriate to change the sensitivity of str1.
 *
**/
String &String::operator=( const char *str )
{
  len = strlen( str );
  expandBuffer( len );
  memcpy( this->getCP(), str, len + 1 );
  return ( *this );
}

/**
 * Assignment operator
 *
 * DO NOT change the value of case_sensitive here.  Imagine:
 *
 * String str1;
 * SmartCaseString str2( "value", false );
 * str1 = str2;
 *
 * It would not be appropriate to change the sensitivity of str1.
 *
**/
String &String::operator=( const String &str )
{
  if (this->getCP() != str.getCP())
  {
    len = str.length();
    expandBuffer( len );
    memcpy( this->getCP(), str.getCP(), len + 1 );
  }
  return ( *this );
}

String String::operator+( const String &str ) const
{
  String newstr;
  newstr.setLength( this->length() + str.length() );
  newstr.expandBuffer( newstr.length() );
  memcpy( newstr.getCP(), this->getCP(), this->length() );
  memcpy( newstr.getCP() + this->length(), str.getCP(), str.length() + 1 );
  return ( newstr );
}

String &String::append( char aChar )
{
  extendTo( length() + 1 );
  *(getCP() + len) = aChar;
  *(getCP() + (++len)) = '\0';
  return (*this);
}

String &String::append( const char *str )
{
  unsigned long str_len = strlen( str );
  extendTo( len + str_len );
  memcpy( getCP() + length(), str, str_len + 1 );
  len += str_len;
  return (*this);
}

String &String::append( const String &str )
{
  extendTo( len + str.length() );
  memcpy( getCP() + length(), str.getCP(), str.length() + 1 );
  len += str.length();
  return (*this);
}

String &String::prepend( char aChar )
{
  extendTo( length() + 1 );
  memmove( getCP() + 1, getCP(), ++len ); // shift right
  *(getCP()) = aChar;
  return (*this);
}

String &String::prepend( const char *str )
{
  unsigned long str_len = strlen( str );
  extendTo( len + str_len );
  memmove( getCP() + str_len, getCP(), this->length() + 1 ); // shift right
  memcpy( getCP(), str, str_len );
  len += str_len;
  return (*this);
}

String &String::prepend( const String &str )
{
  extendTo( len + str.length() );
  memmove( getCP() + str.length(), getCP(), this->length() + 1 ); // shift right
  memcpy( getCP(), str.getCP(), str.length() );
  len += str.length();
  return (*this);
}

#ifdef CASE_INSENSITIVE_OS
boolean String::startsWith( const String &prefix,
  unsigned long start_index ) const
{
  if ((length() - start_index + STRING_FIRST_INDEX) < prefix.length())
    return (false);

  static String senseless_this, senseless_prefix; // desensitized versions

  if (isCaseSensitive() && prefix.isCaseSensitive())
  {
    return (memcmp( getCP() + start_index - STRING_FIRST_INDEX,
        prefix.getCP(), prefix.length() ) == 0);
  }
  else
  {
    senseless_this = *this;
    senseless_this.desensitizeThis();
    senseless_prefix = prefix;
    senseless_prefix.desensitizeThis();
    return (memcmp( senseless_this.getCP() + start_index - STRING_FIRST_INDEX,
        senseless_prefix.getCP(), senseless_prefix.length() ) == 0);
  }
}

boolean String::endsWith( const String &suffix ) const
{
  if (length() < suffix.length())
    return (false);

  static String senseless_this, senseless_suffix; // desensitized versions

  if (isCaseSensitive() && suffix.isCaseSensitive())
    return (memcmp( getCP() + this->length() - suffix.length(),
        suffix.getCP(), suffix.length() ) == 0);
  else
  {
    senseless_this = *this;
    senseless_this.desensitizeThis();
    senseless_suffix = suffix;
    senseless_suffix.desensitizeThis();
    return (memcmp( senseless_this.getCP() + senseless_this.length() -
        senseless_suffix.length(),
        senseless_suffix.getCP(), senseless_suffix.length() ) == 0);
  }
}

boolean String::equals( const String &str ) const
{
  if (this->length() != str.length())
    return ( false );

  static String senseless_this, senseless_str; // desensitized versions

  if (isCaseSensitive() && str.isCaseSensitive())
    return (memcmp( this->getCP(), str.getCP(), this->length() + 1 ) == 0);
  else
  {
    senseless_this = *this;
    senseless_this.desensitizeThis();
    senseless_str = str;
    senseless_str.desensitizeThis();
    return (memcmp( senseless_this.getCP(), senseless_str.getCP(),
        senseless_this.length() + 1 ) == 0);
  }
}

int String::compareTo( const String &cmp ) const
{
  static String senseless_this, senseless_cmp; // desensitized versions

  if (isCaseSensitive() && cmp.isCaseSensitive())
    return (strcoll( this->getCP(), cmp.getCP() ));
  else
  {
    senseless_this = *this;
    senseless_this.desensitizeThis();
    senseless_cmp = cmp;
    senseless_cmp.desensitizeThis();
    return (strcoll( senseless_this.getCP(), senseless_cmp.getCP() ));
  }
}
#endif

String &String::trimThis()
{
  unsigned int tmp_idx = STRING_FIRST_INDEX;
  char *tmp_cp = this->getCP();

  while (*tmp_cp == ' ' || *tmp_cp == '\t')
  {
    ++tmp_idx;
    ++tmp_cp;
  }

  // If the string was all spaces then return an empty string.
  if (tmp_idx > this->lastIndex()) // remember, finish is 1 past last char
  {
    this->setLength( 0 );
    this->getCP()[0] = '\0';
    return (*this);
  }
  else if (tmp_idx > STRING_FIRST_INDEX)
  {
    memmove( this->getCP(), tmp_cp, this->lastIndex() - tmp_idx + 1);
    this->setLength( this->lastIndex() - tmp_idx + 1 );
    this->getCP()[this->length()] = '\0';
  }

  tmp_cp = this->getCP() + this->length() - 1;
  tmp_idx = this->lastIndex();
  while (*tmp_cp == ' ' || *tmp_cp == '\t')
  {
    --tmp_idx;
    *tmp_cp = '\0';  // Pad with null chars
    --tmp_cp;
  }
  this->setLength( tmp_idx ); // don't account for start idx at '1'
  return (*this);
}

String &String::trimFrontThis()
{
  unsigned int tmp_idx = STRING_FIRST_INDEX;
  char *tmp_cp = this->getCP();

  while (*tmp_cp == ' ' || *tmp_cp == '\t')
  {
    ++tmp_idx;
    ++tmp_cp;
  }

  // If the string was all spaces then return an empty string.
  if (tmp_idx > this->lastIndex()) // remember, finish is 1 past last char
  {
    this->setLength( 0 );
    this->getCP()[0] = '\0';
    return (*this);
  }
  else if (tmp_idx > STRING_FIRST_INDEX)
  {
    memmove( this->getCP(), tmp_cp, this->lastIndex() - tmp_idx + 1);
    this->setLength( this->lastIndex() - tmp_idx + 1 );
    this->getCP()[this->length()] = '\0';
  }
  return (*this);
}


/**
 * reduceWhitespaceThis()
 *
 * Eliminates extra tabs and spaces in non-quoted strings.
 *   ->  Converts multiple tabs and spaces to a single space.
 *
 * Note: previously existed as Command::convert()
 *
**/
String &String::reduceWhitespaceThis()
{
  unsigned int newLen = 0;
  char wasspace=(char)0;
  char inquote=(char)0;
  char wasescape=(char)0;

  trimThis();

  char *cp_1 = getCP();
  char *cp_2 = getCP();

  while ((*cp_2 != '\0') && (*cp_2 != '\r') && (*cp_2 != '\n'))
  {
    switch (*cp_2)
    {
      case '"':
      case '\'':
        // If this is an escaped quote then, remember it and continue to
        // the next character.
        if (wasescape != (char)0)
        {
          wasescape = (char)0;
          break;
        }
        // Need to preserve the quotes
        if (inquote != (char)0)
        {
          if (inquote == *cp_2)
            inquote = (char)0;
        }
        else
        {
          inquote = *cp_2;
        }
        wasescape = (char)0;
        break;
      case ' ':
      case '\t':
        // Made the assumption that you can escape a tab or space when
        // outside of a quoted string.
        //
        wasescape = (char)0;
        if (inquote != (char)0)
          break;
        else
        {
          // Already at a space, so skip ahead
          if (wasspace != (char)0)
          {
            ++cp_2;
            continue;
          }
          // If this is the first whitespace, then save a space and
          // continue.
          wasspace = *cp_2;
          *cp_1 = ' ';
          ++cp_1;
          ++newLen;
          ++cp_2;
          continue;
        }
      case '\\':
        // Case of an escaped, escape character
        if (wasescape != (char)0)
          wasescape = (char)0;
        else
          wasescape = *cp_2;
        break;
      default:
        break;
    }
    wasspace  = (char)0;
    if (*cp_2 != '\\')
      wasescape = (char)0;
    *cp_1 = *cp_2;
    ++cp_1;
    ++newLen;
    ++cp_2;
  }

  *cp_1 = '\0';
  this->setLength( newLen );

  return (*this);
}


String &String::toLowerCaseThis()
{
  char *tmp_cp = this->getCP();
  while (*tmp_cp != '\0')
  {
    *tmp_cp = tolower( *tmp_cp );
    ++tmp_cp;
  }

  return (*this);
}

String &String::toUpperCaseThis()
{
  char *tmp_cp = this->getCP();
  while (*tmp_cp != '\0')
  {
    *tmp_cp = toupper( *tmp_cp );
    ++tmp_cp;
  }

  return (*this);
}

boolean String::isDigits() const
{
  char *tmp_cp = this->getCP();
  while (*tmp_cp != '\0')
    if (!isdigit( *(tmp_cp++) ))
      return ( false );
  return ( true );
}

#ifdef CASE_INSENSITIVE_OS
unsigned long String::indexOf( char ch, unsigned long start_index ) const
{
  if (isCaseSensitive())
  {
    for (int idx = start_index; idx <= this->lastIndex(); idx++)
      if (this->charAt( idx ) == ch)
        return ( idx );
  }
  else
  {
    for (int idx = start_index; idx <= this->lastIndex(); idx++)
      if (desensitize( this->charAt( idx ) ) == desensitize( ch ))
        return ( idx );
  }

  return (STRING_NOTFOUND);
}
#endif

unsigned long String::lastIndexOf( char ch, unsigned long start_index ) const
{
  if (start_index == STRING_NOTFOUND) // user wants to start at the end
    start_index = this->lastIndex();

#ifdef CASE_INSENSITIVE_OS
  if (isCaseSensitive())
  {
#endif
    for (int idx = start_index; idx >= this->firstIndex(); idx--)
      if (this->charAt( idx ) == ch)
        return ( idx );
#ifdef CASE_INSENSITIVE_OS
  }
  else
  {
    for (int idx = start_index; idx >= this->firstIndex(); idx--)
      if (desensitize( this->charAt( idx ) ) == desensitize( ch ))
        return ( idx );
  }
#endif

  return (STRING_NOTFOUND);
}

unsigned long String::indexOf( const String &str,
    unsigned long start_index ) const
{
  if (str.isEmpty() || str.length() > this->length())
    return (STRING_NOTFOUND);

  int last_index = this->lastIndex() - str.length() + 1; // optimize end

#ifdef CASE_INSENSITIVE_OS
  if (isCaseSensitive() && str.isCaseSensitive())
  {
#endif
    for (int idx = start_index; idx <= last_index; idx++)
      if (strncmp( this->getCP() + idx - STRING_FIRST_INDEX,
          str.getCP(), str.length() ) == 0)
        return ( idx );
#ifdef CASE_INSENSITIVE_OS
  }
  else
  {
    for (int idx = start_index; idx <= last_index; idx++)
      if (ODEstrncasecmp( this->getCP() + idx - STRING_FIRST_INDEX,
          str.getCP(), str.length() ) == 0)
        return ( idx );
  }
#endif

  return (STRING_NOTFOUND);
}

/**
 *
 * If start_index is given as STRING_NOTFOUND, this
 * indicates the default start_index, which is the end
 * of the string.
 *
**/
unsigned long String::lastIndexOf( const String &str,
    unsigned long start_index ) const
{
  if (str.isEmpty() || str.length() > this->length())
    return (STRING_NOTFOUND);

  if (start_index == STRING_NOTFOUND || // user wants to start at the end
      (start_index + str.length() - 1) > this->lastIndex()) // optimize start
    start_index = this->lastIndex() - str.length() + 1;

#ifdef CASE_INSENSITIVE_OS
  if (isCaseSensitive() && str.isCaseSensitive())
  {
#endif
    for (int idx = start_index; idx >= this->firstIndex(); idx--)
      if (strncmp( this->getCP() + idx - STRING_FIRST_INDEX,
          str.getCP(), str.length() ) == 0)
        return (idx);
#ifdef CASE_INSENSITIVE_OS
  }
  else
  {
    for (int idx = start_index; idx >= this->firstIndex(); idx--)
      if (ODEstrncasecmp( this->getCP() + idx - STRING_FIRST_INDEX,
          str.getCP(), str.length() ) == 0)
        return (idx);
  }
#endif

  return (STRING_NOTFOUND);
}

unsigned long String::indexOfAny( const String &chars,
    unsigned long start_index ) const
{
  int i, j;

#ifdef CASE_INSENSITIVE_OS
  if (isCaseSensitive())
  {
#endif
    for (i = start_index; i <= this->lastIndex(); i++)
      for (j = STRING_FIRST_INDEX; j <= chars.lastIndex(); j++)
        if (this->charAt( i ) == chars[j])
          return ( i );
#ifdef CASE_INSENSITIVE_OS
  }
  else
  {
    for (i = start_index; i <= this->lastIndex(); i++)
      for (j = STRING_FIRST_INDEX; j <= chars.lastIndex(); j++)
        if (desensitize( this->charAt( i ) ) == desensitize( chars[j] ))
          return ( i );
  }
#endif

  return (STRING_NOTFOUND);
}

/**
 *
 * If start_index is given as STRING_NOTFOUND, this
 * indicates the default start_index, which is the end
 * of the string.
 *
**/
unsigned long String::lastIndexOfAny( const String &chars,
    unsigned long start_index ) const
{
  int i, j;

  if (start_index == STRING_NOTFOUND) // user wants to start at the end
    start_index = this->lastIndex();

#ifdef CASE_INSENSITIVE_OS
  if (isCaseSensitive())
  {
#endif
    for (i = start_index; i >= this->firstIndex(); i--)
      for (j = STRING_FIRST_INDEX; j <= chars.lastIndex(); j++)
        if (this->charAt( i ) == chars[j])
          return ( i );
#ifdef CASE_INSENSITIVE_OS
  }
  else
  {
    for (i = start_index; i >= this->firstIndex(); i--)
      for (j = STRING_FIRST_INDEX; j <= chars.lastIndex(); j++)
        if (desensitize( this->charAt( i ) ) == desensitize( chars[j] ))
          return ( i );
  }
#endif

  return (STRING_NOTFOUND);
}

/**
 * Note that the character at end_index is not included in the returned
 * string.  That is, the resulting string is formed from the characters
 * in the half-open interval [start_index, end_index).  This matches
 * Java's substring implementation.
 *
**/
String &String::substringThis( unsigned long start_index, long end_index )
{
  static unsigned long newlen;

  if (this->isEmpty() || start_index == end_index ||
      this->lastIndex() < start_index)
  {
    this->setLength( 0 );
    this->getCP()[0] = '\0';
    return (*this);
  }

  if (end_index == -1 || end_index > this->lastIndex())
    newlen = this->lastIndex() - start_index + 1;
  else
    newlen = end_index - start_index;

  memmove( this->getCP(), this->getCP() + start_index - STRING_FIRST_INDEX,
      newlen );
  this->getCP()[newlen] = '\0'; // Add terminating null char
  this->setLength( newlen );
  return (*this);
}

String &String::replaceThis( const String &find, const String &replace,
    unsigned int times, unsigned int start_index )
{
  // Determine if we can use the character version of replace
  //
  if (find.length() == 1 && replace.length() == 1)
    return (this->replaceThis( find.charAt( find.firstIndex() ),
        replace.charAt( replace.firstIndex() ), times, start_index ));

  if (this->isEmpty() || find.isEmpty())
    return(*this);

  unsigned int ntimes = 0, idx, lidx = start_index;

  // by how many characters does the string length change for each
  // replacement?  might be negative, of course, hence the casts.
  long size_change = (long)replace.length() - (long)find.length();

  if (find.length() < replace.length()) // string may grow in size
    // calculate the maximum possible size
    this->extendTo( ((length() / find.length()) * replace.length()) +
        (length() % find.length()) );

  while (ntimes++ < times)
  {
    // WARNING: You must use indexOf as the searching mechanism
    // to ensure that the correct behavior occurs when the actual
    // object (this) is a SmartCaseString.
    idx = this->indexOf( find, lidx );

    if (idx == STRING_NOTFOUND)
      break;

    // adjust characters to accomodate the replace string
    memmove( this->getCP() + idx - STRING_FIRST_INDEX + replace.length(),
        this->getCP() + idx - STRING_FIRST_INDEX + find.length(),
        // get length of this string past the last char in "find"
        this->lastIndex() - (idx + find.length() - 1) +
        1 ); // plus 1 to include the null terminator

    memcpy( this->getCP() + idx - STRING_FIRST_INDEX,
        replace.toCharPtr(), replace.length() );
    setLength( this->length() + size_change );
    lidx = idx + replace.length(); // Jump ahead past the found string
  }
  return (*this);
}

String &String::replaceThis( char find, char replace, unsigned int times,
  unsigned int start_index )
{
  if (this->isEmpty())
    return(*this);
  unsigned int index = start_index, i = 0;

  // for some reason, a for-loop won't work here on MVS when "times"
  // is UINT_MAX.  A while-loop works fine, though.
  while (i++ < times)
  {
    // WARNING: You must use indexOf as the searching mechanism
    // to ensure that the correct behavior occurs when the actual
    // object (this) is a SmartCaseString.
    if ((index = this->indexOf( find, index )) == STRING_NOTFOUND)
      break;
    (*this).getCP()[index - STRING_FIRST_INDEX] = replace;
    ++index;
  }
  return(*this);
}

String &String::rightJustify( unsigned int length,
  char padChar )
{
  if (this->length() >= length)
    return ( *this );
  String tmpstr;
  for (int idx=0; idx < length - this->length(); idx++)
    tmpstr += padChar;
  tmpstr += *this;
  return (operator=( tmpstr ));
}


String &String::remove( unsigned int start_index, unsigned int num_chars )
{
  if (num_chars < 1)
    return (*this);
  if ((start_index + num_chars - 1) > lastIndex())
    num_chars = lastIndex() - start_index + 1;
  for (int i = start_index; i <= (lastIndex() - num_chars + 1); ++i)
  {
    *(getCP() + (i - STRING_FIRST_INDEX)) =
        *(getCP() + (i - STRING_FIRST_INDEX) + num_chars);
  }
  setLength( length() - num_chars );
  return ( *this );
}

#ifdef CASE_INSENSITIVE_OS
/**
 * Override the hash function to return the default
 * hash for strings, using the correct string.
**/
unsigned long String::ODEHashFunction( unsigned long hash_max ) const
{
  int offset = (this->length() <= MAX_HASH_CHARS) ? 0 :
      (this->length() - MAX_HASH_CHARS);

  static String senseless_this; // desensitized version

  if (isCaseSensitive())
    return (::ODEHashFunction( this->getCP() + offset, hash_max ));
  else
  {
    senseless_this = *this;
    senseless_this.desensitizeThis();
    return (::ODEHashFunction( senseless_this.getCP() + offset, hash_max ));
  }
}
#endif

/**
 * All of the split functions split a string
 * into a string array, as follows: the string
 * is searched for any characters in split_by,
 * and those characters are removed.  The gaps
 * that these removals leave behind establish
 * where the array element boundaries become.
 *
 * The max_strings parameter defines the maximum
 * number of array elements to return.  If zero
 * is passed, there will be no practical maximum
 * (the limit is set to UINT_MAX).  If nonzero and
 * the maximum is reached before all the splitting
 * is finished, the remainder of the original string
 * is concatenated to the last array element (which
 * implies that the final element *may* contain some
 * split_by characters, although it is guaranteed not
 * to *start* with one).
 *
 * Example:
 * assume str = "=a==$b== c ="
 * str.split( "=@$" )
 * produces an array of Strings with the following
 * elements, in order: "a", "b", " c ".
 *
 * NOTE: single/double-quoted strings are not split within
 * quotes.  So:
 * String str( "foo1 'this is a quoted string' foo2" );
 * str.split( " " ); // split into words (on spaces)
 * produces an array of THREE elements:
 * "foo1", "'this is a quoted string'", and "foo2".
 *
 * WARNING: If buf was passed as null, the caller MUST
 * deallocate the returned pointer itself, using the
 * delete operator.  If buf is non-null the new strings
 * will be appended to the end of that array,
 * so it is probably wise to pass an empty array.
 *
**/
StringArray *String::split( const char *split_by,
    unsigned int max_strings, StringArray *buf ) const
{
  StringArray *rc = (buf == 0) ? new StringArray() : buf;
  unsigned long old_index = firstIndex(), new_index = STRING_NOTFOUND;
  unsigned int count = 0;

  rc->clear(); // reset array to zero elements
  if (max_strings == 0) // zero (and UINT_MAX) means "unlimited"
    max_strings = UINT_MAX;

  while (old_index <= lastIndex())
  {
    new_index = findIndexToSplit( split_by, old_index );
    if (new_index != old_index)
    {
      if (++count >= max_strings || new_index == STRING_NOTFOUND)
      {
        rc->add( substring( old_index ) ); // rest of string
        break; // done
      }
      else
        rc->add( substring( old_index, new_index ) );
    }
    old_index = new_index + 1;
  }

  return (rc);
}

int String::findIndexToSplit( const char *split_by, int start_index ) const
{
  char ch, quote = ' '; // init to something other than a quote

#ifdef CASE_INSENSITIVE_OS
  static String senseless_split_by; // desnsitized version

  if (!isCaseSensitive())
  {
    senseless_split_by += split_by;
    senseless_split_by.desensitizeThis();
  }
#endif

  for (int i = start_index; i <= lastIndex(); ++i)
  {
    ch = charAt( i );
    if (ch == '\"' || ch == '\'')
    {
      if (quote == ch) // quote is now closed
      {
        quote = ' ';
        continue;
      }
      else if (quote == ' ') // quote isn't open yet, so maybe open it
      {
        if (strchr( split_by, ch ) == 0) // make sure not splitting on quotes
        {
          quote = ch;
          continue;
        }
      }
    }

    if (quote == ' ')
    {
#ifdef CASE_INSENSITIVE_OS
      if (isCaseSensitive())
      {
#endif
        if (strchr( split_by, ch ) != 0)
          return (i);
#ifdef CASE_INSENSITIVE_OS
      }
      else
      {
        if (strchr( senseless_split_by.getCP(), desensitize( ch ) ) != 0)
          return (i);
      }
#endif
    }
  }

  return (STRING_NOTFOUND);
}

/**
 * dequoteThis
 *
 * Remove the quote chars around a string.
 *
 * Throws an Exception if enforce_closure is true and
 * a quotation is unclosed.
**/
String &String::dequoteThis( boolean enforce_closure )
{
  boolean quoted = false;
  char quote = ' '; // something that's not a quote

  for (int i = STRING_FIRST_INDEX; i <= this->lastIndex(); ++i)
  {
    switch (this->charAt( i ))
    {
      case '\'':
      case '\"':
        if (quoted) // already inside a quotation?
        {
          if (quote == this->charAt( i )) // same type as the open quote?
          {
            quoted = false; // done
            this->remove( i--, 1 );
          }
        }
        else // if not, then we are now!
        {
          quoted = true;
          quote = this->charAt( i ); // remember the type of quote
          this->remove( i--, 1 );
        }
        break;
      default:
        break;
    }
  }
  if (enforce_closure && quoted)
    throw Exception( "unclosed quotation" );
  return (*this);
}

#ifdef FILENAME_BLANKS
/**
 * If non-double-quoted whitespace is found, it is double-quoted.
 * Windows systems treat single quotes as ordinary characters so
 * only double quotes will be used to quote blanks, tabs, etc.
 * If the string has an odd number of double quotes, another quote
 * will be appended.
**/
String &String::doubleQuoteThis()
{
  boolean quoting = false;
  boolean theirQuote = false;
  boolean copying = false;
  String tmp;
  for (int i = STRING_FIRST_INDEX; i <= this->lastIndex(); ++i)
  {
    switch (this->charAt( i ))
    {
      case '\"':
        if (quoting)
        {
          if (theirQuote)
            quoting = false; // end of their quoted section
          else
          { // End our quoted section and start theirs with their quote.
            // Since we were quoting, we were copying chars.
            tmp.append( '\"' );
            theirQuote = true;
          }
        }
        else
        { // We were not quoting; it begins their quoted section.
          quoting = true;
          theirQuote = true;
        }
        break;
      case ' ':
      case '\r':
      case '\n':
      case '\t':
        // quote whitespace stuff
        if (!quoting)
        {
          if (!copying)
          {
            copying = true;
            // It's not necessary to the quote in front, but it looks neater!
            tmp.append( '\"' );
            tmp.append(this->substring( 1, i ));
          }
          else
            tmp.append( '\"' );
          quoting = true;
          theirQuote = false;
        }
        break;
      default:
        break;
    } // end switch
    if (copying)
      tmp.append( this->charAt( i ) );
  } // end for
  // Put a quote at the end if we are still quoting.
  if (quoting)
  {
    if (!copying)
      this->append( '\"' );
    else
      tmp.append( '\"' );
  }
  if (copying)
  { // copy result back to this.
    if (this->getCP() != tmp.getCP())
    {
      len = tmp.length();
      expandBuffer( len );
      memcpy( this->getCP(), tmp.getCP(), len + 1 );
    }
  }
  return (*this);
}
#endif // FILENAME_BLANKS
