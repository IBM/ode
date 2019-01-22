####################################################
# sptargets2.mk - makefile to test special targets
#                .BEGIN, .END, .EXIT, .MAIN, .ORDER
#
# run as:
#    mk -f sptargets2.mk 
#
# used by the perl script MkSpecTgtTest.pm
####################################################

test1: 
	@echo test1 should not be executed as .MAIN is specified
  
test2:
	@echo test2 should be executed after test3

test3:
	@echo test3 should be executed after .BEGIN

.BEGIN: .EXIT
	@echo .BEGIN should be executed after .EXIT

.END:
	@echo .END should be the last target

.EXIT:
	@echo .EXIT should be executed first 

.MAIN: test2 test3
	@echo .MAIN should be executed after test2

## Overwriting the order specified by .MAIN
.ORDER: test3 test2
