###################################################################
# varmod_upper_c.mk - makefile to test the :C variable modifier
#
# run as
#   mk -f varmod_upper_c.mk VAR1=<string1> VAR2=<string2> VAR3=<string3>
# string1: a list of directories
# string2: directories in string1 which cannot be created
# string3: directories in string1 which can actually be created
#
# used by the perl script MkVarModTest.pm
###################################################################

all: test1 test2 test3 test4

test1:
.if (${VAR1:C} == ${VAR2})
	@echo ODEMKPASS
.else
	@echo ODEMKERROR
.endif

test2:
.if (${VAR1:XD} == ${VAR3})
	@echo ODEMKPASS
.else
	@echo ODEMKERROR
.endif

test3:
.if (${VAR2:XD} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR
.endif

test4:
.if (${VAR1:C-} == "")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR
.endif

.ORDER: test1 test2 test3 test4
