# Executable programs to create
PROGRAMS = \
	server${PROG_SUFF} \
	retserver${PROG_SUFF} \
	buildserver${PROG_SUFF}

# Directory where files are installed
IDIR = ${IDIR_PREF}/${PKG_NAME}/bin/
# Special situation for MVS/USS, may need to install .p files
# or normal programs.  Also for MVS/USS, need to install the
# vpl files (.lst files)
.if !empty(MACHINE:Mmvs390_oe_*)

# If you want to ship prelinked object modules, then define
#  USE_PRELINKED
.ifdef USE_PRELINKED
# MVS ships the prelinked object modules
ILIST = ${PROGRAMS:@.PROG.@${.PROG.}.p@}
.else
ILIST = ${PROGRAMS}
.endif # defined(USE_PRELINKED)

# Add the .lst vpl files
ILIST += ${PROGRAMS:@.PROG.@${.PROG.}.lst@}

.else
ILIST = ${PROGRAMS}
.endif # !empty(MACHINE:Mmvs390_oe_*)

.include <${RULES_MK}>

# the library that the server program will be linked with.
# note this must appear *after* the rules are included, since
# PROG_SUFF is normally defined in std.mk.
# LIBBASE is defined in Makeconf, and can be overridden
# on the command line or environment.
#
server${PROG_SUFF}_LIBS = ${LIB_PREF}${LIBBASE}${LIB_SUFF}

# See src\Makeconf about using resources.
.ifdef USE_RESOURCES_FILE
server${PROG_SUFF}_OFILES += server${OBJ_SUFF} ../../lib/mergeres${RES_SUFF}
.if empty(MACHINE:Mx86_os2_*)
server${PROG_SUFF}_EXTRA_LIBS += ${LIB_PREF}user32${LIB_SUFF}
.endif # not OS2
.endif # USE_RESOURCES_FILE

# Add dependencies so .p and .lst files are installed with instdep_all
.ifdef USE_PRELINKED
${PROGRAMS:S/$/.p/g}: ${PROGRAMS}
${PROGRAMS:S/$/.lst/g}: ${PROGRAMS}
.endif

# CMF Generation variable definitions
.if (${PKGFAMILY:l} == "mvs")
CMF_TARGET_DIR  = ABPSBIN
server.p_CMF_TARGET_FILE = SERVER
retserver.p_CMF_TARGET_FILE = RETSRVR
buildserver.p_CMF_TARGET_FILE = BLDSRVR
server.lst_CMF_TARGET_FILE = SERVER
retserver.lst_CMF_TARGET_FILE = RETSRVR
buildserver.lst_CMF_TARGET_FILE = BLDSRVR
server.lst_CMF_SHIP_TYPE = vpl
retserver.lst_CMF_SHIP_TYPE = vpl
buildserver.lst_CMF_SHIP_TYPE = vpl
.else
# server symbolic link
SERVER_LINK = ${IDIR_PREF}/${PKG_NAME}/links/server
.if (${PKGFAMILY:l} != swpackage)
EXTRA_ILIST = ${SERVER_LINK}
.endif
${SERVER_LINK}_CMF_FILE_TYPE = S
${SERVER_LINK}_CMF_TARGET_DIR = ${IDIR}
${SERVER_LINK}_CMF_TARGET_FILE = server
.if (${PKGFAMILY:l} == "rpm")
${SERVER_LINK}_CMF_PARENT = ${CMF_BIN_PARENT}
.else
${SERVER_LINK}_CMF_PARENT = ${CMF_LINKS_PARENT}
.endif
.endif