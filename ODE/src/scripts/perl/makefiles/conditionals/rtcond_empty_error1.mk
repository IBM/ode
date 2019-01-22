#To test runtime conditional empty with a malformed conditional
target1: test1
VAR=""

test1:
	.rif empty & 
	@echo FAIL
	.relse
	@echo PASS
	.rendif

