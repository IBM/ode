#!/bin/ksh
#odelevelcreate.ksh for ode build document   
#basic script creating a new level for odei2.3  
#add the cmvc commands to your path 
read RELEASE     < release.name
read RELEASE_NUM < release.num
read NEWLEVEL    < build.name

NEWLEVEL=${NEWLEVEL##b}

levelcreate="Level -create $NEWLEVEL -release $RELEASE -type production"
echo $levelcreate
if [[ $SIMULATE != "yes" ]]; then
  $levelcreate
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$NEWLEVEL* created successfully for $RELEASE"
else 
  print "Level *$NEWLEVEL* NOT created  for $RELEASE"
  return 1                                       
fi

#   now determine what tracks are available
TRACKS=`Report  -raw -view TrackView -where "releaseName in ('$RELEASE') and state in ('integrate')" \
  | awk -F'|' '{print $2}'`
if [[ $? -eq 0 ]] ; then
  print "$RELEASE integrated tracks list created "
else
  print "$RELEASE integrated tracks list failure "
  return 1
fi

#create a levelmember for each track in the integrate state
levelmemcreate="LevelMember  -create -level $NEWLEVEL -release $RELEASE -defect $TRACKS"
echo $levelmemcreate
if [[ $SIMULATE != "yes" ]]; then
  $levelmemcreate
fi

if [[ $? -eq 0 ]] ; then
  print "Level *$NEWLEVEL* populated successfully for $RELEASE"
else
  print "Level *$NEWLEVEL* not populated successfully for $RELEASE"
  return 1
fi

return 0
