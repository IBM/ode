#########################################
#
# Currentsbtest module
# - module to run Currentsbtest for different
#   test levels
#
#########################################

package Currentsbtest;
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
  my @delete_list = ($OdeTest::test_sb_rc, $OdeTest::test_sb, 
                                           $OdeTest::tmpfile);
  OdeFile::deltree( \@delete_list );

  OdeInterface::logPrint( "in Currentsbtest - test level is normal\n");
  OdeEnv::runOdeCommand( "currentsb -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");
  
  $command = "mksb -dir $OdeTest::test_sbdir -rc $OdeTest::test_sb_rc "
   . "-back $OdeTest::test_bbdir $OdeTest::test_sb_name "
   . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || !(-d $OdeTest::test_sb) || 
     !(-f join( $OdeEnv::dirsep, ($OdeTest::test_sb, "rc_files", "sb.conf") )) )
  {
    #Test case failed
    OdeInterface::printError( "currentsb : mksb does not work" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  $command = "currentsb -rc $OdeTest::test_sb_rc >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );

  if ($status == 0) 
  {
    #Test 1 Passed
    OdeInterface::printResult( "pass", "normal", "currentsb", 1, 
                               "currentsb -rc" );
  }
  else
  {
    #Test 1 Failed
    OdeInterface::printResult( "fail", "normal", "currentsb", 1, 
                               "currentsb -rc" );
    $error = 1;
  }

  $command = "currentsb -rc $OdeTest::test_sb_rc -back > $OdeTest::tmpfile";
  $status = OdeEnv::runOdeCommand( $command );
  if (OdeFile::findInFile( $OdeTest::test_bbdir, $OdeTest::tmpfile ) )
  {
    #Test 2 Passed
    OdeInterface::printResult( "pass", "normal", "currentsb", 2, 
                               "currentsb -rc -back" );
  }
  else
  {
    #Test 2 Failed
    OdeInterface::printResult( "fail", "normal", "currentsb", 2, 
                               "currentsb -rc -back" );
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

  OdeInterface::logPrint( "in Currentsbtest - test level is regression\n");
  OdeEnv::runOdeCommand( "currentsb -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  #Remove any existing temporary files
  OdeFile::deltree( \@delete_list );
  
  $command = "mksb -dir $OdeTest::test_sbdir -rc $OdeTest::test_sb_rc "
             . "-back $OdeTest::test_bbdir $OdeTest::test_sb_name "
             . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  $reg_error = OdeTest::validateSandbox( $OdeTest::test_sb,
                                        $OdeTest::test_sb_rc, \$error_txt );
  if (($status != 0) || ($reg_error != 0))
  {
    #Test case failed
    OdeInterface::printError( "currentsb : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  $command = "mksb -auto -back $OdeTest::test_sb -rc $test_sb1_rc -auto "
             . "-dir $OdeTest::test_sbdir -m $OdeEnv::machine "
             . $OdeTest::test_sb1_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  $reg_error = OdeTest::validateSandbox( $OdeTest::test_sb1,
                                        $test_sb1_rc, \$error_txt );
  if (($status != 0) || ($reg_error != 0))
  {
    #Test case failed
    OdeInterface::printError( "currentsb : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  ## Regression Test Case 1 - run currentsb -rc -all
  $command = "currentsb -rc $OdeTest::test_sb_rc -all > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "currentsb -rc -all";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "currentsb returned a non-zero exit code";
  }
  $filestr = "$OdeTest::test_sb_name $OdeTest::test_sbdir ";
  $filestr = $filestr . "$OdeTest::test_bbdir";
  if (!$reg_error && !(OdeFile::findInFile( $filestr, $OdeTest::tmpfile, 1 )))
  {
    $reg_error = 1;
    $error_txt = "The output of the command is incorrect";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "currentsb", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "currentsb", 1, $tmpstr );
  }

  ## Regression Test Case 2 - run currentsb -rc -chain
  $command = "currentsb -rc $test_sb1_rc -chain > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "currentsb -rc -chain";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "currentsb returned a non-zero exit code";
  }
  open( FILEHND, "< $OdeTest::tmpfile" ) ||
     OdeInterface::logPrint( "Could not open file $OdeTest::tmpfile\n" );
  my $string1 = $OdeTest::test_sb1;
  my $string2 = $OdeTest::test_sb;
  my $string3 = $OdeTest::test_bbdir;
  my $string4 = readline( *FILEHND );
  my $string5 = readline( *FILEHND );
  my $string6 = readline( *FILEHND );
  close( FILEHND );
  chop $string4;  #remove end of line character from the strings read from file
  chop $string5;
  chop $string6;

  if (!$reg_error && (!($string4 eq $string1) ||
                      !($string5 eq $string2) ||
                      !($string6 eq $string3)))
  {
    $reg_error = 1;
    $error_txt = "The output of the command is incorrect";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "currentsb", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "currentsb", 2, $tmpstr );
  }

  ## Regression Test Case 3 - run currentsb -rc bad_rc_file
  $command = "currentsb -rc bad_rc_file -chain > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "currentsb -rc -chain";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status == 0) 
  {
    $reg_error = 1;
    $error_txt = "currentsb returned a zero exit code";
  }
  if (!$reg_error && !(OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $reg_error = 1;
    $error_txt = "The command did not print an error message";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "currentsb", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "currentsb", 3, $tmpstr );
  }

  ## Regression Test Case 4 - run currentsb -all inside workon
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "currentsb -all > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $test_sb1_rc -sb $OdeTest::test_sb1_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -rc -sb; currentsb -all; exit";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: currentsb -all; exit;\n" );
  close( BLDLOG );
  # Copy output of currentsb from  tmpfile to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "workon returned a non-zero exit code";
  }
  #Check if the output of currentsb is correct
  $filestr = "$OdeTest::test_sb1_name $OdeTest::test_sbdir ";
  $filestr = $filestr . "$OdeTest::test_sb";
  if (!$reg_error &&
      !( OdeFile::findInFile( $filestr, $OdeTest::tmpfile, 1 ) )) 
  {
    $reg_error = 1;
    $error_txt = "The output of the command is incorrect";
  }   

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "currentsb", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "currentsb", 4, $tmpstr );
  }

  ## Regression Test Case 5 - run currentsb -rc with a missing backing_build
  ## in the sandbox chain
  $command = "mksb -undo -rc $OdeTest::test_sb_rc $OdeTest::test_sb_name"
             . " -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) && (-d $OdeTest::test_sb))
  {
    #Test case failed
    OdeInterface::logPrint( "currentsbtest: mksb -undo does not work\n" );
    OdeInterface::logPrint( "Test Failed\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  $command = "currentsb -rc $test_sb1_rc -chain > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "currentsb -rc -chain";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "currentsb returned a non-zero exit code";
  }
  if (!$reg_error && !(OdeFile::findInFile( "WARNING", $OdeTest::tmpfile )))
  {
    $reg_error = 1;
    $error_txt = "The command did not give a warning message";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "currentsb", 5, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "currentsb", 5, $tmpstr );
  }

  ## Regression Test Case 6 - run currentsb -rc -all, with explicitly
  ## specified sandbox.
  ## Recreating the sandbox deleted in case 5, except it now will be in the
  ## same rc file as the other sandbox. Also make it the default sandbox.
  $command = "mksb -dir $OdeTest::test_sbdir -rc $test_sb1_rc "
            . "-back $OdeTest::test_bbdir $OdeTest::test_sb_name -def "
            . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );

  $command = "currentsb -rc $test_sb1_rc -all $OdeTest::test_sb_name" .
             " > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "currentsb -rc -all, with explicit sandbox";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "currentsb returned a non-zero exit code";
  }
  $filestr = "$OdeTest::test_sb_name $OdeTest::test_sbdir ";
  $filestr = $filestr . "$OdeTest::test_bbdir";
  if (!$reg_error && !(OdeFile::findInFile( $filestr, $OdeTest::tmpfile, 1 )))
  {
    $reg_error = 1;
    $error_txt = "The output of the command is incorrect";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "currentsb", 6, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "currentsb", 6, $tmpstr );
  }

  ## Regression Test Case 7 - run currentsb -rc -chain
  $command = "currentsb -rc $test_sb1_rc -chain $OdeTest::test_sb1_name"
           . " > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "currentsb -rc -chain, with explicit sandbox";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "currentsb returned a non-zero exit code";
  }
  open( FILEHND, "< $OdeTest::tmpfile" ) ||
     OdeInterface::logPrint( "Could not open file $OdeTest::tmpfile\n" );
  my $string1 = $OdeTest::test_sb1;
  my $string2 = $OdeTest::test_sb;
  my $string3 = $OdeTest::test_bbdir;
  my $string4 = readline( *FILEHND );
  my $string5 = readline( *FILEHND );
  my $string6 = readline( *FILEHND );
  close( FILEHND );
  chop $string4;  #remove end of line character from the strings read from file
  chop $string5;
  chop $string6;

  if (!$reg_error && (!($string4 eq $string1) ||
                      !($string5 eq $string2) ||
                      !($string6 eq $string3)))
  {
    $reg_error = 1;
    $error_txt = "The output of the command is incorrect";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "currentsb", 7, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "currentsb", 7, $tmpstr );
  }

  # Since OS2 shell does not return non-zero if the command run does not
  # return zero, this test should be avoided on OS2
  if (!defined( $OdeEnv::OS2 ))
  {
    ## Regression Test Case 8 - run currentsb -rc inside workon
    ## The current sandbox is not present in the rc_file. Should 
    ## give an error

    # First, delete the sandbox created in case 6; it is in the wrong rcfile
    # for this test.
    $command = "mksb -undo -rc $test_sb1_rc $OdeTest::test_sb_name"
               . " -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
    $status = OdeEnv::runOdeCommand( $command );
    if (($status != 0) && (-d $OdeTest::test_sb))
    {
      #Test case failed
      OdeInterface::logPrint( "currentsbtest: mksb -undo does not work\n" );
      OdeInterface::logPrint( "Test Failed\n" );
      $error = 1;
      OdeFile::deltree( \@delete_list );
      return $error;
    }

    ## Recreating the sandbox deleted in case 5
    $command = "mksb -dir $OdeTest::test_sbdir -rc $OdeTest::test_sb_rc "
              . "-back $OdeTest::test_bbdir $OdeTest::test_sb_name "
              . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
    $status = OdeEnv::runOdeCommand( $command );
    open( INPUT, "> $cmd_file" ) ||
     OdeInterface::logPrint( "Could not open file $cmd_file\n" );
    print( INPUT 
          "currentsb -rc $OdeTest::test_sb_rc > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
    print( INPUT "exit\n" );
    close( INPUT );
    $command = "workon -rc $test_sb1_rc -sb $OdeTest::test_sb1_name "
              . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $tmpstr = "workon -rc -sb; currentsb -rc; exit";
    $status = OdeEnv::runOdeCommand( $command );
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG "Executing: currentsb -rc $OdeTest::test_sb_rc; exit;\n" );
    close( BLDLOG );
    # Copy output of currentsb from  tmpfile to build logfile
    OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
    # Check command exit code
    if ($status == 0)
    {
      $reg_error = 1;
      $error_txt = "workon returned a zero exit code";
    }
    #Check if the output of currentsb is correct
    if (!$reg_error && !(OdeFile::findInFile( "ERROR", $OdeTest::tmpfile ))) 
    {
      $reg_error = 1;
      $error_txt = "The command did not print an error message";
    }   
    if ($reg_error)
    {
      #Test case failed
      OdeInterface::printResult( "fail", "regression", "currentsb", 8, 
                                                             $tmpstr );
      OdeInterface::logPrint( "Test Failed: $error_txt\n" );
      $error = 1;
      $reg_error = 0;
    }
    else
    {
      #Test case passed
      OdeInterface::printResult( "pass", "regression", "currentsb", 8, 
                                                             $tmpstr );
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
