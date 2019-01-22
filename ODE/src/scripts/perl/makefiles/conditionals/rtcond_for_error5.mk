################################################
#rtcond_for_error5.mk -  makefile to test runtime
#                 .rfor error
#
# used by the perl script MkRTConditionalTest.pm
################################################

# Too many rendfor's
error5:
	.rfor ALPHA = A B C
	  @echo ${ALPHA}
	.rendfor
	.rendfor

