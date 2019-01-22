#To test a malformed conditional

target1: test1

test1:
.if make(!target1)
	@echo FAIL
.else
	@echo PASS
