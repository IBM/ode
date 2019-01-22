#
# This is the Makeconf platform specific part for CONTEXT=x86_netbsd_1
#
OBJECT_FORMAT=A.OUT

# boolean vars
NETBSD=
NETBSD_X86=
.undef NO_RANLIB

CCFAMILY ?= gnu

CDEFS += -DNETBSD -DNETBSD_X86 -DUNIX

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
LDFLAGS += -static
SHLDFLAGS += -static
STATIC_BUILD =
.undef USE_SHARED_LIBRARY
_gnu_CFLAGS_ =

# The compiler has optimizer bugs, so clear OPT_LEVEL.
.ifndef DEBUGGING
OPT_LEVEL = 
.endif

.endif # CCFAMILY
