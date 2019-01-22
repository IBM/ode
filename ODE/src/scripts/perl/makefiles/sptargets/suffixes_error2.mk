#################################################################
# suffixes_error2.mk - makefile to test special target .SUFFIXES
#                      should produce an error
#
# run as:
#    mk -f suffixes_error2.mk
#
# used by the perl script MkSpecTgtTest.pm
#################################################################

all: f.3

## note the absence of dots for the suffixes
.SUFFIXES: 3 2 1 
.1.2:
	@echo Converting $(.IMPSRC) to ${.TARGET}
.2.3:
	@echo Converting $(.IMPSRC) to ${.TARGET}

f.1:
	@echo Making ${.TARGET}
