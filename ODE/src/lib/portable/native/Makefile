#
# ${PORTABLE_NATIVE_OFILES} is defined in Makeconf
#
OBJECTS = ${PORTABLE_NATIVE_OFILES} \
          proc${OBJ_SUFF} dir${OBJ_SUFF} dirent${OBJ_SUFF} \
          file${OBJ_SUFF} sleep${OBJ_SUFF} strings${OBJ_SUFF} \
          platver${OBJ_SUFF} regex${OBJ_SUFF}
     
# Make sure library does not import from itself
ODEDLLPORT=

# When library files are compiled, assume a DLL will be created
BUILD_SHARED_OBJECTS=

# Compile all these files as ANSI C, not C++
#
# On HPUX, just use C++ (hpode1 doesn't have the ansi compiler)
#
.ifndef HPUX
CCTYPE = ansi
SRCTYPE_CFLAGS = ${C_CFLAGS}
.endif

.include <${RULES_MK}>

.if defined(WIN32) && ${CCFAMILY} == "visual"
REGEX_CDEFS = ${CDEFS} -DHAVE_STRING_H -DSTDC_HEADERS -DREGEX_MALLOC
regex${OBJ_SUFF}_CDEFS = ${REGEX_CDEFS}
gregex${OBJ_SUFF}_CDEFS = ${REGEX_CDEFS}
.endif
