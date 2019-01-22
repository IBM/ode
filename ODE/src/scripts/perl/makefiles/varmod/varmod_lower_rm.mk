#############################################
# varmod_lower_rm.mk - makefile to test the
# :rm variable modifier
#
# run as
#   mk -f varmod_lower_rm.mk VAR1=<string1> 
# string1: PATH SEPARATER
#
# used by the perl script MkVarModTest.pm
#############################################

TEXTVAR=BlahBlahBlah
FILE=rmfile
DIRECTORY=rmdir
SUBDIR=${DIRECTORY}/subdir
RMVAR=${DIRECTORY}${VAR1}${FILE}

all: test1 test2 test3 test4 test5 test6 test7 test8 test9 test10

test1:
.if (${DIRECTORY:C} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rm variable modifier - Can't create dir 
.endif

test2:
.if (${TEXTVAR:A${FILE}} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rm variable modifier - Can't create file 
.endif

test3:
.if (${RMVAR:rm} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rm variable modifier - expected: "" 
	@echo ODEMKERROR: :rm variable modifier - actual: ${RMVAR:rm}
.endif

test4:
.if (${DIRECTORY:XD} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rm variable modifier - :XD reports dir still exists 
.endif

test5:
.if (${FILE:XF} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rm variable modifier - :XF reports file still exists
.endif

test6:
.if (${FILE:rm} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rm variable modifier - expected: ""
	@echo ODEMKERROR: :rm variable modifier - actual: ${FILE:rm}
.endif

test7:
.if (${RMVAR:rm-} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rm variable modifier - expected: "" 
	@echo ODEMKERROR: :rm variable modifier - actual: ${RMVAR:rm-}
.endif

test8:
.if (${SUBDIR:C} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rm variable modifier - Can't create subdir
.endif

test9:
.if (${DIRECTORY:rm} == ${DIRECTORY})
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rm variable modifier - expected: ${DIRECTORY} 
	@echo ODEMKERROR: :rm variable modifier - actual: ${DIRECTORY:rm}
.endif

test10:
.if (${SUBDIR:XD} == ${SUBDIR})
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: :rm variable modifier - :XD reports subdir deleted
.endif

.ORDER: test1 test2 test3 test4 test5 test6 test7 test8 test9 test10
