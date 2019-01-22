#
# To build exa as a dynamic library, define USE_SHARED_LIBRARY.
# Both the rules and this makefile will take special steps when
# this variable is defined.  See src/Makeconf for more information.
#
# To link a dynamic library with the compiler instead of the linker
# on OS/2 and Windows, also define LINK_WITH_COMPILER.  This variable
# is only meaningful in this makefile (not the rules).
#
# NOTE: LIBBASE is set in Makeconf, and can be overridden from the
# environment or command line (to illustrate the ease of changing
# the name of the library). However, with Visual C++, you must also
# edit visual.def and change the library name manually.
#


.ifdef USE_SHARED_LIBRARY

IDIR = ${IDIR_PREF}/${PKG_NAME}/bin/
ILIST = ${SHARED_LIBRARIES}
.endif

.ifdef USE_RESOURCES

OPTIONAL_RESOURCES=mergeres${RES_SUFF}
CFLAGS += -DUSE_RESOURCES

.if !empty(MACHINE:Mx86_os2_*)

.ifdef USE_SHARED_LIBRARY
CFLAGS += -Gd+ -DUSE_SHARED_LIBRARY
.endif
CFLAGS += -DUSE_OS2_RESOURCES
RCFLAGS += -DUSE_OS2_RESOURCES
ICON_FILE=odeos2${ICO_SUFF}

.else  # not OS2

ICON_FILE=odew32${ICO_SUFF}
.if ${CCFAMILY}=="vage"
.if defined(LINK_WITH_COMPILER)
SHLDFLAGS += -B"-DEF:user32${LIB_SUFF}"
.else
SHLDFLAGS += -DEF:user32${LIB_SUFF}
.endif
.endif # VisualAge

.endif # not OS2

RESOURCES = mergeres${RES_SUFF}
RESOURCES_SRCS = mergeres${RC_SUFF} ${ICON_FILE} ..\inc\mergeres.h

.endif # USE_RESOURCES

.ifdef USE_SHARED_LIBRARY
# We're building a shared library, so compile the
# object files with the proper flags.
BUILD_SHARED_OBJECTS =
SHARED_LIBRARIES = ${LIB_PREF}${LIBBASE}${SHLIB_SUFF}
.else
LIBRARIES = ${LIB_PREF}${LIBBASE}${LIB_SUFF}
.endif

OFILES = printmsg${OBJ_SUFF} printnl${OBJ_SUFF} ${OPTIONAL_RESOURCES}

EXPDIR    = /usr/lib/
EXPLIBTOP = ../../
EXPLIBS   = ${LIB_PREF}${LIBBASE}${LIB_SUFF}


#
# IBM Visual Age 3.x dynamic library settings
#
.if ${CCFAMILY} == "vage" && defined(USE_SHARED_LIBRARY)

.if defined(LINK_WITH_COMPILER)

_vage_SHLD_ = icc
_vage_SHLDFLAGS_ = /Ge- /Fe${.TARGET}
_vage_SHLD_OPT_LEVEL_ =
.if !empty(MACHINE:Mx86_os2_*)
EXTRA_LIBS += ${vageos2${DEFFILE_SUFF}:P}
.else
EXTRA_LIBS += ${vage${DEFFILE_SUFF}:P}
# just let the import library depend on the DLL, no commands
NO_IMPLIB_COMMANDS =
.endif

.else # LINK_WITH_COMPILER

.if !empty(MACHINE:Mx86_os2_*)
LIBS += ${LIBBASE}${DEFFILE_SUFF}
.else
LIBS += ${LIBBASE}${EXPFILE_SUFF}
.endif

.endif # LINK_WITH_COMPILER

.endif # vage && USE_SHARED_LIBRARY


#
# MS Visual C++ 4.x dynamic library settings
#
.if ${CCFAMILY} == "visual" && defined(USE_SHARED_LIBRARY)

.if defined(LINK_WITH_COMPILER)

_visual_SHLD_ = cl
_visual_SHLDFLAGS_ = /LD /Fe${.TARGET}
_visual_SHLD_OPT_LEVEL_ =
#
# Because of the irritating way in which linker flags are
# passed from the compiler, they must appear after all of
# the objects.  EXTRA_LIBS is the only way to accomplish
# this for now.  Take care to use dashes in this case,
# since forward slashes are converted to backslashes for
# this variable.
#
EXTRA_LIBS = -link -def:${visual${DEFFILE_SUFF}:P}
.ifdef USE_RESOURCES
EXTRA_LIBS += -DEFAULTLIB:user32${LIB_SUFF}
.endif

# just let the import library depend on the DLL, no commands
NO_IMPLIB_COMMANDS =

.else # LINK_WITH_COMPILER

.if (${CCVERSION} == "5.0" || ${CCVERSION} == "6.0")
IMPLIB_SOURCES = visual${DEFFILE_SUFF} ${_OFILES_}
LIBS += ${LIBBASE}${EXPFILE_SUFF}
.else
SHLDFLAGS = /def:${visual${DEFFILE_SUFF}:P}
.ifdef USE_RESOURCES
SHLDFLAGS += -DEFAULTLIB:user32${LIB_SUFF}
.endif
.endif

.endif # LINK_WITH_COMPILER

.endif # visual && USE_SHARED_LIBRARY

.include <${RULES_MK}>
