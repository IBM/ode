####################################################
# dirs.mk - makfile to test special source .DIRS
#
# run as:
#    mk -f dirs.mk
# all is a directory in the current directory
# t1.c is a file in the current directory
#
# used by the perl script MkSpecSrcTest.pm
####################################################
all : t1.c .DIRS
	@echo making all

