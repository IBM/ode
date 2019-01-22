###########################################################################
# tryinclude.mk - makefile to test tryinclude
#
# used by the perl script MkIncludeTest.pm
# 
# To be run as:
#   mk -f tryinclude.mk FILEPATH=<path> -I <path1> PASS=[1 2]
# uses 4 makefiles: testfile1.mk, testfile2.mk, testfile3.mk, testfile4.mk
# path = the path to the directory having test makefiles 1 to 4
# path1 = the path to the directory having test makefiles 1 to 4
# PASS = 1 if -I is not specified and MAKEINCLUDECOMPAT is not defined
# PASS = 2 if either -I is specified or MAKEINCLUDECOMPAT is defined
#
# CONTENTS OF TESTFILE1.MK:
# testvar1=100
# CONTENTS OF TESTFILE2.MK:
# testvar2=100
# CONTENTS OF TESTFILE3.MK:
# .PATH : ${FILEPATH}
# .include "testfile4.mk"
# .tryinclude "non-existent-file"
# CONTENTS OF TESTFILE4.MK:
# testvar4=100
#############################################################################
all : test1 test2 test3
.PATH : ${FILEPATH}
FILENAME=testfile2.mk

.tryinclude "testfile1.mk"
.tryinclude <${FILENAME}>
.tryinclude <testfile3.mk>
## testfile7.mk doesn't exist in FILEPATH
.tryinclude "testfile7.mk"
.tryinclude <non-existent-file>

test1:
.if (${testvar1}==100)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for tryinclude failed
.endif

## Testing variable expansion in .tryinclude
test2:
.if ((${PASS}==1) && (${testvar2}!=100))
	@echo ODEMKPASS
.elif ((${PASS}==2) && (${testvar2}==100))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 2 for tryinclude failed
.endif

## Testing nested tryinclude logic
test3:
.if ((${PASS}==1) && (${testvar4}!=100))
	@echo ODEMKPASS
.elif ((${PASS}==2) && (${testvar4}==100))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 3 for tryinclude failed
.endif

