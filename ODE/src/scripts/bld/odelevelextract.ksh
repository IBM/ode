#!/bin/ksh
#odelevelextract.ksh for ode build document   
# written to read the new build name, create the new directories,
# and extract the files for this new build
#basic script extracting the last committed release for ode  

read RELEASE     < release.name
read RELEASE_NUM < release.num
read LEVEL       < build.name
read LASTLEVEL   < lastbuild.name

#this will report on all integrated levels for the ode release, the last level in integrate will be at the top of the list and will be used for the extract.

#what is the new build name?
NAME=$LEVEL
BUILD_ROOT=/ode/build
BB_BASE=$BUILD_ROOT/$RELEASE_NUM/$NAME
LOGDIR=$BB_BASE/logs

echo mkdir $LOGDIR
if [[ $SIMULATE != "yes" ]]; then
mkdir $LOGDIR
fi

DMASK=750
FMASK=450
#extract the committed release
levelext1="Level -extract ${LASTLEVEL##b} -release $RELEASE -root $BB_BASE/src -fmask $FMASK -dmask $DMASK -full -node direct"

echo $levelext1
if [[ $SIMULATE != "yes" ]]; then
  $levelext1
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LASTLEVEL* extracted successfully for $RELEASE"
else 
  print "Level *$LASTLEVEL* not extracted successfully for $RELEASE"
  return 1
fi
 
# extract the last integrated level
levelext2="Level  -extract ${LEVEL##b}  -release $RELEASE -root $BB_BASE/src -fmask $FMASK -dmask $DMASK -node direct "

echo $levelext2
if [[ $SIMULATE != "yes" ]]; then
  $levelext2
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* extracted successfully for $RELEASE"
else 
  print "Level *$LEVEL* not extracted successfully for $RELEASE"
  return 1
fi

echo mkdir $BB_BASE/logs
echo chmod 755 $BB_BASE/src/scripts/bld/*
echo $BB_BASE/src/scripts/bld/prebld $RELEASE_NUM ${NAME##b}
if [[ $SIMULATE != "yes" ]]; then
  mkdir $BB_BASE/logs
  chmod 755 $BB_BASE/src/scripts/bld/*
  $BB_BASE/src/scripts/bld/prebld $RELEASE_NUM ${NAME##b}
fi
if [[ $? -eq 0 ]] ; then
  print "prebld succeeded for $RELEASE build $LEVEL"
else 
  print "prebld failed for $RELEASE build $LEVEL"
  return 1
fi

return 0
