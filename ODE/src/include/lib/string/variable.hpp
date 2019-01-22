/**
 * Variable
**/
#ifndef _ODE_LIB_STRING_VARIABLE_HPP_
#define _ODE_LIB_STRING_VARIABLE_HPP_

/**
 * Character that signals the beginning of a variable.
**/
#define VARIABLE_START_CHAR '$'

#include "lib/portable/env.hpp"
#include "lib/string/setvars.hpp"
#include "lib/string/strfind.hpp"
#include "lib/string/svarlink.hpp"
#include "lib/string/strcon.hpp"
#include "lib/portable/vector.hpp"

class InfoPrinter
{
  public:
    virtual void print( const String &msg ) = 0;
};

class VarInfoPrinter : public InfoPrinter
{
  public:
    void print( const String &msg );
};

/**
 * Makefile variable functionality.
 *
 * Constructor notes:
 *
 * @param vars The variable list hierarchy to use when
 * obtaining the values of variables.
 * @param path_finder The object which is used to find
 * the path of a file (used by the :P modifier).  If null
 * is passed, :P will simply evaluate to the filename.
 * @param virtual_cwd The virtual current working directory.
 * This is used with the :! modifier; a change to
 * this dir is prepended to the given command.
 * @param envs The environment variables to use when running
 * the :! modifier's command.  If null, Env() is used.
 * @param allow_modifiers If true, allow variable modifiers
 * to be used.  If false, throw a MalformedVariable exception
 * if modifiers are used.
**/
class Variable
{
  public:

    Variable( const SetVarLinkable *vars = Env::getSetVars(),
        const StringFindable *path_finder = 0,
        const String &virtual_cwd = StringConstants::EMPTY_STRING,
        const SetVars *envs = Env::getSetVars(),
        boolean allow_modifiers = true,
        char var_start_char = VARIABLE_START_CHAR ) :
        vars( vars ), envs( envs ),
        path_finder( path_finder ), virtual_cwd( virtual_cwd ),
        allow_modifiers( allow_modifiers ),
        variable_start_char( var_start_char ) {};
    Variable( const SetVarLinkable *vars, boolean allow_modifiers,
        char var_start_char = VARIABLE_START_CHAR ) :
        vars( vars ), envs( Env::getSetVars() ),
        path_finder( 0 ), virtual_cwd( StringConstants::EMPTY_STRING ),
        allow_modifiers( allow_modifiers ),
        variable_start_char( var_start_char ) {};
    Variable( const SetVarLinkable *vars,
        const StringFindable *path_finder,
        boolean allow_modifiers,
        char var_start_char = VARIABLE_START_CHAR ) :
        vars( vars ), envs( Env::getSetVars() ),
        path_finder( path_finder ),
        virtual_cwd( StringConstants::EMPTY_STRING ),
        allow_modifiers( allow_modifiers ),
        variable_start_char( var_start_char ) {};
    Variable( const SetVarLinkable *vars,
        const StringFindable *path_finder,
        const String &virtual_cwd, boolean allow_modifiers,
        char var_start_char = VARIABLE_START_CHAR ) :
        vars( vars ), envs( Env::getSetVars() ),
        path_finder( path_finder ), virtual_cwd( virtual_cwd ),
        allow_modifiers( allow_modifiers ),
        variable_start_char( var_start_char ) {};

    inline const SetVarLinkable *getVars() const;
    inline static char escapedChar( char ch );
    StringArray *evaluate( const String &str, StringArray *buf = 0 ) const;
    StringArray *parseUntil( const String &str, const String &until_chars,
        boolean allow_escaped_vars = true, boolean backslash_is_escape = false,
        StringArray *buf = 0, InfoPrinter *ip = 0  ) const;
    static String envVarEval( const String &dir );
    static String runShellCmd( const String  &val_str,
                               const SetVars *envs,
                               const SetVars *globals,
                               const String  &opName,
                                     boolean checkGlobals,
                                     boolean echoCmd     = false,
                                     boolean ignoreError = false );
    static const String ODEDLLPORT EOL_CHARS_STRING; // "\r\n"


  private:

    static const char ODEDLLPORT MODIFIER_CHAR; // ':'
    static const char ODEDLLPORT GLOBAL_SUBST_CHAR; // 'g'
    static const char ODEDLLPORT GLOBAL_SUBST_FIRST_MATCH_CHAR; // 'f'
    static const char ODEDLLPORT GLOBAL_SUBST_ALL_WORD_CHAR; // 'w'
    static const char ODEDLLPORT REGEX_IGNORE_CASE_CHAR; // 'i'
    static const char ODEDLLPORT REGEX_CONDITIONAL_IGNORE_CASE_CHAR; // 'c'
    static const char ODEDLLPORT REGEX_EXTENDED_SYNTAX_CHAR; // 'e'
    static const String ODEDLLPORT WORD_SPLIT_STRING; // " \t\r\n"
    static StringArray ODEDLLPORT recursive_vars;
    static boolean ODEDLLPORT only_parse;
    const SetVarLinkable *const vars;
    const SetVars *const envs;
    const StringFindable *const path_finder;
    const String virtual_cwd;
    const boolean allow_modifiers;
    const char variable_start_char; // usually $

