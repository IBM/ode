################################################################################
# SCO OpenServer 5 specific variables
#
# This is the Makeconf platform specific part for CONTEXT=x86_sco_5
#

OBJECT_FORMAT=ELF

# Booleans
DEPENDENCIES=
UNIX=
SCO=
NO_RANLIB=

CCFAMILY ?= native
CCTYPE   ?= cc

GENDEPFLAGS = -I/usr/include -E/usr/include
