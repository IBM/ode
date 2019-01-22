#################################################################
# suffixes_error1.mk - makefile to test special target .SUFFIXES
#                      should produce an error
#
# run as:
#    mk -f suffixes_error1.mk
#
# used by the perl script MkSpecTgtTest.pm
#################################################################

all: f.3

.SUFFIXES: .3 .2 .1 
.1.2:
	@echo Converting $(.IMPSRC) to ${.TARGET}
## should be .2.3: instead
.3.2:
	@echo Converting $(.IMPSRC) to ${.TARGET}

f.1:
	@echo Making ${.TARGET}
