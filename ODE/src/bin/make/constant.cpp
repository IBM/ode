using namespace std;
/**
 * Constants
 *
**/
using namespace std;

#define _ODE_BIN_MAKE_CONSTANT_CPP_

#include "base/binbase.hpp"
#include "bin/make/constant.hpp"

const char Constants::SUFF_SEP = '|'; // Separates suffixes

#ifdef __WEBMAKE__
const String Constants::MAKE_NAME       = "webmake";
#else
const String Constants::MAKE_NAME       = "mk";
#endif // __WEBMAKE__

const String Constants::DOT_DIRS        = ".DIRS";
const String Constants::DOT_EXEC        = ".EXEC";
const String Constants::DOT_IGNORE      = ".IGNORE";
const String Constants::DOT_INVISIBLE   = ".INVISIBLE";
const String Constants::DOT_JOIN        = ".JOIN";
const String Constants::DOT_LINKS       = ".LINKS";
const String Constants::DOT_MAKE        = ".MAKE";
const String Constants::DOT_NOREMOTE    = ".NOREMOTE";
const String Constants::DOT_NOTMAIN     = ".NOTMAIN";
const String Constants::DOT_OPTIONAL    = ".OPTIONAL";
const String Constants::DOT_PASSES      = ".PASSES";
const String Constants::DOT_PMAKE       = ".PMAKE";
const String Constants::DOT_PRECIOUS    = ".PRECIOUS";
const String Constants::DOT_RECURSIVE   = ".RECURSIVE";
const String Constants::DOT_REPLSRCS    = ".REPLSRCS";
const String Constants::DOT_SILENT      = ".SILENT";
const String Constants::DOT_SPECTARG    = ".SPECTARG";
const String Constants::DOT_USE         = ".USE";
const String Constants::DOT_PRECMDS     = ".PRECMDS";
const String Constants::DOT_POSTCMDS    = ".POSTCMDS";
const String Constants::DOT_REPLCMDS    = ".REPLCMDS";
const String Constants::DOT_NORMTARG    = ".NORMTARG";
const String Constants::DOT_FORCEBLD    = ".FORCEBLD";

// Special targets used in make
const String Constants::DOT_BEGIN       = ".BEGIN";
const String Constants::DOT_DEFAULT     = ".DEFAULT";
const String Constants::DOT_END         = ".END";
const String Constants::DOT_ERROR       = ".ERROR";
const String Constants::DOT_EXIT        = ".EXIT";
const String Constants::DOT_INCLUDES    = ".INCLUDES";
const String Constants::DOT_INTERRUPT   = ".INTERRUPT";
const String Constants::DOT_LIBS        = ".LIBS";
const String Constants::DOT_LINKTARGS   = ".LINKTARGS";
const String Constants::DOT_MAIN        = ".MAIN";
const String Constants::DOT_MAKEFLAGS   = ".MAKEFLAGS";
const String Constants::DOT_MFLAGS      = ".MFLAGS";
const String Constants::DOT_NOTPARALLEL = ".NOTPARALLEL";
const String Constants::DOT_NULL        = ".NULL";
const String Constants::DOT_ORDER       = ".ORDER";
const String Constants::DOT_PATH        = ".PATH";
const String Constants::DOT_SUFFIXES    = ".SUFFIXES";

const String Constants::DOT_TARGET      = ".TARGET";
const String Constants::DOT_TARGETS     = ".TARGETS";
const String Constants::DOT_PREFIX      = ".PREFIX";
const String Constants::DOT_IMPSRC      = ".IMPSRC";
const String Constants::DOT_ALLSRC      = ".ALLSRC";
const String Constants::DOT_ARCHIVE     = ".ARCHIVE";
const String Constants::DOT_MEMBER      = ".MEMBER";
const String Constants::DOT_OODATE      = ".OODATE";

// obsolete
const String Constants::DOT_CURDIR     = ".CURDIR";

const String Constants::CURDIR         = "CURDIR";
const String Constants::POUND          = "POUND";
const String Constants::MAKECONF       = "Makeconf";
const String Constants::MAKEACTION     = "MAKEACTION";
const String Constants::MAKEPASS       = "MAKEPASS";
const String Constants::MAKESLEEP      = "MAKESLEEP";
const String Constants::MAKEFILE       = "MAKEFILE";
const String Constants::MAKETOP        = "MAKETOP";
const String Constants::MAKEDIR        = "MAKEDIR";
const String Constants::MAKESUB        = "MAKESUB";
const String Constants::MAKEINCLUDECOMPAT = "MAKEINCLUDECOMPAT";
const String Constants::MAKE           = "MAKE";
const String Constants::MAKESYSPATH    = "MAKESYSPATH";
const String Constants::MAKEFLAGS      = "MAKEFLAGS";
const String Constants::MACHINE        = "MACHINE";
const String Constants::ODERELEASE     = "ODERELEASE";
const String Constants::DEFAULT        = "DEFAULT";
const String Constants::VPATH          = "VPATH";

const String Constants::UNTIL_CHARS    = "=:!";
const String Constants::EMPTY_TARGET   = "Empty\tTarget";

const String Constants::DOT_INCLUDE    = ".include";
const String Constants::DOT_TRYINCLUDE = ".tryinclude";
const String Constants::DOT_UNDEF      = ".undef";
const String Constants::DOT_IFDEF      = ".ifdef";
const String Constants::DOT_IFNDEF     = ".ifndef";
const String Constants::DOT_IFNMAKE    = ".ifnmake";
const String Constants::DOT_IFMAKE     = ".ifmake";
const String Constants::DOT_IF         = ".if";
const String Constants::DOT_ELSE       = ".else";
const String Constants::DOT_ELIFDEF    = ".elifdef";
const String Constants::DOT_ELIFNDEF   = ".elifndef";
const String Constants::DOT_ELIFMAKE   = ".elifmake";
const String Constants::DOT_ELIFNMAKE  = ".elifnmake";
const String Constants::DOT_ELIF       = ".elif";
const String Constants::DOT_ENDIF      = ".endif";

const String Constants::DOT_RIF         = ".rif";
const String Constants::DOT_RELSE       = ".relse";
const String Constants::DOT_RELIF       = ".relif";
const String Constants::DOT_RENDIF      = ".rendif";
const String Constants::DOT_RFOR        = ".rfor";
const String Constants::DOT_RENDFOR     = ".rendfor";
