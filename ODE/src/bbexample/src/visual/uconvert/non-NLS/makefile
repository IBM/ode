# Example from Microsoft DevStudio 97 converted to ODE

PROGRAMS    = uconvert${PROG_SUFF}
OFILES      = uconvert${OBJ_SUFF} install${OBJ_SUFF} dialogs${OBJ_SUFF} uconvert${RES_SUFF} 
HELP_FILES  = uconvert${HLP_SUFF}
INCFLAGS   += -I.
CFLAGS     += \
	-W3 -Dtry=__try -Dexcept=__except -Dleave=__leave -Dfinally=__finally \
	-DCRTAPI1=_cdecl -DCRTAPI2=_cdecl -D_X86_=1 -D_WINNT -DWINVER=0x030A -DWIN32  -D_WIN32
LDFLAGS    += /NODEFAULTLIB /INCREMENTAL:NO /PDB:NONE /RELEASE -subsystem:windows,3.50 -entry:WinMainCRTStartup

EXTRA_LIBS  = \
	libc${LIB_SUFF} oldnames${LIB_SUFF} kernel32${LIB_SUFF}  \
	advapi32${LIB_SUFF} user32${LIB_SUFF} gdi32${LIB_SUFF} \
	comdlg32${LIB_SUFF} winspool${LIB_SUFF} advapi32${LIB_SUFF}

# This example requires MS-VisualC++
CCFAMILY = visual

.include <${RULES_MK}>

runuconvert:
	uconvert.exe


