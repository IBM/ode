################################################
#
# RulesTargetsTest_clobberall module
# - module to test predefined target clobber_all
#
################################################

package RulesTargetsTest_clobberall;

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
  OdeInterface::logPrint( "in RulesTargetsTest_clobberall\n" );
  $status = test_clobberall();
  return $status;
}

#########################################
#
# sub test_clobberall
# - to test predefined target clobber_all
#
#########################################
sub test_clobberall()
{
  my $status;
  my $error = 0;
  my $command;
  my $obj_dir = join( $OdeEnv::dirsep, 
                      ($OdeTest::test_sb, "obj", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;
  my $tfile1;
  my $tfile2;

  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") );
  OdePath::chgdir( $tmpstr );

  ## need a fully built sandbox for testing clobber_all perfectly
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

  ## running build clobber_all
  $command = "build -rc $OdeTest::test_sb_rc clobber_all"
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = run_test( $command, 1 );
  if (!$error) { $error = $status; }

  ## running build clobber_all -a
  ## -a should not make any difference
  $command = "build -rc $OdeTest::test_sb_rc clobber_all -a"
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = run_test( $command, 2 );
  if (!$error) { $error = $status; }

  OdePath::chgdir( $dir );
  return $error;
}

#########################################
#
# sub run_test
# - to run the command and do the tests
#
# arguments:
# command - the command to be run
# testno  - the test number
#
#########################################
sub run_test( $$ )
{
  my ($command, $testno) = @_;
  my $status;
  my $rules_error = 0;
  my $error_txt;
  my $obj_dir = join( $OdeEnv::dirsep, 
                      ($OdeTest::test_sb, "obj", $OdeEnv::machine) );
  my $export_dir = join( $OdeEnv::dirsep, 
                         ($OdeTest::test_sb, "export", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;
  my $tfile1 = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  my $tfile2 = join( $OdeEnv::dirsep, ($obj_dir, "bin", "client") );
  my $filecount1;
  my $filecount2;
  my $filecount3;

  if (defined( $OdeEnv::MVS ))
  {
    $filecount1 = 6;
    $filecount2 = 7;
    $filecount3 = 2;
  }
  else
  {
    $filecount1 = 0;
    $filecount2 = 0;
    $filecount3 = 0;
  }

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
        (-f $tfile1 . $OdeEnv::dirsep . "server" . $OdeEnv::obj_suff) ||
        (-f $tfile1 . $OdeEnv::dirsep . "retserver" . $OdeEnv::obj_suff) ||
        (-f $tfile1 . $OdeEnv::dirsep . "depend.mk") ||
        (-f $tfile2 . $OdeEnv::dirsep . "client" . $OdeEnv::prog_suff) ||
        (-f $tfile2 . $OdeEnv::dirsep . "client" . $OdeEnv::obj_suff) ||
        (-f $tfile2 . $OdeEnv::dirsep . "depend.mk") ||
        (-f $tmpstr . $OdeEnv::dirsep . "printmsg" . $OdeEnv::prog_suff) ||
        (-f $tmpstr . $OdeEnv::dirsep . "printmsg" . $OdeEnv::obj_suff) ||
        (-f $tmpstr . $OdeEnv::dirsep . "depend.mk")))
  {
    $rules_error = 1;
    $error_txt = "a file not deleted in $tfile1 or $tfile2 or $tmpstr";
  }
  if (!$rules_error && ((OdeFile::numDirEntries( $tfile1 ) != $filecount1) ||
                        (OdeFile::numDirEntries( $tfile2 ) != $filecount2) ||
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount3)))
  {
    $rules_error = 1;
    $error_txt = "all files not deleted in $tfile1 or $tfile2 or $tmpstr";
  }

  if (defined( $OdeEnv::UNIX ))
  {
    $tmpstr .= $OdeEnv::dirsep . "libexa.a";
    $tfile1 = join( $OdeEnv::dirsep, ($export_dir, "usr", "lib", "libexa.a") );
    $tfile2 = join( $OdeEnv::dirsep, 
                    ($export_dir, "usr", "include", "server.h") );
    if (!$rules_error && ((-l $tfile1) || (-l $tfile2)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 or $tfile2 not deleted";
    }
  }
  else
  {
    $tmpstr .= $OdeEnv::dirsep . "exa.lib";
    $tfile1 = join( $OdeEnv::dirsep, ($export_dir, "usr", "lib", "exa.lib") );
    $tfile2 = join( $OdeEnv::dirsep, 
                    ($export_dir, "usr", "include", "server.h") );
    if (!$rules_error && ((-f $tfile1) || (-f $tfile2))) 
    {
      $rules_error = 1;
      $error_txt = "$tfile1 or $tfile2 not deleted";
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
       (OdeFile::numDirEntries( $tfile1 . $OdeEnv::dirsep . "lib" ) == 0))
  {
    ## the internal class files do not get deleted in lib
    $rules_error = 1;
    $error_txt = "lib directory in $tfile1 is empty";
  }
  if (!$rules_error && 
        (OdeFile::numDirEntries( $tfile1 . $OdeEnv::dirsep . "bin" ) != 0))
  {
    $rules_error = 1;
    $error_txt = "all files in bin directory of $tfile1 not removed";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               $testno, "predefined target clobber_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               $testno, "predefined target clobber_all" );
  }
  return $rules_error;
}

1;
