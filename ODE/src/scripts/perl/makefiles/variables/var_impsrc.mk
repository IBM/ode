###################################################
# var_impsrc.mk - makefile to test .IMPSRC
#
# run as
#    mk -f var_impsrc.mk
#
# Used by the perl script MkImpVarTest.pm
###################################################

all: test.a

.SUFFIXES: .c .o .a
.c.o:
	@echo using impsrc, ${.IMPSRC}
	@echo using symbol, ${<}
	@echo using symbol without braces, $<
.o.a:
	@echo using impsrc, ${.IMPSRC}
	@echo using symbol, ${<}
  
test.c:
