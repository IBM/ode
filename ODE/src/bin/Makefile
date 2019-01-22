SUBDIRS = gendep mkdep mkpath genpath build make

.ifdef VMS
SUBDIRS += oderun
.else
SUBDIRS += workon
.endif

.ifndef COREMAKE_BUILD
SUBDIRS += resb currsb sbls sbinfo sbmerge mklinks mksb mkbb crlfcon
.endif

.ifdef OS2 || WIN32
SUBDIRS += odeecho
.endif

.ifndef OS400 && MVSOE && VMS && HPUX_IA64 && HPOSS
.ifdef BUILD_JIKES
SUBDIRS += jikes
.endif
.endif

# Overwrite all other SUBDIRS settings just to include 'make'
# for the purposes of WEBMAKE
.ifdef WEBMAKE
SUBDIRS = make
.endif

.include <${RULES_MK}>
