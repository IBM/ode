#
# This is the Makeconf platform specific part for CONTEXT=zseries_linux_2
#
OBJECT_FORMAT=ELF

# boolean vars
LINUX=
LINUX_ZSERIES=

CCFAMILY ?= gnu

# Macros needed for /usr/include headers
CDEFS += -D_POSIX_SOURCE -D_POSIX_C_SOURCE=199309L -D_BSD_SOURCE

# Macros that are used conditionally in our code:
#
# LINUX/LINUX_ZSERIES - We're compiling on Linux for zSeries (should ONLY be
#                    used in the lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
#
CDEFS += -DLINUX -DLINUX_ZSERIES -DUNIX

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if ${CCFAMILY}=="gnu"

# -x c++ : treat .c files as C++ code
CPP_CFLAGS += -x c++

STATIC_BUILD =
.undef USE_SHARED_LIBRARY
_gnu_CFLAGS_ = 

.endif # CCFAMILY
