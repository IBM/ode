########################################################
#cond_ifnmake - makefile to test conditional
#               ifnmake
#
#used by the perl script MkConditionalTest.pm
#should be used as mk -f cond_ifnmake target1 target2
########################################################
target1: test1 test2
target2: test3 test4
target3: test5

test1:
.ifnmake target1
	@echo ODEMKERROR: Test 1 for .ifnmake failed
.elifnmake target2
	@echo ODEMKERROR: Test 1 for .ifnmake failed
.elifnmake target3
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for .ifnmake failed
.endif

test2:
.ifnmake (target1 && target2) 
	@echo ODEMKERROR: Test 2 for .ifnmake failed
.elifnmake (target2 && target3)
	@echo ODEMKERROR: Test 2 for .ifnmake failed
.elifnmake (!target1 && !target2)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 2 for .ifnmake failed
.endif

test3:
.ifnmake (target1 || target2)
	@echo ODEMKERROR: Test 3 for .ifnmake failed
.elifnmake (target2 || target3)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 3 for .ifnmake failed
.endif

# Testing the case-sensitiveness of the target
test4:
.ifnmake TARGET1
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 4 for .ifnmake failed
.endif

# This test should not be executed
test5:
	@echo ODEMKERROR: Test 5 for .ifnmake failed
