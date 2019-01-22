#############################################
# suffix_test1.mk - makefile to test the
# suffix transformations
#
# used by the perl script MkSuffixTest.pm
#############################################

all:  test1.a test1
test1.c:
	@echo Working with: ${.TARGET}

.SUFFIXES: .c .o .a
.o.a:
	@echo Converting: $(.IMPSRC) to ${.TARGET}
.c.o:
	@echo Converting: $(.IMPSRC) to ${.TARGET}

.c:
	@echo Converting: $(.IMPSRC) to ${.TARGET}
        

