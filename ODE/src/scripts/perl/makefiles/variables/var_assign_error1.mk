#######################################################
# var_assign_error1.mk - makefile to test = operator
#   Should produce an error as it tries to assign a 
#   a value to $
#
# used by the perl script MkVarAssgnTest.pm
#######################################################
$=tmp1
test:
.if ($$==tmp1)
	@echo PASS
.else
	@echo FAIL
.endif
