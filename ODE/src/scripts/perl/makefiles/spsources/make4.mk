###################################################
# make3.mk - makefile to test special source .PMAKE
#
# run as:
#    mk -f make4.mk [-t] [-n]
#
# used by the perl script MkSpecSrcTest.pm
###################################################
all : test1

test1: .PMAKE test2 test3 test4 
	@echo executing test1
	@echo oodate is ${.OODATE}

test2: .PMAKE
	@echo executing test2
	@echo executing test2
	@echo executing test2

test3: .PMAKE
	@echo executing test3
        
test4: .PMAKE
	@echo executing test4




