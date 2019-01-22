################################################################
# notparallel.mk - makefile to test special target .NOTPARALLEL
#
# run as:
#    mk -f notparallel.mk -j3
#
# used by the perl script MkSpecTgtTest.pm
################################################################

all : test1 test2 test3

## test4 should be ignored
.NOTPARALLEL: test4

test1:
	@echo starting test1
	@echo ending test1

test2:
	@echo starting test2
	@echo ending test2
 
test3:
	@echo starting test3
	@echo ending test3

test4:
	@echo executing test4
