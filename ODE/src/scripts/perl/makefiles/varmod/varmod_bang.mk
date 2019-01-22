#############################################
# varmod_bang.mk - makefile to test the
# !command! variable modifier
#
# used by the perl script MkVarModTest.pm
#############################################

CMD1=echo two
CMD2=echo $$NOTANENVVAR

all: test1 test2 test3 test4

test1:
.if (${VAR1:!echo one!} != "one")
	@echo ODEMKERROR: = variable modifier - expected: one
	@echo ODEMKERROR: = variable modifier - actual: ${VAR1:!echo one!}
.else
	@echo ODEMKPASS
.endif

test2:
.if (${VAR1:!${CMD1}!} != "two")
	@echo ODEMKERROR: = variable modifier - expected: two
	@echo ODEMKERROR: = variable modifier - actual: ${VAR1:!${CMD1}!}
.else
	@echo ODEMKPASS
.endif

test3:
.if defined( UNIX )
.if (${VAR1:!${CMD2}!} != "")
	@echo ODEMKERROR: = variable modifier - expected: 
	@echo ODEMKERROR: = variable modifier - actual: ${VAR1:!${CMD2}!}
.else
	@echo ODEMKPASS
.endif
.else # not Unix
.if (${VAR1:!${CMD2}!} != "$$NOTANENVVAR")
	@echo ODEMKERROR: = variable modifier - expected: $$NOTANENVVAR
	@echo ODEMKERROR: = variable modifier - actual: ${VAR1:!${CMD2}!}
.else
	@echo ODEMKPASS
.endif
.endif

# this test runs 'echo ODEMKPASS' which also gets echoed to stdout, where
# the 'ODEMKPASS' will be found and counted as the success message for test4.
test4:
	.rif (${VAR1:!echo ODEMKPASS!e} != "ODEMKPASS")
	@echo ODEMKERROR: = variable modifier - expected: ODEMKPASS
	.rendif


# Run this test to check if 'notacommand' is caught as an illegal command.
# The .rif should be treated as a malformed conditional, and mk should
# terminate. No ODEMKERROR or ODEMKPASS messages should be output.
test5:
	.rif (${VAR1:!notacommand!} != "")
	@echo ODEMKERROR: = variable modifier - expected:
	@echo ODEMKERROR: = variable modifier - actual: ${VAR1:!notacommand!}
	.relse
	@echo ODEMKPASS
	.rendif
