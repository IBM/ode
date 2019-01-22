#
# This is the Makeconf platform specific part for CONTEXT=ia64_hposs_6
#
OBJECT_FORMAT=TNS

#
# Boolean defines
#
HPOSS=
HPOSS_IA64=

CCFAMILY ?= native

# Macros that are used conditionally in our code:
#
# HPOSS - We're compiling on HP/OSS (should ONLY be used in the
#         lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
#
CDEFS += -DHPOSS -DHPOSS_IA64 -DUNIX -D_XOPEN_SOURCE_EXTENDED -D_TANDEM_SOURCE

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if (${CCFAMILY}=="native")

# Compiler-specific macros that are used conditionally in our code:
#
# ENFORCE_EXPLICIT_CTRS - Turn on use of "explicit" on certain constructors
#                         to make sure they can't be used in generating
#                         temporaries.
#
CDEFS += -DENFORCE_EXPLICIT_CTRS -DNO_BINARY_OPENMODE

# Even exe's .cpp files have to be compiled in PIC format...
BUILD_SHARED_OBJECTS = 

# -Wnoinline is only needed to work around a compiler bug.
C_CFLAGS   += -Wnowarn=262,734,770,1506
CPP_CFLAGS += -Wcplusplus -Wversion2 -Wnowarn=262,734,770,1506 -Wnoinline

LDFLAGS    += -Wcplusplus -Wversion2 -L/G/system/zd11004 -lzsptdll
SHLDFLAGS  += -Wcplusplus -Wversion2 -L/G/system/zd11004 -lzsptdll \
              -Weld=-export_all

.endif # CCFAMILY
