/**
 * Dir
 *
**/
#ifndef _ODE_BIN_MAKE_DIR_HPP_
#define _ODE_BIN_MAKE_DIR_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/strfind.hpp"
#include "lib/io/path.hpp"
#include "lib/util/cachfile.hpp"
#include "lib/util/filecach.hpp"

#include "bin/make/mkcmdln.hpp"
#include "bin/make/passinst.hpp"

class CachedArchMember;

class Dir : public StringFindable
{
  public:
    // constructor and destructor
    Dir() : passnode( 0 ) 
    {
      setFindDirs( MkCmdLine::findDirs() );
    };
    Dir( const StringArray &path, PassInstance *passnode = 0 ) :
        passnode( passnode )
    {
      append( path );
      setFindDirs( MkCmdLine::findDirs() );
    };
    Dir( const StringArray *path, PassInstance *passnode = 0) :
        passnode( passnode )
    {
      if (path !=0)
        append( *path );
      setFindDirs( MkCmdLine::findDirs() );
    };
    Dir( const Dir &rhs ) :
        passnode( rhs.passnode ), path( rhs.path )
    {
      setFindDirs( MkCmdLine::findDirs() );
    };

    ~Dir(){};

   inline Dir &operator=( const Dir &rhs )
   {
     if (this == &rhs) return *this;

     passnode = rhs.passnode;
     path = rhs.path;
     return *this;
   };

   // Search path utilities.
   //
          void                  append( const String &name );
          void                  prepend( const String &name );
          void                  append( const StringArray &paths );
   inline void                  append( const Dir &paths );
   /**
    * Find a cached file from filecache. If str has suffix, get the Dir path
    * of this Suffix that was found from suffix transform as the search path,
    * otherwise using current Dir path as search path. If str is relative path,
    * look for it from each of the search path array( prepending passnode.cwd
    * if ones of the search path were also relative).
    * Note: caller shouldn't deallocate the returned CachedFile pointer
   **/
          CachedFile           *findFile( const String &str,
                                          boolean force_stat=false,
                                          boolean dirs_only=false ) const;

   /**
    * Find a file or files in a path, similarly to findFile(). 
    * If str does not have any wildcard characters, findfile() is used. 
    * Otherwise findFilesChoosePath is called to do the rest of the work.
    * If matching file(s) were found, true is returned, and the matching
    * files are appended to StringArray matches.
   **/
          boolean findFilesWithPattern( const String &str,
                                        StringArray &matches,
                                        boolean doWildCards,
                                        boolean dirs_only
                                        ) const;

    /**
     * Find a file or files in a path, similarly to findFile(). 
     * If tgt has a suffix with no wildcards, get the Dir path
     * of the Suffix that was found from suffix transform as the search path,
     * otherwise use current Dir path as search path. If tgt is a relative path,
     * look for it from each of the search path array( prepending passnode.cwd
     * if ones of the search path were also relative).
     * The return value is true if one or more matches were found.
     * The matched file(s) are appended to the StringArray matches.
    **/
          boolean findFilesChoosePath( const StringArray &tgt,
                                       StringArray &matches,
                                       boolean allowSuffixPath,
                                       boolean dirs_only
                                       ) const;

   inline boolean stringFinder( const String &str,
                                StringArray &matches,
                                boolean doWildCards,
                                boolean dirs_only ) const;

   /**
    * Find a cached file from FileCache. if name is relative path,
    * look for it in each of path array( prepending passnode.cwd to
    * each ones that are also relative).
    * return non-zero the cached file found, zero finding failed.
    * Note: The caller shouldn't deallocate the returned CachedFile pointer
    **/
   inline CachedFile           *find( const String &name, const String &cwd,
                                      boolean force_stat=false,
                                      boolean dirs_only=false ) const;

   /**
    * Find a cached file from FileCache. if name is relative path,
    * look for it in each of path array( prepending cwd to each ones
    * that are also relative).
    * return non-zero the cached file found, zero finding failed.
    * Note: The caller shouldn't deallocate the returned CachedFile pointer
   **/
   inline CachedFile           *find( const String &name,
                                      boolean force_stat=false,
                                      boolean dirs_only=false ) const;

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
    * Note: The calller shouldn't deallocate the returned pointer
   **/
   CachedArchMember             *findArchMemb( const String &arch,
                                               const String &memb ) const;

