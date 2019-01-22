#
# This is the Makeconf platform specific part for CONTEXT=as400_os400_4
#
OBJECT_FORMAT=ELF

OS400=

# default directory/library for the library objects
OUTPUTDIR %= ODELIB${REL_NAME:S/ode//:S/i//:S/.//}

# use del so both the symlink and the underlying file are deleted
# NOTE: del only fails when the symlink exists and the underlying file doesn't
RM ?= del

CCFAMILY ?= native

TOOLSBASE ?= /home/odebld/bin/

PORTABLE_NATIVE_OFILES = noarch${OBJ_SUFF}

# Macros that are used conditionally in our code:
#
# OS400 - We're compiling on OS/400 (should ONLY be used in the
#         lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
# EBCDIC_CHARSET - This platform uses the EBCDIC character set by default.
# FORCE_TEXT_OPENMODE - Files are opened in binary mode by default, so we
#                       must explicitly request that a file be opened in
#                       text mode.
# ODE_USE_GLOBAL_ENVPTR - The third parameter to main() is empty on this
#                         platform, so use the global "environ" variable
#                         to read the environment variables instead.
# DEFAULT_SHELL_IS_QSH - Used for shell-specific conditionals...for this shell,
#                        DIRSEP is a slash and PATHSEP is a colon. May
#                        be used to determine other aspects as well.
# IGNORE_SIGS_BEFORE_FORK - For spawning certain commands (like the shell)
#                           for which you want the parent to ignore SIGINT
#                           during their lifetime, the call to ignore signals
#                           must occur BEFORE spawning the command.
# USE_SIGACTION_FOR_SIGS - Use sigaction() instead of signal() to set signal
#                          handling.
#
CDEFS += -DOS400 -DUNIX -DEBCDIC_CHARSET -DFORCE_TEXT_OPENMODE \
         -DODE_USE_GLOBAL_ENVPTR -DDEFAULT_SHELL_IS_QSH \
         -DIGNORE_SIGS_BEFORE_FORK -DUSE_SIGACTION_FOR_SIGS \
         -DARGV0_IS_UNUSABLE_PATH

GENDEPFLAGS += -I/usr/include -E/usr/include

# some includes are not in /usr/include (they're stored in MVS datasets),
# so tell gendep to keep quiet about not finding them.
GENDEPFLAGS += -quiet

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if ${CCFAMILY}=="native"

# Compiler-specific macros that are used conditionally in our code:
#
# USE_PRAGMA_FOR_TEMPINST - Compiler can't explicitly instantiate templates
#                           with the usual syntax, so must use #pragma.
#
CDEFS += -DUSE_PRAGMA_FOR_TEMPINST

#
# -qTGTRLS=V4RxM0 : allow code to work on earlier releases
# -+ : the C compiler can't use long paths, so must use C++ for everything
#
CFLAGS    += -qTGTRLS=V4R4M0 -+
LDFLAGS   += -qTGTRLS=V4R4M0
SHLDFLAGS += -qTGTRLS=V4R4M0

STATIC_BUILD =
.undef USE_SHARED_LIBRARY

.endif # CCFAMILY
