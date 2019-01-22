#########################################################
# normtarg.mk - makefile to test special source .NORMTARG
#
# run as:
#    mk -f normtarg.mk
#
# Used by the perl script MkSpecSrcTest.pm
#########################################################
 
.SUFFIXES: .a .b

all: transform.a .b.a

.b.a:
	@echo ${.TARGET}
	@echo ${.IMPSRC}

.b.a: transform.a .NORMTARG transform.b
	@echo ${.TARGET}

transform.b:
	@echo ${.TARGET}