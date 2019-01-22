/**
 * Variable
**/

#define _ODE_LIB_STRING_VARIABLE_CPP_
#include "lib/string/variable.hpp"
#include "lib/exceptn/mfvarexc.hpp"
#include "lib/exceptn/ioexcept.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/string/pattern.hpp"
#include "lib/portable/runcmd.hpp"
#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/intcmds/genpath.hpp"

const char Variable::MODIFIER_CHAR = ':';
const char Variable::GLOBAL_SUBST_CHAR = 'g';
const char Variable::GLOBAL_SUBST_FIRST_MATCH_CHAR = 'f';
const char Variable::GLOBAL_SUBST_ALL_WORD_CHAR = 'w';
const char Variable::REGEX_IGNORE_CASE_CHAR = 'i';
const char Variable::REGEX_CONDITIONAL_IGNORE_CASE_CHAR = 'c';
const char Variable::REGEX_EXTENDED_SYNTAX_CHAR = 'e';
const String Variable::EOL_CHARS_STRING = "\r\n";
const String Variable::WORD_SPLIT_STRING = " \t\r\n";
StringArray Variable::recursive_vars( 5, 5 );
boolean Variable::only_parse = false;

/**
 * Evaluate one variable.  String should begin with '$', and will
 * be evaluated to the end of that variable.  Resulting evaluated
 * string is placed in the first array entry of the return value.
 * Any extra characters are returned unmodified in the second array
 * entry.
 *
 * WARNING: User must deallocate the returned pointer with the
 * delete operator if they did not pass the "buf" argument.
 *
 * @param str The string containing the variable specification.
 * @return A two-element array.  The first element contains the
 * evaluated variable results.  The second contains any extraneous
 * characters (unmodified) found after the variable specification.
 * @exception COM.ibm.ode.lib.exception.MalformedVariable Indicates
 * that the variable specification is malformed.
**/
StringArray *Variable::evaluate( const String &str,
    StringArray *buf ) const
// throw (MalformedVariable)
{
  if (buf == 0)
    buf = new StringArray( 2, 2 );
  else
    buf->extendTo( 2 ); // make sure buffer has two elements

  Variable::recursive_vars.clear();

  (*buf)[ARRAY_FIRST_INDEX + 1] = evaluateString(
      str.toCharPtr(), (*buf)[ARRAY_FIRST_INDEX] );
  return (buf);
}


/**
 * Non-recursive callers (evaluate and parseUntil) should
 * clear the recursive_vars array.
**/
const char *Variable::evaluateString( const char *str, String &result,
                                      InfoPrinter *ip ) const
// throw (MalformedVariable)
{
  result = StringConstants::EMPTY_STRING;
  if (*str != variable_start_char) // hey, this isn't a variable!
    return (str);
  else if (*(str + 1) == '\0')       // dollar sign by itself?
  {
    if (vars != 0 && vars->find(StringConstants::ODEMAKE_DOLLARS_VAR) != 0)
      result = StringConstants::DOLLAR_SIGN;
    return (str + 1);
  }

  ++str; // skip the dollar sign

  char closing_brace = ')';
  const String *find_result;
  String until_chars, variable_name, value;
  boolean shortNameVar = false;

  // we can have any of the following schemes:
  // $(var) or ${var}   [normal]
  // $c   [single character variable name]
  // $$   [escaped, don't evaluate]
  switch (*str)
  {
    case '$': // "escaped" dollar sign...just return it
      result = StringConstants::DOLLAR_SIGN;
      return (str + 1);
    case '(': // good
      closing_brace = ')';
      break;
    case '{': // good
      closing_brace = '}';
      break;
    case '<':
    case '>':
    case '?':
    case '*':
    case '@':
    case '!':
    case '%':
      // If any of these special short version variables are next,
      // flag it so ODEMAKE_DOLLARS case processes it correctly.
      shortNameVar = true;
      // Fall thru to default on purpose!
    default: // assume this single character is the variable name
      if ((vars != 0) &&
          (vars->find(StringConstants::ODEMAKE_DOLLARS_VAR) != 0) &&
          !shortNameVar)
      {
        result = StringConstants::DOLLAR_SIGN;
        return (str);
      }
      variable_name = String( *str );
      checkRecursiveVars( variable_name ); // throws exception if found
      if (vars != 0 && (find_result = vars->find( variable_name )) != 0)
      {
        recursive_vars.add( variable_name );
        parseString( find_result->toCharPtr(), result );
        recursive_vars.setNumElements( recursive_vars.size() - 1 );
      }
      return (str + 1);
  }
  ++str; // skip the open brace
  // we have now seen either "$(" or "${", so
  // get the variable name.  parse
  // characters until a ":" or closing brace is found.
  until_chars += closing_brace;
  until_chars += Variable::MODIFIER_CHAR;
  str = parseString( str, variable_name, until_chars );
  checkRecursiveVars( variable_name ); // throws exception if found
  if (vars != 0 && (find_result = vars->find( variable_name )) != 0)
  {
    recursive_vars.add( variable_name );
    parseString( find_result->toCharPtr(), (allow_modifiers) ? value : result,
                 StringConstants::EMPTY_STRING, false, ip );
    recursive_vars.setNumElements( recursive_vars.size() - 1 );
  }

  if (allow_modifiers)
  {
    str = evaluateModifiers( value.trimThis(), str, variable_name,
        closing_brace, result, ip );
  }
  else
  {
    if (*str != closing_brace)
      throw (MalformedVariable( "Missing closing brace" ));
    result.trimThis();
    ++str;
  }

  return (str);
}

void Variable::checkRecursiveVars( const String &variable_name ) const
// throw (MalformedVariable)
{
  for (int i = ARRAY_FIRST_INDEX; i <= recursive_vars.lastIndex(); ++i)
    if (recursive_vars[i] == variable_name)
      throw (MalformedVariable( String( "Variable " ) +
          variable_name + " is recursive." ));
}

/**
 * Similar to parseUntil, but is private and works on char*.
 *
 * NOTE: don't make tmpeval static, since this function is
 * recursively called.
**/
const char *Variable::parseString( const char *str, String &results,
    const String &until_chars, boolean backslash_is_escape,
    InfoPrinter *ip, boolean eval_vars ) const
{
  String tmpeval;
  const char *newstr;
  boolean tmpbool;

  results = StringConstants::EMPTY_STRING;
  while (*str != '\0')
  {
    if (*str == '\\' && backslash_is_escape)
    {
      // add the next character instead
      if (*(++str) != '\0')
        results += *(str++);
    }
    else if (until_chars.length() > 0 &&
        until_chars.indexOf( *str ) != STRING_NOTFOUND)
      break;
    else if (*str == variable_start_char)
    {
      if (ip)
        ip->print( String( "Var: Parsing " ) + str );
      if (!eval_vars)
      {
        tmpbool = Variable::only_parse;
        Variable::only_parse = true;
      }
      newstr = evaluateString( str, tmpeval, ip );
      // reset only_parse
      if (!eval_vars)
        Variable::only_parse = tmpbool;
      if (ip)
      {
        ip->print( String( "Var: Result " ) +
            ( (tmpeval.length() == 0) ?
            StringConstants::DOUBLE_QUOTE + StringConstants::DOUBLE_QUOTE :
            String( "`" ) + tmpeval + StringConstants::SINGLE_QUOTE ) );
      }
      if (eval_vars)
        results += tmpeval;
      else
        results += String( str ).substring( STRING_FIRST_INDEX,
            STRING_FIRST_INDEX + (newstr - str) );
      str = newstr;
    }
    else
      results += *(str++);
  }
  return (str);
}

