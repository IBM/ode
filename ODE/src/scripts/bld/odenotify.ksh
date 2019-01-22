#!/bin/ksh
# This script is used to notify those people listed in the dist.lst file
# that the ODE build has been completed.

FILETOSEND=build.note
read RELEASE < release.name
read RELEASE_NUM < release.num
read LEVEL < build.name
read DISTLIST < /ode/build/$RELEASE_NUM/dist.lst

EMAILOWNER=`sed -e 's; ;|;g' < /ode/build/$RELEASE_NUM/dist_error.lst`

LEVEL=${LEVEL##b}

print "${RELEASE}_b${LEVEL} has been built!" >> $FILETOSEND 2>>$FILETOSEND
print "Latest link has been updated." >> $FILETOSEND 2>>$FILETOSEND
print "" >> $FILETOSEND 2>>$FILETOSEND
print "Please DO NOT reply to this address." >> $FILETOSEND 2>>$FILETOSEND
print "" >> $FILETOSEND 2>>$FILETOSEND
print "To be removed from the distribution list send email to: $EMAILOWNER" >> $FILETOSEND 2>>$FILETOSEND
print "" >> $FILETOSEND 2>>$FILETOSEND

mail  -s "${RELEASE}_b${LEVEL} is done"  $DISTLIST < $FILETOSEND 

rm -f $FILETOSEND

return 0

