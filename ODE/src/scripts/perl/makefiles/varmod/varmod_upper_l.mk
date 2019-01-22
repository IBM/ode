#############################################
# varmod_upper_l.mk - makefile to test the
# :L variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=something
VAR2=something more

all: test1 test2 test3

test1:
.if (!${VAR1:L} == "VAR1")
	@echo ODEMKERROR: :L variable modifier - expected: VAR1
	@echo ODEMKERROR: :L variable modifier - actual: ${VAR1:L}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR2:L} == "VAR2")
	@echo ODEMKERROR: :L variable modifier - expected: VAR2
	@echo ODEMKERROR: :L variable modifier - actual: ${VAR2:L}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR3:L} == "VAR3")
	@echo ODEMKERROR: :L variable modifier - expected: VAR3
	@echo ODEMKERROR: :L variable modifier - actual: ${VAR3:L}
.else
	@echo ODEMKPASS
.endif
