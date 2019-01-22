#define _ODE_LIB_IO_SETVARCF_CPP_

#include "lib/io/setvarcf.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/variable.hpp"
#include "lib/io/cmdline.hpp"

#include "lib/exceptn/exceptn.hpp"
#include "lib/exceptn/ioexcept.hpp"
#include "lib/exceptn/mfvarexc.hpp"

#define VAR_MARKER_CHAR ' '
#define GLOBAL_VAR_CHAR 'g'
#define LOCAL_VAR_CHAR 'l'
#define NEST_LEVEL_MAX 32

const String SetVarConfigFile::ON_KEYWORD = "on";
const String SetVarConfigFile::FOR_KEYWORD = "for";
const String SetVarConfigFile::SET_KEYWORD = "set";
const String SetVarConfigFile::UNSET_KEYWORD = "unset";
const String SetVarConfigFile::SETENV_KEYWORD = "setenv";
const String SetVarConfigFile::UNSETENV_KEYWORD = "unsetenv";
const String SetVarConfigFile::REPLACE_KEYWORD = "replace";
const String SetVarConfigFile::INCLUDE_KEYWORD = "include";

/**
 * NOTES:
 *
 * The information read in from the config file is stored
 * in a total of four places (in different formats and for
 * different reasons):
 *
 * 1. the save_vars SetVars object passed to the constructor
 * 2. the global Env class's SetVars object
 * 3. the private local_vars OR global_vars SetVarTemplate object
 * 4. the private lines StringArray object
 *
 * The save_vars object contains all the local variables, with
 * values stored as String objects.  It does NOT include variables
 * that aren't used (because of the "on machine" keyword, or if
 * they already existed in find_vars [and no replace keyword was used]).
 *
 * The Env class is used to store all global (environment) variables
 * with the same save_vars comments applicable.
 *
 * The local_vars and global_vars are SetVarTemplate< SetVarConfigFileData >
 * copies of the save_vars and Env information, respectively, with one
 * other difference: they also contain unset/unsetenv variables.  They do
 * NOT contain variables specified for other platforms (on/for machine,
 * where the machine is NOT the one we're running on).  Another difference
 * is that the "value" variable inside the SetVarConfigFileData object is
 * stored WITHOUT evaluating ${VAR}-style variables.  This is so that
 * the ${VAR} usage can be written back out unmodified to the file.
 *
 * The "lines" object contains a copy of virtually every line in the file,
 * including blank lines and comments.  However, for lines which used
 * set/unset or setenv/unsetenv keywords, only the variable name is stored
 * in "lines" so that the current value can be looked up in the local_vars
 * and global_vars objects.  These special lines are designated by starting
 * with two characters - the first is VAR_MARKER_CHAR, the second is either
 * GLOBAL_VAR_CHAR or LOCAL_VAR_CHAR.  Kind of kludgey, for sure.
 * Anyway, these lines are used ONLY for writing the file back out.  What
 * a pain just to preserve file contents!
 *
 * One known side effect is the fact that "on machine" lines for the
 * current machine will NOT be written out with the "on machine" keyword.
 *
**/


/**
 * Change a variable (set or unset it).
 *
 * @param var The name of the variable.
 * @param data The variable's data (value & modifiers).
 * @param replace If true and the variable already exists,
 * the new value will overwrite the old one.  If false, the
 * variable will only be added if it doesn't already exist.
 * This also affects how the line will be written to the
 * file (if true, "replace" will be prepended).
 * @param env If true, the variable is assumed to be an
 * environment variable.  If false, it will be treated as
 * a local variable.
 * @param on_machine This string will be interpreted as a
 * machine name.  If the platform
 * we're currently running on matches this string (or if
 * null is passed), the
 * variable will be added to memory...if not, it will only
 * be added to the file.  If non-null and non-empty, the string
 * will be prepended with "on " for file output purposes.
 * @return True on success, false on failure.
**/
boolean SetVarConfigFile::change( const String &var,
    const SetVarConfigFileData &data )
{
  boolean rc = true;
  SetVarConfigFileData locdata( data );
  String val = quoteIfNeeded( locdata.value );

  if (locdata.unset)
    rc = (locdata.env)
        ? parseUnsetenv( var, locdata )
        : parseUnset( var, locdata );
  else
    rc = (locdata.env)
        ? parseSetenv( var, val, locdata )
        : parseSet( var, val, locdata );

  if (rc)
    rc = rewriteFile();

  return (rc);
}


