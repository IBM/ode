OBJECTS_SUBDIRS	= lib
EXPLIB_SUBDIRS	= lib
SUBDIRS		= lib bin

.if !defined(WEBMAKE)
SUBDIRS += ${PKG_IS_SUPPORTED:Dcmf}
.endif

# Only need to traverse "doc" directory so the
# appropriate txt files are "built" and installed
#
# We need to copy the license agreement file into all the bin subdirs
# 
.if defined(AIX_PPC) || defined(RPM_PACKAGE) || (${DIST_TYPE} == "base")
SUBDIRS += doc
.endif # defined(AIX_PPC) || DIST_TYPE == "base"

##################################
# Parse Related Variables
##################################
PARSE=
PKG_CMF_FILE=${cmf/ode_cmf.pp:P}
.if defined (AIX_PPC)
CMFPARSE_SUBDIRS = pkgtools/aix
.endif

##################################
# Gather Related Variables
##################################
GATHER=

##################################
# RunPkgTool Related Variables
##################################
RUNPKGTOOL=
.if defined (AIX_PPC)
RUNPKGTOOL_SUBDIRS = pkgtools/aix
.endif

# Define the ZIP command to be used
_ZIP_ ?= jar -cvMf
ZIP_SUFF ?= .zip

.if !empty(MACHINE:Mx86_beos_*)
_ZIP_ = zip -r
.elif !empty(MACHINE:Mx86_openbsd_*) || !empty(MACHINE:Mzseries_linux_*)
_ZIP_        = tar -cvf
ZIP_SUFF     = .tar.gz
_COMPRESS_   = gzip
USE_COMPRESS =
.endif

# Only when we build Java
.if defined(BUILDJAVA)
#Removed the plugins com/ibm/sdwb/ode from below
EXPINC_SUBDIRS  += com/ibm/ode/pkg/pkgMvs com/ibm/ode/pkg/test 
OBJECTS_SUBDIRS += com/ibm/ode com/ibm/sdwb/bps/api/servicepkg 
#EXPLIB_SUBDIRS  += com/ibm/sdwb/ode
JAVADOC_SUBDIRS += com/ibm/ode

##################################
# Java JAR Related Variables
##################################
JAR_LIBRARIES     = ${BUILD_PREFIX}tools${JAR_SUFF}
OTHER_JAR_OBJECTS = com org javax

.endif # BULIDJAVA

# Only do for install_all
.if make(install_all) && !defined(WEBMAKE)

VPATH    = install${PATHSEP}install/${CONTEXT}

.if ${DIST_TYPE:l} != "base"
# Only go into the pkgtools directory when we do an install_all
.if empty(MACHINE:M*_openvms_*)
SUBDIRS += pkgtools 
.endif

# On OpenVMS, we provide a setup script.
#
.if !empty(MACHINE:M*_openvms_*)
ILIST    = odesyms.com
.endif

# Only install the jar file on the platform that created it
.if defined(BUILDJAVA)
ILIST   += ${JAR_LIBRARIES}
VPATH   := ${VPATH}${PATHSEP}../export/classes
${BUILD_PREFIX}tools.jar_IDIR = /../ship/
.endif # BUILDJAVA

.endif # DIST_TYPE != "base"

# Platform specific install information
.if defined(UNIX)
.if !defined(AIX_PPC)

.if defined(MVSOE)
SUBDIRS += rules_mk
.endif # MVSOE

.else # defined(MVSOE)
SUBDIRS += rules_mk

.if ( defined(AIX_PACKAGE) ) 
VPATH  := ${VPATH}${PATHSEP}${SANDBOXBASE}/inst.images/ship
${BUILD_PREFIX}bbexample${ZIP_SUFF}_IDIR = ${IDIR}..${DIRSEP}examples${DIRSEP}
${BUILD_PREFIX}tools.jar_IDIR = ${IDIR}..${DIRSEP}bin${DIRSEP}
${BUILD_PREFIX}rules${ZIP_SUFF}_IDIR = ${IDIR}..${DIRSEP}examples${DIRSEP}
${BUILD_PREFIX}confs${ZIP_SUFF}_IDIR = ${IDIR}..${DIRSEP}examples${DIRSEP}
ILIST += ${BUILD_PREFIX}bbexample${ZIP_SUFF} ${BUILD_PREFIX}tools.jar
ILIST += ${BUILD_PREFIX}rules${ZIP_SUFF} ${BUILD_PREFIX}confs${ZIP_SUFF}
.endif # AIX_PACKAGE 
.endif # AIX_PPC

