###########################################################################
#cond_make_target.mk - makefile to test conditional make
#
# used by the perl script MkConditionalTest.pm
# should be used as mk -f cond_make_target.mk cond_make_target.mk target1
##########################################################################
target1 : test1 test2 test3
target2 : test4

test1:
.if make(target2)
	@echo ODEMKERROR: Test 1 for make failed
.elif make(target1)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for make failed
.endif

test2:
.if !make target1
	@echo ODEMKERROR: Test 2 for make failed
.elif make target1
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 2 for make failed
.endif

test3:
.if (!make(target2) && make(TARGET1)) 
	@echo ODEMKERROR: Test 3 for make failed
.elif (!make(target1) || !make(target2)) 
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 3 for make failed
.endif

test4:
	@echo ODEMKERROR: Test 4 for make failed
