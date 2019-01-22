
PROGRAMS = jikes${PROG_SUFF}

IDIR     ?= /bin/
ILIST    = ${PROGRAMS}

INCFLAGS += -I.

OFILES = ast${OBJ_SUFF} body${OBJ_SUFF} bytecode${OBJ_SUFF} \
     case${OBJ_SUFF} code${OBJ_SUFF} control${OBJ_SUFF} decl${OBJ_SUFF} \
     definite${OBJ_SUFF} depend${OBJ_SUFF} diagnose${OBJ_SUFF} \
     double${OBJ_SUFF} dump${OBJ_SUFF} error${OBJ_SUFF} expr${OBJ_SUFF} \
     getclass${OBJ_SUFF} incrmnt${OBJ_SUFF} init${OBJ_SUFF} \
     javaact${OBJ_SUFF} jikes${OBJ_SUFF} jikesapi${OBJ_SUFF} long${OBJ_SUFF} \
     lookup${OBJ_SUFF} lpginput${OBJ_SUFF} modifier${OBJ_SUFF} op${OBJ_SUFF} \
     option${OBJ_SUFF} parser${OBJ_SUFF} platform${OBJ_SUFF} \
     scanner${OBJ_SUFF} segment${OBJ_SUFF} set${OBJ_SUFF} stream${OBJ_SUFF} \
     symbol${OBJ_SUFF} system${OBJ_SUFF} tab${OBJ_SUFF} unparse${OBJ_SUFF} \
     unzip${OBJ_SUFF} zip${OBJ_SUFF} instant${OBJ_SUFF}

# don't use old iostream library
.undef OLDSTREAM_CFLAGS

.if defined(HPUX) && (${CCFAMILY} == "native")
CFLAGS += +W740 +W749 +W829
GENDEPFLAGS += -I/opt/aCC/include -E/opt/aCC/include
.endif

.if !defined(DEBUGGING) && (defined(OPENBSD) || defined(OS2))
OPT_LEVEL = 
.endif

.ifdef AIX || TRU64
EXTRA_LIBS += libiconv
.endif

.ifdef DYNIXPTX
EXTRA_LIBS += libintl
.endif

.ifdef AIX || TRU64 || IRIX || SCO || DYNIXPTX
EXTRA_LIBS += libm
.endif

.include <${RULES_MK}>
