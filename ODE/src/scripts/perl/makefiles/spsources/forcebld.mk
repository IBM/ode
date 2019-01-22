#########################################################
# forcebld.mk - makefile to test special source .FORCEBLD
#
# run as:
#    mk -f forcebld.mk -DTESTDIR=c:\testdir
#
# Used by the perl script MkSpecSrcTest.pm
#########################################################

.ORDER : ${TESTDIR}targ1 ${TESTDIR}targ2 ${TESTDIR}targa ${TESTDIR}targb ${TESTDIR}targ.abc ${TESTDIR}targ.o clean
all    : ${TESTDIR}targ1 ${TESTDIR}targ2 ${TESTDIR}targa ${TESTDIR}targb 
all    : ${TESTDIR}targ.abc ${TESTDIR}targ.o clean
.SUFFIXES: .abc .xyz
.LINKTARGS: ${TESTDIR}targa   ${TESTDIR}targb
TEXTVAR=ODEisGreat

init :
	@echo initialization
	${TEXTVAR:A${TESTDIR}targ1}
	${TEXTVAR:A${TESTDIR}targ2}
	${TEXTVAR:A${TESTDIR}targa}
	${TEXTVAR:A${TESTDIR}targb}
	${TEXTVAR:A${TESTDIR}targ.xyz}
	${TEXTVAR:A${TESTDIR}targ.abc}  
	${TEXTVAR:A${TESTDIR}targ.o}  

${TESTDIR}targ1 :
	@echo targ1

${TESTDIR}targ2 : .FORCEBLD
	@echo targ2

${TESTDIR}targa : 
	@echo making targa and targb

${TESTDIR}targb : .FORCEBLD

${TESTDIR}targ1 : .FORCEBLD

.xyz.abc : .FORCEBLD
	@echo making targ.abc

%.o : %.abc  .FORCEBLD
	@echo making targ.o

clean :
	@echo cleanup
	.rrm ${TESTDIR}targ1 ${TESTDIR}targ2 ${TESTDIR}targa ${TESTDIR}targb 
	.rrm ${TESTDIR}targ.xyz ${TESTDIR}targ.abc ${TESTDIR}targ.o