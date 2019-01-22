#include "lib/util/filecach.hpp"

#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/string/strcon.hpp"
#include "lib/portable/native/file.h"
#include "lib/portable/native/dir.h"
#include "lib/portable/native/dirent.h"

#define CACHE_SOME_FILES_LIMIT 1

const int FileCache::INITIAL_CAPACITY = 100;
Hashtable< SmartCaseString, CachedDir* > FileCache::cache(
    FileCache::INITIAL_CAPACITY );

int FileCache::cache_level = CACHE_DIR_CONTENTS;
int FileCache::cache_some_files_limit = CACHE_SOME_FILES_LIMIT;

int FileCache::gets=0; // number of get() requests
int FileCache::hits=0; // number of gets that were already cached
int FileCache::max_files=0;

/**
 * Get a file from the cache.  If str is a relative path,
 * look for it in each of the paths arrays (prepending cwd
 * to the ones that are also relative), caching
 * all that it finds (but returning only the first).
**/
CachedFile *FileCache::get( const String &filepath,
    const String &cwd, const StringArray &paths, boolean force_stat,
    int find_type )
{
  // Depend on these static variables to be reinitialized later
  static SmartCaseString lookup_name;
  static const String *cur_dir;
  static CachedFile *test;
  static int i;

  // is filepath only whitespace?
  for (i = STRING_FIRST_INDEX; i <= filepath.lastIndex(); ++i)
    if (filepath[i] != ' ' && filepath[i] != '\t')
      break;

  if (i > filepath.lastIndex())
    return (0);

  ++gets;

  if (!Path::absolute( filepath )) // relative path...prepend cwd and/or paths
  {
    if (paths.length() > 0)
    {
      if (cwd.length() == 0)
        cur_dir = &(Path::getcwd());
      else
        cur_dir = &cwd;
      for (int i = paths.firstIndex(); i <= paths.lastIndex(); ++i)
      {
        if (!Path::absolute( paths[i] ))
        {
          lookup_name  = *cur_dir;
          lookup_name += Path::DIR_SEPARATOR;
          lookup_name += paths[i];
          lookup_name += Path::DIR_SEPARATOR;
          lookup_name += filepath;
        }
        else
        {
          lookup_name  = paths[i];
          lookup_name += Path::DIR_SEPARATOR;
          lookup_name += filepath;
        }

        if ((test = put( lookup_name, force_stat, find_type )) != 0)
          return (test);
      } // end for
    }
  }
  else // absolute path
  {
    lookup_name = filepath;
    return (put( lookup_name, force_stat, find_type ));
  }
			
  return (0);
}

