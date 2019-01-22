#basic script for building, installing and zipping ODE

open( RELFILE, "< release.name" ) || die "Couldn't open release.name";
$RELEASE=readline( *RELFILE );
close( RELFILE );
open( RELNUMFILE, "< release.num" ) || die "Couldn't open release.num";
$RELEASE_NUM=readline( *RELNUMFILE );
close( RELNUMFILE );
open( LVLFILE, "< build.name" ) || die "Couldn't open build.name";
$LEVEL=readline( *LVLFILE );
close( LVLFILE );

#remove end of line character from the strings read from file
chop $RELEASE;
chop $RELEASE_NUM;
chop $LEVEL;

# Get the environment variable and set to a local variable
$SIMULATE = $ENV{'SIMULATE'};

$BUILD_ROOT = "d:/build";

# Determine the parameters to pass to the 'build' command
if (@ARGV)
{
  $bld_args = join( ' ', @ARGV );
}
else
{
  $bld_args = "/unjarXMLParser_all /build_all /install_all /zip_all";
}

# Determine context for logfile name
print "OS level=$^O\n";
if ($^O eq MSWin32)
{
    $context="x86_nt_4";
}
else
{
    die "Context $^O is not supported yet\n";
}

$BB_BASE="$BUILD_ROOT/$RELEASE_NUM/$LEVEL";

if ($context eq "x86_nt_4")
{
  $_ = "$BB_BASE/logs/$context.bldlog";
  s#/#\\#g; # so strings given directly to shell must use backslashes
  $LOGFILE = $_;
  $buildcmd = "build -sb $LEVEL $bld_args >$LOGFILE 2>&1";

  if (!$SIMULATE)
  {
    print "Running: $buildcmd\n";
    mkdir($BB_BASE/logs);
    system( "cmd", "/c", $buildcmd ) &&
      die "Level *$LEVEL* build FAILED for $RELEASE\n";
  }
  else
  {
    print "SIMULATE: would have run: $buildcmd\n";
  }
}

print "Level *$LEVEL* built for $RELEASE\n";

exit;
