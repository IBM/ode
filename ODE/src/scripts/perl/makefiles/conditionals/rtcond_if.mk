################################################
#rtcond_if.mk -  makefile to test runtime conditional
#              if
#
# used by the perl script MkRTConditionalTest.pm
################################################
all: test1 test2 test3 test4 test5 
VAR1=var1					
VAR2="this is var2"
VAR3=

test1:
	.rif !${VAR1}
	@echo ODEMKERROR: Test 1 for .rif failed
	.relif !${VAR2}
	@echo ODEMKERROR: Test 1 for .rif failed
	.relif ${VAR3}
	@echo ODEMKERROR: Test 1 for .rif failed
	.relse
	@echo ODEMKPASS
	.rendif

test2:
	.rif ((${VAR1}!="var1") || (${VAR3}==" "))
	@echo ODEMKERROR: Test 2 for .rif failed
	.relif ((${VAR1}!="var1") || (${VAR3}==""))
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 2 for .rif failed
	.rendif

test3:
	.rif ((${VAR1}=="var1") && (${VAR2}=="yyy"))
	@echo ODEMKERROR: Test 3 for .rif failed
	.relif ((${VAR1}=="var1") && (${VAR2}=="this is var2"))
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 3 for .rif failed
	.rendif

test4:
	.rif (!${VAR1} && ${VAR2})
	@echo ODEMKERROR: Test 4 for .rif failed
	.relif (${VAR3} || ${VAR4})
	@echo ODEMKERROR: Test 4 for .rif failed
	.relse
	@echo ODEMKPASS
	.rendif

#Test for .rif depth
test5:
	.rif (${VAR1}=="var1")
	.rif (${VAR2}=="this is var2")
	.rif (${VAR3}!=" ")
	.rif (!${VAR4})
	.rif (${VAR1}!=${VAR2})
	.rif (${VAR3}==${VAR4})
	@echo ODEMKPASS
	.relse
	@echo ODEMKERROR: Test 5 for .rif failed
	.rendif
	.relse
	@echo ODEMKERROR: Test 5 for .rif failed
	.rendif
	.relse
	@echo ODEMKERROR: Test 5 for .rif failed
	.rendif
	.relse
	@echo ODEMKERROR: Test 5 for .rif failed
	.rendif
	.relse
	@echo ODEMKERROR: Test 5 for .rif failed
	.rendif
	.relse
	@echo ODEMKERROR: Test 5 for .rif failed
	.rendif