/**
 * Modifies a string based on the modifier(s) specified.
 *
 * WARNING: calls to the doXXXModifier() functions often
 * receive str and result as the same object.  So, those
 * functions should NOT modify result's value while
 * parsing str.
 *
 * @param str Contents of the variable that we're modifying.  This
 * can be null if variable_name doesn't have a value.
 * @param mod_str Beginning of the modifier specification.
 * End is expected to be at a closing curly brace (which
 * signals the end of the variable).
 * @return The resulting string array, after applying any and all
 * modifiers.  The array is always two elements in size, in which
 * the first element is the evaluated string, and the second is
 * any mod_str characters leftover.  Neither element will ever be
 * null (empty strings are used instead).
 * @exception COM.ibm.ode.lib.exception.MalformedVariable Indicates that
 * the variable modifier string is malformed.
 */
const char *Variable::evaluateModifiers( const String &str,
    const char *mod_str, const String &variable_name,
    char closing_brace, String &result, InfoPrinter *ip ) const
// throw (MalformedVariable)
{
  result = str;

  // at the beginning of each loop iteration, mod_str should be pointing
  // to either a closing brace or another colon.
  while (*mod_str != closing_brace)
  {
    if (*mod_str == '\0')
      throw (MalformedVariable( "Unclosed variable specification" ));
    if (*mod_str != Variable::MODIFIER_CHAR)
      throw (MalformedVariable( String( "Malformed variable; expecting '" )+
          String( Variable::MODIFIER_CHAR ) + "' or '" +
          String( closing_brace ) + "' and found '" + *mod_str + "'." ));

    // we have seen a colon now, so the next char is the modifier
    ++mod_str;

    // The order of the modifiers within the switch statement is
    // governed primarily by frequency of use in our rules (starting
    // with the most often used).  Rough approximation, of course.
    if (ip)
      if (*mod_str == 'X' || (*mod_str == 'C' && *(mod_str+1) == '-'))
        ip->print( String( "Var: Applying " ) +
            String( Variable::MODIFIER_CHAR ) + String( *mod_str ) +
            String( *(mod_str+1) ) + " to " + variable_name );
      else
        ip->print( String( "Var: Applying " ) +
            String( Variable::MODIFIER_CHAR ) + String( *mod_str ) +
            " to " + variable_name );
    switch (*mod_str)
    {
      case 'S': // Substitution (splits strings into words)
      case 's': // substitution (doesn't split string into words)
        mod_str = doSubstitutionModifier( result, mod_str + 1,
            (*mod_str == 'S') );
        break;
      case '@': // simple substitution modifier
        mod_str = doAtSignModifier( result, mod_str + 1 );
        break;
      case 'U': // Undefined (if so, return newval, else original value)
        mod_str = doUndefinedModifier( result, mod_str + 1, closing_brace,
            variable_name );
        break;
      case 'D': // Defined (if so, return newval, else previous string)
        mod_str = doDefinedModifier( result, mod_str + 1, closing_brace,
            variable_name );
        break;
      case 'A': // Overwrite and append to file
      case 'a': // append to file
        mod_str = doAppendModifier( result, mod_str + 1, closing_brace,
            (*mod_str == 'A') );
        break;
      case 'M': // Matching (splits strings into words)
      case 'm': // Matching (doesn't split strings into words)
        mod_str = doMatchingModifier( result, mod_str + 1, closing_brace,
            (*mod_str == 'M') );
        break;
      case 'N': // Non-matching (splits strings into words)
      case 'n': // Non-matching (doesn't split strings into words)
        mod_str = doNonMatchingModifier( result, mod_str + 1, closing_brace,
            (*mod_str == 'N') );
        break;
      case '!': // run external command
        mod_str = doBangSignModifier( result, mod_str + 1 );
        break;
      case 'P': // Path...use variable name
      case 'p': // Path...use variable contents
        if (*mod_str == 'P')
          result = variable_name;
        doFindPathModifier( result, false );
        ++mod_str;
        break;
      case 'Q': //QuickSort
        doQuickSort( result, mod_str + 1 );
        mod_str += 2;   //Must be :Q+  or  :Q-
        break;
      case 'd': // Path search for dirs...use variable contents
        doFindPathModifier( result, true );
        ++mod_str;
        break;
      case 'E': // Extension (path suffix)
      case 'H': // Head (path without file)
      case 'R': // Root (pathname without suffix)
      case 'T': // Tail (file without path)
        doPathTypeModifier( result, *mod_str );
        ++mod_str;
        break;
      case 'L': // Literal (name of the variable being modified)
        result = variable_name;
        ++mod_str;
        break;
      case 'G': // generate path(s) a la the genpath command
        doGenPathModifier( result );
        ++mod_str;
        break;
      case 'C': // create path(s)
        mod_str = doMakePathModifier( result, mod_str + 1 );
        break;
      case 'b': // non-blank (defined and not only whitespace)
        mod_str = doNonBlankModifier( result, mod_str + 1, closing_brace );
        break;
      case 'B': // Blank (undefined or only whitespace)
        mod_str = doBlankModifier( result, mod_str + 1, closing_brace );
        break;
      case 'F': // Find file in path
        mod_str = doFindFileModifier( result, mod_str + 1, closing_brace );
        break;
      case 'x': // regular expression modifier
        mod_str = doRegexModifier( result, mod_str + 1 );
        break;
      case 'X': // eXists (removes files/dirs that don't exist from the string)
        mod_str = doExistsModifier( result, mod_str + 1 );
        break;
      case 'r': // remove file(s) and/or path(s)
        mod_str = doRemoveModifier( result, mod_str + 1 );
        break;
      case 'u': // uppercase
        result.toUpperCaseThis();
        ++mod_str;
        break;
      case 'l': // lowercase
        result.toLowerCaseThis();
        ++mod_str;
        break;
      case 'i': // indexed word/char
        mod_str = doIndexModifier( result, mod_str + 1, closing_brace );
        break;
      case '\0': // reached end of string prematurely
        throw (MalformedVariable( "Missing variable modifier after ':'" ));
      default: // assume "old_string=new_string" format
        mod_str = doEqualSignModifier( result, mod_str, closing_brace );
    } // switch
    if (ip)
      ip->print( String( "Var: Result " ) +
          ( (result.length() == 0) ?
          StringConstants::DOUBLE_QUOTE + StringConstants::DOUBLE_QUOTE :
          String( "`" ) + result + StringConstants::SINGLE_QUOTE ) );
  } // while

  return (mod_str + 1);
}

