#############################################
# varmod_eq.mk - makefile to test the
# = (System V substitution) variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=abc aba bac

all: test1 test2

test1:
.if (${VAR1:c=X} != "abX aba baX")
	@echo ODEMKERROR: = variable modifier - expected: abX aba baX 
	@echo ODEMKERROR: = variable modifier - actual: ${VAR1:c=X}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${VAR1:=.o} != "abc.o aba.o bac.o")
	@echo ODEMKERROR: = variable modifier - expected: abc.o aba.o bac.o 
	@echo ODEMKERROR: = variable modifier - actual: ${VAR1:=.o}
.else
	@echo ODEMKPASS
.endif
