#############################################
# varmod_upper_h.mk - makefile to test the
# :H variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=/usr/bin/chown ../file1.c file2.o /file3.c	
VAR2=c:\usr ..\file1.c file2.o \file3.c
VAR3=../dir1/file1.c/ /home/.profile ./../../dir2\dir3

all: test1 test2 test3

test1:
.if (!${VAR1:H} == "/usr/bin .. .")
	@echo ODEMKERROR: :H variable modifier - expected: /usr/bin .. .
	@echo ODEMKERROR: :H variable modifier - actual: ${VAR1:H}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR2:H} == "c: .. .")
	@echo ODEMKERROR: :H variable modifier - expected: c: .. .
	@echo ODEMKERROR: :H variable modifier - actual: ${VAR2:H}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR3:H} == "../dir1/file1.c /home ./../../dir2")
	@echo ODEMKERROR: :H variable modifier - expected: ../dir1/file1.c /home ./../../dir2
	@echo ODEMKERROR: :H variable modifier - actual: ${VAR3:H}
.else
	@echo ODEMKPASS
.endif
