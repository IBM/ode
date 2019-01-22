#############################################
# varmod_lower_n.mk - makefile to test the
# :n variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=file1.c foo.o main.c main.o file2.obj file3
VAR2=hello world
VAR3=hello?world hello
VAR4=hello*world hello

all: test1 test2 test3 test4 test5 test6 test7

test1:
.if (!${VAR1:nf*} == "")
	@echo ODEMKERROR: :n variable modifier - expected:
	@echo ODEMKERROR: :n variable modifier - actual: ${VAR1:nf*}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR1:nfile} == "file1.c foo.o main.c main.o file2.obj file3")
	@echo ODEMKERROR: :n variable modifier - expected: file1.c foo.o main.c main.o file2.obj file3
	@echo ODEMKERROR: :n variable modifier - actual: ${VAR1:nfile}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR1:nmain.?} == "file1.c foo.o main.c main.o file2.obj file3")
	@echo ODEMKERROR: :n variable modifier - expected: file1.c foo.o main.c main.o file2.obj file3
	@echo ODEMKERROR: :n variable modifier - actual: ${VAR1:nmain.?}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR1:n*.[oc]} == "file1.c foo.o main.c main.o file2.obj file3")
	@echo ODEMKERROR: :n variable modifier - expected: file1.c foo.o main.c main.o file2.obj file3
	@echo ODEMKERROR: :n variable modifier - actual: ${VAR1:n*.[oc]}
.else
	@echo ODEMKPASS
.endif

test5:
.if (!${VAR2:nhello world} == "hello world")
	@echo ODEMKERROR: :n variable modifier - expected: hello world
	@echo ODEMKERROR: :n variable modifier - actual: ${VAR2:nhello world}
.else
	@echo ODEMKPASS
.endif

test6:
.if (!${VAR3:n*[?]*} == "")
	@echo ODEMKERROR: :n variable modifier - expected:
	@echo ODEMKERROR: :n variable modifier - actual: ${VAR3:n*[?]*}
.else
	@echo ODEMKPASS
.endif

test7:
.if (!${VAR4:n*[*]*} == "")
	@echo ODEMKERROR: :n variable modifier - expected:
	@echo ODEMKERROR: :n variable modifier - actual: ${VAR4:n*[*]*}
.else
	@echo ODEMKPASS
.endif
