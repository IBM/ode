#############################################
# varmod_lower_x_upper_s.mk - makefile to test the
# :xS variable modifier
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
VAR9="a 	z"

all: test1 test2 test3 test4 test5 test6 test7 test8 test9 test10 test11 \
     test12 test13 test14 test15 test16 test17 test18 test19 test20

test1:
.if (${VAR1:xS/a/X/} != "Xba abc bac")
	@echo ODEMKERROR: :xS variable modifier - expected: Xba abc bac 
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR1:xS/a/X/}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${VAR1:xS/a/X/g} != "XbX Xbc bXc")
	@echo ODEMKERROR: :xS variable modifier - expected: XbX Xbc bXc 
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR1:xS/a/X/g}
.else
	@echo ODEMKPASS
.endif

test3:
.if (${VAR1:xS/a/X/f} != "Xba Xbc bXc")
	@echo ODEMKERROR: :xS variable modifier - expected: Xba Xbc bXc 
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR1:xS/a/X/f}
.else
	@echo ODEMKPASS
.endif

test4:
.if (${VAR1:xS/a/X/w} != "XbX abc bac")
	@echo ODEMKERROR: :xS variable modifier - expected: XbX abc bac 
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR1:xS/a/X/w}
.else
	@echo ODEMKPASS
.endif

test5:   # $ escaped
.if (${VAR1:xS/c\$/X/g} != "aba abX baX")
	@echo ODEMKERROR: :xS variable modifier - expected: aba abX baX 
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR1:xS/c\$/X/g}
.else
	@echo ODEMKPASS
.endif

test6:   # $ begins variable
.if (${VAR1:xS/${VAR2}/XYZ/g} != "aXYZ abc XYZc")
	@echo ODEMKERROR: :xS variable modifier - expected: aXYZ abc XYZc
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR1:xS/${VAR2}/XYZ/g}
.else
	@echo ODEMKPASS
.endif

test7:    # ampersand substitution
.if (${VAR1:xS/ab/&X/g} != "abXa abXc bac")
	@echo ODEMKERROR: :xS variable modifier - expected: abXa abXc bac 
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR1:xS/ab/&X/g}
.else
	@echo ODEMKPASS
.endif

test8:    # ampersand escaped
.if (${VAR3:xS/\&/X/g} != "abXcd")
	@echo ODEMKERROR: :xS variable modifier - expected: abXcd
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR3:xS/\&/X/g}
.else
	@echo ODEMKPASS
.endif

test9:    # escaped delimiter
.if (${VAR4:xS/\//X/g} != "abXcd")
	@echo ODEMKERROR: :xS variable modifier - expected: abXcd
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR4:xS/\//X/g}
.else
	@echo ODEMKPASS
.endif

test10:   # ignore case 
.if (${VAR4:xS/C/X/i} != "ab/Xd")
	@echo ODEMKERROR: :xS variable modifier - expected: ab/Xd
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR4:xS/C/X/i}
.else
	@echo ODEMKPASS
.endif

test11:   # ignore case conditionally
	.rif (${CASE_INSENSITIVE_FILE_NAMES})
	.rif (${VAR4:xS/C/X/c} != "ab/Xd")
	@echo ODEMKERROR: :xS variable modifier - expected: ab/Xd
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR4:xS/C/X/c}
	.relse
	@echo ODEMKPASS
	.rendif
	.relse
	.rif (${VAR4:xS/C/X/c} != "ab/cd")
	@echo ODEMKERROR: :xS variable modifier - expected: ab/cd
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR4:xS/C/X/c}
	.relse
	@echo ODEMKPASS
	.rendif
	.rendif

test12:
.if (${VAR1:xS/\(a\).*\1/X/g} != "X abc bac")
	@echo ODEMKERROR: :xS variable modifier - expected: X abc bac
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR1:xS/\(a\).*\1/X/g}
.else
	@echo ODEMKPASS
.endif

test13:
.if (${VAR5:xS/\(bc\)\{1,2\}/X/} != "aXbca")
	@echo ODEMKERROR: :xS variable modifier - expected: aXbca
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR5:xS/\(bc\)\{1,2\}/X/}
.else
	@echo ODEMKPASS
.endif

test14:
.if (${VAR5:xS/(bc){1,2}/X/e} != "aXbca")
	@echo ODEMKERROR: :xS variable modifier - expected: aXbca
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR5:xS/(bc){1,2}/X/e}
.else
	@echo ODEMKPASS
.endif

test15:
.if (${VAR6:xS/(N[[:digit:]]*|X[[:xdigit:]]*|AN[[:alnum:]]*)/X/fe} != "Xc3p Xp X")
	@echo ODEMKERROR: :xS variable modifier - expected: Xc3p Xp X
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR6:xS/(N[[:digit:]]*|X[[:xdigit:]]*|AN[[:alnum:]]*)/X/fe}
.else
	@echo ODEMKPASS
.endif

test16:
.if (${VAR7:xS/[[:alpha:]]+@[[:upper:]]+2[[:lower:]]+/X/e} != "*X4")
	@echo ODEMKERROR: :xS variable modifier - expected: *X4
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR7:xS/[[:alpha:]]+@[[:upper:]]+2[[:lower:]]+/X/e}
.else
	@echo ODEMKPASS
.endif

test17:
.if (${VAR7:xS/[O-U]/X/gie} != "*BiXXY@XXXEX2lXweX4")
	@echo ODEMKERROR: :xS variable modifier - expected: *BiXXY@XXXEX2lXweX4
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR7:xS/[O-U]/X/gie}
.else
	@echo ODEMKPASS
.endif

test18:
.if (${VAR8:xS/[[:punct:]]/X/g} != "aXXXXz")
	@echo ODEMKERROR: :xS variable modifier - expected: aXXXXz
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR8:xS/[[:punct:]]/X/g}
.else
	@echo ODEMKPASS
.endif

test19:
.if (${VAR9:xS/[[:blank:]]/X/g} != "aXXz")
	@echo ODEMKERROR: :xS variable modifier - expected: aXXz
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR9:xS/[[:blank:]]/X/g}
.else
	@echo ODEMKPASS
.endif

test20:
.if (${VAR9:xS/[[:space:]]/X/g} != "aXXXz")
	@echo ODEMKERROR: :xS variable modifier - expected: aXXXz
	@echo ODEMKERROR: :xS variable modifier - actual: ${VAR9:xS/[[:space:]]/X/g}
.else
	@echo ODEMKPASS
.endif
