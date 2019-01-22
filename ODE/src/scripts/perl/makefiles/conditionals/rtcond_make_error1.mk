#To test a malformed runtime conditional

target1: test1

test1:
	.rif make(!target1)
	@echo FAIL
	.relse
	@echo PASS
