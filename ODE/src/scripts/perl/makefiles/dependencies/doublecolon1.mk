########################################################
# doublecolon1.mk - makefile to test target dependencies
#                   when "::" is used 
#  t1.c and t2.c are built always even if up to date
#
# run as:
#  mk -f doublecolon1.mk
#
# used by the perl script MkTargDepTest.pm
########################################################
t.o :: t1.c t2.c
	@echo making t.c
	@echo " " > t.o

t1.c ::
	@echo making t1.c
	@echo " " > t1.c
  
t2.c ::
	@echo making t2.c
	@echo " " > t2.c
