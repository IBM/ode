#############################################
# varmod_upper_q1.mk - makefile to test the
# :Q variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

VAR1=foo
VAR2=foo moo boo zoo ba zzz
VAR3=


all: test1 test2 test3 test4 test5

test1:
.if (!${VAR1:Q+} == "foo")
	@echo ODEMKERROR: :Q variable modifier - expected: foo
	@echo ODEMKERROR: :Q variable modifier - actual: ${VAR1:Q+}
.else
	@echo ODEMKPASS
.endif

test2:
.if (!${VAR1:Q-} == "foo")
	@echo ODEMKERROR: :Q variable modifier - expected: foo
	@echo ODEMKERROR: :Q variable modifier - actual: ${VAR1:Q-}
.else
	@echo ODEMKPASS
.endif

test3:
.if (!${VAR2:Q+} == "ba boo foo moo zoo zzz")
	@echo ODEMKERROR: :Q variable modifier - expected: ba boo foo moo zoo zzz
	@echo ODEMKERROR: :Q variable modifier - actual: ${VAR2:Q+}
.else
	@echo ODEMKPASS
.endif

test4:
.if (!${VAR2:Q-} == "zzz zoo moo foo boo ba")
	@echo ODEMKERROR: :Q variable modifier - expected: zzz zoo moo foo boo ba
	@echo ODEMKERROR: :Q variable modifier - actual: ${VAR2:Q-}
.else
	@echo ODEMKPASS
.endif

test5:
.if (!${VAR3:Q+} == "")
	@echo ODEMKERROR: :Q variable modifier - expected: zzz zoo moo foo boo ba
	@echo ODEMKERROR: :Q variable modifier - actual: ${VAR2:Q-}
.else
	@echo ODEMKPASS
.endif

test6:
	${VAR2:Q}
