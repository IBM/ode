################################################
#rtcond_for_error2.mk -  makefile to test runtime
#                 .rfor error
#
# used by the perl script MkRTConditionalTest.pm
################################################


# No .rendfor
error2:    
	.rfor ALPHA = A B C
	  @echo ${ALPHA}

