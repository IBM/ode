#
# This is the Makeconf platform specific part for CONTEXT=x86_nt_4
#
OBJECT_FORMAT=OMF

#
# Turn on (define) options
#
.undef UNIX
WIN32=
WINNT=

ECLIPSE_ROOT ?= C:
PLATFORM_CLASSPATH = ${ECLIPSE_ROOT}${DIRSEP}eclipse${DIRSEP}plugins${DIRSEP}org.eclipse.swt.win32_2.0.2${DIRSEP}ws${DIRSEP}win32${DIRSEP}swt.jar

.ifndef WEBMAKE
# Use DIRSEP at the end since a hardcoded backslash would be interpreted
# as a continuation character.
TOOLSBASE ?= \
    o:\build\${TOOLSBASE_REL}\latest\inst.images\${MACHINE}\bin${DIRSEP}
.endif # WEBMAKE

PORTABLE_NATIVE_OFILES = winarch${OBJ_SUFF}

CCFAMILY ?= visual

# Macros that are used conditionally in our code:
#
# WIN32/WINNT - We're compiling on Windows (should ONLY be used in the
#               lib/portable and lib/portable/native code).
# SLOW_SIGNALS - Parent process may get SIGCHLD before SIGINT.
# CASE_INSENSITIVE_OS - Operating system (file system) isn't case sensitive.
# FILENAME_BLANKS - Allow spaces in filenames.
# BOOLEAN_AS_MACRO - Define "boolean" as a #define, not a typedef.
# NO_SYMLINKS - Operating system doesn't support symbolic links.
# TRAILING_PERIOD_FOR_DIRS - For calls to rmdir/mkdir, simplify them by
#                            always appending a period after the last slash.
# DEFAULT_SHELL_IS_CMD - Used for shell-specific conditionals...for this shell,
#                        DIRSEP is a backslash, PATHSEP is a semicolon, and
#                        drive letters at the front of paths are used.  May
#                        be used to determine other aspects as well.  Although
#                        it effectively means "I'm not on Unix", it shouldn't
#                        used as such...only things that relate to the shell.
# IGNORE_SIGS_BEFORE_FORK - For spawning certain commands (like the shell)
#                           for which you want the parent to ignore SIGINT
#                           during their lifetime, the call to ignore signals
#                           must occur BEFORE spawning the command.
# NO_PIPES_FOR_OUTPUT - Cannot use pipes when spawning child processes to
#                       gather output.  Must output to a file manually.
#
CDEFS += -DWIN32 -DWINNT -DSLOW_SIGNALS -DCASE_INSENSITIVE_OS \
         -DFILENAME_BLANKS -DBOOLEAN_AS_MACRO -DNO_SYMLINKS  \
         -DTRAILING_PERIOD_FOR_DIRS -DDEFAULT_SHELL_IS_CMD \
         -DIGNORE_SIGS_BEFORE_FORK -DNO_PIPES_FOR_OUTPUT

GENDEPFLAGS += -I${ODECPPBASE}/include -E${ODECPPBASE}/include

