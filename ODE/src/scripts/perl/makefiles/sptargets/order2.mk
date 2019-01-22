######################################################
# order2.mk - makefile to test special target .ORDER
#
# run as:
#    mk -f order2.mk
#
# used by the perl script MkSpecTgtTest.pm
######################################################
.END:
	@echo END

.EXIT:
	@echo EXIT

.BEGIN:
	@echo BEGIN

.ORDER: .EXIT .BEGIN .END

test:
