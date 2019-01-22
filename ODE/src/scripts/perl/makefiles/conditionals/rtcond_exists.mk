#######################################################
#rtcond_exists.mk - makefile to test runtime conditional exists
#
# used by the perl script MkRTConditionalTest.pm
# variables TMPFILE, LOGFILE, MACHINE are set from the 
# command line
#######################################################
all : test1 test2 test3 test4
.PATH:${TMPFILE} ${LOGFILE}
FILE=tempfile

test1:
	.rif exists(temp.txt)
	@echo ODEMKERROR: Test 1 for exists failed
	.relif exists(tempfile.txt)
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 1 for exists failed
	.rendif

test2:
	.rif exists("tempfile.txt")
	@echo ODEMKERROR: Test 2 for exists failed
	.relif exists(${FILE}.txt)
	@echo ODEMKPASS
	.relse 
	@echo ODEMKERROR: Test 2 for exists failed
	.rendif

test3:
	.rif exists(tempfile.*)
	@echo ODEMKERROR: Test 3 for exists failed
	.relif exists(${MACHINE}.testlog)
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 3 for exists failed
	.rendif

test4:
	.rif (!exists(tempfile.txt) || exists(tempfiletxt))
	@echo ODEMKERROR: Test 4 for exists failed
	.relif (exists(tempfile.txt) && exists(${MACHINE}.testlog))
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 4 for exists failed
	.rendif

