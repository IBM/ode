/**
 * StringConstants
 *
**/
#ifndef _ODE_LIB_STRING_STRCON_HPP_
#define _ODE_LIB_STRING_STRCON_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"


/**
 * Commonly used String constants.
**/
class StringConstants
{
  public:

    // environment variable names, normally not used by classes
    // (the get() functions below are typically used instead
    // to retrieve the variable's values)
    static const String ODEDLLPORT HOME_VAR; // "HOME";
    static const String ODEDLLPORT SHELL_VAR; // "SHELL";
    static const String ODEDLLPORT SHELL_K_FLAG_VAR; // "SHELL_K_FLAG";
    static const String ODEDLLPORT USER_VAR; // "USER";
    static const String ODEDLLPORT UNKNOWN_USERNAME; // "guest";
    static const String ODEDLLPORT VAR_SEP_STRING; // "=";
    static const String ODEDLLPORT MACHINE_VAR; // "MACHINE";

    static const String ODEDLLPORT ODEMAKE_BOMSHOWALL_VAR;
    static const String ODEDLLPORT ODEMAKE_BOMSHOWTIME_VAR;
    static const String ODEDLLPORT ODEMAKE_DOLLARS_VAR; // "ODEMAKE_DOLLARS";
    static const String ODEDLLPORT ODEMAKE_NOCONTSPC_VAR;
    static const String ODEDLLPORT ODEMAKE_SHELL_VAR;   // "ODEMAKE_SHELL";
    static const String ODEDLLPORT ODEMAKE_RHOST_VAR;
    static const String ODEDLLPORT ODEMAKE_RSHELL_VAR;
    static const String ODEDLLPORT ODEMAKE_RUSER_VAR;
    static const String ODEDLLPORT ODEMAKE_RTMPDIR_VAR;
    static const String ODEDLLPORT ODEMAKE_RCMDPREPEND_VAR;
    static const String ODEDLLPORT ODEMAKE_RCMDAPPEND_VAR;
    static const String ODEDLLPORT ODEMAKE_RSHELLTYPE_VAR;
    static const String ODEDLLPORT ODEMAKE_RDCELOGIN_VAR;
    static const String ODEDLLPORT ODEMAKE_RDCECMD_QUOTED_VAR;
    static const String ODEDLLPORT ODEMAKE_TFMFIRST_VAR;

    // used to get the value of the above environment variables
    // (returns default values if not set in the environment,
    // or an empty string if there is no default).
    static const String &getODEMAKE_RSHELL();
    static const String &getODEMAKE_RUSER();
    static const String &getODEMAKE_RTMPDIR();
    static const String &getODEMAKE_RCMDPREPEND();
    static const String &getODEMAKE_RCMDAPPEND();
    static const String &getODEMAKE_RSHELLTYPE();
    static const String &getODEMAKE_RDCELOGIN();
    static const String &getODEMAKE_RDCECMD_QUOTED();

    // valid types for the return value of getODEMAKE_RSHELLTYPE()
    static const String ODEDLLPORT ODEMAKE_RSHELLTYPE_SH;
    static const String ODEDLLPORT ODEMAKE_RSHELLTYPE_KSH;
    static const String ODEDLLPORT ODEMAKE_RSHELLTYPE_CSH;
    static const String ODEDLLPORT ODEMAKE_RSHELLTYPE_CMD;

    static const String ODEDLLPORT EMPTY_STRING;
    static const String ODEDLLPORT DOLLAR_SIGN;
    static const String ODEDLLPORT SPACE;
    static const String ODEDLLPORT DOUBLE_QUOTE; // "
    static const String ODEDLLPORT SINGLE_QUOTE; // '
    static const String ODEDLLPORT EXCLAMATION;
    static const String ODEDLLPORT EQUAL_SIGN;
    static const String ODEDLLPORT AMPERSAND;
    static const String ODEDLLPORT AT_SIGN;
    static const String ODEDLLPORT OPEN_PAREN;
    static const String ODEDLLPORT CLOSE_PAREN;
    static const String ODEDLLPORT PERIOD;
    static const String ODEDLLPORT LESS_THAN;
    static const String ODEDLLPORT BIGGER_THAN;
    static const String ODEDLLPORT PERCENT_SIGN;
    static const String ODEDLLPORT STAR_SIGN;
    static const String ODEDLLPORT POUND_SIGN;
    static const String ODEDLLPORT QUESTION_MARK;
    static const String ODEDLLPORT FORW_SLASH;
    static const String ODEDLLPORT BACK_SLASH;
    static const String ODEDLLPORT SPACE_TAB;
    static const String ODEDLLPORT TAB;
    static const String ODEDLLPORT UNDERSCORE;
    static const String ODEDLLPORT NEWLINE;
    static const String ODEDLLPORT COLON;
    static const String ODEDLLPORT SEMICOLON;
    static const String ODEDLLPORT BAR;

  private:

    // default values if not set in the environment
    static const String ODEDLLPORT ODEMAKE_RSHELL_DEFAULT;
    static const String ODEDLLPORT ODEMAKE_RCMDPREPEND_DEFAULT;
    static const String ODEDLLPORT ODEMAKE_RSHELLTYPE_DEFAULT;
};

#endif /* _ODE_LIB_STRING_STRCON_HPP_ */
