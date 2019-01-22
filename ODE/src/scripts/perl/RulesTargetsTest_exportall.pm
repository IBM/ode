###############################################
#
# RulesTargetsTest_exportall module
# - module to test predefined target export_all
#
###############################################

package RulesTargetsTest_exportall;

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
  OdeInterface::logPrint( "in RulesTargets_exportall\n" );
  $status = test_exportall();
  return $status;
}

#########################################
#
# sub test_exportall
# - to test predefined target export_all
#
#########################################
sub test_exportall()
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
  my $export_dir = join( $OdeEnv::dirsep, 
                         ($OdeTest::test_sb, "export", $OdeEnv::machine) );
  my $images_dir = join( $OdeEnv::dirsep, 
                         ($OdeTest::test_sb, "inst.images", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;

  OdeFile::deltree( $export_dir );
  if (-d $export_dir)
  {
    OdeInterface::logPrint( "$export_dir not deleted\n" );
  }
  $tmpstr = join( $OdeEnv::dirsep, 
                  ($OdeTest::test_sb, "export", "classes") );
  OdeFile::deltree( $tmpstr );
  if (-d $tmpstr)
  {
    OdeInterface::logPrint( "$tmpstr not deleted\n" );
  }
  $tfile1 = join( $OdeEnv::dirsep, 
                  ($OdeEnv::tempdir, "bbexample", "src", "inc", "server.h") );
  $tmpstr = join( $OdeEnv::dirsep, 
                  ($OdeTest::test_sb, "src", "inc") );
  mkpath( $tmpstr, 1 );
  OdeEnv::runSystemCommand( "$OdeEnv::copy $tfile1 $tmpstr" );
  $tmpstr .= $OdeEnv::dirsep . "server.h";
  if (! -f $tmpstr)
  {
    OdeInterface::logPrint( "Error in copying server.h to sandbox\n" );
  }
  OdeFile::touchFile( $tmpstr );
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") );
  OdePath::chgdir( $tmpstr );

  ## running build export_all
  $command = "build -rc $OdeTest::test_sb_rc export_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tfile1 = join( $OdeEnv::dirsep, 
                  ($export_dir, "usr", "include", "server.h") );
  if (defined( $OdeEnv::UNIX ))
  {
    $tfile2 = join( $OdeEnv::dirsep, 
                    ($export_dir, "usr", "lib", "libexa.a") );
    if (!$rules_error && ((! -l $tfile1) || (! -l $tfile2)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 or $tfile2 doesn't exist";
    }
    else
    {
      $mtime1 = (stat $tfile2)[9];
    }
  }
  else
  {
    $tfile2 = join( $OdeEnv::dirsep, 
                    ($export_dir, "usr", "lib", "exa.lib") );
    if (!$rules_error && ((! -f $tfile1) || (! -f $tfile2)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 or $tfile2 doesn't exist";
    }
    else
    {
      $mtime1 = (stat $tfile2)[9];
    }
  }
  $tmpstr = dirname( $export_dir ) . $OdeEnv::dirsep  ."classes";
  $tfile1 = join( $OdeEnv::dirsep, ($tmpstr, "COM") );
  if (!$rules_error && (-d $tfile1))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 created";
  }
  $tmpstr .= $OdeEnv::dirsep . "MakeMake.jar";
  if (!$rules_error && (! -f $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr does not exist";
  }
  else
  {
      $mtime2 = (stat $tmpstr)[9];
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
                               1, "predefined target export_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target export_all" );
  }

  ## running build export_all
  ## only MakeMake.jar should be overwritten
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (defined( $OdeEnv::UNIX ))
  {
    $tmpstr = join( $OdeEnv::dirsep, 
                    ($export_dir, "usr", "lib", "libexa.a") );
    if (!$rules_error && (! -l $tmpstr))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr does not exist";
    }
    elsif (!$rules_error && ((stat $tmpstr)[9] != $mtime1))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr overwritten";
    }
  }
  else
  {
    $tmpstr = join( $OdeEnv::dirsep, 
                    ($export_dir, "usr", "lib", "exa.lib") );
    if (!$rules_error && (! -f $tmpstr))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr does not exist";
    }
    elsif (!$rules_error && ((stat $tmpstr)[9] != $mtime1))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr overwritten";
    }
  }
  $tmpstr = dirname( $export_dir ) . $OdeEnv::dirsep  ."classes";
  $tmpstr .= $OdeEnv::dirsep . "MakeMake.jar";
  if (!$rules_error && (! -f $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr does not exist";
  }
  elsif (!$rules_error && ((stat $tmpstr)[9] == $mtime2))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not overwritten";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target export_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target export_all" );
  }
  OdePath::chgdir( $dir );
  return $error;
}

1;
