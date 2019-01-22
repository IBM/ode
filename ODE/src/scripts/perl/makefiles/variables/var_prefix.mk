#####################################################
# var_prefix.mk - makefile to test .PREFIX
#
# run as
#   mk -f var_prefix.mk
#
# Used by the perl script MkImpVarTest.pm
#####################################################
test.c: src.o test.java test.c.java  

src.o: 
	@echo ${.PREFIX}

test.java: test.*
	@echo first $*

#Testing the use of .PREFIX in the dependency line
test.c.java: $${.PREFIX}.h.java
	@echo ${*}

test.*:
	@echo second ${.PREFIX}

test.c.h.java:
	@echo ${.PREFIX}
