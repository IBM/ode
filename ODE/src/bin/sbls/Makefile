PROGRAMS = sbls${PROG_SUFF}

OFILES   = sbls${OBJ_SUFF} sblsc${OBJ_SUFF} instant${OBJ_SUFF}

LIBS     = ${LIB_PREF}${ODELIBNAME}${LIB_SUFF}

.if ${DIST_TYPE:l} != "base"
IDIR    ?= /bin/
ILIST    = ${PROGRAMS}
.endif # DIST_TYPE != "base"

.include <${RULES_MK}>
