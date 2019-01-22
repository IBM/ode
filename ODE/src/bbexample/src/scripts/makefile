.if (!empty(MACHINE:Mhp9000_ux_*))
# Files that will be copied to the install directory
ILIST			=post  preremove pre 

# Don't include this ILIST in the generated CMF
EXCLUDE_ILIST = ${ILIST}

# Directory to where installed files will be copied
IDIR = /

.elif (!empty(MACHINE:M*_linux_*))
#Files that will be copied to the install directory
ILIST                   =pre

# Directory to where installed files will be copied
IDIR = /

.endif

.include <${RULES_MK}>
