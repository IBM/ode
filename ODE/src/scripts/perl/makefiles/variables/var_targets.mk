#################################################
# var_targets.mk - makefile to test .TARGETS
#
# run as
#    mk -f var_targets.mk targ1 targ2
#
# Used by the perl script MkImpVarTest.pm
#################################################
targ1 targ2 targ3: test1 test2
	@echo targets are ${.TARGETS}

test1:
	@echo targets for test1 are ${.TARGETS}

test2: test3
	@echo targets for test2 are ${.TARGETS}

test3:
	@echo targets for test3 are ${.TARGETS}
