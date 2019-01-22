################################################################################
# Issues:
#   Makefile:
#     - Creating multiple groups
#     - Chaining groups together.  For example, I'd like group odelib and then
#       sbtools, bldtools groups based off of odelib.
#     - Filtering files out of src.lst
#     - Setting values in the cc.lst file
#     - Organization of dependencies.  Who should depend on what?
#
#   Lemma:
#     - Providing the src list and updating the src list to be sandbox aware.
#       In other words, the -dir option to support multiple entries like 
#         "-dir o:\build\i2.1\latest\src;c:\sb1\src"
#     - Way to provide relative directories for files so can easily be used in
#       the sandbox. 
#     - Easily scripted way of excluding files from src.lst file
#     - Easily scripted way of updating cc.lst file.  Would ultimately prefer
#       C/C++ preprocessor handling
#
#   Additions:
#     - language-based whole build groups : cpp java makefile
#     - component-based groups : bin lib rules_mk make (for mk)
#       any of those groups may be included to user group in sandbox
#     - all groups will be build by default. If LEMMAGROUP defined, only specified
#       group will be build
#
################################################################################

# Lemma is only supported on the following platforms
.if defined(WIN32) || defined(AIX) || defined(OS2)

LEMMA         ?= Lemmab
_LEMMA_       ?= ${LEMMA}

LEMMATGTDIR   ?= ${SANDBOXBASE}${DIRSEP}export${DIRSEP}lemma${DIRSEP}
LEMMASRCDIR   ?= ${LEMMATGTDIR}${DIRSEP}lemma${DIRSEP}
LEMMAGROUP    ?= cpp java makefile bin_ lib_ rules_mk_ make_
LEMMASRCLST   ?= ${LEMMAGROUP:@.GRP.@${LEMMATGTDIR}${DIRSEP}lemma${DIRSEP}${.GRP.}.lst@}
LEMMACCLST    ?= ${LEMMAGROUP:@.GRP.@${LEMMATGTDIR}${DIRSEP}lemma${DIRSEP}${.GRP.}.cst@}
LEMMAGROUPTGT ?= ${LEMMAGROUP:@.GRP.@${LEMMATGTDIR}${DIRSEP}lemma${DIRSEP}${.GRP.}.lmg@} 

LEMMASRCBASE  ?= ${SANDBOXBASE}${DIRSEP}${ODESRCNAME}

lemma_all : ${LEMMAGROUPTGT}

# Rule to create a Lemma src list
#
_LEMMASRCFLAGS_ ?= -includesubdir -cd ${LEMMATGTDIR}

${LEMMASRCLST} :
	${LEMMASRCDIR:C}
	${_LEMMA_} -srclist ${.TARGET} -dir ${LEMMASRCBASE}${DIRSEP}${.TARGET:T:R:M*_:s#make_#bin${DIRSEP}make_#:s/_$//} \
	${.TARGET:T:R:s/pp$/++/:s/makefile/ODEmake/:N*_:@.LANG.@-m ${.LANG.}@} ${_LEMMASRCFLAGS_}

.ifdef USE_LEMMACCLST
# Rule to create a Lemma Conditional Compile list
#

${LEMMACCLST} : $${.TARGET:s/cst$$/lst/}
	${LEMMASRCDIR:C}
	${_LEMMA_} -cclist ${.TARGET} -extractsrc @${.TARGET:s/cst$/lst/} \
	${.TARGET:T:R:s/pp$/++/:s/makefile/ODEmake/:N*_:@.LANG.@-l ${.LANG.}@}

.endif # ifdef USE_LEMMACCLST

# Rule to create a Lemma Group
#
_LEMMAGROUPFLAGS_ ?= \
	-a @${.TARGET:R}.lst ${.TARGET:T:R:s/pp$/++/:s/makefile/ODEmake/:N*_:@.LANG.@-m ${.LANG.}@} \
	${USE_LEMMACCLST:D-cc @${.TARGET:R}.cst} \
	-allowduplnames T -buildscratch -cd ${LEMMATGTDIR}

${LEMMAGROUPTGT} : $${USE_LEMMACCLST:D$${.TARGET:R}.cst} $${USE_LEMMACCLST:U$${.TARGET:R}.lst}
	${LEMMASRCDIR:C}
	${_LEMMA_} -update ${.TARGET:R:T:s/_$//} ${_LEMMAGROUPFLAGS_}

.endif # defined(WIN32) || defined(AIX) || defined(OS2)

