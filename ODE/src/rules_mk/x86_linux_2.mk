#
# This is the Makeconf platform specific part for CONTEXT=x86_linux_2
#
OBJECT_FORMAT=ELF

INSTALL_TOOL ?= rpm
PKG_TOOL ?= rpm 
PKG_ARCH ?= i386
INSTALL_IMAGE_NAME = ode-bin-${MAJOR_VERSION:S/\'//g}.${MINOR_VERSION:S/\'//g}.${MINOR_MINOR_VERSION:S/\'//g}-${BUILD_NUMBER:S/\'//g}.${PKG_ARCH}.rpm

# boolean vars
LINUX=
LINUX_X86=

EXTRA_LIBS = -ldl

CCFAMILY ?= gnu

ECLIPSE_ROOT ?= /usr/local/wswb-2.01

# Macros needed for /usr/include headers
CDEFS += -D_POSIX_SOURCE -D_POSIX_C_SOURCE=199309L -D_BSD_SOURCE

# Macros that are used conditionally in our code:
#
# LINUX/LINUX_X86 - We're compiling on Linux for x86 (should ONLY be
#                   used in the lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
#
CDEFS += -DLINUX -DLINUX_X86 -DUNIX

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
