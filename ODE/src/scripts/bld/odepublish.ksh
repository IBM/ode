#!/bin/ksh
#odepublish.ksh for ode build document   
# Just a wrapper form "pubbld" so it calls with appropriate info

read RELEASE < release.name
read RELEASE_NUM < release.num
read LEVEL < build.name

#what is the new build name?
NAME=$LEVEL
BUILD_ROOT=/ode/build
NFS_BUILD_ROOT=/build
BB_BASE=$BUILD_ROOT/$RELEASE_NUM/$NAME

pubbldcmd="${BB_BASE}/src/scripts/bld/pubbld ${RELEASE_NUM} ${NAME##b}"
echo $pubbldcmd
if [[ $SIMULATE != "yes" ]]; then
  $pubbldcmd
fi
if [[ $? -eq 0 ]] ; then
  print "pubbld succeeded for $RELEASE build $LEVEL"
else 
  print "pubbld failed for $RELEASE build $LEVEL"
  return 1
fi

return 0
