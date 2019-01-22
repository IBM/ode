#!/bin/ksh
# Copy zip/jar/txt files from DFS to NFS
#
read RELEASE     < release.name
read RELEASE_NUM < release.num
read LEVEL       < build.name


NFS_BUILD_ROOT=/build
DFS_BUILD_ROOT=/ode/build
FROM_DIR=$DFS_BUILD_ROOT/$RELEASE_NUM/$LEVEL/inst.images/ship
TO_DIR=$NFS_BUILD_ROOT/$RELEASE_NUM/$LEVEL/inst.images/ship

cpcmd1="cp $FROM_DIR/ode${RELEASE_NUM##i}_b${LEVEL##b}_rules.zip $TO_DIR/."
echo $cpcmd1
if [[ $SIMULATE != "yes" ]]; then
  $cpcmd1
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* local zip copy succeeded for $RELEASE"
else 
  print "Level *$LEVEL* local zip copy failed for $RELEASE"
  return 1                                       
fi

cpcmd2="cp $FROM_DIR/ode${RELEASE_NUM##i}_b${LEVEL##b}_bbexample.zip $TO_DIR/."
echo $cpcmd2
if [[ $SIMULATE != "yes" ]]; then
  $cpcmd2
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* local zip copy succeeded for $RELEASE"
else 
  print "Level *$LEVEL* local zip copy failed for $RELEASE"
  return 1                                       
fi

cpcmd3="cp $FROM_DIR/ode${RELEASE_NUM##i}_b${LEVEL##b}_tools.jar $TO_DIR/."
echo $cpcmd3
if [[ $SIMULATE != "yes" ]]; then
  $cpcmd3
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* local zip copy succeeded for $RELEASE"
else 
  print "Level *$LEVEL* local zip copy failed for $RELEASE"
  return 1                                       
fi

cpcmd4="cp $FROM_DIR/ode${RELEASE_NUM##i}_b${LEVEL##b}_readme.txt $TO_DIR/."
echo $cpcmd4
if [[ $SIMULATE != "yes" ]]; then
  $cpcmd4
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* local txt copy succeeded for $RELEASE"
else 
  print "Level *$LEVEL* local txt copy failed for $RELEASE"
  return 1                                       
fi

DOC_DIR=$FROM_DIR/../doc
if [[ -d $DOC_DIR ]]; then
  mdcmd5="mkdir -p $TO_DIR/../doc"
  cpcmd5="cp $DOC_DIR/* $TO_DIR/../doc/."
  echo $mdcmd5
  echo $cpcmd5
  if [[ $SIMULATE != "yes" ]]; then
    $mdcmd5
    $cpcmd5
  fi
  if [[ $? -eq 0 ]] ; then
    print "Level *$LEVEL* local txt copy succeeded for $RELEASE"
  else 
    print "Level *$LEVEL* local txt copy failed for $RELEASE"
    return 1                                       
  fi
else
  if [[ $RELEASE_NUM != "i2.3" ]]; then
    print "No doc directory at $DOC_DIR"
    print "Level *$LEVEL* local txt copy failed for $RELEASE"
    return 1                                       
  fi
fi

cpcmd6="cp $FROM_DIR/ode${RELEASE_NUM##i}_b${LEVEL##b}_confs.zip $TO_DIR/."
echo $cpcmd6
if [[ $SIMULATE != "yes" ]]; then
  $cpcmd6
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* local zip copy succeeded for $RELEASE"
else 
  print "Level *$LEVEL* local zip copy failed for $RELEASE"
  return 1                                       
fi

return 0
