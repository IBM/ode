#Testing ifdef with an extra $

VAR1=
a:
.ifdef ${VAR2}
	@echo PASS
.else
	@echo FAIL
.endif
