#
# This is the Makeconf platform specific part for CONTEXT=x86_ptx_4
#
OBJECT_FORMAT=ELF

# boolean vars
DYNIXPTX=
DYNIXPTX_X86=

TOOLSBASE ?= ${HOME}/build/${TOOLSBASE_REL}/latest/inst.images/${MACHINE}/bin/

CCFAMILY ?= native

CDEFS += -DDYNIXPTX -DDYNIXPTX_X86 -DUNIX -DREFERENCE_TYPE_OVERLOADS \
         -DNO_BINARY_OPENMODE

GENDEPFLAGS += -I/usr/include -E/usr/include

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if (${CCFAMILY}=="native")

# use the classic iostream library
OLDSTREAM_CFLAGS = -preansilibs

# -tpautooff : do not auto-generate template instantiations
CPP_CFLAGS += -tpautooff ${OLDSTREAM_CFLAGS}
LDFLAGS    += -tpautooff ${OLDSTREAM_CFLAGS}
SHLDFLAGS  += -tpautooff ${OLDSTREAM_CFLAGS}

GENDEPFLAGS += -I/opt/ptxC++/include/ansi -E/opt/ptxC++/include/ansi

# Compiler optimizer bug...can't build with optimization on!
.ifndef DEBUGGING
OPT_LEVEL =
.endif

.endif # CCFAMILY
