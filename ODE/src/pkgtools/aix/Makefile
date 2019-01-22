SUBDIRS = bin lib

# Flags to initiate packaging
PARSE= 
PKG_CMF_FILE=${../../cmf/pkgtools_cmf.pp:P}
PKG_CONTROL_DIR=${PACKAGEBASE}/mdata/pkgtools

RUNPKGTOOL=
PKG_OUTPUT_DIR=${PACKAGEBASE}/images/pkgtools
PKG_GEN_DIST_FILE =
INSTALL_IMAGE_NAME = pkgtools
INSTALL_IMAGE_FILE = ${SANDBOXBASE}/${INSTDIR}/ship/aix_${INSTALL_IMAGE_NAME}_${INSTALL_TOOL}

ILIST = README_Aix_Pkg_Tools
IDIR = ${IDIR_PREF}/opt/pkgtools/rte/

.include <${RULES_MK}>
