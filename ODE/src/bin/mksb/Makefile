PROGRAMS = mksb${PROG_SUFF}

OFILES       = mksb${OBJ_SUFF} mksbc${OBJ_SUFF}
OTHER_OFILES = ../mklinks/mklinksc${OBJ_SUFF}

.if empty(MAKEFILE_PASS)
SUBDIRS  += ../mklinks
.endif

.if !empty(MAKEFLAGS:M*-a*)
EXTRA_OFILES += ${OTHER_OFILES}
.else
OFILES       += ${OTHER_OFILES}
.endif

LIBS     = ${LIB_PREF}${ODELIBNAME}${LIB_SUFF}

IDIR     ?= /bin/
ILIST    = ${PROGRAMS}

.include <${RULES_MK}>
