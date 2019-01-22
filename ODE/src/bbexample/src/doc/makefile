# Files that will be copied to the install directory
ILIST			= bps0cntl.htm bpsov.gif 

# Directory to where installed files will be copied
IDIR = ${IDIR_PREF}/${PKG_NAME}/html/

.include <${RULES_MK}>

# CMF Generation variable definitions
.if (${PKGFAMILY:l} == "rpm")
CMF_PARENT = ${CMF_BIN_PARENT}
.else
CMF_PARENT = ${CMF_DOC_PARENT}
.endif

.if (${PKGFAMILY:l} == "mvs")
CMF_TARGET_DIR = ABPSHTML
CMF_FILE_TYPE  = hfs
bps0cntl.htm_CMF_TARGET_FILE = bps0cntl
bpsov.gif_CMF_TARGET_FILE = bpsov
bps0cntl.htm_CMF_PART_INFO_LINK = bps0cntl.htm
bps0cntl.htm_CMF_PART_INFO_TEXT = TEXT
bpsov.gif_CMF_PART_INFO_LINK = bpsov.gif
bpsov.gif_CMF_PART_INFO_BINARY = BINARY
.endif
