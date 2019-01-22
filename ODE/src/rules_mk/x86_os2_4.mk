#
# This is the Makeconf platform specific part for CONTEXT=x86_os2_4
#
OBJECT_FORMAT=OMF

# boolean vars
.undef UNIX
OS2=
USE_RESPFILE=

# Use DIRSEP at the end since a hardcoded backslash would be interpreted
# as a continuation character.
TOOLSBASE ?= \
    o:\build\${TOOLSBASE_REL}\latest\inst.images\${MACHINE}\bin${DIRSEP}

PORTABLE_NATIVE_OFILES = os2arch${OBJ_SUFF}

CCFAMILY ?= vage

# Macros that are used conditionally in our code:
#
# OS2 - We're compiling on OS/2 (should ONLY be used in the
#       lib/portable and lib/portable/native code).
# CASE_INSENSITIVE_OS - Operating system (file system) isn't case sensitive.
# NO_SYMLINKS - Operating system doesn't support symbolic links.
# LAZY_DIR_CREATION - Sometimes concurrent mkpath calls will fail because
#                     the file system takes a long time to realize a new
#                     directory exists...this will cause ODE to wait a bit.
# TRAILING_PERIOD_FOR_DIRS - For calls to rmdir/mkdir, simplify them by
#                            always appending a period after the last slash.
# DEFAULT_SHELL_IS_CMD - Used for shell-specific conditionals...for this shell,
#                        DIRSEP is a backslash, PATHSEP is a semicolon, and
#                        drive letters at the front of paths are used.  May
#                        be used to determine other aspects as well.  Although
#                        it effectively means "I'm not on Unix", it shouldn't
#                        used as such...only things that relate to the shell.
# IGNORE_SIGS_BEFORE_FORK - For spawning certain commands (like the shell)
#                           for which you want the parent to ignore SIGINT
#                           during their lifetime, the call to ignore signals
#                           must occur BEFORE spawning the command.
# NO_PIPES_FOR_OUTPUT - Cannot use pipes when spawning child processes to
#                       gather output.  Must output to a file manually.
#
CDEFS += -DOS2 -DCASE_INSENSITIVE_OS -DNO_SYMLINKS -DLAZY_DIR_CREATION \
         -DTRAILING_PERIOD_FOR_DIRS -DDEFAULT_SHELL_IS_CMD \
         -DIGNORE_SIGS_BEFORE_FORK -DNO_PIPES_FOR_OUTPUT

GENDEPFLAGS += -I${ODECPPBASE}/include -E${ODECPPBASE}/include

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if (${CCFAMILY} == "vage")

ODECPPBASE  ?= ${CXXMAIN}
ODETKBASE   ?= ${TKMAIN}
ODECLIBNAME ?= odeclibo
CCVERSION   ?= 3.6

# one of the following gets added to CFLAGS in Makeconf
C_CFLAGS   += /Tdc
CPP_CFLAGS += /Tdp /Ft-

GENDEPFLAGS += -I${ODECPPBASE}/include/os2 -E${ODECPPBASE}/include/os2

# Compiler-specific macros that are used conditionally in our code:
#
# SIGFUNC_SIGFUNC - Type for signal function pointers is "_SigFunc".
# USE_PRAGMA_FOR_TEMPINST - Compiler can't explicitly instantiate templates
#                           with the usual syntax, so must use #pragma.
#
CDEFS += -DSIGFUNC_SIGFUNC -DUSE_PRAGMA_FOR_TEMPINST

# ignore nonzero rc due to duplicate symbol warnings
_vage_SHLD_ = -ilink

# Make sure we never use /Ge- (this allows us to use the
# library objects in both static and shared libraries
# without recompiling).
_vage_CFLAGS_ =

.ifndef DEBUGGING
CFLAGS    += /Oi
.endif # DEBUGGING

CFLAGS  += /Gd /Gn /Gm-
LDFLAGS += ${MESSAGE.DEF:P}

EXTRA_LIBS += ${ODECLIBNAME}${IMPLIB_SUFF} \
              ${ODECPPBASE}/lib/cpprso36${STATLIB_SUFF} \
              ${ODETKBASE}/lib/os2386${IMPLIB_SUFF}

STACK_SIZE ?= 65536
LDFLAGS    += /NOE /NOD /PMTYPE:VIO /STACK:${STACK_SIZE}
SHLDFLAGS  += /NOE /NOD /PMTYPE:VIO /STACK:${STACK_SIZE}

# MSGBIND has a problem when the C/C++ runtime library is
# linked dynamically.  When this is fixed, uncomment the
# USE_MSGBIND line.
#USE_MSGBIND = 
BIND_FILES = ${ODECPPBASE}\\help\\dde4.msg 
BIND_MESSAGES = *

.endif # CCFAMILY
