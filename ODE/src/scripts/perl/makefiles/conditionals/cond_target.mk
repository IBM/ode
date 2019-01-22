#################################################
#cond_target.mk - makefile to test conditional
#                  target
#
# used by the perl script MkConditionalTest.pm
#################################################
target1 : test1 test2 test3
target2 : test4

test1:
.if !target(target1)
	@echo ODEMKERROR: Test 1 for target failed
.elif !target(target2)
	@echo ODEMKERROR: Test 1 for target failed
.elif !target(target3)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for target failed
.endif

test2:
.if target target1
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 2 for target failed
.endif

test3:
.if (defined(UNIX) && (target(TARGET1) || target(target2target2)))
	@echo ODEMKERROR: Test 3 for target failed
.elif (defined(UNIX) && (target(target1) && target(target2)))
	@echo ODEMKPASS
.elif (!defined(UNIX) && (target(TARGET1) && target(target2)))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 3 for target failed
.endif

test4:
	@echo ODEMKERROR: Test 4 for target failed
