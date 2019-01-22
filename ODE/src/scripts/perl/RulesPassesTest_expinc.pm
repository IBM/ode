##############################################
#
# RulesPassesTest_expinc module
# - module to test predefined pass EXPINC
#
##############################################
package RulesPassesTest_expinc;

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
  OdeInterface::logPrint( "in RulesPassesTest_expinc\n" );
  my $status = test_expinc();
  return $status;
}

#########################################
#
# sub test_expinc
# - to test predefined pass EXPINC
#
#########################################
sub test_expinc()
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

  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") );
  OdePath::chgdir( $tmpstr );

  ## running build MAKEFILE_PASS=EXPINC
  ## nothing should be built
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=EXPINC "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "inc";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or is not empty";
  }
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "lib") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "cmf") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "bin") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "COM") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "copyright")))
  {
    $rules_error = 1;
    $error_txt = "a directory other than inc created in $obj_dir";
  }
  if (!$rules_error && ((OdeFile::numDirEntries( $tools_dir ) != 0) ||
                        (OdeFile::numDirEntries( $export_dir ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tools_dir or $export_dir is not empty";
  }
  $tmpstr = dirname( $export_dir ) . $OdeEnv::dirsep  . "classes";
  if (!$rules_error && (-d $tmpstr)) 
  {
    $rules_error = 1;
    $error_txt = "$tmpstr created";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $images_dir ) != 3))
  {
    $rules_error = 1;
    $error_txt = "incorrect number of contents in $images_dir";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined pass EXPINC" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined pass EXPINC" );
  }

  ## running build MAKEFILE_PASS=EXPINC -a
  ## all the header files should get exported. Nothing is built
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=EXPINC -a "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "inc";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or is not empty";
  }
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "lib") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "cmf") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "bin") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "COM") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "copyright")))
  {
    $rules_error = 1;
    $error_txt = "a directory other than inc created in $obj_dir";
  }
  if (!$rules_error && ((OdeFile::numDirEntries( $tools_dir ) != 0) ||
                        (OdeFile::numDirEntries( $images_dir ) != 3)))
  {
    $rules_error = 1;
    $error_txt = "$tools_dir or $images_dir has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($export_dir, "usr") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 3)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of contents";
  }
  $tmpstr .= $OdeEnv::dirsep . "include" . $OdeEnv::dirsep . "server.h";
  if (defined( $OdeEnv::UNIX ))
  {
    if (!$rules_error && (! -l $tmpstr))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr not created";
    }
    else
    {
      $mtime1 = (stat $tmpstr)[9];
    }
  }
  else
  {
    if (!$rules_error && (! -f $tmpstr))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr not created";
    }
    else
    {
     $mtime1 = (stat $tmpstr)[9];
    }
  }
  $tmpstr = dirname( $export_dir ) . $OdeEnv::dirsep  . "classes";
  if (!$rules_error && (-d $tmpstr)) 
  {
    $rules_error = 1;
    $error_txt = "$tmpstr created";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined pass EXPINC" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined pass EXPINC" );
  }

  ## running build MAKEFILE_PASS=EXPINC
  ## server.c is recently modified. Nothing should be built and exported
  ## sleep for 2 secs to make sure that the files are out of date
  sleep 2;
  $tfile1 = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "bbexample", "src", 
                                    "bin", "server", "server.c") );
  $tfile2 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", 
                                    "bin", "server") );
  mkpath( $tfile2 );
  if (! -d $tfile2)
  {
    OdeInterface::logPrint( "$tfile2 not created\n" );
  }
  ## copying server.c into sandbox and touching it
  else
  {
    OdeEnv::runSystemCommand( "$OdeEnv::copy $tfile1 $tfile2" );
  }
  $tfile2 .= $OdeEnv::dirsep . "server.c";
  if (! -f $tfile2)
  {
    OdeInterface::logPrint( "$tfile2 not copied\n" );
  }
  else
  {
    OdeFile::touchFile( $tfile2 );
  }
    
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=EXPINC "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "inc";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or is not empty";
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
  }
  else
  {
    if (!$rules_error && (! -f $tmpstr))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr does not exist";
    }
  }
  if (!$rules_error && ((stat $tmpstr)[9] != $mtime1))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr remade when up to date";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               3, "predefined pass EXPINC" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               3, "predefined pass EXPINC" );
  }

  ## running build MAKEFILE_PASS=EXPINC
  ## server.h is recently modified. Nothing should be built and 
  ## server.h should be exported
  $tfile1 = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "bbexample", "src", 
                                    "inc", "server.h") );
  $tfile2 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "inc") );
  mkpath( $tfile2 );
  if (! -d $tfile2)
  {
    OdeInterface::logPrint( "$tfile2 not created\n" );
  }
  ## copying server.h into sandbox and touching it
  else
  {
    OdeEnv::runSystemCommand( "$OdeEnv::copy $tfile1 $tfile2" );
  }
  $tfile2 .= $OdeEnv::dirsep . "server.h";
  if (! -f $tfile2)
  {
    OdeInterface::logPrint( "$tfile2 not copied\n" );
  }
  else
  {
    OdeFile::touchFile( $tfile2 );
  }
    
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=EXPINC "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "inc";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or is not empty";
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
  }
  else
  {
    if (!$rules_error && (! -f $tmpstr))
    {
      $rules_error = 1;
      $error_txt = "$tmpstr does not exist";
    }
  }
  if (!$rules_error && ((stat $tmpstr)[9] == $mtime1))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not remade when out of date";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               4, "predefined pass EXPINC" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               4, "predefined pass EXPINC" );
  }

  ## deleting obj directory
  OdeFile::deltree( $obj_dir );
  if (-d $obj_dir)
  {
    OdeInterface::logPrint( "$obj_dir not deleted\n" );
  }

  ## running build MAKEFILE_PASS=expinc
  ## should do nothing
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=expinc "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (!$rules_error && ((! -d $obj_dir) || 
                        (OdeFile::numDirEntries( $obj_dir ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$obj_dir not created or is not empty";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               5, "predefined pass EXPINC" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               5, "predefined pass EXPINC" );
  }
  
  OdePath::chgdir( $dir );
  OdeFile::deltree( \@delete_list );
  return $error;
}

1;
