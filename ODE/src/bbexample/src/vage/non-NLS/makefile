# Example from VisualAgeC++ 3.5-win converted to ODE

PROGRAMS    = hello5${PROG_SUFF}
OFILES      = ahellow5${OBJ_SUFF} adialog5${OBJ_SUFF} aearthw5${OBJ_SUFF} ahellow5${RES_SUFF}
.if defined(USE_IPF)
IPFHELPS    = ahellow5${HLP_SUFF}
INFS    = ahellow5${INF_SUFF}
CFLAGS += -DUSE_IPF
.else
HELPS       = ahellow5${HLP_SUFF}
.endif
INCFLAGS   += -I.
CFLAGS     += /Gm+

# This example requires VisualAgeC++
CCFAMILY = vage
CCTYPE = cpp

.include <${RULES_MK}>

runhello:
	${PROGRAMS}





