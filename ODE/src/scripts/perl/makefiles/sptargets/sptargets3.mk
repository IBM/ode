####################################################
# sptargets3.mk - makefile to test special targets
#                .BEGIN, .END, .EXIT, .ERROR
#
# run as:
#    mk -f sptargets3.mk 
#
# used by the perl script MkSpecTgtTest.pm
####################################################
test1:
	crap

test2:
	@echo test2 should not be executed 

.BEGIN: 
	@echo .BEGIN should be executed first

.END:
	@echo .END should not be executed

.EXIT:
	@echo .EXIT executed at end

.ERROR:
	@echo .ERROR executed
