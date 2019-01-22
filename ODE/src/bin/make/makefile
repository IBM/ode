.ifdef WEBMAKE
PROGRAMS = webmake${PROG_SUFF}
.else
PROGRAMS = mk${PROG_SUFF}
.endif # WEBMAKE

OFILES = make${OBJ_SUFF} command${OBJ_SUFF} commands${OBJ_SUFF}\
         constant${OBJ_SUFF} dir${OBJ_SUFF} graph${OBJ_SUFF} \
         graphnd${OBJ_SUFF} keyword${OBJ_SUFF} \
         makec${OBJ_SUFF} makefile${OBJ_SUFF} mfstmnt${OBJ_SUFF} \
         mkcmdln${OBJ_SUFF} mkfilcac${OBJ_SUFF} passndc${OBJ_SUFF} \
         passndp${OBJ_SUFF} suffix${OBJ_SUFF} \
         sufftrfm${OBJ_SUFF}  patterns${OBJ_SUFF} \
         targnode${OBJ_SUFF} \
         job${OBJ_SUFF} passndpd${OBJ_SUFF} instant${OBJ_SUFF}

# SLL: On Interix, template instantiation for this code part
# does only work correctly with this option.
.if !empty(MACHINE:Mx86_interix_*)
CFLAGS += -frepo
.endif

# SLL: The -frepo option causes a file with extension .rpo
# to be created for each object file. Add these files to the
# GARBAGE variable to have these files deleted when cleaning.
GARBAGE = ${OFILES:S/${OBJ_SUFF}$/.rpo/g}

LIBS = ${LIB_PREF}${ODELIBNAME}${LIB_SUFF}

IDIR     ?= /bin/
ILIST    = ${PROGRAMS}

.if defined(WIN32)
LDFLAGS += /STACK:524288
.elif defined(OS2)
STACK_SIZE = 524288
.endif

.include <${RULES_MK}>

