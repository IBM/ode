################################################################################
# Solaris 7+ Sparc specific variables
#
# This is the Makeconf platform specific part for CONTEXT=sparc_solaris_7
#

OBJECT_FORMAT  = ELF

# Booleans
SOLARIS        = 
SOLARIS_SPARC  =
UNIX           =
DEPENDENCIES   =

LDFLAGS       += -lsocket -lnsl
CCFAMILY      ?= gnu
CCTYPE        ?= cc

# Gendep flags - include then exclude the system header files
GENDEPFLAGS = -I/usr/include -E/usr/include
