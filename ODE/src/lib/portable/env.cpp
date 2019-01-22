/**
 * Env
 *
**/

#define _ODE_LIB_PORTABLE_ENV_CPP_
#include "lib/portable/env.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/exceptn/ioexcept.hpp"
#include "lib/io/path.hpp"
#include "lib/portable/native/proc.h"
#if defined(VMS)
#include "lib/portable/runcmd.hpp"
#endif


const String Env::CMDARG_SEP_STRING = " \t";
const String Env::EOL_CHARS_STRING = "\r\n";
const String Env::KSH_PREPEND = "export ";
const String Env::KSH_SEPARATOR = "=";
const String Env::CSH_PREPEND = "setenv ";
const String Env::CSH_SEPARATOR = " ";
const String Env::CMD_PREPEND = "@set ";
const String Env::CMD_SEPARATOR = "=";

boolean Env::inited = false;
SetVars Env::envir = SetVars( PlatformConstants::onCaseSensitiveMachine() );

void Env::init( const char **envp )
{
  if (!inited)
  {
#if defined(VMS)
    envir.set( StringConstants::ODEMAKE_DOLLARS_VAR, "1", true );
    StringArray args, varval;
    args += "SHOW SYMBOL /GLOBAL /ALL";
    RunSystemCommand cmd( args, true, true, true );
    cmd.start();
    cmd.waitFor();
    String envs( cmd.getOutputText() );
    envs.split( "\r\n", 0, &args );
    for (int i = ARRAY_FIRST_INDEX; i <= args.size(); ++i)
    {
      args[i].split( "=", 2, &varval );
      if (varval.size() == 2)
        envir.set( varval[ARRAY_FIRST_INDEX].trimThis(),
            varval[ARRAY_FIRST_INDEX + 1].trimThis().dequoteThis(), true );
    }
#else
    while (envp && *envp)
      envir.put( *(envp++) );
#ifdef OS2
    char *buf = new char[ODE_LIBPATH_LEN + 1];
    ODEgetExtLibPath( buf, ODE_BEGIN_LIBPATH );
    envir.put( buf );
    ODEgetExtLibPath( buf, ODE_END_LIBPATH );
    envir.put( buf );
    delete[] buf;
#elif defined(OS400)
    envir.put( "QIBM_USE_DESCRIPTOR_STDIO=Y" );
#endif
#endif
    inited = true;
  }
}

void Env::init( const StringArray &envp )
{
  if (!inited)
  {
    for (int i = envp.firstIndex(); i <= envp.lastIndex(); ++i)
      envir.put( envp[i] );
    inited = true;
  }
}

/**
 * Implementation of optional SetVarLinkable method.
**/
const SetVarLinkable *Env::getParent() const
{
  return (envir.getParent());
}

/**
 * Implementation for SetVarLinkable.
 * Find a variable in the hierarchy.
 *
 * @param var The variable to search for.
 * @return The value of var.  The first occurrence
 * either this object or in the parental hierarchy
 * is returned.  null is returned if the variable is
 * not defined anywhere in the hierarchy.
**/
const String *Env::find( const String &var ) const
{
  return (envir.find( var ));
}

const String *Env::get( const String &var ) const
{
  return (envir.get( var ));
}

StringArray *Env::get( boolean uppercase_vars,
    StringArray *buf, boolean get_elements ) const
{
  return (envir.get( uppercase_vars, buf, get_elements ));
}

/**
 * Implementation of clone method.  Since Env is mostly
 * static methods, what is returned is actually a
 * SetVars object containing copies of all of the
 * environment variables currently in memory.  If Env
 * has parents, they are cloned normally as well.
 * See SetVars.clone() for more info.
 *
 * @return A cloned SetVars object, cast to an Object.
 * User must delete this AND ALL PARENT POINTERS with
 * the delete operator.
**/
SetVarLinkable *Env::clone() const
{
  return (envir.clone());
}

/**
 * Creates a copy of the environment variables.
 * What is returned is actually a
 * SetVars object containing copies of all of the
 * environment variables currently in memory.
 * Parent objects are NOT copied (the parent pointers
 * of the object returned will point to same objects as
 * the original)...use clone() if the parent
 * hierarchy objects should be duplicated.
 *
 * @return A SetVars object, cast to an Object,
 * containing a copy of the environment variables.
 * User must delete this with the delete operator.
**/
SetVarLinkable *Env::copy() const
{
  return (envir.copy());
}


/**
 * Creates a file which contains the env variables'
 * list.
 *
 * @param fullpath The name of the file (full path)
 * that the env variables should be dumped to.
 * @param env_vars The list of environment variables to
 * write to the file.
 * @return true, if the file was successfully created
 * else false.
**/
boolean Env::createEnvDumpFile( const String &fullpath,
    const StringArray &env_vars, int format )
{
  String prepend, separator;
  boolean quote_val = true;

  if (env_vars.length() <= 0)
    return (false);

  switch (format)
  {
    case CMD_FORMAT:
      prepend = CMD_PREPEND;
      separator = CMD_SEPARATOR;
      quote_val = false;
      break;
    case CSH_FORMAT:
      prepend = CSH_PREPEND;
      separator = CSH_SEPARATOR;
      break;
    case KSH_FORMAT:
      prepend = KSH_PREPEND;
      separator = KSH_SEPARATOR;
      break;
    case SH_FORMAT:
    default: // default is arbitrary, should never happen
      prepend = "";
      separator = KSH_SEPARATOR;
      break;
  }

  try
  {
    fstream *dump_file = Path::openFileWriter( fullpath, false, true );
    StringArray var_and_val;

    for (int i = env_vars.firstIndex(); i <= env_vars.lastIndex(); i++)
    {
      SetVars::separateVarFromVal( env_vars[i], &var_and_val );
      if (var_and_val.length() < 1)
        continue;
      else if (var_and_val.length() < 2)
        var_and_val.add( "" );
      Path::putLine( *dump_file, prepend + var_and_val[ARRAY_FIRST_INDEX] +
          separator + ((quote_val) ? "\"" : "") +
          var_and_val[ARRAY_FIRST_INDEX + 1] + ((quote_val) ? "\"" : "") );
      if (format == SH_FORMAT)
        Path::putLine( *dump_file, KSH_PREPEND +
            var_and_val[ARRAY_FIRST_INDEX] );
      var_and_val.clear();
    }
    Path::closeFileWriter( dump_file );
  }
  catch (IOException &e)
  {
    return (false);
  }

  return (true);
}
