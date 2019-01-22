#Testing ifmake with a malformed conditional
target1: test1
VAR1=

test1:
.ifmake ${target1}
	@echo PASS
.else
	@echo FAIL
.endif