const char *Variable::doIndexModifier( String &str, const char *mod_str,
    char closing_brace ) const
// throw (MalformedVariable)
{
  String index_str;
  boolean char_index = false;
  int index = 0;

  mod_str = parseString( mod_str, index_str,
      String( closing_brace ) + Variable::MODIFIER_CHAR, true );

  if (index_str.length() > 0 && index_str[index_str.lastIndex()] == 'c')
  {
    index_str.remove( index_str.lastIndex(), 1 );
	 char_index = true;
  }

  if (index_str.length() < 1 || !index_str.isDigits())
    throw (MalformedVariable( String( "Malformed " ) +
        "'i' modifier; index is not a number" ));

  // note: index is in the range 1..n
  index = index_str.asInt();

  // indices less than 1 will be treated as 1
  // (just as later, indices greater than the upper bound
  // will be treated as the upper bound itself)
  if (index < 1)
    index = 1;

  if (str.length() < 1)
  {
    // do nothing...any index of an empty string is still empty
  }
  if (char_index)
  {
    // normalize the index for the string range
    int str_index = index + (STRING_FIRST_INDEX - 1);
    if (str_index > str.lastIndex())
	 	str_index = str.lastIndex();
    str = String( str[str_index] );
  }
  else
  {
    // normalize the index for the array range
    int arr_index = index + (ARRAY_FIRST_INDEX - 1);
    StringArray words;
	 str.split( Variable::WORD_SPLIT_STRING, 0, &words );
	 if (arr_index > words.lastIndex())
	   arr_index = words.lastIndex();
    str = words[arr_index];
  }
  return (mod_str);
}

const char *Variable::doMatchTypeModifier( String &str,
    const char *mod_str, char closing_brace, boolean matching,
    boolean split_words ) const
// throw (MalformedVariable)
{
  static StringArray words, patterns;
  String pattern;
  boolean matched;
  int i, j;

  mod_str = parseString( mod_str, pattern,
      String( closing_brace ) + Variable::MODIFIER_CHAR, true );

  words.clear();
  patterns.clear();
  if (split_words)
    str.split( Variable::WORD_SPLIT_STRING, 0, &words );
  else
    words.add( str );
  pattern.split( Variable::WORD_SPLIT_STRING, 0, &patterns );

  str = StringConstants::EMPTY_STRING;
  for (i = ARRAY_FIRST_INDEX; i <= words.lastIndex(); ++i)
  {
    matched = false;
    for (j = ARRAY_FIRST_INDEX; j <= patterns.lastIndex(); ++j)
    {
      if (Pattern::isMatching( patterns[j], words[i], true ))
      {
        matched = true;
        // if searching for a match & the word matches, no need to continue.
        if (matching)
          break;
        // otherwise, if searching for a non-match, we must try
        // every pattern
      }
    }
    if ((matched && matching) || (!matched && !matching))
    {
      if (str.length() > 0)
        str += StringConstants::SPACE;
      str += words[i];
    }
  }
  return (mod_str);
}

const char *Variable::doSubstitutionModifier( String &str,
    const char *mod_str, boolean split_words ) const
// throw (MalformedVariable)
{
  String old_string, new_string;
  boolean global = false, begin_anchor = false, end_anchor = false;
  boolean all_wd_matches = false;
  char delim = *mod_str;

  if (*mod_str == '\0')
    throw (MalformedVariable( String( "Malformed '" ) +
        ((split_words) ? "S" : "s") +
        "' variable modifier; first delimiter not found" ));

  // skip over delimiter
  ++mod_str;

  // first, get old_string
  mod_str = substParseString( mod_str, old_string, delim, 0,
      begin_anchor, end_anchor );
  if (*mod_str == '\0')
    throw (MalformedVariable( String( "Malformed '" ) +
        ((split_words) ? "S" : "s") +
        "' variable modifier; second delimiter '" + delim + "' not found" ));

  ++mod_str; // skip delimiter between old and new string

  // get the new string
  mod_str = substParseString( mod_str, new_string, delim, &old_string,
      begin_anchor, end_anchor );

  if (*mod_str == '\0')
    throw (MalformedVariable( String( "Malformed '" ) +
        ((split_words) ? "S" : "s") +
        "' variable modifier; third delimiter '" + delim + "' not found" ));

  ++mod_str; // skip the last delimiter

  if (*mod_str == Variable::GLOBAL_SUBST_CHAR)
  {
    global = true;
    all_wd_matches = true;
    ++mod_str;
  }
  else if (*mod_str == Variable::GLOBAL_SUBST_FIRST_MATCH_CHAR)
  {
    global = true;
    ++mod_str;
  }
  else if (*mod_str == Variable::GLOBAL_SUBST_ALL_WORD_CHAR)
  {
    all_wd_matches = true;
    ++mod_str;
  }
  substitute( str, old_string, new_string, global, all_wd_matches, split_words,
      begin_anchor, end_anchor );
  return (mod_str);
}

const char *Variable::doRegexModifier( String &str,
    const char *mod_str ) const
