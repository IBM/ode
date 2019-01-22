#############################################
# varmod_upper_n.mk - makefile to test the
# :N variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=file1.c foo.o main.c main.o file2.obj file3
VAR2=hello world
VAR3=hello?world hello
VAR4=hello*world hello

all: test1 test2 test3 test4 test5 test6 test7

test1:
.if (!${VAR1:Nf*} == "main.c main.o")
	@echo ODEMKERROR: :N variable modifier - expected: main.c main.o
	@echo ODEMKERROR: :N variable modifier - actual: ${VAR1:Nf*}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR1:Nfile} == "file1.c foo.o main.c main.o file2.obj file3")
	@echo ODEMKERROR: :N variable modifier - expected: file1.c foo.o main.c main.o file2.obj file3
	@echo ODEMKERROR: :N variable modifier - actual: ${VAR1:Nfile}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR1:Nmain.?} == "file1.c foo.o file2.obj file3")
	@echo ODEMKERROR: :N variable modifier - expected: file1.c foo.o file2.obj file3
	@echo ODEMKERROR: :N variable modifier - actual: ${VAR1:Nmain.?}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR1:N*.[oc]} == "file2.obj file3")
	@echo ODEMKERROR: :N variable modifier - expected: file2.obj file3
	@echo ODEMKERROR: :N variable modifier - actual: ${VAR1:N*.[oc]}
.else
	@echo ODEMKPASS
.endif

test5:
.if (!${VAR2:Nhello world} == "")
	@echo ODEMKERROR: :N variable modifier - expected: 
	@echo ODEMKERROR: :N variable modifier - actual: ${VAR2:Nhello world}
.else
	@echo ODEMKPASS
.endif

test6:
.if (!${VAR3:N*[?]*} == "hello")
	@echo ODEMKERROR: :N variable modifier - expected: hello
	@echo ODEMKERROR: :N variable modifier - actual: ${VAR3:N*[?]*}
.else
	@echo ODEMKPASS
.endif

test7:
.if (!${VAR4:N*[*]*} == "hello")
	@echo ODEMKERROR: :N variable modifier - expected: hello
	@echo ODEMKERROR: :N variable modifier - actual: ${VAR4:N*[*]*}
.else
	@echo ODEMKPASS
.endif
