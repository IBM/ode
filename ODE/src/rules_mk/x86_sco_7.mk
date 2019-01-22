#
# This is the Makeconf platform specific part for CONTEXT=x86_sco_7
#
OBJECT_FORMAT=ELF

INSTALL_TOOL ?= pkgadd
PKG_TOOL ?= mkinstall

# boolean vars
SCO=
SCO_X86=

CCFAMILY ?= native

GENDEPFLAGS += -I/usr/include -E/usr/include

# Macros needed by /usr/include headers
CDEFS += -D_XOPEN_SOURCE -D_XOPEN_SOURCE_EXTENDED

# Macros that are used conditionally in our code:
#
# SCO/SCO_X86 - We're compiling on SCO/Intel (should ONLY be used in the
#       lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
#
CDEFS += -DSCO -DSCO_X86 -DUNIX

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if (${CCFAMILY}=="native")

GENDEPFLAGS += -I/usr/include/CC -E/usr/include/CC

# -Tno_auto,no_implicit,none : only generate explicit instantiations
CPP_CFLAGS += -Tno_auto,no_implicit,none

# Make sure we can run on SCO OpenServer
LDFLAGS   += -Kudk
SHLDFLAGS += -Kudk

# Compiler-specific macros that are used conditionally in our code:
#
# NO_BINARY_OPENMODE - Native compiler library doesn't implement ios::binary.
#
CDEFS += -DNO_BINARY_OPENMODE

.elif ${CCFAMILY}=="gnu"

# -x c++ : treat .c files as C++ code
CPP_CFLAGS += -x c++

.endif # CCFAMILY
