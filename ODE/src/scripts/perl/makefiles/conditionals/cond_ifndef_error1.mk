#Testing ifndef with a malformed conditional
VAR1=

test1:
.ifndef !VAR2
	@echo PASS
.elifdef (VAR2=="")
	@echo FAIL
.else
.endif
