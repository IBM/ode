####################################################
# flag_b.mk - makfile to test commandline 
#             flag -b
#
# run as:
#    mk -f flag_b.mk
# if the current directory has directories "all", "dir1" 
# and are uptodate, nothing will be executed
#
# used by the perl script MkCmdFlagTest.pm
####################################################
all : t1.c dir1
	@echo making all

t1.c:
	@echo making t1.c