// throw (MalformedVariable)
{
  String old_string, new_string;
  boolean global = false;
  boolean all_wd_matches = false;
  boolean do_substitution = false;
  boolean matching, split_words;

  char submodifier = *mod_str;
  if (*mod_str == '\0')
    throw (MalformedVariable(
                "Malformed x variable modifier; no submodifier after ':x';"
                " expecting 'M', 'm', 'N', 'n', 'S', or 's'" ));
  switch (submodifier)
  {
    case 'M':
      matching = true;
      split_words = true;
      break;
    case 'm':
      matching = true;
      split_words = false;
      break;
    case 'N':
      matching = false;
      split_words = true;
      break;
    case 'n':
      matching = false;
      split_words = false;
      break;
    case 'S':
      split_words = true;
      do_substitution = true;
      break;
    case 's':
      split_words = false;
      do_substitution = true;
      break;
    default:
      throw (MalformedVariable(
         String( "Malformed 'x' variable modifier; invalid submodifier '" ) +
         submodifier +
         "' after ':x'; expecting 'M', 'm', 'N', 'n', 'S', or 's'" ));
      break;
  }
  // skip over submodifier
  ++mod_str;

  char delim = *mod_str;

  if (*mod_str == '\0')
    throw (MalformedVariable( String( "Malformed variable modifier 'x" ) +
        submodifier + "'; first delimiter not found" ));

  // skip over delimiter
  ++mod_str;

  // first, get old_string
  mod_str = regexParseString( mod_str, old_string, delim, 0 );
  if (*mod_str == '\0')
    throw (MalformedVariable( String( "Malformed variable modifier 'x" ) +
        submodifier + "'; second delimiter '" + delim + "' not found" ));
  ++mod_str; // skip the second delimiter

  if (do_substitution)
  {
    // get the new string
    mod_str = parseString( mod_str, new_string, String( delim ),
        false, 0, false );

    if (*mod_str == '\0')
      throw (MalformedVariable( String( "Malformed variable modifier 'x" ) +
          submodifier + "'; third delimiter '" + delim + "' not found" ));

    ++mod_str; // skip the last delimiter
  }

  boolean extended = false;
  boolean ignoreCase = false;
  boolean caseChar = false;
  boolean globalChar = false;
  while (! extended || ! caseChar || (do_substitution && ! globalChar))
  {
    if (*mod_str == Variable::GLOBAL_SUBST_CHAR)
    {
      if (globalChar || ! do_substitution)
        break;
      global = true;
      all_wd_matches = true;
      globalChar = true;
    }
    else if (*mod_str == Variable::GLOBAL_SUBST_FIRST_MATCH_CHAR)
    {
      if (globalChar || ! do_substitution)
        break;
      global = true;
      globalChar = true;
    }
    else if (*mod_str == Variable::GLOBAL_SUBST_ALL_WORD_CHAR)
    {
      if (globalChar || ! do_substitution)
        break;
      all_wd_matches = true;
      globalChar = true;
    }
    else if (*mod_str == Variable::REGEX_IGNORE_CASE_CHAR)
    {
      if (caseChar)
        break;
      ignoreCase = true;
      caseChar = true;
    }
    else if (*mod_str == Variable::REGEX_CONDITIONAL_IGNORE_CASE_CHAR)
    {
      if (caseChar)
        break;
#ifdef CASE_INSENSITIVE_OS
      ignoreCase = true;
#endif
      caseChar = true;
    }
    else if (*mod_str == Variable::REGEX_EXTENDED_SYNTAX_CHAR)
    {
      if (extended)
        break;
      extended = true;
    }
    else
      break;
    ++mod_str;
  }
  if (do_substitution)
    regexSubstitute( str, old_string, new_string, global, all_wd_matches,
                     split_words, extended, ignoreCase );
  else
    regexMatch( str, old_string, matching,
                split_words, extended, ignoreCase );
  return (mod_str);
}

/**
 * Note: remember to check the "only_parse" boolean to see
 * if we should not be running commands.
 *
**/
const char *Variable::doBangSignModifier( String &str,
    const char *mod_str ) const
// throw (MalformedVariable)
{
  String command_str;

  mod_str = parseString( mod_str, command_str,
      StringConstants::EXCLAMATION, true );
  command_str.trimThis();
  if (*mod_str == '\0') // missing ! character
    throw (MalformedVariable(
                  "Malformed '!' variable modifier; missing final '!'" ));
  ++mod_str; // skip the ! sign

  // Look for 'i' and 'e' options.
  boolean echoCmd = false;
  boolean ignore  = false;
  boolean stop    = false;
  // Only process at most one 'i' and one 'e'.  No duplicates.
  while ((!echoCmd || !ignore) && !stop)
  {
    if (!echoCmd && (*mod_str == 'e'))
      echoCmd = true;

    else if (!ignore && (*mod_str == 'i'))
      ignore = true;

    else
      break;

    ++mod_str;
  }

  if (command_str.length() < 1) // nothing to run
    return (mod_str);
  if (Variable::only_parse) // we're inside a conditional's replacement string
    return (mod_str); // and it evaluated to false, so don't run the command!
  convertEOLChars( runShellCmd( command_str, envs, 0,
                                String( "!" ) + command_str + "!", false,
                                echoCmd, ignore ),
                   str );
  return (mod_str);
}


/******************************************************************************
 *
 */
String Variable::runShellCmd( const String &val_str,
                              const SetVars *envs,
                              const SetVars *globals,
                              const String &opName,
                              boolean checkGlobals,
                              boolean echoCmd,
                              boolean ignoreError
                              )
// throws MalformedVariable
{
  static StringArray cmds;
  cmds.clear();
  String  reduced_val_str = val_str.reduceWhitespace();
  if (echoCmd)
    Interface::printAlways( reduced_val_str );
  RunSystemCommand::buildShellCmdArray( reduced_val_str,
                                        StringConstants::EMPTY_STRING,
                                        &cmds );

  StringArray env_vars;
  boolean upcase_vars = !PlatformConstants::onCaseSensitiveMachine();
  if (envs == 0 || (checkGlobals && globals == 0))
    Env::getSetVars()->getAll( 0, upcase_vars, &env_vars );
  else
    envs->getAll( globals, upcase_vars, &env_vars );

  RunSystemCommand cmd( cmds, env_vars.toCharStarArray(), env_vars.size(),
      true, true, true, false, true );
  cmd.run();
  cmd.waitFor();

  if (!ignoreError && cmd.getExitCode() != 0)
    throw( MalformedVariable( "Command from " + opName +
                              " returned non-zero" + " (" +
                              String( cmd.getExitCode() ) + ")" ) );
  return (cmd.getOutputText());
}


/**
 * Used by doBangSignModifier to change sequences of EOL chars
 * into a single space.
**/
void Variable::convertEOLChars( const String &input, String &output ) const
{
  int i = STRING_FIRST_INDEX;
  output.extendTo( input.length() );
  output = StringConstants::EMPTY_STRING;

  // first, ignore EOLs at the head and tail, so we don't have to trim output
  while ((input[i] == '\r' || input[i] == '\n') && i <= input.lastIndex())
    ++i;
  int start_index = i;
  i = input.lastIndex();
  while ((input[i] == '\r' || input[i] == '\n') && i >= start_index)
    --i;
  int end_index = i;

  for (i = start_index; i <= end_index; ++i)
  {
    if (input[i] == '\r' || input[i] == '\n')
    {
      output.append( StringConstants::SPACE );
      ++i;
      while (input[i] == '\r' || input[i] == '\n')
        ++i;
      --i; // for loop will increment back to where we need to be
    }
    else
      output.append( input[i] );
  }
}


const char *Variable::doEqualSignModifier( String &str,
    const char *mod_str, char closing_brace ) const
// throw (MalformedVariable)
{
  String old_string, new_string;
  char first_char = *mod_str;

  // get old string
  mod_str = parseString( mod_str, old_string, StringConstants::EQUAL_SIGN );

  if (*mod_str == '\0') // missing = character
  {
    if (first_char == '}')
      throw (MalformedVariable( "Missing variable modifier after ':'" ));
    else
      throw (MalformedVariable( String( "Invalid variable modifier '" ) +
                                first_char + "' after ':'" ));
  }
  ++mod_str; // skip over "="

  // get new string
  mod_str = parseString( mod_str, new_string, String( closing_brace ) );

  substitute( str, old_string, new_string, true, true, true, false, true );

  return (mod_str);
}


