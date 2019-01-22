#!/bin/ksh
#Copy build zip files from NFS to DFS
#
read RELEASE     < release.name
read RELEASE_NUM < release.num
read LEVEL       < build.name

if [[ $# -ne 1 ]]; then
  instdir=inst.images
else
  instdir=$1  
fi

NFS_BUILD_ROOT=/build
DFS_BUILD_ROOT=/ode/build
FROM_DIR=$NFS_BUILD_ROOT/$RELEASE_NUM/$LEVEL/$instdir/ship
TO_DIR=$DFS_BUILD_ROOT/$RELEASE_NUM/$LEVEL/$instdir/ship

cpcmd="cp $FROM_DIR/*.zip $TO_DIR/."
echo $cpcmd
if [[ $SIMULATE != "yes" ]]; then
  $cpcmd
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* local zip copy succeeded for $RELEASE"
else 
  print "Level *$LEVEL* local zip copy failed for $RELEASE"
  return 1                                       
fi

if [[ "`ls $FROM_DIR/*.gz 2>/dev/null`" != "" ]]; then
  cpcmd="cp $FROM_DIR/*.gz $TO_DIR/."
  echo $cpcmd
  if [[ $SIMULATE != "yes" ]]; then
    $cpcmd
  fi
  if [[ $? -eq 0 ]] ; then
    print "Level *$LEVEL* local gz copy succeeded for $RELEASE"
  else 
    print "Level *$LEVEL* local gz copy failed for $RELEASE"
    return 1                                       
  fi
fi

return 0
