PROGRAMS = sbmerge${PROG_SUFF}

OFILES   = sbmerge${OBJ_SUFF} sbmergec${OBJ_SUFF}

LIBS     = ${LIB_PREF}${ODELIBNAME}${LIB_SUFF}

.if ${DIST_TYPE:l} != "base"
IDIR    ?= /bin/
ILIST    = ${PROGRAMS}
.endif # DIST_TYPE != "base"

.include <${RULES_MK}>