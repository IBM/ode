#!/usr/bin/perl

#BEGIN block and FindBin are used to be able to run this script from anywhere by
#specifying fullpath to it and to get along without specifying -I on OS2 and MVS

BEGIN
{
  if ($^O eq os2)
  {
    push( @INC, "O:\\tools\\i386_os2\\perl\\lib" );
    push( @INC, "O:\\tools\\i386_os2\\perl\\lib\\5.00553\\os2" );
    push( @INC, "O:\\tools\\i386_os2\\perl\\lib\\5.00553" );
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
use OdeEnv;
use OdeTest;
use OdeUtil;
use OdeInterface;
use Oderuntest;

################################################
#
# Perl script to run ode test scripts
# - this script will initialzie all platform 
#   contants, and create a bbexample backing build
#   that will be used by the test scripts.
#   The test scripts in Oderuntest() will then
#   be callee
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
my $tmpvar;

print( "\n\tRunning ODE Test Scripts...\n\n" );

#### Parse input command line ####
$status = OdeUtil::parseCommandLine( );
if ($status) { die "Exiting ....\n" };

#### Initialize all platform-specific variables ####
OdeEnv::initPlatform();

#### Set paths to ode binaries ####
$status = OdeEnv::setPaths();
if ($status) 
{ 
  OdeFile::deltree( $OdeEnv::tempdir );
  die "Exiting ....\n" 
}

#### copy bbexample to home/machine only if bbdir is not defined
#### else copy into bbdir if it doesn't have bbexample
if (!$OdeEnv::bbex_dir)
{
  #### delete existing bbexample if necessary ####
  $mybbdir = $OdeEnv::homedir . "bbexample";
  if (-d "$mybbdir")
  {
    OdeInterface::logPrint( "Deleting a bbexample in $OdeEnv::homedir\n" );
    OdeFile::deltree( $mybbdir );
  }
  ## creates the bbexample
  OdeEnv::createBbExample( $OdeEnv::homedir );
}
elsif (! -d $OdeEnv::bbex_dir . $OdeEnv::dirsep . "bbexample")
{
  OdeEnv::createBbExample( $OdeEnv::bbex_dir );
}

OdeInterface::logPrint( 
                "\nOutputing build info to log file : $OdeEnv::bldcurr \n" );

# Write current time and date to log files
$nowdate = OdeUtil::getCurrentDate();
$nowtime = OdeUtil::getCurrentTime();
open( BLDLOG, "> $OdeEnv::bldcurr" );
print( BLDLOG "Test started at: $nowtime on $nowdate\n\n" );
close( BLDLOG );

#### Run main perl test script - Oderuntest ####
$status = Oderuntest::run( );
if ($status)
{
  OdeInterface::logPrint( 
    "RESULT: odetest FAILED during $OdeUtil::arghash{'testlevel'} testing\n" );
}
else
{
  OdeInterface::logPrint( 
    "RESULT: odetest PASSED during $OdeUtil::arghash{'testlevel'} testing\n" );
}
OdeInterface::logPrint( "See $OdeEnv::bldcurr for more details.\n" );

# Delete temp working directory
OdeFile::deltree( $OdeEnv::tempdir );

# Write current time and date to log files
$nowdate = OdeUtil::getCurrentDate();
$nowtime = OdeUtil::getCurrentTime();
open( BLDLOG, ">> $OdeEnv::bldcurr" );
print( BLDLOG "Test ended at: $nowtime on $nowdate\n" );
close( BLDLOG );

exit( $status );

