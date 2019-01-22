#########################################################
#
# RulesPkgTest_buildandpackageall module
# - module to test predefined target buildandpackage_all
#
#########################################################
package RulesPkgTest_buildandpackageall;

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
  OdeInterface::logPrint( "in RulesPkgTest_buildandpackageall\n" );
  my $status = test_buildandpackageall();
  return $status;
}

#########################################
#
# sub test_buildandpackageall
# - to test predefined target buildandpackage_all
#
#########################################
sub test_buildandpackageall()
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
  my $bb_dir;
  my $sourcezip;
  my $targetzip;
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
  my $mdata_count;
  my $hp_pkgdir;
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

  if (defined( $OdeEnv::HP ))
  {
    if (!$ENV{'PKG_OUTPUT_DIR'})
    {
      OdeInterface::logPrint( "setting PKG_OUTPUT_DIR = $OdeEnv::tempdir\n" );
      $ENV{'PKG_OUTPUT_DIR'} = $OdeEnv::tempdir;
    }
    $hp_pkgdir = $ENV{'PKG_OUTPUT_DIR'};
    OdeInterface::logPrint( 
     "WARNING: files created in PKG_OUTPUT_DIR can be deleted by root only\n" );
  }

  OdePath::chgdir( $OdeTest::test_sb_src );

  ## running build buildandpackage_all
  ## all the files are up to date. Only java files are to be built and 
  ## run packaging passes
  $command = "build -rc $OdeTest::test_sb_rc buildandpackage_all "
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
  if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) != 16))
  {
    $rules_error = 1;
    $error_txt = "incorect number of files in $tmpstr";
  }
  if (defined( $OdeEnv::WIN32 ))
  {
    $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "mdata", "META-INF",
                                      "MANIFEST.MF") );
    $tfile2 = join( $OdeEnv::dirsep, ($images_dir, "mdata", "OdeHello") );
    if (!$rules_error && ((! -f $tfile1) || (! -d $tfile2)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 or $tfile2 not created";
    }
    if (!$rules_error && (OdeFile::numDirEntries( $tfile2 ) != 111))
    {
      $rules_error = 1;
      $error_txt = "Incorrect number of entries in $tfile2";
    }
    $tfile1 = $images_dir . $OdeEnv::dirsep . "images";
    if (!$rules_error && (OdeFile::numDirEntries( $tfile1 ) != 24))
    {
      $rules_error = 1;
      $error_txt = "Incorrect number of entries in $tfile1";
    }
    $tfile1 = join( $OdeEnv::dirsep, 
                ($images_dir, "images", "Disk Images", "Disk1", "setup.lid") );
    if (!$rules_error && (! -f $tfile1))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created";
    }
    else
    {
      $mtime1 = (stat $tfile1)[9];
    }
    $tfile1 = join( $OdeEnv::dirsep, 
                ($images_dir, "images", "Disk Images", "Disk1", "Setup.exe") );
    if (!$rules_error && (! -f $tfile1))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created";
    }
  }
  else
  {
    if (defined( $OdeEnv::AIX ))
    {
      $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "images", "odehello") );
      $mdata_count = 4;
    }
    elsif (defined( $OdeEnv::HP ))
    {
      $tfile1 = $images_dir . $OdeEnv::dirsep . "images";
      if (!$rules_error && ((! -d $tfile1) ||
                            (OdeFile::numDirEntries( $tfile1 ) != 0)))
      {
        $rules_error = 1;
        $error_txt = "$tfile1 not created or is not empty";
      }
      if (!$rules_error && 
           (! -d $hp_pkgdir . $OdeEnv::dirsep . "odehello"))
      {
        $rules_error = 1;
        $error_txt = "directory odehello not created in $hp_pkgdir";
      }
      if (!$rules_error && 
           (! -d $hp_pkgdir . $OdeEnv::dirsep . "catalog"))
      {
        $rules_error = 1;
        $error_txt = "directory catalog not created in $hp_pkgdir";
      }
      $tfile1 = $hp_pkgdir . $OdeEnv::dirsep . "swagent.log";
      $mdata_count = 1;
    }
    elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
           defined( $OdeEnv::IRIX ))
    {
      $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "images", "odehello") );
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
      $tfile1 .= $OdeEnv::dirsep . "depend";
      $mdata_count = 6;
    }
    if (!$rules_error && (! -f $tfile1))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created"; 
    }
    else
    {
      $mtime1 = (stat $tfile1)[9];
    }
    $tfile1 = $images_dir . $OdeEnv::dirsep . "mdata";
    if (!$rules_error && (OdeFile::numDirEntries( $tfile1 ) != $mdata_count))
    {
      $rules_error = 1;
      $error_txt = "incorect number of files in $tfile1";
    }
    if (defined( $OdeEnv::AIX ))
    {
      $tfile1 = join( $OdeEnv::dirsep, 
                  ($images_dir, "shipdata", "usr", "lpp", "odehello", "bin") );
      $tfile2 = join( $OdeEnv::dirsep, 
                  ($images_dir, "shipdata", "usr", "lpp", "odehello", "html") );
    }
    else
    {
      $tfile1 = join( $OdeEnv::dirsep, 
                      ($images_dir, "shipdata", "opt", "odehello", "bin") );
      $tfile2 = join( $OdeEnv::dirsep, 
                      ($images_dir, "shipdata", "opt", "odehello", "html") );
    }
    if (!$rules_error && ((! -d $tfile1) || 
                          (OdeFile::numDirEntries( $tfile1 ) != 7)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created or has incorrect number of files";
    }
    $tfile1 .= $OdeEnv::dirsep . "client" . $OdeEnv::prog_suff;
    if (!$rules_error && (! -f $tfile1))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created";
    }
    else
    {
      $mtime2 = (stat $tfile1)[9];
    }
    if (!$rules_error && ((! -d $tfile2) || 
                          (OdeFile::numDirEntries( $tfile2 ) != 2)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created or has incorrect number of files";
    }
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target buildandpackage_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target buildandpackage_all" );
  }

  ## running build buildandpackage_all -a
  ## all the files are to be built
  $command = "build -rc $OdeTest::test_sb_rc buildandpackage_all -a "
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
  if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) != 7))
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
  if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) != 4))
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
  if (!$rules_error && (OdeFile::numDirEntries( $tools_dir ) != 0))
  {
    $rules_error = 1;
    $error_txt = "$tools_dir not empty";
  }
  if (defined( $OdeEnv::WIN32 ))
  {
    $tfile1 = $images_dir . $OdeEnv::dirsep . "images";
    ## The number of entries in this directory increases due to the 
    ## additional log files created
    if (!$rules_error && (OdeFile::numDirEntries( $tfile1 ) != 26))
    {
      $rules_error = 1;
      $error_txt = "Incorrect number of entries in $tfile1";
    }
    $tfile1 = join( $OdeEnv::dirsep, 
                ($images_dir, "images", "Disk Images", "Disk1", "setup.lid") );
  }
  elsif (defined( $OdeEnv::AIX ))
  {
    $tfile1 = join( $OdeEnv::dirsep, ($images_dir, "images", "odehello") );
  }
  elsif (defined( $OdeEnv::HP ))
  {
    $tfile1 = $images_dir . $OdeEnv::dirsep . "images";
    if (!$rules_error && ((! -d $tfile1) ||
                          (OdeFile::numDirEntries( $tfile1 ) != 0)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created or is not empty";
    }
    $tfile1 = $hp_pkgdir . $OdeEnv::dirsep . "swagent.log";
  }
  elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
         defined( $OdeEnv::IRIX ))
  {
    $tfile1 = join( $OdeEnv::dirsep, 
                    ($images_dir, "images", "odehello", "install") );
    if (!$rules_error && ((! -d $tfile1 ) || 
                          (OdeFile::numDirEntries( $tfile1 ) != 4)))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created or has incorrect number of files";
    }
    $tfile1 .= $OdeEnv::dirsep . "depend";
  }
  if (!$rules_error && (! -f $tfile1))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 not created";
  }
  if (!$rules_error && ((stat $tfile1)[9] == $mtime1))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 not remade";
  }
  if (defined( $OdeEnv::UNIX ))
  {
    if (defined( $OdeEnv::AIX ))
    {
      $tfile1 = join( $OdeEnv::dirsep, 
                  ($images_dir, "shipdata", "usr", "lpp", "odehello", "bin") );
    }
    else
    {
      $tfile1 = join( $OdeEnv::dirsep, 
                      ($images_dir, "shipdata", "opt", "odehello", "bin") );
    }
    $tfile1 .= $OdeEnv::dirsep . "client" . $OdeEnv::prog_suff;
    if (!$rules_error && (! -f $tfile1))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not created";
    }
    if (!$rules_error && ((stat $tfile1)[9] == $mtime2))
    {
      $rules_error = 1;
      $error_txt = "$tfile1 not remade";
    }
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target buildandpackage_all -a" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target buildandpackage_all -a" );
  }
  OdePath::chgdir( $dir );
  OdeFile::deltree( \@delete_list );
  return $error;

}

1;
