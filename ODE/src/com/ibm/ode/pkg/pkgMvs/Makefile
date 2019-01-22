SUBDIRS = service
OBJECTS_SUBDIRS = service
REXXFILES = alloclib.rexx \
            gather.rexx \
            genalias.rexx \
            ococheck.rexx \
            vplfiles.rexx

# Override EXPORTDIR definition for this makefile
EXPORTDIR = ../export/classes/com/ibm/ode/pkg/pkgMvs
INCLUDES = ${REXXFILES}
EXPDIR = /./
EXPINCTOP = ./
EXPORT_USING_TAR=

JAVA_CLASSES = MvsPkgError${CLASS_SUFF} \
	             MvsProperties${CLASS_SUFF} \
	             MvsValidation${CLASS_SUFF} \
	             MvsProductData${CLASS_SUFF} \
	             MvsShipData${CLASS_SUFF} \
	             MvsPackageData${CLASS_SUFF} \
	             MvsControlFile${CLASS_SUFF} \
	             MvsJobInfo${CLASS_SUFF} \
	             MvsJobOutputScanner${CLASS_SUFF} \
	             MvsPdsData${CLASS_SUFF} \
	             MvsJclGenerator${CLASS_SUFF} \
	             MvsFtpException${CLASS_SUFF} \
	             MvsFtp${CLASS_SUFF} \
	             MvsJobManager${CLASS_SUFF} \
	             RunMvsPkg${CLASS_SUFF} \
	             MvsExecInstallation${CLASS_SUFF}

.include <$(RULES_MK)>
