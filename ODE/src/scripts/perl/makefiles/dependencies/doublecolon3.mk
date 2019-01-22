########################################################
# doublecolon3.mk - makefile to test target dependencies
#                   when "::" is used 
# target t1.c is repeated multiple times
# "t1.c :: t2.c" should be executed only if t2.c is up to date
# "t1.c ::" should be executed always
#
# run as:
#  mk -f doublecolon3.mk
#
# used by the perl script MkTargDepTest.pm
########################################################
t.o :: t1.c 
	@echo making t.o
	@echo " " > t.o

t1.c :: t2.c
	@echo making t1.c
  
t1.c ::
	@echo making t1.c
	@echo " " > t1.c

t2.c :
	@echo making t2.c
	@echo " " > t2.c
