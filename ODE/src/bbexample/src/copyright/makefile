# copyright.txt is installed only on MVS
.if !empty(MACHINE:Mmvs390_oe_*)

# The files to be installed
ILIST = copyright.txt

# Don't include this ILIST in the generated CMF
EXCLUDE_ILIST = ${ILIST}

# The directory where installed files will be copied
IDIR  = /./

.endif # mvs

.include <${RULES_MK}>