# Packaging variables to build ODE ISPE package and executable
PKG_ISPE_IS_DIR        = o:\build\${REL_NAME:S/ode//:S/i//}\InstallShield
PKG_ISPE_PKGFORWEB_DIR = ${PKG_ISPE_IS_DIR}\PackageForTheWeb\Projects
PKG_CONTROL_DIR         = ${PKG_ISPE_IS_DIR}\${REL_NAME}
PKG_ISPE_MEDIA         = ${REL_NAME}
PKG_ISPE_PFW_FILE      = ${PKG_ISPE_PKGFORWEB_DIR}\${REL_NAME}.pfw

INSTALL_IMAGE_NAME      ?= ${REL_NAME}_win32.exe
INSTALL_IMAGE_FILE      ?= ${SANDBOXBASE}\${INSTDIR}\ship\${REL_NAME}_${BUILD_NUM}_win32.exe

#########################################
#########################################
#
# COMPILER-SPECIFIC SECTIONS BEGIN HERE
#
#########################################
#########################################

.if (${CCFAMILY}=="vage")

ODECPPBASE  ?= ${CPPMAIN}
ODECLIBNAME ?= odeclibw

# one of the following gets added to CFLAGS in Makeconf
C_CFLAGS   += /Tdc
CPP_CFLAGS += /Tdp /Ft-

GENDEPFLAGS += -I${ODECPPBASE}/sdk/winh -E${ODECPPBASE}/sdk/winh

# Compiler-specific macros that are used conditionally in our code:
#
# SIGFUNC_SIGFUNC - Type for signal function pointers is "_SigFunc".
# USE_PRAGMA_FOR_TEMPINST - Compiler can't explicitly instantiate templates
#                           with the usual syntax, so must use #pragma.
#
CDEFS += -DSIGFUNC_SIGFUNC -DUSE_PRAGMA_FOR_TEMPINST

# ignore nonzero rc due to duplicate symbol warnings
_vage_IMPLIB_ = -ilib

# Make sure we never use /Ge- (this allows us to use the
# library objects in both static and shared libraries
# without recompiling).
_vage_CFLAGS_ =

ODEDLLPORT = _Import

.ifndef DEBUGGING
CFLAGS    += /Oi
.endif # DEBUGGING

CFLAGS += /Gd /Gn /Gm-
LDFLAGS += ${CPPWRTM.RBJ:P}
EXTRA_LIBS += ${ODECLIBNAME}${IMPLIB_SUFF} \
              ${ODECPPBASE}/lib/cppws35o${STATLIB_SUFF} \
              ${ODECPPBASE}/sdk/lib/kernel32${IMPLIB_SUFF}

.ifdef WEBMAKE
EXTRA_LIBS += ${ODECPPBASE}/sdk/lib/advapi32${IMPLIB_SUFF}   \
              ${ODECPPBASE}/sdk/lib/user32${IMPLIB_SUFF}   \
              ${ODECPPBASE}/sdk/lib/winmm${IMPLIB_SUFF}   \
              ${ODECPPBASE}/sdk/lib/gdi32${IMPLIB_SUFF}
.endif # WEBMAKE

.ifndef USE_SHARED_LIBRARY
EXTRA_LIBS += ${ODECPPBASE}/sdk/lib/user32${IMPLIB_SUFF} \
              ${ODECPPBASE}/sdk/lib/advapi32${IMPLIB_SUFF}
.endif


LDFLAGS   += /NOE /NOD
SHLDFLAGS += /NOE /NOD

.elif (${CCFAMILY}=="visual")

.ifdef WEBMAKE
ODECPPBASE    ?= C:/MSDEV
.else
ODECPPBASE    ?= ${MSVCDIR}
ODECLIBNAME   ?= odeclw
ODECPPLIBNAME ?= odecplw
.endif

CCVERSION     ?= 6.0

PORTABLE_NATIVE_OFILES += gregex${OBJ_SUFF}

# Compiler-specific macros that are used conditionally in our code:
#
# ENFORCE_EXPLICIT_CTRS - Turn on use of "explicit" on certain constructors
#                         to make sure they can't be used in generating
#                         temporaries.
# NOCREATE_READ_OPENMODE - When opening a file in readonly mode, use the
#                          ios::nocreate flag to prevent the file from
#                          being created if it doesn't exist.
#
CDEFS += -DENFORCE_EXPLICIT_CTRS -DNOCREATE_READ_OPENMODE

.ifndef WEBMAKE
CFLAGS    += /MD /GX
LDFLAGS   += /NODEFAULTLIB:MSVCRT,MSVCIRT
SHLDFLAGS += /NODEFAULTLIB:MSVCRT,MSVCIRT

EXTRA_LIBS += ${ODECLIBNAME}${IMPLIB_SUFF} ${ODECPPLIBNAME}${IMPLIB_SUFF}

ODEDLLPORT = _declspec(dllimport)
.endif # WEBMAKE

GENDEPFLAGS += -I${ODECPPBASE}/mfc/include -E${ODECPPBASE}/mfc/include

.endif # CCFAMILY
