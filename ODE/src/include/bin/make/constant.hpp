/**
 *  Constants
 *
**/
#ifndef _ODE_BIN_MAKE_CONSTANT_HPP_
#define _ODE_BIN_MAKE_CONSTANT_HPP_

#include "lib/string/string.hpp"

class Constants
{
  public:

    static const char SUFF_SEP; // Separates suffixes

    // Types of GraphNodes
    enum
    {
      OP_UNKNOWN      = 0,
      OP_DEPEND       = 1, /* Execution of commands depends on
                            * kids (:) */
      OP_FORCEDEP     = 2, /* Always execute commands (!) */
      OP_DOUBLEDEP    = 3, /* Multiple targets with same name (::) */
      OP_OPTIONAL     = 4, /* Don't care if the target doesn't
                            * exist and can't be created */
      OP_USE          = 5, /* Use associated commands for parents */
      OP_EXEC         = 6, /* Target is never out of date, but always
                            * execute commands anyway. Its time
                                       * doesn't matter, so it has none...sort
                                          * of */
      OP_IGNORE       = 7, /* Ignore errors when creating the node */
      OP_SILENT       = 8, /* Don't echo commands when executed */
      OP_MAKE         = 9, /* Target is a recurrsive make so its
                            * commands should always be executed when
                                    * it is out of date, regardless of the
                                    * state of the -n or -t flags */
      OP_PMAKE        = 10,/* Same as OP_MAKE without enforcing ordering */
      OP_JOIN         = 11,/* Target is out-of-date only if any of its
                            * children was out-of-date */
      OP_LINK         = 12,/* The node is a symlink, we should use
                            * lstat instead of stat in dir.c */
      OP_SPECTARG     = 13,/* The node is a special target (don't touch) */
      OP_INVISIBLE    = 14,/* The node is invisible to its parents.
                                 * I.e. it doesn't show up in the parents's
                                 * local variables. */
      OP_NOTMAIN      = 15,/* The node is exempt from normal 'main
                            * target' processing in parse.c */
      OP_NOREMOTE     = 16,/* The commands associated with this node are only
                              to run locally */
      OP_PRECIOUS     = 17,/* The target is precious and should not be removed
                               on interrupt */
      OP_PASSES       = 18,/* The node marks a pass node */
      OP_DIRS         = 19,/* The node should be looked as directory only */

      /* Attributes applied by PMake */
      OP_TRANSFORM    = 20,/* The node is a transformation rule */
      OP_MEMBER       = 21,/* Target is a member of an archive */
      OP_LIB          = 22,/* Target is a library */
      OP_ARCHV        = 23,/* Target is an archive construct */
      OP_HAS_COMMANDS = 24,/* Target has all the commands it should.
                            * Used when parsing to catch multiple
                            * commands for a target */
      OP_SAVE_CMDS    = 25,/* Saving commands on .END (Compat) */
      OP_DEPS_FOUND   = 26,/* Already processed by Suff_FindDeps */
      OP_ORDER        = 27,/* Marks the node as beinging ordered */
      OP_SOURCEONLY   = 28,/* This node is only a source */

      OP_PRECMDS      = 29,/* Commands are prepended */
      OP_POSTCMDS     = 30,/* Commands are appended */
      OP_REPLCMDS     = 31,/* Commands are replaced */
      OP_NORMTARG     = 32,/* Target is normal, not a suffix transform */
      OP_FORCEBLD     = 33,/* Force rebuilding of target */
      OP_REPLSRCS     = 34,/* Force rebuilding of target */
      OP_LINKTARGS    = 35 /* Share commands when pattern has mult. targs */
    };

    // Special targets
    enum
    {
      TGT_BEGIN       = 1,
      TGT_DEFAULT     = 2,
      TGT_END         = 4,
      TGT_ERROR       = 8,
      TGT_EXIT        = 16,
      TGT_IGNORE      = 32,
      TGT_INCLUDES    = 64,
      TGT_INTERRUPT   = 128,
      TGT_LIBS        = 256,
      TGT_LINKTARGS   = 512,
      TGT_MAIN        = 1024,
      TGT_MAKEFLAGS   = 2048,
      TGT_NOTPARALLEL = 4096,
      TGT_NULL        = 8192,
      TGT_ORDER       = 16384,
      TGT_PATH        = 32768,
      TGT_PRECIOUS    = 65536,
      TGT_SILENT      = 131072,
      TGT_SUFFIXES    = 262144
    };

