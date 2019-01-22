#####################################################
# mf.mk - to test .PASSES
#         used by makefiles/spsources/passes.mk
#####################################################

_action1_pass1_TARGETS_ = tgt1
_action1_pass2_TARGETS_ = tgt2

tgt1:
	@echo "in ${CURDIR} looking for ${.TARGET}"

tgt2:
	@echo "in ${CURDIR} looking for ${.TARGET}"

