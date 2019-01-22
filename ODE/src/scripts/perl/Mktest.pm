#########################################
#
# Mktest module
# - module to run mktest for different
#   test levels
#
#########################################

package Mktest;

use File::Path;
use File::Copy;
use File::Basename;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdeUtil;
use OdeInterface;
# Since we use subroutine references we can't use strict
#use strict;

use MkVarModTest;
use MkConditionalTest;
use MkRTConditionalTest;
use MkRTMkdepCmdTest;
use MkIncludeTest;
use MkSuffixTest;
use MkVarAssgnTest;
use MkVarPrecTest;
use MkImpVarTest;
use MkSpecSrcTest;
use MkSpecTgtTest;
use MkTargDepTest;
use MkBomTargDepTest;
use MkCmdFlagTest;
use MkNormalTest;

# The regression test subroutines to run.
# key = subroutine name (must be quoted
#       to disable actual execution)
# value = return value of subroutine
my %reg_tests = (
  'MkVarModTest::run'      => 0,
  'MkConditionalTest::run' => 0,
  'MkRTConditionalTest::run' => 0,
  'MkRTMkdepCmdTest::run'  => 0,
  'MkIncludeTest::run'     => 0,
  'MkSuffixTest::run'      => 0,
  'MkVarAssgnTest::run'    => 0,
  'MkVarPrecTest::run'     => 0,
  'MkImpVarTest::run'      => 0,
  'MkSpecSrcTest::run'     => 0,
  'MkSpecTgtTest::run'     => 0,
  'MkTargDepTest::run'     => 0,
  'MkBomTargDepTest::run'  => 0,
  'MkCmdFlagTest::run'     => 0
);
my %fvt_tests = %reg_tests;
# Add the "normal" test for fvt
$fvt_tests{ 'MkNormalTest::run' } = 0;

################################################################################
# sub run
# - run test sequence based on testlevel
sub run ()
{
  if ( $OdeUtil::arghash{'testlevel'} eq "normal" )
  { return normal();  }
  if ( $OdeUtil::arghash{'testlevel'} eq "regression" )
  { return regression(); }
  elsif ( $OdeUtil::arghash{'testlevel'} eq "fvt" )
  { return fvt(); }
  else
  {
    OdeInterface::logPrint( 
      "ERROR: undefined testlevel " . $OdeUtil::arghash{'testlevel'} . "\n" );
    return( 1 );
  }
}

################################################################################
# sub normal
# - execute normal (minimal) set of tests
sub normal ()    { return (MkNormalTest::run());      }

################################################################################
# sub regression
# - execute thorough but not complete set of tests
sub regression() { return (run_tests( \%reg_tests )); }

################################################################################
# sub fvt
# - execute FVT (complete) set of tests
sub fvt()        { return (run_tests( \%fvt_tests )); }

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
  if ($status)
  {
    OdeInterface::logPrint(
      "  mk - " . $OdeUtil::arghash{'testlevel'} . " tests failed\n");
  }
  else
  {
    OdeInterface::logPrint( 
      "All mk " . $OdeUtil::arghash{'testlevel'} . " tests passed!\n");
  }
  return( $status );
}

1;
