##############################################################
#cond_ifmake.mk - makefile to test conditional
#                 ifmake
#
#used by the perl script MkConditionalTest.pm
#should be used as mk -f cond_ifmake.mk target1 target2
##############################################################
target1: test1 test2
target2: test3 test4
target3: test5

test1:
.ifmake !target1
	@echo ODEMKERROR: Test 1 for .ifmake failed
.elifmake !target2
	@echo ODEMKERROR: Test 1 for .ifmake failed
.elifmake !(!target3)
	@echo ODEMKERROR: Test 1 for .ifmake failed
.else
	@echo ODEMKPASS
.endif

test2:
.ifmake (!target1 && target2)
	@echo ODEMKERROR: Test 2 for .ifmake failed
.elifmake (target1 && target2)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 2 for .ifmake failed
.endifcrap

test3:
.ifmake (!target1 || !target2)
	@echo ODEMKERROR: Test 3 for .ifmake failed
.elifmake (target1 || !target2)
	@echo ODEMKPASS
.endif

test4:
.ifmake test4
	@echo ODEMKERROR: Test 4 for .ifmake failed
.else
	@echo ODEMKPASS
.endif

#test5 should not be executed as only target1 and target2 are
#specified at the command line
test5:
	@echo ODEMKERROR: Test 5 for .ifmake failed