   /**
    * Find a member from a archive file. Same logic as findArchMemb(), except
    * using current Dir path as search path for archive file
   **/
   inline CachedArchMember      *findMemb( const String &arch,
                                           const String &memb ) const;
   /**
    * Find a member from a archive file. Same logic as findMemb(), except
    * using cwd instead of passnode->cwd
   **/
   CachedArchMember             *findMemb( const String &arch,
                                           const String &memb,
                                           const String &cwd ) const;
   /**
    * This method will append a subdirectory name to the default search path.  For example,
    * if the default search path is searchpath[0]=/proj/obj/rios_aix_4 and searchpath[1]=/proj/src
    * and the subdirectory 'x' is to be added the result will be dirlst[0]=
    * /proj/obj/rios_aix_4/x and dirlst[1]=/proj/src/x
    * the caller should deallocate the returned StringArray pointer or passing *buf
   **/
   inline        StringArray    *appendSubDirToDefaultSearchPath( const String &subdir,
                                                        StringArray *buf=0 );

   //the caller should deallocate the returned
   //StringArray pointer or passing *buf
          static StringArray    *appendSubDirToAll( const StringArray *paths,
                                  const String &subdir, StringArray *buf=0 );

   inline void                  setPath( const StringArray *newpath );
   inline const StringArray    *getPath() const;
   inline const StringArray    *getDefaultPath() const;
   inline       String          getCwd() const;

  private:
    PassInstance *passnode;
    StringArray path;

                void            printDebug( const String &name,
                                  const String &cwd,
                                  CachedFile* res ) const;

};

inline boolean Dir::stringFinder( const String &str,
                                  StringArray &matches,
                                  boolean doWildCards,
                                  boolean dirs_only ) const
{
  String tmpstr = str.trim();
  if (tmpstr.length() ==0)
    return false;

  return (findFilesWithPattern( tmpstr, matches, doWildCards, dirs_only ));
}

/**
 * Find a cached file from FileCache. if name is relative path,
 * look for it in each of path array( prepending cwd to each ones
 * that are also relative).
 * return non-zero the cached file found,
 *        zero finding fail.
**/
inline CachedFile *Dir::find( const String &name, const String &cwd,
    boolean force_stat, boolean dirs_only ) const
{
  CachedFile *res;

  if( dirs_only )
    res = FileCache::get( name, cwd, path, force_stat,
                          FileCache::FIND_DIRS );
  else
    res = FileCache::get( name, cwd, path, force_stat,
                          (getFindDirs() ?
                          FileCache::FIND_BOTH : FileCache::FIND_FILES) );

  if (MkCmdLine::dDirs())
    printDebug( name, cwd, res );

  return (res);
}

inline CachedFile *Dir::find( const String &name,
                              boolean force_stat,
                              boolean dirs_only ) const
{
  if (passnode == 0 || passnode->getCwd().length() == 0)
    return (0);
  return (find( name, passnode->getCwd(), force_stat, dirs_only ));
}


inline void Dir::setPath( const StringArray *newpath )
{
  if (newpath ==0 )
    path.clear();
  else
    path = *newpath;
}

inline const StringArray *Dir::getPath() const
{
  if (path.length() == 0)
    return 0;

  return &path;
}

inline const StringArray *Dir::getDefaultPath()  const
{
  return passnode->getDefaultSearchPath();
}

inline void Dir::append( const Dir &paths )
{
  const StringArray *tmppaths = paths.getPath();
  if (tmppaths != 0)
    append( *tmppaths );
}

inline String Dir::getCwd() const
{
  return passnode->getCwd();
}

inline CachedArchMember *Dir::findMemb( const String &arch,
                                        const String &memb ) const
{
  if (passnode == 0 || passnode->getCwd().length() ==0)
    return 0;
  return findMemb( arch, memb, passnode->getCwd());
}

/**
 * This method will append a subdirectory name to the default search path.
 * For example, if the default search path is
 * searchpath[0]=/proj/obj/rios_aix_4 and searchpath[1]=/proj/src
 * and the subdirectory 'x' is to be added the result will be
 * dirlst[0]=/proj/obj/rios_aix_4/x and dirlst[1]=/proj/src/x
 * the caller should deallocate the returned StringArray pointer or
 * passing *buf
**/
inline StringArray *Dir::appendSubDirToDefaultSearchPath( const String &subdir,
                                                   StringArray *buf )
{
  if (subdir.length() == 0)
    return (0);

  return (Dir::appendSubDirToAll( passnode->getDefaultSearchPath(), subdir,
            ((buf == 0) ? new StringArray() : buf) ));
}


#endif //_ODE_BIN_MAKE_DIR_HPP_