const char *Variable::doAppendModifier( String &str, const char *mod_str,
                                      char closing_brace, boolean flush ) const
// throw (MalformedVariable)
{
  String filestr;

  mod_str = parseString( mod_str, filestr,
                  String( closing_brace ) + Variable::MODIFIER_CHAR, true );
  if (Variable::only_parse) // we're inside a conditional's replacement string
    return (mod_str); // and it evaluated to false, so don't run the command!
  if (filestr.isEmpty())   // missing target filename.
  {
    throw (MalformedVariable( (flush)?
                   "Malformed 'A' variable modifier; no filename after 'A'" :
                   "Malformed 'a' variable modifier; no filename after 'a'" ));
  }
  else if (filestr == "&STDOUT")
  {
    Interface::printAlways( str );
    str = StringConstants::EMPTY_STRING;
  }
  else if (filestr == "&STDERR")
  {
    Interface::printAlwaysToErrorStream( str );
    str = StringConstants::EMPTY_STRING;
  }
  else
  {
    try
    {
      fstream *inlinefile = Path::openFileWriter( filestr, !flush, true );
      if (Path::putLine( *inlinefile, str ))
        str = StringConstants::EMPTY_STRING;
      else
        str = filestr;
      Path::closeFileWriter( inlinefile );
    }
    catch (IOException &e)
    {
      str = filestr;
    }
  }

  return (mod_str);
}


/**
 * WARNING: this function uses old_string and new_string as
 * work areas, so their value upon return is undefind.
**/
void Variable::substitute( String &str, String &old_string,
    String &new_string, boolean global, boolean all_wd_matches,
    boolean split_words, boolean begin_anchor, boolean end_anchor ) const
{
  static StringArray words;
  boolean made_change;

  words.clear();
  if (split_words)
  {
    str.split( Variable::WORD_SPLIT_STRING, 0, &words );
    if (words.length() < 1)
    {
      str = StringConstants::EMPTY_STRING;
      return;
    }
  }
  else
    words.add( str );

  for (int i = ARRAY_FIRST_INDEX; i <= words.lastIndex(); ++i)
  {
    made_change = false;

    if (begin_anchor && end_anchor)
    {
      if (words[i].equals( old_string ))
      {
        made_change = true;
        words[i] = new_string;
      }
    }
    else if (begin_anchor)
    {
      if (words[i].startsWith( old_string ))
      {
        made_change = true;
        words[i] = new_string + words[i].substringThis(
            STRING_FIRST_INDEX + old_string.length() );
      }
    }
    else if (end_anchor)
    {
      if (words[i].endsWith( old_string ))
      {
        made_change = true;
        words[i].remove( words[i].lastIndex() + 1 - old_string.length(),
            old_string.length() );
        words[i] += new_string;
      }
    }
    else
    {
      String replaced( words[i] );
      replaced.replaceThis( old_string, new_string,
                            all_wd_matches ? UINT_MAX : 1 );
      // must know whether or not we made a change (to preserve the
      // old ODE behavior).
      made_change = (replaced != words[i]);
      if (made_change) // this is also checked elsewhere
        words[i] = replaced;
    }

    if (!global && made_change)
      break;
  }
  concatWords( words, str );
}

void Variable::regexSubstitute( String &str, String &old_string,
    String &new_string, boolean global, boolean all_wd_matches,
    boolean split_words, boolean extended, boolean ignoreCase ) const
{
  try
  {
    Pattern expr( old_string, extended, ignoreCase, false, false );
    static StringArray words;

    words.clear();
    if (split_words)
    {
      str.split( Variable::WORD_SPLIT_STRING, 0, &words );
      if (words.length() < 1)
      {
        str = StringConstants::EMPTY_STRING;
        return;
      }
    }
    else
      words.add( str );

    for (int i = ARRAY_FIRST_INDEX; i <= words.lastIndex(); ++i)
    {
      String replaced, tmp_repl, tmp_match;
      unsigned long startScan, startMatch, stopMatch;
      boolean made_change = false;

      for (startScan = STRING_FIRST_INDEX;
           expr.match( words[i], startScan, startMatch, stopMatch );
           startScan = stopMatch)
      {
        made_change = true;
        if (startMatch > startScan) // the stuff before or between matches
          replaced += words[i].substring( startScan, startMatch );
        if (stopMatch > startMatch) // substring matched
          tmp_match = words[i].substring( startMatch, stopMatch );
        else
          tmp_match = StringConstants::EMPTY_STRING;
        regexParseString( new_string.toCharPtr(), tmp_repl, '\0', &tmp_match );
        replaced += tmp_repl; // squirt in the replacement for the match
        if (! all_wd_matches)
          break;
        if (startMatch == stopMatch) // zero length substring matched
        {
          if (stopMatch > words.lastIndex())
            break; // exit if at the end
          else
          { // start scan again at next character, and copy char we skip over
            stopMatch++;
            replaced += words[i].substring( startMatch, stopMatch );
          }
        }
      }

      if (made_change) // this is also checked elsewhere
      {
        replaced += words[i].substring( stopMatch ); // the stuff after the
                                                     // last match
        words[i] = replaced;
      }

      if (!global && made_change)
        break;
    }
    concatWords( words, str );
  }
  catch (Exception &e)
  {
    throw (MalformedVariable( String( "Malformed 'x' variable modifier; " ) +
        e.getMessage() ));
  }
}

void Variable::regexMatch( String &str, String &old_string, boolean matching,
    boolean split_words, boolean extended, boolean ignoreCase ) const
// throw (MalformedVariable)
{
  try
  {
    Pattern expr( old_string, extended, ignoreCase, false, true );
    static StringArray words;
    boolean matched;
    int i, j;

    words.clear();
    if (split_words)
      str.split( Variable::WORD_SPLIT_STRING, 0, &words );
    else
      words.add( str );

    str = StringConstants::EMPTY_STRING;
    for (i = ARRAY_FIRST_INDEX; i <= words.lastIndex(); ++i)
    {
      matched = expr.match( words[i] );
      if ((matched && matching) || (!matched && !matching))
      {
        if (str.length() > 0)
          str += StringConstants::SPACE;
        str += words[i];
      }
    }
  }
  catch (Exception &e)
  {
    throw (MalformedVariable( String( "Malformed 'x' variable modifier; " ) +
        e.getMessage() ));
  }
}

/**
 * Note: we change only_parse's value only when it's not already
 * true.  It is set only when the replacement string is NOT used
 * (when the word is an empty string), to prevent bang-sign
 * modifiers in the replacement string from being run.
 *
**/
const char *Variable::doAtSignModifier( String &str,
    const char *mod_str ) const
