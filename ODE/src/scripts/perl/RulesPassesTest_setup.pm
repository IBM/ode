###############################################
#
# RulesPassesTest_setup module
# - module to test predefined pass SETUP
#   Just tests if the files are copied properly
#
###############################################

package RulesPassesTest_setup;

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

#########################################
#
# sub run
#
#########################################
sub run ()
{
  OdeInterface::logPrint( "in RulesPassesTest_setup\n" );
  my $status = test_setup();
  return $status;
}

#########################################
#
# sub test_setup
# - to test predefined pass SETUP
#
#########################################
sub test_setup()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $tfile1;
  my $tfile2;
  my $setupdir =  "/setup/";
  my $makefile;
  my $bb_dir;
  my $tools_dir = join( $OdeEnv::dirsep, 
                      ($OdeTest::test_sb, "tools", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb_rc);

  if (-d $OdeTest::test_sb)
  {
    OdeFile::deltree( \@delete_list );
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

  $tfile1 = $tools_dir . $setupdir . "crap1";
  $tfile2 = $tools_dir . $setupdir . "crap2";
  $makefile = join( $OdeEnv::dirsep, 
                    ($OdeTest::test_sb, "src", "makefile") );
  OdeFile::deltree( $tfile1 );
  OdeFile::deltree( $tfile2 );
  if ((-f $tfile1) || (-f $tfile2))
  {
    OdeInterface::logPrint( "$tfile1 or $tfile2 not deleted\n" );
  }
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") );
  OdePath::chgdir( $tmpstr );

  $tfile1 = $tmpstr . $OdeEnv::dirsep . "crap1";
  open( INPUT, "> $tfile1" ) ||
    OdeInterface::logPrint( "Could not open file $tfile1\n" ); 
  print( INPUT "crap1\n" );
  close( INPUT );

  $tfile2 = $tmpstr . $OdeEnv::dirsep . "crap2";
  open( INPUT, "> $tfile2" ) ||
    OdeInterface::logPrint( "Could not open file $tfile2\n" ); 
  print( INPUT "crap2\n" );
  close( INPUT );

  if ((! -f $tfile1) || (! -f $tfile2))
  {
    OdeInterface::logPrint( "$tfile1 or $tfile2 does not exist\n" );
  }
  open( INPUT, "> $makefile" ) ||
    OdeInterface::logPrint( "Could not open file $makefile\n" ); 
  print( INPUT "SETUP_SCRIPTS=crap1\n" );
  print( INPUT "SETUP_PROGRAMS=$tfile2\n" );
  print( INPUT "SETUPDIR=$setupdir\n" );
  print( INPUT ".include<\${RULES_MK}>\n" );
  close( INPUT );
  ## running build MAKEFILE_PASS=SETUP setup_all -fmakefile
  ## can't use MAKEFILE_PASS=SETUP with out setup_all
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=SETUP "
             . "setup_all -f$makefile >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tfile1 = $tools_dir . $setupdir . "crap1";
  $tfile2 = $tools_dir . $setupdir . "crap2";
  if (!$rules_error && ((! -f $tfile1) || (! -f $tfile2)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 not created";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined pass SETUP" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined pass SETUP" );
  }

  OdePath::chgdir( $dir );
  OdeFile::deltree( \@delete_list );
  return $error;
}

1;
