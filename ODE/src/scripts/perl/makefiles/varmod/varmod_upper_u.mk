#############################################
# varmod_upper_u.mk - makefile to test the
# :U variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=oldvalue					
VAR2=""
VAR3=

all: test1 test2 test3 test4

test1:
.if (!${VAR1:Unewvalue} == "oldvalue")
	@echo ODEMKERROR: :U variable modifier - expected: oldvalue
	@echo ODEMKERROR: :U variable modifier - actual: ${VAR1:Unewvalue}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR2:Unewvalue} == "")
	@echo ODEMKERROR: :U variable modifier - expected: 
	@echo ODEMKERROR: :U variable modifier - actual: ${VAR2:Unewvalue}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR3:Unewvalue} == "")
	@echo ODEMKERROR: :U variable modifier - expected: ""
	@echo ODEMKERROR: :U variable modifier - actual: ${VAR3:Unewvalue}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR4:Unewvalue} == "newvalue")
	@echo ODEMKERROR: :U variable modifier - expected: "newvalue"
	@echo ODEMKERROR: :U variable modifier - actual: ${VAR4:Unewvalue}
.else
	@echo ODEMKPASS
.endif

