######################################################
# passes.mk - makefile to test special source .PASSES
#
# run as:
#   mk -f passes.mk
#
# used by the perl script MkSpecSrcTest.pm
######################################################

action1: .PASSES pass1 pass2 pass3

_pass1_SUBDIRS_ = testdir1
_pass2_SUBDIRS_ = testdir1 testdir2
_pass3_SUBDIRS_ = testdir1/testdir11


_action1_pass1_TARGETS_ = tgt1
_action1_pass2_TARGETS_ = tgt2
_action1_pass3_TARGETS_ = tgt2

## testdir1 has makefile, mf.mk, testdir11/makefile
## but only makefile should be parsed for pass1 and pass2
## For pass3, makefile in testdir1 or testdir1/testdir11 should not be parsed

tgt1:
	@echo "in ${CURDIR} looking for ${.TARGET}"

tgt2:
	@echo "in ${CURDIR} looking for ${.TARGET}"

