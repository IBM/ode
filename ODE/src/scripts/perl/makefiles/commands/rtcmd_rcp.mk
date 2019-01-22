#########################################################
# rtcmd_rcp.mk - makefile to test runtime cmd  .rcp 
#
# run as:
#    mk -f rtcmd_rcp.mk
#
# Used by the perl script MkRTMkdepCmdTest.pm
#########################################################

.ORDER : init targ1 targ2 clean targ3
all : init targ1 targ2 clean targ3 
FILES=file? subdir1/file* subdir2/*
TEXTVAR=ODEisGreat

init :
	@echo initialization
	${TEXTVAR:Afile1}
	${subdir1:L:C}
	${subdir2:L:C}
	${TEXTVAR:Asubdir1/file3}
 

targ1 :
	.rcp file1 file2
	.rcp file1 file2 subdir1
	.rcp subdir1/file3 ./
	.rcp ${file*:P:T:Q+} subdir2

targ2 targ3 : 
	@echo ${FILES:p:T:Q+}

clean :
	@echo cleanup
	.rrm ${file*:P:T:Q+}
	.rrm subdir1/file1 subdir1/file2 subdir1/file3
	.rrm subdir2/file1 subdir2/file2 subdir2/file3
	.rrm subdir1 subdir2