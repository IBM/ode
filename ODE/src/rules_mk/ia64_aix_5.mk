#
# This is the Makeconf platform specific part for CONTEXT=ia64_aix_5
#
OBJECT_FORMAT=ELF

# If not using a cross compiler, use native packaging tools.
.if ${MACHINE}==${CONTEXT}
INSTALL_TOOL ?= installp
PKG_TOOL     ?= mkinstall
.else
PATH %= /sde/usr/bin${PATHSEP}${PATH}
LD_ROOT %= /sde
.endif

# boolean vars
AIX=
AIX_IA64=

PORTABLE_NATIVE_OFILES = aixarch${OBJ_SUFF}

CCFAMILY ?= cset

# Macros that are used conditionally in our code:
#
# AIX/AIX_IA64 - We're compiling on AIX/IA64 (should ONLY be used in the
#       lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
# IGNORE_SIGS_BEFORE_FORK - For spawning certain commands (like the shell)
#                           for which you want the parent to ignore SIGINT
#                           during their lifetime, the call to ignore signals
#                           must occur BEFORE spawning the command.
#
CDEFS += -DAIX -DAIX_IA64 -DUNIX -DIGNORE_SIGS_BEFORE_FORK

GENDEPFLAGS += -I/usr/include -E/usr/include

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if (${CCFAMILY}=="cset")

# c89 has harsh -I directory existence restriction
_cset_ansi_CC_ = xlc

# Compiler-specific macros that are used conditionally in our code:
#
# USE_PRAGMA_FOR_TEMPINST - Compiler can't explicitly instantiate templates
#                           with the usual syntax, so must use #pragma.
#
CDEFS += -DUSE_PRAGMA_FOR_TEMPINST

# Uncomment the following line if c89 is used as the ANSI C compiler
#CDEFS += -D_ALL_SOURCE -D_LARGE_FILES

# -qnotempinc : don't create separate template instance files
# -+ : compile all files as C++ code
# -qmaxmem : use up to 4MB memory for optimization work
CPP_CFLAGS += -qnotempinc -+ -qmaxmem=4096

# Put string literals and const values in read-only memory
CFLAGS += -qro -qroconst

# If not using a cross compiler...
.if ${MACHINE}==${CONTEXT}

GENDEPFLAGS += -I/usr/vacpp/include -E/usr/vacpp/include
LDFLAGS     += -Wl,-blibpath:/usr/lib/:/lib
SHLDFLAGS   += -blibpath:/usr/lib/:/lib

# ...otherwise, if we ARE cross-compiling...
.else

GENDEPFLAGS += -I/sde/usr/include -E/sde/usr/include
# Cross compiler has bugs in the optimizer
.ifndef DEBUGGING
OPT_LEVEL =
.endif
STATIC_BUILD =
.undef USE_SHARED_LIBRARY

.endif # Native vs. Cross-compiler settings

.endif # CCFAMILY
