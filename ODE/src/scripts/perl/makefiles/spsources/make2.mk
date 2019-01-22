##################################################
# make2.mk - makefile to test special source .MAKE
#            when used as a target, it should
#            function like a normal target
#            Also test .PMAKE
#
# run as:
#    mk -f make2.mk [-t] [-n]
#
# used by the perl script MkSpecSrcTest.pm
##################################################
.MAKE: test1
	@echo executing make

.PMAKE: test1
	@echo executing pmake

test1 :
	@echo executing test1


