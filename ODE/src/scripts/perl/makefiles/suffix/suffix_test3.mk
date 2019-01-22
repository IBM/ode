#############################################
# suffix_test3.mk - makefile to test an invalid
# circular suffix transformation rule.  mk should
# complain and exit with a non-zero error code
#
# used by the perl script MkSuffixTest.pm
#############################################

all:  test1.o
test1.c:
	@echo Working with: ${.TARGET}

.SUFFIXES: .c .o
.c.o:
	@echo Converting: $(.IMPSRC) to ${.TARGET}
.c.c:
	@echo Converting: $(.IMPSRC) to ${.TARGET}
