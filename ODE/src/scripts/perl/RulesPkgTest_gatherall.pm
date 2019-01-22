################################################
#
# RulesPkgTest_gatherall module
# - module to test predefined target gather_all
#
################################################

package RulesPkgTest_gatherall;

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
  OdeInterface::logPrint( "in RulesPkgTest_gatherall\n" );
  my $status = test_gatherall();
  return $status;
}

#########################################
#
# sub test_gatherall
# - to test predefined target gather_all
#
#########################################

sub test_gatherall()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $bb_dir;
  my $images_dir = join( $OdeEnv::dirsep, 
                         ($OdeTest::test_sb, "inst.images", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;
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

  ## running build gather_all
  $command = "build -rc $OdeTest::test_sb_rc gather_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (defined( $OdeEnv::AIX ))
  {
    $tmpstr = join( $OdeEnv::dirsep, ($images_dir, "shipdata", "usr", 
                                      "odehello") );
  }
  elsif (defined( $OdeEnv::HP ) || defined( $OdeEnv::SOLARIS ) ||
         defined( $OdeEnv::SCO ) || defined( $OdeEnv::IRIX ))
  {
    $tmpstr = join( $OdeEnv::dirsep, ($images_dir, "shipdata", "opt",
                                      "odehello") );
  }
  else
  {
    $tmpstr = join( $OdeEnv::dirsep, ($images_dir, "mdata", "OdeHello", "usr", 
                                      "lpp", "odehello") );
  }
  if (!$rules_error && (! -d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  $tmpstr .= $OdeEnv::dirsep . "bin";
  if (!$rules_error && (! -d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  if (!$OdeEnv::MVS)
  {
    if (!$rules_error && 
        ((! -f $tmpstr . $OdeEnv::dirsep . "server" . $OdeEnv::prog_suff) ||
         (! -f $tmpstr . $OdeEnv::dirsep . "client" . $OdeEnv::prog_suff) ||
         (! -f $tmpstr . $OdeEnv::dirsep . "logger" . $OdeEnv::prog_suff)))
    {
     $rules_error = 1;
     $error_txt = "a file missing in $tmpstr";
    }
  }
  else
  {
    if (!$rules_error && 
        ((! -f $tmpstr . $OdeEnv::dirsep . "server.p") ||
         (! -f $tmpstr . $OdeEnv::dirsep . "client.p") ||
         (! -f $tmpstr . $OdeEnv::dirsep . "logger.p") ||
         (! -f $tmpstr . $OdeEnv::dirsep . "server.lst") ||
         (! -f $tmpstr . $OdeEnv::dirsep . "client.lst") ||
         (! -f $tmpstr . $OdeEnv::dirsep . "logger.lst")))
    {
     $rules_error = 1;
     $error_txt = "a file missing in $tmpstr";
    }
  }
    
  $tmpstr = dirname( $tmpstr ) . $OdeEnv::dirsep . "html";
  if (!$rules_error && (! -d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) == 0))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr empty";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                1, "predefined target gather_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                1 , "predefined target gather_all" );
  }
  OdePath::chgdir( $dir );
  OdeFile::deltree( \@delete_list );
  return $error;
}

1;