CachedFile *FileCache::put( SmartCaseString &fullpath, boolean force_stat,
    int find_type )
{
  static CachedDir *dirresult, * const *dpresult;
  CachedFile *cfile = 0;
  static SmartCaseString pathname;
  boolean cache_nonexistent_files = false;
  boolean load_dir_cache          = false;
  Path::canonicalizeThis( fullpath, false );

  if (cache_level == CACHE_NOTHING)
    return (cacheFile( 0, fullpath, false, find_type ));

  pathname = fullpath;
  filePathThis( pathname ); // now remove the filename from the path

  if ((dpresult = cache.get( pathname )) != 0)
  {
    // The non-existent directory was cached, see if we need to stat()
    if (!(*dpresult)->doesExist())
    {
      if (force_stat)
      {
        (*dpresult)->stat();
        // Now that the directory exists, we can cache the contents as 
        // appropriate.
        if ((*dpresult)->doesExist())
          load_dir_cache = true;
        else 
          // In this case, no matter what the cache level, we know the directory
          // doesn't exist so there is no chance the file would exist.
          return (0);
      }
      else
        return (0);
    }

    // If this was a non-existent directory, only lookup files when appropriate.
    // If it is now existant, let the logic fall through to the bottom section
    // of this method where directories are originally cached.
    if (!load_dir_cache)
    {
      cache_nonexistent_files = (cache_level == CACHE_FILES ||
          (cache_level == CACHE_SOME_FILES && (*dpresult)->isCachedSome()));

      cfile = getCachedFile( *(*dpresult), fullpath, force_stat, 
                             cache_nonexistent_files, find_type );

      if (cfile == 0)
      {
        if (force_stat || cache_nonexistent_files)
        {
          cfile = cacheFile( *dpresult, fullpath, cache_nonexistent_files, 
                             find_type );
          if (cfile == 0 || !cfile->doesExist())
            return (0);
        } /* end if */
      }
      else if (cache_nonexistent_files)
      {
        if (!cfile->doesExist())
        {
          if (!force_stat)
            return (0);

          cfile->stat();

          // Now need to recheck to see if file does exist and meets the 
          // request criteria for a match.
          if (cfile->doesExist())
          {
            if (find_type == FileCache::FIND_FILES && !cfile->isFile())
              return (0);
            if (find_type == FileCache::FIND_DIRS && !cfile->isDir())
              return (0);
          } /* end if */
          else
            return (0);
        }        
      } /* end if */
      return (cfile);
    } /* end if */
  } /* end if */
  else
  {
    // If the directory was not found in the cache, put it in now.
    dirresult = new CachedDir( pathname, true );
    cache.put( *dirresult, dirresult );
  }

  // In the case of a non-existent directory or file, set the type as dir.
  if (!dirresult->doesExist() || dirresult->isFile())
    dirresult->setDir();
  else 
  {
    if (cache_level == FileCache::CACHE_DIR_CONTENTS)
    {
      cacheFilesInDir( *dirresult );
      return (getCachedFile( *dirresult, fullpath, force_stat, false, 
                             find_type ));
    }
    else if (cache_level == CACHE_SOME_FILES)
    {
      cacheFilesInDir( *dirresult );
      cfile = getCachedFile( *dirresult, fullpath, force_stat,
                             dirresult->isCachedSome(), find_type );

      // If directory was fully cached, then return result.
      if (cfile != 0 || !dirresult->isCachedSome())
        return (cfile);

      cfile = cacheFile( dirresult, fullpath, dirresult->isCachedSome(), 
                         find_type );
      if (cfile != 0 && cfile->doesExist())
        return (cfile);
    }
    else if (cache_level == FileCache::CACHE_FILES)
    {
      cfile = cacheFile( dirresult, fullpath, true, find_type );
      if (cfile != 0 && cfile->doesExist())
        return (cfile);
    }
  }

  return (0);
}

CachedFile *FileCache::cacheFile( CachedDir *cacheddir,
  const SmartCaseString &fullpath,
  boolean cache_nonexistent_files, int find_type )
{
  static CachedFile *newfile = 0; // Holds memory for use later
  CachedFile *cfile = 0;

  // If we are to force a check for non-existent files then
  // do so and cache results if the file is found.
  if (newfile == 0)
    newfile = new CachedFile( fullpath, true );
  else
    *newfile = fullpath; // calls stat automatically (rhs is a string)

  if (newfile->doesExist())
  {
    if (find_type == FileCache::FIND_FILES && !newfile->isFile())
      return (0);
    if (find_type == FileCache::FIND_DIRS && !newfile->isDir())
      return (0);
    if (cacheddir != 0)
      cacheddir->files.put( fullpath, newfile );
    cfile = newfile;
    newfile = 0;
  }
  else if (cache_nonexistent_files)
  {
    if (cacheddir != 0)
      cacheddir->files.put( fullpath, newfile );
    cfile = newfile;
    newfile = 0;
  }
  return (cfile);
}

void FileCache::printStats()
{
  int misses=gets-hits, entries=0, dir_entries=cache.size();
  float hitpercent = (gets == 0 ? 0.0 : (((float)hits/gets)*100));
  String hitpercentstr( hitpercent );
  float  misspercent = (gets == 0 ? 0.0 : (((float)misses/gets)*100));
  String misspercentstr( misspercent );
  HashElementEnumeration< SmartCaseString, CachedDir* > enumer( &cache );
  while (enumer.hasMoreElements())
    entries += (*enumer.nextElement())->files.size();

  float  entryratio = (entries == 0 ? 0.0 : ((float)hits/entries));
  String entryratiostr( entryratio );
  String cache_level_str;
  switch (cache_level)
  {
  case FileCache::CACHE_NOTHING:
    cache_level_str = "Cache nothing";
    break;
  case FileCache::CACHE_FILES:
    cache_level_str = "Cache only requested files";
    break;
  case FileCache::CACHE_SOME_FILES:
    cache_level_str = "Cache some directory contents, at most " +
                      (String)cache_some_files_limit + " files at a time";
    break;
  case FileCache::CACHE_DIR_CONTENTS:
    cache_level_str = "Cache entire directory contents (default)";
    break;
  default:
    cache_level_str = "Unknown caching level";
    break;
  }
  Interface::printAlways( "" ); // blank line
  Interface::printAlways( "Caching Level : " + cache_level_str );
  Interface::printAlways( "" ); // blank line
  Interface::printAlways( "Requests : " + (String)gets );
  Interface::printAlways( "Hits     : " + (String)hits );
  Interface::printAlways( "Misses   : " + (String)misses );
  Interface::printAlways( "Hit  %   : " + hitpercentstr + "%" );
  Interface::printAlways( "Miss %   : " + misspercentstr + "%" );
  Interface::printAlways( "" ); // blank line
  Interface::printAlways( "Cache entries (files) : " + (String)entries );
  Interface::printAlways( "Cache entries (dirs)  : " + (String)dir_entries );
  Interface::printAlways( "Max num files/dir     : " + (String)max_files );
  Interface::printAlways( "Hits per file entry   : " + entryratiostr);

}

