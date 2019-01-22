#############################################
# varmod_upper_s.mk - makefile to test the
# :S variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=abc aba bac
VAR2=ab/cd
VAR3=ab&cd
VAR4=ab^cd
VAR5=ab$$cd
VAR6=aba abc bac

all: test1 test2 test3 test4 test5 test6 test7 test8 test9 test10 test11

test1:
.if (${VAR1:S/c/X/} != "abX aba bac")
	@echo ODEMKERROR: :S variable modifier - expected: abX aba bac 
	@echo ODEMKERROR: :S variable modifier - actual: ${VAR1:S/c/X/}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${VAR1:S/c/X/g} != "abX aba baX")
	@echo ODEMKERROR: :S variable modifier - expected: abX aba baX 
	@echo ODEMKERROR: :S variable modifier - actual: ${VAR1:S/c/X/g}
.else
	@echo ODEMKPASS
.endif

test3:
.if (${VAR1:S/^a/X/g} != "Xbc Xba bac")
	@echo ODEMKERROR: :S variable modifier - expected: Xbc Xba bac 
	@echo ODEMKERROR: :S variable modifier - actual: ${VAR1:S/^a/X/g}
.else
	@echo ODEMKPASS
.endif

test4:
.if (${VAR1:S/a$/X/g} != "abc abX bac")
	@echo ODEMKERROR: :S variable modifier - expected: abc abX bac 
	@echo ODEMKERROR: :S variable modifier - actual: ${VAR1:S/a$/X/g}
.else
	@echo ODEMKPASS
.endif

test5:
.if (${VAR1:S/ab/&X/g} != "abXc abXa bac")
	@echo ODEMKERROR: :S variable modifier - expected: abXc abXa bac 
	@echo ODEMKERROR: :S variable modifier - actual: ${VAR1:S/ab/&X/g}
.else
	@echo ODEMKPASS
.endif

test6:
.if (${VAR2:S/\//X/g} != "abXcd")
	@echo ODEMKERROR: :S variable modifier - expected: abXcd
	@echo ODEMKERROR: :S variable modifier - actual: ${VAR2:S/\//X/g}
.else
	@echo ODEMKPASS
.endif

test7:
.if (${VAR3:S/\&/X/g} != "abXcd")
	@echo ODEMKERROR: :S variable modifier - expected: abXcd
	@echo ODEMKERROR: :S variable modifier - actual: ${VAR3:S/\&/X/g}
.else
	@echo ODEMKPASS
.endif

test8:
.if (${VAR4:S/\^/X/g} != "abXcd")
	@echo ODEMKERROR: :S variable modifier - expected: abXcd
	@echo ODEMKERROR: :S variable modifier - actual: ${VAR4:S/\^/X/g}
.else
	@echo ODEMKPASS
.endif

test9:
.if (${VAR5:S/\$/X/g} != "abXcd")
	@echo ODEMKERROR: :S variable modifier - expected: abXcd
	@echo ODEMKERROR: :S variable modifier - actual: ${VAR5:S/\$/X/g}
.else
	@echo ODEMKPASS
.endif

test10:
.if (${VAR1:S/a/X/f} != "Xbc Xba bXc")
	@echo ODEMKERROR: :S variable modifier - expected: Xbc Xba bXc 
	@echo ODEMKERROR: :S variable modifier - actual: ${VAR1:S/a/X/f}
.else
	@echo ODEMKPASS
.endif

test11:
.if (${VAR6:S/a/X/w} != "XbX abc bac")
	@echo ODEMKERROR: :S variable modifier - expected: XbX abc bac 
	@echo ODEMKERROR: :S variable modifier - actual: ${VAR6:S/a/X/w}
.else
	@echo ODEMKPASS
.endif
