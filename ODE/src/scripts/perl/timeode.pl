#!/usr/bin/perl

#BEGIN block and FindBin are used to be able to run this script from anywhere by
#specifying fullpath to it and to get along without specifying -I on OS2 and MVS

BEGIN
{
  if ($^O eq os2)
  {
    push( @INC, "O:\\tools\\i386_os2\\perl\\lib" );
    push( @INC, "O:\\tools\\i386_os2\\perl\\lib\\os2\\5.00305" );
  }
  elsif ($^O eq os390)
  {
    push( @INC, "/u/ode/bin/perl/lib/perl5");
  }
}
use FindBin;
use lib "$FindBin::Bin/lib";
use lib "$FindBin::Bin";

use strict;
use File::Path;
use File::Copy;
use Cwd;
use OdeEnv;
use OdeUtil;
use OdeFile;
use OdePath;
use OdeInterface;


################################################
#
# Perl script to run mk/build performance stats
#
#################################################

my $status;
my $mybbdir;
my $mybbsrcdir;
my $srcfiles;
my $bb_rc;
my $retval;
my $myrulesdir;
my $nowtime;
my $nowdate;
my $rulesfiles;
my $count;
my $utd_sum;
my $ood_sum;
my @utd_array;
my @ood_array;
my $utd_ave;
my $ood_ave;
my $begintime;
my $endtime;
my $diff_ood;
my $diff_utd;
my $mybbexpdir;
my $mybbobjdir;
my $timeprev;
my $timecurr;
my $orig_dir;

print( "\n\tRunning ODE Timing Scripts...\n\n" );

#### Parse input command line ####
$status = OdeUtil::parseCommandLine( );
if ($status) { die "Exiting ....\n" };

#### Initialize all platform-specific variables ####
OdeEnv::initPlatform();

#### Set paths to ode binaries ####
$status = OdeEnv::setPaths();
if ($status) { die "parseCommandLine failed" };

#### Set paths to bbexample and rules base ####
# use 2.1 bbexample since it won't be changing
# Linux and SCO are only supported for 2.2
if (defined( $OdeEnv::MVS ))
{
  $OdeEnv::bb_base="/u/ode/build/i2.1/latest";
}
elsif (defined( $OdeEnv::AIX ) || 
       defined( $OdeEnv::HP ) || 
       defined( $OdeEnv::SOLARIS ) ||
	 defined( $OdeEnv::SCO))
{
  $OdeEnv::bb_base="/ode/build/i2.1/latest";
}
elsif (defined( $OdeEnv::WIN32 ) || 
       defined( $OdeEnv::OS2 ))
{
  $OdeEnv::bb_base="O:\\build\\i2.1\\latest";
}

#### delete existing bbexample if necessary ####
$mybbdir = $OdeEnv::homedir . "bbexample";
if (-d "$mybbdir")
{
  OdeInterface::logPrint( "Deleting a bbexample in $OdeEnv::homedir\n" );
  OdeFile::deltree( $mybbdir );
}


#### Create backing build ####
$bb_rc = $mybbdir . $OdeEnv::dirsep . ".sbrc";

$retval = 
  OdeEnv::runOdeCommand( 
        "mkbb -rc $bb_rc -dir $OdeEnv::homedir -m $OdeEnv::machine bbexample" );
if ($retval) { die("mkbb failed!"); }
OdeInterface::logPrint( "created backing build ...\n" );

#### Copy files into bbexample ####
# - no Perl API copy function to copy entire directory trees
# - using system copies instead
$srcfiles = join( $OdeEnv::dirsep, 
                      ( $OdeEnv::bb_base, "inst.images", "bbexample", "src" ) );
$mybbsrcdir = $mybbdir . $OdeEnv::dirsep . "src";
$rulesfiles = join( $OdeEnv::dirsep, 
                      ( $OdeEnv::bb_base, "inst.images", "rules_mk" ));
