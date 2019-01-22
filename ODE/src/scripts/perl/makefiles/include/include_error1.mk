############################################################
# include_error1.mk - makefile to test include
#  tries to include a non_existent_file
#
# used by the perl script MkIncludeTest.pm
# FILEPATH set from the command line to the directory 
# not having non_existent_file
############################################################
all : test1  
.PATH : ${FILEPATH}
.include "non_existent_file"

test1:
.if (${testvar1}==100)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for include failed
.endif

