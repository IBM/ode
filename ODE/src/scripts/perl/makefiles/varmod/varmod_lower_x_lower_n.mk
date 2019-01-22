#############################################
# varmod_lower_x_lower_n.mk - makefile to test the
# :xn variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=file1.c foo.o main.c main.o file2.obj file3
VAR2=hello world
VAR3=hello?world hello hello*world hello
VAR4=hello[*]world

all: test1 test2 test3 test4 test5 test6 test7

test1:
.if (!${VAR1:xn/f/} == "")
	@echo ODEMKERROR: :xn variable modifier - expected:
	@echo ODEMKERROR: :xn variable modifier - actual: ${VAR1:xn/f/}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR1:xn/main/} == "")
	@echo ODEMKERROR: :xn variable modifier - expected:
	@echo ODEMKERROR: :xn variable modifier - actual: ${VAR1:xn/main/}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR1:xn/main[.].\$/} == "file1.c foo.o main.c main.o file2.obj file3")
	@echo ODEMKERROR: :xn variable modifier - expected: file1.c foo.o main.c main.o file2.obj file3
	@echo ODEMKERROR: :xn variable modifier - actual: ${VAR1:xn/main[.]o\$/}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR3:xn/${VAR4}/} == "")
	@echo ODEMKERROR: :xn variable modifier - expected:
	@echo ODEMKERROR: :xn variable modifier - actual: ${VAR3:xn/${VAR4}/}
.else
	@echo ODEMKPASS
.endif

test5:
.if (!${VAR2:xn/(HELLO|WORLD)/ie} == "")
	@echo ODEMKERROR: :xn variable modifier - expected: 
	@echo ODEMKERROR: :xn variable modifier - actual: ${VAR2:xn/(HELLO|WORLD)/ie}
.else
	@echo ODEMKPASS
.endif

test6:
.if (!${VAR3:xn?\??} == "")
	@echo ODEMKERROR: :xn variable modifier - expected:
	@echo ODEMKERROR: :xn variable modifier - actual: ${VAR3:xn?\??}
.else
	@echo ODEMKPASS
.endif

test7:
.if (!${VAR4:xn/\([[][*][]]\)\{1\}/} == "")
	@echo ODEMKERROR: :xn variable modifier - expected:
	@echo ODEMKERROR: :xn variable modifier - actual: ${VAR4:xn/\([[][*][]]\)\{1\}/}
.else
	@echo ODEMKPASS
.endif
