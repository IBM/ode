##########################################################################
# include.mk - makefile to test include
#
# used by the perl script MkIncludeTest.pm
#
# To be run as
#  mk -f include.mk FILEPATH=<path1> -I <path2>
# uses 5 test makefiles: testfile1.mk, testfile2.mk, testfile3.mk, 
#                        testfile4.mk, testfile5.mk
# path1 = path to the directory having the test makefiles 1 to 4
# path2 = path to the directory having testfile5.mk
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
# CONTENTS OF TESTFILE5.MK:
# testvar5=100
##########################################################################

all : test1 test2 test3 test4
FILENAME=testfile2.mk
.PATH : ${FILEPATH} 

.include "testfile1.mk"
.include "${FILENAME}"
.include "testfile3.mk"
.include <testfile5.mk>

test1:
.if (${testvar1}==100)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for include failed
.endif

## Testing variable expansion in .include
test2:
.if (${testvar2}==1000)
	@echo ODEMKERROR: Test 2 for include failed
.elif (defined(testvar2) && (${testvar2}==100))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 2 for include failed
.endif

## Testing nested include logic
test3:
.if (${testvar4}==100)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 3 for include failed
.endif

test4:
.if (${testvar5}==100)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 4 for include failed
.endif
