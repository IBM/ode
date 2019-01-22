###############################################
#
# RulesTargetsTest_setupall module
# - module to test predefined target setup_all
#   Just tests if the files are copied properly
#
###############################################

package RulesTargetsTest_setupall;

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
  my $status;
  OdeInterface::logPrint( "in RulesTargetsTest_setupall\n" );
  $status = test_setupall();
  return $status;
}

#########################################
#
# sub test_setupall
# - to test predefined target setup_all
#
#########################################
sub test_setupall()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $tfile1;
  my $tfile2;
  my $setupdir = "/setup/";
  my $makefile;
  my $tools_dir = join( $OdeEnv::dirsep, 
                      ($OdeTest::test_sb, "tools", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;

  $tfile1 = $tools_dir . $setupdir . "crap1";
  $tfile2 = $tools_dir . $setupdir . "crap2";
  $makefile = join( $OdeEnv::dirsep, 
                    ($OdeTest::test_sb, "src", "makefile") );
  OdeFile::deltree( $tfile1 );
  OdeFile::deltree( $tfile2 );
  if ((-f $tfile1) || (-f $tfile2))
  {
    OdeInterface::logPrint( 
               "$tfile1 or $tfile2 not deleted\n" );
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
    OdeInterface::logPrint( 
               "$tfile1 or $tfile2 does not exist\n" );
  }
  open( INPUT, "> $makefile" ) ||
    OdeInterface::logPrint( "Could not open file $makefile\n" ); 
  print( INPUT "SETUP_SCRIPTS=crap1\n" );
  print( INPUT "SETUP_PROGRAMS=$tfile2\n" );
  print( INPUT "SETUPDIR=$setupdir\n" );
  print( INPUT ".include<\${RULES_MK}>\n" );
  close( INPUT );
  ## running build setup_all -fmakefile
  $command = "build -rc $OdeTest::test_sb_rc setup_all "
             . "-f$makefile >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tfile1 = $tools_dir . $setupdir . "crap1";
  $tfile2 = $tools_dir . $setupdir . "crap2";
  if ((! -f $tfile1) || (! -f $tfile2))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 not created";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target setup_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target setup_all" );
  }

  OdeFile::deltree( $tfile1 );
  OdeFile::deltree( $tfile2 );
  if ((-f $tfile1) || (-f $tfile2))
  {
    OdeInterface::logPrint( "$tfile1 or $tfile2 not deleted\n" );
  }

  ## running build setup_all MAKEFILE_PASS=crap -fmakefile
  ## should not do anything
  $command = "build -rc $OdeTest::test_sb_rc setup_all "
             . "MAKEFILE_PASS=crap -f$makefile >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if ((-f $tfile1) || (-f $tfile2))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 created";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target setup_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target setup_all" );
  }

  OdePath::chgdir( $dir );
  return $error;
}

1;