SetVarsTemplate< SetVarConfigFileData > &SetVarConfigFile::getVars(
    SetVarsTemplate< SetVarConfigFileData > &buffer,
    boolean local, boolean replace ) const
{
  StringArray vars;
  const SetVarConfigFileData *val;

  if (local)
    file_info->local_vars.get( false, &vars, false ); // get all variable names
  else
    file_info->global_vars.get( false, &vars, false ); // get all variable names

  for (int i = ARRAY_FIRST_INDEX; i <= vars.lastIndex(); ++i)
  {
    if (local)
      val = file_info->local_vars.get( vars[i] );
    else
      val = file_info->global_vars.get( vars[i] );
    if (val == 0)
      continue;
    buffer.set( vars[i], *val, replace );
  }
  return (buffer);
}


SetVars &SetVarConfigFile::getVars( SetVars &buffer,
    boolean local, boolean replace ) const
{
  StringArray vars, val_eval;
  const String *val;

  if (local)
    file_info->local_vars.get( false, &vars, false ); // get all variable names
  else
    file_info->global_vars.get( false, &vars, false ); // get all variable names

  for (int i = ARRAY_FIRST_INDEX; i <= vars.lastIndex(); ++i)
  {
    if (local)
      val = file_info->save_vars.get( vars[i] );
    else
      val = Env::getenv( vars[i] );
    if (val == 0)
      continue;
    buffer.set( vars[i], *val, replace );
  }
  return (buffer);
}


/**
 *
**/
String SetVarConfigFile::quoteIfNeeded( const String &value ) const
{
  if (!value.startsWith( "\'" ) &&
      !value.startsWith( "\"" ) &&
      (value.indexOf( ' ' ) != STRING_NOTFOUND ||
       value.indexOf( '\t' ) != STRING_NOTFOUND ||
       value.length() < 1))
    return ("\"" + value + "\"");

  return (value);
}


/**
 * Write the variable specification to the config file.
 *
 * @param var The name of the variable.
 * @param value The variable's value.
 * @param replace If true, "replace" will be prepended
 * to the variable.  If false, it won't.
 * @param env If true, "setenv" will be prepended to the
 * variable.  If false, it won't.
 * @param on_machine If non-null and non-empty, the string
 * will be prepended with "on ", and then prepended to the
 * rest of the line.
 * @return True on success, false on failure.
**/
boolean SetVarConfigFile::writeLineToFile( const String &var,
    const SetVarConfigFileData *data, boolean append )
{
  if (data == 0)
    return (false);

  String line;

  if (data->machine != StringConstants::EMPTY_STRING)
  {
    line = (data->use_on_keyword) ?
        SetVarConfigFile::ON_KEYWORD : SetVarConfigFile::FOR_KEYWORD;
    line += ' ';
    line += data->machine;
    line += ' ';
  }

  if (data->unset)
  {
    line += (data->env) ?
        SetVarConfigFile::UNSETENV_KEYWORD : SetVarConfigFile::UNSET_KEYWORD;
  }
  else
  {
    if (data->replace)
    {
      line += SetVarConfigFile::REPLACE_KEYWORD;
      line += ' ';
    }
    line += (data->env) ?
        SetVarConfigFile::SETENV_KEYWORD : StringConstants::EMPTY_STRING;
  }
  line += ' ';
  line += var;
  line += ' ';
  line += data->value;

  return (putLine( line, append ));
}


/**
 * Rewrite the entire file, using the following defaults:
 * "replace" is always true and "on_machine" is not used.
 *
 * @return True on success, false on failure.
**/
boolean SetVarConfigFile::rewriteFile()
{
  StringArray glist, llist, var_and_val;
  boolean rc = true;

  for (int i = ARRAY_FIRST_INDEX; i <= file_info->lines.lastIndex(); ++i)
  {
    if (file_info->lines[i].length() > 0 &&
        file_info->lines[i][STRING_FIRST_INDEX] == VAR_MARKER_CHAR)
    {
      String var = file_info->lines[i].substring( STRING_FIRST_INDEX + 2 );
      writeLineToFile( var,
          (file_info->lines[i][STRING_FIRST_INDEX + 1] == GLOBAL_VAR_CHAR)
              ? file_info->global_vars.get( var )
              : file_info->local_vars.get( var ),
          (i > ARRAY_FIRST_INDEX) );
    }
    else
      putLine( file_info->lines[i], (i > ARRAY_FIRST_INDEX) );
  }

  this->close();
  return (rc);
}


