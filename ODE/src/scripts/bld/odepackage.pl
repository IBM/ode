# basic script for packaging ODE

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

# Determine context for logfile name
print "OS level=$^O\n";
if ($^O eq MSWin32)
{
    $context="x86_nt_4";
}
else
{
    print "Packaging on *$^O* is not supported\n";
    exit(1);
}

$BB_BASE="$BUILD_ROOT\\$RELEASE_NUM\\$LEVEL";

if ($context eq "x86_nt_4")
{
  $classpath="$BUILD_ROOT\\$RELEASE_NUM\\$LEVEL\\inst.images\\ship\\ode${RELEASE_NUM}_b${LEVEL}_tools.jar";
  $path="$BUILD_ROOT\\$RELEASE_NUM\\$LEVEL\\inst.images\\${context}\\bin\\";
  $pathsep = ";";

  $LOGFILE="$BB_BASE\\logs\\${context}.pkglog";
  $buildcmd="build -rc $BUILD_ROOT\\$RELEASE_NUM\\$SBRC -sb $LEVEL -k /runpkgtool_all > $LOGFILE 2>&1";
  print "$buildcmd\n";
  if ( $SIMULATE ne "yes" )
  {
    $ENV{'PATH'} = $path . $pathsep . $ENV{'PATH'};
    $ENV{'CLASSPATH'} = $classpath . $pathsep . $ENV{'CLASSPATH'};

    $status = system( "cmd", "/c", $buildcmd );
    if ($status != 0)
    {
      print "Level *$LEVEL* package FAILED for $RELEASE\n";
      exit(1);
    }
  }
}

print "Level *$LEVEL* packageD for $RELEASE\n";
exit(0);

