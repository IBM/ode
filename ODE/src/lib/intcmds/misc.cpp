#include <base/binbase.hpp>
#include "lib/intcmds/misc.hpp"

#include "lib/string/smartstr.hpp"
#include "lib/io/path.hpp"


void Misc::copyArrayToVector( const StringArray &src, Vector<String> &dest )
{
  if (src.length() == 0) return;
  for (int i=src.firstIndex(); i<=src.lastIndex(); i++)
    dest.addElement( SmartCaseString( src[i] ) );
}
  

boolean Misc::stringStartsWith( const String &str, const String &prefix )
{
  if (str.length() == 0 || prefix.length() == 0)
      return false;
  return (SmartCaseString( str ).startsWith( prefix ));    
}


boolean Misc::stringEndsWith( const String &str, const String &suffix )
{
  if (str.length() == 0 || suffix.length() == 0)
    return false;
  return (SmartCaseString( str ).endsWith( suffix ));    
}
  

void Misc::normalizePaths( StringArray &paths )
{
  for (int i=paths.firstIndex(); i<=paths.lastIndex(); i++)
    Path::normalizeThis( paths[i] );
}
