################################################
#cond_ifdef.mk - makefile to test conditional
#                ifdef
#
#used by the perl script MkConditionalTest.pm
################################################
all: test1 test2 test3 test4
VAR1=var1					
VAR2="this is var2"
VAR3=

test1:
	.ifdef !VAR1
	  @echo ODEMKERROR: Test 1 for .if failed
	.elifdef !VAR2
	  @echo ODEMKERROR: Test 1 for .if failed
	.elifdef !VAR3
	  @echo ODEMKERROR: Test 1 for .if failed
	.elifdef !VAR4
	  @echo ODEMKPASS
	.else
	  @echo ODEMKERROR: Test 1 for .if failed
	.endif

test2:
.ifdef (!VAR1 || VAR4)
	@echo ODEMKERROR: Test 2 for .ifdef failed
.elifdef (VAR3 || VAR4)
	@echo ODEMKPASS 
.else
	@echo ODEMKERROR: Test 2 for .ifdef failed
.endif

.undef VAR1

test3:
.ifdef (VAR1 && VAR2)
	@echo ODEMKERROR: Test 3 for .ifdef failed
.elifdef (!VAR1 && VAR2)
	@echo ODEMKPASS 
.else
	@echo ODEMKERROR: Test 3 for .ifdef failed
.endif

test4:
.ifdef (VAR1 || VAR4)
	@echo ODEMKERROR: Test 4 for .ifdef failed
.elifdef ((VAR1 || VAR3) && (VAR2 && !VAR3))
	@echo ODEMKERROR: Test 4 for .ifdef failed
.else
	@echo ODEMKPASS 
#.endifcrap used intentionally, mk should not complain 
.endifcrap
