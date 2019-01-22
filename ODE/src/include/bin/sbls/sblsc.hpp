#ifndef _ODE_BIN_SBLS_SBLS_HPP
#define _ODE_BIN_SBLS_SBLS_HPP


#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/io/cmdline.hpp"
#include "lib/io/path.hpp"
#include "lib/portable/stack.hpp"
#include "lib/portable/vector.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/string.hpp"


class DirEntry;

class Sbls : public Tool
{
  public:
    Sbls( const char **argv, const char **envp )
        : Tool( envp ), args( argv ), cmdLine( 0 ) {};

    static int classMain( const char **argv, const char **envp );
    int        run();
    void       printUsage() const;

    ~Sbls() {};

  private:
    const char      **args;
    CommandLine     *cmdLine;
    StringArray     backingChain;
    Stack< String > dirStack;

    void    checkCommandLine();
    boolean processInputs( const StringArray &inputs );
    void    getBackingChain();
    boolean listInput( const String &ip );
    boolean checkIfDir( const String &ip );
    String  getSbPath( const String &path );
    boolean display( boolean firstTime );
    void    getFiles( const String &path, const String &pattern,
        Vector< DirEntry > &buffer, boolean inSb = true, int chain_level = 0 );
    void    addAncestors( const Vector< DirEntry > &ancestors,
                          Vector< DirEntry > &total );
    void    addToStack( const String &path,
                        const Vector< DirEntry > &dir_conts );
    void    print( const String &path, const Vector< DirEntry > &dir_contents );
};


/******************************************************************************
 *
 */
class DirEntry
{
  public:

    DirEntry() {};

    DirEntry( const String &new_entry, const String &new_path,
        boolean inSb = true, int chain_level = 0 ) :
        entry( new_entry ), path( new_path ), foundInSb( inSb ),
        chain_level( chain_level ) {};

    /**
     */
    inline void addEntry( const String &new_entry, const String &new_path,
        boolean inSb, int chain_level )
    {
      this->entry = new_entry;
      this->path = new_path;
      this->foundInSb = inSb;
      this->chain_level = chain_level;
    };

    /**
     */
    inline SmartCaseString getEntry() const { return entry; };
    inline SmartCaseString getPath() const { return path; };
    inline boolean         isInSb() const { return foundInSb; };

    /**
     */
    inline boolean isDirectory() const
    {
      return Path::isDirectory( path + Path::DIR_SEPARATOR + entry );
    };

    /**
     */
    inline boolean isLink() const
    {
      return Path::isLink( path + Path::DIR_SEPARATOR + entry );
    };

    /**
     */
    inline String fullEntry() const
    {
      return (path + Path::DIR_SEPARATOR + entry);
    };

    /**
     */
    inline String getTime() const
    {
      time_t file_time = (time_t)Path::lastModified( fullEntry() );
      String mtime( ctime( &file_time ) );
      return( mtime.substring( mtime.firstIndex() + 4, mtime.length() ) );
    };

    /**
     */
    inline String getSize() const
    {
      String size( (int)Path::size( fullEntry() ) );
      size.rightJustify( 9 );
      return size;
    };

    inline int getChainLevel() const
    {
      return (chain_level);
    }

    /**
     */
    inline boolean operator==( const DirEntry &compareTo ) const
    {
      return (this->entry == compareTo.entry && this->path == compareTo.path);
    }


  private:

    SmartCaseString entry;
    SmartCaseString path;
    boolean         foundInSb;
    int chain_level;
};


#endif //_ODE_BIN_SBINFO_SBINFO_HPP
