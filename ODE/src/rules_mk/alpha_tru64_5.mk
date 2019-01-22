#
# This is the Makeconf platform specific part for CONTEXT=alpha_tru64_5
#
OBJECT_FORMAT=COFF

# boolean vars
TRU64 =

CCFAMILY ?= native

# Macros that are used conditionally in our code:
#
# TRU64 - We're compiling on Tru64 (should ONLY be
#        used in the lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
#
CDEFS += -DTRU64 -DUNIX

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
CPP_CFLAGS += -nopt

# Compiler-specific macros that are used conditionally in our code:
#
# NO_BINARY_OPENMODE - Native compiler library doesn't implement ios::binary.
#
CDEFS += -DNO_BINARY_OPENMODE

.elif ${CCFAMILY}=="gnu"

# -x c++ : treat .c files as C++ code
CPP_CFLAGS += -x c++

.endif # CCFAMILY
