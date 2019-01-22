#############################################
# varmod_upper_m.mk - makefile to test the
# :M variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=file1.c foo.o main.c main.o file2.obj file3
VAR2=hello world
VAR3=hello?world hello hello*world hello
VAR4=hello*world hello

all: test1 test2 test3 test4 test5 test6 test7

test1:
.if (!${VAR1:Mf*} == "file1.c foo.o file2.obj file3")
	@echo ODEMKERROR: :M variable modifier - expected: file1.c foo.o file2.obj file3
	@echo ODEMKERROR: :M variable modifier - actual: ${VAR1:Mf*}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR1:Mfile} == "")
	@echo ODEMKERROR: :M variable modifier - expected: 
	@echo ODEMKERROR: :M variable modifier - actual: ${VAR1:Mfile}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR1:Mmain.?} == "main.c main.o")
	@echo ODEMKERROR: :M variable modifier - expected: main.c main.o
	@echo ODEMKERROR: :M variable modifier - actual: ${VAR1:Mmain.?}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR1:M*.[oc]} == "file1.c foo.o main.c main.o")
	@echo ODEMKERROR: :M variable modifier - expected: file1.c foo.o main.c main.o
	@echo ODEMKERROR: :M variable modifier - actual: ${VAR1:M*.[oc]}
.else
	@echo ODEMKPASS
.endif

test5:
.if (!${VAR2:Mhello world} == "hello world")
	@echo ODEMKERROR: :M variable modifier - expected: hello world
	@echo ODEMKERROR: :M variable modifier - actual: ${VAR2:Mhello world}
.else
	@echo ODEMKPASS
.endif

test6:
.if (!${VAR3:M*[?]*} == "hello?world")
	@echo ODEMKERROR: :M variable modifier - expected: hello?world
	@echo ODEMKERROR: :M variable modifier - actual: ${VAR3:M*[?]*}
.else
	@echo ODEMKPASS
.endif

test7:
.if (!${VAR4:M*[*]*} == "hello*world")
	@echo ODEMKERROR: :M variable modifier - expected: hello*world
	@echo ODEMKERROR: :M variable modifier - actual: ${VAR4:M*[*]*}
.else
	@echo ODEMKPASS
.endif
