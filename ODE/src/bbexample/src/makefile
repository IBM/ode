######################################################
#
# Subdirectories to process
#
# Note the ability to skip directories (for example,
# COM and COM/ibm are not processed).  This is a
# simple way to reduce the amount of recursive calls
# to Make (at a slight maintainability expense).
#
######################################################

# Subdirectories to traverse during the EXPINC pass
EXPINC_SUBDIRS          = inc

# Subdirectories to traverse during the OBJECTS pass
OBJECTS_SUBDIRS	        = bin/client COM/ibm/makemake

# Subdirectories to traverse during the EXPLIB pass
EXPLIB_SUBDIRS          = lib

# Subdirectories to traverse during the STANDARD pass
SUBDIRS                 = lib inc bin/client bin/server bin/logger doc

.if empty(MACHINE:M*_openvms_*)
SUBDIRS += cmf
.endif

SUBDIRS += copyright COM/ibm/makemake scripts

##################################
# CMF Generation Related Variables
##################################
GENCMF=
PKG_CMF_PRODUCT_FILE=${cmf/bbex_proj_cmf.pp:P}

##################################
# Parse Related Variables
##################################
PARSE=
.if make(gencmf_all)
PKG_CMF_FILE=${SANDBOXBASE}/src/${MAKEOBJDIR}/cmf/bbex_cmf.pp
.else
PKG_CMF_FILE=${cmf/bbex_cmf.pp:P}
.endif
##################################
# Gather Related Variables
##################################
GATHER=

##################################
# RunPkgTool Related Variables
##################################
RUNPKGTOOL=

##################################
# Java JAR Related Variables
##################################
JAR_LIBRARIES = MakeMake${JAR_SUFF}
OTHER_JAR_OBJECTS = COM

.include <${RULES_MK}>
