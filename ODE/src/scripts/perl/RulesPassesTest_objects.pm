##############################################
#
# RulesPassesTest_objects module
# - module to test predefined pass OBJECTS
#
##############################################
package RulesPassesTest_objects;

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
  OdeInterface::logPrint( "in RulesPassesTest_objects\n" );
  my $status = test_objects();
  return $status;
}

#########################################
#
# sub test_objects
# - to test predefined pass OBJECTS
#
#########################################
sub test_objects()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $tfile1;
  my $tfile2;
  my $mtime1;
  my $mtime2;
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
  my $filecount;
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

  $tfile1 = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "bbexample", "src", 
                                    "bin", "client", "makefile") );
  $tfile2 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", 
                                    "bin", "client") );
  mkpath( $tfile2 );
  if (! -d $tfile2)
  {
    OdeInterface::logPrint( "$tfile2 not created\n" );
  }
  ## copying makefile into sandbox
  else
  {
    OdeEnv::runSystemCommand( "$OdeEnv::copy $tfile1 $tfile2" );
  }
  ## Adding OBJECTS=client${OBJ_SUFF} into makefile
  $tfile2 .= $OdeEnv::dirsep . "makefile";
  open( INPUT, ">> $tfile2" ) ||
     warn( "Could not open $tfile2\n" );
  print( INPUT "OBJECTS=client\${OBJ_SUFF}" );
  close( INPUT );

  ## running build MAKEFILE_PASS=OBJECTS
  ## the java class files should get built
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=OBJECTS "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "client") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or is not empty";
  }
  $tfile1 = join( $OdeEnv::dirsep, ($obj_dir, "bin", "logger") );
  $tfile2 = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  if (!$rules_error && ((-d $tfile1) || (-d $tfile2)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 created";
  }
  $tfile1 = join( $OdeEnv::dirsep, ($obj_dir, "COM", 
                                    "ibm", "makemake", "bin") );
  $tfile2 = join( $OdeEnv::dirsep, ($obj_dir, "COM", 
                                    "ibm", "makemake", "lib") );
  if (!$rules_error && ((! -d $tfile1) || 
                        (OdeFile::numDirEntries( $tfile1 ) != 0) ||
                        (! -d $tfile2) ||
                        (OdeFile::numDirEntries( $tfile2 ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 not created or is not empty";
  }
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "lib") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "cmf") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "copyright")))
  {
    $rules_error = 1;
    $error_txt = "a directory other than bin and COM created in $obj_dir";
  }
  if (!$rules_error && ((OdeFile::numDirEntries( $tools_dir ) != 0) ||
                        (OdeFile::numDirEntries( $export_dir ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tools_dir or $export_dir is not empty";
  }
  $tfile1 = join( $OdeEnv::dirsep, (dirname( $export_dir ), "classes", "COM",
                                    "ibm", "makemake", "lib") );
  $tfile2 = join( $OdeEnv::dirsep, (dirname( $export_dir ), "classes", "COM",
                                    "ibm", "makemake", "bin") );
  if (!$rules_error && ((! -d $tfile1) || 
                        (OdeFile::numDirEntries( $tfile1 ) != 9) ||
                        (! -d $tfile2) ||
                        (OdeFile::numDirEntries( $tfile2 ) != 1)))
  {
    $rules_error = 1;
    $error_txt = 
         "$tfile1 or $tfile2 not created has incorrect number of files";
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
                               1, "predefined pass OBJECTS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined pass OBJECTS" );
  }

  if (defined( $OdeEnv::MVS ))
  {
    $filecount = 3;
  }
  else
  {
    $filecount = 2;
  }

  ## running build MAKEFILE_PASS=OBJECTS -a
  ## the client.obj should get built
  ## the java class files should get rebuilt
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=OBJECTS -a "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "client") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr .= $OdeEnv::dirsep . "client" . $OdeEnv::obj_suff;
  if (!$rules_error && (! -f $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  else
  {
    $mtime1 = (stat $tmpstr)[9];
  }
  $tfile1 = join( $OdeEnv::dirsep, ($obj_dir, "bin", "logger") );
  $tfile2 = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  if (!$rules_error && ((-d $tfile1) || (-d $tfile2)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 created";
  }
  $tfile1 = join( $OdeEnv::dirsep, ($obj_dir, "COM", 
                                    "ibm", "makemake", "bin") );
  $tfile2 = join( $OdeEnv::dirsep, ($obj_dir, "COM", 
                                    "ibm", "makemake", "lib") );
  if (!$rules_error && ((! -d $tfile1) || 
                        (OdeFile::numDirEntries( $tfile1 ) != 0) ||
                        (! -d $tfile2) ||
                        (OdeFile::numDirEntries( $tfile2 ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 not created or is not empty";
  }
  if (!$rules_error && ((OdeFile::numDirEntries( $tools_dir ) != 0) ||
                        (OdeFile::numDirEntries( $export_dir ) != 0) ||
                        (OdeFile::numDirEntries( $images_dir ) != 3)))
  {
    $rules_error = 1;
    $error_txt = 
       "$tools_dir or $export_dir or $images_dir has incorrect number of files";
  }
  $tfile1 = join( $OdeEnv::dirsep, (dirname( $export_dir ), "classes", "COM",
                                    "ibm", "makemake", "lib") );
  $tfile2 = join( $OdeEnv::dirsep, (dirname( $export_dir ), "classes", "COM",
                                    "ibm", "makemake", "bin") );
  if (!$rules_error && ((! -d $tfile1) || 
                        (OdeFile::numDirEntries( $tfile1 ) != 9) ||
                        (! -d $tfile2) ||
                        (OdeFile::numDirEntries( $tfile2 ) != 1)))
  {
    $rules_error = 1;
    $error_txt = 
         "$tfile1 or $tfile2 not created or has incorrect number of files";
  }
  $tfile2 .= $OdeEnv::dirsep . "MakeMake.class";
  if (!$rules_error && (! -f $tfile2))
  {
    $rules_error = 1;
    $error_txt = "$tfile2 not created";
  }
  else
  {
    $mtime2 = (stat $tfile2)[9];
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined pass OBJECTS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined pass OBJECTS" );
  }

  ## running build MAKEFILE_PASS=OBJECTS
  ## client.c is recently modified. client.obj should be remade along with
  ## java class files
  ## sleep for 2 secs to make sure that the files are out of date
  sleep 2;
  $tfile1 = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "bbexample", "src", 
                                    "bin", "client", "client.c") );
  $tfile2 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", 
                                    "bin", "client") );
  if (! -d $tfile2)
  {
    OdeInterface::logPrint( "$tfile2 not created\n" );
  }
  ## copying client.c into sandbox and touching it
  else
  {
    OdeEnv::runSystemCommand( "$OdeEnv::copy $tfile1 $tfile2" );
  }
  $tfile2 .= $OdeEnv::dirsep . "client.c";
  if (! -f $tfile2)
  {
    OdeInterface::logPrint( "$tfile2 not copied\n" );
  }
  else
  {
    OdeFile::touchFile( $tfile2 );
  }
    
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=OBJECTS "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "client") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr .= $OdeEnv::dirsep . "client" . $OdeEnv::obj_suff;
  if (!$rules_error && (! -f $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  if (!$rules_error && ((stat $tmpstr)[9] == $mtime1))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not remade when out of date";
  }
  $tfile1 = join( $OdeEnv::dirsep, ($obj_dir, "COM", 
                                    "ibm", "makemake", "bin") );
  $tfile2 = join( $OdeEnv::dirsep, ($obj_dir, "COM", 
                                    "ibm", "makemake", "lib") );
  if (!$rules_error && ((! -d $tfile1) || 
                        (OdeFile::numDirEntries( $tfile1 ) != 0) ||
                        (! -d $tfile2) ||
                        (OdeFile::numDirEntries( $tfile2 ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 not created or is not empty";
  }
  $tfile1 = join( $OdeEnv::dirsep, (dirname( $export_dir ), "classes", "COM",
                                    "ibm", "makemake", "lib") );
  $tfile2 = join( $OdeEnv::dirsep, (dirname( $export_dir ), "classes", "COM",
                                    "ibm", "makemake", "bin") );
  if (!$rules_error && ((! -d $tfile1) || 
                        (OdeFile::numDirEntries( $tfile1 ) != 9) ||
                        (! -d $tfile2) ||
                        (OdeFile::numDirEntries( $tfile2 ) != 1)))
  {
    $rules_error = 1;
    $error_txt = 
         "$tfile1 or $tfile2 not created or has incorrect number of files";
  }
  $tfile2 .= $OdeEnv::dirsep . "MakeMake.class";
  if (!$rules_error && (! -f $tfile2))
  {
    $rules_error = 1;
    $error_txt = "$tfile2 not created";
  }
  if (!$rules_error && ((stat $tfile2)[9] == $mtime2))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not remade when out of date";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               3, "predefined pass OBJECTS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               3, "predefined pass OBJECTS" );
  }

  ## deleting obj directory
  OdeFile::deltree( $obj_dir );
  if (-d $obj_dir)
  {
    OdeInterface::logPrint( "$obj_dir not deleted\n" );
  }

  ## running build MAKEFILE_PASS=objects
  ## should do nothing
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=objects "
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
                               4, "predefined pass OBJECTS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               4, "predefined pass OBJECTS" );
  }
  
  OdePath::chgdir( $dir );
  OdeFile::deltree( \@delete_list );
  return $error;
}

1;
