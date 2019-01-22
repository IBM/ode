#######################################################
# var_assign_error2.mk - makefile to test != operator
#   Should produce an error as it tries to execute a 
#   shell command "crapcommand" which is invalid
#
# used by the perl script MkVarAssgnTest.pm
#######################################################
VAR1!=crapcommand
test:
.if (${VAR1}==crapcommand)
	@echo PASS
.else
	@echo FAIL
.endif
