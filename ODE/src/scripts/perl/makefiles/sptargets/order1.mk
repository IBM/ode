####################################################
# order1.mk - makefile to test special target .ORDER
#
# run as:
#    mk -f order1.mk 
#
# used by the perl script MkSpecTgtTest.pm
####################################################
all: test1 test2 
test1: test3
	@echo test1

test2:
	@echo test2

test3: test4 test5
	@echo test3

test4:
	@echo test4

test5: test6 test7
	@echo test5

test6:
	@echo test6

test7:
	@echo test7

.ORDER: test2 test1 test7 test6
