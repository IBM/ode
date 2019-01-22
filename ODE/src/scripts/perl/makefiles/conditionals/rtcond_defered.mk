#######################################################
#rtcond_defered.mk - makefile to test runtime conditional
#
# used by the perl script MkRTConditionalTest.pm
# Tests if the runtime conditional is defered until
# all the makefile has been parsed and targets are
# being made.
#######################################################
all: test1 test2 test3 test4

VAR1=value1

test1:
.if ${VAR1}==value1
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for defered failed
.endif

test2:
	.rif ${VAR1}==value2
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 2 for defered failed
	.rendif

# this is the value VAR1 will have when making all
VAR1=value2

test3:
.if ${VAR1}==value2
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 3 for defered failed
.endif

test4:
	.rif ${VAR1}!=value1
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 4 for defered failed
	.rendif