    // The name of the 'make' tool, is usually 'mk'.
    static const String MAKE_NAME;

    // String constants
    // Special sources used in make
    static const String DOT_DIRS;
    static const String DOT_EXEC;
    static const String DOT_IGNORE;
    static const String DOT_INVISIBLE;
    static const String DOT_JOIN;
    static const String DOT_LINKS;
    static const String DOT_MAKE;
    static const String DOT_NOREMOTE;
    static const String DOT_NOTMAIN;
    static const String DOT_OPTIONAL;
    static const String DOT_PASSES;
    static const String DOT_PMAKE;
    static const String DOT_PRECIOUS;
    static const String DOT_RECURSIVE;
    static const String DOT_REPLSRCS;
    static const String DOT_SILENT;
    static const String DOT_SPECTARG;
    static const String DOT_USE;
    static const String DOT_PRECMDS;
    static const String DOT_POSTCMDS;
    static const String DOT_REPLCMDS;
    static const String DOT_NORMTARG;
    static const String DOT_FORCEBLD;


   // Special targets used in make
    static const String DOT_BEGIN;
    static const String DOT_DEFAULT;
    static const String DOT_END;
    static const String DOT_ERROR;
    static const String DOT_EXIT;
    static const String DOT_INCLUDES;
    static const String DOT_INTERRUPT;
    static const String DOT_LIBS;
    static const String DOT_LINKTARGS;
    static const String DOT_MAIN;
    static const String DOT_MAKEFLAGS;
    static const String DOT_MFLAGS;
    static const String DOT_NOTPARALLEL;
    static const String DOT_NULL;
    static const String DOT_ORDER;
    static const String DOT_PATH;
    static const String DOT_SUFFIXES;

   //Others
    static const String DOT_TARGET;
    static const String DOT_TARGETS;
    static const String DOT_PREFIX;
    static const String DOT_IMPSRC;
    static const String DOT_ALLSRC;
    static const String DOT_MEMBER;
    static const String DOT_OODATE;
    static const String DOT_ARCHIVE;

    static const String DOT_CURDIR;

    static const String CURDIR;
    static const String POUND;
    static const String MAKEACTION;
    static const String MAKECONF;
    static const String MAKEPASS;
    static const String MAKESLEEP;
    static const String MAKEFILE;
    static const String MAKETOP;
    static const String MAKEDIR;
    static const String MAKESUB;
    static const String MAKEINCLUDECOMPAT;
    static const String MAKESYSPATH;
    static const String MAKE;
    static const String MAKEFLAGS;
    static const String MACHINE;
    static const String ODERELEASE;
    static const String DEFAULT;
    static const String VPATH;

    static const String UNTIL_CHARS;
    static const String EMPTY_TARGET;

    static const String DOT_INCLUDE;
    static const String DOT_TRYINCLUDE;
    static const String DOT_UNDEF;
    static const String DOT_IFNDEF;
    static const String DOT_IFDEF;
    static const String DOT_IFNMAKE;
    static const String DOT_IFMAKE;
    static const String DOT_IF;
    static const String DOT_ELSE;
    static const String DOT_ELIFDEF;
    static const String DOT_ELIFNDEF;
    static const String DOT_ELIFMAKE;
    static const String DOT_ELIFNMAKE;
    static const String DOT_ELIF;
    static const String DOT_ENDIF;

    static const String DOT_RIF;
    static const String DOT_RELSE;
    static const String DOT_RELIF;
    static const String DOT_RENDIF;
    static const String DOT_RFOR;
    static const String DOT_RENDFOR;

  private:

};
#endif //_ODE_BIN_MAKE_CONSTANT_HPP_

