##########################################################
# colon_bang_error.mk - makefile to test the repetition of
#                       a target with : and ! operators             
#
# run as:
#   mk -f colon_bang_error.mk
#
# used by the perl script MkTargDepTest.pm
#########################################################
t.o: t1.c
	@echo making t.o with a colon
	@echo " " > t.o

t.o ! t1.c
	@echo making t.o with a bang
	@echo " " > t.o

t1.c!
	@echo making t1.c 
	@echo " " > t1.c
