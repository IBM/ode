###############################################
#
# RulesTargetsTest_dependall module
# - module to test predefined target depend_all
#
###############################################

package RulesTargetsTest_dependall;

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
  OdeInterface::logPrint( "in RulesTargetsTest_dependall\n" );
  $status = test_dependall();
  return $status;
}

#########################################
#
# sub test_dependall
# - to test predefined target depend_all
#
#########################################
sub test_dependall()
{
  my ($testno) = @_;
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $mtime1;
  my $mtime2;
  my $tfile1;
  my $tfile2;
  my $obj_dir = join( $OdeEnv::dirsep, 
                      ($OdeTest::test_sb, "obj", $OdeEnv::machine) );
  my $images_dir = join( $OdeEnv::dirsep, 
                         ($OdeTest::test_sb, "inst.images", $OdeEnv::machine) );
  my $tools_dir = join( $OdeEnv::dirsep, 
                        ($OdeTest::test_sb, "tools", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;
  OdeFile::deltree( $images_dir );
  OdeFile::deltree( $tools_dir );
  OdeFile::deltree( $obj_dir );
  if ((-d $images_dir) || (-d $tools_dir) || (-d $obj_dir))
  {
    OdeUtil::logPrint( "$images_dir or $tools_dir or $obj_dir not deleted\n" );
  }
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") );
  OdePath::chgdir( $tmpstr );

  ## running build depend_all
  $command = "build -rc $OdeTest::test_sb_rc depend_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tfile1 = join( $OdeEnv::dirsep, ($obj_dir, "lib") );
  $tfile2 = join( $OdeEnv::dirsep, ($obj_dir, "bin") );
  if (!$rules_error && ((! -d $tfile1) || (! -d $tfile2)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 doesn't exist";
  }
  $tfile2 .= $OdeEnv::dirsep . "client";
  if (!$rules_error && ((! -f $tfile1 . $OdeEnv::dirsep . "depend.mk") ||
                        (! -f $tfile2 . $OdeEnv::dirsep . "depend.mk")))
  {
    $rules_error = 1;
    $error_txt = "depend.mk not created in $tfile1 or $tfile2";
  }
  if (!$rules_error && ((OdeFile::numDirEntries( $tfile1 ) != 1) ||
                        (OdeFile::numDirEntries( $tfile2 ) != 1)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 has incorrect number of files";
  }
  if (!$rules_error && ((-d $tools_dir) || (-d $images_dir)))
  {
    $rules_error = 1;
    $error_txt = "$tools_dir or $images_dir created";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target depend_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target depend_all" );
  }
  OdePath::chgdir( $dir );
  return $error;
}

1;
