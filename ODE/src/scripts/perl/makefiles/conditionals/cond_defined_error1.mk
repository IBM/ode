#### Testing defined with a malformed conditional
all: test1 
VAR1=var1

test1:
.if defined(!VAR1)
	@echo PASS
.else
	@echo FAIL
.endif
