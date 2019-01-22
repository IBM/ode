#############################################
#
# RulesTargetsTest_compall module
# - module to test predefined target comp_all
#
#############################################

package RulesTargetsTest_compall;

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
  OdeInterface::logPrint( "in RulesTargetsTest_compall\n" );
  $status = test_compall();
  return $status;
}

#########################################
#
# sub test_compall
# - to test predefined target comp_all
#
#########################################
sub test_compall()
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
  my $export_dir = join( $OdeEnv::dirsep, 
                         ($OdeTest::test_sb, "export", $OdeEnv::machine) );
  my $images_dir = join( $OdeEnv::dirsep, 
                         ($OdeTest::test_sb, "inst.images", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;

  OdeFile::deltree( $obj_dir );
  OdeFile::deltree( $export_dir );
  OdeFile::deltree( $images_dir );
  if ((-d $obj_dir) || (-d $export_dir) || (-d $images_dir))
  {
    OdeInterface::logPrint( 
               "$obj_dir or $export_dir or $images_dir not deleted\n" );
  }
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") );
  OdePath::chgdir( $tmpstr );

  ## running build comp_all
  $command = "build -rc $OdeTest::test_sb_rc comp_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (!$rules_error && ((! -d $obj_dir . $OdeEnv::dirsep . "COM") ||
                        (! -d $obj_dir . $OdeEnv::dirsep . "bin") ||
                        (! -d $obj_dir . $OdeEnv::dirsep . "cmf") ||
                        (! -d $obj_dir . $OdeEnv::dirsep . "doc") ||
                        (! -d $obj_dir . $OdeEnv::dirsep . "inc") ||
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
  if (!$rules_error && (-d $export_dir))
  {
    $rules_error = 1;
    $error_txt = "$export_dir created";
  }
  $tmpstr = dirname( $export_dir ) . $OdeEnv::dirsep . "classes";
  if (!$rules_error && (! -d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  $tmpstr .= $OdeEnv::dirsep . "MakeMake.jar";
  if (!$rules_error && (! -f $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "MakeMake.jar not created in $tmpstr";
  }
  else
  {
    $mtime1 = (stat $tmpstr)[9];
  }
  $tmpstr = dirname( $export_dir ) . $OdeEnv::dirsep . "classes";
  $tfile1 = join( $OdeEnv::dirsep, ($tmpstr, "COM", "ibm", 
                                    "makemake", "bin", "MakeMake.class") );
  $tfile2 = join( $OdeEnv::dirsep, ($tmpstr, "COM", "ibm", 
                                    "makemake", "lib", "CommandLine.class") );
  if (!$rules_error && ((! -f $tfile1) || (! -f $tfile2)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 does not exist";
  }
  else
  {
    $mtime2 = (stat $tfile1)[9];
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target comp_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target comp_all" );
  }

  ## sleep for 2 secs to make sure timestamp changes on MakeMake.jar
  sleep 2;
  ## running build comp_all with a source file copied into the sandbox
  $tfile1 = join( $OdeEnv::dirsep, 
          ($OdeEnv::tempdir, "bbexample", "src", "bin", "server", "server.c") );
  $tmpstr = join( $OdeEnv::dirsep, 
                  ($OdeTest::test_sb, "src", "bin", "server") );
  mkpath( $tmpstr, 1 );
  OdeEnv::runSystemCommand( "$OdeEnv::copy $tfile1 $tmpstr" );
  $tmpstr .= $OdeEnv::dirsep . "server.c";
  if (! -f $tmpstr)
  {
    OdeInterface::logPrint( "Error in copying server.c to sandbox\n" );
  }
  OdeFile::touchFile( $tmpstr );
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  if (!$OdeEnv::MVS)
  {
    if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) != 3))
    {
      $rules_error = 1;
      $error_txt = "incorrect number of files in $tmpstr";
    }
  }
  else
  {
    if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) != 5))
    {
      $rules_error = 1;
      $error_txt = "incorrect number of files in $tmpstr";
    }
  }
  if (!$rules_error && 
       ((! -f $tmpstr . $OdeEnv::dirsep . "server" . $OdeEnv::obj_suff) ||
        (! -f $tmpstr . $OdeEnv::dirsep . "server" . $OdeEnv::prog_suff) ||
        (! -f $tmpstr . $OdeEnv::dirsep . "depend.mk")))
  {
    $rules_error = 1;
    $error_txt = "a file missing in server directory of $tmpstr";
  }
  $tmpstr = dirname( $export_dir ) . $OdeEnv::dirsep . "classes";
  $tfile1 = $tmpstr . $OdeEnv::dirsep . "MakeMake.jar";
  $tfile2 = join( $OdeEnv::dirsep, ($tmpstr, "COM", "ibm", 
                                    "makemake", "bin", "MakeMake.class") );
  if (!$rules_error && ((! -f $tfile1) || (! -f $tfile2)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 does not exist";
  }
  if (!$rules_error && (((stat $tfile1)[9] == $mtime1) || 
                        ((stat $tfile2)[9] == $mtime2)))
  {
    $rules_error = 1;
    $error_txt = "$tfile1 or $tfile2 not overwritten";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target comp_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target comp_all" );
  }
  OdePath::chgdir( $dir );
  return $error;
}

1;
