#define _ODE_LIB_UTIL_ARCH_CPP_
#include "lib/string/strcon.hpp"
#include "lib/util/arch.hpp"
#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/native/arch.h"
#include "lib/exceptn/parseexc.hpp"

void Archive::readMembers()
{
  ODEArchInfo *ptr = ODEopenArch( Path::canonicalize( *this ).toCharPtr() );

  if (ptr != 0)
  {
    members.clear();
    valid = true;
    while (ODEreadArchNext( ptr ) == 0)
    {
      members.put( ptr->name, new CachedArchMember(
          ptr->date, *this, ptr->name, ptr->current_member ) );
    }
    ODEcloseArch( ptr );
  }
  else
    valid = false;
}

/**
 * Archive::parse()
 *
 * Note that a space is appended to the input before parsing.
 * This avoids the need to check the parsed buffer after the for
 * loop has ended to see if there's still one name left.  Since
 * spaces are ignored outside of quoted strings, this won't
 * affect the returned values.
 *
 * Example input:
 *    libc.a( foo1.o  foo2.o  "foo 3.o") foo4.o
 *
 * Results in four array elements:
 *    libc.a(foo1.o) libc.a(foo2.o) libc.a(foo 3.o) foo4.o
 *
**/
void Archive::parse( const String &input, StringArray *result )
// throw ParseException
{
  char quote = ' '; // init to something other than a quote
  String arch, buf;
  String line = input;
  line += StringConstants::SPACE;

  result = (result == 0) ? new StringArray() : result;

  for (int i = line.firstIndex(); i <= line.lastIndex(); ++i)
  {
    if (quote == ' ' || line[i] == quote) // unquoted or closing quote now
    {
      switch (line[i])
      {
        case ' ': case '\t':
          if (buf != StringConstants::EMPTY_STRING)
          {
            if (arch != StringConstants::EMPTY_STRING)
              result->add( arch + StringConstants::OPEN_PAREN + buf +
                  StringConstants::CLOSE_PAREN );
            else
              result->add( buf );
            buf = StringConstants::EMPTY_STRING;
          }
          continue; // jump to next for-loop value
        case '\"': case '\'':
          if (quote == ' ') // not quoted yet, so line[i] is an open quote
            quote = line[i];
          else // we know this is a matching closing quote
            quote = ' ';
          continue; // strip quotes
        case '(':
          if (arch != StringConstants::EMPTY_STRING)
            throw ParseException( "recursive archives not supported" );
          if (buf == StringConstants::EMPTY_STRING)
            throw ParseException( "'(' must be preceded by archive name" );
          arch = buf;
          buf = StringConstants::EMPTY_STRING;
          continue; // jump to next for-loop value
        case ')':
          if (arch == StringConstants::EMPTY_STRING)
            throw ParseException( "unmatched ')'" );
          if (buf == StringConstants::EMPTY_STRING)
            throw ParseException( "archive contains no members" );
          result->add( arch + StringConstants::OPEN_PAREN + buf +
              StringConstants::CLOSE_PAREN );
          arch = StringConstants::EMPTY_STRING;
          buf = StringConstants::EMPTY_STRING;
          continue; // jump to next for-loop value
        default:
          break;
      }
    }
    buf += line[i];
  }
}

/**
 * This assumes the input string was parsed by Archive::parse.
 * Output is an empty string if 'name' is not an archive
 * specification.
 *
 * Input : libc.a(file.o)
 * Output: libc.a
**/
String Archive::extractArchName( const String &name )
{
  int index = name.indexOf( StringConstants::OPEN_PAREN );
  if (index == STRING_NOTFOUND)
    return (StringConstants::EMPTY_STRING);
  return (name.substring( name.firstIndex(), index ));
}

/**
 * This assumes the input string was parsed by Archive::parse.
 * Output is an empty string if 'name' is not an archive
 * specification.
 *
 * Input : libc.a(file.o)
 * Output: file.o
**/
String Archive::extractMembName( const String &name )
{
  int index = name.indexOf( StringConstants::OPEN_PAREN );
  if (index == STRING_NOTFOUND)
    return (StringConstants::EMPTY_STRING);
  return (name.substring( index + 1, name.lastIndex() ));
}

CachedArchMember *Archive::getMemb( const String &name ) const
{
  CachedArchMember **memptr = (CachedArchMember **)members.get( name );
  if (memptr != 0)
    return (*memptr);
  return (0);
}