/**
 *
**/
boolean SetVarConfigFile::separateVarFromVal( const String &str,
    StringArray *buf ) const
{
  buf->clear();
  SetVars::separateVarFromVal( str, buf );
  if (buf->length() != 2)
    return (false);

  return true;
}


/**
 *
**/
void SetVarConfigFile::readAll()
{
  String line;

  try
  {
    while (getLine( true, false, &line, false ) != 0)
      parseLine( line );
  }
  catch (IOException &e)
  {
  }

  this->close();
}


/**
 *
**/
void SetVarConfigFile::parseLine( const String &str )
{
  StringArray str_split;
  SetVarConfigFileData data;

  if (str.length() < 1 || str.startsWith( StringConstants::POUND_SIGN ))
  {
    file_info->lines.add( str );
    return;
  }

  str.split( StringConstants::SPACE_TAB, 2, &str_split );

  if (str_split.length() < 2)
    printParseWarning( str );
  else if (str_split[ARRAY_FIRST_INDEX].equals(
      SetVarConfigFile::INCLUDE_KEYWORD ))
    parseInclude( str_split[ARRAY_FIRST_INDEX + 1] );
  else if (str_split[ARRAY_FIRST_INDEX].equals(
      SetVarConfigFile::ON_KEYWORD ))
    parseOnOrFor( str_split[ARRAY_FIRST_INDEX + 1], data, true );
  else if (str_split[ARRAY_FIRST_INDEX].equals(
      SetVarConfigFile::FOR_KEYWORD ))
    parseOnOrFor( str_split[ARRAY_FIRST_INDEX + 1], data, false );
  else if (str_split[ARRAY_FIRST_INDEX].equals(
      SetVarConfigFile::REPLACE_KEYWORD ))
    parseReplace( str_split[ARRAY_FIRST_INDEX + 1], data );
  else if (str_split[ARRAY_FIRST_INDEX].equals(
      SetVarConfigFile::SET_KEYWORD ))
    parseSet( str_split[ARRAY_FIRST_INDEX + 1], data );
  else if (str_split[ARRAY_FIRST_INDEX].equals(
      SetVarConfigFile::SETENV_KEYWORD ))
    parseSetenv( str_split[ARRAY_FIRST_INDEX + 1], data );
  else if (str_split[ARRAY_FIRST_INDEX].equals(
      SetVarConfigFile::UNSET_KEYWORD ))
    parseUnset( str_split[ARRAY_FIRST_INDEX + 1], data );
  else if (str_split[ARRAY_FIRST_INDEX].equals(
      SetVarConfigFile::UNSETENV_KEYWORD ))
    parseUnsetenv( str_split[ARRAY_FIRST_INDEX + 1], data );
  else // must be a local variable name
    parseSet( str_split[ARRAY_FIRST_INDEX],
        str_split[ARRAY_FIRST_INDEX + 1], data );
}


/**
 *
**/
boolean SetVarConfigFile::parseInclude( const String &str )
{
  StringArray split;
  String filename;

  file_info->lines.add( SetVarConfigFile::INCLUDE_KEYWORD +
      StringConstants::SPACE + str );
  parseValueUntil( str, StringConstants::SPACE_TAB, &split );

  if (split.length() < 1)
  {
    printParseWarning( SetVarConfigFile::INCLUDE_KEYWORD +
        StringConstants::SPACE + str );
    return (false);
  }

  if (nest_level > NEST_LEVEL_MAX)
  {
    printParseWarning(
        String( "include depth has exceeded maximum...ignoring \"" ) +
        split[ARRAY_FIRST_INDEX] + "\"" );
    return (false);
  }

  if (Path::absolute( split[ARRAY_FIRST_INDEX] ))
    filename = split[ARRAY_FIRST_INDEX];
  else
  {
    filename = Path::filePath( getPathname() );
    filename += '/';
    filename += split[ARRAY_FIRST_INDEX];
  }

  if (!Path::exists( filename ))
  {
    printParseWarning( String( "include file \"" ) + split[ARRAY_FIRST_INDEX] +
        "\" does not exist (ignored)" );
    return (false);
  }

  SetVarConfigFile newvars( filename, file_info->save_vars,
      file_info->find_vars, nest_level + 1 );

  newvars.getLocalVars( file_info->local_vars );
  newvars.getGlobalVars( file_info->global_vars );

  return (true);
}


