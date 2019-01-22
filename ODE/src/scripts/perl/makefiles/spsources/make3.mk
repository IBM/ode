##################################################
# make3.mk - makefile to test special source .MAKE
#
# run as:
#    mk -f make3.mk [-t] [-n]
#
# used by the perl script MkSpecSrcTest.pm
##################################################
all : test1

test1: .MAKE test2 test3 test4 
	@echo executing test1
	@echo oodate is ${.OODATE}

test2: .MAKE
	@echo executing test2
test3: .MAKE
	@echo executing test3
test4: .MAKE
	@echo executing test4




