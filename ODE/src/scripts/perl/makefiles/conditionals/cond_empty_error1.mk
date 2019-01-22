#To test conditional empty with a malformed conditional
target1: test1
VAR=""

test1:
.if empty & 
	@echo FAIL
.else
	@echo PASS
.endif

