PROGRAMS = currentsb${PROG_SUFF}

OFILES   = currsb${OBJ_SUFF} currsbc${OBJ_SUFF}

LIBS     = ${LIB_PREF}${ODELIBNAME}${LIB_SUFF}

.if ${DIST_TYPE:l} != "base"
IDIR    ?= /bin/
ILIST    = ${PROGRAMS}
.endif # DIST_TYPE != "base"

.include <${RULES_MK}>
