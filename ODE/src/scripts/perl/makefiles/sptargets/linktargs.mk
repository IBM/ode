################################################################
# linktargs.mk - makefile to test special target .LINKTARGS
#
# run as:
#    mk -f linktargs.mk
#
# used by the perl script MkSpecTgtTest.pm
################################################################

.LINKTARGS: a1 a2 a3
.LINKTARGS: d1 d2
.LINKTARGS: d3 d4
.LINKTARGS: d1 d3

all : a1 a2 a3 b1 b2 c1 c2 d3

a1 a2 a3 :
	@echo making a1 a2 a3

b1 b2 c3 c4 c5 d1:
	@echo ${.TARGET}

c1 : c3
	@echo ${.TARGET}

.LINKTARGS: c1 c2

c2 : c4 c5 .POSTCMDS
	@echo ${.ALLSRC}


d2 : .PRECMDS
	@echo pre-d cmd

d4 :
	@echo making d4
