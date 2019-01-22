#############################################
# varmod_upper_t.mk - makefile to test the
# :T variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=/usr/bin/chown ../file1.c file2.o /file3.c
VAR2=c:\usr ..\file1.c file2.o \file3.c
VAR3=../dir1/file1.c/ /home/.profile ./../../dir2\dir3

all: test1 test2 test3

test1:
.if (!${VAR1:T} == "chown file1.c file2.o file3.c")
	@echo ODEMKERROR: :T variable modifier - expected: chown file1.c file2.o file3.c
	@echo ODEMKERROR: :T variable modifier - actual: ${VAR1:T}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR2:T} == "usr file1.c file2.o file3.c")
	@echo ODEMKERROR: :T variable modifier - expected: usr file1.c file2.o file3.c
	@echo ODEMKERROR: :T variable modifier - actual: ${VAR2:T}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR3:T} == ".profile dir3")
	@echo ODEMKERROR: :T variable modifier - expected: file1.c .profile dir3
	@echo ODEMKERROR: :T variable modifier - actual: ${VAR3:T}
.else
	@echo ODEMKPASS
.endif
