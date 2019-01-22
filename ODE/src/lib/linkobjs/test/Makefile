#
# These programs are only usable on NT and OS/2
#
.ifndef UNIX

PROGRAMS = tshlib${PROG_SUFF} tstatlib${PROG_SUFF}

LIBS     = ${LIB_PREF}${ODELIBNAME}${LIB_SUFF}

.ifdef OS2
VAGELIBNAME = cppos30
EXTRALIBS = ${ODECPPBASE}/lib/os2386${IMPLIB_SUFF}
.else
VAGELIBNAME = cppws35
EXTRALIBS = ${ODECPPBASE}/sdk/lib/kernel32${IMPLIB_SUFF}
.endif

.include <${RULES_MK}>

tshlib${PROG_SUFF}_EXTRA_LIBS = \
    ${ODECLIBNAME}${IMPLIB_SUFF} \
    ${ODECPPBASE}/lib/${VAGELIBNAME}o${STATLIB_SUFF} \
    ${EXTRALIBS}

tstatlib${PROG_SUFF}_EXTRA_LIBS = \
    ${ODECPPBASE}/lib/${VAGELIBNAME}${STATLIB_SUFF} \
    ${EXTRALIBS}

.endif # UNIX
