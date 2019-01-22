###############################################################
# notmain_error1.mk - makefile to test special source .NOTMAIN 
#              should produce an error as all the targets have
#              .NOTMAIN
#
# run as:
#    mk -f notmain_error1.mk
#
# Used by the perl script MkSpecSrcTest.pm
###############################################################

targ1: .NOTMAIN 
	@echo targ1

targ2: .NOTMAIN
	@echo targ2

