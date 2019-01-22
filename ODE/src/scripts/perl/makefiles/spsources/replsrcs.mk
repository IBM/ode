#########################################################
# replsrcs.mk - makefile to test special source .REPLSRCS
#
# run as:
#    mk -f replsrcs.mk
#
# Used by the perl script MkSpecSrcTest.pm
#
# targ1 - simple target w/out REPLSRCS
# targ2 - simple target w/REPLSRCS, cmd1 should remain
# targ3 - LINKTARGS, REPLSRCS, and POSTCMDS, both cmds
#         should run, but only once b/c of link.
# targ4 - LINKED to targ3
# targ.b - Suffix Xform, cmd2 should be run.
# targ.c - Pattern Rule, cmd2 should be run.
#########################################################


all : targ1 targ2 targ3 targ4 targ.b targ.c

.LINKTARGS: targ3 targ4
.SUFFIXES: .a .b


targ1:  srcA  srcB
	@echo targ1-cmd1

targ1:  srcC
	@echo targ1-cmd2

targ2:  srcD
	@echo targ2-cmd1

targ2:  srcE  srcF  .REPLSRCS
	@echo targ2-cmd2

targ3:  srcG
	@echo targ3+4-cmd1

targ4:  srcH .REPLSRCS .POSTCMDS
	@echo targ3+4-cmd2

.a.b:  srcI
	@echo targ.b-cmd1

.a.b:  srcJ  .REPLSRCS
	@echo targ.b-cmd2

%c : %d  srcK srcL
	@echo targ.c-cmd1

%c : %d srcM  .REPLSRCS
	@echo targ.c-cmd2


srcA srcB srcC srcD srcE srcF srcG srcH srcI srcJ srcK srcL srcM targ.a targ.d:
	@echo ${.TARGET}
