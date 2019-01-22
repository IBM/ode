###########################################################
# linktargs.mk - makefile to test special source .LINKTARGS
#
# run as:
#    mk -f linktargs.mk
#
# Used by the perl script MkSpecSrcTest.pm
#  targ1 & targ2:  .LINKTARGS should not do anything
#  targa & targb:  Cmds for pattern rule should only be
#                    executed once.
#  targx & targy:  Pattern rule example w/out .LINKTARGS
###########################################################

all : targ1 targ2 targa targb targx targy

targ1 targ2 : .LINKTARGS
	@echo ${.TARGET}

%a %b : %c .LINKTARGS 
	@echo making targa and targb

%x %y : %z 
	@echo making ${.TARGET}

targc targz : 
	@echo ${.TARGET}

