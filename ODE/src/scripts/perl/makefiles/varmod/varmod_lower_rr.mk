#############################################
# varmod_lower_rr.mk - makefile to test the
# :rr variable modifier
#
# run as
#   mk -f varmod_lower_rr.mk VAR1=<string1> 
# string1: PATH SEPARATER
#
# used by the perl script MkVarModTest.pm
#############################################

TEXTVAR=BlahBlahBlah
FILE=rrfile
DIRECTORY1=rrdir1
DIRECTORY2=rrdir2
SUBDIR1=${DIRECTORY1}/subdir1
SUBDIR2=${DIRECTORY2}/subdir2
RRVAR=${DIRECTORY1}${VAR1}${DIRECTORY2}

all: test1 test2 test3 test4 test5 test6 test7

test1:
.if (${SUBDIR1:C} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rr variable modifier - Can't create dir 
.endif

test2:
.if (${SUBDIR2:C} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rr variable modifier - Can't create dir
.endif

test3:
.if (${TEXTVAR:A${DIRECTORY1}/${FILE}} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rr variable modifier - Can't create file 
.endif

test4:
.if (${RRVAR:rr} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rr variable modifier - expected: "" 
	@echo ODEMKERROR: :rr variable modifier - actual: ${RRVAR:rr}
.endif

test5:
.if (${DIRECTORY1:XD} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rr variable modifier - :XD reports dir1 still exists 
.endif

test5:
.if (${DIRECTORY2:XD} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rr variable modifier - :XF reports dir2 still exists
.endif

test6:
.if (${DIRECTORY1:rr} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rr variable modifier - expected: ""
	@echo ODEMKERROR: :rr variable modifier - actual: ${DIRECTORY:rr}
.endif

test7:
.if (${DIRECTORY1:rr-} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rr variable modifier - expected: "" 
	@echo ODEMKERROR: :rr variable modifier - actual: ${DIRECTORY1:rr-}
.endif

.ORDER: test1 test2 test3 test4 test5 test6 test7
