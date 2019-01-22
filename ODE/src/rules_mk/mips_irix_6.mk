#
# This is the Makeconf platform specific part for CONTEXT=mips_irix_6
#
OBJECT_FORMAT=ELF

# boolean vars
IRIX =

CCFAMILY ?= gnu

# Macros needed for /usr/include headers
CDEFS += -D_POSIX_SOURCE -D_POSIX_C_SOURCE=199309L

# Macros that are used conditionally in our code:
#
# IRIX - We're compiling on IRIX (should ONLY be
#        used in the lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
#
CDEFS += -DIRIX -DUNIX

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if ${CCFAMILY} == "native"

# -no_auto_include : do not include template definition files automatically
# -no_prelink : do not generate .ii files for instantiation
# -ptnone : do not generate template instantiations automatically
CPP_CFLAGS += -no_auto_include -no_prelink -ptnone

# Compiler-specific macros that are used conditionally in our code:
#
# NO_BINARY_OPENMODE - Native compiler library doesn't implement ios::binary.
#
CDEFS += -DNO_BINARY_OPENMODE

.elif ${CCFAMILY}=="gnu"

# -x c++ : treat .c files as C++ code
CPP_CFLAGS += -x c++

.endif # CCFAMILY
