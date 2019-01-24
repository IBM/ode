using namespace std;
/**
 * Dir
 *
**/
using namespace std;
#define _ODE_BIN_MAKE_DIR_CPP_

#include <base/binbase.hpp>
#include "lib/exceptn/ioexcept.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/util/arch.hpp"
#include "bin/make/dir.hpp"
#include "bin/make/mkcmdln.hpp"
#include "bin/make/suffix.hpp"
#include "bin/make/sufftrfm.hpp"
#include "bin/make/passnode.hpp"

void Dir::append( const String &name )
{
  static SmartCaseString tstr;

  if (name.length() == 0)
    return;

  tstr = name;
  for (int idx = path.firstIndex(); idx <= path.lastIndex(); idx++ )
  {
    // If the name already exists then don't append it
    if (tstr == path[ idx ])
      return;
  }
  path.append( name );
}

void Dir::prepend( const String &name )
{
  static StringArray tmparr1;
  static StringArray tmparr2;

  if (name.length() == 0)
    return;

  // Reinitialize static variables
  tmparr1.clear();
  tmparr2 = path;

  tmparr1.append( name );
  path = tmparr1;
  append( tmparr2 );
}

void Dir::append( const StringArray &paths )
{
  for (int idx = paths.firstIndex(); idx <= paths.lastIndex(); idx++)
    append( paths[ idx ] );
}

/**
 * Find a cached file from filecache. If str has suffix, get the Dir path
 * of this Suffix that was found from suffix transform as the search path,
 * otherwise using current Dir path as search path. If str is relative path,
 * look for it from each of the search path array( prepending passnode.cwd
 * if ones of the search path were also relative).
 * Note: caller shouldn't deallocate the returned CachedFile pointer
**/
CachedFile *Dir::findFile( const String &str, boolean force_stat,
                           boolean dirs_only )  const
{
  static StringArray tgtsplit( 3 );
  CachedFile *file;

  if (str.length() == 0)
    return (0);

  // Reinitialize static variables
  tgtsplit.clear();

  Path::fileRootSuffixThis( str, tgtsplit );

  // We may need to look at through the suffix search paths.
  // Only consider suffix paths for files, not directories.
  if (!dirs_only && (tgtsplit[ARRAY_FIRST_INDEX+1].length() > 0))
  {
    Suffix *suff = passnode->getSuffTransforms()->findSuff(
        tgtsplit[ARRAY_FIRST_INDEX+1] );
    // If a suffix search path exists then use it. Otherwise use
    // the default path.
    if (suff != 0)
      file = suff->getPath().find( tgtsplit[ARRAY_FIRST_INDEX], force_stat, dirs_only );
    else
      file = find( tgtsplit[ARRAY_FIRST_INDEX], passnode->getCwd(), force_stat, dirs_only );
  }
  else
    file = find( tgtsplit[ARRAY_FIRST_INDEX], passnode->getCwd(), force_stat, dirs_only );

  return (file);
}

/**
 * Find a file or files in a path, similarly to findFile().
 * If str does not have any wildcard characters, findfile() is used.
 * Otherwise findFilesChoosePath is called to do the rest of the work.
 * If matching file(s) were found, true is returned, and the matching
 * files are appended to StringArray matches.
**/
boolean Dir::findFilesWithPattern( const String &str,
                              StringArray &matches,
                              boolean doWildCards,
                              boolean dirs_only
                              ) const
{
  static StringArray tgtsplit2( 3 );
  String files;

  if (str.length() == 0)
    return (false);
  if (! doWildCards || str.indexOfAny("*?[") == STRING_NOTFOUND)
  {
    CachedFile *cf = findFile( str, false, dirs_only ); // check the cache
    if (cf != 0)
    {
      if (! dirs_only && ! getFindDirs() && Path::isDirectory( *cf ))
        cf = 0;
      else
        matches.append( *cf );
    }
    return (cf != 0);
  }

  // Reinitialize static variables
  tgtsplit2.clear();

  Path::fileRootSuffixThis( str, tgtsplit2 );

  return (findFilesChoosePath( tgtsplit2, matches, true, dirs_only ));
}

/**
 * Find a file or files in a path, similarly to findFile().
 * If tgt has a suffix with no wildcards, get the Dir path
 * of the Suffix that was found from suffix transform as the search path,
 * otherwise use current Dir path as search path.
 * tgt contains the file or target with wildcards that is being searched
 * for.  tgt[ARRAY_FIRST_INDEX+2] has the prefix, tgt[ARRAY_FIRST_INDEX+1]
 * has the suffix, and tgt[ARRAY_FIRST_INDEX] has prefix+suffix.
 * The value of tgt comes from splitTarget.  If tgt is a relative path,
 * look for it from each of the search path array( prepending passnode.cwd
 * if ones of the search path were also relative).
 * The return value is true if one or more matches were found.
 * The matched file(s) are appended to the StringArray matches.
**/
boolean Dir::findFilesChoosePath( const StringArray &tgt,
                                  StringArray &matches,
                                  boolean allowSuffixPath,
                                  boolean dirs_only
                                  ) const
{
  if (tgt[ARRAY_FIRST_INDEX].length() == 0)
    return (false);

  // We may need to look through the suffix search paths.
  // However, if there is a wildcard character in the suffix we will not
  // search the suffix paths.
  // Maybe later we could write code that applies pattern matching
  // to all the possible matching suffixes.  Then for each matching suffix,
  // if there was a path, we would search it.  We would append the found
  // file(s) to file, separated by blanks. But not yet...
  if (allowSuffixPath && tgt[ARRAY_FIRST_INDEX+1].length() > 0 &&
        tgt[ARRAY_FIRST_INDEX+1].indexOfAny("*?[") == STRING_NOTFOUND)
  {
    Suffix *suff = passnode->getSuffTransforms()->findSuff(
                                                tgt[ARRAY_FIRST_INDEX+1] );
    // If a suffix search path exists then use it. Otherwise use
    // the default path.
    if (suff != 0)
      return (suff->getPath().findFilesChoosePath( tgt, matches, false,
                                                   dirs_only ));
    else
      Path::findFilesInChain( tgt[ARRAY_FIRST_INDEX], path, &matches,
                              getFindDirs(), 0, dirs_only );
  }
  else
    Path::findFilesInChain( tgt[ARRAY_FIRST_INDEX], path, &matches,
                            getFindDirs(), 0, dirs_only );
  return ( matches.length() > 0 );
}

