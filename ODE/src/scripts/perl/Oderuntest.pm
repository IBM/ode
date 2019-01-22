#!/usr/bin/perl

package Oderuntest;

use File::Path;
use File::Copy;
use OdeEnv;
use OdeTest;
use OdeUtil;
use OdeInterface;
use Mksbtest;
use Mkbbtest;
use Buildtest;
use Mkpathtest;
use Resbtest;
use Workontest;
use Currentsbtest;
use Sbinfotest;
use Sblstest;
use Gendeptest;
use Mkdeptest;
use Mklinkstest;
use Genpathtest;
use Mktest;
use Rulestest;
use Pkgtoolstest;

my %test_subs = (
  'Mksbtest::run'      => 0,
  'Mkbbtest::run'      => 0,
  'Mkpathtest::run'    => 0,
  'Resbtest::run'      => 0,
  'Workontest::run'    => 0,
  'Currentsbtest::run' => 0,
  'Sbinfotest::run'    => 0,
  'Sblstest::run'      => 0,
  'Gendeptest::run'    => 0,
  'Mkdeptest::run'     => 0,
  'Mklinkstest::run'   => 0,
  'Genpathtest::run'   => 0,
  'Buildtest::run'     => 0,
  'Mktest::run'        => 0,
  'Rulestest::run'     => 0,
  'Pkgtoolstest::run'  => 0
  );

#########################################
#
# sub run
#
#########################################
sub run ()
{ 
  #### Set up global test variables ####
  OdeTest::setTestVars();

  return( run_tests( \%test_subs ) );
}

################################################################################
# sub run_tests
# - runs the subroutines given in the has
#   and prints status of each
sub run_tests ( \% )
{
  my $status = 0;
  my %tests;
  # Don't know of a better way to get the 1 % arg.
  foreach my $params (@_) 
  { %tests = %$params; }
  # Run all the subroutines storing there return value
  foreach my $key (keys %tests)
  {
    $tests{ $key } = &$key();
  } 
  # Print a summary of the results after all the tests have been run
  foreach my $key (keys %tests)
  {
    if ($tests{ $key })
    {
      OdeInterface::logPrint( "  " . $key . "() - test failed\n" );
      $status = 1;
    }
  } 
  if ($status == 0)
  {
    OdeInterface::logPrint( 
      "All " . $OdeUtil::arghash{'testlevel'} . " tests passed!\n");
  }
  return( $status );
}

1;