// throw (MalformedVariable)
{
  String varname;
  StringArray words;
  str.split( Variable::WORD_SPLIT_STRING, 0, &words );

  mod_str = parseString( mod_str, varname, StringConstants::AT_SIGN );
  if (*mod_str == '\0')
    throw (MalformedVariable(
                    "Malformed '@' variable modifier; missing second '@'" ));

  ++mod_str; // skip the middle @ sign

  if (words.length() > 0)
  {
    SetVars local_vars( vars );
    // homegrown mutable or devious underhandedness?  you be the judge.
    const SetVarLinkable **orig_vars = (const SetVarLinkable **)&vars;
    *orig_vars = &local_vars; // RED ALERT!  remember to reset this!

    const char *tmp_mod = mod_str;
    for (int i = ARRAY_FIRST_INDEX; i <= words.lastIndex(); ++i)
    {
      local_vars.set( varname, words[i], true );
      try
      {
        tmp_mod = parseString( mod_str, str, StringConstants::AT_SIGN, true );
        if (*tmp_mod == '\0')
          throw (MalformedVariable(
                  "Malformed '@' variable modifier; missing third '@'" ));
      }
      catch (MalformedVariable &e)
      {
        *orig_vars = local_vars.getParent();
        throw;
      }
      catch (Exception &e)
      {
        *orig_vars = local_vars.getParent();
        throw (MalformedVariable(
             String( "Malformed '@' variable modifier; " ) + e.getMessage() ));
      }
      catch (...)
      {
        *orig_vars = local_vars.getParent();
        throw (MalformedVariable( "Malformed '@' variable modifier" ));
      }
      words[i] = str;
    }
    mod_str = tmp_mod;
    concatWords( words, str );
    *orig_vars = local_vars.getParent();
  }
  else // just parse for mod_str's sake
  {
    Variable::only_parse = true;
    mod_str = parseString( mod_str, str, StringConstants::AT_SIGN, true );
    Variable::only_parse = false;
    str = StringConstants::EMPTY_STRING;
  }

  if (*mod_str == '\0')
    throw (MalformedVariable(
                      "Malformed '@' variable modifier; missing third '@'" ));
  ++mod_str; // now skip the final @ sign
  return (mod_str);
}

void Variable::doPathTypeModifier( String &str, char modifier ) const
{
  static StringArray words;
  words.clear();
  str.split( Variable::WORD_SPLIT_STRING, 0, &words );
  for (int i = ARRAY_FIRST_INDEX; i <= words.lastIndex(); ++i)
    doWordModifier( words[i], modifier );
  concatWords( words, str );
}

void Variable::concatWords( const StringArray &words, String &result ) const
{
  result = StringConstants::EMPTY_STRING;
  if (words.length() > 0)
  {
    for (int i = ARRAY_FIRST_INDEX; i <= words.lastIndex(); ++i)
    {
      if (words[i].length() > 0)
      {
        result += words[i];
        result += StringConstants::SPACE;
      }
    }
    if (result.length() > 0)
      result.remove( result.lastIndex(), 1 ); // remove trailing space
  }
}

void Variable::doWordModifier( String &word, char modifier ) const
{
  switch (modifier)
  {
    case 'E': // Extension (path suffix)
      Path::fileSuffixThis( word, false );
      break;
    case 'H': // Head (path without file)
      Path::filePathThis( word );
      break;
    case 'R': // Root (pathname without suffix)
      Path::fileRootThis( word, false );
      break;
    case 'T': // Tail (file without path)
      Path::fileNameThis( word );
      break;
    default:
      break; // word will not be modified
  }
}

/**
 * Note: we change only_parse's value only when it's not already
 * true.  It is set only when the replacement string is NOT used
 * (when this conditional evaluates to false), to prevent bang-
 * sign modifiers in the replacement string from being run.
 *
**/
const char *Variable::doNonBlankModifier( String &str,
    const char *mod_str, char closing_brace ) const
// throw (MalformedVariable)
{
  boolean is_blank = (str.trim().length() < 1);
  String dummy;

  if (!Variable::only_parse && is_blank)
  {
    Variable::only_parse = true;
    mod_str = parseString( mod_str, (is_blank) ? dummy : str,
        String( closing_brace ) + Variable::MODIFIER_CHAR, true );
    Variable::only_parse = false;
  }
  else
    mod_str = parseString( mod_str, (is_blank) ? dummy : str,
        String( closing_brace ) + Variable::MODIFIER_CHAR, true );
  return (mod_str);
}

/**
 * Note: we change only_parse's value only when it's not already
 * true.  It is set only when the replacement string is NOT used
 * (when this conditional evaluates to false), to prevent bang-
 * sign modifiers in the replacement string from being run.
 *
**/
const char *Variable::doBlankModifier( String &str,
    const char *mod_str, char closing_brace ) const
// throw (MalformedVariable)
{
  boolean is_blank = (str.trim().length() < 1);
  String dummy;

  if (!Variable::only_parse && !is_blank)
  {
    Variable::only_parse = true;
    mod_str = parseString( mod_str, (is_blank) ? str : dummy,
        String( closing_brace ) + Variable::MODIFIER_CHAR, true );
    Variable::only_parse = false;
  }
  else
    mod_str = parseString( mod_str, (is_blank) ? str : dummy,
        String( closing_brace ) + Variable::MODIFIER_CHAR, true );
  return (mod_str);
}

/**
 * Note: we change only_parse's value only when it's not already
 * true.  It is set only when the replacement string is NOT used
 * (when this conditional evaluates to false), to prevent bang-
 * sign modifiers in the replacement string from being run.
 *
**/
const char *Variable::doDefinedModifier( String &str,
    const char *mod_str, char closing_brace,
    const String &variable_name ) const
// throw (MalformedVariable)
{
  boolean is_defined = (vars != 0 && vars->find( variable_name ) != 0);
  String dummy;

  if (!Variable::only_parse && !is_defined)
  {
    Variable::only_parse = true;
    mod_str = parseString( mod_str, (is_defined) ? str : dummy,
        String( closing_brace ) + Variable::MODIFIER_CHAR, true );
    Variable::only_parse = false;
  }
  else
    mod_str = parseString( mod_str, (is_defined) ? str : dummy,
        String( closing_brace ) + Variable::MODIFIER_CHAR, true );
  return (mod_str);
}

/**
 * Note: we change only_parse's value only when it's not already
 * true.  It is set only when the replacement string is NOT used
 * (when this conditional evaluates to false), to prevent bang-
 * sign modifiers in the replacement string from being run.
 *
**/
const char *Variable::doUndefinedModifier( String &str,
    const char *mod_str, char closing_brace,
    const String &variable_name ) const
// throw (MalformedVariable)
{
  boolean is_defined = (vars != 0 && vars->find( variable_name ) != 0);
  String dummy;

  if (!Variable::only_parse && is_defined)
  {
    Variable::only_parse = true;
    mod_str = parseString( mod_str, (is_defined) ? dummy : str,
        String( closing_brace ) + Variable::MODIFIER_CHAR, true );
    Variable::only_parse = false;
  }
  else
    mod_str = parseString( mod_str, (is_defined) ? dummy : str,
        String( closing_brace ) + Variable::MODIFIER_CHAR, true );
  return (mod_str);
}

