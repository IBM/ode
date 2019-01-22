#
# This is the Makeconf platform specific part for CONTEXT=x86_openbsd_2
#
OBJECT_FORMAT=A.OUT

# boolean vars
OPENBSD=
OPENBSD_X86=
.undef NO_RANLIB

CCFAMILY ?= gnu

CDEFS += -DOPENBSD -DOPENBSD_X86 -DUNIX

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

# because of exception limitations, we have to link statically
# can't even build PIC code for the library either
STATIC_BUILD =
.undef USE_SHARED_LIBRARY
_gnu_CFLAGS_ =

.endif # CCFAMILY
