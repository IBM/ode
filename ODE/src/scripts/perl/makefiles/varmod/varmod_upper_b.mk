#############################################
# varmod_upper_b.mk - makefile to test the
# :B variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=oldvalue					
VAR2=""
VAR3=

all: test1 test2 test3 test4

test1:
.if (!${VAR1:Bnewvalue} == "oldvalue")
	@echo ODEMKERROR: :B variable modifier - expected: oldvalue
	@echo ODEMKERROR: :B variable modifier - actual: ${VAR1:Bnewvalue}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR2:Bnewvalue} == "")
	@echo ODEMKERROR: :B variable modifier - expected:
	@echo ODEMKERROR: :B variable modifier - actual: ${VAR2:Bnewvalue}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR3:Bnewvalue} == "newvalue")
	@echo ODEMKERROR: :B variable modifier - expected: "newvalue"
	@echo ODEMKERROR: :B variable modifier - actual: ${VAR3:Bnewvalue}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR4:Bnewvalue} == "newvalue")
	@echo ODEMKERROR: :B variable modifier - expected: "newvalue"
	@echo ODEMKERROR: :B variable modifier - actual: ${VAR4:Bnewvalue}
.else
	@echo ODEMKPASS
.endif

