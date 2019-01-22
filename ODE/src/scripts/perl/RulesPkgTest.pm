###########################################
#
# RulesPkgTest module
# - module to test packaging rules
#
###########################################

package RulesPkgTest;

use File::Path;
use File::Basename;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdePath;
use OdeUtil;
use OdeInterface;
use strict;
use Cwd;

use Pkgtoolstest;                    # To get sub platform_supported()
use RulesPkgTest_packageall;
use RulesPkgTest_buildandpackageall;
use RulesPkgTest_parseall;
use RulesPkgTest_runpkgtoolall;
use RulesPkgTest_gatherall;

#########################################
#
# sub run
#
#########################################
sub run ()
{
  my $status;
  my $error = 0;

  OdeInterface::logPrint( "in RulesPkgTest\n");
  if (!Pkgtoolstest::platform_supported()) 
  {
    OdeInterface::logPrint( 
          "\nPackaging not supported on `$OdeEnv::machine'\n" );
    return 0;
  }
  if (defined( $OdeEnv::MVS ))
  {
    OdeInterface::logPrint( "\nTesting for MVS will be implemented later\n" );
    return 0;
  }

  ## testing predefined action parse_all
  $status = RulesPkgTest_parseall::run();
  if (!$error) { $error = $status; }

  ## testing predefined action runpkgtool_all
  ## this test is avoided on HP so as to avoid the problem of creating files
  ## owned by root
  if (!defined( $OdeEnv::HP ))
  {
    $status = RulesPkgTest_runpkgtoolall::run();
    if (!$error) { $error = $status; }
  }

  ## testing predefined action gather_all
  $status = RulesPkgTest_gatherall::run();

  if ( $OdeUtil::arghash{'testlevel'} eq "fvt" )
  {
    ## testing predefined action package_all
    $status = RulesPkgTest_packageall::run();
    if (!$error) { $error = $status; }

    ## testing predefined action buildandpackage_all
    $status = RulesPkgTest_buildandpackageall::run();
    if (!$error) { $error = $status; }
  }

  return $error;
}

1;
