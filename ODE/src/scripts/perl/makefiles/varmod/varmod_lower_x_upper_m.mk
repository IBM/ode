#############################################
# varmod_lower_x_upper_m.mk - makefile to test the
# :xM variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=file1.c foo.o main.c main.o file2.obj file3
VAR2=hello world
VAR3=hello?world hello hello*world hello
VAR4=hello[*]world

all: test1 test2 test3 test4 test5 test6 test7

test1:
.if (!${VAR1:xM/o/} == "foo.o main.o file2.obj")
	@echo ODEMKERROR: :xM variable modifier - expected: foo.o main.o file2.obj
	@echo ODEMKERROR: :xM variable modifier - actual: ${VAR1:xM/o/}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR1:xM/main[.]c main[.]o/} == "")
	@echo ODEMKERROR: :xM variable modifier - expected: 
	@echo ODEMKERROR: :xM variable modifier - actual: ${VAR1:xM/main[.]c main[.]o/}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR1:xM/main[.].\$/} == "main.c main.o")
	@echo ODEMKERROR: :xM variable modifier - expected: main.c main.o
	@echo ODEMKERROR: :xM variable modifier - actual: ${VAR1:xM/main[.].\$/}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR3:xM/${VAR4}/} == "hello*world")
	@echo ODEMKERROR: :xM variable modifier - expected: hello*world
	@echo ODEMKERROR: :xM variable modifier - actual: ${VAR3:xM/${VAR4}/}
.else
	@echo ODEMKPASS
.endif

test5:
.if (!${VAR2:xM/(HELLO|WORLD)/ie} == "hello world")
	@echo ODEMKERROR: :xM variable modifier - expected: hello world
	@echo ODEMKERROR: :xM variable modifier - actual: ${VAR2:xM/(HELLO|WORLD)/ie}
.else
	@echo ODEMKPASS
.endif

test6:
.if (!${VAR3:xM?\??} == "hello?world")
	@echo ODEMKERROR: :xM variable modifier - expected: hello?world
	@echo ODEMKERROR: :xM variable modifier - actual: ${VAR3:xM?\??}
.else
	@echo ODEMKPASS
.endif

test7:
.if (!${VAR4:xM/\([[][*][]]\)\{1\}/} == "hello[*]world")
	@echo ODEMKERROR: :xM variable modifier - expected: hello[*]world
	@echo ODEMKERROR: :xM variable modifier - actual: ${VAR4:xM/\([[][*][]]\)\{1\}/}
.else
	@echo ODEMKPASS
.endif
