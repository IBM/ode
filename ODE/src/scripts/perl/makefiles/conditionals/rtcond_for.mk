################################################
#rtcond_for.mk -  makefile to test runtime conditional
#                 for
#
# used by the perl script MkRTConditionalTest.pm
################################################

all: test1 test2 test3 test4 test5 elvis.alive

VARX=xyz

test1:
	@echo ${VARX}
	.rfor VARX = ABC  DEF  GHI 
	  @echo ${VARX} 
	  .rif defined(ELVIS)
	    @echo Hound Dog
	  .relse
	    @echo Hello
	  .rendif  
	.rendfor
	@echo ${VARX}

test2:
	.rfor VAR = 4 - 7
	  @echo ${VAR}
	.rendfor

test3:
	.rfor ALPHA1 =  A  B  C 
	  .rfor NUMERAL = 1 - 3
	    .rfor ALPHA2 = X 
	      @echo ${ALPHA1}${NUMERAL}${ALPHA2}
	    .rendfor
	  .rendfor
	.rendfor

test4:
	.rfor NUMERAL = 5 - 2
	  @echo ${NUMERAL}
	.rendfor

test5:
	.rfor ALPHA = A - C
	  @echo ${ALPHA}
	.rendfor

%.alive : %.dead
	.rif defined(ELVIS)
	  .rfor VARX = Blue Suede Shoes
	    @echo ${VARX}
	  .rendfor
	.relse
	  .rfor VARX = 1 - 2
	    @echo What, No Elvis?
	  .rendfor  
	.rendif

elvis.dead :
	@echo JailhouseRock