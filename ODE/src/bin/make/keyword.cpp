/**
 * Keyword
 *
**/
#define _ODE_BIN_MAKE_KEYWORD_CPP_

#include <base/binbase.hpp>
#include "bin/make/keyword.hpp"

Hashtable< String, Keyword *> Keyword::special_sources( 15 );
Hashtable< String, Keyword *> Keyword::special_targets( 20 );
Keyword Keyword::pathKeyword;

boolean Keyword::initialize()
{
  // Initialize the special sources
  special_sources.put( Constants::DOT_DIRS,
    new Keyword(Constants::DOT_DIRS, Constants::OP_DIRS, true) );
  special_sources.put( Constants::DOT_EXEC,
    new Keyword(Constants::DOT_EXEC, Constants::OP_EXEC, true) );
  special_sources.put( Constants::DOT_IGNORE,
    new Keyword(Constants::DOT_IGNORE, Constants::OP_IGNORE, true) );
  special_sources.put( Constants::DOT_INVISIBLE,
    new Keyword(Constants::DOT_INVISIBLE, Constants::OP_INVISIBLE, true) );
  special_sources.put( Constants::DOT_JOIN,
    new Keyword(Constants::DOT_JOIN, Constants::OP_JOIN, true) );
  special_sources.put( Constants::DOT_LINKS,
    new Keyword(Constants::DOT_LINKS, Constants::OP_LINK, true) );
  special_sources.put( Constants::DOT_MAKE,
    new Keyword(Constants::DOT_MAKE, Constants::OP_MAKE, true) );
  special_sources.put( Constants::DOT_NOREMOTE,
    new Keyword(Constants::DOT_NOREMOTE, Constants::OP_NOREMOTE, true) );
  special_sources.put( Constants::DOT_NOTMAIN,
    new Keyword(Constants::DOT_NOTMAIN, Constants::OP_NOTMAIN, true) );
  special_sources.put( Constants::DOT_OPTIONAL,
    new Keyword(Constants::DOT_OPTIONAL, Constants::OP_OPTIONAL, true) );
  special_sources.put( Constants::DOT_PASSES,
    new Keyword(Constants::DOT_PASSES, Constants::OP_PASSES, true) );
  special_sources.put( Constants::DOT_PMAKE,
    new Keyword(Constants::DOT_PMAKE, Constants::OP_PMAKE, true) );
  special_sources.put( Constants::DOT_PRECIOUS,
    new Keyword(Constants::DOT_PRECIOUS, Constants::OP_PRECIOUS, true) );
  special_sources.put( Constants::DOT_RECURSIVE,
    new Keyword(Constants::DOT_RECURSIVE, Constants::OP_MAKE, true) );
  special_sources.put( Constants::DOT_REPLSRCS,
    new Keyword(Constants::DOT_REPLSRCS, Constants::OP_REPLSRCS, true) );
  special_sources.put( Constants::DOT_SILENT,
    new Keyword(Constants::DOT_SILENT, Constants::OP_SILENT, true) );
  special_sources.put( Constants::DOT_SPECTARG,
    new Keyword(Constants::DOT_SPECTARG, Constants::OP_SPECTARG, true) );
  special_sources.put( Constants::DOT_USE,
    new Keyword(Constants::DOT_USE, Constants::OP_USE, true) );
  special_sources.put( Constants::DOT_PRECMDS,
    new Keyword(Constants::DOT_PRECMDS, Constants::OP_PRECMDS, true) );
  special_sources.put( Constants::DOT_POSTCMDS,
    new Keyword(Constants::DOT_POSTCMDS, Constants::OP_POSTCMDS, true) );
  special_sources.put( Constants::DOT_REPLCMDS,
    new Keyword(Constants::DOT_REPLCMDS, Constants::OP_REPLCMDS, true) );
  special_sources.put( Constants::DOT_NORMTARG,
    new Keyword(Constants::DOT_NORMTARG, Constants::OP_NORMTARG, true) );
  special_sources.put( Constants::DOT_FORCEBLD,
    new Keyword(Constants::DOT_FORCEBLD, Constants::OP_FORCEBLD, true) );
  special_sources.put( Constants::DOT_LINKTARGS,
    new Keyword(Constants::DOT_LINKTARGS, Constants::OP_LINKTARGS, true) );

  // Initialize the special targets
  special_targets.put( Constants::DOT_BEGIN,
    new Keyword(Constants::DOT_BEGIN, Constants::TGT_BEGIN, true) );
  special_targets.put( Constants::DOT_DEFAULT,
    new Keyword(Constants::DOT_DEFAULT, Constants::TGT_DEFAULT, true) );
  special_targets.put( Constants::DOT_END,
    new Keyword(Constants::DOT_END, Constants::TGT_END, true) );
  special_targets.put( Constants::DOT_ERROR,
    new Keyword(Constants::DOT_ERROR, Constants::TGT_ERROR, true) );
  special_targets.put( Constants::DOT_EXIT,
    new Keyword(Constants::DOT_EXIT, Constants::TGT_EXIT, true) );
  special_targets.put( Constants::DOT_IGNORE,
    new Keyword(Constants::DOT_IGNORE, Constants::TGT_IGNORE, true) );
  special_targets.put( Constants::DOT_INCLUDES,
    new Keyword(Constants::DOT_INCLUDES, Constants::TGT_INCLUDES, true) );
  special_targets.put( Constants::DOT_INTERRUPT,
    new Keyword(Constants::DOT_INTERRUPT, Constants::TGT_INTERRUPT, true) );
  special_targets.put( Constants::DOT_LIBS,
    new Keyword(Constants::DOT_LIBS, Constants::TGT_LIBS, true) );
  special_targets.put( Constants::DOT_LINKTARGS,
    new Keyword(Constants::DOT_LINKTARGS, Constants::TGT_LINKTARGS, true) );
  special_targets.put( Constants::DOT_MAIN,
    new Keyword(Constants::DOT_MAIN, Constants::TGT_MAIN, true) );
  special_targets.put( Constants::DOT_MAKEFLAGS,
    new Keyword(Constants::DOT_MAKEFLAGS, Constants::TGT_MAKEFLAGS, true) );
  special_targets.put( Constants::DOT_MFLAGS,
    new Keyword(Constants::DOT_MFLAGS, Constants::TGT_MAKEFLAGS, true) );
  special_targets.put( Constants::DOT_NOTPARALLEL,
    new Keyword(Constants::DOT_NOTPARALLEL, Constants::TGT_NOTPARALLEL, true) );
  special_targets.put( Constants::DOT_NULL,
    new Keyword(Constants::DOT_NULL, Constants::TGT_NULL, true) );
  special_targets.put( Constants::DOT_ORDER,
    new Keyword(Constants::DOT_ORDER, Constants::TGT_ORDER, true) );
  pathKeyword = Keyword(Constants::DOT_PATH, Constants::TGT_PATH, true );
  special_targets.put( Constants::DOT_PATH, &pathKeyword );
  special_targets.put( Constants::DOT_PRECIOUS,
    new Keyword(Constants::DOT_PRECIOUS, Constants::TGT_PRECIOUS, true) );
  special_targets.put( Constants::DOT_SILENT,
    new Keyword(Constants::DOT_SILENT, Constants::TGT_SILENT, true) );
  special_targets.put( Constants::DOT_SUFFIXES,
    new Keyword(Constants::DOT_SUFFIXES, Constants::TGT_SUFFIXES, true) );

  return (true);
}
