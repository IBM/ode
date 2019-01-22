#### Testing defined with a malformed runtime conditional
all: test1 
VAR1=var1

test1:
	.rif defined(!VAR1)
	@echo PASS
	.relse
	@echo FAIL
	.rendif
