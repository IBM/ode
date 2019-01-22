#####################################################
#cond_empty.mk - makefile to test empty
#
# used by the perl script MkConditionalTest.pm
#####################################################
all : test1 test2 test3 test4 test5
VAR=""
VAR1=

test1:
.if empty(VAR)
	@echo ODEMKERROR: Test 1 for empty failed
.else
	@echo ODEMKPASS
.endif

test2:
.if empty(VAR1)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 2 for empty failed
.endif

# No space between if and empty, VAR with a $
test3:
.ifempty(${VAR})
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 3 for empty failed
.endif

# No parenthesis for VAR
test4:
.if empty VAR
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 4 for empty failed
.endif

test5:
.if (empty(VAR) && empty(VAR1))
	@echo ODEMKERROR: Test 5 for empty failed
.elif (!empty(VAR) && empty(VAR1))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 5 for empty failed
.endif
