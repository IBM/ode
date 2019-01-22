#############################################
# varmod_lower_x_lower_s.mk - makefile to test the
# :xs variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################
# Note that the [..] and [==] forms are not tested, since they require
# locale features that English does not have.

VAR1=aba abc bac
VAR2=ba
VAR3=ab&cd
VAR4=ab/cd
VAR5=abcbcbca
VAR6=N90c3p X90c3p AN90c3p
VAR7=*BiPpY@UPPER2lower4 
VAR8=a.,:;z
VAR9=a 	z  # a blank cntrl-i(tab) cntrl-k(vertical tab) cntrl-e(ENQ) z
VAR10=a z   # a blank cntrl-e(ENQ) z

all: test1 test2 test3 test4 test5 test6 test7 test8 test9 test10 test11 \
     test12 test13 test14 test15 test16 test17 test18 test19 test20 test21 \
     test22 test23

test1:
.if (${VAR1:xs/a/X/} != "Xba abc bac")
	@echo ODEMKERROR: :xs variable modifier - expected: Xba abc bac 
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR1:xs/a/X/}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${VAR1:xs/a/X/g} != "XbX Xbc bXc")
	@echo ODEMKERROR: :xs variable modifier - expected: XbX Xbc bXc 
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR1:xs/a/X/g}
.else
	@echo ODEMKPASS
.endif

test3:
.if (${VAR1:xs/a/X/f} != "Xba abc bac")
	@echo ODEMKERROR: :xs variable modifier - expected: Xba abc bac 
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR1:xs/a/X/f}
.else
	@echo ODEMKPASS
.endif

test4:
.if (${VAR1:xs/a/X/w} != "XbX Xbc bXc")
	@echo ODEMKERROR: :xs variable modifier - expected: XbX Xbc bXc 
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR1:xs/a/X/w}
.else
	@echo ODEMKPASS
.endif

test5:   # $ escaped
.if (${VAR1:xs/c\$/X/g} != "aba abc baX")
	@echo ODEMKERROR: :xs variable modifier - expected: aba abc baX 
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR1:xs/c\$/X/g}
.else
	@echo ODEMKPASS
.endif

test6:   # $ begins variable
.if (${VAR1:xs/${VAR2}/XYZ/g} != "aXYZ abc XYZc")
	@echo ODEMKERROR: :xs variable modifier - expected: aXYZ abc XYZc
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR1:xs/${VAR2}/XYZ/g}
.else
	@echo ODEMKPASS
.endif

test7:    # ampersand substitution
.if (${VAR1:xs/ab/&X/g} != "abXa abXc bac")
	@echo ODEMKERROR: :xs variable modifier - expected: abXa abXc bac 
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR1:xs/ab/&X/g}
.else
	@echo ODEMKPASS
.endif

test8:    # ampersand escaped
.if (${VAR3:xs/\&/X/g} != "abXcd")
	@echo ODEMKERROR: :xs variable modifier - expected: abXcd
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR3:xs/\&/X/g}
.else
	@echo ODEMKPASS
.endif

test9:    # escaped delimitor
.if (${VAR4:xs/\//X/g} != "abXcd")
	@echo ODEMKERROR: :xs variable modifier - expected: abXcd
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR4:xs/\//X/g}
.else
	@echo ODEMKPASS
.endif

test10:   # ignore case
.if (${VAR4:xs/C/X/i} != "ab/Xd")
	@echo ODEMKERROR: :xs variable modifier - expected: ab/Xd
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR4:xs/C/X/i}
.else
	@echo ODEMKPASS
.endif

test11:   # ignore case conditionally
	.rif (${CASE_INSENSITIVE_FILE_NAMES})
	.rif (${VAR4:xs/C/X/c} != "ab/Xd")
	@echo ODEMKERROR: :xs variable modifier - expected: ab/Xd
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR4:xs/C/X/c}
	.relse
	@echo ODEMKPASS
	.rendif
	.relse
	.rif (${VAR4:xs/C/X/c} != "ab/cd")
	@echo ODEMKERROR: :xs variable modifier - expected: ab/cd
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR4:xs/C/X/c}
	.relse
	@echo ODEMKPASS
	.rendif
	.rendif

