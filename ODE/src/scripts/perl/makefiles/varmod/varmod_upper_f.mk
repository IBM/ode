#############################################
# varmod_upper_f.mk - makefile to test the
# :F variable modifier
#
# used by the perl script MkVarModTest.pm
# variables passed in from the mk command line
# - FINDPATH - search path to find the file
# - VAR1 - file to find
# - RES1 - expected result of var mod operation
#############################################
VAR1F=${FINDPATH:F*.testlog}

all: test1 test2 test3

test1:
.if (${FINDPATH:F${VAR1}} != "${RES1}")
	@echo ODEMKERROR: :F variable modifier - expected: ${RES1}
	@echo ODEMKERROR: :F variable modifier - actual: ${FINDPATH:F${VAR1}}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${NOPATH:F${VAR1}} != "")
	@echo ODEMKERROR: :F variable modifier - expected: ""
	@echo ODEMKERROR: :F variable modifier - actual: ${NOPATH:F${VAR1}}
.else
	@echo ODEMKPASS
.endif

test3:
.if (${FINDPATH:F*.testlog} == "")
	@echo ODEMKERROR: :F variable modifier wildcards - expect at least file: ${RES1}
	@echo ODEMKERROR: :F variable modifier wildcards - actual: ${FINDPATH:F*.testlog}
.else
# this tests both :F and :M wildcard behavior
.if (${VAR1F:M${RES1}} != ${RES1})
	@echo ODEMKERROR: :F and :M variable modifier wildcards - expected: ${RES1} 
	@echo ODEMKERROR: :F and :M variable modifier wildcards - actual: ${VAR1F:M${RES1}}
.else
	@echo ODEMKPASS
.endif
.endif

