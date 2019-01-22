##############################################
#
# RulesPassesTest_explib module
# - module to test predefined pass EXPLIB
#
##############################################
package RulesPassesTest_explib;

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
  OdeInterface::logPrint( "in RulesPassesTest_explib\n" );
  my $status = test_explib();
  return $status;
}

#########################################
#
# sub test_explib
# - to test predefined pass EXPLIB
#
#########################################
sub test_explib()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $mtime1;
  my $mtime2;
  my $mtime3;
  my $mtime4;
  my $tfile1;
  my $tfile2;
  my $tfile3;
  my $filecount1;
  my $filecount2;
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

  if (defined( $OdeEnv::MVS ))
  {
    $filecount1 = 6;
    $filecount2 = 5;
  }
  else
  {
    $filecount1 = 4;
    $filecount2 = 3;
  }
  ## running build MAKEFILE_PASS=EXPLIB
  ## nothing should be built
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=EXPLIB "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "lib";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or is not empty";
  }
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "cmf") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "bin") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "COM") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "copyright")))
  {
    $rules_error = 1;
    $error_txt = "a directory other than lib created in $obj_dir";
  }
  if (!$rules_error && ((OdeFile::numDirEntries( $tools_dir ) != 0) ||
                        (OdeFile::numDirEntries( $export_dir ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tools_dir or $export_dir is not empty";
  }
  $tmpstr = dirname( $export_dir ) . $OdeEnv::dirsep  . "classes";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 1)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr .= $OdeEnv::dirsep . "MakeMake.jar";
  if (!$rules_error && (! -f $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
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
                               1, "predefined pass EXPLIB" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined pass EXPLIB" );
  }

  ## running build MAKEFILE_PASS=EXPLIB -a
  ## only lib directory should get built
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=EXPLIB -a "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "lib";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount1)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tfile1 = $tmpstr . $OdeEnv::dirsep . "printmsg" . $OdeEnv::obj_suff;
  $tfile2 = $tmpstr . $OdeEnv::dirsep . "printnl" . $OdeEnv::obj_suff;
  $tfile3 = $tmpstr . $OdeEnv::dirsep . "depend.mk";
  if (!$rules_error && (-f $tfile1) && (-f $tfile2) && (-f $tfile3))
  {
    $mtime1 = (stat $tfile1)[9];
    $mtime2 = (stat $tfile2)[9];
    $mtime3 = (stat $tfile3)[9];
  }
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "cmf") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "bin") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "COM") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "copyright")))
  {
    $rules_error = 1;
    $error_txt = "a directory other than lib created in $obj_dir";
  }
  if (!$rules_error && ((OdeFile::numDirEntries( $tools_dir ) != 0) ||
                        (OdeFile::numDirEntries( $images_dir ) != 3)))
  {
    $rules_error = 1;
    $error_txt = "$tools_dir or $images_dir has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($export_dir, "usr") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 2)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of contents";
  }
  $tmpstr .= $OdeEnv::dirsep . "lib";
  if (defined( $OdeEnv::UNIX ))
  {
    $tmpstr .= $OdeEnv::dirsep . "libexa.a";
  }
  else
  {
    $tmpstr .= $OdeEnv::dirsep . "exa.lib";
  }
  if (!$rules_error && (! -f $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  else
  {
    $mtime4 = (stat $tmpstr)[9];
  }
  $tmpstr = dirname( $export_dir ) . $OdeEnv::dirsep  . "classes";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 1)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr .= $OdeEnv::dirsep . "MakeMake.jar";
  if (!$rules_error && (! -f $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined pass EXPLIB" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined pass EXPLIB" );
  }

  ## running build MAKEFILE_PASS=EXPLIB
  ## server.c is recently modified. It shouldn't be built as it is not in lib
  ## printmsg.c is recently modified. It should be built 
  ## sleep for 2 secs to make sure that the files are out of date
  sleep 2;
  $tmpstr = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "bbexample", "src", 
                                    "lib", "printmsg.c") );
  $tfile1 = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "bbexample", "src", 
                                    "bin", "server", "server.c") );
  $tfile2 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", 
                                    "bin", "server") );
  $tfile3 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "lib") );
  mkpath( $tfile2 );
  mkpath( $tfile3 );
  if ((! -d $tfile2)|| (! -d $tfile3))
  {
    OdeInterface::logPrint( "$tfile2 or $tfile3 not created\n" );
  }
  ## copying server.c and printmsg.c into sandbox and touching them
  else
  {
    OdeEnv::runSystemCommand( "$OdeEnv::copy $tmpstr $tfile3" );
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
  $tfile3 .= $OdeEnv::dirsep . "printmsg.c";
  if (! -f $tfile3)
  {
    OdeInterface::logPrint( "$tfile3 not copied\n" );
  }
  else
  {
    OdeFile::touchFile( $tfile3 );
  }
    
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=EXPLIB "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "lib";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount1)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tfile1 = $tmpstr . $OdeEnv::dirsep . "printmsg" . $OdeEnv::obj_suff;
  $tfile2 = $tmpstr . $OdeEnv::dirsep . "printnl" . $OdeEnv::obj_suff;
  $tfile3 = $tmpstr . $OdeEnv::dirsep . "depend.mk";
  if (!$rules_error && ((! -f $tfile1 ) || (! -f $tfile2) || (! -f $tfile3)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 or $tfile3 not created";
  }
  if (!$rules_error && (((stat $tfile1)[9] == $mtime1) || 
                        ((stat $tfile3)[9] == $mtime3)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile3 not remade when out of date";
  }
  if (!$rules_error && ((stat $tfile2)[9] != $mtime2))
  {
    $rules_error = 1;
    $error_txt = "$tfile2 remade when up to date";
  }
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "cmf") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "bin") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "COM") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "copyright")))
  {
    $rules_error = 1;
    $error_txt = "a directory other than lib created in $obj_dir";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($export_dir, "usr") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 2)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of contents";
  }
  $tmpstr .= $OdeEnv::dirsep . "lib";
  if (defined( $OdeEnv::UNIX ))
  {
    $tmpstr .= $OdeEnv::dirsep . "libexa.a";
  }
  else
  {
    $tmpstr .= $OdeEnv::dirsep . "exa.lib";
  }
  if (!$rules_error && (! -f $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  if (!$rules_error && ((stat $tmpstr)[9] == $mtime4))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not remade when out of date";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               3, "predefined pass EXPLIB" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               3, "predefined pass EXPLIB" );
  }
  ## deleting depend.mk in obj/lib
  $tfile1 = join( $OdeEnv::dirsep, ($obj_dir, "lib", "printmsg") );
  $tfile1 .= $OdeEnv::obj_suff;
  if (-f $tfile1)
  {
    $mtime1 = (stat $tfile1)[9];
  }
  else
  {
    OdeInterface::logPrint( "$tfile1 does not exist" );
  }
  $tfile2 = join( $OdeEnv::dirsep, ($obj_dir, "lib", "depend.mk") );
  if (-f $tfile2)
  {
    OdeFile::deltree( $tfile2 );
    if (-f $tfile2)
    {
     OdeInterface::logPrint( "$tfile2 not deleted\n" );
    }
  }
  else
  {
    OdeInterface::logPrint( "$tfile2 does not exist" );
  }

  ## running build MAKEFILE_PASS=STANDARD MAKEFILE_PASS=EXPLIB
  ## should do an EXPLIB pass and neglect STANDARD pass
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=STANDARD "
             . "MAKEFILE_PASS=EXPLIB >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = dirname( $tfile1 );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount2)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr does not exist or has incorrect number of files";
  }
  ## depend.mk should not be created
  if (!$rules_error && ((! -f $tfile1) || (-f $tfile2)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 does not exist or $tfile2 created";
  }
  if (!$rules_error && ((stat $tfile1)[9] != $mtime1))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 remade";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               4, "predefined passes EXPLIB STANDARD" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               4, "predefined passes EXPLIB STANDARD" );
  }

  ## deleting obj directory
  OdeFile::deltree( $obj_dir );
  if (-d $obj_dir)
  {
    OdeInterface::logPrint( "$obj_dir not deleted\n" );
  }

  ## running build MAKEFILE_PASS=explib
  ## should do nothing
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=explib "
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
                               5, "predefined pass EXPLIB" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               5, "predefined pass EXPLIB" );
  }
  
  OdePath::chgdir( $dir );
  OdeFile::deltree( \@delete_list );
  return $error;
}

1;