    const char *substParseString( const char *str, String &results,
        char delim, const String *old_string,
        boolean &begin_anchor, boolean &end_anchor ) const;
    const char *evaluateString( const char *str,
        String &result, InfoPrinter *ip = 0  ) const;
    void checkRecursiveVars( const String &variable_name ) const;
    const char *parseString( const char *str, String &results,
        const String &until_chars = StringConstants::EMPTY_STRING,
        boolean backslash_is_escape = false, InfoPrinter *ip = 0,
        boolean eval_vars = true ) const;
    const char *regexParseString( const char *str, String &results,
        char delim, const String *old_string ) const;
	 const char *doIndexModifier( String &str, const char *mod_str,
	     char closing_brace ) const;
    inline const char *doMatchingModifier( String &str,
        const char *mod_str, char closing_brace, boolean split_words ) const;
    inline const char *doNonMatchingModifier( String &str,
        const char *mod_str, char closing_brace, boolean split_words ) const;
    const char *evaluateModifiers( const String &str,
        const char *mod_str, const String &variable_name,
        char closing_brace, String &result, InfoPrinter *ip = 0  ) const;
    const char *doMatchTypeModifier( String &str,
        const char *mod_str, char closing_brace, boolean matching,
        boolean split_words) const;
    const char *doSubstitutionModifier( String &str,
        const char *mod_str, boolean split_words ) const;
    const char *doRegexModifier( String &str,
        const char *mod_str ) const;
    const char *doBangSignModifier( String &str,
        const char *mod_str ) const;
    void convertEOLChars( const String &input, String &output ) const;
    const char *doAppendModifier( String &str, const char *mod_str,
                        char closing_brace, boolean flush ) const;
    const char *doEqualSignModifier( String &str,
        const char *mod_str, char closing_brace ) const;
    void substitute( String &str, String &old_string, String &new_string,
        boolean global, boolean all_wd_matches, boolean split_words,
        boolean begin_anchor, boolean end_anchor ) const;
    void regexSubstitute( String &str, String &old_string, String &new_string,
        boolean global, boolean all_wd_matches, boolean split_words,
        boolean extended, boolean ignoreCase ) const;
    void regexMatch( String &str, String &old_string, boolean matching,
        boolean split_words, boolean extended, boolean ignoreCase ) const;
    const char *doAtSignModifier( String &str, const char *mod_str ) const;
    void doPathTypeModifier( String &str, char modifier ) const;
    void concatWords( const StringArray &words, String &result ) const;
    void doWordModifier( String &word, char modifier ) const;
    const char *doBlankModifier( String &str,
        const char *mod_str, char closing_brace ) const;
    const char *doNonBlankModifier( String &str,
        const char *mod_str, char closing_brace ) const;
    const char *doDefinedModifier( String &str, const char *mod_str,
        char closing_brace, const String &variable_name ) const;
    const char *doUndefinedModifier( String &str, const char *mod_str,
        char closing_brace, const String &variable_name ) const;
    const char *doExistsModifier( String &str, const char *mod_str ) const;
    const char *doRemoveModifier( String &str, const char *mod_str ) const;
    const char *doFindFileModifier( String &str, const char *mod_str,
        char closing_brace ) const;
    void doQuickSort( String &list, const char *mod_str ) const;
    void doFindPathModifier( String &str, boolean dirs_only ) const;
    const char *doMakePathModifier( String &str, const char *mod_str ) const;
    void doGenPathModifier( String &str ) const;
};

/**
 * Get the variable hierarchy being used to search for
 * variables.
 *
 * @return The SetVarLinkable object which evaluate() uses to
 * search for variables.
 */
inline const SetVarLinkable *Variable::getVars() const
{
  return (vars);
}

inline const char *Variable::doMatchingModifier( String &str,
    const char *mod_str, char closing_brace, boolean split_words ) const
// throws MalformedVariable
{
  return (doMatchTypeModifier( str, mod_str, closing_brace, true,
                               split_words ));
}

inline const char *Variable::doNonMatchingModifier( String &str,
    const char *mod_str, char closing_brace, boolean split_words ) const
// throws MalformedVariable
{
  return (doMatchTypeModifier( str, mod_str, closing_brace, false,
                               split_words ));
}

/**
 * This could be used to implement special characters
 * like \n, \t, etc.  For now, just return the character as-is.
 * This might be moved to a more appropriate class, too.
 *
 * @param ch The escaped character.
 * @return The escaped version of the character.
 */
inline char Variable::escapedChar( char ch )
{
  return (ch);
}


#endif /* _ODE_LIB_STRING_VARIABLE_HPP_ */