/**
 *
**/
boolean SetVarConfigFile::parseOnOrFor( const String &str,
    SetVarConfigFileData &data, boolean is_on_keyword )
{
  StringArray str_split;

  data.use_on_keyword = is_on_keyword;
  str.split( StringConstants::SPACE_TAB, 2, &str_split );

  if (str_split.length() < 2)
  {
    printParseWarning( (data.use_on_keyword)
        ? (SetVarConfigFile::ON_KEYWORD + StringConstants::SPACE + str)
        : (SetVarConfigFile::FOR_KEYWORD + StringConstants::SPACE + str) );
    return (false);
  }

  data.machine = str_split[ARRAY_FIRST_INDEX];
  data.on_machine = isOnOrForMachine( data.machine, data.use_on_keyword );

  StringArray split;
  str_split[ARRAY_FIRST_INDEX + 1].split(
      StringConstants::SPACE_TAB, 2, &split );
  if (split.length() < 2)
  {
    printParseWarning( (data.use_on_keyword)
        ? (SetVarConfigFile::ON_KEYWORD + StringConstants::SPACE + str)
        : (SetVarConfigFile::FOR_KEYWORD + StringConstants::SPACE + str) );
    return (false);
  }

  if (split[ARRAY_FIRST_INDEX].equals( SetVarConfigFile::REPLACE_KEYWORD ))
    parseReplace( split[split.firstIndex() + 1], data );
  else if (split[ARRAY_FIRST_INDEX].equals( SetVarConfigFile::SET_KEYWORD ))
    parseSet( split[ARRAY_FIRST_INDEX + 1], data );
  else if (split[ARRAY_FIRST_INDEX].equals( SetVarConfigFile::SETENV_KEYWORD ))
    parseSetenv( split[ARRAY_FIRST_INDEX + 1], data );
  else if (split[ARRAY_FIRST_INDEX].equals( SetVarConfigFile::UNSET_KEYWORD ))
    parseUnset( split[ARRAY_FIRST_INDEX + 1], data );
  else if (split[ARRAY_FIRST_INDEX].equals( SetVarConfigFile::UNSETENV_KEYWORD ))
    parseUnsetenv( split[ARRAY_FIRST_INDEX + 1], data );
  else // must be a variable name: set locally, don't replace existing
    parseSet( split[ARRAY_FIRST_INDEX], split[ARRAY_FIRST_INDEX + 1], data );

  return (true);
}


/**
 *
**/
boolean SetVarConfigFile::isOnOrForMachine( const String &machine_name,
    boolean is_on_keyword ) const
{
  if (machine_name == StringConstants::EMPTY_STRING) // any machine
    return (true);

  const String *compare_to;
  compare_to = (is_on_keyword)
      ? Env::getenv( StringConstants::MACHINE_VAR )
      : Env::getenv( SandboxConstants::CONTEXT_VAR );

  if (compare_to == 0)
    return ((is_on_keyword)
        ? machine_name.equals( PlatformConstants::CURRENT_MACHINE )
        : false);
  else
    return (machine_name.equals( *compare_to ));
}


/**
 *
**/
boolean SetVarConfigFile::parseReplace( const String &str,
    SetVarConfigFileData &data )
{
  StringArray str_split;

  data.replace = true;
  str.split( StringConstants::SPACE_TAB, 2, &str_split );

  if (str_split.length() < 2)
  {
    printParseWarning( SetVarConfigFile::REPLACE_KEYWORD +
        StringConstants::SPACE + str );
    return (false);
  }

  if (str_split[ARRAY_FIRST_INDEX].equals( SetVarConfigFile::SET_KEYWORD ))
    parseSet( str_split[ARRAY_FIRST_INDEX + 1], data );
  else if (str_split[ARRAY_FIRST_INDEX].equals(
      SetVarConfigFile::SETENV_KEYWORD ))
    parseSetenv( str_split[ARRAY_FIRST_INDEX + 1], data );
  else // must be a variable name: set locally, replace existing
    parseSet( str_split[ARRAY_FIRST_INDEX],
                 str_split[ARRAY_FIRST_INDEX + 1], data );

  return (true);
}


