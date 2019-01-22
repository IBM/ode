PROGRAMS = mkbb${PROG_SUFF}

OFILES       = mkbb${OBJ_SUFF} mkbbc${OBJ_SUFF}
OTHER_OFILES = ../mklinks/mklinksc${OBJ_SUFF} ../mksb/mksbc${OBJ_SUFF}

.if empty(MAKEFILE_PASS)
SUBDIRS  += ../mklinks ../mksb
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

