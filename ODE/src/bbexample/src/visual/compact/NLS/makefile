# Example from Microsoft DevStudio 97 converted to ODE

NLV_PROGRAMS   = compact${PROG_SUFF}
NLV_RESOURCES  = compact${RES_SUFF}

# The objects and resources that PROGRAMS depends on
# Use the relative reference to the resource via _NLV_RESOURCES_
OFILES         = compact${OBJ_SUFF} support${OBJ_SUFF} ${_NLV_RESOURCES_}

CFLAGS        += \
        -W3 -Dtry=__try -Dexcept=__except -Dleave=__leave -Dfinally=__finally \
        -DCRTAPI1=_cdecl -DCRTAPI2=_cdecl -nologo -D_X86_=1 -D_WINNT \
	-DWINVER=0x0400 -Z7 -DWIN32  -D_WIN32 

LDFLAGS       += \
        /NODEFAULTLIB /INCREMENTAL:NO /PDB:NONE /RELEASE /NOLOGO -align:0x1000 \
        -subsystem:console,3.51 -entry:mainCRTStartup 

EXTRA_LIBS     = \
	libc${LIB_SUFF} oldnames${LIB_SUFF} kernel32${LIB_SUFF}  \
	advapi32${LIB_SUFF} shell32${LIB_SUFF} user32${LIB_SUFF} 

INCFLAGS      += -I.

RCINPUTFILES = compact.rc


# The header files generated from 'mc'
MC_HDRS = msg.h

# This example requires MS-VisualC++
CCFAMILY = visual

.include <${RULES_MK}>

# The compiled files depend on language specific header files
INCFLAGS += ${_NLV_GENINC_}

runcompact:
	${_NLV_PROGRAMS_}

