#
# This is the Makeconf platform specific part for CONTEXT=alpha_openvms_7
#
OBJECT_FORMAT=VMS

#
# Turn on (define) options
#
.undef UNIX
VMS=
OPENVMS=
VMS_ALPHA=
OPENVMS_ALPHA=

JAR = jar
JAVAC = javac
JAVADOC = javadoc
USE_JAVA_RESPFILE =

NO_TOOLSBASE = 

PORTABLE_NATIVE_OFILES = noarch${OBJ_SUFF} cutime${OBJ_SUFF} gregex${OBJ_SUFF}

CCFAMILY ?= native

# Macros that are used conditionally in our code:
#
# VMS/OPENVMS/VMS_ALPHA/OPENVMS_ALPHA -
#         We're compiling on OpenVMS for Alpha (should ONLY be used in the
#         lib/portable and lib/portable/native code).
# UNIX - behave like Unix for the most part
# CASE_INSENSITIVE_OS - Operating system (file system) isn't case sensitive.
# NO_SYMLINKS - Operating system doesn't support symbolic links.
# DEFAULT_SHELL_IS_VMS - Used for shell-specific conditionals.
# NO_PIPES_FOR_OUTPUT - Cannot use pipes when spawning child processes to
#                       gather output.  Must output to a file manually.
# ENCLOSED_DIRS - Directory specifications are enclosed; separated from the
#                 rest of the path by square brackets.
#
CDEFS += /DEF=(VMS,OPENVMS,VMS_ALPHA,OPENVMS_ALPHA,UNIX, \
         CASE_INSENSITIVE_OS,NO_SYMLINKS,DEFAULT_SHELL_IS_VMS, \
         NO_BINARY_OPENMODE,NO_PIPES_FOR_OUTPUT,ENCLOSED_DIRS, \
         ARGV0_IS_UNUSABLE_PATH,ODEDLLPORT=${ODEDLLPORT})

GENDEPFLAGS += -quiet

STATIC_BUILD = 
.undef USE_SHARED_LIBRARY
SHORT_OBJ_PATHS =

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if (${CCFAMILY}=="native")

# one of the following gets added to CFLAGS in Makeconf
C_CFLAGS   += 
CPP_CFLAGS += /TEMPLATE=(NOAUTO,IMPLICIT)

.endif # CCFAMILY
