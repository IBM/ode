#
# This is the Makeconf platform specific part for CONTEXT=x86_beos_4
#
OBJECT_FORMAT=ELF

# boolean vars
BEOS=
BEOS_X86=

CCFAMILY ?= gnu

# Macros that are used conditionally in our code:
#
# BEOS/BEOS_X86 - We're compiling on BeOS for x86 (should ONLY be used in the
#                 lib/portable and lib/portable/native code).
# UNIX - This is a Unix-like operating system (should ONLY be used in the
#        lib/portable and lib/portable/native code).
# NO_TRAILING_SLASH_FOR_DIRS - For calls to rmdir/mkdir, the path must NOT
#                              end with a slash (or even "/.").
# DEFAULT_SHELL_IS_SH - Used for shell-specific conditionals...for this shell,
#                       DIRSEP is a slash and PATHSEP is a colon. May
#                       be used to determine other aspects as well.  Although
#                       it effectively means "I'm on Unix", it shouldn't be
#                       used as such...only things that relate to the shell.
# IGNORE_SIGS_BEFORE_FORK - For spawning certain commands (like the shell)
#                           for which you want the parent to ignore SIGINT
#                           during their lifetime, the call to ignore signals
#                           must occur BEFORE spawning the command.
#
CDEFS += -DBEOS -DBEOS_X86 -DUNIX -DNO_TRAILING_SLASH_FOR_DIRS \
         -DDEFAULT_SHELL_IS_SH -DIGNORE_SIGS_BEFORE_FORK

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

.endif # CCFAMILY
