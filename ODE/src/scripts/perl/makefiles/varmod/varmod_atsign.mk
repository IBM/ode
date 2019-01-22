#############################################
# varmod_atsign.mk - makefile to test the
# @tmpvar@newval@ variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=file1 file2
WORD=foo.c
NEWVAR=${WORD}new
NAMES = "a b c.obj" "x y z.obj"
"a b c.obj"_VALUE = abc
"x y z.obj"_VALUE = xyz
VALUE = ${${.NAME.}_VALUE}

all: test1 test2 test3

test1:
.if ((!${VAR1:@WORD@${WORD}.o@} == "file1.o file2.o") || (${WORD} != "foo.c"))
	@echo ODEMKERROR: @ variable modifier - expected: file1.o file2.o
	@echo ODEMKERROR: @ variable modifier - actual: ${VAR1:@WORD@${WORD}.o@}
.else
	@echo ODEMKPASS
.endif

test2:
.if ((!${VAR1:@WORD@${NEWVAR}@} == "file1new file2new") || (${WORD} != "foo.c"))
	@echo ODEMKERROR: @ variable modifier - expected: file1new file2new
	@echo ODEMKERROR: @ variable modifier - actual: ${VAR1:@WORD@${NEWVAR}@}
.else
	@echo ODEMKPASS
.endif

test3:
.if ${NAMES:@.NAME.@${VALUE}@} == "abc xyz"
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: @ variable modifier - expected: abc xyz
	@echo ODEMKERROR: @ variable modifier - actual: ${NAMES:@.NAME.@${VALUE}@}
.endif
