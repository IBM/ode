########################################################
# notmain.mk - makefile to test special source .NOTMAIN 
#
# run as:
#    mk -f notmain.mk
#
# Used by the perl script MkSpecSrcTest.pm
########################################################

## using .NOTMAIN along with no source
targ1: .NOTMAIN
	@echo targ1 should not be executed first

## using .NOTMAIN along with multiple sources
targ2: .NOTMAIN targ21 targ22
	@echo targ2 should not be executed

## targ3 should be picked up as the default target
targ3: targ4
	@echo executing targ3

targ4: .NOTMAIN
	@echo executing targ4 

targ21: 
  
targ22:
 
targ31:


