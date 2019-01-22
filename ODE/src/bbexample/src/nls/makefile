# The executable programs to create
NLV_PROGRAMS   = hello${PROG_SUFF}

# Objects that NLV_PROGRAMS depends on
OFILES         = hello${OBJ_SUFF}

# For this example, we'll use C++
CCTYPE = cpp

# Files to be run through a codepage conversion program (iconv)
CODEPAGE_TGTS  = README.TXT

# Java classes to generate
JAVA_CLASSES   = Hello${CLASS_SUFF}

# Java property files (resource bundles) to run native2ascii on
PROPERTIES     = Hello_en_US${PROP_SUFF} \
                 Hello_fr_FA${PROP_SUFF} \
                 Hello_ja_JP${PROP_SUFF} \
                 Hello${PROP_SUFF}

# Catalogs that are to be generated using gencat
CATALOGS       = hello${CAT_SUFF}

# For our example, we don't have mkcatdefs on all platforms
.if defined(SOLARIS) || defined(HPUX) || defined(LINUX) || defined(SCO)
NO_MKCATDEFS = 
.endif

.if !defined(NO_MKCATDEFS)

MSG_HDRS_BASE = helloinc

# Use en_US as the default language
.if (${NLV} == "en_US")
# C header files generated from message files.
#   On AIX it is hello_msg.h, on NT it is hello.h.  Hence the MSG_HDRS_TAIL
MSG_HDRS = ${MSG_HDRS_BASE}${MSG_HDRS_TAIL}${HDR_SUFF}

# Header files to export
INCLUDES = ${MSG_HDRS}

# Where to export the header files
EXPINCTOP = ../../
EXPINCDIR = /usr/include/

# Since MSG_HDRS are generated in the obj directory, we need to "copy" the
# header file to the export directory
EXPORT_USING_TAR =

.else
# Only for English we need to generate the header file helloinc.h.  For all 
# other NLVs we just use CATALOGS and mkcatdefs
CATALOGS     += ${MSG_HDRS_BASE}${CAT_SUFF}

# Need to use mkcatdefs to process symbol definitions
USE_MKCATDEFS =

# Suppress generation of header file
MKCATFLAGS    = -h

.endif # NLV == en_US
.endif # !defined(NO_MKCATDEFS)

# For hello.cpp, we need to know whether to include helloinc.h or helloinc_msg.h
CDEFS = ${MSG_HDRS_TAIL:D-DMSG_HDRS_TAIL} ${NO_MKCATDEFS:D-DNO_MSGHDR} 

.if defined(MVSOE)
# For MVS Unix System Services we need to enable XOpen features
CDEFS  += -D_XOPEN_SOURCE
# For MVS Unix System Services we need to allow .cpp as C++ extension
CFLAGS += -+
.endif #defined(MVSOE)

# Since a catalog file is generated as a side-effect of generating the 
# MSG_HDRS, we derive all the catalogs from both CATALOGS and MSG_HDRS
ALL_CATS = ${_CATALOGS_} ${MSG_HDRS:D${MSG_HDRS_BASE}${CAT_SUFF}}

# ILIST must be defined before RULES_MK is included since targets
# are created based off of it
ILIST = ${_NLV_PROGRAMS_} ${ALL_CATS}

.include <${RULES_MK}>

# We'll install the PROGRAMS to the <nlv>/bin directory
IDIR = /${NLV}/bin/

# We'll install the CATALOGS to the <nlv>/cat directory
#   The _IDIR settings need to be after RULES_MK is included since it
# defines the variable _CATALOGS_
${ALL_CATS:@.CAT.@${.CAT.}_IDIR@} = /${NLV}/cat/


.if !defined(NO_MKCATDEFS)
# Since we generate MSG_HDRS and hello.cpp depends on this, we need to explicitly
# list the dependency.
hello${OBJ_SUFF} : ${MSG_HDRS}

.endif # !defined(NO_MKCATDEFS)

################################################################################
# We'll use Hello_en_US.prop for the default, so copy
# Hello_en_US.prop to Hello.prop
# Because we use a variable defined in std.mk (PROPSRC_SUFF)
# in the target name, we must include the rules beforehand.
#
Hello${PROPSRC_SUFF}: Hello_en_US${PROPSRC_SUFF}
	-${RM} ${_RMFLAGS_} ${.TARGET}
	${CP} ${.ALLSRC:S;/;${DIRSEP};g} ${.TARGET}

# An easy way to invoke the sample program
runhello: .SPECTARG
	${_NLV_PROGRAMS_}
