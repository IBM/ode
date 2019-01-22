# Program names to build
#
PROGRAMS = \
	client${PROG_SUFF} \
	admingui${PROG_SUFF} \
	developergui${PROG_SUFF}

# Object files common to all entries in PROGRAMS
#
COMMON_OFILES = \
	printmsg${OBJ_SUFF}

# Install directory for the programs
#
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

# Make sure the current directory is included in the header file
# search path so printmsg.h is found.
INCFLAGS += -I.

.include <${RULES_MK}>

# Define program specific OFILES here since PROG_SUFF isn't
# defined until rules.mk is processed
#
client${PROG_SUFF}_OFILES	= \
	${COMMON_OFILES} \
	client${OBJ_SUFF}
admingui${PROG_SUFF}_OFILES 	= \
	${COMMON_OFILES} \
	admingui${OBJ_SUFF}
developergui${PROG_SUFF}_OFILES	= \
	${COMMON_OFILES} \
	developergui${OBJ_SUFF}

# Add dependencies so .p and .lst files are installed with instdep_all
.ifdef USE_PRELINKED
${PROGRAMS:S/$/.p/g}: ${PROGRAMS}
${PROGRAMS:S/$/.lst/g}: ${PROGRAMS}
.endif

# CMF Generation variable definitions
.if (${PKGFAMILY:l} == "mvs")
CMF_TARGET_DIR  = ABPSBIN
client.p_CMF_TARGET_FILE = CLIENT
developergui.p_CMF_TARGET_FILE = DEVGUI
admingui.p_CMF_TARGET_FILE = ADMINGUI
client.lst_CMF_TARGET_FILE = CLIENT
developergui.lst_CMF_TARGET_FILE = DEVGUI
admingui.lst_CMF_TARGET_FILE = ADMINGUI
client.lst_CMF_SHIP_TYPE = vpl
developergui.lst_CMF_SHIP_TYPE = vpl
admingui.lst_CMF_SHIP_TYPE = vpl
.endif

