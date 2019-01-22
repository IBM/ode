#############################################
# varmod_lower_m.mk - makefile to test the
# :m variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=file1.c foo.o main.c main.o file2.obj file3
VAR2=hello world
VAR3=hello?world hello hello*world hello
VAR4=hello*world hello

all: test1 test2 test3 test4 test5 test6 test7

test1:
.if (!${VAR1:mf*} == "file1.c foo.o main.c main.o file2.obj file3")
	@echo ODEMKERROR: :m variable modifier - expected: file1.c foo.o main.c main.o file2.obj file3
	@echo ODEMKERROR: :m variable modifier - actual: ${VAR1:mf*}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR1:mfile} == "")
	@echo ODEMKERROR: :m variable modifier - expected: 
	@echo ODEMKERROR: :m variable modifier - actual: ${VAR1:mfile}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR1:mmain.?} == "")
	@echo ODEMKERROR: :m variable modifier - expected:
	@echo ODEMKERROR: :m variable modifier - actual: ${VAR1:mmain.?}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR1:m*.[oc]} == "")
	@echo ODEMKERROR: :m variable modifier - expected:
	@echo ODEMKERROR: :m variable modifier - actual: ${VAR1:m*.[oc]}
.else
	@echo ODEMKPASS
.endif

test5:
.if (!${VAR2:mhello world} == "")
	@echo ODEMKERROR: :m variable modifier - expected:
	@echo ODEMKERROR: :m variable modifier - actual: ${VAR2:mhello world}
.else
	@echo ODEMKPASS
.endif

test6:
.if (!${VAR3:m*[?]*} == "hello?world hello hello*world hello")
	@echo ODEMKERROR: :m variable modifier - expected: hello?world hello hello*world hello
	@echo ODEMKERROR: :m variable modifier - actual: ${VAR3:m*[?]*}
.else
	@echo ODEMKPASS
.endif

test7:
.if (!${VAR4:m*[*]*} == "hello*world hello")
	@echo ODEMKERROR: :m variable modifier - expected:hello*world hello
	@echo ODEMKERROR: :m variable modifier - actual: ${VAR4:m*[*]*}
.else
	@echo ODEMKPASS
.endif