void Variable::doGenPathModifier( String &str ) const
// throw (MalformedVariable)
{
  static StringArray args;

  args.clear();
  str.split( Variable::WORD_SPLIT_STRING, 0, &args );
  try
  {
    if (!Genpath::run( args, str, envs ))
      throw (MalformedVariable( String( "Malformed 'G' variable modifier; "
          "invalid arguments: " ) + args.join( StringConstants::SPACE ) ));
  }
  catch (Exception &e)
  {
    throw (MalformedVariable( String( "Malformed 'G' variable modifier; " ) +
        e.getMessage() ));
  }
}

const char *Variable::doMakePathModifier( String &str,
    const char *mod_str ) const
// throw (MalformedVariable)
{
  static StringArray paths;
  boolean ignore_errors = false;

  if (*mod_str == '-')
  {
    ignore_errors = true;
    ++mod_str;
  }
  paths.clear();
  str.split( Path::PATH_SEPARATOR + Variable::WORD_SPLIT_STRING, 0, &paths );
  str = StringConstants::EMPTY_STRING;
  for (int i = paths.firstIndex(); i <= paths.lastIndex(); ++i)
  {
    if (!Path::createPath( paths[i].trimThis() ) && !ignore_errors)
      str += paths[i] + Path::PATH_SEPARATOR; // results in a trailing pathsep
  }
  if (str.length() > 0)
    str.remove( str.lastIndex(), 1 ); // remove trailing pathsep char
  return (mod_str);
}

const char *Variable::doExistsModifier( String &str,
    const char *mod_str ) const
// throw (MalformedVariable)
{
  static StringArray paths;
  static StringArray matches;

  paths.clear();
  str.split( Path::PATH_SEPARATOR + Variable::WORD_SPLIT_STRING, 0, &paths );
  if (*mod_str == '\0')
    throw (MalformedVariable(
           "Malformed 'X' variable modifier; missing submodifier after ':X';"
           " expecting 'F', 'D', or 'B'" ));
  str = StringConstants::EMPTY_STRING;
  if (*mod_str != 'F' && *mod_str != 'D' && *mod_str != 'B')
    throw (MalformedVariable(
         String( "Malformed 'X' variable modifier; invalid submodifier '" ) +
         *mod_str + "' after ':X'; expecting 'F', 'D', or 'B'" ));

  for (int i = ARRAY_FIRST_INDEX; i <= paths.lastIndex(); ++i)
  {
    if (*mod_str == 'F' || *mod_str == 'B')
    {
      matches.clear();
      if ((path_finder != 0 &&
           path_finder->stringFinder( paths[i], matches, false, false )) ||
          (path_finder == 0 && Path::isFile( paths[i] )))
      {
        str += paths[i];
        str += StringConstants::SPACE;
        continue; // for 'B', we don't want to accidentally add twice
      }
    }
    if (*mod_str == 'D' || *mod_str == 'B')
    {
      if (Path::isDirectory( paths[i] ))
      {
        str += paths[i];
        str += StringConstants::SPACE;
      }
    }
  }
  str.trimThis();
  return (mod_str + 1);
}


const char *Variable::doRemoveModifier( String &str, const char *mod_str ) const
// throw (MalformedVariable)
{
  static StringArray paths;
  boolean recursive = false;
  boolean ignore_errors = false;

  paths.clear();
  str.split( Path::PATH_SEPARATOR + Variable::WORD_SPLIT_STRING, 0, &paths );
  str = StringConstants::EMPTY_STRING;
  if (*mod_str == '\0')
    throw (MalformedVariable(
            "Malformed 'r' variable modifier; missing submodifier after ':r';"
            " expecting 'r' or 'm'" ));
  if (*mod_str != 'r' && *mod_str != 'm')
    throw (MalformedVariable(
        String( "Malformed 'r' variable modifier: invalid submodifier '" ) +
        *mod_str + "' after ':r'; expecting 'r' or 'm'" ));

  if (*mod_str == 'r')
    recursive = true;
  if (*(mod_str + 1) == '-')
    ignore_errors = true;

  for (int i = ARRAY_FIRST_INDEX; i <= paths.lastIndex(); ++i)
  {
    if (!Path::deletePath( paths[i].trimThis(), recursive, ignore_errors ) &&
        !ignore_errors)
    {
      str += paths[i];
      str += StringConstants::SPACE;
    }
  }

  str.trimThis();
  if (ignore_errors)
    return (mod_str + 2);
  else
    return (mod_str + 1);
}


const char *Variable::doFindFileModifier( String &str,
    const char *mod_str, char closing_brace ) const
// throw (MalformedVariable)
{
  StringArray paths;
  str.split( Path::PATH_SEPARATOR + Variable::WORD_SPLIT_STRING, 0, &paths );
  mod_str = parseString( mod_str, str,
      String( closing_brace ) + Variable::MODIFIER_CHAR, true );
  if (str.indexOfAny("*?[") != STRING_NOTFOUND) // if wildcards in the target
  {
    StringArray found;
    Path::findFilesInChain( str, paths, &found, path_finder->getFindDirs() );
    str = found.join( " " );
  }
  else
  {
    str = Path::findFileInChain( str, paths );
    if (!path_finder->getFindDirs() && str.length() > 0 &&
        Path::isDirectory( str ))
      str = "";
  }
  return (mod_str);
}

void Variable::doFindPathModifier( String &path, boolean dirs_only ) const
{
  static StringArray paths;
  static String dequoted_path;
  static StringArray matches;

  if (path_finder != 0)
  {
    matches.clear();
    paths.clear();
    path.split( Path::PATH_SEPARATOR + Variable::WORD_SPLIT_STRING, 0, &paths );
    path = StringConstants::EMPTY_STRING;
    for (int i = ARRAY_FIRST_INDEX; i <= paths.lastIndex(); ++i)
    {
      dequoted_path = paths[i];
      dequoted_path.dequoteThis();
      if (! path_finder->stringFinder( dequoted_path, matches, true,
                                       dirs_only ))
      {
        matches.append( paths[i]);
      }
    }

    path = matches.join( StringConstants::SPACE );
    Path::userizeThis( path );
  }
}


