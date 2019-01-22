#############################################
# varmod_lower_u.mk - makefile to test the
# :u variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=hello
VAR2=Hello World
VAR3=hello1234WORLD
VAR4=HELLO/world

all: test1 test2 test3 test4

test1:
.if (!${VAR1:u} == "HELLO")
	@echo ODEMKERROR: :u variable modifier - expected: HELLO
	@echo ODEMKERROR: :u variable modifier - actual: ${VAR1:u}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR2:u} == "HELLO WORLD")
	@echo ODEMKERROR: :u variable modifier - expected: HELLO WORLD
	@echo ODEMKERROR: :u variable modifier - actual: ${VAR2:u}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR3:u} == "HELLO1234WORLD")
	@echo ODEMKERROR: :u variable modifier - expected: HELLO1234WORLD
	@echo ODEMKERROR: :u variable modifier - actual: ${VAR3:u}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR4:u} == "HELLO/WORLD")
	@echo ODEMKERROR: :u variable modifier - expected: HELLO/WORLD
	@echo ODEMKERROR: :u variable modifier - actual: ${VAR4:u}
.else
	@echo ODEMKPASS
.endif
