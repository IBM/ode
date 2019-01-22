#!/bin/ksh
#basic script for building, installing and zipping ODE

read RELEASE < release.name
read RELEASE_NUM < release.num
read LEVEL < build.name

BUILD_ROOT=/ode/build
BB_BASE=$BUILD_ROOT/$RELEASE_NUM/$LEVEL
LOGDIR=$BB_BASE/logs
SCRIPTDIR=$BB_BASE/src/scripts/perl

# Determine operating system
OSTYPE=`uname`
if [[ $OSTYPE = "HP-UX"    ]] ; then
   export CCFAMILY=gnu;
   print "CCFAMILY = $CCFAMILY"
fi

# Need to step over to the script directory first
cd $SCRIPTDIR
testcmd="perl testbld.pl release=$RELEASE_NUM build=$LEVEL testlevel=regression logdir=$LOGDIR"
echo $testcmd
if [[ $SIMULATE != "yes" ]]; then
  $testcmd
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* test scripts PASSED for $RELEASE"
else
  print "Level *$LEVEL* test scripts FAILED for $RELEASE"
  return 1
fi

return 0
