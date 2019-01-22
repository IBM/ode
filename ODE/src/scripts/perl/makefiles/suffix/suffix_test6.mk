#############################################
# suffix_test6.mk - makefile to test overriding
# the default suffix transformations by using an
# explicit dependency.
#
# used by the perl script MkSuffixTest.pm
#############################################
# Testing Suffix search order.
#
.SUFFIXES: .1 .2 .3 .4

all: f.4
f.4: f.3  # Explicit dependency to use different suffix rules

f.1:
	@echo Working with: ${.TARGET}

.1.3:
	@echo Converting: $(.IMPSRC) to ${.TARGET}

.3.4:
	@echo Converting: $(.IMPSRC) to ${.TARGET}

.2.4:
	@echo Converting: $(.IMPSRC) to ${.TARGET}

.1.2:
	@echo Converting: $(.IMPSRC) to ${.TARGET}
