# Example from VisualAgeC++ 3.5-win converted to ODE

NLV_PROGRAMS  = hello5${PROG_SUFF}
NLV_RESOURCES = ahellow5${RES_SUFF}
OFILES        = ahellow5${OBJ_SUFF} adialog5${OBJ_SUFF} aearthw5${OBJ_SUFF} \
	${_NLV_RESOURCES_}
NLV_HELPS     = ahellow5${HLP_SUFF}

# We have header files locally that we need to put in the compiler'ss search 
# path.
INCFLAGS   += -I.

# We need the IBM Open Class Library
CFLAGS     += /Gm+

RCINPUTFILES = ${MAKETOP}${NLV}${MAKEDIR}/ahellow5${ICO_SUFF}

# This example requires VisualAgeC++
CCFAMILY = vage

.include <${RULES_MK}>

runhello:
	cd ${_NLV_PROGRAMS_:H:S!/!${DIRSEP}!g} ${CMDSEP} ${NLV_PROGRAMS}





