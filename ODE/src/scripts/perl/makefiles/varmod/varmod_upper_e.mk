#############################################
# varmod_upper_e.mk - makefile to test the
# :E variable modifier
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


all: test1 test2 test3 test4 test5 test6 test7 test8 test9 test10

test1:
.if (!${VAR1:E} == ".o")
	@echo ODEMKERROR: :E variable modifier - expected: .o
	@echo ODEMKERROR: :E variable modifier - actual: ${VAR1:E}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR2:E} == ".mk")
	@echo ODEMKERROR: :E variable modifier - expected: .mk
	@echo ODEMKERROR: :E variable modifier - actual: ${VAR2:E}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR3:E} == "")
	@echo ODEMKERROR: :E variable modifier - expected: 
	@echo ODEMKERROR: :E variable modifier - actual: ${VAR3:E}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR4:E} == ".cpp")
	@echo ODEMKERROR: :E variable modifier - expected: .cpp 
	@echo ODEMKERROR: :E variable modifier - actual: ${VAR4:E}
.else
	@echo ODEMKPASS
.endif

test5:
.if (!${VAR5:E} == ".")
	@echo ODEMKERROR: :E variable modifier - expected: .
	@echo ODEMKERROR: :E variable modifier - actual: ${VAR5:E}
.else
	@echo ODEMKPASS
.endif

test6:
.if (!${VAR6:E} == ".obj")
	@echo ODEMKERROR: :E variable modifier - expected: .obj 
	@echo ODEMKERROR: :E variable modifier - actual: ${VAR6:E}
.else
	@echo ODEMKPASS
.endif

test7:
.if (!${VAR7:E} == ".o .mk .cpp . .obj")
	@echo ODEMKERROR: :E variable modifier - expected: .o .mk .cpp .obj
	@echo ODEMKERROR: :E variable modifier - actual: ${VAR7:E}
.else
	@echo ODEMKPASS
.endif


test8:
.if (!${VAR8:E} == ".one.2 .StartsWith.Bar .")
	@echo ODEMKERROR: :E variable modifier - expected: .one.2 .StartwWith.Bar .
	@echo ODEMKERROR: :E variable modifier - actual: ${VAR8:E}
.else
	@echo ODEMKPASS
.endif


test9:
.if (!${VAR9:E} == "2")
	@echo ODEMKERROR: :E variable modifier - expected: 2
	@echo ODEMKERROR: :E variable modifier - actual: ${VAR9:E}
.else
	@echo ODEMKPASS
.endif


test10:
.if (!${VAR10:E} == ".")
	@echo ODEMKERROR: :E variable modifier - expected: . 
        @echo ODEMKERROR: :E variable modifier - actual: ${VAR10:E}
.else
	@echo ODEMKPASS
.endif


