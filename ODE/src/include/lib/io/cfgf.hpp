#ifndef _ODE_LIB_IO_CFGF_HPP_
#define _ODE_LIB_IO_CFGF_HPP_

#include <fstream.h>

#include <base/odebase.hpp>
#include "lib/io/path.hpp"
#include "lib/portable/vector.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/exceptn/ioexcept.hpp"
#include "lib/util/finforep.hpp"
#ifdef __WEBMAKE__
#include "lib/util/condeval.hpp"
#endif


/**
 * This class defines basic config file behavior.
 *
 * When using putLine() in overwrite mode, i.e.
 * putLine( str, false );, care must be taken to avoid
 * unwanted results.  If there are intervening calls to
 * getLine() or append-mode putLine(), the next overwrite-mode
 * putLine() will restart at the beginning of the file.  To
 * rewrite a file, it is sufficient to call putLine() in
 * overwrite mode a single time, and then use append mode for
 * all successive calls.
 *
 * When using putLine in append mode, putLine()'s and
 * getLine()'s may be mixed freely with no ill effects
 * (without requiring calls to close()).
 */

class ConfigFile : public FileInfoReportable
{
  public:

    inline ConfigFile( const String &path, boolean auto_flush = true ) :
        pathname( path ), pathdir( path ), // pathdir is modified below
        auto_flush( auto_flush ), file_info( new ConfigFileInfo )
    {
      Path::filePathThis( pathdir );
    }

    inline ConfigFile( const ConfigFile &copy ) :
        pathname( copy.pathname ), pathdir( copy.pathdir ),
        auto_flush( copy.auto_flush ),
        file_info( copy.file_info )
    {
      ++(file_info->copy_count);
    }

    virtual ~ConfigFile();

    // Implement FileInfoReportable
    virtual const String &getPathname() const;
    virtual int getLineNumber() const; // same as getLastLineNumber()

    inline int getLastLineNumber() const;
    inline int getNextLineNumber() const;
    void close();
    inline void flush();
    inline boolean remove();
    inline boolean purge();
    inline boolean exists() const;
    boolean putLine( const String &line );
    inline boolean putLine( const String &line, boolean append );
#ifdef __WEBMAKE__
    String *getLine( CondEvaluator *condeval,boolean trim = true,
        boolean backslash_escape = false, // IGNORED
        String *buf = 0, boolean ignore_comment =true);
#endif
    String *getLine( boolean trim = true,
                     boolean backslash_escape = false, // IGNORED
                     String *buf = 0,
                     boolean ignore_comment = true,
                     boolean replCont = true );
    static boolean stripComment( String &str );


  private:

#ifdef __WEBMAKE__
    const static String ODEDLLPORT DOT_IFDEF;
    const static String ODEDLLPORT DOT_IFNDEF;
    const static String ODEDLLPORT DOT_IF;
    const static String ODEDLLPORT DOT_ELSE;
    const static String ODEDLLPORT DOT_ELIFDEF;
    const static String ODEDLLPORT DOT_ELIFNDEF;
    const static String ODEDLLPORT DOT_ELIF;
    const static String ODEDLLPORT DOT_ENDIF;
#endif

    const static char ODEDLLPORT MODE_READONLY;
    const static char ODEDLLPORT MODE_APPEND;
    const static char ODEDLLPORT MODE_OVERWRITE;
    const String pathname;
    String pathdir;
    const boolean auto_flush;

    class ConfigFileInfo
    {
      public:

        ConfigFileInfo() :
            current_mode( '\0' ), lastline( 0 ), nextline( 1 ),
            fdr( 0 ), fdw( 0 ), copy_count( 0 ) {};

        char current_mode;
        int lastline;
        int nextline;
        ifstream *fdr;
        fstream *fdw;
        int copy_count;
    } *file_info;

    // operator= is not implemented/allowed
    ConfigFile &operator=( const ConfigFile &cfgf );
    boolean putLineForMode( const String &line, char mode );
    void open( char mode );
    boolean reopen( int mode );
    static boolean cleanLine( String &str, boolean trim,
                              boolean replCont = true );
    static boolean isCommentLine( const String &str );
};


/******************************************************************************
 * Get the [first] line number of the last line that was read in
 * via getLine()/getLineArray().  Remember that each call to
 * getLine() may cause the
 * line number to increase by more than one.  This is because
 * getLine() automatically skips comment lines and also joins
 * together continuation lines (so for continuation lines, this
 * method will return the line number of the first line that
 * started the complete line [the last line of it will be
 * getNextLineNumber() - 1]).
 *
 * @return The line number that was just read in.
 * Always returns zero if the config file is currently
 * opened in write mode (i.e., if the last call was to
 * putLine()).
 */
inline int ConfigFile::getLastLineNumber() const
{
  return( file_info->lastline );
}


/******************************************************************************
 * Get the line number of the next line that will be read
 * by getLine()/getLineArray().  This may not be the same
 * as what getLastLineNumber() will be after the next
 * call to getLine()/getLineArray().  This is because the
 * next line may be a comment line (which is skipped).
 *
 * @return The line number that is about to be read in.
 * Always returns zero if the config file is currently
 * opened in write mode (i.e., if the last call was to
 * putLine()).  If no get/put lines have been called since
 * this object was constructed, the return value is
 * meaningless.
 */
inline int ConfigFile::getNextLineNumber() const
{
  return( file_info->nextline );
}


/******************************************************************************
 * See if the config file exists.
 *
 * @return True if the file exists, false if not.
 */
inline boolean ConfigFile::exists() const
{
  return ( Path::exists( pathname ) );
}


/**
 * Flush the write buffer.  This is only necessary if
 * the object was instantiated with flush=false.
**/
inline void ConfigFile::flush()
{
  if (file_info->fdw != 0)
    file_info->fdw->flush();
}


/**
 * Delete the config file.
 *
 * @return True if the config file was deleted (or
 * didn't exist to begin with).  False if it could
 * not be deleted.
**/
inline boolean ConfigFile::remove()
{
  this->close();
  return (Path::deletePath( pathname ));
}


/**
 * Empty, but do not delete, the config file.  This results
 * in a file of zero length.
 *
 * @return True if the config file was emptied.  False if not.
**/
inline boolean ConfigFile::purge()
{
  boolean rc = reopen( MODE_OVERWRITE );
  this->close();
  return (rc);
}

/**
 * Write a line to the config file.
 *
 * @param line The string to output.  A newline will be
 * added to this string.
 * @param append If true, the line will be appended to the
 * end of the file.  If false, the line will be added in
 * overwrite mode (if previous writes to the file were done
 * in overwrite mode, the line will be added after the last
 * line that was written; otherwise, the line will begin
 * overwriting at the beginning of the file again).
 * @return True on success, false on failure.
**/
inline boolean ConfigFile::putLine( const String &line, boolean append )
{
  return (putLineForMode( line, (append) ? MODE_APPEND : MODE_OVERWRITE ));
}

#endif // _ODE_LIB_IO_CFGF_HPP_

