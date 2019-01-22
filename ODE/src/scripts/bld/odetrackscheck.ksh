#!/bin/ksh
#odetrackscheck.ksh for ode build document   
#basic script for verifying integrate tracks exist for the build  
if [[ $# = 0 ]]; then
RELEASE=ODE3.0
RELEASE_NUM=${RELEASE##ODE}
elif [[ $# = 1 ]]; then
RELEASE_NUM=$1
RELEASE=ODE$RELEASE_NUM
else
  print "Usage: odetrackscheck.ksh [release num]"
  print "Got  : odetrackscheck.ksh $*"
  exit 1
fi
print $RELEASE > release.name
print $RELEASE_NUM > release.num

#set the family you wish to work with            
export CMVC_FAMILY=spacmvc

#set the valid user id                           
export CMVC_BECOME=odebld
 
#set the release                                 
export CMVC_RELEASE=$RELEASE

# This will report on all committed levels for ode3.0.  The last level
# committed will be at the top of the list and will be used for the extract.

LASTLEVEL=`Report -raw -view LevelView -where "releaseName in ('$RELEASE') and state in ('complete') order by commitDate desc"\
        | head -1 | awk -F'|' '{print $1}'`

print $LASTLEVEL > lastbuild.name

# figure out what number this level is and create the next one
# need to replace this release with the variable
(( NEWLEVEL=${LASTLEVEL%%*([a-z])} + 1 ))

#   keep the name in a file for the extract later
print $NEWLEVEL > build.name

# now determine what tracks are available
TRACKS=`Report  -raw -view TrackView -where "releaseName in ('$RELEASE') and state in ('integrate')" \
  | awk -F'|' '{print $2}'`
if [[ $? -eq 0 ]] ; then
  if [[ $TRACKS = "" ]]; then
    print "No integrate tracks available for release *$RELEASE*"
    return 2
  else
    print "$RELEASE has integreated tracks: $TRACKS"
  fi
else
  print "$RELEASE integrated tracks list failure "
  return 1
fi

return 0