test12:
.if (${VAR1:xs/\(a\).*\1/X/g} != "Xc")
	@echo ODEMKERROR: :xs variable modifier - expected: Xc
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR1:xs/\(a\).*\1/X/g}
.else
	@echo ODEMKPASS
.endif

test13:
.if (${VAR5:xs/\(bc\)\{1,2\}/X/} != "aXbca")
	@echo ODEMKERROR: :xs variable modifier - expected: aXbca
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR5:xs/\(bc\)\{1,2\}/X/}
.else
	@echo ODEMKPASS
.endif

test14:
.if (${VAR5:xs/(bc){1,2}/X/e} != "aXbca")
	@echo ODEMKERROR: :xs variable modifier - expected: aXbca
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR5:xs/(bc){1,2}/X/e}
.else
	@echo ODEMKPASS
.endif

test15:
.if (${VAR6:xs/(N[[:digit:]]*|X[[:xdigit:]]*|AN[[:alnum:]]*)/X/we} != "Xc3p Xp X")
	@echo ODEMKERROR: :xS variable modifier - expected: Xc3p Xp X
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR6:xs/(N[[:digit:]]*|X[[:xdigit:]]*|AN[[:alnum:]]*)/X/we}
.else
	@echo ODEMKPASS
.endif

test16:
.if (${VAR7:xs/[[:alpha:]]+@[[:upper:]]+2[[:lower:]]+/X/e} != "*X4")
	@echo ODEMKERROR: :xs variable modifier - expected: *X4
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR7:xs/[[:alpha:]]+@[[:upper:]]+2[[:lower:]]+/X/e}
.else
	@echo ODEMKPASS
.endif

test17:
.if (${VAR7:xs/[O-U]/X/gie} != "*BiXXY@XXXEX2lXweX4")
	@echo ODEMKERROR: :xs variable modifier - expected: *BiXXY@XXXEX2lXweX4
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR7:xs/[O-U]/X/gie}
.else
	@echo ODEMKPASS
.endif

test18:
.if (${VAR8:xs/[[:punct:]]/X/g} != "aXXXXz")
	@echo ODEMKERROR: :xs variable modifier - expected: aXXXXz
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR8:xs/[[:punct:]]/X/g}
.else
	@echo ODEMKPASS
.endif

test19:
.if (${VAR9:xs/[[:blank:]]/X/g} != "aXXz")
	@echo ODEMKERROR: :xs variable modifier - expected: aXXz
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR9:xs/[[:blank:]]/X/g}
.else
	@echo ODEMKPASS
.endif

test20:
.if (${VAR9:xs/[[:space:]]/X/g} != "aXXXz")
	@echo ODEMKERROR: :xs variable modifier - expected: aXXXz
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR9:xs/[[:space:]]/X/g}
.else
	@echo ODEMKPASS
.endif

test21:
.if (${VAR10:xs/[[:graph:]]/X/g} != "X X")
	@echo ODEMKERROR: :xs variable modifier - expected: X X
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR10:xs/[[:graph:]]/X/g}
.else
	@echo ODEMKPASS
.endif

test22:
.if (${VAR10:xs/[[:print:]]/X/g} != "XXX")
	@echo ODEMKERROR: :xs variable modifier - expected: XXX
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR10:xs/[[:print:]]/X/g}
.else
	@echo ODEMKPASS
.endif

test23:
.if (${VAR9:xs/[[:cntrl:]]/X/g} != "a XXXz")
	@echo ODEMKERROR: :xs variable modifier - expected: a XXXz
	@echo ODEMKERROR: :xs variable modifier - actual: ${VAR9:xs/[[:cntrl:]]/X/g}
.else
	@echo ODEMKPASS
.endif
