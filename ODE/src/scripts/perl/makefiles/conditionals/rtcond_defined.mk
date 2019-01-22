###################################################
#rtcond_defined.mk - makefile to test runtime conditional
#	           defined
#
# used by the perl script MkRTConditionalTest.pm
###################################################
all: test1 test2 test3 test4
VAR1=var1
VAR2=
VAR3=VAR1

test1:
	.rif !defined(VAR1)
	@echo ODEMKERROR: Test 1 for defined failed
	.relif !defined(VAR2)
	@echo ODEMKERROR: Test 1 for defined failed
	.relif !defined(VAR4)
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 1 for defined failed
	.rendif

test2:
	.rif defined($VAR1)
	@echo ODEMKERROR: Test 2 for defined failed
	.relif defined(${VAR1})
	@echo ODEMKERROR: Test 2 for defined failed
	.relse
	@echo ODEMKPASS
	.rendif

test3:
	.rif defined($VAR3)
	@echo ODEMKERROR: Test 3 for defined failed
	.relif defined(${VAR3})
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 3 for defined failed
	.rendif

test4:
	.rif (defined(var1) || !!defined(var1))
	@echo ODEMKERROR: Test 4 for defined failed
	.relif (!defined(var1) && !!defined(VAR1))
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 4 for defined failed
	.rendif
