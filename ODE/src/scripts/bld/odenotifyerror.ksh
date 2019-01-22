#!/bin/ksh
# This script is used to notify those people listed in the dist_error.lst file
# that the ODE build has an error.

FILETOSEND=build.error
read RELEASE < release.name
read RELEASE_NUM < release.num
read LEVEL < build.name
read DISTLIST < /ode/build/$RELEASE_NUM/dist_error.lst

EMAILOWNER=`sed -e 's; ;|;g' < /ode/build/$RELEASE_NUM/dist_error.lst`

LEVEL=${LEVEL##b}

print "${RELEASE}_b${LEVEL} has an ERROR!" >> $FILETOSEND 2>>$FILETOSEND
print "Check the corresponding BPS Build Document for more information." >> $FILETOSEND 2>>$FILETOSEND
print "Please DO NOT reply to this address." >> $FILETOSEND 2>>$FILETOSEND
print "" >> $FILETOSEND 2>>$FILETOSEND
print "To be removed from the distribution list send email to: $EMAILOWNER" >> $FILETOSEND 2>>$FILETOSEND
print "" >> $FILETOSEND 2>>$FILETOSEND

mail  -s "${RELEASE}_b${LEVEL} ERROR !!!"  $DISTLIST  < $FILETOSEND 

rm -f $FILETOSEND

return 0

