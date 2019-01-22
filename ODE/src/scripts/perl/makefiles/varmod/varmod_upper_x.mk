############################################################
# varmod_upper_x.mk - makefile to test the
# :XD, :XF, and :XB variable modifiers
#
# used by the perl script MkVarModTest.pm
# - VAR1 - variable containg full paths to 2 directories,
#          one that exists and one that doesn't, as well
#          as 2 files, one that exists and one that doesn't.
#          directories/files are separated by both whitespace
#          and path separator
# - RES_XD - full path of directory that exists in VAR1
# - RES_XF - full path of file that exists in VAR1
# - RES_XB - full path of both directory and file that
#            exists in VAR1, separated by whitespace
############################################################

all: test1 test2 test3

test1:
.if (${VAR1:XD} != "${RES_XD}" )
	@echo ODEMKERROR: :XD variable modifier - expected: ${RES_XD} 
	@echo ODEMKERROR: :XD variable modifier - actual: ${VAR1:XD}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${VAR1:XF} != "${RES_XF}" )
	@echo ODEMKERROR: :XF variable modifier - expected: ${RES_XF} 
	@echo ODEMKERROR: :XF variable modifier - actual: ${VAR1:XF}
.else
	@echo ODEMKPASS
.endif

test3:
.if (${VAR1:XB} != "${RES_XB}" )
	@echo ODEMKERROR: :XB variable modifier - expected: ${RES_XB} 
	@echo ODEMKERROR: :XB variable modifier - actual: ${VAR1:XB}
.else
	@echo ODEMKPASS
.endif

