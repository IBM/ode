###################################################
#cond_defined.mk - makefile to test conditional
#	           defined
#
# used by the perl script MkConditionalTest.pm
###################################################
all: test1 test2 test3 test4
VAR1=var1
VAR2=
VAR3=VAR1

test1:
.if !defined(VAR1)
	@echo ODEMKERROR: Test 1 for defined failed
.elif !defined(VAR2)
	@echo ODEMKERROR: Test 1 for defined failed
.elif !defined(VAR4)
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 1 for defined failed
.endif

test2:
.if defined($VAR1)
	@echo ODEMKERROR: Test 2 for defined failed
.elif defined(${VAR1})
	@echo ODEMKERROR: Test 2 for defined failed
.else
	@echo ODEMKPASS
.endif

test3:
.if defined($VAR3)
	@echo ODEMKERROR: Test 3 for defined failed
.elif defined(${VAR3})
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 3 for defined failed
.endif

test4:
.if (defined(var1) || !!defined(var1))
	@echo ODEMKERROR: Test 4 for defined failed
.elif (!defined(var1) && !!defined(VAR1))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 4 for defined failed
.endif
