#
# This is the Makeconf platform specific part for CONTEXT=sparc_solaris_2
#
OBJECT_FORMAT=ELF
INSTALL_TOOL ?= pkgadd
PKG_TOOL ?= mkinstall

# boolean vars
BUILDJAVA=
BUILDJAVADOCS=
SOLARIS=
SOLARIS_SPARC=

CCFAMILY ?= gnu

CDEFS += -DSOLARIS -DSOLARIS_SPARC -DUNIX

GENDEPFLAGS += -I/usr/include -E/usr/include

ECLIPSE_ROOT ?= /ode/tools/java

PLATFORM_CLASSPATH = ${ECLIPSE_ROOT}${DIRSEP}eclipse${DIRSEP}plugins${DIRSEP}org.eclipse.swt.motif_2.0.2${DIRSEP}ws${DIRSEP}motif${DIRSEP}swt.jar

EXTRA_LIBS += -lposix4 -ldl 

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if (${CCFAMILY}=="cset")

GENDEPFLAGS += -I/opt/IBMcset/include -E/opt/IBMcset/include

# -Q : turn on inlining
.ifndef DEBUGGING
CFLAGS += -Q
.endif # DEBUGGING

# -qnotempinc : put template instantiations in .o files
# -+ : treat all files as C++ code
CPP_CFLAGS += -qnotempinc -+

.ifdef USE_SHARED_LIBRARY
LDFLAGS   += -Wl,-zmuldefs
SHLDFLAGS += -Wl,-zmuldefs
.endif

.elif (${CCFAMILY}=="kai")

GENDEPFLAGS += -I/usr/local/KAI/KCC_BASE/include \
               -E/usr/local/KAI/KCC_BASE/include

CDEFS += -DREFERENCE_TYPE_OVERLOADS

# --c++ : treat source files as C++ code
# --c   : treat source files as C   code
CPP_CFLAGS += --c++
C_CFLAGS += --c

.elif (${CCFAMILY}=="gnu")

GENDEPFLAGS += -I/usr/local/include/g++ -E/usr/local/include/g++

# -x c++ : treat source files as C++ code
CPP_CFLAGS += -x c++ 

.elif (${CCFAMILY}=="native")

GENDEPFLAGS += -I/usr/SUNWspro/SC4.2/include/CC -E/usr/SUNWspro/SC4.2/include/CC

# -w : Turn off all warnings (specific warnings cannot be turned off)
CFLAGS += -w

# -instances=explicit: user must write template instantiations
CPP_CFLAGS += -instances=explicit

# uncomment this conditional when CCVERSION is implemented:
#.if (${CCVERSION} > 4)
# use the classic iostream library
OLDSTREAM_CFLAGS = -library=iostream,no%Cstd
CFLAGS    += ${OLDSTREAM_CFLAGS}
LDFLAGS   += ${OLDSTREAM_CFLAGS}
SHLDFLAGS += ${OLDSTREAM_CFLAGS}
#.endif

.endif # CCFAMILY
