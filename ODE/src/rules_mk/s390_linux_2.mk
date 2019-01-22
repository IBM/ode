#
# This is the Makeconf platform specific part for CONTEXT=s390_linux_2
#
OBJECT_FORMAT=ELF

# boolean vars
LINUX=
LINUX_S390=

CCFAMILY ?= gnu

# Macros needed for /usr/include headers
CDEFS += -D_POSIX_SOURCE -D_POSIX_C_SOURCE=199309L -D_BSD_SOURCE

# Macros that are used conditionally in our code:
#
# LINUX/LINUX_S390 - We're compiling on Linux for S/390 (should ONLY be
#                    used in the lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
#
CDEFS += -DLINUX -DLINUX_S390 -DUNIX

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

.endif # CCFAMILY
