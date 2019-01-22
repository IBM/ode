/**
 * Env
 *
**/
#ifndef _ODE_LIB_PORTABLE_ENV_HPP_
#define _ODE_LIB_PORTABLE_ENV_HPP_

#include <base/odebase.hpp>
#include "lib/portable/platcon.hpp"
#include "lib/string/svarlink.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/setvars.hpp"


/**
 * Just a static wrapper around a SetVars object with
 * no parent.
 * An instantiation can be made, which provides the
 * find() method necessary for the SetVarLinkable interface.
 *
**/
class Env : public SetVarLinkable
{
  public:

    // for the 3rd parameter of createEnvDumpFile()
    enum
    {
      SH_FORMAT  = 0,  // "VAR=value", "export VAR"
      KSH_FORMAT = 1,  // "export VAR=value"
      CSH_FORMAT = 2,  // "setenv VAR value"
      CMD_FORMAT = 3   // "set VAR=value"
    };

    static void init( const char **envp );
    static void init( const StringArray &envp );
    inline static boolean isInited();
    inline static void clear();
    virtual const String *find( const String &var ) const;
    inline static const String *findenv( const String &var );
    virtual const SetVarLinkable *getParent() const;
    inline void setParent( const SetVarLinkable *parent );
    inline void unsetParent();
    inline static boolean putenv( const String &envstr );
    inline static boolean putenv( const StringArray &envstrs );
    inline static boolean setenv( const String &var, const String &val,
        boolean replace );
    inline static boolean unsetenv( const String &var );
    inline static const String *getenv( const String &var );
    virtual const String *get( const String &var ) const;
    inline static StringArray *getenv( boolean uppercase_vars =
        !PlatformConstants::onCaseSensitiveMachine(),
        StringArray *buf = 0 );
    virtual StringArray *get( boolean uppercase_vars =
        !PlatformConstants::onCaseSensitiveMachine(),
        StringArray *buf = 0, boolean get_elements = true ) const;
    inline static const SetVars *getSetVars();
    virtual SetVarLinkable *clone() const;
    virtual SetVarLinkable *copy() const;

    // use the shell format enumeration types for the third parameter
    static boolean createEnvDumpFile( const String &fullpath,
        const StringArray &env_vars, int format );


  private:

    static const String ODEDLLPORT CMDARG_SEP_STRING; // " \t"
    static const String ODEDLLPORT EOL_CHARS_STRING; // "\r\n"
    static const String ODEDLLPORT KSH_PREPEND;
    static const String ODEDLLPORT KSH_SEPARATOR;
    static const String ODEDLLPORT CSH_PREPEND;
    static const String ODEDLLPORT CSH_SEPARATOR;
    static const String ODEDLLPORT CMD_PREPEND;
    static const String ODEDLLPORT CMD_SEPARATOR;
    static boolean ODEDLLPORT inited; // false
    static SetVars ODEDLLPORT envir;
};

inline boolean Env::isInited()
{
  return (inited);
}

inline void Env::clear()
{
  envir.clear();
  inited = false;
}

inline const String *Env::findenv( const String &var )
{
  return (envir.find( var ));
}

/**
 * Implementation of optional SetVarLinkable method.
 * This allows the environment
 * variable list to be embedded into a hierarchical
 * organization (especially useful when the -e flag
 * is given to mk).
 *
 * @param parent The parent object.
**/
inline void Env::setParent( const SetVarLinkable *parent )
{
  envir.setParent( parent );
}

/**
 * Implementation of optional SetVarLinkable method.
 * This allows the environment
 * variable list to be removed from a hierarchical
 * organization.
**/
inline void Env::unsetParent()
{
  envir.unsetParent();
}

/**
 * Set the value of an environment variable.  envstr should
 * be of the form "VARIABLE=value" (where "=" is the attribute
 * SetVars.VAR_SEP_STRING).  If the variable already
 * exists in memory, the old value will be overwritten with
 * the new one.  Leading SetVars.VAR_SEP_STRING characters
 * in the value will be discarded.  If the form "VARIABLE="
 * (where "=" is SetVars.VAR_SEP_STRING) is used, the variable
 * will be unset.
 *
 * @param envstr The variable/value pair to set, in the form
 * "VARIABLE=value" (using SetVars.VAR_SEP_STRING instead of
 * "=").
 * @return True on success, false on failure.
**/
inline boolean Env::putenv( const String &envstr )
{
  return (envir.put( envstr ));
}

/**
 * Set the values of many environment variables.  Each element
 * of envstrs should
 * be of the form "VARIABLE=value" (where "=" is the attribute
 * SetVars.VAR_SEP_STRING).  If a variable already
 * exists in memory, the old value will be overwritten with
 * the new one.  Leading SetVars.VAR_SEP_STRING characters
 * in each value will be discarded.  If the form "VARIABLE="
 * (where "=" is SetVars.VAR_SEP_STRING) is used, that
 * variable will be unset.
 *
 * @param envstrs An array of variable/value pairs to set,
 * each in the form
 * "VARIABLE=value" (using SetVars.VAR_SEP_STRING instead of
 * "=").
 * @return True on success, false on failure.
**/
inline boolean Env::putenv( const StringArray &envstrs )
{
  return (envir.put( envstrs ));
}

/**
 * Set the value of an environment variable.  Allows control
 * over whether to replace an existing variable.
 *
 * @param var The variable name to set.
 * @param val The variable's value.  If null, the variable
 * will be unset (replace must be true).
 * @param replace If the variable is already set, tells
 * whether or not to replace the old value with val.
 * @return True on success, false on failure.
**/
inline boolean Env::setenv( const String &var, const String &val,
    boolean replace )
{
  return (envir.set( var, val, replace ));
}

/**
 * Removes a variable from memory.
 *
 * @param var The variable to unset.
 * @return True on success, false on failure.
**/
inline boolean Env::unsetenv( const String &var )
{
  return (envir.unset( var ));
}

/**
 * Get the value of an environment variable.
 *
 * @param var The variable to retrieve the value of.
 * @return The variable's value.  null is returned
 * if the variable doesn't exist.
**/
inline const String *Env::getenv( const String &var )
{
  return (envir.get( var ));
}

/**
 * Get the list of all environment variables in memory,
 * in no particular order.  Suitable for use when calling
 * Runtime.exec() or RunSystemCommand().
 *
 * @param uppercase_vars If true, convert all variable
 * names to uppercase before returning.
 * @return The array of all variable/value pairs (in the
 * form "VARIABLE=value", where "=" is SetVars.VAR_SEP_STRING).
 * null is returned if no variables exist.
**/
inline StringArray *Env::getenv( boolean uppercase_vars,
    StringArray *buf )
{
  return (envir.get( uppercase_vars, buf ));
}

inline const SetVars *Env::getSetVars()
{
  return (&envir);
}

#endif /* _ODE_LIB_PORTABLE_ENV_HPP_ */
