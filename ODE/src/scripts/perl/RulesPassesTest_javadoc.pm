################################################
#
# RulesPassesTest_javadoc module
# - module to test predefined pass JAVADOC
#
################################################

package RulesPassesTest_javadoc;

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
  OdeInterface::logPrint( "in RulesPassesTest_javadoc\n" );
  my $status = test_javadoc();
  return $status;
}

#########################################
#
# sub test_javadoc
# - to test predefined pass JAVADOC
#
#########################################
sub test_javadoc()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $bb_dir;
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
  my $java11files_not_found;
  my $java12files_not_found;
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
  mkpath( $java_dir, 1 );
  if (! -d $java_dir)
  {
    OdeInterface::logPrint( "$java_dir not created\n" );
  }
  OdePath::chgdir( $java_dir );

  ## running build -DBUILDJAVADOCS JAVADOCS=MakeMake.java JAVADOC
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=JAVADOC "
             . "-DBUILDJAVADOCS JAVADOCS=MakeMake.java " 
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = "COM.ibm.makemake.bin.MakeMake.html";
  $java11files_not_found = 
                 (! -f $doc_dir . $OdeEnv::dirsep . "packages.html") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . "AllNames.html") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . "tree.html") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . $tmpstr);
  $java12files_not_found = 
                 (! -f $doc_dir . $OdeEnv::dirsep . "overview-tree.html") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . "index-all.html") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . "deprecated-list.html") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . "allclasses-frame.html") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . "index.html") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . "packages.html") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . "COM" . $OdeEnv::dirsep .
                  "ibm" . $OdeEnv::dirsep . "makemake" . $OdeEnv::dirsep .
                  "bin" . $OdeEnv::dirsep . "MakeMake.html") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . "serialized-form.html") ||
                 # package-list is created but has 0 length
                 (! -f $doc_dir . $OdeEnv::dirsep . "package-list") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . "help-doc.html") ||
                 (! -f $doc_dir . $OdeEnv::dirsep . "stylesheet.css");
  if (!$rules_error && $java11files_not_found && $java12files_not_found)
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
                               1, "predefined pass JAVADOC" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "predefined pass JAVADOC" );
  }

  OdePath::chgdir( $dir );
  OdeFile::deltree( \@delete_list );
  return $error;
}

1;
