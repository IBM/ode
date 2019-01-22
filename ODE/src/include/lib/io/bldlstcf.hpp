#ifndef _ODE_LIB_IO_BLDLSTCF_HPP_
#define _ODE_LIB_IO_BLDLSTCF_HPP_

#include <base/odebase.hpp>
#include "lib/io/cfgf.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/portable/vector.hpp"


class BuildListConfigFile : public ConfigFile
{
  public:
  
    BuildListConfigFile( const String &pathname ) :
        ConfigFile( pathname ),
        file_info( new BuildListConfigFileInfo )
    {
      readAll();
    };
    
    BuildListConfigFile( const BuildListConfigFile &copy ) :
        ConfigFile( copy ), file_info( copy.file_info )
    {
      ++(file_info->copy_count);
    };
    
    virtual ~BuildListConfigFile()
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

    boolean     add( const String &build, const String &builddir,
                     boolean replace, boolean is_default );
    boolean     del( const String &build );
    boolean     inBuildList( const String &sandbox );
    StringArray *getBuildList( StringArray *buf = 0 ) const;
    String      getBuildDir( const String &build ) const;
    boolean     setDefaultBuild( const String &build );


  private:

    class BuildListConfigFileInfo
    {
      public:

        BuildListConfigFileInfo() : copy_count( 0 ) {};
        Vector< SmartCaseString > builds;
        Vector< SmartCaseString > builddirs;
        int copy_count;
    } *file_info;

    // operator= is not implemented/allowed
    BuildListConfigFile &operator=( const BuildListConfigFile &cfgf );
    int     addToMemory( const String &build, const String &builddir,
                         boolean replace, boolean is_default,
                         boolean append );
    boolean saveToFile( int index, boolean append );
    boolean rewriteFile();
    int     getIndex( const String &build ) const;
    void    readAll();
    void    parseLine( const String &str );
};

#endif // _ODE_LIB_IO_BLDLSTCF_HPP_
