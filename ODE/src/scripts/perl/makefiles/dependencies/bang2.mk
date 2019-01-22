#####################################################
# bang2.mk - makefile to test target dependencies
#            when "!" is used.
#            Though the target all appears twice,
#            only the commands associated with the
#            first are used but the sources accumulate
#
# run as:
#   mk -f bang2.mk
#
# used by the perl script MkTargDepTest.pm
######################################################
all ! t1 t2
	@echo making all

all ! t3 t4
	@echo making second all

t1 ! 
	@echo making t1 

t2 ! 
	@echo making t2

t3 !
	@echo making t3

t4 !
	@echo making t4

