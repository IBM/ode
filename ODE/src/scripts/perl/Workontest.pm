#########################################
#
# Workontest module
# - module to run workon for different
#   test levels
#
#########################################

package Workontest;

use File::Path;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdeUtil;
use OdeInterface;
use strict;

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
  if ( $OdeUtil::arghash{'testlevel'} eq "regression" )
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
      "ERROR: undefined testlevel $OdeUtil::arghash{'testlevel'}\n" );
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
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my $sb_src_dir = $OdeTest::test_sb . $OdeEnv::dirsep . "src";
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb_rc, 
                     $cmd_file, $OdeTest::tmpfile);

  OdeInterface::logPrint( "in Workontest - test level is normal\n");
  
  OdeEnv::runOdeCommand( "workon -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  # remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  $command = "mksb -auto -back $OdeTest::test_bbdir -dir $OdeTest::test_sbdir"
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine "
             . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  # Create sandbox
  $status = OdeEnv::runOdeCommand( $command );
  if ( ($status == 0) && (-d $OdeTest::test_sb) && (-f $OdeTest::test_sb_conf))
  {
    # call workon with "exit" command - Test #1
    open( INPUT, "> $cmd_file" ) ||
      OdeInterface::logPrint( "Could not open file $cmd_file\n" );
    print( INPUT "exit\n" );
    close( INPUT );
    $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file "
               . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command );
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG "Executing: exit;\n" );
    close( BLDLOG );
    if ($status == 0)
    {
      OdeInterface::printResult( "pass", "normal", "workon", 1, 
                                 "workon -rc; exit");
    }
    else
    {
      OdeInterface::printResult( "fail", "normal", "workon", 1, 
                                 "workon -rc; exit");
      $error = 1;
    }
    # call workon with "pwd", and "exit" commands - Test #2
    open( INPUT, "> $cmd_file" ) ||
      OdeInterface::logPrint( "Could not open file $cmd_file\n" );
    print( INPUT "$OdeEnv::pwd > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
    print( INPUT "exit\n" );
    close( INPUT );
    $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file "
               . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command );
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG "Executing: pwd; exit;\n" );
    close( BLDLOG );
    # Copy contents of tempfile into build logfile
    OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
    if (($status == 0) && 
            (OdeFile::findInFile( $sb_src_dir, $OdeTest::tmpfile, 1 )))
    {
      OdeInterface::printResult( "pass", "normal", "workon", 2, 
                                 "workon -rc; pwd; exit");
    }
    else
    {
      OdeInterface::printResult( "fail", "normal", "workon", 2, 
                                 "workon -rc; pwd; exit");
      $error = 1;
    }
  }
  else
  {
    # mksb doesn't work
    OdeInterface::printError( "workon : mksb does not work!"); 
    $error = 1;
  } 

  OdeFile::deltree( \@delete_list );

  return( $error );
}