/**
 *
**/
boolean SetVarConfigFile::parseUnset( const String &str,
    SetVarConfigFileData &data )
{
  String line;
  StringArray str_split;

  data.unset = true;
  data.env = false;
  str.split( StringConstants::SPACE_TAB, 2, &str_split );

  if (str_split.length() < 1)
  {
    printParseWarning( SetVarConfigFile::UNSET_KEYWORD +
        StringConstants::SPACE + str );
    return (false);
  }

  if (data.on_machine)
  {
    file_info->save_vars.unset( str_split[ARRAY_FIRST_INDEX] );
    if (!file_info->local_vars.varExists( str_split[ARRAY_FIRST_INDEX] ))
    {
      line = String( VAR_MARKER_CHAR );
      line += String( LOCAL_VAR_CHAR );
      line += str_split[ARRAY_FIRST_INDEX];
      file_info->lines.add( line );
    }
    file_info->local_vars.set( str_split[ARRAY_FIRST_INDEX], data, true );
  }
  else
  {
    if (data.machine != StringConstants::EMPTY_STRING)
    {
      line = (data.use_on_keyword) ?
          SetVarConfigFile::ON_KEYWORD : SetVarConfigFile::FOR_KEYWORD;
      line += ' ';
      line += data.machine;
      line += ' ';
    }
    line += SetVarConfigFile::UNSET_KEYWORD;
    line += ' ';
    line += str;
    file_info->lines.add( line );
  }

  return (true);
}


/**
 *
**/
boolean SetVarConfigFile::parseUnsetenv( const String &str,
    SetVarConfigFileData &data )
{
  String line;
  StringArray str_split;

  data.unset = true;
  data.env = true;
  str.split( StringConstants::SPACE_TAB, 2, &str_split );

  if (str_split.length() < 1)
  {
    printParseWarning( SetVarConfigFile::UNSETENV_KEYWORD +
        StringConstants::SPACE + str );
    return (false);
  }

  if (data.on_machine)
  {
    Env::unsetenv( str_split[ARRAY_FIRST_INDEX] );
    if (!file_info->global_vars.varExists( str_split[ARRAY_FIRST_INDEX] ))
    {
      line = String( VAR_MARKER_CHAR );
      line += String( GLOBAL_VAR_CHAR );
      line += str_split[ARRAY_FIRST_INDEX];
      file_info->lines.add( line );
    }
    file_info->global_vars.set( str_split[ARRAY_FIRST_INDEX], data, true );
  }
  else
  {
    if (data.machine != StringConstants::EMPTY_STRING)
    {
      line = (data.use_on_keyword) ?
          SetVarConfigFile::ON_KEYWORD : SetVarConfigFile::FOR_KEYWORD;
      line += ' ';
      line += data.machine;
      line += ' ';
    }
    line += SetVarConfigFile::UNSETENV_KEYWORD;
    line += ' ';
    line += str;
    file_info->lines.add( line );
  }

  return (true);
}


/**
 *
**/
boolean SetVarConfigFile::parseSetenv( const String &str,
    SetVarConfigFileData &data )
{
  StringArray str_split;
  str.split( StringConstants::SPACE_TAB, 2, &str_split );
  if (str_split.length() < 2)
  {
    printParseWarning( SetVarConfigFile::SETENV_KEYWORD +
        StringConstants::SPACE + str );
    return (false);
  }
  return (parseSetenv( str_split[ARRAY_FIRST_INDEX],
      str_split[ARRAY_FIRST_INDEX + 1], data ));
}


boolean SetVarConfigFile::parseSetenv( const String &var,
    const String &val, SetVarConfigFileData &data )
{
  String line;
  StringArray split;
  parseValueUntil( val, StringConstants::SPACE_TAB, &split );

  data.value = val; // so file is written with variables unevaluated

  if (data.on_machine)
  {
    Env::setenv( var, (split.length() < 1)
        ? StringConstants::EMPTY_STRING : split[ARRAY_FIRST_INDEX],
        data.replace );
    const SetVarConfigFileData *var_data = file_info->global_vars.get( var );
    if (var_data == 0)
    {
      line = String( VAR_MARKER_CHAR );
      line += String( GLOBAL_VAR_CHAR );
      line += var;
      file_info->lines.add( line );
    }
    if (var_data == 0 || var_data->unset || data.replace)
      file_info->global_vars.set( var, data, data.replace );
  }
  else
  {
    if (data.machine != StringConstants::EMPTY_STRING)
    {
      line = (data.use_on_keyword) ?
          SetVarConfigFile::ON_KEYWORD : SetVarConfigFile::FOR_KEYWORD;
      line += ' ';
      line += data.machine;
      line += ' ';
    }
    if (data.replace)
    {
      line += SetVarConfigFile::REPLACE_KEYWORD;
      line += ' ';
    }
    line += SetVarConfigFile::SETENV_KEYWORD;
    line += ' ';
    line += var;
    line += ' ';
    line += val;
    file_info->lines.add( line );
  }
  return (true);
}


