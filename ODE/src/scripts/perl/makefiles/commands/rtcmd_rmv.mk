#########################################################
# rtcmd_rmv.mk - makefile to test runtime cmd  .rmv 
#
# run as:
#    mk -f rtcmd_rmv.mk
#
# Used by the perl script MkRTMkdepCmdTest.pm
#########################################################

.ORDER : init targ1 targ2 clean targ3		    
all : init targ1 targ2 clean targ3
FILES=file? subdir1/file* subdir2/*
FILES1=file?
FILES2=subdir1/file*
FILES3=subdir2/*
TEXTVAR=ODEisGreat

init :
	@echo initialization
	${TEXTVAR:Afile1}
	${TEXTVAR:Afile3}
	${subdir1:L:C}
	${subdir2:L:C}
	${TEXTVAR:Asubdir1/file4}
	${TEXTVAR:Asubdir1/file5}
 

targ1 :
	.rmv file1 file2
	.rmv file2 file3 subdir1
	.rmv subdir1/file5 ./
	.rmv ${subdir1/file*:P:T:S|^file|subdir1/file|f:Q+} subdir2

targ2 targ3 : 
	@echo ${FILES1:p:T:Q+}
	@echo ${FILES2:p:T:Q+}
	@echo ${FILES3:p:T:Q+}

clean :
	@echo cleanup
	.rrm ${file*:P:T:Q+}
	.rrm subdir2/file4 subdir2/file2 subdir2/file3
	.rrm subdir1 subdir2