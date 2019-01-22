# If on Solaris or Hp use gnu compiler
.if !empty(TARGET_MACHINE:M*_solaris_*)
CCFAMILY=gnu
.endif

.if !empty(TARGET_MACHINE:Mhp9000_ux_*) || !empty(TARGET_MACHINE:Mia64_hpux_*)
CCFAMILY=gnu
CPP_CFLAGS=
.endif

.if !empty(TARGET_MACHINE:Mx86_sco_*)
CCFAMILY=gnu
.endif

CONTEXT_ARCH_OS = ${CONTEXT:xs/_[[:digit:]]*\$//}
CONTEXT_OS      = ${CONTEXT_ARCH_OS:xs/.*_//}

# Extra flags for the preprocessing compilation
# The surrounding underscores are just to minimize confusion
# with existing macro defines such as "linux".
PPFLAGS = -D_${CONTEXT}_ -D_${CONTEXT_ARCH_OS}_ -D_${CONTEXT_OS}_

PPFLAGS += -DEXAMPLES=${REL_NAME}_${BUILD_NUM}_bbexample.zip
PPFLAGS += -DRULES=${REL_NAME}_${BUILD_NUM}_rules.zip
PPFLAGS += -DCONFS=${REL_NAME}_${BUILD_NUM}_confs.zip
PPFLAGS += -DTOOLS=${REL_NAME}_${BUILD_NUM}_tools.jar
PPFLAGS += -DBASE_NAME=${IDIR_PREF}
PPFLAGS += -DMAJOR_VERSION=${MAJOR_VERSION}
PPFLAGS += -DMINOR_VERSION=${MINOR_VERSION}
PPFLAGS += -DMINOR_MINOR_VERSION=${MINOR_MINOR_VERSION}
PPFLAGS += -DBUILD_NUMBER=${BUILD_NUMBER}
PPFLAGS += -DLIBODE=${LIB_PREF}${ODELIBNAME}${SHLIB_SUFF}
PPFLAGS += -DTARGET_MACHINE=${TARGET_MACHINE}

# Files on which to run the preprocessor
# (the compiler with certain flags)
PREPROCESS = ode_cmf${PP_SUFF}

.if !empty(TARGET_MACHINE:M*_aix_*)
PREPROCESS += pkgtools_cmf${PP_SUFF}
PPFLAGS += -DOS_NAME=\"${TARGET_MACHINE:S/_/ /g:s/ /_/:M*_aix}\"
.endif

.include <${RULES_MK}>

.if (${PKGFAMILY:M*mkinstall*})
PPFLAGS += -DMKINSTALL=1
.elif (${PKGFAMILY:M*pkgmk*})
PPFLAGS += -DPKGMK=1 
.elif (${PKGFAMILY:M*swpackage*})
PPFLAGS += -DSWPACKAGE=1
.elif (${PKGFAMILY:M*mvs*})
PPFLAGS += -DMVS=1
.elif (${PKGFAMILY:M*rpm*})
PPFLAGS += -DRPM=1
.endif