if (defined ($OdeEnv::UNIX))
{
  $retval = OdeEnv::runSystemCommand( "$OdeEnv::copy $srcfiles $mybbdir" );
  if ($retval) { die("copy failed!"); }
  $retval = OdeEnv::runSystemCommand( "$OdeEnv::copy $rulesfiles $mybbsrcdir" );
  if ($retval) { die("copy failed!"); }
  $retval = OdeEnv::runSystemCommand( "chmod -R +w $mybbsrcdir" );
  if ($retval) { die("chmod failed!"); }
  
}
else
{
  $myrulesdir = $mybbsrcdir . $OdeEnv::dirsep . "rules_mk";
  mkpath( $myrulesdir, 1 );
  $retval = OdeEnv::runSystemCommand( "$OdeEnv::copy $srcfiles $mybbsrcdir" );
  if ($retval) { die("copy failed!"); }
  $retval = OdeEnv::runSystemCommand( "$OdeEnv::copy $rulesfiles $myrulesdir" );
  if ($retval) { die("copy failed!"); }
}

OdeInterface::logPrint( "copied files into bbexample ...\n" );

# Timing logs
$timecurr = $OdeEnv::logdir . "$OdeEnv::machine.times";
$timeprev = $OdeEnv::logdir . "$OdeEnv::machine.times.prev";

if (-f "$timeprev")
{
  unlink( $timeprev );
}
if (-f "$timecurr")
{
  copy( $timecurr, $timeprev );
}

OdeInterface::logPrint( 
                "\nOutputing build info to log file : $OdeEnv::bldcurr \n" );
OdeInterface::logPrint( "Outputing timing info to log file : $timecurr \n" );

# Write current time and date to log files

$nowdate = OdeUtil::getCurrentDate();
$nowtime = OdeUtil::getCurrentTime();
open( BLDLOG, "> $OdeEnv::bldcurr" );
open( TIMELOG, "> $timecurr" );
print( BLDLOG "Test started at: $nowtime on $nowdate\n" );
print( TIMELOG "Test started at: $nowtime on $nowdate\n" );
close( BLDLOG );

#### Begin build tests ####
$orig_dir = cwd();
OdePath::chgdir( $mybbsrcdir );

$count = 1;
$ood_sum = 0;
$utd_sum = 0;
$mybbobjdir = 
        $mybbdir . $OdeEnv::dirsep . "obj" . $OdeEnv::dirsep . $OdeEnv::machine;
$mybbexpdir = 
     $mybbdir . $OdeEnv::dirsep . "export" . $OdeEnv::dirsep . $OdeEnv::machine;

