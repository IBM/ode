################################################
#rtcond_for_error1.mk -  makefile to test runtime 
#                       .rfor errors
#                
# used by the perl script MkRTConditionalTest.pm
################################################

# No equal sign
error1:
	.rfor ALPHA A B C 
	  @echo ALPHA
	.rendfor

