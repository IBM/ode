#Testing ifmake with an undefined source
target1: test1 test2
VAR1=

test1:
.ifmake target1
	@echo PASS
.else
	@echo FAIL
.endif