/**
 * Just like Path::filePathThis, except in the case where
 * that function returns the empty string (i.e., the path
 * contains no directory information), this version
 * substitutes a single directory separator (in Unix format).
**/
String &FileCache::filePathThis( String &filepath )
{
  Path::filePathThis( filepath );
  if (filepath.length() == 0)
    filepath = StringConstants::FORW_SLASH;
#ifdef DEFAULT_SHELL_IS_CMD
  else if (filepath.length() == 2 &&
           isalpha( filepath[STRING_FIRST_INDEX] ) &&
           filepath[STRING_FIRST_INDEX + 1] == ':')
    filepath += Path::DIR_SEPARATOR_CHAR;
#endif
  return (filepath);
}

void FileCache::cacheFilesInDir( CachedDir &dir )
{
  static ODEDIR *dirp;
  static ODEDIRENT entry;
  static CachedFile *cfile;
  int    files_cached = 0;

  if (ODEsetcwd( dir.toCharPtr() ) != 0)
    return;
  if (dir.charAt( dir.lastIndex()) != Path::DIR_SEPARATOR_CHAR)
    dir += Path::DIR_SEPARATOR;
  dirp = ODEopendir( "." );
  while (ODEreaddir( dirp, &entry ) == 0)
  {
    if (strcmp( entry.d_name, "." ) == 0 || strcmp( entry.d_name, ".." ) == 0)
      continue;

    // If we're only caching some files, see if we hit the limit.
    // We purposely put this test prior to the actual incrementing of the 
    // counter and the first part of the loop (after the call to readdir()).  
    // This allows us to not have to setCacheSome() for directories where the
    // number of files in the directory is the same as the 
    // cache_some_files_limit.  Should save us from future calls to stat() in
    // this case.
    if (cache_level == CACHE_SOME_FILES &&
        files_cached == cache_some_files_limit)
    {
      dir.setCachedSome();
      break; // We're done caching
    }
    cfile = new CachedFile( dir, entry.d_name );
    dir.files.put( *cfile, cfile );
    ++files_cached;
#ifdef DEFAULT_SHELL_IS_VMS
    String tmpfile( entry.d_name );
    if (tmpfile.endsWith( '.' ))
    {
      tmpfile.remove( tmpfile.lastIndex(), 1 );
      cfile = new CachedFile( dir, tmpfile );
      dir.files.put( *cfile, cfile );
      ++files_cached;
    }
#endif
  }
  if (files_cached > max_files)
    max_files = files_cached;
  ODEclosedir( dirp );
  dir.remove( dir.lastIndex(), 1 ); // remove the DIR_SEPARATOR
  ODEsetcwd( Path::getcwd().toCharPtr() );
}

CachedFile *FileCache::getCachedFile( CachedDir &dir,
  const SmartCaseString &filename, boolean force_stat,
  boolean cache_nonexistent_files, int find_type )
{
  static CachedFile * const *fpresult;
  
  fpresult = dir.files.get( filename );
  if (fpresult != 0)
  {
    ++hits;
    if (!(*fpresult)->doesExist())
    {
      if (force_stat && (*fpresult)->isLink())
        (*fpresult)->stat();

      // Check to see if there non-existent files are cached and handle
      // appropriately.
      if (cache_nonexistent_files)
      {
        --hits; // Don't count this as a hit, hits are only real files
        return (*fpresult);
      }
    }
    else
    {
      if (find_type == FileCache::FIND_FILES && !(*fpresult)->isFile())
      {
        --hits;
        return (0);
      }
      if (find_type == FileCache::FIND_DIRS && !(*fpresult)->isDir())
      {
        --hits;
        return (0);
      }
      return (*fpresult); 
    }
  }

  return (0);
}
