#######################################################
# var_assign_error3.mk - makefile to test the use of 
#                        recursive variable
#   Should produce an error as its use is illegal
#
# used by the perl script MkVarAssgnTest.pm
#######################################################
VAR1=var1
VAR2=var2
VAR2=${VAR1}+${VAR2}
test:
.if (${VAR2}=="var1 var2")
	@echo PASS
.else
	@echo FAIL
.endif
