############################################################
# invisible.mk - makefile to test special target .INVISIBLE
#
# run as:
#     mk -f invisible.mk
#
# used by the perl script MkSpecSrcTest.pm
############################################################
all: src1 src2 src3
	@echo In all, allsrc=${.ALLSRC}
	@echo In all, oodate=${.OODATE}

src1: .INVISIBLE src11 src12
	@echo In src1, allsrc=${.ALLSRC}
	@echo In src1, oodate=${.OODATE}

src11: .INVISIBLE 
	@echo In src11, allsrc=${.ALLSRC}
	@echo In src11, oodate=${.OODATE}

src12:

src2: 

src3: src3a .INVISIBLE src3b
	@echo In src3, allsrc=${.ALLSRC}
	@echo In src3, oodate=${.OODATE}
  
src3a:

src3b:

