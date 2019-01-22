#################################################
#rtcond_target.mk - makefile to test runtime conditional
#                  target
#
# used by the perl script MkRTConditionalTest.pm
#################################################
target1 : test1 test2 test3
target2 : test4

test1:
	.rif !target(target1)
	@echo ODEMKERROR: Test 1 for target failed
	.relif !target(target2)
	@echo ODEMKERROR: Test 1 for target failed
	.relif !target(target3)
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 1 for target failed
	.rendif

test2:
	.rif target(target1)
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 2 for target failed
	.rendif

test3:
	.rif (defined(UNIX) && (target(TARGET1) || target(target2target2)))
	@echo ODEMKERROR: Test 3 for target failed
	.relif (defined(UNIX) && (target(target1) && target(target2)))
	@echo ODEMKPASS
	.relif (!defined(UNIX) && (target(TARGET1) && target(target2)))
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 3 for target failed
	.rendif

test4:
	@echo ODEMKERROR: Test 4 for target failed
