################################################################################
# include_searchorder.mk - makefile to test the search order in including files
#
# used by the perl script MkIncludeTest.pm
# uses 3 test makefiles: testfile1.mk
#                        testfile1.mk, testfile7.mk in another location
# To be run as:
# mk -f include_searchorder.mk FILEPATH=<path> FILEPATH1=<path1> PASS=[1 2 3 4]
# path =  the path to the directory having testfile1.mk
# path1 = the path to the directory having the other 2 files 
# PASS set from the command line to either 1, 2, 3 or 4
#
# CONTENTS OF TESTFILE1.MK:
# testvar1=100
# CONTENTS OF FILEPATH1/TESTFILE1.MK:
# testvar1=200
# testvar6=300
# CONTENTS OF FILEPATH1/TESTFILE7.MK:
# testvar6=400
#
# Notice that testvar1 is redefined in FILEPATH1/testfile1.mk. But since
# FILEPATH is before FILEPATH1 in .PATH, mk gets testvar1=100 rather than 
# testvar1=200
# Also, testvar6 is defined in both FILEPATH/testfile1.mk and 
# FILEPATH1/testfile7.mk.
# When PASS=1, FILEPATH/testfile1.mk gets included first but since it does 
# not define testvar6, it picks testvar6 from testfile7.mk and hence 
# testvar6=400. Note that, it ignores testvar6 defined in FILEPATH1/testfile1.mk
# When PASS=2, testfile7.mk gets included first and hence testvar6=400
# PASS 3 is same as PASS 1 except that include is replaced by tryinclude
# PASS 4 is same as PASS 2 except that include is replaced by tryinclude
######################################################################################
all : test1 test2
.PATH : ${FILEPATH} ${FILEPATH1}

.if (${PASS}==1)
.include "testfile1.mk"
.include "testfile7.mk"
.elif (${PASS}==2)
.include "testfile7.mk"
.include "testfile1.mk"
.elif (${PASS}==3)
.tryinclude "testfile1.mk"
.tryinclude "testfile7.mk"
.elif (${PASS}==4)
.tryinclude "testfile7.mk"
.tryinclude "testfile1.mk"
.endif

test1:
.if (${testvar1}==100)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for include failed
.endif

test2:
.if (${testvar6}==400)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 2 for include failed
.endif
