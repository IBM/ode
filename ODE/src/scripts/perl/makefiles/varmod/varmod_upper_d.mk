#############################################
# varmod_upper_d.mk - makefile to test the
# :D variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=oldvalue					
VAR2=""
VAR3=

all: test1 test2 test3 test4

test1:
.if (!${VAR1:Dnewvalue} == "newvalue")
	@echo ODEMKERROR: :D variable modifier - expected: newvalue
	@echo ODEMKERROR: :D variable modifier - actual: ${VAR1:Dnewvalue}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR2:Dnewvalue} == "newvalue")
	@echo ODEMKERROR: :D variable modifier - expected: newvalue
	@echo ODEMKERROR: :D variable modifier - actual: ${VAR2:Dnewvalue}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR3:Dnewvalue} == "newvalue")
	@echo ODEMKERROR: :D variable modifier - expected: ""
	@echo ODEMKERROR: :D variable modifier - actual: ${VAR3:Dnewvalue}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR4:Dnewvalue} == "")
	@echo ODEMKERROR: :D variable modifier - expected: ""
	@echo ODEMKERROR: :D variable modifier - actual: ${VAR4:Dnewvalue}
.else
	@echo ODEMKPASS
.endif

