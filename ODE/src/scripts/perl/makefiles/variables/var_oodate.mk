###############################################
# var_oodate.mk - makefile to test .OODATE
#
# run as
#   mk -f var_oodate.mk
#
# Used by the perl script MkImpVarTest.pm
###############################################

.ORDER: test1 test2 test3 test4
test: test1 test2 test3 test4
	@echo test is ${.OODATE}

test1: test1a
	@echo test1 is ${.OODATE}
test1a:

test2: .INVISIBLE test2a
	@echo test2 is ${?}
test2a:

test3: test3a
	@echo test3 is ${.OODATE}
test3a: .INVISIBLE test3b
	@echo test3a is $?
test3b:

test4: test4a test4b
	@echo test4 is ${.OODATE}
	@echo "test4" > test4
test4a:
	@echo "test4a" > test4a

