#!/bin/ksh
# Copy build package files from NFS to DFS
#
read RELEASE     < release.name
read RELEASE_NUM < release.num
read LEVEL       < build.name


NFS_BUILD_ROOT=/build
DFS_BUILD_ROOT=/ode/build
FROM_DIR=$NFS_BUILD_ROOT/$RELEASE_NUM/$LEVEL/inst.images/ship
TO_DIR=$DFS_BUILD_ROOT/$RELEASE_NUM/$LEVEL/inst.images/ship

cpcmd="cp $FROM_DIR/*_pkgadd* $TO_DIR/."
echo $cpcmd
if [[ $SIMULATE != "yes" ]]; then
  $cpcmd
fi

if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* local copy succeeded for $RELEASE"
else 
  print "Level *$LEVEL* local copy failed for $RELEASE"
  return 1                                       
fi

cpcmd="cp $FROM_DIR/*_rpm* $TO_DIR/."
echo $cpcmd
if [[ $SIMULATE != "yes" ]]; then
  $cpcmd
fi

if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* local copy succeeded for $RELEASE"
else 
  print "Level *$LEVEL* local copy failed for $RELEASE"
  return 1                                       
fi

return 0
