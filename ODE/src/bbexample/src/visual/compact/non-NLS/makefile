# Example from Microsoft DevStudio 97 converted to ODE

PROGRAMS = compact${PROG_SUFF}
OFILES   = compact${OBJ_SUFF} support${OBJ_SUFF} compact${RES_SUFF}

CFLAGS  += \
        -W3 -Dtry=__try -Dexcept=__except -Dleave=__leave -Dfinally=__finally \
        -DCRTAPI1=_cdecl -DCRTAPI2=_cdecl -nologo -D_X86_=1 -D_WINNT -DWINVER=0x0400 \
	-Z7 -DWIN32  -D_WIN32 

LDFLAGS += \
        /NODEFAULTLIB /INCREMENTAL:NO /PDB:NONE /RELEASE /NOLOGO -align:0x1000 \
        -subsystem:console,3.51 -entry:mainCRTStartup 

EXTRA_LIBS = \
	libc${LIB_SUFF} oldnames${LIB_SUFF} kernel32${LIB_SUFF}  \
	advapi32${LIB_SUFF} shell32${LIB_SUFF} user32${LIB_SUFF} 

INCFLAGS += -I.

# The header files generated from 'mc'
MC_HDRS = msg.h

# This example requires MS-VisualC++
CCFAMILY = visual

.include <${RULES_MK}>

runcompact:
	compact.exe



