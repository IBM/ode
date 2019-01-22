################################################################################
# Version: %RELEASE_NAME% (Build %LEVEL_NAME%, %BUILD_DATE%)
#
#  This file is the top-level tso build rules file
#   Included makefiles:
#     tso.mvsbld.mk
#
################################################################################
.ifndef __TSO_RULES_MK__
__TSO_RULES_MK__=

.if !empty(MACHINE:M*_openvms_*)
.include <tso_mvsbld.mk>
.else
.include <tso.mvsbld.mk>
.endif


.endif

