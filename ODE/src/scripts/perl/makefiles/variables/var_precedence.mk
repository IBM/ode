##############################################################################
#var_precedence.mk - makefile to test variable precedence
#
# used by the perl script MkVarPrecTest.pm
#
# To be run as
# mk -f var_precedence.mk [-e FLAG=-E] PREC_VAR3=cmd_var3 PREC_VAR5=cmd_var5 
#                                      PREC_VAR6=cmd_var6 PREC_VAR7=cmd_var7 
#                                      FILEPATH=<path>
#
# path = path to the directory having testfile.mk
#
# CONTENTS OF TESTFILE.MK:
# PREC_VAR8=global_var8
#
# Variables to be set in the environment before running:
# PREC_VAR1=env_var1, PREC_VAR4=env_var4, PREC_VAR6=env_var6, 
# PREC_VAR7=env_var7, PREC_VAR8=env_var8
#
# The precedence of local variables is not tested in this makefile but would
# be tested while testing rules
###############################################################################

all: test1 test2 test3 test4 test5 test6 test7 test8
.PATH : ${FILEPATH}

PREC_VAR2=global_var2
PREC_VAR4=global_var4
PREC_VAR5=global_var5
PREC_VAR7=global_var7
.include "testfile.mk"

## PREC_VAR1 is set only in the environment
test1:
.if (${PREC_VAR1}=="env_var1")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for variable precedence failed
.endif

## PREC_VAR2 is set only as a global variable
test2:
.if (${PREC_VAR2}=="global_var2")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 2 for variable precedence failed
.endif

## PREC_VAR3 is set only from the command line
test3:
.if (${PREC_VAR3}=="cmd_var3")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 3 for variable precedence failed
.endif

## PREC_VAR4 is set in the environment and also as a global variable
## when -e is specified in the command line, environment variable
## should have precedence over the global variable
test4:
.if ((${FLAG}=="-E") && (${PREC_VAR4}=="env_var4"))
	@echo ODEMKPASS
.elif ((${FLAG}!="-E") && (${PREC_VAR4}=="global_var4"))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 4 for variable precedence failed
.endif

## PREC_VAR5 is set as a global variable and also from the command line
## command line variable should be dominant over the global variable
test5:
.if (${PREC_VAR5}=="cmd_var5")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 5 for variable precedence failed
.endif

## PREC_VAR6 is set from the command line and also as an environment variable
## command line variable should be dominant over the environment variable
## and -e should not change the precedence
test6:
.if (${PREC_VAR6}=="cmd_var6")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 6 for variable precedence failed
.endif

## PREC_VAR7 is set from the command line, as a global and also as an 
## environment variable
## command line variable should have the highest precedence
test7:
.if (${PREC_VAR7}=="cmd_var7")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 7 for variable precedence failed
.endif

## PREC_VAR8 is set as a global variable in the included file 
## "var_precedence1.mk" and also as an environment variable
## when -e is set from the command line, environment variables
## should have precedence over the global variables
test8:
.if ((${FLAG}=="-E") && (${PREC_VAR8}=="env_var8"))
	@echo ODEMKPASS
.elif ((${FLAG}!="-E") && (${PREC_VAR8}=="global_var8"))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 8 for variable precedence failed
.endif
