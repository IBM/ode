################################################
#
# RulesTargetsTest_installall module
# - module to test predefined target install_all
#
################################################

package RulesTargetsTest_installall;

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
  my $error = 0;
  my $status;

  OdeInterface::logPrint( "in RulesTargetsTest_installall\n" );
  $status = test_installall( 1 );
  if (!$error) { $error = $status; }

  ## deleting obj/machine in sandbox. Files should be installed from 
  ## backing build
  my $tmpstr = join( $OdeEnv::dirsep, 
                     ($OdeTest::test_sb, "obj", $OdeEnv::machine) );
  OdeFile::deltree( $tmpstr );
  $status = test_installall( 2 );
  if (!$error) { $error = $status; }
  
  return $error;
}

#########################################
#
# sub test_installall
# - to test predefined target install_all
#
#########################################

sub test_installall( $ )
{
  my ($testno) = @_;
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $images_dir = join( $OdeEnv::dirsep, 
                         ($OdeTest::test_sb, "inst.images", $OdeEnv::machine) );
  my $tools_dir = join( $OdeEnv::dirsep, 
                        ($OdeTest::test_sb, "tools", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;
  OdeFile::deltree( $images_dir );
  if (-d $images_dir)
  {
    OdeInterface::logPrint( "$images_dir not deleted\n" );
  }
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") );
  OdePath::chgdir( $tmpstr );

  ## running build install_all
  $command = "build -rc $OdeTest::test_sb_rc install_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (defined( $OdeEnv::UNIX ))
  {
    if ((defined ($OdeEnv::LINUX)) || (defined ($OdeEnv::SCO)) ||
        (defined ($OdeEnv::ANY_BSD)))
    {
     	$tmpstr = join( $OdeEnv::dirsep, ($images_dir, "shipdata", "usr", "lpp",
                                     "odehello") );
    }
    elsif (defined( $OdeEnv::AIX ))
    {
    $tmpstr = join( $OdeEnv::dirsep, ($images_dir, "mdata", "OdeHello", "usr", 
                                      "odehello") );
    }

    else
    {
     	$tmpstr = join( $OdeEnv::dirsep, ($images_dir, "shipdata", "opt", 
                                     "odehello") );
    }
  }
  elsif (defined( $OdeEnv::WIN32 ))
  {
    $tmpstr = join( $OdeEnv::dirsep, ($images_dir, "mdata", "OdeHello", "usr", 
                                      "lpp", "odehello") );
  }
  elsif (defined( $OdeEnv::OS2 ))
  {
    $tmpstr = join( $OdeEnv::dirsep, ($tools_dir, "usr", "lpp", "odehello") );
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
                               $testno, "predefined target install_all" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                               $testno, "predefined target install_all" );
  }
  OdePath::chgdir( $dir );
  return $error;
}

1;
