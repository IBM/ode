################################################
# spectarg.mk - makefile to test .SPECTARG
#
# run as:
#    mk -f spectarg.mk -t
#
# used by the perl script MkSpecSrcTest.pm
################################################
test1 : .SPECTARG 
	@echo test1 should not be touched or echoed

all : test1 test2 test3

test2 : .SPECTARG test21
	@echo test2 should not be touched or echoed

test3: 
	@echo this should not be echoed

test21:
	@echo this should not be echoed