/**
 *
**/
boolean SetVarConfigFile::parseSet( const String &str,
    SetVarConfigFileData &data )
{
  StringArray str_split;
  str.split( StringConstants::SPACE_TAB, 2, &str_split );
  if (str_split.length() < 2)
  {
    printParseWarning( SetVarConfigFile::SET_KEYWORD +
        StringConstants::SPACE + str );
    return false;
  }
  return (parseSet( str_split[ARRAY_FIRST_INDEX],
      str_split[ARRAY_FIRST_INDEX + 1], data ));
}


/**
 *
**/
boolean SetVarConfigFile::parseSet( const String &var, const String &val,
    SetVarConfigFileData &data )
{
  String line;
  StringArray str_split;
  parseValueUntil( val, StringConstants::SPACE_TAB, &str_split );

  data.value = val; // so file is written with variables unevaluated

  if (data.on_machine && (data.replace || file_info->find_vars == 0 ||
      !file_info->find_vars->varExists( var )))
  {
    file_info->save_vars.set( var, (str_split.length() < 1)
        ? StringConstants::EMPTY_STRING : str_split[ARRAY_FIRST_INDEX],
        data.replace );
    const SetVarConfigFileData *var_data = file_info->local_vars.get( var );
    if (var_data == 0)
    {
      line = String( VAR_MARKER_CHAR );
      line += String( LOCAL_VAR_CHAR );
      line += var;
      file_info->lines.add( line );
    }
    if (var_data == 0 || var_data->unset || data.replace)
      file_info->local_vars.set( var, data, true );
  }
  else
  {
    if (data.machine != StringConstants::EMPTY_STRING)
    {
      line = (data.use_on_keyword) ?
          SetVarConfigFile::ON_KEYWORD : SetVarConfigFile::FOR_KEYWORD;
      line += ' ';
      line += data.machine;
      line += ' ';
    }
    if (data.replace)
    {
      line += SetVarConfigFile::REPLACE_KEYWORD;
      line += ' ';
    }
    line += var;
    line += ' ';
    line += val;
    file_info->lines.add( line );
  }
  return (true);
}


/**
 *
**/
void SetVarConfigFile::printParseWarning( const String &str ) const
{
  Interface::printWarning( CommandLine::getProgramName() + ": " + 
                           String( "Error at " ) +
      Path::canonicalize( getPathname() ) + "[line " +
      String( getLastLineNumber() ) + "] (ignored): " + str );
}


/**
 *
**/
StringArray *SetVarConfigFile::parseValueUntil( const String &value,
    const String &until_chars, StringArray *buffer ) const
{
  Variable    var_eval( &file_info->save_vars, false );
  String      result, str = value;
  boolean     quoted = false;
  char        quote  = ' '; // init to something other than ' or "
  int         index  = str.firstIndex();
  StringArray var_result;
  StringArray *results = (buffer) ? buffer : new StringArray();

  results->clear();

  if (str == StringConstants::EMPTY_STRING)
    return (results);

  while (index <= str.lastIndex() && (quoted ||
      until_chars.indexOf( str.charAt( index ) ) == ELEMENT_NOTFOUND))
  {
    switch (str.charAt( index ))
    {
      case '$':
        try
        {
          var_result.clear();
          var_eval.evaluate( str.substring( index ), &var_result );
          result += var_result[var_result.firstIndex()];
          str = var_result[var_result.firstIndex() + 1];
          index = ELEMENT_NOTFOUND; // loop increments this for us
        }
        catch (MalformedVariable &e)
        { // just throw some RuntimeException so parseLine knows
          printParseWarning( str ); // reasonable
          if (results != buffer)
            delete results;
          return 0;
        }
        break;

      case '\'':
      case '\"':
        if (quoted) // are we already inside a quoted section?
        {
          if (quote == str.charAt( index )) // same kind?
          { // finished with quoted section
            quoted = false;
            quote = ' ';
          }
          else // different kind of quote...treat as normal char
            result += str.charAt( index );
        }
        else
        {
          quoted = true;
          quote = str.charAt( index );
        }
        break;

      default:
        result += str.charAt( index );
        break;
    }
    ++index;
  }

  results->add( result );

  if (index <= str.lastIndex())
    results->add( str.substring( index ) );

  return (results);
}
