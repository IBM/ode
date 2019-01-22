#############################################
# varmod_lower_p.mk - makefile to test the
# :p variable modifier
#
# used by the perl script MkVarModTest.pm
# - FINDPATH - search path to find the file
# - VAR1 - file to find
# - RES1 - expected result of var mod operation
#############################################

.PATH: ${FINDPATH}
NOFILE=notafile
VAR1P=*.testlog
VAR1P2=${VAR1P:p}

all: test1 test2 test3

test1:
.if (${VAR1:p} != ${RES1})
	@echo ODEMKERROR: :p variable modifier - expected: ${RES1} 
	@echo ODEMKERROR: :p variable modifier - actual: ${VAR1:p}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${NOFILE:p} != "${NOFILE}")
	@echo ODEMKERROR: :p variable modifier - expected: ${NOFILE}
	@echo ODEMKERROR: :p variable modifier - actual: ${${NOFILE}:p}
.else
	@echo ODEMKPASS
.endif

test3:
.if (${VAR1P:p} == "*.testlog")
	@echo ODEMKERROR: :p variable modifier wildcards - expected at least file: ${RES1} 
	@echo ODEMKERROR: :p variable modifier wildcards - actual: ${VAR1P:p}
.else
# this tests both :p and :M wildcard behavior
.if (${VAR1P2:M${RES1}} != ${RES1})
	@echo ODEMKERROR: :p and :M variable modifier wildcards - expected: ${RES1} 
	@echo ODEMKERROR: :p and :M variable modifier wildcards - actual: ${VAR1P2:M${RES1}}
.else
	@echo ODEMKPASS
.endif
.endif