void Variable::doQuickSort( String &list, const char *mod_str ) const
{
  int i = 0;
  boolean ascending = false;
  static StringArray elements;
  elements.clear();

  if (*mod_str == '\0')
    throw (MalformedVariable(
            "Malformed 'Q' variable modifier; missing submodifier after ':Q';"
            " expecting '+' or '-'" ));

  if (*mod_str != '+' && *mod_str != '-')
    throw (MalformedVariable(
        String( "Malformed 'Q' variable modifier: invalid submodifier '" ) +
        *mod_str + "' after ':Q'; expecting '+' or '-'" ));

  if (*mod_str == '+')
    ascending = true;

  list.split( Variable::WORD_SPLIT_STRING, 0, &elements );

  if (elements.size() < 2)
    return; // nothing to sort

  list = StringConstants::EMPTY_STRING;
  char **sortedArray = elements.toSortedCharStarArray();

  if (ascending)
  {
    // note: this while condition requires elements to be non-empty
    for (i = 0; i <= (elements.lastIndex() - elements.firstIndex()); i++)
      list += sortedArray[i] + StringConstants::SPACE;
  }
  else
  {
    for (i = (elements.lastIndex() - elements.firstIndex()); i >= 0; i--)
      list += sortedArray[i] + StringConstants::SPACE;
  }

  delete[] sortedArray;

  list.trimThis();
}


const char *Variable::substParseString( const char *str, String &results,
    char delim, const String *old_string,
    boolean &begin_anchor, boolean &end_anchor ) const
// throw (MalformedVariable)
{
  String tmp_eval;

  results = StringConstants::EMPTY_STRING;
  if (old_string == 0 && *str == '^' && delim != '^')
  {
    begin_anchor = true;
    ++str;
  }
  while (*str != '\0' && *str != delim)
  {
    if (*str == '\\') // might be an escape character
      results += escapedChar( *(++str) );
    else if (*str == '$') // maybe a variable, maybe the anchor
    {
      if (old_string == 0 && *(str + 1) == delim)
        end_anchor = true;
      else
      {
        str = evaluateString( str, tmp_eval );
        results += tmp_eval;
        continue;
      }
    }
    else if (*str == '&' && old_string != 0)
      results += *old_string;
    else // just add the character
      results += *str;

    ++str;
  }
  return (str);
}


const char *Variable::regexParseString( const char *str, String &results,
    char delim, const String *old_string ) const
// throw (MalformedVariable)
{
  String tmp_eval;

  results = StringConstants::EMPTY_STRING;
  while (*str != '\0' && *str != delim)
  {
    if (*str == '\\') // might be an escape character
    {
      ++str;  // get the next character
      if (*str != '$' && *str != delim && (*str != '&' || old_string == 0))
      {
        results += '\\';  // the escape char is not escaping anything; copy it
        continue;
      }
      else
        results += *str; // don't copy escape; copy the escaped char
    }
    else if (*str == '$') // maybe a variable
    {
      str = evaluateString( str, tmp_eval );
      results += tmp_eval;
      continue;
    }
    else if (*str == '&' && old_string != 0)
      results += *old_string;
    else // just add the character
      results += *str;

    ++str;
  }
  return (str);
}


/**
 * Parse a string until any one of a set of characters
 * are found, evaluating variables as it goes.
 * Characters that are not part of variables are just
 * concatenated to the entire string.  Parsing will not
 * stop if one of the until_chars occurs INSIDE a
 * variable specification.
 *
 * @param str The string to parse.
 * @param until_chars The set of characters which signal
 * parsing should end.
 * @param allow_escaped_vars If true, escaped variables
 * will be allowed (though they are not evaluated at this
 * time).  If false, any escaped variables (i.e., any
 * occurrence of two consecutive dollar signs) will cause
 * a MalformedVariable exception to be thrown.
 * @param backslash_is_escape If true, the backslash character
 * will be treated as an escape character (so that only the
 * character following it is concatenated).  If false, the
 * backslash is not treated as a special character.
 * @return A two-element array: the first element contains
 * everything that was parsed before finding one of the
 * until_chars.  The second element contains everything
 * left over (the first character of which will be one of
 * the until_chars).  If no until_chars were found, the
 * second array element will be the empty string.
 * @exception COM.ibm.ode.lib.exception.MalformedVariable Indicates
 * that a variable specification is malformed somewhere in the string.
**/
StringArray *Variable::parseUntil( const String &string,
    const String &until_chars, boolean allow_escaped_vars,
    boolean backslash_is_escape, StringArray *buf, InfoPrinter *ip ) const
// throw (MalformedVariable)
{
  static String tmp_result;
  const char *str = string.toCharPtr();
  boolean quoted = false;

  if (buf == 0)
    buf = new StringArray( 2, 2 );
  else
    buf->extendTo( 2 ); // make sure buffer has two elements

  (*buf)[ARRAY_FIRST_INDEX] = StringConstants::EMPTY_STRING;
  while (*str != '\0')
  {
    if (*str == '\"')
    {
      if (until_chars.indexOf( *str ) != STRING_NOTFOUND)
        break;
      quoted = !quoted;
      (*buf)[ARRAY_FIRST_INDEX] += *(str++);
    }
    else if (!quoted && until_chars.indexOf( *str ) != STRING_NOTFOUND)
      break;
    else if (*str == '\\' && backslash_is_escape)
    {
      ++str; // add the next character instead
      if (*str == '\0') // premature end of string
        break;
      (*buf)[ARRAY_FIRST_INDEX] += escapedChar( *str );
      ++str;
    }
    else if (*str == variable_start_char) // a variable, evaluate it
    {
      if (!allow_escaped_vars && *(str+1) == variable_start_char)
        throw (MalformedVariable( String( variable_start_char ) +
            String( variable_start_char ) + " is not allowed" ));
      Variable::recursive_vars.clear();
      if (ip)
        ip->print( String( "Var: Parsing " ) + str );
      str = evaluateString( str, tmp_result, ip );
      (*buf)[ARRAY_FIRST_INDEX] += tmp_result;
      if (ip)
        ip->print( String( "Var: Result " ) +
            ( ((*buf)[ARRAY_FIRST_INDEX].length() == 0) ?
            StringConstants::DOUBLE_QUOTE + StringConstants::DOUBLE_QUOTE :
            String( "`" ) + (*buf)[ARRAY_FIRST_INDEX] + str +
            StringConstants::SINGLE_QUOTE ) );
    }
    else
      (*buf)[ARRAY_FIRST_INDEX] += *(str++);
  }
  (*buf)[ARRAY_FIRST_INDEX + 1] = str;

  return (buf);
}



/**
 * envVarEval parses a string, and any variables in the string are assumed
 * to be environment variables.  A string is returned that is the result
 * of the parse.
**/
// throw (MalformedVariable)
String Variable::envVarEval( const String &dir )
{
  String temp_dir;
  if (dir.indexOf( '$' ) == STRING_NOTFOUND)
    temp_dir = dir;
  else
  {
    static Variable var_eval( Env::getSetVars(), false, '$' );
    StringArray dirEval( 2, 2 );

    var_eval.parseUntil( dir, StringConstants::EMPTY_STRING,
        true, false, &dirEval, 0 );
    temp_dir = dirEval[ARRAY_FIRST_INDEX];
  }
  return (temp_dir);
}


/************************************************
 *
 */
void VarInfoPrinter::print( const String &msg )
{
  Interface::printAlways( msg );
}
