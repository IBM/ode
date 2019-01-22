PROGRAMS = gendep${PROG_SUFF}

OFILES   = gendep${OBJ_SUFF} gendepc${OBJ_SUFF} instant${OBJ_SUFF}

LIBS     = ${LIB_PREF}${ODELIBNAME}${LIB_SUFF}

IDIR     ?= /bin/

# We only ship gendep.exe with the "full" distribution of ODE, otherwise
# we ship simple scripts to print out a message.
#
.if ${DIST_TYPE:l} != "base"
ILIST    = ${PROGRAMS}
.else
.ifdef UNIX
ILIST    = scripts/gendep
.else
ILIST    = scripts/gendep.cmd
.endif # UNIX
.endif # DIST_TYPE != "base"

.include <${RULES_MK}>
