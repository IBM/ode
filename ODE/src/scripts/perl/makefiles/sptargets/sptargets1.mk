#######################################################
# sptargets1.mk - makefile to test special targets
#                 .BEGIN, .END, .EXIT
#
# run as:
#   mk -f sptargets.mk 
#
# used by the perl script MkSpecTgtTest.pm
#######################################################
test1:
	@echo test1 should be executed after .BEGIN

.EXIT:
	@echo .EXIT should be executed at the end

.END:
	@echo .END should be executed penultimately

.BEGIN:
	@echo .BEGIN should be executed first

.ERROR:
	@echo .ERROR should not be executed

.INTERRUPT:
	@echo .INTERRUPT should not be executed
