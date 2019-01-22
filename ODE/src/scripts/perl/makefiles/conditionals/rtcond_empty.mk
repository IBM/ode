#####################################################
#rtcond_empty.mk - makefile to test empty
#
# used by the perl script MkRTConditionalTest.pm
#####################################################
all : test1 test2 test3 test4
VAR=""
VAR1=

test1:
	.rif empty(VAR)
	@echo ODEMKERROR: Test 1 for empty failed
	.relse
	@echo ODEMKPASS
	.rendif

test2:
	.rif empty(VAR1)
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 2 for empty failed
	.rendif

# No space between if and empty, VAR with a $
test3:
	.rifempty(${VAR})
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 3 for empty failed
	.rendif

test4:
	.rif (empty(VAR) && empty(VAR1))
	@echo ODEMKERROR: Test 5 for empty failed
	.relif (!empty(VAR) && empty(VAR1))
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 5 for empty failed
	.rendif
