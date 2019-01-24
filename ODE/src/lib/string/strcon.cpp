/**
 * StringConstants
 *
**/
using namespace std ;
#define _ODE_LIB_STRING_STRCON_CPP_
#include "lib/string/strcon.hpp"
#include "lib/portable/env.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/native/file.h"

const String StringConstants::HOME_VAR = "HOME";
const String StringConstants::SHELL_VAR = "SHELL";
const String StringConstants::SHELL_K_FLAG_VAR = "SHELL_K_FLAG";
const String StringConstants::USER_VAR = "USER";
const String StringConstants::UNKNOWN_USERNAME = "guest";
const String StringConstants::VAR_SEP_STRING = "=";
const String StringConstants::MACHINE_VAR = "MACHINE";

const String StringConstants::ODEMAKE_BOMSHOWALL_VAR  = "ODEMAKE_BOMSHOWALL";
const String StringConstants::ODEMAKE_BOMSHOWTIME_VAR = "ODEMAKE_BOMSHOWTIME";
const String StringConstants::ODEMAKE_DOLLARS_VAR = "ODEMAKE_DOLLARS";
const String StringConstants::ODEMAKE_NOCONTSPC_VAR = "ODEMAKE_NOCONTSPC";
const String StringConstants::ODEMAKE_SHELL_VAR   = "ODEMAKE_SHELL";
const String StringConstants::ODEMAKE_RHOST_VAR   = "ODEMAKE_RHOST";
const String StringConstants::ODEMAKE_RSHELL_VAR  = "ODEMAKE_RSHELL";
const String StringConstants::ODEMAKE_RUSER_VAR   = "ODEMAKE_RUSER";
const String StringConstants::ODEMAKE_RTMPDIR_VAR = "ODEMAKE_RTMPDIR";
const String StringConstants::ODEMAKE_RCMDPREPEND_VAR = "ODEMAKE_RCMDPREPEND";
const String StringConstants::ODEMAKE_RCMDAPPEND_VAR  = "ODEMAKE_RCMDAPPEND";
const String StringConstants::ODEMAKE_RSHELLTYPE_VAR  = "ODEMAKE_RSHELLTYPE";
const String StringConstants::ODEMAKE_RDCELOGIN_VAR   = "ODEMAKE_RDCELOGIN";
const String StringConstants::ODEMAKE_RDCECMD_QUOTED_VAR =
    "ODEMAKE_RDCECMD_QUOTED";
const String StringConstants::ODEMAKE_TFMFIRST_VAR = "ODEMAKE_TFMFIRST";

#if defined(REMSH_IS_REMOTE_SHELL)
const String StringConstants::ODEMAKE_RSHELL_DEFAULT = "remsh";
#elif defined(REXEC_IS_REMOTE_SHELL)
const String StringConstants::ODEMAKE_RSHELL_DEFAULT = "rexec";
#else /* default is rsh */
const String StringConstants::ODEMAKE_RSHELL_DEFAULT = "rsh";
#endif

#ifdef DEFAULT_SHELL_IS_CMD
const String StringConstants::ODEMAKE_RCMDPREPEND_DEFAULT = "";
#else
const String StringConstants::ODEMAKE_RCMDPREPEND_DEFAULT = ". ~/.profile ; ";
#endif

const String StringConstants::ODEMAKE_RSHELLTYPE_SH = "sh";
const String StringConstants::ODEMAKE_RSHELLTYPE_KSH = "ksh";
const String StringConstants::ODEMAKE_RSHELLTYPE_CSH = "csh";
const String StringConstants::ODEMAKE_RSHELLTYPE_CMD = "cmd";

const String StringConstants::ODEMAKE_RSHELLTYPE_DEFAULT =
#if defined(DEFAULT_SHELL_IS_SH)
    StringConstants::ODEMAKE_RSHELLTYPE_SH;
#elif defined(DEFAULT_SHELL_IS_CSH)
    StringConstants::ODEMAKE_RSHELLTYPE_CSH;
#elif defined(DEFAULT_SHELL_IS_CMD)
    StringConstants::ODEMAKE_RSHELLTYPE_CMD;
#else /* default is ksh */
    StringConstants::ODEMAKE_RSHELLTYPE_KSH;
#endif

