#############################################
# suffix_test4.mk - makefile to test an invalid
# circular suffix transformation rule.  mk should
# complain and exit with a non-zero error code
#
# used by the perl script MkSuffixTest.pm
#############################################

all:  test1.o test1.c

.SUFFIXES: .o .c
.c.o:
	@echo Converting: $(.IMPSRC) to ${.TARGET}
.o.c:
	@echo Converting: $(.IMPSRC) to ${.TARGET}
