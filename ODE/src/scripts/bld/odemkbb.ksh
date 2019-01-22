#!/bin/ksh
#basic script creating a new backing build
# - Create backing build
# - Set 'in_progress' link to point to this build in progress

read RELEASE     < release.name
read RELEASE_NUM < release.num
read LEVEL       < build.name
read LASTLEVEL   < lastbuild.name

BUILD_ROOT=/ode/build
SBRC_UNIX=$BUILD_ROOT/$RELEASE_NUM/.sbrc

#
# see if backing build already exists. if so, nothing to do
#
if [[ -f $BUILD_ROOT/$RELEASE_NUM/$LEVEL/rc_files/sb.conf ]]; then
  return 0
fi

mkbbcmd="mkbb -rc $SBRC_UNIX $LEVEL -def"
echo $mkbbcmd
if [[ $SIMULATE != "yes" ]]; then
  $mkbbcmd
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* backing build created for $RELEASE"
else 
  print "Level *$LEVEL* backing build creation failed for $RELEASE"
  return 1                                       
fi

lncmd="cd $BUILD_ROOT/$RELEASE_NUM && ln -s -f $LEVEL in_progress"
echo $lncmd
if [[ $SIMULATE != "yes" ]]; then
  ksh -c "$lncmd"
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* link created for $RELEASE"
else 
  print "Level *$LEVEL* link failed for $RELEASE"
  return 1                                       
fi

# We need to prepare for the FTP of the OS/2 binaries.
mkdircmd="mkdir -p $BUILD_ROOT/$RELEASE_NUM/$LEVEL/inst.images/x86_os2_4/bin"
echo $mkdircmd
if [[ $SIMULATE != "yes" ]]; then
  $mkdircmd
fi

return 0
