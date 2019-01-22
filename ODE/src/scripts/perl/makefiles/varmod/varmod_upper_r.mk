#############################################
# varmod_upper_r.mk - makefile to test the
# :R variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=fubar.o					
VAR2=osf.rules.mk
VAR3=file1
VAR4=foo.cpp
VAR5=file2.
VAR6=.obj
VAR7=fubar.o osf.rules.mk file1 foo.cpp file2. .obj
VAR8=foo|.one.2  |.StartsWith.Bar  Ends_in.Bar|  |  .|.
VAR9=foo|.1|2  
VAR10=.


all: test1 test2 test3 test4 test5 test6 test7 test8 test9 test10 test11


test1:
.if (!${VAR1:R} == "fubar")
	@echo ODEMKERROR: :R variable modifier - expected: fubar
	@echo ODEMKERROR: :R variable modifier - actual: ${VAR1:R}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR2:R} == "osf.rules")
	@echo ODEMKERROR: :R variable modifier - expected: osf.rules
	@echo ODEMKERROR: :R variable modifier - actual: ${VAR2:R}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR3:R} == "file1")
	@echo ODEMKERROR: :R variable modifier - expected: file1
	@echo ODEMKERROR: :R variable modifier - actual: ${VAR3:R}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR4:R} == "foo")
	@echo ODEMKERROR: :R variable modifier - expected: foo 
	@echo ODEMKERROR: :R variable modifier - actual: ${VAR4:R}
.else
	@echo ODEMKPASS
.endif

test5:
.if (!${VAR5:R} == "file2")
	@echo ODEMKERROR: :R variable modifier - expected: file2
	@echo ODEMKERROR: :R variable modifier - actual: ${VAR5:R}
.else
	@echo ODEMKPASS
.endif

test6:
.if (!${VAR6:R} == "")
	@echo ODEMKERROR: :R variable modifier - expected: 
	@echo ODEMKERROR: :R variable modifier - actual: ${VAR6:R}
.else
	@echo ODEMKPASS
.endif

test7:
.if (!${VAR7:R} == "fubar osf.rules file1 foo file2")
	@echo ODEMKERROR: :R variable modifier - expected: fubar osf.rules file1 foo file2
	@echo ODEMKERROR: :R variable modifier - actual: ${VAR7:R}
.else
	@echo ODEMKPASS
.endif


test8:
.if (!${VAR8:R} == "foo Ends_in.Bar .")
	@echo ODEMKERROR: :R variable modifier - expected: .one.2 .StartwWith.Bar .
	@echo ODEMKERROR: :R variable modifier - actual: ${VAR8:R}
.else
	@echo ODEMKPASS
.endif


test9:
.if (!${VAR9:R} == "foo|.1")
	@echo ODEMKERROR: :R variable modifier - expected: 2
	@echo ODEMKERROR: :R variable modifier - actual: ${VAR9:R}
.else
	@echo ODEMKPASS
.endif


test10:
.if (!${VAR10:R} == "")
	@echo ODEMKERROR: :R variable modifier - expected: 
	@echo ODEMKERROR: :R variable modifier - actual: ${VAR10:R}
.else
	@echo ODEMKPASS
.endif


test11:
.if (!${VAR2:R:R} == "osf")
	@echo ODEMKERROR: :R variable modifier - expected: osf
	@echo ODEMKERROR: :R variable modifier - actual: ${VAR2:R:R}
.else
	@echo ODEMKPASS
.endif
