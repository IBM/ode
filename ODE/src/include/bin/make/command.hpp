/**
 * Command
 *
**/
#ifndef _ODE_BIN_MAKE_COMMAND_HPP_
#define _ODE_BIN_MAKE_COMMAND_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/variable.hpp"

#include "bin/make/linesrc.hpp"
#include "bin/make/mfstmnt.hpp"

class Command : public LineSource
{
  public:
    // constructors and destructor
    Command( const String &cmd, const MakefileStatement *mfs ) :
      LineSource( mfs ), name( cmd ), be_silent( false ), ignore_errors ( false )
      {};
    Command( const Command &rhs ) :
      LineSource( rhs.mfs ), name( rhs.name ), be_silent( rhs.be_silent),
      ignore_errors( rhs.ignore_errors )
      {};
    ~Command() {};

    inline boolean operator==( const Command &rhs ) const;
    int formatCommand( const Variable &var_eval, String &cmdbuf );
    inline const String &getCmdName() const;
    inline void setCmdName( const String &new_name );
    inline boolean isSilent() const;
    inline boolean isIgnoreErrors() const;

  private:
    String name;
    boolean be_silent;
    boolean ignore_errors;

    static void convert( String &cmd );
};

inline boolean Command::operator==( const Command &rhs ) const
{
  return (name.equals(rhs.name));
}

inline const String &Command::getCmdName() const
{
  return name;
}

/************************************************
  * --- inline void Command::setCmdName ---
  *
  ************************************************/
inline void Command::setCmdName( const String &new_name )
{
  name = new_name;
}

/************************************************
  * --- inline boolean Command::isSilent ---
  *
  ************************************************/
inline boolean Command::isSilent() const
{
  return( be_silent );
}

/************************************************
  * --- inline boolean Command::isIgnoreErrors ---
  *
  ************************************************/
inline boolean Command::isIgnoreErrors() const
{
  return( ignore_errors );
}


#endif //_ODE_BIN_MAKE_COMMAND_HPP_

