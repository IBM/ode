##################################################
# var_target_error1.mk - makefile to test .TARGET
#     Should produce an error because .TARGET
#     is assigned inside a conditional in a target
#
# run as
#   mk -f var_target_error1.mk
#
# used by the perl script MkImpVarTest.pm
##################################################
all : test1 

test1:
.if (1)
.TARGET=xyz
.endif
	@echo first target is ${.TARGET}
