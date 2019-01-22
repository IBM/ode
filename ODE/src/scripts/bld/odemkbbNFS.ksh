#!/bin/ksh
#basic script creating a new backing build
# - Create a "local" non-DFS backing build
# - Set 'in_progress' link to point to this build in progress
# - Copy build data files from DFS

read RELEASE     < release.name
read RELEASE_NUM < release.num
read LEVEL       < build.name

NFS_BUILD_ROOT=/build
DFS_BUILD_ROOT=/ode/build
SBRC_UNIX=$NFS_BUILD_ROOT/$RELEASE_NUM/.sbrc
NFS_LOGDIR=$NFS_BUILD_ROOT/$RELEASE_NUM/$LEVEL/logs

#
# see if backing build already exists. if so, nothing to do
#
if [[ -f $NFS_BUILD_ROOT/$RELEASE_NUM/$LEVEL/rc_files/sb.conf ]]; then
  return 0
fi

mkbbcmd="mkbb -rc $SBRC_UNIX $LEVEL -def"
echo $mkbbcmd
if [[ $SIMULATE != "yes" ]]; then
  $mkbbcmd
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* backing build local created for $RELEASE"
else 
  print "Level *$LEVEL* backing build local creation failed for $RELEASE"
  return 1                                       
fi

lncmd="cd $NFS_BUILD_ROOT/$RELEASE_NUM && ln -s -f $LEVEL in_progress"
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

mkdircmd="mkdir $NFS_LOGDIR"
echo $mkdircmd
if [[ $SIMULATE != "yes" ]]; then
  $mkdircmd
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* local log dir created for $RELEASE"
else 
  print "Level *$LEVEL* local log dir creation failed for $RELEASE"
  return 1                                       
fi

SRCSUBDIR=$RELEASE_NUM/$LEVEL/src
cpcmd1="cp -r $DFS_BUILD_ROOT/$RELEASE_NUM/$LEVEL/src $NFS_BUILD_ROOT/$RELEASE_NUM/$LEVEL/."
cpcmd2="cp $DFS_BUILD_ROOT/$RELEASE_NUM/release.name $NFS_BUILD_ROOT/$RELEASE_NUM/."
cpcmd3="cp $DFS_BUILD_ROOT/$RELEASE_NUM/release.num $NFS_BUILD_ROOT/$RELEASE_NUM/."
cpcmd4="cp $DFS_BUILD_ROOT/$RELEASE_NUM/build.name $NFS_BUILD_ROOT/$RELEASE_NUM/."
echo $cpcmd1
echo $cpcmd2
echo $cpcmd3
echo $cpcmd4
if [[ $SIMULATE != "yes" ]]; then
  $cpcmd1
  if [[ $? -ne 0 ]] ; then
    _error=yes
  fi
  $cpcmd2
  if [[ $? -ne 0 ]] ; then
    _error=yes
  fi
  $cpcmd3
  if [[ $? -ne 0 ]] ; then
    _error=yes
  fi
  $cpcmd4
  if [[ $? -ne 0 ]] ; then
    _error=yes
  fi
fi
if [[ $_error != "yes" ]] ; then
  print "Level *$LEVEL* local src copy succeeded for $RELEASE"
else 
  print "Level *$LEVEL* local src copy failed for $RELEASE"
  return 1                                       
fi

return 0
