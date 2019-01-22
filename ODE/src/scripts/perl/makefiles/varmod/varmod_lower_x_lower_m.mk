#############################################
# varmod_lower_x_lower_m.mk - makefile to test the
# :xm variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=file1.c foo.o main.c main.o file2.obj file3
VAR2=hello world
VAR3=hello?world hello hello*world hello
VAR4=hello[*]world

all: test1 test2 test3 test4 test5 test6 test7

test1:
.if (!${VAR1:xm/o/} == "file1.c foo.o main.c main.o file2.obj file3")
	@echo ODEMKERROR: :xm variable modifier - expected: file1.c foo.o main.c main.o file2.obj file3
	@echo ODEMKERROR: :xm variable modifier - actual: ${VAR1:xm/f/}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR1:xm/main[.]c main[.]o/} == "file1.c foo.o main.c main.o file2.obj file3")
	@echo ODEMKERROR: :xm variable modifier - expected: file1.c foo.o main.c main.o file2.obj file3
	@echo ODEMKERROR: :xm variable modifier - actual: ${VAR1:xm/main[.]c main[.]o/}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR1:xm/main[.].*\$/} == "file1.c foo.o main.c main.o file2.obj file3")
	@echo ODEMKERROR: :xm variable modifier - expected: file1.c foo.o main.c main.o file2.obj file3
	@echo ODEMKERROR: :xm variable modifier - actual: ${VAR1:xm/main[.].*\$/}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR3:xm/${VAR4}/} == "hello?world hello hello*world hello")
	@echo ODEMKERROR: :xm variable modifier - expected: hello?world hello hello*world hello
	@echo ODEMKERROR: :xm variable modifier - actual: ${VAR3:xm/${VAR4}/}
.else
	@echo ODEMKPASS
.endif

test5:
.if (!${VAR2:xm/(HELLO|WORLD)/ie} == "hello world")
	@echo ODEMKERROR: :xm variable modifier - expected: hello world
	@echo ODEMKERROR: :xm variable modifier - actual: ${VAR2:xm/(HELLO|WORLD)/ie}
.else
	@echo ODEMKPASS
.endif

test6:
.if (!${VAR3:xm?\??} == "hello?world hello hello*world hello")
	@echo ODEMKERROR: :xm variable modifier - expected: hello?world hello hello*world hello
	@echo ODEMKERROR: :xm variable modifier - actual: ${VAR3:xm?\??}
.else
	@echo ODEMKPASS
.endif

test7:
.if (!${VAR4:xm/\([[][*][]]\)\{1\}/} == "hello[*]world")
	@echo ODEMKERROR: :xm variable modifier - expected:hello[*]world
	@echo ODEMKERROR: :xm variable modifier - actual: ${VAR4:xm/\([[][*][]]\)\{1\}/}
.else
	@echo ODEMKPASS
.endif
