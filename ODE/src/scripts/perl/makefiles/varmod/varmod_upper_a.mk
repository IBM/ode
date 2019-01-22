#############################################
# varmod_upper_A.mk - makefile to test the
# :A variable modifier
#
# run as
#   mk -f varmod_upper_a.mk FILE=<string1> VAR1=<string1> VAR2=<string2>
# string2: filename
# string1: text to append to FILE
# string2: other text to append to FILE
#
# used by the perl script MkVarModTest.pm
#############################################

all: test1 test2

test1:
.if (${VAR1:A${FILE}} != "")
	@echo ODEMKERROR: :A variable modifier - expected: "" 
	@echo ODEMKERROR: :A variable modifier - actual: ${VAR1:A${FILE}}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${VAR2:A${FILE}} != "")
	@echo ODEMKERROR: :A variable modifier - expected: "" 
	@echo ODEMKERROR: :A variable modifier - actual: ${VAR2:A${FILE}}
.else
	@echo ODEMKPASS
.endif

.ORDER: test1 test2

Errtest1:
	@echo ${VAR1:A}
