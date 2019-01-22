###########################################
#
# RulesTargetsTest module
# - module to test predefined targets
#
###########################################

package RulesTargetsTest;

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

use RulesTargetsTest_bldall;
use RulesTargetsTest_compall;
use RulesTargetsTest_dependall;
use RulesTargetsTest_exportall;
use RulesTargetsTest_installall;
use RulesTargetsTest_javadocall;
use RulesTargetsTest_rmtargetall;
use RulesTargetsTest_cleanall;
use RulesTargetsTest_clobberall;
use RulesTargetsTest_setupall;

#########################################
#
# sub run
#
#########################################
sub run ()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $bb_dir;
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb_rc, 
                     $OdeTest::tmpfile);

  OdeInterface::logPrint( "in RulesTargetsTest\n");

  if (-d $OdeTest::test_sb)
  {
    OdeFile::deltree( $OdeTest::test_sb );
  }
  $bb_dir = $OdeEnv::tempdir . $OdeEnv::dirsep . "bbexample";
  ## creating the sandbox
  $command = "mksb -auto -back $bb_dir -dir $OdeTest::test_sbdir"
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine -auto "
             . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $error_txt = "mksb returned a non-zero exit code";
  }
  if (!$status)
  {
    $rules_error = OdeTest::validateSandbox( $OdeTest::test_sb, 
                                         $OdeTest::test_sb_rc, \$error_txt );
  }
  if ($status || $rules_error) 
  {
    # Test case failed
    OdeInterface::printError( 
          "rules Predefined Targets Test : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  ## Testing predefined targetes
  ## testing build_all
  $status = RulesTargetsTest_bldall::run();
  if (!$error) { $error = $status; }

  ## testing export_all
  $status = RulesTargetsTest_exportall::run();
  if (!$error) { $error = $status; }

  ## testing install_all
  $status = RulesTargetsTest_installall::run();
  if (!$error) { $error = $status; }

  ## testing depend_all
  $status = RulesTargetsTest_dependall::run();
  if (!$error) { $error = $status; }

  ## testing comp_all
  $status = RulesTargetsTest_compall::run();
  if (!$error) { $error = $status; }

  ## testing javadoc_all
  $status = RulesTargetsTest_javadocall::run();
  if (!$error) { $error = $status; }

  ## testing rmtarget_all
  $status = RulesTargetsTest_rmtargetall::run();
  if (!$error) { $error = $status; }

  ## testing clean_all
  $status = RulesTargetsTest_cleanall::run();
  if (!$error) { $error = $status; }

  ## testing clobber_all
  $status = RulesTargetsTest_clobberall::run();
  if (!$error) { $error = $status; }

  ## testing setup_all
  $status = RulesTargetsTest_setupall::run();
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );
  return $error;
}

1;
