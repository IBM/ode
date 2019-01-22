####################################################
# bang1.mk - makefile to test target dependencies
#            when "!" is used 
#
# run as:
#  mk -f bang1.mk
#
# used by the perl script MkTargDepTest.pm
####################################################
t.o ! t1.c t2.c
	@echo making t.o
	@echo " " > t.o

t1.c !
	@echo making t1.c
	@echo " " > t1.c

t2.c !
	@echo making t2.c
	@echo " " > t2.c
