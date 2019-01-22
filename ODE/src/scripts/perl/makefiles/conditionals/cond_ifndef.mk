####################################################
#cond_ifndef.mk - makefile to test conditional
#                 .ifndef
#
# used by the perl script MkConditonalTest.pm
####################################################
all: test1 test2 test3 test4 test5 
VAR1=var1					
VAR2=

test1:
.ifndef VAR1
	@echo ODEMKERROR: Test 1 for .ifndef failed
.elifndef VAR2
	@echo ODEMKERROR: Test 1 for .ifndef failed
.elifndef VAR3
	@echo ODEMKPASS 
.else
	@echo ODEMKERROR: Test 1 for .ifndef failed
.endif

test2:
.ifndef (VAR1 || VAR2)
	@echo ODEMKERROR: Test 2 for .ifndef failed
.elifndef (VAR1 || VAR3)
	@echo ODEMKPASS 
.else
	@echo ODEMKERROR: Test 2 for .ifndef failed
.endif

test3:
.ifndef (VAR1 && VAR2)
	@echo ODEMKERROR: Test 3 for .ifndef failed
.elifndef (!VAR1 && !VAR2)
	@echo ODEMKPASS 
.else
	@echo ODEMKERROR: Test 3 for .ifndef failed
.endif

test4:
.ifndef (VAR1 || !VAR3)
	@echo ODEMKERROR: Test 4 for .ifndef failed
.elifndef (VAR2 && !VAR3)
	@echo ODEMKERROR: Test 4 for .ifndef failed
.else
	@echo ODEMKPASS 
.endif

test5:
.ifndef !(!VAR3)
	@echo ODEMKPASS 
.else
	@echo ODEMKERROR: Test 5 for .ifndef failed
.endif

