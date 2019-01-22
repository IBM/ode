####################################################
# var_target.mk - makefile to test .TARGET
#
# run as
#   mk -f var_target.mk
#
# Used by the perl script MkImpVarTest.pm
####################################################
targ1 targ2: test1 test2
	@echo target is ${.TARGET}

#.TARGET is overwritten but should not affect the tests
.TARGET=xyz
test1:
	@echo target for test1 is ${.TARGET}

#Testing the use of .TARGET in the dependency line
test2: $${.TARGET}.c
	@echo target for test2 is ${@}

test2.c:
	@echo target for test3 is ${.TARGET}
