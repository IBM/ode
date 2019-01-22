##############################################
#
# RulesTargetsTest_bldall module
# - module to test predefined target build_all
#
##############################################
package RulesTargetsTest_bldall;

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
sub run()
{
  OdeInterface::logPrint( "in RulesTargetsTest_bldall\n" );
  my $status = test_bldall();
  return $status;
}

#########################################
#
# sub test_bldall
# - to test predefined target build_all
#
#########################################
sub test_bldall()
{
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
  my $tools_dir = join( $OdeEnv::dirsep,
                        ($OdeTest::test_sb, "tools", $OdeEnv::machine) );
  my $export_dir = join( $OdeEnv::dirsep,
                         ($OdeTest::test_sb, "export", $OdeEnv::machine) );
  my $images_dir = join( $OdeEnv::dirsep,
                         ($OdeTest::test_sb, "inst.images", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;
  my $filecount1;
  my $filecount2;
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") );
  OdePath::chgdir( $tmpstr );

  if (defined( $OdeEnv::MVS ))
  {
    $filecount1 = 13;
    $filecount2 = 6;
  }
  else
  {
    $filecount1 = 7;
    $filecount2 = 4;
  }
  ## running build build_all
  ## all the files are up to date. Only java files are to be built
  $command = "build -rc $OdeTest::test_sb_rc build_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (!$rules_error && ((! -d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (! -d $obj_dir . $OdeEnv::dirsep . "COM") ||
                        (! -d $obj_dir . $OdeEnv::dirsep . "doc") ||
                        (! -d $obj_dir . $OdeEnv::dirsep . "cmf") ||
                        (! -d $obj_dir . $OdeEnv::dirsep . "bin") ||
                        (! -d $obj_dir . $OdeEnv::dirsep . "lib") ||
                        (! -d $obj_dir . $OdeEnv::dirsep . "copyright")))
  {
    $rules_error = 1;
    $error_txt = "a directory missing in $obj_dir";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin") );
  if (!$rules_error && ((! -d $tmpstr . $OdeEnv::dirsep . "server") ||
                        (! -d $tmpstr . $OdeEnv::dirsep . "client") ||
                        (! -d $tmpstr . $OdeEnv::dirsep . "logger")))
  {
    $rules_error = 1;
    $error_txt = "a missing sub directory in bin directory of $obj_dir";
  }
  $tmpstr .=  $OdeEnv::dirsep . "server";
  if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) != 0))
  {
    $rules_error = 1;
    $error_txt = "bin/server in $obj_dir built when the files are up to date";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "lib";
  if (!$rules_error && ((OdeFile::numDirEntries( $tmpstr ) != 0) ||
                        (OdeFile::numDirEntries( $tools_dir ) != 0 ) ||
                        (OdeFile::numDirEntries( $export_dir ) != 0 )))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr or $tools_dir or export_dir built " .
                 "when the files are up to date";
  }
  $tmpstr = dirname( $export_dir ) . $OdeEnv::dirsep  ."classes";
  if (!$rules_error && (! -d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  $tfile1 = join( $OdeEnv::dirsep, ($tmpstr, "COM", "ibm",
                                    "makemake", "bin", "MakeMake.class") );
  $tfile2 = join( $OdeEnv::dirsep, ($tmpstr, "COM", "ibm",
                                    "makemake", "lib", "CommandLine.class") );
  if (!$rules_error && ((!-f $tfile1) || (! -f $tfile2)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 does not exist";
  }
  else
  {
    $mtime1 = (stat $tfile1)[9];
    $mtime2 = (stat $tfile2)[9];
  }
  if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) != 16))
  {
    $rules_error = 1;
    $error_txt = "incorect number of files in $tmpstr";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $images_dir ) != 3))
  {
    $rules_error = 1;
    $error_txt = "incorect number of files in $images_dir";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target build_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target build_all" );
  }

  ## running build build_all -a
  ## all the files are to be built
  $command = "build -rc $OdeTest::test_sb_rc build_all -a "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  if (!$rules_error && (! -d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr does not exist";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) != $filecount1))
  {
    $rules_error = 1;
    $error_txt = "all files not made in $tmpstr";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "lib") );
  if (!$rules_error && (! -d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr does not exist";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) != $filecount2))
  {
    $rules_error = 1;
    $error_txt = "all files not made in $tmpstr";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "cmf") );
  if (!$rules_error && (! -d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr does not exist";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) == 0))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr is empty";
  }
  $tmpstr = join( $OdeEnv::dirsep,
                  ($export_dir, "usr", "include", "server.h") );
  if (defined( $OdeEnv::UNIX ))
  {
    if (!$rules_error && (! -l $tmpstr))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr does not exist";
    }
    $tmpstr = join( $OdeEnv::dirsep,
                    ($export_dir, "usr", "lib", "libexa.a") );
    if (!$rules_error && (! -l $tmpstr))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr does not exist";
    }
  }
  else
  {
    if (!$rules_error && (! -f $tmpstr))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr does not exist";
    }
    $tmpstr = join( $OdeEnv::dirsep,
                    ($export_dir, "usr", "lib", "exa.lib") );
    if (!$rules_error && (! -f $tmpstr))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr does not exist";
    }
  }
  $tmpstr = dirname( $export_dir ) . $OdeEnv::dirsep  ."classes";
  if (!$rules_error && (! -d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) != 16))
  {
    $rules_error = 1;
    $error_txt = "incorect number of files in $tmpstr";
  }
  if (!$rules_error && (! -f $tfile1))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 does not exist";
  }
  if (!$rules_error && ((stat $tfile1)[9] == $mtime1))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 not remade";
  }
  if (!$rules_error && (! -f $tfile2))
  {
    $rules_error = 1;
    $error_txt = "$tfile2 does not exist";
  }
  if (!$rules_error && ((stat $tfile2)[9] == $mtime2))
  {
    $rules_error = 1;
    $error_txt = "$tfile2 not remade";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $tools_dir ) != 0))
  {
    $rules_error = 1;
    $error_txt = "$tools_dir not empty";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $images_dir ) != 3))
  {
    $rules_error = 1;
    $error_txt = "incorrect number of files in $images_dir";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target build_all -a" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target build_all -a" );
  }
  OdePath::chgdir( $dir );
  return $error;

}

1;
