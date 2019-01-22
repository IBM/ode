#
# This is the Makeconf platform specific part for CONTEXT=x86_interix_35
# Created by: Sven Lange-Last
#
# Windows, and thus Interix, support a derivate of COFF, called PE.
OBJECT_FORMAT=COFF

# Currently, no packaging support is available under the base Interix system.
INSTALL_TOOL ?=

# boolean vars
INTERIX=
INTERIX_X86=

EXTRA_LIBS = -ldl

CCFAMILY ?= gnu

# No Eclipse available
ECLIPSE_ROOT ?=

# Macros that are used conditionally in our code:
#
# _ALL_SOURCE -       Use all available library code not only the POSIX set.
# UNIX -              This is a Unix-like operating system (should ONLY
#                     be used in the lib/portable and lib/portable/native code).
# USE_OPENMODE_TYPE - Force C++ ios constants to be used for the mode
#                     when opening a file.
# INTERIX -           The build result is targeted for Interix.
# INTERIX_X86 -       The build result is targeted for the x86 version of Interix.
#
CDEFS += -D_ALL_SOURCE -DUNIX -DUSE_OPENMODE_TYPE -DINTERIX -DINTERIX_X86


#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if ${CCFAMILY}=="gnu"

# -x c++ : treat .c files as C++ code
CPP_CFLAGS += -Wno-deprecated -x c++

# Compile the code with debugging information.
# Neither -fpic nor -fPIC is defined because
# position independent code causes problems on Interix
_gnu_CFLAGS_ = -g

.endif # CCFAMILY
