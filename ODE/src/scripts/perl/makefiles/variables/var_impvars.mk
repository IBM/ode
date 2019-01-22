##################################################################
# var_impvars.mk - makefile to test some of the implied variables
#
# run as
#    mk -f var_impvars.mk
#
# Used by the perl script MkImpVarTest.pm
##################################################################
test:
	@echo dollar is $$
	@echo make is ${MAKE:T}
	@echo curdir is ${CURDIR}
	@echo makeflags is ${MAKEFLAGS}
	@echo oderelease is ${ODERELEASE}
	@echo pound is "${POUND}"
