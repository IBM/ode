#############################################
# varmod_lower_a.mk - makefile to test the
# :a variable modifier
#
# run as
#   mk -f varmod_lower_a.mk FILE=<string1> VAR1=<string1> VAR2=<string2>
# string2: filename
# string1: text to append to FILE
# string2: additional text to append to FILE
#
# used by the perl script MkVarModTest.pm
#############################################

all: test1 test2

test1:
	.rif (${VAR1:a${FILE}} != "")
	@echo ODEMKERROR: :a variable modifier - expected: "" 
	@echo ODEMKERROR: :a variable modifier - actual: ${VAR1:a${FILE}}
	.relse
	@echo ODEMKPASS
	.rendif

test2:
	.rif (${VAR2:a${FILE}} != "")
	@echo ODEMKERROR: :a variable modifier - expected: "" 
	@echo ODEMKERROR: :a variable modifier - actual: ${VAR2:a${FILE}}
	.relse
	@echo ODEMKPASS
	.rendif

.ORDER: test1 test2

Errtest1:
	@echo ${VAR1:a}
