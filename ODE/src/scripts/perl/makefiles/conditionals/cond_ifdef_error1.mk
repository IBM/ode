#Testing ifdef with a malformed conditional

VAR1=
a:
.ifdef VAR2
	@echo PASS
.elifdef (VAR1 | VAR2)
	@echo FAIL
.else
.endif
