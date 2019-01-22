#####################################################
# var_vpath.mk - makefile to test VPATH
#
# run as
#    mk -f var_vpath.mk FILEPATH=<path>
# <path> is a path to the directory having testfile.mk
#
# Contents of test.mk:
# testvar=100
#
# Used by the perl script MkImpVarTest.pm
#####################################################
VPATH=${FILEPATH}
.include "testfile.mk"
test:
.if (${testvar}==100)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for VPATH failed
.endif
