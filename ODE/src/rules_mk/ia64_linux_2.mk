#
# This is the Makeconf platform specific part for CONTEXT=ia64_linux_2
#
OBJECT_FORMAT=ELF

# boolean vars
LINUX=
LINUX_IA64=

CCFAMILY ?= gnu

# Macros needed for /usr/include headers
CDEFS += -D_POSIX_SOURCE -D_POSIX_C_SOURCE=199309L -D_BSD_SOURCE

# Macros that are used conditionally in our code:
#
# LINUX/LINUX_IA64 - We're compiling on Linux for IA64 (should ONLY be
#                   used in the lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
#
# USE_OPENMODE_TYPE - Second argument to the fstream constructors needs to
#                     be of type ios::openmode instead of int.
#
CDEFS += -DLINUX -DLINUX_IA64 -DUNIX
CDEFS += -DUSE_OPENMODE_TYPE

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if ${CCFAMILY}=="gnu"

# -x c++ : treat .c files as C++ code
CPP_CFLAGS += -x c++ -Wno-deprecated

.endif # CCFAMILY