# Copy the rules tools and examples up into the ship tree.
.if defined(RPM_PACKAGE) 
VPATH  := ${VPATH}${PATHSEP}${SANDBOXBASE}/inst.images/ship
${BUILD_PREFIX}bbexample${ZIP_SUFF}_IDIR = ${IDIR}..${DIRSEP}examples${DIRSEP}
${BUILD_PREFIX}tools.jar_IDIR = ${IDIR}..${DIRSEP}bin${DIRSEP}
${BUILD_PREFIX}rules${ZIP_SUFF}_IDIR = ${IDIR}..${DIRSEP}examples${DIRSEP}
${BUILD_PREFIX}confs${ZIP_SUFF}_IDIR = ${IDIR}..${DIRSEP}examples${DIRSEP}
ILIST += ${BUILD_PREFIX}bbexample${ZIP_SUFF} ${BUILD_PREFIX}tools.jar
ILIST += ${BUILD_PREFIX}rules${ZIP_SUFF} ${BUILD_PREFIX}confs${ZIP_SUFF}
.endif # defined RPM_PACKAGE

.endif # UNIX
.ifndef UNIX && VMS
ILIST += ${ODECLIBNAME}${SHLIB_SUFF}
.if !empty(ODECPPLIBNAME)
ILIST += ${ODECPPLIBNAME}${SHLIB_SUFF}
.endif # !empty(ODECPPLIBNAME)
.endif # ndef UNIX

.endif # make(install_all)

# Variables to trigger the building of one file install images (jar files)
PKG_GEN_DIST_FILE =

.include <${RULES_MK}>

.if defined(AIX_PPC) || defined(MVSOE)

_INSTALL_STANDARD_TARGETS_ += bbexample

bbexample: # .DIRS
# If we are in a sandbox then update the sandbox files
.ifdef IN_SANDBOX
	mklinks -auto -copy /src/bbexample
.else
# Do nothing if we are building the backing build
	@
.endif # IN_SANDBOX

.endif # AIX_PPC

################################################################################
# Rules to create zip files

# The directory to place the zip files
SHIPTOP ?= ${SANDBOXBASE}${DIRSEP}${INSTDIR}${DIRSEP}ship${DIRSEP}

# The tools to dependency checking with.  Not all inclusive list
TOOLS = \
	build \
	genpath \
	mk \
	mkbb \
	mkdep \
	mklinks \
	mkpath \
	mksb

# For the full distribution we'll depend on a little more
.if ${DIST_TYPE:l} != "base"
TOOLS += \
	currentsb \
	gendep \
	resb \
	sbinfo \
	sbls
.endif # DIST_TYPE != "base"

# Library files to install
INSTALL_LIBS = ${LIB_PREF}${ODELIBNAME}${SHLIB_SUFF}

# All the shippables
SHIPABLES = ${TOOLS:@WORD@${WORD}${PROG_SUFF}@}

.ifndef STATIC_BUILD
SHIPABLES += ${INSTALL_LIBS}
.endif

# We default the names of the zip files to be 
#     ${REL_NAME}_${BUILD_NUM}_${CONTEXT}${ZIP_SUFF}
.if !empty(MACHINE:Mx86_nt_*)
MAPZIPNAME ?= x86_win32
.else
MAPZIPNAME ?= ${CONTEXT}
.endif

# ZIP_NAMES will look like 'odei2.5_b16_rios_aix_4${ZIP_SUFF}'
CONTEXT_ZIP   = ${BUILD_PREFIX}${MAPZIPNAME}${ZIP_SUFF}
RULES_ZIP     = ${BUILD_PREFIX}rules${ZIP_SUFF}
CONFS_ZIP     = ${BUILD_PREFIX}confs${ZIP_SUFF}
BBEXAMPLE_ZIP = ${BUILD_PREFIX}bbexample${ZIP_SUFF}
DOCS_ZIP      = ${BUILD_PREFIX}docs${ZIP_SUFF}
ZIP_NAMES     = ${CONTEXT_ZIP}

