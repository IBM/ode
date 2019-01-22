HTML_FILES = \
  CMFGenerationUserGuide.htm \
  HPServicePackagingReference.htm \
  index.htm \
  ode390_service_config.htm \
  ode390_service_scenario.htm \
  ode390_service_users.htm \
  ODEAdminsGuide.htm \
  ODEAixPackagingToolsReference.htm \
  ODEBuildReference.htm \
  ODECommonMakefiles.htm \
  ODEConcepts.htm \
  ODEConvertExample.htm \
  ODEGuiReference.htm \
  ODELinksResources.htm \
  ODEMakeMake.htm \
  ODEMakeReference.htm \
  ODEMigrationGuide.htm \
  ODEPackagingConcepts.htm \
  ODEPackagingReference.htm \
  ODEPackagingUserGuide.htm \
  ODEPkgTutorial.htm \
  ODERequisitesInfoStanzaDesign.htm \
  ODEServicePackagingReference.htm \
  ODETsoBuildUserGuide.htm \
  ODETutorial.htm \
  RecoveryGuide.htm \
  RPMServicePackagingReference.htm \
  SolarisServicePackagingReference.htm \
  buildpatch_conf.htm \
  buildpatch_README.htm \
  buildSolarisPatch.ps \
  StyleGuide.htm

PIC_FILTER = *.gif *.jpg

_PIC_FILES_ != cd ${.CURDIR} && sbls -p ${MAKETOP}../../src${MAKEDIR}/.
PIC_FILES := ${_PIC_FILES_:T}
PIC_FILES := ${PIC_FILTER:@.F.@${PIC_FILES:M${.F.}}@}
ILIST = ${HTML_FILES} ${PIC_FILES}
IDIR  = /../htmls/

.include <${RULES_MK}>

