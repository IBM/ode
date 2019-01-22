#############################################
# suffix_test2.mk - makefile to test the
# suffix transformations with multiple dots
#
# used by the perl script MkSuffixTest.pm
#############################################

all:  test2|.tar.Z  test2
test2.tar:
	@echo "Working with: ${.TARGET}"

.SUFFIXES: .tar .tar.Z 
.tar|.tar.Z:
	@echo "Converting: $(.IMPSRC) to ${.TARGET}"
        
.tar.Z|:
	@echo "Converting: $(.IMPSRC) to ${.TARGET}"

