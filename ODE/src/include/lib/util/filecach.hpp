/**
 * File Cache
 * Purpose:
 *  The main purpose of the FileCache class is a way to provide a "fast"
 *  file retrieval.
 *  The first level of caching is actual directories.  The directory
 *  entries have hash tables of files in each directory.  If a
 *  file is not found in the directory's hash table then it is
 *  assumed that the file doesn't exist.
**/
#ifndef _ODE_LIB_UTIL_FILECACH_HPP_
#define _ODE_LIB_UTIL_FILECACH_HPP_


#include <base/odebase.hpp>
#include "lib/string/strarray.hpp"
#include "lib/util/cachfile.hpp"
#include "lib/util/cachdir.hpp"
#include "lib/portable/hashtabl.hpp"

class FileCache
{
  public:

    // WARNING: currently, only CACHE_DIR_CONTENTS is implemented
    enum { CACHE_NOTHING = 0, CACHE_FILES = 1, 
           CACHE_SOME_FILES = 2, CACHE_DIR_CONTENTS = 3};
    enum { FIND_FILES = 0, FIND_DIRS = 1, FIND_BOTH = 2 };
    static int ODEDLLPORT cache_level;
    static int ODEDLLPORT cache_some_files_limit;

    /**
     * Get a file from the cache.  If str is a relative path,
     * look for it in each of the paths arrays (prepending cwd
     * to the ones that are also relative), caching
     * all that it finds (but returning only the first).
     * If force_stat is true, then if the file in the cache is not
     * found, it is inserted into the cache.
    **/
    static CachedFile* get( const String &str, const String &cwd = "",
        const StringArray &paths = StringArray(), boolean force_stat = false,
        int find_type = FileCache::FIND_FILES );

   /**
     * Set the caching level.
     * @param level The new level
    **/
    inline static void setCacheLevel( int level );
    inline static void setCacheSomeFilesLimit( int limit );
    inline static int  getCacheSomeFilesLimit( );
    static void printStats();


  private:

    static const int ODEDLLPORT INITIAL_CAPACITY;
    static Hashtable< SmartCaseString, CachedDir* > ODEDLLPORT cache;
    static int ODEDLLPORT gets; // number of get() requests
    static int ODEDLLPORT hits; // number of gets that were already cached
    static int ODEDLLPORT max_files;

    // WARNING: put() modifies fullpath for performance reasons
    static CachedFile* put( SmartCaseString &fullpath, boolean force_stat,
        int find_type );
    static String &filePathThis( String &path );
    static void cacheFilesInDir( CachedDir &dir );
    static CachedFile *getCachedFile( CachedDir &dir,
        const SmartCaseString &filename, boolean force_stat, 
        boolean cache_nonexistent_files, int find_type );
    static CachedFile *cacheFile( CachedDir *cacheddir,
        const SmartCaseString &filename, boolean cache_nonexistent_files,
        int find_type );
};

/**
 * Set the caching level.
 * @param level The new level
**/
inline void FileCache::setCacheLevel( int level )
{
  if (level >= FileCache::CACHE_NOTHING &&
      level <= FileCache::CACHE_DIR_CONTENTS)
    cache_level = level;
}

inline void FileCache::setCacheSomeFilesLimit( int limit )
{
  // Nothing other than 1 makes any sense.  We could always switch
  // the cache level to CACHE_NOTHING but this may not be the users
  // desired/expected result.
  if (limit < 1)
    cache_some_files_limit = 1;
  else
    cache_some_files_limit = limit;
}

inline int FileCache::getCacheSomeFilesLimit()
{
  return (cache_some_files_limit);
}
#endif /* _ODE_LIB_UTIL_FILECACH_HPP_ */