.if defined(AIX_PPC) || defined(ALLZIPS)

ZIP_NAMES += ${RULES_ZIP} ${BBEXAMPLE_ZIP} ${CONFS_ZIP} ${DOCS_ZIP}

.elif !empty(MACHINE:Mmvs390_oe_*)
# Used for _rules_ebcdic.zip and _bbexample_ebcdic.zip
MVS       ?= _ebcdic

RULES_ZIP      = ${BUILD_PREFIX}rules${MVS}${ZIP_SUFF}
CONFS_ZIP      = ${BUILD_PREFIX}confs${MVS}${ZIP_SUFF}
BBEXAMPLE_ZIP  = ${BUILD_PREFIX}bbexample${MVS}${ZIP_SUFF}
ZIP_NAMES     += ${RULES_ZIP} ${CONFS_ZIP} ${BBEXAMPLE_ZIP}

_INSTALL_STANDARD_TARGETS_ += bbexample

.endif # ALLZIPS 

ZIP_FILES = ${ZIP_NAMES:@WORD@${SHIPTOP}${WORD}@}

zip_all: ${ZIP_FILES} .SPECTARG

${SHIPTOP}${CONTEXT_ZIP}: \
	    ${SHIPABLES:@WORD@${TOSTAGE}/bin/${WORD}@}
	${.TARGET:H:C}
.if !empty(MACHINE:M*_openvms_*)
	${CHDIR} ${${TOSTAGE}/bin:L:@.PATH.@${VMS_PATH_CONV}@} && jar -cvf ../../ship/${.TARGET:T} *.*
.else
	${.TARGET:rm-}
.ifdef USE_COMPRESS
	cd ${TOSTAGE}${DIRSEP}bin && ${_ZIP_} ../../ship/${.TARGET:T:R} *
	${_COMPRESS_} ${TOSTAGE}/../ship/${.TARGET:T:R}
.else
	cd ${TOSTAGE}${DIRSEP}bin && ${_ZIP_} ../../ship/${.TARGET:T} *
.endif # USE_COMPRESS
.endif # openvms

.if !empty(MACHINE:Mrios_aix_*) || !empty(MACHINE:Mmvs390_oe_*) \
 || defined(ALLZIPS) 

${SHIPTOP}${BBEXAMPLE_ZIP}!
	cd ${SANDBOXBASE}${DIRSEP}${ODESRCNAME} && ${_ZIP_} ../${INSTDIR}/ship/${.TARGET:T} bbexample

${SHIPTOP}${RULES_ZIP}!
	cd ${TOSTAGE:H} && ${_ZIP_} ${.TARGET} rules_mk
#	cd ${SHIPTOP} && jar -uvfM ${SANDBOXBASE}/export/plugins/ode/com.ibm.sdwb.ode.core_%PLUGIN_VER%${JAR_SUFF} ${RULES_ZIP}

${SHIPTOP}${CONFS_ZIP}!
	cd ${SANDBOXBASE}${DIRSEP}${ODESRCNAME}${DIRSEP}install${DIRSEP}confs && ${_ZIP_} ${.TARGET} *
#	cd ${SHIPTOP} && jar -uvfM ${SANDBOXBASE}/export/plugins/ode/com.ibm.sdwb.ode.core_%PLUGIN_VER%${JAR_SUFF} ${CONFS_ZIP}

${SHIPTOP}${DOCS_ZIP}!
	cd ${TOSTAGE:H}${DIRSEP}htmls && ${_ZIP_} ${.TARGET} *

.endif # ALLZIPS

# also install tools.jar to location that BPS uses
.if defined(AIX_PPC) && (${USER} == "odebld")
VPATH := ${VPATH}${PATHSEP}../export/classes
instdep_all install_all : /build/tools/java/ode/${REL_NAME}_tools.jar \
                          /ode/tools/java/ode/${REL_NAME}_tools.jar

/build/tools/java/ode/${REL_NAME}_tools.jar \
/ode/tools/java/ode/${REL_NAME}_tools.jar : ${BUILD_PREFIX}tools.jar
	${CP} ${_CPFLAGS_} ${.ALLSRC} ${.TARGET}
	${CHMOD} 644 ${.TARGET}
.endif
