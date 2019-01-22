#!/bin/ksh
# odereadme.ksh for ode build document   
#basic script to create a defect and update files for the next build

read RELEASE < release.name
read RELEASE_NUM < release.num
read LEVEL < build.name
                     
LEVEL=${LEVEL##b}
BB_DIR=/ode/build/$RELEASE_NUM

README_DIR=/ode/test/odebld
TRACKS_LIST=$README_DIR/doc/tracks.lst

# create a  defect 
ABSTRACT="Update fixes.txt for build $LEVEL"
if [[ $SIMULATE != "yes" ]]; then
  DEFECT=`Defect -open -component odeinternal -remarks "$ABSTRACT" -abstract "$ABSTRACT" -release $RELEASE -prefix d -severity 3 -symptom function_needed -phaseFound building | tail -1 | awk '{print $6}' | sed -e "s;\.$;;"`
  if [[ $? -eq 0 ]] ; then
    print "Opened defect for fixes.txt updates $DEFECT"
  else 
    print "Defect open failed!!!"
    return 1                                       
  fi
else
  DEFECT=tstdef1
fi

defacccmd="Defect -accept $DEFECT -answer program_defect"
print $defacccmd
if [[ $SIMULATE != "yes" ]]; then
  $defacccmd
  if [[ $? -eq 0 ]] ; then
    print "Defect accepted for defect $DEFECT"
  else 
    print "Failure accepting defect $DEFECT!!!"
   return 1                                       
  fi
fi

trackcmd="Track -create -defect $DEFECT -release $RELEASE -target $RELEASE"
print $trackcmd
if [[ $SIMULATE != "yes" ]]; then
  $trackcmd
  if [[ $? -eq 0 ]] ; then
    print "Track created for defect $DEFECT"
  else 
    print "Track creation failure for defect $DEFECT!!!"
   return 1                                       
  fi
fi

#check out the files to be updated  
filecocmd="File  -checkout doc/txts/fixes.txt  -release $RELEASE -defect $DEFECT -relative $README_DIR"
print $filecocmd
if [[ $SIMULATE != "yes" ]]; then
  $filecocmd
  if [[ $? -eq 0 ]] ; then
    print "Files checked out for defect $DEFECT"
  else 
    print "Failure checking out files!!!"
    return 1                                       
  fi
fi

LEVEL_MEMBERS=`Report -view levelMemberView -where "releaseName in ('$RELEASE') and levelName in ('$LEVEL')" -raw | awk -F'|' '{print $3}'`

if [[ $SIMULATE != "yes" ]]; then
  rm -f $TRACKS_LIST
  print "Fixes in ODE${RELEASE_NUM##i} build $LEVEL:" > $TRACKS_LIST
  for _defects in $LEVEL_MEMBERS; do
    Report -view defectView -where "name in ('$_defects') and compName not in ('odetest','odeinternal','odescripts')" -raw | awk -F'|' '{printf "  %-8.8s   %s\n",$3,$9}' >> $TRACKS_LIST
  done
  /bin/sort -b -o$TRACKS_LIST $TRACKS_LIST
else
  print "Would have added *$LEVEL_MEMBERS* to $TRACKS_LIST"
fi

# Now update the fixes.txt file
perlcmd="perl $BB_DIR/latest/src/scripts/bld/odereadme.pl"
print $perlcmd
if [[ $SIMULATE != "yes" ]]; then
  $perlcmd
  if [[ $? -eq 0 ]] ; then
    print "fixes.txt updated for defect $DEFECT"
  else 
    print "Failure updating fixes.txt!!!"
    return 1                                       
  fi
else
  print "Would have updated fixes with tracks"
fi

# Check in the files to be updated  
filecicmd="File -checkin doc/txts/fixes.txt -force -defect $DEFECT -release $RELEASE -relative $README_DIR"
print $filecicmd
if [[ $SIMULATE != "yes" ]]; then
  $filecicmd
  if [[ $? -eq 0 ]] ; then
    print "fixes.txt checked in for defect $DEFECT"
  else 
    print "Failure checking in fixesp.txt!!!"
    return 1                                       
  fi
fi

fixcmd="Fix -complete -defect $DEFECT -component odedoc -release $RELEASE"
print $fixcmd
if [[ $SIMULATE != "yes" ]]; then
  $fixcmd
  if [[ $? -eq 0 ]] ; then
    print "Fix records completed for defect $DEFECT"
  else 
    print "Failure completing fix records for defect $DEFECT!!!"
    return 1                                       
  fi
fi

levmemcmd="LevelMember -create -defect $DEFECT -level $LEVEL -release $RELEASE"
print $levmemcmd
if [[ $SIMULATE != "yes" ]]; then
  $levmemcmd
  if [[ $? -eq 0 ]] ; then
    print "LevelMember defect $DEFECT added to level $LEVEL successfully"
  else 
    print "LevelMember defect $DEFECT NOT added to level $LEVEL!!!"
    return 1                                       
  fi
fi

return 0

