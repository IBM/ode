###################################################
# make1.mk - makefile to test special sources .MAKE
#            and .PMAKE
#
# run as:
#    mk -f make1.mk [-t] [-n]
#
# used by the perl script MkSpecSrcTest.pm
###################################################
all : test1 test2 test3

test1: .MAKE
	@echo executing test1

test2: 
	@echo executing test2

test3: .PMAKE
	@echo executing test3


## should not be executed
.MAKE:
	@echo executing make

.PMAKE:
	@echo executing make

