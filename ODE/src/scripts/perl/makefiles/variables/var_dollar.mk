####################################################
# var_dollar.mk - makefile to test $ in variable names
#
# run as:
#   mk -f var_dollar.mk
#   mk -f var_dollar.mk -DODEMAKE_DOLLARS
#
# Used by the perl script MkVarImpVarTest.pm
#
####################################################

VPATH=$a$bc

all: $bub$ba$.class

$bub$ba$.class: $${.PREFIX}.java
	${${.TARGET}:L:A&STDOUT}
	${a$b:L:A&STDOUT}
       
$bub$ba$.java: $x$
	${${.TARGET}:L:A&STDOUT}
	${${VPATH}:L:A&STDOUT}
	${${.ALLSRC}:L:A&STDOUT}

ubaclass.java:
	${${.TARGET}:L:A&STDOUT}
	${${VPATH}:L:A&STDOUT}

.ifdef ODEMAKE_DOLLARS
$x$:
	${${.TARGET}:L:A&STDOUT}
.endif

