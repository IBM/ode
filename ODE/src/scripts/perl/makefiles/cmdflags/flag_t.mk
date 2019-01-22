###########################################
# flag_t.mk - makefile to test commandline
#             flag -t
#
# run as:
#    mk -f flag_t.mk -t
#
# used by the perl script MkCmdFlagTest.pm
###########################################
t.o : .SPECTARG
	@echo making t.o

t1.c:
	@echo making t1.c

