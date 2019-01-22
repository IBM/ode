############################################################
# include_error2.mk - makefile to test include
#  tries to include a file present in FILEPATH before
#  defining .PATH               
#
# used by the perl script MkIncludeTest.pm
# FILEPATH set from the command line to the directory 
# having testfile1.mk
############################################################
all : test1  
.include "testfile1.mk"
.PATH : ${FILEPATH}

test1:
.if (${testvar1}==100)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for include failed
.endif

