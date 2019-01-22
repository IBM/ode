#############################################
# varmod_upper_p.mk - makefile to test the
# :P variable modifier
#
# used by the perl script MkVarModTest.pm
# - FINDPATH - search path to find the file
# - VAR1 - file to find
# - RES1 - expected result of var mod operation
#############################################
VAR1P2=${*.testlog:P}

.PATH: ${FINDPATH}
NOFILE=notafile

all: test1 test2 test3

test1:
.if (${${VAR1}:P} != ${RES1})
	@echo ODEMKERROR: :P variable modifier - expected: ${RES1} 
	@echo ODEMKERROR: :P variable modifier - actual: ${${VAR1}:P}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${${NOFILE}:P} != "${NOFILE}")
	@echo ODEMKERROR: :P variable modifier - expected: ${NOFILE}
	@echo ODEMKERROR: :P variable modifier - actual: ${${NOFILE}:P}
.else
	@echo ODEMKPASS
.endif

test3:
.if (${*.testlog:P} == "*.testlog")
	@echo ODEMKERROR: :P variable modifier wildcards - expected at least file: ${RES1} 
	@echo ODEMKERROR: :P variable modifier wildcards - actual: ${*.testlog:P}
.else
# this tests both :P and :M wildcard behavior
.if (${VAR1P2:M${RES1}} != ${RES1})
	@echo ODEMKERROR: :P and :M variable modifier wildcards - expected: ${RES1} 
	@echo ODEMKERROR: :P and :M variable modifier wildcards - actual: ${VAR1P2:M${RES1}}
.else
	@echo ODEMKPASS
.endif
.endif

