#basic script for building Lemma DB for ODE

open( RELFILE, "< release.name" );
$RELEASE=readline( *RELFILE );
close( RELFILE );
open( RELNUMFILE, "< release.num" );
$RELEASE_NUM=readline( *RELNUMFILE );
close( RELNUMFILE );
open( LVLFILE, "< build.name" );
$LEVEL=readline( *LVLFILE );
close( LVLFILE );

#remove end of line character from the strings read from file
chop $RELEASE;
chop $RELEASE_NUM;
chop $LEVEL;

# Get the environment variable and set to a local variable
$SIMULATE = $ENV{'SIMULATE'};

$BUILD_ROOT = "o:\\build";
$SBRC       = ".sbrc";

$BB_BASE="$BUILD_ROOT\\$RELEASE_NUM";
$LOGFILE="$BB_BASE\\$LEVEL\\logs\\lemma.bldlog";
$buildcmd="build -rc $BB_BASE\\$SBRC -sb $LEVEL /lemma_all > $LOGFILE 2>&1";
print "$buildcmd\n";
if ($SIMULATE ne "yes")
{
  $status = system( "cmd", "/c", $buildcmd );
  if ($status != 0)
  {
    print "Level *$LEVEL* Lemma DB build FAILED for $RELEASE\n";
    exit(1);
  }
}

print "Level *$LEVEL* Lemma DB built for $RELEASE\n";
exit(0);

