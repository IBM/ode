#############################################
# varmod_lower_x_upper_n.mk - makefile to test the
# :xN variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=file1.c foo.o main.c main.o file2.obj file3
VAR2=hello world
VAR3=hello?world hello hello*world hello
VAR4=hello[*]world

all: test1 test2 test3 test4 test5 test6 test7

test1:
.if (!${VAR1:xN/f/} == "main.c main.o")
	@echo ODEMKERROR: :xN variable modifier - expected: main.c main.o
	@echo ODEMKERROR: :xN variable modifier - actual: ${VAR1:xN/f/}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR1:xN/file/} == "foo.o main.c main.o")
	@echo ODEMKERROR: :xN variable modifier - expected: foo.o main.c main.o
	@echo ODEMKERROR: :xN variable modifier - actual: ${VAR1:xN/file/}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR1:xN/main[.].\$/} == "file1.c foo.o file2.obj file3")
	@echo ODEMKERROR: :xN variable modifier - expected: file1.c foo.o file2.obj file3
	@echo ODEMKERROR: :xN variable modifier - actual: ${VAR1:xN/main[.].\$/}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR3:xN/${VAR4}/} == "hello?world hello hello")
	@echo ODEMKERROR: :xN variable modifier - expected: hello?world hello hello
	@echo ODEMKERROR: :xN variable modifier - actual: ${VAR3:xN/${VAR4}/}
.else
	@echo ODEMKPASS
.endif

test5:
.if (!${VAR2:xN/(HELLO|WORLD)/ie} == "")
	@echo ODEMKERROR: :xN variable modifier - expected: 
	@echo ODEMKERROR: :xN variable modifier - actual: ${VAR2:xN/(HELLO|WORLD)/ie}
.else
	@echo ODEMKPASS
.endif

test6:
.if (!${VAR3:xN?\??} == "hello hello*world hello")
	@echo ODEMKERROR: :xN variable modifier - expected: hello hello*world hello
	@echo ODEMKERROR: :xN variable modifier - actual: ${VAR3:xN?\??}
.else
	@echo ODEMKPASS
.endif

test7:
.if (!${VAR4:xN/\([[][*][]]\)\{1\}/} == "")
	@echo ODEMKERROR: :xN variable modifier - expected:
	@echo ODEMKERROR: :xN variable modifier - actual: ${VAR4:xN/\([[][*][]]\)\{1\}/}
.else
	@echo ODEMKPASS
.endif
