#basic script for building, installing and zipping ODE

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

# Determine context for logfile name
print "OS level=$^O\n";
if ($^O eq os2)
{
    $context="x86_os2_4";
}
elsif ($^O eq MSWin32)
{
    $context="x86_nt_4";
}
else
{
    print "Context *$^O* is not supported\n";
    exit(1);
}

$BUILD_ROOT="o:\\build";
$BB_BASE="$BUILD_ROOT\\$RELEASE_NUM\\$LEVEL";
$SCRIPTDIR="$BB_BASE\\src\\scripts\\perl";
if (defined($NO_DFS))
{
  $LOGDIR="d:\\build\\$RELEASE_NUM\\$LEVEL\\logs";
}
else
{
  $LOGDIR="$BB_BASE\\logs";
}

$testcmd="cd $SCRIPTDIR && perl testbld.pl release=$RELEASE_NUM build=$LEVEL testlevel=regression logdir=$LOGDIR";
print "$testcmd\n";
if ($SIMULATE ne "yes")
{
  if ($context eq "x86_nt_4")
  {
    $status = system( "cmd", "/c", $testcmd );
  }
  elsif ($context eq "x86_os2_4")
  {
    $status = system( "cmd", "/c", $testcmd );
  }
}
if ($status == 0)
{
  print "Level *$LEVEL* test scripts PASSED for $RELEASE\n";
  exit(0);
}
else 
{
  print "Level *$LEVEL* test scripts FAILED for $RELEASE\n";
  exit(1);
}

