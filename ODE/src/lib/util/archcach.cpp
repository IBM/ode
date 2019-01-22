#define _ODE_LIB_UTIL_ARCHCACH_CPP_
#include "lib/string/strcon.hpp"
#include "lib/util/archcach.hpp"
#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"

Hashtable< SmartCaseString, Archive * > ArchiveCache::cache( 53 );

/**
 * While searching 'paths', the first valid archive found is
 * returned (all others are cached).
**/
Archive *ArchiveCache::get( const String &name, const String &cwd,
    const StringArray &paths, boolean output_header_msg )
{
  Archive *result = 0, *tmp, **rc;
  StringArray fullpaths;
  int first_good = ELEMENT_NOTFOUND;

  if (formPaths( name, cwd, paths, &fullpaths ) == 0 ||
      fullpaths.length() < 1)
    return (0);
  for (int i = fullpaths.firstIndex(); i <= fullpaths.lastIndex(); ++i)
  {
    if ((rc = (Archive **)cache.get( fullpaths[i] )) == 0)
    {
      if (Path::exists( fullpaths[i] ))
      {
        tmp = new Archive( fullpaths[i] );

        if (tmp->isValid())
        {
          if (output_header_msg)
            Interface::printAlways( "Arch: " + fullpaths[i] + " header OK" );
          if (first_good == ELEMENT_NOTFOUND)
            first_good = i;
        }
        else if (output_header_msg)
          Interface::printAlways( "Arch: " + fullpaths[i] + " bad header" );

        cache.put( fullpaths[i], tmp );
      }
    }
    else if (result == 0 && (*rc)->isValid())
      result = *rc;
  }

  if (result == 0 && first_good != ELEMENT_NOTFOUND)
    if ((rc = (Archive **)cache.get( fullpaths[first_good] )) != 0)
      result = *rc;

  return (result);
}

StringArray *ArchiveCache::formPaths( const String &name, const String &cwd,
    const StringArray &paths, StringArray *buf )
{
  if (name == StringConstants::EMPTY_STRING)
    return 0;

  String filepath;
  String curdir = (cwd == StringConstants::EMPTY_STRING) ? Path::getcwd() : cwd;
  StringArray *result = (buf == 0) ? new StringArray() : buf;

  filepath = name;
  Path::normalizeThis( filepath );
  if (!Path::absolute( filepath ))
  {
    if (paths.length() < 1)
    {
      if (result != buf)
        delete result;
      return 0;
    }
    for (int i = paths.firstIndex(); i <= paths.lastIndex(); ++i)
    {
      if (!Path::absolute( Path::normalize( paths[i] ) ))
        result->add( Path::canonicalize( curdir + Path::DIR_SEPARATOR +
            paths[i] + Path::DIR_SEPARATOR + filepath ) );
      else
        result->add( Path::canonicalize( paths[i] + Path::DIR_SEPARATOR +
            filepath ) );
    }
  }
  else
    result->add( filepath );

  return (result);
}