const String StringConstants::EMPTY_STRING;
const String StringConstants::DOLLAR_SIGN   = "$";
const String StringConstants::SPACE         = " ";
const String StringConstants::DOUBLE_QUOTE  = "\"";
const String StringConstants::SINGLE_QUOTE  = "\'";
const String StringConstants::EXCLAMATION   = "!";
const String StringConstants::EQUAL_SIGN    = "=";
const String StringConstants::AMPERSAND     = "&";
const String StringConstants::AT_SIGN       = "@";
const String StringConstants::OPEN_PAREN    = "(";
const String StringConstants::CLOSE_PAREN   = ")";
const String StringConstants::PERIOD        = ".";
const String StringConstants::LESS_THAN     = "<";
const String StringConstants::BIGGER_THAN   = ">";
const String StringConstants::PERCENT_SIGN  = "%";
const String StringConstants::STAR_SIGN     = "*";
const String StringConstants::POUND_SIGN    = "#";
const String StringConstants::QUESTION_MARK = "?";
const String StringConstants::FORW_SLASH    = "/";
const String StringConstants::BACK_SLASH    = "\\";
const String StringConstants::SPACE_TAB     = " \t";
const String StringConstants::TAB           = "\t";
const String StringConstants::UNDERSCORE    = "_";
const String StringConstants::NEWLINE       = "\n";
const String StringConstants::COLON         = ":";
const String StringConstants::SEMICOLON     = ";";
const String StringConstants::BAR           = "|";


const String &StringConstants::getODEMAKE_RSHELL()
{
  static const String *envptr = Env::getenv( ODEMAKE_RSHELL_VAR );
  static const String ODEMAKE_RSHELL(
      ((envptr == 0) ? ODEMAKE_RSHELL_DEFAULT : *envptr) );

  return (ODEMAKE_RSHELL);
}

const String &StringConstants::getODEMAKE_RUSER()
{
  static const String *envptr = Env::getenv( ODEMAKE_RUSER_VAR );
  static const String ODEMAKE_RUSER(
      ((envptr == 0) ? String( "" ) : *envptr) );

  return (ODEMAKE_RUSER);
}

const String &StringConstants::getODEMAKE_RCMDPREPEND()
{
  static const String *envptr = Env::getenv( ODEMAKE_RCMDPREPEND_VAR );
  static const String ODEMAKE_RCMDPREPEND(
      ((envptr == 0) ? ODEMAKE_RCMDPREPEND_DEFAULT : *envptr) );

  return (ODEMAKE_RCMDPREPEND);
}

const String &StringConstants::getODEMAKE_RCMDAPPEND()
{
  static const String *envptr = Env::getenv( ODEMAKE_RCMDAPPEND_VAR );
  static const String ODEMAKE_RCMDAPPEND(
      ((envptr == 0) ? String( "" ) : *envptr) );

  return (ODEMAKE_RCMDAPPEND);
}

const String &StringConstants::getODEMAKE_RSHELLTYPE()
{
  static const String *envptr = Env::getenv( ODEMAKE_RSHELLTYPE_VAR );
  static const String ODEMAKE_RSHELLTYPE(
      ((envptr == 0) ? ODEMAKE_RSHELLTYPE_DEFAULT : *envptr) );

  return (ODEMAKE_RSHELLTYPE);
}

const String &StringConstants::getODEMAKE_RDCELOGIN()
{
  static const String *envptr = Env::getenv( ODEMAKE_RDCELOGIN_VAR );
  static const String ODEMAKE_RDCELOGIN(
      ((envptr == 0) ? String( "" ) : *envptr) );

  return (ODEMAKE_RDCELOGIN);
}

const String &StringConstants::getODEMAKE_RTMPDIR()
{
  static const String *envptr = Env::getenv( ODEMAKE_RTMPDIR_VAR );
  static const String ODEMAKE_RTMPDIR(
      ((envptr == 0) ? String( "" ) : *envptr) );

  return (ODEMAKE_RTMPDIR);
}

/**
 * A little extra processing is done on this variable, since
 * the value of the variable isn't important...only that it
 * is non-blank.  So we must dequote and trim it here.
**/
const String &StringConstants::getODEMAKE_RDCECMD_QUOTED()
{
  static const String *envptr = Env::getenv( ODEMAKE_RDCECMD_QUOTED_VAR );
  static String ODEMAKE_RDCECMD_QUOTED(
      ((envptr == 0) ? String( "" ) : *envptr) );
  ODEMAKE_RDCECMD_QUOTED.dequoteThis().trimThis();

  return (ODEMAKE_RDCECMD_QUOTED);
}
