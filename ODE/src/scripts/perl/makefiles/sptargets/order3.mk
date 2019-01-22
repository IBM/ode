################################################################
# order3.mk - makefile to test special target .ORDER
#
# run as:
#    mk -f order3.mk -j3
#    Though -j3 is specified, jobs should follow .ORDER
#
# used by the perl script MkSpecTgtTest.pm
################################################################

all : test1 test2 test3

.ORDER : test1 test2 test3

test1:
	@echo starting test1
	@echo ending test1

test2:
	@echo starting test2
	@echo ending test2
 
test3:
	@echo starting test3
	@echo ending test3

