#############################################
# suffix_test5.mk - makefile to test order of
#  precedence of suffix transformation rules
#  as specified on the .SUFFIXES line and
#  their respective order in the makefile.
#
# used by the perl script MkSuffixTest.pm
#############################################
# Testing Suffix search order.
#
.SUFFIXES: .1 .2 .3 .4

all: f.4  foo

f.1  foo.2:
	@echo Working with: ${.TARGET}

.1.3:
	@echo Converting: $(.IMPSRC) to ${.TARGET}

.3.4:
	@echo Converting: $(.IMPSRC) to ${.TARGET}

.2.4:
	@echo Converting: $(.IMPSRC) to ${.TARGET}

.1.2:
	@echo Converting: $(.IMPSRC) to ${.TARGET}

.4:
	@echo Converting: $(.IMPSRC) to ${.TARGET}

.2:
	@echo Converting: $(.IMPSRC) to ${.TARGET}

.3:
	@echo Converting: $(.IMPSRC) to ${.TARGET}
