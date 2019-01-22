########################################################
#
# RulesPkgTest_parseall module
# - module to test predefined target parse_all
#
#########################################################
package RulesPkgTest_parseall;

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
  OdeInterface::logPrint( "in RulesPkgTest_parseall\n" );
  my $status = test_parseall();
  return $status;
}

#########################################
#
# sub test_parseall
# - to test predefined target parse_all
#
#########################################
sub test_parseall()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $mtime1;
  my $tfile1;
  my $tfile2;
  my $tfile3;
  my $tfile4;
  my $tstring1;
  my $tstring2;
  my $bb_dir;
  my $obj_dir = join( $OdeEnv::dirsep,
                      ($OdeTest::test_sb, "obj", $OdeEnv::machine) );
  my $tools_dir = join( $OdeEnv::dirsep,
                        ($OdeTest::test_sb, "tools", $OdeEnv::machine) );
  my $export_dir = join( $OdeEnv::dirsep,
                         ($OdeTest::test_sb, "export", $OdeEnv::machine) );
  my $images_dir = join( $OdeEnv::dirsep,
                         ($OdeTest::test_sb, "inst.images", $OdeEnv::machine) );
  my $dir = cwd();
  my $mdata_count;
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

  OdePath::chgdir( $OdeTest::test_sb_src );

  ## running build parse_all
  $command = "build -rc $OdeTest::test_sb_rc parse_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (!$rules_error && ((OdeFile::numDirEntries( $obj_dir ) != 0) ||
                        (OdeFile::numDirEntries( $export_dir ) != 0) ||
                        (OdeFile::numDirEntries( $tools_dir ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "obj or export or tools directory not empty";
  }
  $tfile1 = $images_dir . $OdeEnv::dirsep . "images";
  if (!$rules_error && ((! -d $tfile1 ) ||
                        (OdeFile::numDirEntries( $tfile1 ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 does not exist or is not empty";
  }
  ## parse_all does nothing on NT, hence no further checks need to be done
  if (!defined( $OdeEnv::WIN32 ))
  {
    $tfile1 = $images_dir . $OdeEnv::dirsep . "shipdata";
    if (!$rules_error && ((! -d $tfile1 ) ||
                          (OdeFile::numDirEntries( $tfile1 ) != 0)))
    {
      # Since the HP/swpackage parser/generator actually creates links
      # and directories, "shipdata" will contain stuff
      if (!defined( $OdeEnv::HP ))
      {
        $rules_error = 1;
        $error_txt = "$tfile1 does not exist or is not empty";
      }
    }
    if (defined( $OdeEnv::AIX ))
    {
      $tfile1 = $images_dir . $OdeEnv::dirsep . "mdata";
      if (!$rules_error &&
           ((! -f $tfile1 . $OdeEnv::dirsep . "odehello.bin.il") ||
            (! -f $tfile1 . $OdeEnv::dirsep . "odehello.doc.il") ||
            (! -f $tfile1 . $OdeEnv::dirsep . "pcd.pd")))
      {
        $rules_error = 1;
        $error_txt = "a file missing in $tfile1";
      }
      $mdata_count = 4;
      $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "mdata", "pcd.pd") );
      $tstring1 = "odehello.bin.il";
      $tstring2 = "odehello.doc.il";
    }
    elsif (defined( $OdeEnv::HP ))
    {
      $mdata_count = 1;
      $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "mdata", "pcd.psf") );
      $tstring1 = "odehellobin";
      $tstring2 = "odehellodoc";
    }
    elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
           defined( $OdeEnv::IRIX ))
    {
      $tfile1 = $images_dir . $OdeEnv::dirsep . "mdata";
      if (!$rules_error &&
           ((! -f $tfile1 . $OdeEnv::dirsep . "compver") ||
            (! -f $tfile1 . $OdeEnv::dirsep . "copyright") ||
            (! -f $tfile1 . $OdeEnv::dirsep . "depend") ||
            (! -f $tfile1 . $OdeEnv::dirsep . "prototype") ||
            (! -f $tfile1 . $OdeEnv::dirsep . "pkginfo") ||
            (! -f $tfile1 . $OdeEnv::dirsep . "space")))
      {
        $rules_error = 1;
        $error_txt = "a file missing in $tfile1";
      }
      $mdata_count = 6;
      $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "mdata", "prototype") );
      $tstring1 = "odehello.bin";
      $tstring2 = "odehello.doc";
    }
    $tfile2 = $images_dir . $OdeEnv::dirsep . "mdata";
    if (!$rules_error && (OdeFile::numDirEntries( $tfile2 ) != $mdata_count ))
    {
      $rules_error = 1;
      $error_txt = "incorrect number of files in $tfile2";
    }
    if (!$rules_error && ((!OdeFile::findInFile( $tstring1, $tfile1 )) ||
                          (!OdeFile::findInFile( $tstring2, $tfile1 ))))
    {
      $rules_error = 1;
      $error_txt = "$tstring1 or $tstring2 not found in $tfile1";
    }
    if (!$rules_error)
    {
      $mtime1 = (stat $tfile1)[9];
    }
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target parse_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target parse_all" );
  }

  if (defined( $OdeEnv::MVS ))
  {
    ## running build parse_all gather_all
    $command = "build -rc $OdeTest::test_sb_rc parse_all gather_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  }
  else
  {
    ## running build gather_all parse_all
    $command = "build -rc $OdeTest::test_sb_rc gather_all parse_all "
               . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  }
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tfile1 = $images_dir . $OdeEnv::dirsep . "images";
  if (!$rules_error && ((! -d $tfile1 ) ||
                        (OdeFile::numDirEntries( $tfile1 ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 does not exist or is not empty";
  }
  if (defined( $OdeEnv::WIN32 ))
  {
    ## these files are created by gather_all
    $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "mdata", "OdeHello") );
    if (!$rules_error && ((! -d $tfile1) ||
                          (OdeFile::numDirEntries( $tfile1 ) != 14)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 does not exist or has incorrect number of files";
    }
  }
  else
  {
    if (defined( $OdeEnv::AIX ))
    {
      $mdata_count = 4;
      $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "mdata", "pcd.pd") );
      $tfile2 = join( $OdeEnv::dirsep,
                   ($images_dir, "shipdata", "usr",  "odehello", "bin") );
      $tfile3 = join( $OdeEnv::dirsep,
                  ($images_dir, "shipdata", "usr",  "odehello", "html") );
    }
    else
    {
      if (defined( $OdeEnv::HP ))
      {
        $mdata_count = 1;
        $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "mdata", "pcd.psf") );
      }
      elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
             defined( $OdeEnv::IRIX ))
      {
        $mdata_count = 6;
        $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "mdata", "prototype") );
      }
      $tfile2 = join( $OdeEnv::dirsep,
                   ($images_dir, "shipdata", "opt", "odehello", "bin") );
      $tfile3 = join( $OdeEnv::dirsep,
                  ($images_dir, "shipdata", "opt", "odehello", "html") );
    }
    if (!$rules_error && ((! -f $tfile1 ) || ((stat $tfile1)[9] == $mtime1)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 does not exist or not overwritten";
    }
    ## these files are created by gather_all
    if (!$rules_error && ((! -d $tfile2) ||
                          (OdeFile::numDirEntries( $tfile2 ) != 7)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created or has incorrect number of files";
    }
    if (!$rules_error && ((! -d $tfile3) ||
                          (OdeFile::numDirEntries( $tfile3 ) != 2)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created or has incorrect number of files";
    }
    $tfile4 = $images_dir . $OdeEnv::dirsep . "mdata";
    if (!$rules_error && (OdeFile::numDirEntries( $tfile4 ) != $mdata_count ))
    {
      $rules_error = 1;
      $error_txt = "incorrect number of files in $tfile4";
    }
  }
  if ($rules_error)
  {
    # Test case failed
    if (defined( $OdeEnv::MVS ))
    {
      OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                 2, "predefined targets parse_all gather_all" );
    }
    else
    {
      OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                 2, "predefined targets gather_all parse_all" );
    }
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
   if (defined( $OdeEnv::MVS ))
    {
      OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined targets parse_all gather_all" );
    }
    else
    {
      OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined targets gather_all parse_all" );
    }
  }
  OdePath::chgdir( $dir );
  OdeFile::deltree( \@delete_list );
  return $error;
}

1;
