#########################################
#
# Sbinfotest module
# - module to run Sbinfotest for different
#   test levels
#
#########################################

package Sbinfotest;
use strict;
use OdeUtil;
use OdeEnv;
use OdeTest;
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
  my @delete_list = ($OdeTest::test_sb_rc, $OdeTest::test_sb, 
                                           $OdeTest::tmpfile);
  OdeFile::deltree( \@delete_list );

  OdeInterface::logPrint( "in Sbinfotest - test level is normal\n");
  OdeEnv::runOdeCommand( "sbinfo -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");
  
  $command = "mksb -dir $OdeTest::test_sbdir -rc $OdeTest::test_sb_rc "
   . "-back $OdeTest::test_bbdir $OdeTest::test_sb_name "
   . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || !(-d $OdeTest::test_sb) || 
     !(-f join( $OdeEnv::dirsep, ($OdeTest::test_sb, "rc_files", "sb.conf") )) )
  {
    #Test case failed
    OdeInterface::printError( "sbinfo : mksb does not work" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  $command = "sbinfo -rc $OdeTest::test_sb_rc >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );

  if ($status == 0) 
  {
    #Test 1 Passed
    OdeInterface::printResult( "pass", "normal", "sbinfo", 1, "sbinfo -rc" );
  }
  else
  {
    #Test 1 Failed
    OdeInterface::printResult( "fail", "normal", "sbinfo", 1, "sbinfo -rc" );
    $error = 1;
  }

  $command = "sbinfo -rc $OdeTest::test_sb_rc > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  if (OdeFile::findInFile( 
             "MAKESYSPATH=$OdeTest::test_sb", $OdeTest::tmpfile, 0 ))
  {
    #Test 2 Passed
    OdeInterface::printResult( "pass", "normal", "sbinfo", 2, "sbinfo -rc" );
  }
  else
  {
    #Test 2 Failed
    OdeInterface::printResult( "fail", "normal", "sbinfo", 2, "sbinfo -rc" );
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
  my $filestr;
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my $test_sb1_rc = $OdeTest::test_sb1 . $OdeEnv::dirsep . "odetestrc";
  my @delete_list = ($OdeTest::test_sb_rc, $OdeTest::test_sb, 
                     $OdeTest::test_sb1, $OdeTest::tmpfile, $cmd_file);

  OdeInterface::logPrint( "in sbinfotest - test level is regression\n");
  OdeEnv::runOdeCommand( "sbinfo -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

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
    OdeInterface::printError( "sbinfo : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  $command = "mksb -auto -back $OdeTest::test_sb -rc $OdeTest::test_sb_rc "
             . $OdeTest::test_sb1_name . " -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || (OdeTest::validateSandbox( $OdeTest::test_sb1,
                                        $OdeTest::test_sb_rc, \$error_txt ))) 
  {
    #Test case failed
    OdeInterface::printError( "sbinfo : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  ## Regression Test Case 1 - run sbinfo -rc 
  ## Should print information about the default sandbox
  $command = "sbinfo -rc $OdeTest::test_sb_rc > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "sbinfo -rc";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "sbinfo returned a non-zero exit code";
  }
  #Check MAKESYSPATH environment variable
  $filestr = $OdeTest::test_sb . $OdeEnv::dirsep . "src" . $OdeEnv::dirsep .
             "rules_mk" . $OdeEnv::pathsep . $OdeTest::test_bbdir . 
             $OdeEnv::dirsep . "src" . $OdeEnv::dirsep . "rules_mk";
  if (!$reg_error && !(OdeFile::findInFile( 
                        "MAKESYSPATH=$filestr", $OdeTest::tmpfile, 1 )))
  {
    $reg_error = 1;
    $error_txt = "MAKESYSPATH environment variable not defined properly";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "sbinfo", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "sbinfo", 1, $tmpstr );
  }

  ## Regression Test Case 2 - run sbinfo -rc -sb
  ## Should print information about the specified sandbox
  $command = "sbinfo -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name"
             . " > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "sbinfo -rc -sb";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "sbinfo returned a non-zero exit code";
  }
  #Check MAKESYSPATH environment variable
  $filestr = $OdeTest::test_sb1 . $OdeEnv::dirsep . "src" . $OdeEnv::dirsep .
             "rules_mk" . $OdeEnv::pathsep .
             $OdeTest::test_sb . $OdeEnv::dirsep . "src" . $OdeEnv::dirsep .
             "rules_mk" . $OdeEnv::pathsep . $OdeTest::test_bbdir . 
             $OdeEnv::dirsep . "src" . $OdeEnv::dirsep . "rules_mk";
  if (!$reg_error && !(OdeFile::findInFile( 
                        "MAKESYSPATH=$filestr", $OdeTest::tmpfile, 1 )))
  {
    $reg_error = 1;
    $error_txt = "MAKESYSPATH environment variable not defined properly";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "sbinfo", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "sbinfo", 2, $tmpstr );
  }

  ## Regression Test Case 3 - run sbinfo inside workon
  ## Should print information about the current sandbox
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "sbinfo > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n");
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -rc -sb; sbinfo; exit";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: sbinfo; exit;\n" );
  close( BLDLOG );
  # Copy output of sbinfo from  tmpfile to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "workon returned a non-zero exit code";
  }
  #Check MAKESYSPATH environment variable
  $filestr = $OdeTest::test_sb . $OdeEnv::dirsep . "src" . $OdeEnv::dirsep .
             "rules_mk" . $OdeEnv::pathsep . $OdeTest::test_bbdir . 
             $OdeEnv::dirsep . "src" . $OdeEnv::dirsep . "rules_mk";
  if (!$reg_error && !(OdeFile::findInFile( 
                        "MAKESYSPATH=$filestr", $OdeTest::tmpfile, 1 )))
  {
    $reg_error = 1;
    $error_txt = "MAKESYSPATH environment variable not defined properly";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "sbinfo", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "sbinfo", 3, $tmpstr );
  }

  ## Regression Test Case 4 - run sbinfo -rc -sb inside workon
  ## Should print information about the specified sandbox
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n");
  print( INPUT 
     "sbinfo -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
     . "> $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n");
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -rc -sb; sbinfo -rc -sb; exit";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: sbinfo -rc $OdeTest::test_sb_rc $OdeTest::test_sb1_name; exit;\n" );
  close( BLDLOG );
  # Copy output of sbinfo from  tmpfile to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "workon returned a non-zero exit code";
  }
  #Check MAKESYSPATH environment variable
  $filestr = $OdeTest::test_sb1 . $OdeEnv::dirsep . "src" . $OdeEnv::dirsep .
             "rules_mk" . $OdeEnv::pathsep .
             $OdeTest::test_sb . $OdeEnv::dirsep . "src" . $OdeEnv::dirsep .
             "rules_mk" . $OdeEnv::pathsep . $OdeTest::test_bbdir . 
             $OdeEnv::dirsep . "src" . $OdeEnv::dirsep . "rules_mk";
  if (!$reg_error && !(OdeFile::findInFile( 
                        "MAKESYSPATH=$filestr", $OdeTest::tmpfile, 1 )))
  {
    $reg_error = 1;
    $error_txt = "MAKESYSPATH environment variable not defined properly";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "sbinfo", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "sbinfo", 4, $tmpstr );
  }

  # Since OS2 shell does not return non-zero if the command run does not
  # return zero, this test should be avoided on OS2
  if (!defined( $OdeEnv::OS2 ))
  {
    ## Regression Test Case 5 - run sbinfo -rc inside workon
    ## The rc_file is invalid. Should give an error
    open( INPUT, "> $cmd_file" ) ||
     OdeInterface::logPrint( "Could not open file $cmd_file\n");
    print( INPUT "sbinfo -rc $OdeTest::tmpfile > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
    print( INPUT "exit\n");
    close( INPUT );
    $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb_name "
              . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $tmpstr = "workon -rc -sb; sbinfo -rc invalid-rc-file; exit";
    $status = OdeEnv::runOdeCommand( $command );
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG "Executing: sbinfo -rc $OdeTest::tmpfile; exit;\n" );
    print( BLDLOG "Executing: sbinfo; exit;\n" );
    close( BLDLOG );
    # Copy output of sbinfo from  tmpfile to build logfile
    OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
    # Check command exit code
    if ($status == 0)
    {
      $reg_error = 1;
      $error_txt = "workon returned a zero exit code";
    }
    #Check if the output of sbinfo is correct
    if (!$reg_error && !(OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
    {
      $reg_error = 1;
      $error_txt = "The command did not print an error message";
    }
    if ($reg_error)
    {
      #Test case failed
      OdeInterface::printResult( "fail", "regression", "sbinfo", 5, $tmpstr );
      OdeInterface::logPrint( "Test Failed: $error_txt\n" );
      $error = 1;
      $reg_error = 0;
    }
    else
    {
      #Test case passed
      OdeInterface::printResult( "pass", "regression", "sbinfo", 5, $tmpstr );
    }
  }
  OdeFile::deltree( \@delete_list );
  return $error;
}

#########################################
#
# sub fvt
# - execute FVT set of tests
#
#########################################
sub fvt ()
{
  print("fvt not implemented yet!\n");
}
             
1;
