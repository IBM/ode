#########################################
#
# Resbtest module
# - module to run resbtest for different
#   test levels
#
#########################################

package Resbtest;
use strict;
use OdeUtil;
use OdeEnv;
use OdeTest;
use OdePath;
use OdeInterface;

#########################################
#
# sub run
# - run test sequence based on testlevel
#
#########################################
sub run ()
{
  my $status;
  if ( $OdeUtil::arghash{'testlevel'} eq "normal" )
  {
    $status = normal();
    return( $status );
  }
  elsif ( $OdeUtil::arghash{'testlevel'} eq "regression" )
  {
    $status = regression();
    return( $status );
  }
  elsif ( $OdeUtil::arghash{'testlevel'} eq "fvt" )
  {
    $status = fvt();
    return( $status );
  }
  else
  {
    OdeInterface::logPrint(
         "ERROR: undefined testlevel $OdeUtil::arghash{'testlevel'}\n");
    return( 1 );
  }

}

#########################################
#
# sub normal
# - execute normal (minimal) set of tests
#
#########################################
sub normal ()
{
  my $command;
  my $status;
  my $error = 0;
  my $test_sb1_rc = join( $OdeEnv::dirsep, ($OdeTest::test_sb1, "odetestrc1") );
  my @delete_list = ($OdeTest::test_sb_rc, $OdeTest::test_sb, 
                      $OdeTest::test_sb1, $OdeTest::tmpfile);
  OdeFile::deltree( \@delete_list );

  OdeInterface::logPrint( "in Resbtest - test level is normal\n");
  OdeEnv::runOdeCommand( "resb -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");
  
  $command = "mksb -dir $OdeTest::test_sbdir -rc $OdeTest::test_sb_rc "
              . "-back $OdeTest::test_bbdir $OdeTest::test_sb_name "
              . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || !(-d $OdeTest::test_sb) || 
      !(-f join( $OdeEnv::dirsep, 
       ($OdeTest::test_sb, "rc_files", "sb.conf") )) )
  {
    #Test case failed
    OdeInterface::printError( "resb : mksb does not work" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  $command = "mksb -dir $OdeTest::test_sbdir -rc $test_sb1_rc "
             . "-back $OdeTest::test_bbdir $OdeTest::test_sb1_name "
             . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || !(-d $OdeTest::test_sb1) || 
    !(-f join( $OdeEnv::dirsep, ($OdeTest::test_sb1, "rc_files", "sb.conf") )) )
  {
    #Test case failed
    OdeInterface::printError( "resb : mksb does not work" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  $command = "resb -rc $test_sb1_rc -sb $OdeTest::test_sb1_name "
              . "$OdeTest::test_sb >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );

  if ($status == 0) 
  {
    #Test 1 Passed
    OdeInterface::printResult( "pass", "normal", "resb", 1, "resb -rc -sb" );
  }
  else
  {
    #Test 1 Failed
    OdeInterface::printResult( "fail", "normal", "resb", 1, "resb -rc -sb" );
    $error = 1;
  }

  $command = "currentsb -rc $test_sb1_rc -back > $OdeTest::tmpfile";
  $status = OdeEnv::runOdeCommand( $command );
  if (( OdeFile::findInFile( $OdeTest::test_sb, $OdeTest::tmpfile ) ) && 
      !( OdeFile::findInFile( $OdeTest::test_bbdir, $OdeTest::tmpfile ) ))
  {
    #Test 2 Passed
    OdeInterface::printResult( "pass", "normal", "resb", 2, "resb -rc -sb" );
  }
  else
  {
    #Test 2 Failed
    OdeInterface::printResult( "fail", "normal", "resb", 2, "resb -rc -sb" );
    $error = 1;
  }
  OdeFile::deltree( \@delete_list );
  return $error;
}

    
#########################################
#
# sub regression
# - execute regression set of tests
#
#########################################
sub regression ()
{
  my $command;
  my $status;
  my $error = 0;
  my $reg_error = 0;
  my $error_txt;
  my $tmpstr;
  my $chgstr;
  my $test_sb1_rc = join( $OdeEnv::dirsep, ($OdeTest::test_sb1, "odetestrc1") );
  my $test_sb_sbconf = join( $OdeEnv::dirsep, 
                             ($OdeTest::test_sb, "rc_files", "sb.conf") );
  my $test_sb1_sbconf = join( $OdeEnv::dirsep, 
                             ($OdeTest::test_sb1, "rc_files", "sb.conf") );
  my @delete_list = ( $OdeTest::test_sb_rc, $OdeTest::test_sb, 
                      $OdeTest::test_sb1, $OdeTest::tmpfile );

  OdeInterface::logPrint( "in Resbtest - test level is regression\n");
  OdeEnv::runOdeCommand( "resb -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  #Remove any existing temporary files
  OdeFile::deltree( \@delete_list );
  
  $command = "mksb -dir $OdeTest::test_sbdir -rc $OdeTest::test_sb_rc "
             . "-back $OdeTest::test_bbdir $OdeTest::test_sb_name "
             . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || (OdeTest::validateSandbox( $OdeTest::test_sb,
                                        $OdeTest::test_sb_rc, \$error_txt ))) 
  {
    #Test case failed
    OdeInterface::printError( "resb : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  $command = "mksb -dir $OdeTest::test_sbdir -rc $test_sb1_rc "
             . "-back $OdeTest::test_bbdir $OdeTest::test_sb1_name "
             . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || (OdeTest::validateSandbox( $OdeTest::test_sb1,
                                        $test_sb1_rc, \$error_txt ))) 
  {
    #Test case failed
    OdeInterface::printError( "resb : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  ## Regression Test Case 1 - Retarget a sandbox 
  $command = "resb -rc $test_sb1_rc -sb $OdeTest::test_sb1_name "
              . "$OdeTest::test_sb >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "resb -rc rc_file -sb sb_name bbuild";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if (!($status == 0)) 
  {
    $reg_error = 1;
    $error_txt = "resb returned a non-zero exit code";
  }

  #backing build name in sb.conf always has forward slashes. So converting
  #back slashes in test_sb to forward slashes for non-unix platforms
  $chgstr = OdePath::unixize( $OdeTest::test_sb );
  if (!$reg_error && 
       !( OdeFile::findInFile( $chgstr, $test_sb1_sbconf ) )) 
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_sb not found in $test_sb1_sbconf";
  }

  $chgstr = OdePath::unixize( $OdeTest::test_bbdir );
  if (!$reg_error && 
       ( OdeFile::findInFile( $chgstr, $test_sb1_sbconf ) )) 
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_bbdir not removed from $test_sb1_sbconf";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "resb", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "resb", 1, $tmpstr );
  }
    
  ## Regression Test Case 2 - Retarget a sandbox to itself
  $command = "resb -rc $test_sb1_rc -sb $OdeTest::test_sb1_name "
             . "$OdeTest::test_sb1 "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  $tmpstr = "resb -rc rc_file -sb sb_name sb_name";
  #check command exit code
  if ($status == 0) 
  {
    $reg_error = 1;
    $error_txt = "resb returned a zero exit code instead of a non-zero";
  }

  $chgstr = OdePath::unixize( $OdeTest::test_sb );
  if (!$reg_error && 
       !( OdeFile::findInFile( $chgstr, $test_sb1_sbconf ) )) 
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_sb not found in $test_sb1_sbconf";
  }
        
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "resb", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "resb", 2, $tmpstr );
  }

  ## Regression Test Case 3 - Retarget a sandbox to non-existent backing build
  $command = "resb -rc $test_sb1_rc -sb $OdeTest::test_sb1_name -auto "
             . join( $OdeEnv::dirsep, 
                              ($OdeTest::test_sbdir, "non_existent_bb") )
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  $tmpstr = "resb -rc rc_file -sb sb_name non-existent-bbuild";
  #check command exit code
  if ($status == 0) 
  {
    $reg_error = 1;
    $error_txt = "resb returned a zero exit code instead of a non-zero";
  }
  $chgstr = OdePath::unixize( $OdeTest::test_sb );
  if (!$reg_error && 
       !( OdeFile::findInFile( $chgstr, $test_sb1_sbconf ) )) 
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_sb not found in $test_sb1_sbconf";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "resb", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "resb", 3, $tmpstr );
    OdeFile::deltree( \@delete_list );
  }
  return( $error );
}

#########################################
#
# sub fvt
# - execute FVT set of tests
#
#########################################
sub fvt ()
{
  #One test case can be to delete the backing build line from sb.conf and try
  #to retarget the sandbox
  print("fvt not implemented yet!\n");
}
             
1;
