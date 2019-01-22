#!/bin/ksh
# odeos2chmod.ksh for ode build document   
# Need to make OS/2 binaries visable after the FTP

read RELEASE < release.name
read RELEASE_NUM < release.num
read LEVEL < build.name

#what is the new build name?
NAME=$LEVEL
BUILD_ROOT=/ode/build
NFS_BUILD_ROOT=/build
BB_BASE=$BUILD_ROOT/$RELEASE_NUM/$NAME

if [[ -d ${BB_BASE}/inst.images/x86_os2_4/bin ]]; then
  chmodcmd="chmod 755 ${BB_BASE}/inst.images/x86_os2_4/bin/*"
  echo $chmodcmd
  if [[ $SIMULATE != "yes" ]]; then
    $chmodcmd
  fi
  if [[ $? -eq 0 ]] ; then
    print "chmod succeeded for $RELEASE build $LEVEL"
  else 
    print "chmod failed for $RELEASE build $LEVEL"
    return 1
  fi
fi

return 0
