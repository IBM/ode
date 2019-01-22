####################################################
# var_allsrc.mk - makefile to test .ALLSRC
#
# run as:
#   mk -f var_allsrc.mk
#
# Used by the perl script MkVarImpVarTest.pm
#
####################################################

all: test1 test2 test3
	@echo using allsrc, ${.ALLSRC}
	@echo using symbol, ${>}

test1: test4

test2:

test3:

test4:

