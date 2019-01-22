#Testing ifnmake with a malformed conditional
target1:
.ifnmake ${target1}
	@echo PASS
.else
	@echo FAIL
.endif
