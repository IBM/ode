#ifndef _ODE_LIB_IO_SBRCCF_HPP_
#define _ODE_LIB_IO_SBRCCF_HPP_

#include "base/odebase.hpp"
#include "lib/io/cfgf.hpp"
#include "lib/io/path.hpp"
#include "lib/portable/hashtabl.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/string.hpp"
#include "lib/string/smartstr.hpp"

/******************************************************************************
 *
 */
class SandboxRCConfigFile : public ConfigFile
{
  public:

    SandboxRCConfigFile(
        const String &path = SandboxConstants::getDEFAULT_SANDBOXRCDIR(),
        const String &filename = SandboxConstants::getSANDBOXRC_FILENAME() ) :
        ConfigFile( path + Path::DIR_SEPARATOR + filename ),
        file_info( new SandboxRCConfigFileInfo )
    {
      readAll();
    };

    SandboxRCConfigFile( const SandboxRCConfigFile &copy ) :
        ConfigFile( copy ), file_info( copy.file_info )
    {
      ++(file_info->copy_count);
    };

    virtual ~SandboxRCConfigFile()
    {
      if (file_info->copy_count <= 0)
      {
        delete file_info;
      }
      else
      {
        --(file_info->copy_count);
      }
    };

    inline boolean add( const String &sandbox, boolean is_default );
    boolean        add( const String &sandbox, const String &base_dir,
                        boolean is_default );
    boolean        del( const String &sandbox );
    inline boolean setDefaultSandbox( const String &sandbox );
    inline String  getDefaultSandbox() const;
    String         getSandboxBase( const String &sandbox = "",
                                   boolean eval = true ) const;
    inline String  getDefaultBase() const;
    StringArray    *getSandboxList( StringArray *buf = 0 ) const;
    StringArray    *getSandboxPathList( StringArray *buf = 0 ) const;
    inline boolean setMKSBList( const StringArray &list );
    StringArray    *getMKSBList( StringArray *buf = 0 ) const;
    inline boolean sandboxExists( const String &sandbox ) const;
    // getDefaultBaseDir was moved from private because mksbc needed it.
    // getDefaultBaseDir() returns unevaluated base directory.
    inline String  getDefaultBaseDir() const;


  private:

    static const String ODEDLLPORT DEFAULT_BASENAME;

    class SandboxRCConfigFileInfo
    {
      public:

        SandboxRCConfigFileInfo() : copy_count( 0 ) {};
        Vector< SmartCaseString > sandboxes;
        Vector< SmartCaseString > bases;
        Vector< String > mksblist;
        Hashtable< SmartCaseString, SmartCaseString > baselist;
        String default_sandbox;
        int copy_count;
        StringArray inputLines;
        StringArray outputLines;
    } *file_info;

    // operator= is not implemented/allowed
    SandboxRCConfigFile &operator=( const SandboxRCConfigFile &cfgf );
    void           saveDefaultToFile();
    void           saveBasesToFile();
    void           saveSandboxesToFile();
    void           saveMKSBToFile();
    boolean        rewriteFile();
    int            addSandboxToMemory( const String &sandbox, boolean findbase,
                                       boolean replace );
    boolean        addBaseToMemory( const String &base, const String &basedir,
                                    boolean replace );
    void    addMKSBToMemory( const String &str );
    int            getIndex( const String &sandbox ) const;
    void    setBases();
    inline boolean setBase( const String &sandbox );
    boolean        setBase( int index );
    inline String  findBase( const String &sandbox ) const;
    String         findBase( int index ) const;
    void           readAll();
    void           parseLine( const String &str );
    void           parseDefault( const String &str );
    boolean        parseBase( const String &str );
    void           parseSandbox( const String &str );
    inline void    parseMKSB( const String &str );
    void           verifyDefault();
    boolean        setDefaultSandbox( const String &sandbox, boolean rewrite );
    // getSandboxBaseDir() returns unevaluated
    String         getSandboxBaseDir( const String &basename ) const;
    String         normalizeBaseDir( const String &basedir ) const;
    // setSandboxBase() should take unevaluated basedir
    boolean        setSandboxBase( const String &sandbox,
                                   const String &basedir,
                                   boolean rewrite );
    boolean        setMKSBList( const StringArray &list, boolean rewrite );
    void           checkSection( boolean &doDefault, int &ix, int &rememberIx,
                                 String &sectionName, String &nextName );
    void           copyComments( int fromIx, int toIx );
    void           copyBasesForOutput( StringArray &baseLines );
    void           writeBaseLine( boolean lastOne, StringArray &baseLines,
                                  String &sandbox );
    void           writeSbLine( boolean lastOne,
                                StringArray &sbList,
                                String &sandbox );
    void           writeMksbLine( boolean lastOne,
                                  Vector< String > &mksbList,
                                  String &flag );
    boolean        isComment( String line );
};


/******************************************************************************
 *
 */
inline boolean SandboxRCConfigFile::add( const String &sandbox, boolean is_default )
{
  return ( add( sandbox, getDefaultBaseDir(), is_default ) );
}


/******************************************************************************
 *
 */
inline boolean SandboxRCConfigFile::setDefaultSandbox( const String &sandbox )
{
  return ( setDefaultSandbox( sandbox, true ) );
}


/******************************************************************************
 *
 */
inline String SandboxRCConfigFile::getDefaultSandbox() const
{
  return ( file_info->default_sandbox );
}


/******************************************************************************
 *
 */
inline String SandboxRCConfigFile::getDefaultBase() const
{
  return (getSandboxBase());
}


/******************************************************************************
 *
 */
inline boolean SandboxRCConfigFile::setMKSBList( const StringArray &list )
{
  return ( setMKSBList( list, true ) );
}


/******************************************************************************
 *
 */
inline boolean SandboxRCConfigFile::sandboxExists( const String &sandbox ) const
{
  return ( getIndex( sandbox ) >= file_info->sandboxes.firstIndex() );
}



/******************************************************************************
 *
 */
inline boolean SandboxRCConfigFile::setBase( const String &sandbox )
{
  return ( setBase( getIndex( sandbox ) ) );
}


/******************************************************************************
 *
 */
inline String SandboxRCConfigFile::findBase( const String &sandbox ) const
{
  return ( findBase( getIndex( sandbox ) ) );
}



/******************************************************************************
 *
 */
inline void SandboxRCConfigFile::parseMKSB( const String &str )
{
  addMKSBToMemory( str );
}


/******************************************************************************
 *
 */
inline String SandboxRCConfigFile::getDefaultBaseDir() const
{
  return ( getSandboxBaseDir( DEFAULT_BASENAME ) );
}


#endif //_ODE_LIB_IO_SBRCCF_HPP_
