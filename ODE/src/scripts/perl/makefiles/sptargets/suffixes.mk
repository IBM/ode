##########################################################
# suffixes.mk - makefile to test special target .SUFFIXES
#
# run as:
#    mk -f suffixes.mk
#
# used by the perl script MkSpecTgtTest.pm
##########################################################

all: test.a testb|.tar.Z f.2 g.2
.ORDER: test.a testb|.tar.Z f.2 g.2

# testing conversion of a .c to .o to .a file
test.c:
	@echo Making ${.TARGET}

.SUFFIXES: .c .o .a
.o.a:
	@echo Converting $(.IMPSRC) to ${.TARGET}
.c.o:
	@echo Converting $(.IMPSRC) to ${.TARGET}



# testing the conversion of .tar to .tar.Z
testb.tar:
	@echo Making ${.TARGET}

.SUFFIXES: .tar .tar.Z
.tar|.tar.Z:
	@echo Converting $(.IMPSRC) to ${.TARGET}


# testing Suffix search order and using same multiple suffixes.
.SUFFIXES: .1 .2 .3 .1 .1 .2 .3

.3.2:
	@echo Converting $(.IMPSRC) to ${.TARGET}
	
.1.2:
	@echo Converting $(.IMPSRC) to ${.TARGET}

g.3:
	@echo Making ${.TARGET}
g.1:
	@echo Making ${.TARGET}

f.3:
	@echo Making ${.TARGET}
f.1:
	@echo Making ${.TARGET}
