#
# This is the Makeconf platform specific part for CONTEXT=x86_solaris_2
#
OBJECT_FORMAT=ELF

INSTALL_TOOL ?= pkgadd
PKG_TOOL ?= mkinstall

# boolean vars
SOLARIS=
SOLARIS_X86=

CCFAMILY ?= gnu

CDEFS += -DSOLARIS -DSOLARIS_X86 -DUNIX

GENDEPFLAGS += -I/usr/include -E/usr/include

EXTRA_LIBS += -lposix4

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if (${CCFAMILY}=="native")

GENDEPFLAGS += -I/opt/SUNWspro/SC5.0/include/CC -E/opt/SUNWspro/SC5.0/include/CC

# -w : Turn off all warnings (specific warnings cannot be turned off)
CFLAGS += -w

# -instances=explicit: user must write template instantiations
CPP_CFLAGS += -instances=explicit

# use the classic iostream library
OLDSTREAM_CFLAGS    += -library=iostream,no%Cstd
CFLAGS    += ${OLDSTREAM_CFLAGS}
LDFLAGS   += ${OLDSTREAM_CFLAGS}
SHLDFLAGS += ${OLDSTREAM_CFLAGS}

.elif (${CCFAMILY}=="gnu")

GENDEPFLAGS += -I/usr/local/include/g++ -E/usr/local/include/g++

# -x c++ : treat .c files as C++ code
CPP_CFLAGS += -x c++ 

.endif # CCFAMILY
