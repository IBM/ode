IDIR ?=/bin/
.if !empty(TARGET_MACHINE:M*_aix_*)
SUBDIRS = aix
.endif

# Add all Spti_* scripts to the ILIST so that people can install odei 
# using for rpm for example and create pkgmk packages.  This is similar to 
# what we do.  Installing only one script would not allow them to do that.

ILIST = Spti_mkinstall Spti_pkgmk Spti_swpackage Spti_rpm Spti_buildpatch \
        Spti_isbuild.cmd Spti_pftwwiz.cmd

.include <${RULES_MK}>
