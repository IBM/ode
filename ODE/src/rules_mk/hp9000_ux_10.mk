#
# This is the Makeconf platform specific part for CONTEXT=hp9000_ux_10
#
OBJECT_FORMAT=A.OUT

INSTALL_TOOL ?= swinstall
INSTALL_IMAGE_NAME ?= ode catalog
PKG_TOOL ?= mkinstall
# Since /tmp/odebld/<rel num> has been modified so there is world read
# permission we'll continue to use it
PKG_OUTPUT_DIR = /tmp/odebld/${REL_NAME:S/^ode//}

#
# Boolean defines
#
HPUX=
HPUX_RISC=

CCFAMILY ?= native

#Variable to control the execution of swreg by Spti_pkgmk
RUN_SWREG ?= "no"
RUN_SWREG %= ${RUN_SWREG}

# Macros needed for /usr/include headers
CDEFS += -D_HPUX_SOURCE 

# Macros that are used conditionally in our code:
#
# HPUX - We're compiling on HP-UX (should ONLY be used in the
#        lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
# REMSH_IS_REMOTE_SHELL - The command to run a remote shell is "remsh".
#
CDEFS += -DHPUX -DHPUX_RISC -DUNIX -DREMSH_IS_REMOTE_SHELL

GENDEPFLAGS += -I/usr/include -E/usr/include

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if (${CCFAMILY}=="native")

.ifdef PURIFIED
DEBUGGING =
_native_cpp_LD_ = purify aCC
INCDIRS += -I/usr/pure4.2/purify-4.2-hpux
.endif # PURIFIED

.ifdef QUANTIFIED
_native_cpp_LD_ = quantify aCC
INCDIRS += -I/usr/quantify4.2/quantify-4.2-hpux
.endif # QUANTIFIED

# Compiler-specific macros that are used conditionally in our code:
#
# REFERENCE_TYPE_OVERLOADS - Compiler is picky about overloaded functions
#                            with different constness on pointers and such.
#                            This creates extra overloads with references to
#                            shut the compiler up.
# ENFORCE_EXPLICIT_CTRS - Turn on use of "explicit" on certain constructors
#                         to make sure they can't be used in generating
#                         temporaries.
#
CDEFS += -DREFERENCE_TYPE_OVERLOADS -DENFORCE_EXPLICIT_CTRS

# +inst_directed - instantiations are handled explicitly
# +W6062 - ignore optimizer limitation warning
# +DAportable - produce code that works on 700 and 800 series machines
CPP_CFLAGS += +inst_directed +DAportable

# HP-UX compiler ignores some function template instantiations with
# +inst_directed, so compile instantiation files with +inst_compiletime.
instant.o_CFLAGS ?= ${CFLAGS:N+inst_*} +inst_compiletime

.ifdef USE_SHARED_LIBRARY
LDFLAGS += -Wl,+s -Wl,+b/usr/lib:/usr/ccs/lib
SHLDFLAGS += -Wl,-E
.endif

GENDEPFLAGS += -I/opt/aCC/include/iostream -E/opt/aCC/include/iostream

.endif # CCFAMILY
