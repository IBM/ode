################################################
#rtcond_for_error4.mk -  makefile to test runtime
#                 .rfor error
#
# used by the perl script MkRTConditionalTest.pm
################################################

# No Variable
error4:
	.rfor = A B C
	  @echo ${ALPHA}
	.rendfor

