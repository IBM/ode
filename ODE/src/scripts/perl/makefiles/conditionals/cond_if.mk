################################################
#cond_if.mk -  makefile to test conditional
#              if
#
# used by the perl script MkConditionalTest.pm
################################################
all: test1 test2 test3 test4 test5 
VAR1=var1					
VAR2="this is var2"
VAR3=

test1:
.if !${VAR1}
	@echo ODEMKERROR: Test 1 for .if failed
.elif !${VAR2}
	@echo ODEMKERROR: Test 1 for .if failed
.elif ${VAR3}
	@echo ODEMKERROR: Test 1 for .if failed
.else
	@echo ODEMKPASS
.endif

test2:
.if ((${VAR1}!="var1") || (${VAR3}==" "))
	@echo ODEMKERROR: Test 2 for .if failed
.elif ((${VAR1}!="var1") || (${VAR3}==""))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 2 for .if failed
.endif

test3:
.if ((${VAR1}=="var1") && (${VAR2}=="yyy"))
	@echo ODEMKERROR: Test 3 for .if failed
.elif ((${VAR1}=="var1") && (${VAR2}=="this is var2"))
	@echo ODEMKPASS
.else
	@echo ODEMKERROR: Test 3 for .if failed
.endif

test4:
.if (!${VAR1} && ${VAR2})
	@echo ODEMKERROR: Test 4 for .if failed
.elif (${VAR3} || ${VAR4})
	@echo ODEMKERROR: Test 4 for .if failed
.else
	@echo ODEMKPASS
.endif

#Test for .if depth
result=ODEMKERROR: Test 5 for .if failed
.if (${VAR1}=="var1")
  .if (${VAR2}=="this is var2")
    .if (${VAR3}!=" ")
      .if (!${VAR4})
        .if (${VAR1}!=${VAR2})
          .if (${VAR3}==${VAR4})
            result=ODEMKPASS
          .endif
        .endif
      .endif
    .endif
  .endif
.endif
test5:
	@echo ${result}

