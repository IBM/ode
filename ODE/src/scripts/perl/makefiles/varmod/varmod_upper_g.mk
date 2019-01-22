###################################################################
# varmod_upper_g.mk - makefile to test the :G variable modifier
#
# run as
#   mk -f varmod_upper_c.mk VAR1=<string1> VAR2=<string2>
# string1: contains the paths to be tested
# string2: the output of :G modifier
#
# used by the perl script MkVarModTest.pm
###################################################################

all: 
.if (${${VAR1}:L:G:u} == ${VAR2:u})
	@echo ODEMKPASS
.else
	@echo ODEMKERROR
.endif

error:
.if (${${VAR1}:G} == ${VAR2})
	@echo ODEMKPASS
.else
	@echo ODEMKERROR
.endif

