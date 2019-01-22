#
# This is the Makeconf platform specific part for CONTEXT=mvs390_oe_2
#
OBJECT_FORMAT=A.OUT

MVSOE=

CCFAMILY ?= native

TOOLSBASE ?= /ode/build/${TOOLSBASE_REL}/latest/inst.images/${MACHINE}/bin/

PORTABLE_NATIVE_OFILES = mvsarch${OBJ_SUFF}

# Macros needed for /usr/include headers
CDEFS += -D_XOPEN_SOURCE_EXTENDED -DNEEDSIEEE754 -DNEEDSLONGLONG -D_OPEN_SYS

# Macros that are used conditionally in our code:
#
# MVS/MVSOE - We're compiling on MVS (should ONLY be used in the
#             lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
# SIGFUNC_VOIDFCN - Type for signal function pointers is "__void_fcn".
# EBCDIC_CHARSET - This platform uses the EBCDIC character set, not ASCII.
# ODE_USE_GLOBAL_ENVPTR - The third parameter to main() is empty on this
#                         platform, so use the global "environ" variable
#                         to read the environment variables instead.
# DEFAULT_SHELL_IS_SH - Used for shell-specific conditionals...for this shell,
#                       DIRSEP is a slash and PATHSEP is a colon. May
#                       be used to determine other aspects as well.
#
CDEFS += -DMVS -DMVSOE -DUNIX -DSIGFUNC_VOIDFCN -DEBCDIC_CHARSET \
         -DODE_USE_GLOBAL_ENVPTR -DDEFAULT_SHELL_IS_SH

GENDEPFLAGS += -I/usr/include -E/usr/include

# some includes are not in /usr/include (they're stored in MVS datasets),
# so tell gendep to keep quiet about not finding them.
GENDEPFLAGS += -quiet

.ifdef DEBUGGING
# dbx debugger cannot debug dynamic libraries, so must build statically
.undef USE_SHARED_LIBRARY
.endif # DEBUGGING

# Packaging needed variables
PKG_MVS_EXEC_DATASET        = ode.bps.exec
PKG_MVS_RTG_LOADLIB         = r349120.b390.load
PKG_MVS_FUNCCNTL_DATASET    = ode.function.cntl
PKG_MVS_TERSE_LOAD_DATASET  = ode.tersemvs.loadlib
PKG_MVS_TERSE_CLIST_DATASET = r349120.tersemvs.clist
# PKG_MVS_JOBMONITOR_TIME and PKG_MVS_CRC_LOADLIB are optional
PKG_MVS_JOBMONITOR_TIME     = 1
PKG_MVS_CRC_LOADLIB         = ode.crcmvs.load
PKG_MVS_VSAM_VOLUME_INFO    = RCL004
PKG_MVS_USERID=ode
PKG_MVS_PASSWORD=od2od2od
PKG_MVS_JOBCARD=/u/ode/jobcard
PKG_MVS_DELETE_OUTPUT=YES
PKG_MVS_DISPLAY_OUTPUT=YES
PKG_MVS_SAVE_OUTPUT_FILE=NO

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if ${CCFAMILY}=="native"

CPP_CFLAGS += -Wc,NOTEMPINC -+

# Compiler-specific macros that are used conditionally in our code:
#
# USE_PRAGMA_FOR_TEMPINST - Compiler can't explicitly instantiate templates
#                           with the usual syntax, so must use #pragma.
#
CDEFS += -DUSE_PRAGMA_FOR_TEMPINST

# The compiler has optimizer bugs, so clear OPT_LEVEL.
.ifndef DEBUGGING
OPT_LEVEL =
.endif

.endif # CCFAMILY