do
{
  OdeInterface::logPrint( "Executing test number $count\n" );

## Execute out of date tests
  print( TIMELOG "\nTest $count - all out of date\n" );
  print( TIMELOG "------------------\n" );
  OdeEnv::runOdeCommand( "build -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir" );
  $begintime = time(); # recorded in seconds 
  $retval = OdeEnv::runOdeCommand( 
    "build -rc $bb_rc -sb bbexample -DNO_TOOLSBASE >> $OdeEnv::bldcurr $OdeEnv::stderr_redir" ); 
  $endtime = time();
  if ($retval) { die("Out of date build failed!"); }
  $diff_ood = $endtime - $begintime;
  print( "Test $count out of date time is $diff_ood seconds\n" );
  print( TIMELOG "Test $count out of date time is $diff_ood seconds\n" );
  $ood_array[$count - 1] = $diff_ood;

## Execute up to date tests
  print( TIMELOG "\nTest $count - all up to date\n" );
  print( TIMELOG "------------------\n" );
  $begintime = time();
  $retval = OdeEnv::runOdeCommand( 
    "build -rc $bb_rc -sb bbexample -DNO_TOOLSBASE >> $OdeEnv::bldcurr $OdeEnv::stderr_redir" );
  if ($retval) { die("Up to date build failed!"); }
  $endtime = time();
  $diff_utd = $endtime - $begintime;
  print( "Test $count up to date time is $diff_utd seconds\n" );
  print( TIMELOG "Test $count up to date time is $diff_utd seconds\n\n" );
  $utd_array[$count - 1] = $diff_utd;

# remove obj and export dirs for next out of date test
#  note that ode2.0.1 complains if an obj or export directory
#  does not exist
if (-d $mybbexpdir)
{
  OdeInterface::logPrint( "-- removing export dir ...\n" );
  if (index( $OdeUtil::arghash{'release'}, "i2.0.1" ) >= 0)
  {
    OdeFile::deltree( $mybbexpdir . $OdeEnv::dirsep . "usr" );
  }
  else
  {
    OdeFile::deltree( $mybbexpdir );
  }
}

if (-d $mybbobjdir)
{
  OdeInterface::logPrint( "-- removing obj dir ...\n" );
  if (index( $OdeUtil::arghash{'release'}, "i2.0.1" ) >= 0)
  {
    OdeFile::deltree( $mybbobjdir . $OdeEnv::dirsep . "inc" );
    OdeFile::deltree( $mybbobjdir . $OdeEnv::dirsep . "lib" );
    OdeFile::deltree( $mybbobjdir . $OdeEnv::dirsep . "bin" );
    OdeFile::deltree( $mybbobjdir . $OdeEnv::dirsep . "doc" );
    OdeFile::deltree( $mybbobjdir . $OdeEnv::dirsep . "cmf" );
  }
  else
  {
    OdeFile::deltree( $mybbobjdir );
  }
}

  $count++;
} while($count <= $OdeUtil::arghash{'numtests'});

#### Calculate results ####

# Sort arrays - best to worst
@ood_array = sort { $a <=> $b } @ood_array;
@utd_array = sort { $a <=> $b } @utd_array;

# Keep best $numkeep results
$count = 0;
while( $count < $OdeUtil::arghash{'numkeep'} )
{
  $ood_sum += $ood_array[$count];
  $utd_sum += $utd_array[$count];
  $count++;
}

$ood_ave = sprintf( "%4.2f", ($ood_sum/$OdeUtil::arghash{'numkeep'}) );
$utd_ave = sprintf( "%4.2f", ($utd_sum/$OdeUtil::arghash{'numkeep'}) );

#### Output results ####
OdeInterface::logPrint( "\nOde Version\n" );
print( TIMELOG "\nOde Version\n" );
OdeInterface::logPrint( "  Release: $OdeUtil::arghash{'release'}\n");
print( TIMELOG "  Release: $OdeUtil::arghash{'release'}\n");
OdeInterface::logPrint( "  Build: $OdeUtil::arghash{'build'}\n");
print( TIMELOG "  Build: $OdeUtil::arghash{'build'}\n");

OdeInterface::logPrint( "\nTest Results\n" );
print( TIMELOG "\nTest Results\n" );
OdeInterface::logPrint("-----------------------------------\n" );
print( TIMELOG "-----------------------------------\n" );
OdeInterface::logPrint( "Total tests run: $OdeUtil::arghash{'numtests'}\n" );
print( TIMELOG "Total tests run: $OdeUtil::arghash{'numtests'}\n" );
OdeInterface::logPrint( "Best $OdeUtil::arghash{'numkeep'} tests averaged\n" );
print( TIMELOG "Best $OdeUtil::arghash{'numkeep'} tests averaged\n" );
OdeInterface::logPrint( "Out of date average time = $ood_ave seconds\n" );
print( TIMELOG "Out of date average time = $ood_ave seconds\n" );
OdeInterface::logPrint( "Up to date average time = $utd_ave seconds\n" );
print( TIMELOG "Up to date average time = $utd_ave seconds\n" );

close( TIMELOG );

OdePath::chgdir( $orig_dir );

exit();

