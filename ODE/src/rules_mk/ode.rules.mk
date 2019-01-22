
# A shortcut for building
do_all: build_all install_all zip_all

.if !empty(MACHINE:Mas400_os400_*) && (${MAKEDIR:H:T} == "bin")
OUTPUTDIR %= ${MAKEDIR:T}${REL_NAME:S/ode//:S/i//:S/.//}
.endif

.if !empty(MACHINE:Mrios_aix_*)
${_PROGRAMS_} : .POSTCMDS
	chmod 755 ${.TARGET}
.endif

.if !empty(MACHINE:M*_openvms_*)
.include <${PROJECT_NAME}_lemma.mk>
.else
.include <${PROJECT_NAME}.lemma.mk>
.endif

unjarXMLParser_all:
	${_CLASSGENDIR_:C}
	${CHDIR} ${_CLASSGENDIR_} ${CMDSEP} ${_JAR_} xvf ${ODE_ROOT}/build/util/latest/src/xmlJars/xercesImpl.jar
	${CHDIR} ${_CLASSGENDIR_} ${CMDSEP} ${_JAR_} xvf ${ODE_ROOT}/build/util/latest/src/xmlJars/xmlParserAPIs.jar
