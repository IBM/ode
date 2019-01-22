#####################################################
#envvars.mk - makefile to echo environment variables
#
# used by the perl script MkVarAssgnTest.pm
#
# To be run as
#   mk -f envvars.mk
#####################################################
all: test1

test1:
	@echo ${HOME}
	@echo ${PATH}
