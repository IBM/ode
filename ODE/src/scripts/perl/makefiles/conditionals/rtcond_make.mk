###########################################################################
#rtcond_make.mk - makefile to test runtime conditional make
#
# used by the perl script MkRTConditionalTest.pm
# should be used as mk -f rtcond_make.mk target1
##########################################################################
target1 : test1 test2 test3
target2 : test4

test1:
	.rif make(target2)
	@echo ODEMKERROR: Test 1 for make failed
	.relif make(target1)
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 1 for make failed
	.rendif

test2:
	.rif !make(target1)
	@echo ODEMKERROR: Test 2 for make failed
	.relif make(target1)
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 2 for make failed
	.rendif

test3:
	.rif (!make(target2) && make(TARGET1)) 
	@echo ODEMKERROR: Test 3 for make failed
	.relif (!make(target1) || !make(target2)) 
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 3 for make failed
	.rendif

test4:
	@echo ODEMKERROR: Test 4 for make failed
