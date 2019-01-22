.if make(install_all)
SUBDIRS = pubs
.endif

INSTALL_USING_CRLFCON = yes

.if defined(AIX) || defined(RPM_PACKAGE)

########################################
# AIX && RPM_PACKAGE specific installation
#  - build and install <release name>_<build name>_readme.txt files
#  from readme.txt files.  These include: readme.txt, fixes.txt, known_bugs.txt
#

README_TXT   = ${BUILD_PREFIX}readme.txt
FIXES_TXT    = ${BUILD_PREFIX}fixes.txt
BUGS_TXT     = ${BUILD_PREFIX}known_bugs.txt
LICENSE1_TXT = ${BUILD_PREFIX}ODELicense_ILA.txt
LICENSE2_TXT = ${BUILD_PREFIX}ODELicense_PUT.txt
LICENSE3_TXT = ${BUILD_PREFIX}ODELicense_NOTICES.txt
LICENSE4_TXT = ${BUILD_PREFIX}ODELicense_13Langs.zip

ILIST += \
	txts/fixes.txt \
	txts/known_bugs.txt \
	txts/readme.txt \
	txts/ODELicense_ILA.txt \
	txts/ODELicense_PUT.txt \
	txts/ODELicense_NOTICES.txt \
	txts/ODELicense_13Langs.zip

.if !defined(AIX_PACKAGE) && !defined(RPM_PACKAGE)
ILIST += \
	${README_TXT} \
	${FIXES_TXT} \
	${BUGS_TXT} \
	${LICENSE1_TXT} \
	${LICENSE2_TXT} \
	${LICENSE3_TXT} \
	${LICENSE4_TXT}

CRLFFLAGS = -format d

IDIR = /../doc/

.else # defined(AIX_PACKAGE) || defined(RPM_PACKAGE)
# For the AIX installp package we put the docs where the package creation tools can find 
# them
txts/readme.txt_IDIR \
txts/fixes.txt_IDIR \
txts/ODELicense_ILA.txt_IDIR \
txts/ODELicense_PUT.txt_IDIR \
txts/ODELicense_NOTICES.txt_IDIR \
txts/ODELicense_13Langs.zip_IDIR \
txts/known_bugs.txt_IDIR = ${IDIR}..${DIRSEP}doc${DIRSEP}

.endif # !defined(AIX_PACKAGE) && !defined(RPM_PACKAGE)

# Put the TXT files with release and build info in the file names
# to the "ship" location
${README_TXT}_IDIR  ${FIXES_TXT}_IDIR ${BUGS_TXT}_IDIR \
${LICENSE1_TXT}_IDIR ${LICENSE2_TXT}_IDIR \
${LICENSE3_TXT}_IDIR ${LICENSE4_TXT}_IDIR = /../ship/

README_TXT_SRC = txts/${.TARGET:S|${BUILD_PREFIX}||}

${README_TXT}: $${README_TXT_SRC}
	-${RM} ${RMFLAGS} ${.TARGET:S|/|${DIRSEP}|g}
	${CP} ${.ALLSRC:S|/|${DIRSEP}|g} ${.TARGET:S|/|${DIRSEP}|g}

FIXES_TXT_SRC = txts/${.TARGET:S|${BUILD_PREFIX}||}

${FIXES_TXT}: $${FIXES_TXT_SRC}
	-${RM} ${RMFLAGS} ${.TARGET:S|/|${DIRSEP}|g}
	${CP} ${.ALLSRC:S|/|${DIRSEP}|g} ${.TARGET:S|/|${DIRSEP}|g}

BUGS_TXT_SRC = txts/${.TARGET:S|${BUILD_PREFIX}||}

${BUGS_TXT}: $${BUGS_TXT_SRC}
	-${RM} ${RMFLAGS} ${.TARGET:S|/|${DIRSEP}|g}
	${CP} ${.ALLSRC:S|/|${DIRSEP}|g} ${.TARGET:S|/|${DIRSEP}|g}

LICENSE1_TXT_SRC = txts/${.TARGET:S|${BUILD_PREFIX}||}

${LICENSE1_TXT}: $${LICENSE1_TXT_SRC}
	-${RM} ${RMFLAGS} ${.TARGET:S|/|${DIRSEP}|g}
	${CP} ${.ALLSRC:S|/|${DIRSEP}|g} ${.TARGET:S|/|${DIRSEP}|g}

LICENSE2_TXT_SRC = txts/${.TARGET:S|${BUILD_PREFIX}||}

${LICENSE2_TXT}: $${LICENSE2_TXT_SRC}
	-${RM} ${RMFLAGS} ${.TARGET:S|/|${DIRSEP}|g}
	${CP} ${.ALLSRC:S|/|${DIRSEP}|g} ${.TARGET:S|/|${DIRSEP}|g}

LICENSE3_TXT_SRC = txts/${.TARGET:S|${BUILD_PREFIX}||}

${LICENSE3_TXT}: $${LICENSE3_TXT_SRC}
	-${RM} ${RMFLAGS} ${.TARGET:S|/|${DIRSEP}|g}
	${CP} ${.ALLSRC:S|/|${DIRSEP}|g} ${.TARGET:S|/|${DIRSEP}|g}

LICENSE4_TXT_SRC = txts/${.TARGET:S|${BUILD_PREFIX}||}

${LICENSE4_TXT}: $${LICENSE4_TXT_SRC}
	-${RM} ${RMFLAGS} ${.TARGET:S|/|${DIRSEP}|g}
	${CP} ${.ALLSRC:S|/|${DIRSEP}|g} ${.TARGET:S|/|${DIRSEP}|g}

.endif # defined(AIX)

.include <${RULES_MK}>

.if defined(AIX)
_BUILD_STANDARD_TARGETS_ += ${README_TXT} ${FIXES_TXT} ${BUGS_TXT} \
                            ${LICENSE1_TXT} ${LICENSE2_TXT} \
                            ${LICENSE3_TXT} ${LICENSE4_TXT}
.endif # defined(AIX)
