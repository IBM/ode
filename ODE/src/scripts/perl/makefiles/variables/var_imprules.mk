##################################################################
# var_imprules.mk - makefile to test the debug options -dp and -ds
#
# run as
#    mk -f var_impruless.mk -dps
#
# Used by the perl script MkImpVarTest.pm
##################################################################

.SUFFIXES: .a .b
      
all : foo.a  foo.x

.b.a :
	@echo foo.a

%.x : %.y
	@echo foo.x

foo.b  foo.y :
	@echo ${.TARGET}  
