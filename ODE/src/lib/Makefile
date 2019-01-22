#
# ODELIBNAME is defined in Makeconf
#
LIBBASE    = ${ODELIBNAME}
LIBNAME    = ${LIB_PREF}${LIBBASE}${LIB_SUFF}
IMPLIBNAME = ${LIB_PREF}${LIBBASE}${IMPLIB_SUFF}
SHLIBNAME  = ${LIB_PREF}${LIBBASE}${SHLIB_SUFF}

IDIR	       ?= /bin/
.ifdef USE_SHARED_LIBRARY
ILIST	       = ${SHLIBNAME}
.endif
EXPDIR	       = /usr/lib/
EXPLIBTOP      = ../../
EXPLIB_TARGETS = export_${LIBNAME}

.ifdef USE_SHARED_LIBRARY
SHARED_LIBRARIES = ${SHLIBNAME}
.else
LIBRARIES = ${LIBNAME}
.endif

.if defined(MVSOE) && defined(SHARED_LIBRARIES)
ILIST += ${IMPLIBNAME}
.endif # MVSOE

OBJECTS_SUBDIRS = io intcmds portable util string exceptn
DEPEND_STANDARD_SUBDIRS = ${OBJECTS_SUBDIRS}

OFILES = io/ui${OBJ_SUFF} io/cmdline${OBJ_SUFF} io/path${OBJ_SUFF} \
	 io/version${OBJ_SUFF} io/cfgf${OBJ_SUFF} io/bldlstcf${OBJ_SUFF} \
	 io/sbrccf${OBJ_SUFF} io/sandbox${OBJ_SUFF} \
	 io/setvarcf${OBJ_SUFF} io/filec${OBJ_SUFF} \
	 io/sbcnfrdr${OBJ_SUFF} intcmds/genpath${OBJ_SUFF} \
	 intcmds/mkdep${OBJ_SUFF} intcmds/depmkfil${OBJ_SUFF} \
	 intcmds/body${OBJ_SUFF} intcmds/misc${OBJ_SUFF} \
	 portable/ilist${OBJ_SUFF} portable/instant${OBJ_SUFF} \
	 portable/runcmd${OBJ_SUFF} portable/hashable${OBJ_SUFF} \
	 portable/env${OBJ_SUFF} portable/platcon${OBJ_SUFF} \
	 ${PORTABLE_NATIVE_OFILES:S!^!portable/native/!g} \
	 portable/native/proc${OBJ_SUFF} portable/native/strings${OBJ_SUFF} \
	 portable/native/dirent${OBJ_SUFF} portable/native/file${OBJ_SUFF} \
	 portable/native/sleep${OBJ_SUFF} portable/native/dir${OBJ_SUFF} \
	 portable/native/platver${OBJ_SUFF} \
	 portable/native/regex${OBJ_SUFF} \
	 string/string${OBJ_SUFF} string/strarray${OBJ_SUFF} \
	 string/pattern${OBJ_SUFF} string/variable${OBJ_SUFF} \
	 string/strcon${OBJ_SUFF} string/sboxcon${OBJ_SUFF} \
	 util/filecach${OBJ_SUFF} util/signal${OBJ_SUFF} \
	 util/archcach${OBJ_SUFF} util/bitset${OBJ_SUFF} \
	 util/cacharch${OBJ_SUFF} util/cachamem${OBJ_SUFF} \
	 util/cond${OBJ_SUFF} util/condeval${OBJ_SUFF} \
	 exceptn/exceptn${OBJ_SUFF}

.if !defined(UNIX) && !defined(VMS) && defined(USE_SHARED_LIBRARY) && \
    ${CCFAMILY} != "visual"
OFILES += portable/dll${OBJ_SUFF}
.endif

.if ${CCFAMILY} == "vage"
.if !empty(MACHINE:Mx86_os2_*)
LIBS += ${LIBBASE}${DEFFILE_SUFF}
.else
LIBS += ${LIBBASE}${EXPFILE_SUFF}
.endif
.endif

.if defined(USE_SHARED_LIBRARY) && ${CCFAMILY} == "visual"
LIBS += ${LIBBASE}${EXPFILE_SUFF}
${LIBBASE}.def:  $${_OFILES_}
	lib /NOLOGO /OUT:${LIBNAME}  ${.ALLSRC}
	dumpbin /all /rawdata:none ${LIBNAME} >${.TARGET}.TMP
	perl ${gendef.pl:P} ${.TARGET}.TMP  ${.TARGET}
.endif

.include <${RULES_MK}>

# prevent this directory from compiling stuff from subdirectories
.ifnmake install_all
.cpp${OBJ_SUFF} .c${OBJ_SUFF}: ;@
.endif

# don't need a depend.mk for this directory
_DEPEND_STANDARD_TARGETS_=

.if defined(MVSOE) && defined(SHARED_LIBRARIES)
${SHARED_LIBRARIES} : .POSTCMDS
	export _CXX_STEPS=2 && \
  ${_SHLD_} ${_SHLDFLAGS_} -o ${.TARGET} \
	    ${_ALL_SHLIB_OFILES_} ${_LINKING_LIBS_}
.endif
