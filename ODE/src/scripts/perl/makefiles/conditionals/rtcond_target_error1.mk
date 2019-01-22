#### Testing target with a malformed conditional
target1 : test1 

test1:
	.rif !target<target1>
	@echo FAIL
	.relse
	@echo PASS
	.rendif
