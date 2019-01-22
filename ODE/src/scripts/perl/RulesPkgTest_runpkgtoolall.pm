#########################################################
#
# RulesPkgTest_runpkgtoolall module
# - module to test predefined target runpkgtool_all
#
#########################################################
package RulesPkgTest_runpkgtoolall;

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
  OdeInterface::logPrint( "in RulesPkgTest_runpkgtoolall\n" );
  my $status = test_runpkgtoolall();
  return $status;
}

#########################################
#
# sub test_runpkgtoolall
# - to test predefined target runpkgtool_all
#
#########################################
sub test_runpkgtoolall()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $tfile1;
  my $tfile2;
  my $bb_dir;
  my $sourcezip;
  my $targetzip;
  my $images_dir = join( $OdeEnv::dirsep,
                         ($OdeTest::test_sb, "inst.images", $OdeEnv::machine) );
  my $dir = cwd();
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb_rc,
                     $OdeTest::tmpfile);

  OdeFile::deltree( \@delete_list );

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
          "Packaging rules Test : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  ## odehello.zip needs to be unzipped for NT. Hence copying it into sandbox
  ## and unzipping it
  if (defined( $OdeEnv::WIN32 ))
  {
    if (!$OdeEnv::bbex_dir)
    {
      $sourcezip = join( $OdeEnv::dirsep,
                         ($OdeEnv::bb_base, "src", "bbexample", "inst.images",
                          $OdeEnv::machine, "mdata", "odehello.zip") );
    }
    else
    {
      $sourcezip = join( $OdeEnv::dirsep,
                         ($OdeEnv::bbex_dir, "bbexample", "inst.images",
                          $OdeEnv::machine, "mdata", "odehello.zip") );
    }
    $targetzip = join( $OdeEnv::dirsep,
                       ($OdeTest::test_sb, "inst.images", $OdeEnv::machine,
                        "mdata") );
    if (! -d $targetzip)
    {
      mkpath( $targetzip );
    }
    OdeEnv::runSystemCommand( "$OdeEnv::copy $sourcezip $targetzip" );
    if (! -f $targetzip . $OdeEnv::dirsep . "odehello.zip")
    {
      OdeInterface::printError(
            "odehello.zip could not be created in $targetzip" );
      $error = 1;
      OdeFile::deltree( \@delete_list );
      return $error;
    }
    OdePath::chgdir( $targetzip );
    $status = OdeEnv::runSystemCommand( "jar -xf odehello.zip" );
    if ($status)
    {
      OdeInterface::printError( "unjaring of odehello.zip failed" );
      $error = 1;
      OdePath::chgdir( $dir );
      OdeFile::deltree( \@delete_list );
      return $error;
    }
  }

  OdePath::chgdir( $OdeTest::test_sb_src );

  ## running build runpkgtool_all
  ## should give an error as runpkgtool_all should be run after parse_all
  ## and gather_all
  $command = "build -rc $OdeTest::test_sb_rc runpkgtool_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (!$status)
  {
    $rules_error = 1;
    $error_txt = "build returned a zero exit code";
  }
  if (defined( $OdeEnv::WIN32 ))
  {
    $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "images", "Log Files") );
    if (!$rules_error && (OdeFile::numDirEntries( $tfile1 ) != 1))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 has incorrect number of files";
    }
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target runpkgtool_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target runpkgtool_all" );
  }

  if (defined( $OdeEnv::MVS ))
  {
    ## running build parse_all gather_all
    ## parse_all and gather_all should be run before runpkgtool_all
    ## check all directories that will be affected by runpkgtool_all
    $command = "build -rc $OdeTest::test_sb_rc parse_all gather_all"
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  }
  else
  {
    ## running build gather_all parse_all
    ## gather_all and parse_all should be run before runpkgtool_all
    ## check all directories that will be affected by runpkgtool_all
    $command = "build -rc $OdeTest::test_sb_rc gather_all parse_all"
               . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";

  }
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (defined( $OdeEnv::WIN32 ))
  {
    $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "images", "Disk Images") );
    $tfile2 = join( $OdeEnv::dirsep, ($images_dir, "images", "Report Files") );
    if (!$rules_error && ((-d $tfile1) || (-d $tfile2)))
    {
      $error_txt = "$tfile1 or $tfile2 exist before running runpkgtool_all";
    }
  }
  else
  {
    $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "images") );
    if (!$rules_error && (OdeFile::numDirEntries( $tfile1 ) != 0))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not empty before running runpkgtool_all";
    }
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target runpkgtool_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target runpkgtool_all" );
  }

  ## running build runpkgtool_all
  $command = "build -rc $OdeTest::test_sb_rc runpkgtool_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (defined( $OdeEnv::WIN32 ))
  {
    $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "images", "Disk Images") );
    if (!$rules_error && ((! -d $tfile1) ||
                          (OdeFile::numDirEntries( $tfile1 ) != 19)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created or has incorrect number of files";
    }
    $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "images", "Report Files") );
    if (!$rules_error && ((! -d $tfile1) ||
                          (OdeFile::numDirEntries( $tfile1 ) != 1)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created or has incorrect number of files";
    }
  }
  else
  {
    $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "images", "odehello") );
    if (defined( $OdeEnv::AIX ))
    {
      if (!$rules_error && (! -f $tfile1))
      {
        $rules_error = 1;
        $error_txt = "$tfile1 not created";
      }
    }
    elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
           defined( $OdeEnv::IRIX ))
    {
      if (!$rules_error && ((! -f $tfile1 . $OdeEnv::dirsep . "pkginfo") ||
                            (! -f $tfile1 . $OdeEnv::dirsep . "pkgmap")))
      {
        $rules_error = 1;
        $error_txt = "pkginfo or pkgmap does not exist in $tfile1";
      }
      $tfile2 = $tfile1;
      $tfile1 .= $OdeEnv::dirsep . "install";
      if (!$rules_error && ((! -d $tfile1 ) ||
                            (OdeFile::numDirEntries( $tfile1 ) != 4)))
      {
        $rules_error = 1;
        $error_txt = "$tfile1 not created or has incorrect number of files";
      }
      $tfile1 .= $OdeEnv::dirsep . "depend";
      $tfile2 = join( $OdeEnv::dirsep, ($tfile2, "root", "opt",
                                                "odehello", "bin") );
      if (!$rules_error && ((! -d $tfile2) ||
                            (OdeFile::numDirEntries( $tfile2 ) != 7)))
      {
        $rules_error = 1;
        $error_txt = "$tfile2 not created or has incorrect number of files";
      }
      $tfile2 = dirname( $tfile2 ) . $OdeEnv::dirsep .  "html";
      if (!$rules_error && ((! -d $tfile2) ||
                            (OdeFile::numDirEntries( $tfile2 ) != 2)))
      {
        $rules_error = 1;
        $error_txt = "$tfile2 not created or has incorrect number of files";
      }
    }
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               3, "predefined target runpkgtool_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               3, "predefined target runpkgtool_all" );
  }
  OdePath::chgdir( $dir );
  OdeFile::deltree( \@delete_list );
  return $error;

}

1;
