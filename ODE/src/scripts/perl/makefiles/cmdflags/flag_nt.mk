############################################
# flag_nt.mk - makefile to test commandline
#              flags -n and -t
#
# run as:
#    mk -f flag_nt.mk [-n] [-t]
#
# If -t is used, files t.o, t1.c, t2.c should
# exist in the current directory
#
# used by the perl script MkCmdFlagtest.pm
############################################
t.o : t1.c t2.c
	@echo making t.o

t1.c:
	@echo making t1.c

t2.c:
	@echo making t2.c
