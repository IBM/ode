#ifndef _ODE_LIB_IO_SETVARCF_HPP_
#define _ODE_LIB_IO_SETVARCF_HPP_

#include <base/odebase.hpp>
#include "lib/io/cfgf.hpp"
#include "lib/portable/env.hpp"
#include "lib/string/setvars.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/string.hpp"
#include "lib/string/svarlink.hpp"

class SetVarConfigFile; // so SetVarConfigFileData can declare it a friend

class SetVarConfigFileData
{
  public:

    boolean unset;          // if true, variable has no value
    String value;           // the variable's value
    boolean env;            // is the "setenv" keyword used?
    boolean replace;        // is the "replace" keyword used?
    String machine;         // if not an empty string, the following...
    boolean use_on_keyword; // ...is true if "on machine", else "for machine"

    // constructor for convenience
    SetVarConfigFileData( const String &value = "",
        boolean env = false, boolean replace = false, boolean unset = false,
        const String &machine = "", boolean use_on_keyword = true ) :
        unset( unset ), value( value ), env( env ), replace( replace ),
        machine( machine ), use_on_keyword( use_on_keyword ),
        on_machine( true )
    {
    }

    // this is so string-oriented functions in SetVarsTemplate
    // think we're just an ordinary value.
    inline char *toCharPtr() const
    {
      return (value.toCharPtr());
    }
  
    SetVarConfigFileData &operator=( const SetVarConfigFileData &copy )
    {
      if (&copy != this)
      {
        unset = copy.unset;
        value =copy.value;
        replace = copy.replace;
        env = copy.env;
        machine = copy.machine;
        use_on_keyword = copy.use_on_keyword;
        on_machine = copy.on_machine;
      }
      return (*this);
    }

    boolean operator==( const SetVarConfigFileData &cmp ) const
    {
      return (unset == cmp.unset && replace == cmp.replace &&
          env == cmp.env && use_on_keyword == cmp.use_on_keyword &&
          machine == cmp.machine && value == cmp.value);
    }


  private:

    boolean on_machine; // false if machine isn't empty AND not on that machine

  friend class SetVarConfigFile;
};

/******************************************************************************
 * Reads and writes files that contain
 * set/setenv variables.
 */
class SetVarConfigFile : public ConfigFile
{
  public:

    SetVarConfigFile( const String &pathname,
        SetVars &save_vars, const SetVarBase *find_vars = 0 ) :
        ConfigFile( pathname ),
        file_info( new SetVarConfigFileInfo( save_vars, find_vars ) ),
        nest_level( 0 )
    {
      readAll();
    };

    SetVarConfigFile( const SetVarConfigFile &copy ) :
        ConfigFile( copy ), file_info( copy.file_info ),
        nest_level( 0 )
    {
      ++(file_info->copy_count);
    };
    
    virtual ~SetVarConfigFile()
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

    inline SetVars &getLocalVars( SetVars &buffer,
        boolean replace = true ) const;
    inline SetVarsTemplate< SetVarConfigFileData > &getLocalVars(
        SetVarsTemplate< SetVarConfigFileData > &buffer,
        boolean replace = true ) const;
    inline SetVars &getGlobalVars( SetVars &buffer,
        boolean replace = true ) const;
    inline SetVarsTemplate< SetVarConfigFileData > &getGlobalVars(
        SetVarsTemplate< SetVarConfigFileData > &buffer,
        boolean replace = true ) const;
    boolean change( const String &var, const SetVarConfigFileData &data );


  private:

    class SetVarConfigFileInfo
    {
      public:

        SetVarConfigFileInfo( SetVars &save_vars,
            const SetVarBase *find_vars = 0 ) :
            global_vars( 0, PlatformConstants::onCaseSensitiveOS() ),
            local_vars( &global_vars ), save_vars( save_vars ),
            find_vars( find_vars ), copy_count( 0 ) {};
        SetVarsTemplate< SetVarConfigFileData > global_vars;
        SetVarsTemplate< SetVarConfigFileData > local_vars;
        SetVars &save_vars;
        const SetVarBase *find_vars;
        int copy_count;
        StringArray lines;
    } *file_info;

    boolean nest_level; // to detect "include" depth out-of-range

    static const String ODEDLLPORT ON_KEYWORD;
    static const String ODEDLLPORT FOR_KEYWORD;
    static const String ODEDLLPORT SET_KEYWORD;
    static const String ODEDLLPORT UNSET_KEYWORD;
    static const String ODEDLLPORT SETENV_KEYWORD;
    static const String ODEDLLPORT UNSETENV_KEYWORD;
    static const String ODEDLLPORT REPLACE_KEYWORD;
    static const String ODEDLLPORT INCLUDE_KEYWORD;

    SetVarConfigFile( const String &pathname,
        SetVars &save_vars, const SetVarBase *find_vars,
        int nest_level ) :
        ConfigFile( pathname ),
        file_info( new SetVarConfigFileInfo( save_vars, find_vars ) ),
        nest_level( nest_level )
    {
      readAll();
    };

    // operator= is not implemented/allowed
    SetVarConfigFile &operator=( const SetVarConfigFile &cfgf );
    SetVarsTemplate< SetVarConfigFileData > &getVars(
        SetVarsTemplate< SetVarConfigFileData > &buffer,
        boolean local, boolean replace ) const;
    SetVars &getVars( SetVars &buffer, boolean local, boolean replace ) const;
    String      quoteIfNeeded( const String &value ) const;
    boolean     writeLineToFile( const String &var,
        const SetVarConfigFileData *data, boolean append );
    boolean     rewriteFile();
    void        readAll();
    void        parseLine( const String &str );
    boolean     parseInclude( const String &str );
    boolean     parseOnOrFor( const String &str, SetVarConfigFileData &data,
        boolean is_on_keyword );
    boolean     isOnOrForMachine( const String &machine_name,
        boolean is_on_keyword ) const;
    boolean     parseReplace( const String &str, SetVarConfigFileData &data );
    boolean     parseUnset( const String &str, SetVarConfigFileData &data );
    boolean     parseUnsetenv( const String &str, SetVarConfigFileData &data );
    boolean     parseSetenv( const String &str, SetVarConfigFileData &data );
    boolean     parseSetenv( const String &str, const String &val,
        SetVarConfigFileData &data );
    boolean     parseSet( const String &str, SetVarConfigFileData &data );
    boolean     parseSet( const String &str, const String &val,
        SetVarConfigFileData &data );
    StringArray *parseValueUntil( const String &value,
        const String &until_chars, StringArray *buffer = 0 ) const;
    void printParseWarning( const String &str ) const;
    boolean separateVarFromVal( const String &str, StringArray *buf ) const;
};


inline SetVars &SetVarConfigFile::getLocalVars( SetVars &buffer,
    boolean replace ) const
{
  return (getVars( buffer, true, replace ));
}


inline SetVarsTemplate< SetVarConfigFileData > &SetVarConfigFile::getLocalVars(
    SetVarsTemplate< SetVarConfigFileData > &buffer,
    boolean replace ) const
{
  return (getVars( buffer, true, replace ));
}

inline SetVars &SetVarConfigFile::getGlobalVars( SetVars &buffer,
    boolean replace ) const
{
  return (getVars( buffer, false, replace ));
}


inline SetVarsTemplate< SetVarConfigFileData > &SetVarConfigFile::getGlobalVars(
    SetVarsTemplate< SetVarConfigFileData > &buffer,
    boolean replace ) const
{
  return (getVars( buffer, false, replace ));
}

#endif //_ODE_LIB_IO_SETVARCF_HPP_
