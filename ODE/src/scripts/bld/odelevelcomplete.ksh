#!/bin/ksh
#odelevelcomplete.ksh for ode build document   
#basic script for committing/completing a level for ODEi2.4
read RELEASE < release.name
read LEVEL < build.name

LEVEL=${LEVEL##b}

# First we must 'commit' the level.
levelcommit="Level -commit $LEVEL -release $RELEASE"
echo $levelcommit
if [[ $SIMULATE != "yes" ]]; then
  $levelcommit
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* commit for $RELEASE successful"
else 
  print "Level *$LEVEL* commit for $RELEASE failure"
  return 1                                       
fi

# Second we need to 'complete' the level
levelcomplete="Level -complete $LEVEL -release $RELEASE"
echo $levelcomplete
if [[ $SIMULATE != "yes" ]]; then
  $levelcomplete
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* complete for $RELEASE successful"
else 
  print "Level *$LEVEL* complete for $RELEASE failure"
  return 1                                       
fi

return 0
