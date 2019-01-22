#!/bin/ksh
#odelevelcheck.ksh for ode build document   
#basic script creating a new level for ODEi2.4
#add the cmvc commands to your path 
read RELEASE < release.name
read LEVEL < build.name

# Just in case there is a "b", take it off
LEVEL=${LEVEL##b}

levelcheck=`Level -check $LEVEL -release $RELEASE | awk '{print $3}'`
if [[ $? -eq 0 && $levelcheck = "" ]] ; then
  print "Level *$LEVEL* has satisfied co/pre-reqs for $RELEASE"
else 
  print "Level *$LEVEL* has unsatisfied co/pre-reqs for $RELEASE. Defects: $levelcheck"
  return 1                                       
fi

return 0