#########################################
#
# sub regression
# - execute regresssion set of tests
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
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my $pwd_output_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "pwd_output.txt";
  my $sb_src_dir = $OdeTest::test_sb . $OdeEnv::dirsep . "src";
  my $sb1_src_dir = $OdeTest::test_sb1 . $OdeEnv::dirsep . "src";
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb_1, 
                     $OdeTest::test_sb_rc, $cmd_file, $OdeTest::tmpfile,
                     $pwd_output_file);
  my $bck_sand_dir;
  my $bck_src_dir;

  OdeInterface::logPrint( "in Workontest - test level is regression\n");
  
  OdeEnv::runOdeCommand( "workon -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  # remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  # Create sandboxes
  $command = "mksb -auto -back $OdeTest::test_bbdir -dir $OdeTest::test_sbdir"
              . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine "
              . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );  # Check command exit code
  $reg_error = OdeTest::validateSandbox( 
                     $OdeTest::test_sb, $OdeTest::test_sb_rc, \$error_txt );
  if (($status != 0) || ($reg_error != 0) )
  {
    OdeInterface::printError( "workon : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }  
  $command = "mksb -auto -back $OdeTest::test_bbdir"
             . " -rc $OdeTest::test_sb_rc $OdeTest::test_sb1_name"
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );  # Check command exit code
  $reg_error = OdeTest::validateSandbox( 
                 $OdeTest::test_sb1, $OdeTest::test_sb_rc, \$error_txt );
  if (($status != 0) || ($reg_error != 0) )
  {
    OdeInterface::printError( "workon : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }      

  ## Regression Test Case 1 - workon; pwd, and exit
  $reg_error = 0;
  $tmpstr = "workon -rc; pwd; exit";
  open( INPUT, "> $cmd_file" ) ||
     OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "$OdeEnv::pwd > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: pwd; exit;\n" );
  close( BLDLOG );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "workon returned a non-zero exit code";
  }
  # Check working directory was "src"
  if (!$reg_error && 
      !( OdeFile::findInFile( $sb_src_dir, $OdeTest::tmpfile, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "workon did not cd to the sandbox src directory";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "workon", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $reg_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", "regression", "workon", 1, $tmpstr );
  }

  ## Regression Test Case 2 - workon -sb; pwd, and exit
  $tmpstr = "workon -rc -sb; pwd; exit";
  open( INPUT, "> $cmd_file" ) ||
     OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "$OdeEnv::pwd > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name"
             . " < $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: pwd; exit;\n" );
  close( BLDLOG );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "workon returned a non-zero exit code";
  }
  # Check working directory was "src"
  if (!$reg_error && 
      !( OdeFile::findInFile( $sb1_src_dir, $OdeTest::tmpfile, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "workon did not cd to the sandbox src directory";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "workon", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $reg_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", "regression", "workon", 2, $tmpstr );
  }

  ## Regression Test Case 3 - check environment vars
  $tmpstr = "workon -rc; $OdeEnv::list_env";
  open( INPUT, "> $cmd_file" ) ||
     OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "$OdeEnv::list_env > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: $OdeEnv::list_env; exit;\n" );
  close( BLDLOG );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "workon returned a non-zero exit code";
  }
  # Check WORKON environment variable
  if (!$reg_error && 
      !( OdeFile::findInFile( "WORKON=1", $OdeTest::tmpfile, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "WORKON environment variable not defined properly";
  }
  # Check CONTEXT environment variable
  if (!$reg_error && 
      !( OdeFile::findInFile( "CONTEXT=$OdeEnv::machine", 
                             $OdeTest::tmpfile, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "CONTEXT environment variable not defined properly";
  }
  # Check BACKED_SANDBOXDIR 
  $bck_sand_dir = $OdeTest::test_sb . $OdeEnv::pathsep . $OdeTest::test_bbdir;
  $bck_src_dir = $sb_src_dir . $OdeEnv::pathsep . $OdeTest::test_bbdir 
                  . $OdeEnv::dirsep . "src";
  if (!$reg_error && 
      !( OdeFile::findInFile( 
             "BACKED_SANDBOXDIR=$bck_sand_dir", $OdeTest::tmpfile, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "BACKED_SANDBOXDIR environment variable not defined properly";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "workon", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $reg_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", "regression", "workon", 3, $tmpstr );
  }

  ## Regression Test Case 4 - check environment vars using -c
  $tmpstr = "workon -rc -c $OdeEnv::list_env";
  # WARNING! It is assumed that $OdeEnv::list_env returns a single token for
  # the command with no whitespace in it. Thus quotes are not needed around
  # the result. If someday we have a platform where this is not so, we will need
  # some way to get platform-independent command line quotes to be substituted
  # in the following line, also with appropriate escapes if needed.
  $command = "workon -rc $OdeTest::test_sb_rc -c $OdeEnv::list_env "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: $OdeEnv::list_env; exit;\n" );
  close( BLDLOG );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "workon returned a non-zero exit code";
  }
  # Check WORKON environment variable
  if (!$reg_error && 
      !( OdeFile::findInFile( "WORKON=1", $OdeTest::tmpfile, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "WORKON environment variable not defined properly";
  }
  # Check CONTEXT environment variable
  if (!$reg_error && 
      !( OdeFile::findInFile( "CONTEXT=$OdeEnv::machine", 
                             $OdeTest::tmpfile, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "CONTEXT environment variable not defined properly";
  }
  # Check BACKED_SANDBOXDIR
  $bck_sand_dir = $OdeTest::test_sb . $OdeEnv::pathsep . $OdeTest::test_bbdir;
  $bck_src_dir = $sb_src_dir . $OdeEnv::pathsep . $OdeTest::test_bbdir 
                  . $OdeEnv::dirsep . "src";
  if (!$reg_error && 
      !( OdeFile::findInFile( 
             "BACKED_SANDBOXDIR=$bck_sand_dir", $OdeTest::tmpfile, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "BACKED_SANDBOXDIR environment variable not defined properly";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "workon", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $reg_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", "regression", "workon", 4, $tmpstr );
  }

  ## Regression Test Case 5 - check command execution using -k
  $tmpstr = "workon -rc -k $OdeEnv::list_env; pwd; exit";
  # WARNING! It is assumed that $OdeEnv::list_env returns a single token for
  # the command with no whitespace in it. Thus quotes are not needed around
  # the result. If someday we have a platform where this is not so, we will need
  # some way to get platform-independent command line quotes to be substituted
  # in the following line, also with appropriate escapes if needed.
  open( INPUT, "> $cmd_file" ) ||
     OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "$OdeEnv::pwd > $pwd_output_file $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -k $OdeEnv::list_env "
             . " < $cmd_file > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: $OdeEnv::list_env; pwd; exit;\n" );
  close( BLDLOG );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  OdeFile::concatFiles( $pwd_output_file, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "workon returned a non-zero exit code";
  }
  # Check WORKON environment variable
  if (!$reg_error && 
      !( OdeFile::findInFile( "WORKON=1", $OdeTest::tmpfile, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "WORKON environment variable not defined properly";
  }
  # Check CONTEXT environment variable
  if (!$reg_error && 
      !( OdeFile::findInFile( "CONTEXT=$OdeEnv::machine", 
                             $OdeTest::tmpfile, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "CONTEXT environment variable not defined properly";
  }
  # Check BACKED_SANDBOXDIR 
  $bck_sand_dir = $OdeTest::test_sb . $OdeEnv::pathsep . $OdeTest::test_bbdir;
  $bck_src_dir = $sb_src_dir . $OdeEnv::pathsep . $OdeTest::test_bbdir 
                  . $OdeEnv::dirsep . "src";
  if (!$reg_error && 
      !( OdeFile::findInFile( 
             "BACKED_SANDBOXDIR=$bck_sand_dir", $OdeTest::tmpfile, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "BACKED_SANDBOXDIR environment variable not defined properly";
  }
  # Check working directory was "src"
  if (!$reg_error && 
      !( OdeFile::findInFile( $sb_src_dir, $pwd_output_file, 1 )) )
  {
    $reg_error = 1;
    $error_txt = "workon did not cd to the sandbox src directory";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "workon", 5, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $reg_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", "regression", "workon", 5, $tmpstr );
  }

  # Since OS2 shell does not return non-zero if the command run does not
  # return zero, this test should be avoided on OS2 
  if (!defined( $OdeEnv::OS2 ))
  {
    ## Regression Test Case 6 - have workon return non-zero exit code
    $tmpstr = "workon -rc; <invalid command>; exit";
    open( INPUT, "> $cmd_file" ) ||
      OdeInterface::logPrint( "Could not open file $cmd_file\n" );
    # Invalid command, should return non-zero
    print( INPUT "workon -badflag > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" ); 
    print( INPUT "exit\n" );
    close( INPUT );
    $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file "
                . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command );
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG "Executing: workon -badflag; exit;\n" );
    close( BLDLOG );
    # Copy output from output file to build logfile
    OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
    # Check command exit code
    if ($status == 0)
    {
      $reg_error = 1;
      $error_txt = "workon returned a zero exit code";
      $error = 1;
      OdeInterface::printResult( "fail", "regression", "workon", 6, $tmpstr );
      OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    }
    else
    {
      OdeInterface::printResult( "pass", "regression", "workon", 6, $tmpstr );
    }
  }

  OdeFile::deltree( \@delete_list );
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
 
  OdeInterface::logPrint("fvt not implemented yet!\n");
}

1;
