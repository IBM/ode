############################################
# flag_k.mk - makefile to test commandline
#              flag -k
#
# run as:
#    mk -f flag_k.mk -k
#
#
# used by the perl script MkCmdFlagTest.pm
############################################
all : t1 t2
	@echo making all

t1:
	crap

t2:
	@echo making t2

t3:
	@echo making t3
