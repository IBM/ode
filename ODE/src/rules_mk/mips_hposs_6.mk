#
# This is the Makeconf platform specific part for CONTEXT=mips_hposs_6
#
OBJECT_FORMAT=TNS

#
# Boolean defines
#
HPOSS=
HPOSS_MIPS=

CCFAMILY ?= native

# Macros that are used conditionally in our code:
#
# HPOSS - We're compiling on HP/OSS (should ONLY be used in the
#         lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
#
CDEFS += -DHPOSS -DHPOSS_MIPS -DUNIX -D_XOPEN_SOURCE_EXTENDED -D_TANDEM_SOURCE

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

C_CFLAGS   += -Wnowarn=262,734,770,1506
CPP_CFLAGS += -Wcplusplus -Wversion2 -Wnowarn=262,734,770,1506

# get usleep() from the zspt library.
LDFLAGS    += -Wcplusplus -Wversion2 -L/G/system/sys00 -lzsptsrl \
              -Wld=-allow_duplicate_procs -Wld="-unres_symbols error"
SHLDFLAGS  += -Wcplusplus -Wversion2 -L/G/system/sys00 -lzsptsrl \
              -Wld=-export_all -Wld=-allow_duplicate_procs \
              -Wld="-unres_symbols error"

.endif # CCFAMILY
