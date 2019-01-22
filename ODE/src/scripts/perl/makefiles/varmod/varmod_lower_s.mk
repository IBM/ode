#############################################
# varmod_lower_s.mk - makefile to test the
# :s variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=abc aba bac
VAR2=ab/cd
VAR3=ab&cd
VAR4=ab^cd
VAR5=ab$$cd

all: test1 test2 test3 test4 test5 test6 test7 test8 test9 test10 test11 test12

test1:
.if (${VAR1:s/c/X/} != "abX aba bac")
	@echo ODEMKERROR: :s variable modifier - expected: abX aba bac 
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR1:s/c/X/}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${VAR1:s/c/X/g} != "abX aba baX")
	@echo ODEMKERROR: :s variable modifier - expected: abX aba baX 
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR1:s/c/X/g}
.else
	@echo ODEMKPASS
.endif

test3:
.if (${VAR1:s/^a/X/g} != "Xbc aba bac")
	@echo ODEMKERROR: :s variable modifier - expected: Xbc aba bac 
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR1:s/^a/X/g}
.else
	@echo ODEMKPASS
.endif

test4:
.if (${VAR1:s/a$/X/g} != "abc aba bac")
	@echo ODEMKERROR: :s variable modifier - expected: abc aba bac 
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR1:s/a$/X/g}
.else
	@echo ODEMKPASS
.endif

test5:
.if (${VAR1:s/c$/X/g} != "abc aba baX")
	@echo ODEMKERROR: :s variable modifier - expected: abc aba baX 
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR1:s/c$/X/g}
.else
	@echo ODEMKPASS
.endif

test6:
.if (${VAR1:s/ab/&X/g} != "abXc abXa bac")
	@echo ODEMKERROR: :s variable modifier - expected: abXc abXa bac 
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR1:s/ab/&X/g}
.else
	@echo ODEMKPASS
.endif

test7:
.if (${VAR2:s/\//X/g} != "abXcd")
	@echo ODEMKERROR: :s variable modifier - expected: abXcd
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR2:s/\//X/g}
.else
	@echo ODEMKPASS
.endif

test8:
.if (${VAR3:s/\&/X/g} != "abXcd")
	@echo ODEMKERROR: :s variable modifier - expected: abXcd
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR3:s/\&/X/g}
.else
	@echo ODEMKPASS
.endif

test9:
.if (${VAR4:s/\^/X/g} != "abXcd")
	@echo ODEMKERROR: :s variable modifier - expected: abXcd
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR4:s/\^/X/g}
.else
	@echo ODEMKPASS
.endif

test10:
.if (${VAR5:s/\$/X/g} != "abXcd")
	@echo ODEMKERROR: :s variable modifier - expected: abXcd
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR5:s/\$/X/g}
.else
	@echo ODEMKPASS
.endif

test11:
.if (${VAR1:s/c/X/f} != "abX aba bac")
	@echo ODEMKERROR: :s variable modifier - expected: abX aba bac 
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR1:s/c/X/f}
.else
	@echo ODEMKPASS
.endif

test12:
.if (${VAR1:s/c/X/w} != "abX aba baX")
	@echo ODEMKERROR: :s variable modifier - expected: abX aba baX 
	@echo ODEMKERROR: :s variable modifier - actual: ${VAR1:s/c/X/w}
.else
	@echo ODEMKPASS
.endif
