########################################################
# doublecolon2.mk - makefile to test target dependencies
#                   when "::" is used 
#  t1.c is built only if t2.c is recently modified
#
# run as:
#  mk -f doublecolon2.mk
#
# used by the perl script MkTargDepTest.pm
########################################################
t.o :: t1.c 
	@echo making t.o
	@echo " " > t.o

t1.c :: t2.c
	@echo making t1.c
	@echo " " > t1.c
  
t2.c :
	@echo making t2.c
	@echo " " > t2.c
