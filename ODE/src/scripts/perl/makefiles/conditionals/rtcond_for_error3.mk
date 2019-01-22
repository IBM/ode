################################################
#rtcond_for_error2.mk -  makefile to test runtime 
#                 .rfor error
#
# used by the perl script MkRTConditionalTest.pm
################################################


# No arguments
error3:     
	.rfor ALPHA =
	  @echo ${ALPHA}
	.rendfor
