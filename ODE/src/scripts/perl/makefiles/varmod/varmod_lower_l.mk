#############################################
# varmod_lower_l.mk - makefile to test the
# :l variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=HELLO
VAR2=Hello World
VAR3=hello1234WORLD
VAR4=HELLO/world

all: test1 test2 test3 test4

test1:
.if (!${VAR1:l} == "hello")
	@echo ODEMKERROR: :l variable modifier - expected: hello
	@echo ODEMKERROR: :l variable modifier - actual: ${VAR1:l}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR2:l} == "hello world")
	@echo ODEMKERROR: :l variable modifier - expected: hello world
	@echo ODEMKERROR: :l variable modifier - actual: ${VAR2:l}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR3:l} == "hello1234world")
	@echo ODEMKERROR: :l variable modifier - expected: hello1234world
	@echo ODEMKERROR: :l variable modifier - actual: ${VAR3:l}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR4:l} == "hello/world")
	@echo ODEMKERROR: :l variable modifier - expected: hello/world
	@echo ODEMKERROR: :l variable modifier - actual: ${VAR4:l}
.else
	@echo ODEMKPASS
.endif
