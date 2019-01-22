OBJECTS_SUBDIRS = native
DEPEND_STANDARD_SUBDIRS = ${OBJECTS_SUBDIRS}
OBJECTS = runcmd${OBJ_SUFF} hashable${OBJ_SUFF} ilist${OBJ_SUFF} \
          platcon${OBJ_SUFF} env${OBJ_SUFF} instant${OBJ_SUFF}
                     
.ifdef WIN32
ODEDLLPORT=
.endif

.if !defined(UNIX) && !defined(VMS)
.ifndef STATIC_BUILD
OBJECTS += dll${OBJ_SUFF}
.endif
.endif

# When library files are compiled, assume a DLL will be created
BUILD_SHARED_OBJECTS =

.include <${RULES_MK}>
