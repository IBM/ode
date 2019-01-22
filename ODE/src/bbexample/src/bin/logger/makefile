# Executable programs to create
PROGRAMS = logger${PROG_SUFF}

# Directory where installed files are copied
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

# Add dependencies so .p and .lst files are installed with instdep_all
.ifdef USE_PRELINKED
${PROGRAMS:S/$/.p/g}: ${PROGRAMS}
${PROGRAMS:S/$/.lst/g}: ${PROGRAMS}
.endif

# CMF Generation variable definitions
.if (${PKGFAMILY:l} == "mvs")
CMF_TARGET_DIR  = ABPSBIN
logger.p_CMF_TARGET_FILE = LOGGER
logger.lst_CMF_TARGET_FILE = LOGGER
logger.lst_CMF_SHIP_TYPE = vpl
.else
# logger symbolic link
.if (${PKGFAMILY:l} != swpackage)
EXTRA_ILIST = ${LOGGER_LINK}
.endif
LOGGER_LINK = ${IDIR_PREF}/${PKG_NAME}/links/logger
${LOGGER_LINK}_CMF_FILE_TYPE = S
${LOGGER_LINK}_CMF_TARGET_DIR = ${IDIR}
${LOGGER_LINK}_CMF_TARGET_FILE = logger
.if (${PKGFAMILY:l} == "rpm")
${LOGGER_LINK}_CMF_PARENT = ${CMF_BIN_PARENT}
.else
${LOGGER_LINK}_CMF_PARENT = ${CMF_LINKS_PARENT}
.endif
.endif
