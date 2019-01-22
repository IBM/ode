#############################################
# varmod_lower_b.mk - makefile to test the
# :b variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=oldvalue					
VAR2=""
VAR3=

all: test1 test2 test3 test4

test1:
.if (!${VAR1:bnewvalue} == "newvalue")
	@echo ODEMKERROR: :b variable modifier - expected: newvalue
	@echo ODEMKERROR: :b variable modifier - actual: ${VAR1:Bnewvalue}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR2:bnewvalue} == "newvalue")
	@echo ODEMKERROR: :b variable modifier - expected: newvalue
	@echo ODEMKERROR: :b variable modifier - actual: ${VAR2:Bnewvalue}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR3:bnewvalue} == "")
	@echo ODEMKERROR: :b variable modifier - expected: ""
	@echo ODEMKERROR: :b variable modifier - actual: ${VAR3:Bnewvalue}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR4:bnewvalue} == "")
	@echo ODEMKERROR: :B variable modifier - expected: ""
	@echo ODEMKERROR: :B variable modifier - actual: ${VAR4:bnewvalue}
.else
	@echo ODEMKPASS
.endif
