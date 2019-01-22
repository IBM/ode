#####################################################################
# path.mk - makefile to test special target .PATH
#
# run as:
#   mk -f path.mk FILEPATH=<path> CFILEPATH=<path1>
#     the current directory must have testfile1.txt
#     path should have testfile1.txt, testfile2.txt, test1.c
#     path1 should have test1.c and test2.c
#
# testfile1.txt should get included from the current directory
# testfile2.txt should get included from FILEPATH
# test1.c should get included from FILEPATH. Note that, it doesn't 
#   get included from CFILEPATH though listed in .PATH.c because
#   it does not apply to included files
# test2.c should be seen in CFILEPATH
#
# Contents of ./testfile1.txt:
# testvar1=100
# Contents of ./dir1/testfile1.txt:
# testvar1=200
# Contents of ./dir1/testfile2.txt:
# testvar2=200
# Contents of ./dir1/test1.c:
# cvar1=2000
# Contents of ./dir1/dir2/test1.c:
# cvar1=3000
# Contents of ./dir1/dir2/test2.c:
# cvar2=3000
#
# used by the perl script MkSpecTgtTest.pm
#####################################################################

.PATH : ${FILEPATH}
.SUFFIXES : .c
.PATH.c : ${CFILEPATH}
.include "testfile1.txt"
.include "testfile2.txt"
.include "test1.c"

test: testfile1.txt test1.c test2.c 
	@echo testvar1 is ${testvar1}
	@echo testvar2 is ${testvar2}
	@echo cvar1 is ${cvar1}

.ERROR:
	@echo file not found
