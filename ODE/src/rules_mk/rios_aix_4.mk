#
# This is the Makeconf platform specific part for CONTEXT=rios_aix_4
#
OBJECT_FORMAT=XCOFF

INSTALL_TOOL ?= installp
PKG_TOOL ?= mkinstall

# boolean vars
AIX=
AIX_PPC=

PORTABLE_NATIVE_OFILES = aixarch${OBJ_SUFF}
ECLIPSE_ROOT ?= /ode/tools/java

CCFAMILY ?= cset

# Macros that are used conditionally in our code:
#
# AIX/AIX_PPC - We're compiling on AIX/PPC (should ONLY be used in the
#       lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
#
CDEFS += -DAIX -DAIX_PPC -DUNIX

GENDEPFLAGS += -I/usr/include -E/usr/include

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if (${CCFAMILY}=="cset")

GENDEPFLAGS += -I/usr/lpp/xlC/include -E/usr/lpp/xlC/include

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

# -Q : allow inlining
.ifndef DEBUGGING
CFLAGS += -Q
.endif

LDFLAGS += -Wl,-blibpath:/usr/lib/:/lib
SHLDFLAGS += -blibpath:/usr/lib/:/lib

.elif (${CCFAMILY}=="gnu")

CDEFS += -D_LONG_LONG

GENDEPFLAGS += -I/projects/spa/tools/gnu/include/g++ \
               -E/projects/spa/tools/gnu/include/g++

.endif # CCFAMILY
