##############################################################
#var_assign.mk - makefile to test variable assignments
#
# used by the perl script MkVarAssgnTest.pm
#
# To be run as
# On UNIX platforms
#     mk -f var_assign.mk -DUNIX
# Otherwise
#     mk -f var_assign.mk 
##############################################################

all:test1 test2 test3 test4 test5 test6 test7 test8 test9 test10 test11 test12

#Testing operator =
VAR1=var1
VAR2=   this is \tvar2
v=tmp
#the first conditional fails as {} are missing around VAR1 in $VAR1
#VAR2 is compared to a string with no quotes in the second conditional
#should trim the space before the value for VAR2
#v is a variable with a single letter, can use $v to get its value
test1:
.if ($VAR1=="var1")
	@echo ODEMKERROR: Test for operator = failed
.elif ((${VAR1}=="var1") && (${VAR2}==this is \tvar2) && ($v==tmp))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for operator = failed
.endif

#Testing operator ?= 
#Note the use of () instead of {} in the third conditional
VAR3?=${VAR1}
#VAR3 should not be overwritten
VAR3?=${VAR2}
VAR3A?=?
test2:
.if (${VAR3}=="this is \tvar2")
	@echo ODEMKERROR: Test for operator ?= failed 
.elif (${VAR3A}!="?")
	@echo ODEMKERROR: Test for operator ?= failed 
.elif (($(VAR3)=="var1") && ($(VAR3A)=="?"))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for operator ?= failed 
.endif

#Testing operator +=
VAR4=var4
#Note the space after += which should be trimmed before the operation
VAR4+= ${VAR1}
test3:
.if (${VAR4}=="var4 var1")
	@echo ODEMKPASS 
.else
	@echo ODEMKERROR: Test for operator += failed
.endif

#Testing operator :=
VAR5=var5
#VAR6 does not change after this assignment even if VAR5 changes
VAR6:=${VAR5}
VAR6A:=${VAR5}
#VAR7 changes if VAR5 changes
VAR7=${VAR5}
VAR5=varx
#Making sure that VAR6A is overwritten
VAR6A:=${VAR5}
test4:
.if ((${VAR6}=="var5") && (${VAR6A}=="varx") && (${VAR7}=="varx"))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for operator := failed
.endif

#Testing operator !=
VAR8!=echo this is var8
test5:
.if (${VAR8}=="this is var8")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for operator != failed
.endif

#Testing operator %=
VAR9 %= this is var9
#To make sure that VAR9 can be overwritten
VAR9 %= this is var9A
.if defined(UNIX)
VAR10=${DUMMY:!echo $$VAR9!}
.else
VAR10!=echo %VAR9%
.endif
test6:
.if (${VAR10}=="this is var9A")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for operator %= failed
.endif

#Testing the difference between ${VAR1} and VAR1
#Note that VAR11 gets VAR1 but not ${VAR1} which is var1
VAR11=VAR1
test7:
.if (${VAR11}=="var1")
	@echo ODEMKERROR: Test for operator = failed
.elif (${VAR11}=="VAR1")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for operator = failed
.endif

#Testing operator = with $ as a part of the variable name
$VAR12=var12
test8:
.if (${$VAR12}=="var12")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for operator = failed
.endif

#Testing with more than one operator = in a single line
VAR13=VAR13A=var13
test9:
.if (${VAR13}=="var13")
	@echo ODEMKERROR: Test for operator = failed
.elif (${VAR13}=="VAR13A=var13")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for operator = failed
.endif

# Testing use of more than one variable on LHS
VAR14 VAR15=var14var15
test10:
.if (${VAR14 VAR15}=="var14var15")
	@echo ODEMKERROR: Test for operator = failed
.elif ((${VAR14}=="var14var15") && (${VAR15}=="var14var15"))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for operator = failed
.endif

"VAR16 VAR17"=var16var17
# Testing the use of more than one variable on LHS in quotes
test11:
.if ((${VAR16}=="var16var17") || (${VAR17}=="var16var17"))
	@echo ODEMKERROR: Test for operator = failed
.elif (${"VAR16 VAR17"}=="var16var17")
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for operator = failed
.endif

"VAR18 VAR19" VAR20?=var20
"VAR18 VAR19"+=var21
# Testing the combination of test10 and test11
test12:
.if (${"VAR18 VAR19" VAR20}=="var20")
	@echo ODEMKERROR: Test for operator = failed
.elif ((${"VAR18 VAR19"}=="var20 var21") && (${VAR20}=="var20"))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test for operator = failed
.endif

