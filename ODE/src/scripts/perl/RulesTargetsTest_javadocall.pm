################################################
#
# RulesTargetsTest_javadocall module
# - module to test predefined target javadoc_all
#
################################################

package RulesTargetsTest_javadocall;

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
  OdeInterface::logPrint( "in RulesTargetsTest_javadocall\n" );
  $status = test_javadocall();
  return $status;
}

#########################################
#
# sub test_javadocall
# - to test predefined target javadoc_all
#
#########################################
sub test_javadocall()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $obj_dir = join( $OdeEnv::dirsep, 
                      ($OdeTest::test_sb, "obj", $OdeEnv::machine) );
  my $java_dir = join( $OdeEnv::dirsep, 
                 ($OdeTest::test_sb, "src", "COM", "ibm", "makemake", "bin") );
  my $doc_dir = join( $OdeEnv::dirsep, 
                      ($obj_dir, "COM", "ibm", "makemake","bin") );
  my $export_dir = join( $OdeEnv::dirsep, 
                         ($OdeTest::test_sb, "export", "classes") );
  my $dir = cwd();
  my $tmpstr;

  OdeFile::deltree( $obj_dir );
  OdeFile::deltree( $export_dir );
  if ((-d $obj_dir) || (-d $export_dir))
  {
    OdeInterface::logPrint( "$obj_dir or $export_dir not deleted\n" );
  }
  mkpath( $java_dir, 1 );
  if (! -d $java_dir)
  {
    OdeInterface::logPrint( "$java_dir not created\n" );
  }
  OdePath::chgdir( $java_dir );

  ## running build -DBUILDJAVADOCS JAVADOCS=MakeMake.java javadoc_all
  $command = "build -rc $OdeTest::test_sb_rc javadoc_all -DBUILDJAVADOCS"
             . " JAVADOCS=MakeMake.java >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = "COM.ibm.makemake.bin.MakeMake.html";
  if (!$rules_error && ((! -f $doc_dir . $OdeEnv::dirsep . "packages.html") ||
                        (! -f $doc_dir . $OdeEnv::dirsep . "AllNames.html") ||
                        (! -f $doc_dir . $OdeEnv::dirsep . "tree.html") ||
                        (! -f $doc_dir . $OdeEnv::dirsep . $tmpstr)))
  {
    $rules_error = 1;
    $error_txt = "a file(s) missing in $doc_dir";
  }
  if (!$rules_error && (-d $export_dir))
  {
    $rules_error = 1;
    $error_txt = "$export_dir created";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target javadoc_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined target javadoc_all" );
  }

  OdeFile::deltree( $obj_dir );
  OdeFile::deltree( $export_dir );
  if ((-d $obj_dir) || (-d $export_dir))
  {
    OdeInterface::logPrint( "$obj_dir or $export_dir not deleted\n" );
  }

  ## running build -DBUILDJAVADOCS JAVADOCS=MakeMake.java 
  ## should compile MakeMake.java as javadoc_all is not specified
  $command = "build -rc $OdeTest::test_sb_rc -DBUILDJAVADOCS"
             . " JAVADOCS=MakeMake.java >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (!$rules_error && ((! -f $doc_dir . $OdeEnv::dirsep . "packages.html") ||
                        (! -f $doc_dir . $OdeEnv::dirsep . "AllNames.html") ||
                        (! -f $doc_dir . $OdeEnv::dirsep . "tree.html") ||
                        (! -f $doc_dir . $OdeEnv::dirsep . $tmpstr)))
  {
    $rules_error = 1;
    $error_txt = "a file(s) missing in $doc_dir";
  }
  $tmpstr = join( $OdeEnv::dirsep, 
            ($export_dir, "COM", "ibm", "makemake", "bin", "MakeMake.class") );
  if (!$rules_error && (! -f $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target javadoc_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "predefined target javadoc_all" );
  }
  OdePath::chgdir( $dir );
  return $error;
}

1;
