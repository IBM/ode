#################################################
#
# RulesTargetsTest_rmtargetall module
# - module to test predefined target rmtarget_all
#
#################################################

package RulesTargetsTest_rmtargetall;

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

##########################################
#
# sub run
#
##########################################
sub run ()
{
  my $status;
  OdeInterface::logPrint( "in RulesTargetsTest_rmtargetall\n" );
  $status = test_rmtargetall();
  return $status;
}

##########################################
#
# sub test_rmtargetall
# - to test predefined target rmtarget_all
#
##########################################
sub test_rmtargetall()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $obj_dir = join( $OdeEnv::dirsep, 
                      ($OdeTest::test_sb, "obj", $OdeEnv::machine) );
  my $export_dir = join( $OdeEnv::dirsep, 
                         ($OdeTest::test_sb, "export", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;
  my $tfile1;
  my $tfile2;

  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") );
  OdePath::chgdir( $tmpstr );

  ## need a fully built sandbox for testing clean_all perfectly
  ## running build build_all -a
  $command = "build -rc $OdeTest::test_sb_rc build_all -a"
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    OdeInterface::logPrint( "build returned a non-zero exit code\n" );
  }

  $tfile1 = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  $tfile2 = join( $OdeEnv::dirsep, ($obj_dir, "bin", "client") );
  if ((! -f $tfile1 . $OdeEnv::dirsep . "server" . $OdeEnv::prog_suff) ||
      (! -f $tfile2 . $OdeEnv::dirsep . "client" . $OdeEnv::prog_suff))
  {
    OdeInterface::logPrint( "an exe file doesn't exist in $tfile1 or $tfile2" );
  }

  ## running build rmtarget_all
  $command = "build -rc $OdeTest::test_sb_rc rmtarget_all"
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "lib") );
  if (!$rules_error && 
       ((-f $tfile1 . $OdeEnv::dirsep . "server" . $OdeEnv::prog_suff) ||
        (-f $tfile1 . $OdeEnv::dirsep . "retserver" . $OdeEnv::prog_suff) ||
        (-f $tfile2 . $OdeEnv::dirsep . "client" . $OdeEnv::prog_suff) ||
        (-f $tmpstr . $OdeEnv::dirsep . "printmsg" . $OdeEnv::prog_suff)))
  {
    $rules_error = 1;
    $error_txt = "an executable not deleted in $tfile1 or $tfile2 or $tmpstr";
  }
  if (!$rules_error && 
       ((! -f $tfile1 . $OdeEnv::dirsep . "depend.mk") ||
        (! -f $tfile1 . $OdeEnv::dirsep . "server" . $OdeEnv::obj_suff) ||
        (! -f $tfile1 . $OdeEnv::dirsep . "retserver" . $OdeEnv::obj_suff) ||
        (! -f $tfile2 . $OdeEnv::dirsep . "depend.mk") ||
        (! -f $tfile2 . $OdeEnv::dirsep . "client" . $OdeEnv::obj_suff) ||
        (! -f $tmpstr . $OdeEnv::dirsep . "printmsg" . $OdeEnv::obj_suff) ||
        (! -f $tmpstr . $OdeEnv::dirsep . "depend.mk")))
  {
    $rules_error = 1;
    $error_txt = "an $OdeEnv::obj_suff file or depend.mk missing " .
                 "in $tfile1 or $tfile2 or $tmpstr";
  }
  if (defined( $OdeEnv::UNIX ))
  {
    $tmpstr .= $OdeEnv::dirsep . "libexa.a";
    $tfile1 = join( $OdeEnv::dirsep, ($export_dir, "usr", "lib", "libexa.a") );
    $tfile2 = join( $OdeEnv::dirsep, 
                    ($export_dir, "usr", "include", "server.h") );
    if (!$rules_error && (-l $tfile1))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not deleted";
    }
    if (!$rules_error && (! -l $tfile2))
    {
      $rules_error = 1;
      $error_txt = "$tfile2 doesn't exist";
    }
  }
  else
  {
    $tmpstr .= $OdeEnv::dirsep . "exa.lib";
    $tfile1 = join( $OdeEnv::dirsep, ($export_dir, "usr", "lib", "exa.lib") );
    $tfile2 = join( $OdeEnv::dirsep, 
                    ($export_dir, "usr", "include", "server.h") );
    if (!$rules_error && (-f $tfile1))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not deleted";
    }
    if (!$rules_error && (! -f $tfile2))
    {
      $rules_error = 1;
      $error_txt = "$tfile2 doesn't exist";
    }
  }
  $tfile1 = join( $OdeEnv::dirsep, 
                  (dirname( $export_dir ), "classes", "MakeMake.jar") );
  if (!$rules_error && ((-f $tmpstr) || (-f $tfile1)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr or $tfile1 not deleted";
  }
  $tfile1 = join( $OdeEnv::dirsep, 
                  (dirname( $tfile1 ), "COM", "ibm", "makemake") );
  if (!$rules_error && 
       ((OdeFile::numDirEntries( $tfile1 . $OdeEnv::dirsep . "lib" ) == 0) ||
        (OdeFile::numDirEntries( $tfile1 . $OdeEnv::dirsep . "bin" ) == 0)))
  {
    $rules_error = 1;
    $error_txt = "directories lib or bin in $tfile1 is empty";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target rmtarget_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target rmtarget_all" );
  }
  OdePath::chgdir( $dir );
  return $error;
}

1;
