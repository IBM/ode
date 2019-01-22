#### Testing target with a malformed conditional
target1 : test1 

test1:
.if !target<target1>
	@echo FAIL
.else
	@echo PASS
.endif