/**
 * Find a member from a archive file. First find the archive file from
 * archive cache in Archive class. Then try to find the member in this
 * archive file. If found member, return its header information as a
 * CachedArchMember. If couldn't find archive file from cache, and arch
 * has suffix, get the Dir path of this Suffix that was found from suffix
 * transform as the search path, otherwise using current Dir path as
 * search path. If arch name is relative path, look for it in each of
 * path array( prepending cwd to each ones that are also relative ),
 * and put it into cache for further quickly searching.
 * Parameter arch archive name used for searching member in it
 *           memb member name which is searched for
 * Return CachedArchMember the header information of the member
**/
CachedArchMember *Dir::findArchMemb( const String &arch,
    const String &memb ) const
{
  static StringArray tgtsplit(3);

  CachedArchMember *archmemb;

  // Reinitialize static variables
  tgtsplit.clear();

  Path::fileRootSuffixThis( arch, tgtsplit );

  // We may need to look at through the suffix search paths.
  if (tgtsplit[ARRAY_FIRST_INDEX+1].length() > 0)
  {
    Suffix *suff = passnode->getSuffTransforms()->findSuff(
        tgtsplit[ARRAY_FIRST_INDEX+1] );

    // If a suffix search path exists then use it. Otherwise use
    // the default path.
    if (suff != 0)
      archmemb = suff->getPath().findMemb( tgtsplit[ARRAY_FIRST_INDEX], memb );
    else
      archmemb = findMemb( arch, memb, passnode->getCwd());
  }
  else
    archmemb = findMemb( arch, memb, passnode->getCwd() );

  return archmemb;
}

/**
 * Find a member from a archive file. Same logic as findArchMemb(), except
 * using current Dir path as search path for archive file
**/
CachedArchMember        *Dir::findMemb( const String &arch,
                                        const String &memb,
                                        const String &cwd ) const
{
  if (MkCmdLine::dArch())
    Interface::printAlways( "Arch: finding member " +arch+"("+memb+")" );

  // first try to find archive file from archive cache
  Archive *archptr = 0;
  try
  {
    archptr = ArchiveCache::get( arch, cwd, path, MkCmdLine::dArch() );
  }
  catch (IOException &e )
  {
    if (MkCmdLine::dArch())
      Interface::printAlways( "Arch: Error finding member " + arch +
          "(" + memb + "): \"" + e.getMessage() + "\"" );
    return ( 0 );
  }
  if (MkCmdLine::dDirs())
  {
    Interface::printAlways( "Dir: cwd is " + cwd );
    Interface::printAlways( "Dir: Searching for " + arch + " in ..." );
    if (path.length() != 0)
      for ( int i=path.firstIndex(); i<=path.lastIndex(); i++ )
        Interface::printAlways( StringConstants::TAB + path[i] );

    if ( archptr != 0 )
      Interface::printAlways( "    found "+(*archptr) );
    else
      Interface::printAlways( "    not found, Sorry" );
  }

  // archive file doesn't exist
  if (archptr == 0)
    return ( 0 );

  // now try to find member header from cached archive
  CachedArchMember *cachedmemb = archptr->getMemb( memb );
  if (MkCmdLine::dDirs())
  {
    Interface::printAlways( "Dir: Searching for " + memb + " in " + arch );
    if (cachedmemb != 0)
      Interface::printAlways( "    found " + memb );
    else
      Interface::printAlways( "    not found, sorry" );
  }

  return ( cachedmemb );
}

//the caller should deallocate the returned
//StringArray pointer or passing *buf
StringArray *Dir::appendSubDirToAll( const StringArray *paths,
    const String &subdir, StringArray *buf )
{
  static String tmp;

  StringArray *dirlst = (buf==0) ? new StringArray() : buf;

  // If there isn't a default then just add the relative path.
  if (paths == 0 || paths->length() == 0 || Path::absolute( subdir ))
  {
    dirlst->append( subdir );
  }
  else
  {
    for ( int i=paths->firstIndex(); i<=paths->lastIndex(); i++ )
    {
      tmp = (*paths)[i];
      tmp += StringConstants::FORW_SLASH;
      tmp += subdir;
      dirlst->append( Path::unixizeThis(
          Path::canonicalizeThis( tmp, false ) ) );
    }
  }
  return (dirlst);
}

void Dir::printDebug( const String &name, const String &cwd,
    CachedFile* res ) const
{
    Interface::printAlways( "Dir: cwd is "+cwd );
    Interface::printAlways( "Dir: Searching for "+name+" in ..." );
    if (path.length() != 0)
      for ( int i=path.firstIndex(); i<=path.lastIndex(); i++ )
        Interface::printAlways( StringConstants::TAB + path[i] );

    if (res != 0)
      Interface::printAlways( "    found "+ res->getUnixPath() );
    else
      Interface::printAlways( "    not found, Sorry" );
}


