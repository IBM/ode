############################################################
# var_assign_error4.mk - makefile to test the use of 
#                        operator = with mismatching quotes
#
# used by the perl script MkVarAssgnTest.pm
#############################################################
VAR1=var1
test:
.if (${VAR1}==var1")
	@echo PASS
.else
	@echo FAIL
.endif
