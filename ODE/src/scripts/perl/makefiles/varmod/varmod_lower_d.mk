#############################################
# varmod_lower_d.mk - makefile to test the
# :d variable modifier
#
# used by the perl script MkVarModTest.pm
# - FINDPATH - search path to find the directory
# - VAR1 - directory to find
# - VAR2 - directory to find, with * in place of last char
# - RES1 - expected result of var mod operation
#############################################

.PATH: ${FINDPATH}
NOFILE=notafile
VAR1P=${VAR2}
VAR1P2=${VAR1P:d}

all: test1 test2 test3

test1:
.if (${VAR1:d} != ${RES1})
	@echo ODEMKERROR: :d variable modifier - expected: ${RES1} 
	@echo ODEMKERROR: :d variable modifier - actual: ${VAR1:d}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${NOFILE:d} != "${NOFILE}")
	@echo ODEMKERROR: :d variable modifier - expected: ${NOFILE}
	@echo ODEMKERROR: :d variable modifier - actual: ${NOFILE:d}
.else
	@echo ODEMKPASS
.endif

test3:
.if (${VAR1P:d} != ${RES1})
	@echo ODEMKERROR: :d variable modifier wildcards - expected at least file: ${RES1}
	@echo ODEMKERROR: :d variable modifier wildcards - actual: ${VAR1P:d}
.else
# this tests both :d and :M wildcard behavior
.if (${VAR1P2:M${RES1}} != ${RES1})
	@echo ODEMKERROR: :d and :M variable modifier wildcards - expected: ${RES1} 
	@echo ODEMKERROR: :d and :M variable modifier wildcards - actual: ${VAR1P2:M${RES1}}
.else
	@echo ODEMKPASS
.endif
.endif

