CONTEXT_ARCH_OS = ${CONTEXT:xs/_[[:digit:]]*\$//}
CONTEXT_OS      = ${CONTEXT_ARCH_OS:xs/.*_//}

# Extra flags for the preprocessing compilation
# The surrounding underscores are just to minimize confusion
# with existing macro defines such as "linux".
PPFLAGS += -D_${CONTEXT}_ -D_${CONTEXT_ARCH_OS}_ -D_${CONTEXT_OS}_

# If (on MVS/USS) there are prelinked object modules to be installed,
# set the appropriate C-preprocessor variable to include them in the
# CMF.
#
PPFLAGS += ${USE_PRELINKED:D-DUSE_PRELINKED}
PPFLAGS += ${USE_SHARED_LIBRARY:D-DUSE_SHARED_LIBRARY}
PPFLAGS += -DBASE_NAME=${IDIR_PREF}

# Files on which to run the preprocessor (the compiler with certain flags)
PREPROCESS = bbex_cmf${PP_SUFF} bbex_proj_cmf${PP_SUFF}

# Since c89 compiler doesn't always exist on HP-UX and aCC preprocessor 
# can't handle the CMF so we'll use gcc for the default here.
.if !empty(MACHINE:Mhp9000_ux_*) || !empty(MACHINE:Mmips_irix_*)
CCFAMILY = gnu
.endif

.include <${RULES_MK}>
