#######################################################
#cond_exists.mk - makefile to test conditional exists
#
# used by the perl script MkConditionalTest.pm
# variables TMPFILE, LOGFILE, MACHINE are set from the 
# command line
#######################################################
all : test1 test2 test3 test4
.PATH:${TMPFILE} ${LOGFILE}
FILE=tempfile

test1:
.if exists(temp.txt)
	@echo ODEMKERROR: Test 1 for exists failed
.elif exists(tempfile.txt)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for exists failed
.endif

test2:
.if exists("tempfile.txt")
	@echo ODEMKERROR: Test 2 for exists failed
.elif exists(${FILE}.txt)
	@echo ODEMKPASS
.else 
	@echo ODEMKERROR: Test 2 for exists failed
.endif

test3:
.if exists(tempfile.*)
	@echo ODEMKERROR: Test 3 for exists failed
.elif exists(${MACHINE}.testlog)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 3 for exists failed
.endif

test4:
.if (!exists(tempfile.txt) || exists(tempfiletxt))
	@echo ODEMKERROR: Test 4 for exists failed
.elif (exists(tempfile.txt) && exists(${MACHINE}.testlog))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 4 for exists failed
.endif

