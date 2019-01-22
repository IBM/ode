################################################################################
# HP-UX 11.0+ specific variables
#
# This is the Makeconf platform specific part for CONTEXT=hp9000_ux_11
#

OBJECT_FORMAT  = A.OUT

# Booleans
UNIX           = 
HPUX           = 
DEPENDENCIES   =

#Variable to control the execution of swreg by Spti_pkgmk
RUN_SWREG ?= "no"
RUN_SWREG %= ${RUN_SWREG}

LDFLAGS       += -lBSD
CCFAMILY      ?= native
CCTYPE        ?= ansi

# Gendep flags - include then exclude the system header files
GENDEPFLAGS = -I/usr/include -E/usr/include

