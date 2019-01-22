#########################################
#
# Mksbtest module
# - module to run mksbtest for different
#   test levels
#
#########################################

package Mksbtest;

use File::Path;
use File::Copy;
use File::Basename;
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
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb_rc);

  OdeInterface::logPrint( "in Mksbtest - test level is normal\n");
  
  OdeEnv::runOdeCommand( "mksb -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  # remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  $command = "mksb -auto -back $OdeTest::test_bbdir -dir $OdeTest::test_sbdir"
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine "
             . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  # Create sandbox - test #1
  $status = OdeEnv::runOdeCommand( $command );
  if ( ($status == 0) && (-d $OdeTest::test_sb) && (-f $OdeTest::test_sb_conf))
  {
    # Test #1 passed
    OdeInterface::printResult( "pass", "normal", "mksb", 1, 
                                "mksb -back -dir -rc -m" );
    # Remove sandbox - test #2
    $command = "mksb -rc $OdeTest::test_sb_rc -auto -undo "
               . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command );
    if ( ($status == 0) && (! -d $OdeTest::test_sb) )
    {      
      # Test #2 passed
      OdeInterface::printResult( "pass", "normal", "mksb", 2, 
                                 "mksb -undo -auto");
    } 
    else
    {      
      # Test #2 failed
      OdeInterface::printResult( "fail", "normal", "mksb", 2, 
                                 "mksb -undo -auto");
      $error = 1;
    } 
  }
  else
  {
    # Test #1 failed
    OdeInterface::printResult( "fail", "normal", "mksb", 1, 
                                "mksb -back -dir -rc -m" );
    $error = 1;
  } 

  OdeFile::deltree( \@delete_list );

  return( $error );
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
  my $sb_src_dir = $OdeTest::test_sb . $OdeEnv::dirsep . "src";
  my $server_c = 
    join( $OdeEnv::dirsep, ($sb_src_dir, "bin", "server", "server.c"));
  my $buildserver_c = 
    join( $OdeEnv::dirsep, ($sb_src_dir, "bin", "server", "buildserver.c"));
  my $retserver_c = 
    join( $OdeEnv::dirsep, ($sb_src_dir, "bin", "server", "retserver.c"));
  my $server_makefile = 
    join( $OdeEnv::dirsep, ($sb_src_dir, "bin", "server", "makefile"));
  my $num_bb_files;
  my $num_sb_files;
  my $bb_src_size;
  my $sb_src_size;
  my $tmpstr;
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb1, 
                     $OdeTest::test_sb_rc, $OdeTest::tmpfile);

  OdeInterface::logPrint( "in Mksbtest - test level is regression\n");
  OdeEnv::runOdeCommand( "mksb -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  ## Regression Test Case 1 - make a sandbox
  $command = "mksb -auto -back $OdeTest::test_bbdir -dir $OdeTest::test_sbdir"
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine -auto "
             . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mksb -back -dir -rc -m -auto";
  $status = OdeEnv::runOdeCommand( $command ); 
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mksb returned a non-zero exit code";
  }
  # Validate Sandbox
  if (!$reg_error)
  {
    $reg_error = OdeTest::validateSandbox( $OdeTest::test_sb, 
                                          $OdeTest::test_sb_rc, \$error_txt );
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mksb", 1, $tmpstr );
  }

  ## Regression Test Case 2 - make another sandbox
  $command = "mksb -auto -back $OdeTest::test_bbdir"
             . " -rc $OdeTest::test_sb_rc -def -auto "
             . $OdeTest::test_sb1_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mksb -back -rc -def -auto";
  $status = OdeEnv::runOdeCommand( $command ); 
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mksb returned a non-zero exit code";
  }
  # Validate Sandbox
  if (!$reg_error)
  {
    $reg_error = OdeTest::validateSandbox( $OdeTest::test_sb1, 
                                          $OdeTest::test_sb_rc, \$error_txt );
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mksb", 2, $tmpstr );
  }

  ## Regression Test Case 3 - list the sandboxes
  $command = "mksb -auto -rc $OdeTest::test_sb_rc -list"
             . " > $OdeTest::tmpfile";
  $tmpstr = "mksb -rc -list";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from tempfile to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mksb -list returned a non-zero exit code";
  }

  if (!$reg_error && 
      !( OdeFile::findInFile( "Default Sandbox: $OdeTest::test_sb1_name", 
                              $OdeTest::tmpfile) ))
  {
    $reg_error = 1;
    $error_txt = "Default Sandbox not correct in mksb -list output";
  }

  if (!$reg_error && 
      !( OdeFile::findInFile( "Base: $OdeEnv::tempdir", $OdeTest::tmpfile) ))
  {
    $reg_error = 1;
    $error_txt = "Base not correct in mksb -list output";
  }

  if (!$reg_error && 
      !( OdeFile::findInFile( "Sandbox: $OdeTest::test_sb_name", 
                               $OdeTest::tmpfile) ))
  {
    $reg_error = 1;
    $error_txt = "Sandbox not correct in mksb -list output";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return $error;
  } 
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mksb", 3, $tmpstr );
  }

  ## Regression Test Case 4 - test -def and -m on existing sandbox
  $command = "mksb -auto -rc $OdeTest::test_sb_rc -def -auto " 
             . "-m $OdeEnv::machine "
             . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mksb -rc -def -auto -m for existing sandbox";
  $status = OdeEnv::runOdeCommand( $command ); 
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mksb returned a non-zero exit code";
  }

  # Validate Sandbox
  if (!$reg_error)
  {
    $reg_error = OdeTest::validateSandbox( $OdeTest::test_sb, 
                                          $OdeTest::test_sb_rc, \$error_txt );
  }

  if (!$reg_error && !(OdeFile::findInFile( "default $OdeTest::test_sb_name", 
                                            $OdeTest::test_sb_rc )))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_sb_name not set to default in $OdeTest::test_sb_rc";
  }

  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mksb", 4, $tmpstr );
  }

  ## Regression Test Case 5 - remove the second sandbox previously created
  $command = "mksb -rc $OdeTest::test_sb_rc -auto -undo "
             . $OdeTest::test_sb1_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mksb -rc -auto -undo";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mksb -undo returned a non-zero exit code";
  }
  if (!$reg_error && (-d $OdeTest::test_sb1))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_sb1_name not deleted";
  }
  if (!$reg_error &&
      OdeFile::findInFile( $OdeTest::test_sb1_name, $OdeTest::test_sb_rc ))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_sb1_name not deleted from ";
    $error_txt = $error_txt . $OdeTest::test_sb_rc;
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", 5, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return $error;
  }   
  else
  {
    #Test Case Passed
    OdeInterface::printResult( "pass", "regression", "mksb", 5, $tmpstr );
  }

  ## Regression Test Case 6 - remove the first sandbox previously created
  $command = "mksb -rc $OdeTest::test_sb_rc -auto -undo "
             . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mksb -undo returned a non-zero exit code";
  }
  if (!$reg_error && (-d $OdeTest::test_sb))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_sb_name not deleted";
  }
  if (!$reg_error && (-f $OdeTest::test_sb_rc))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_sb_rc not deleted";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", 6, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return $error;
  } 
  else
  {
    #Test Case Passed
    OdeInterface::printResult( "pass", "regression", "mksb", 6, $tmpstr );
  }  

  ## Regression Test Case 7 - test for non-zero exit code
  # force mksb to fail and check if a non-zero exit code was supplied
  $reg_error = 0;
  $command = "mksb -badflag . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mksb <invalid flag>";
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status == 0)
  {
    $reg_error = 1;
    $error_txt = "mksb returned a zero exit code after an error";
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", 7, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mksb", 7, $tmpstr );
  }

  ## Regression Test Case 8 - test UNC path name to backing build
  if ( $OdeEnv::WIN32 ) # maybe add OS2 some day
  {
    $error = uncPathTest( 8 );
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
  my $command;
  my $status;
  my $error = 0;
  my $fvt_error = 0;
  my $error_txt;
  my $sb_src_dir = $OdeTest::test_sb . $OdeEnv::dirsep . "src";
  my $server_c = 
    join( $OdeEnv::dirsep, ($sb_src_dir, "bin", "server", "server.c"));
  my $buildserver_c = 
    join( $OdeEnv::dirsep, ($sb_src_dir, "bin", "server", "buildserver.c"));
  my $retserver_c = 
    join( $OdeEnv::dirsep, ($sb_src_dir, "bin", "server", "retserver.c"));
  my $server_makefile = 
    join( $OdeEnv::dirsep, ($sb_src_dir, "bin", "server", "makefile"));
  my $num_bb_files;
  my $num_sb_files;
  my $bb_src_size;
  my $sb_src_size;
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb1, 
                     $OdeTest::test_sb_rc, $OdeTest::tmpfile);


  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  OdeEnv::runOdeCommand( "mksb -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");  

  ## FVT Test Case - OD91 - make a sandbox
  $command = "mksb -auto -back $OdeTest::test_bbdir -dir $OdeTest::test_sbdir" 
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine "
             . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); 
  # Check command exit code
  if ($status != 0)
  {
    $fvt_error = 1;
    $error_txt = "mksb returned a non-zero exit code";
  }
  # Validate Sandbox
  if (!$fvt_error)
  {
    $fvt_error = OdeTest::validateSandbox( $OdeTest::test_sb, 
                                          $OdeTest::test_sb_rc, \$error_txt );
  }
 
  if ($fvt_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "fvt", "mksb", "OD91" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $fvt_error = 0;   # Reset fvt_error to zero for next test case
    OdeFile::deltree( \@delete_list );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "fvt", "mksb", "OD91" );
  }

  ## FVT Test Case - OD94 - make another sandbox
  $command = "mksb -auto -back $OdeTest::test_bbdir" 
             . " -rc $OdeTest::test_sb_rc $OdeTest::test_sb1_name"
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); 
  # Check command exit code
  if ($status != 0)
  {
    $fvt_error = 1;
    $error_txt = "mksb returned a non-zero exit code";
  }
  # Validate Sandbox
  if (!$fvt_error)
  {
    $fvt_error = OdeTest::validateSandbox( $OdeTest::test_sb1, 
                                           $OdeTest::test_sb_rc, \$error_txt );
  }
  if ($fvt_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "fvt", "mksb", "OD94" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $fvt_error = 0;   # Reset fvt_error to zero for next test case
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "fvt", "mksb", "OD94" );
  }

  ## FVT Test Case - OD92 - list sandboxes
  $command = "mksb -auto -rc $OdeTest::test_sb_rc -list"
             . " > $OdeTest::tmpfile";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from tempfile to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $fvt_error = 1;
    $error_txt = "mksb returned a non-zero exit code";
  }
  if (!$fvt_error && ( !OdeFile::findInFile( 
                "Default Sandbox: $OdeTest::test_sb_name", $OdeTest::tmpfile) ))
  {
    $fvt_error = 1;
    $error_txt = "Default Sandbox not correct in mksb -list output";
  }
  if (!$fvt_error && !( OdeFile::findInFile( 
                                "Base: $OdeEnv::tempdir", $OdeTest::tmpfile) ))
  {
    $fvt_error = 1;
    $error_txt = "Base not correct in mksb -list output";
  }
  if (!$fvt_error && !( OdeFile::findInFile( 
                       "Sandbox: $OdeTest::test_sb1_name", $OdeTest::tmpfile) ))
  {
    $fvt_error = 1;
    $error_txt = "Sandbox not correct in mksb -list output";
  }
  if ($fvt_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "fvt", "mksb", "OD92" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $fvt_error = 0;   # Reset fvt_error to zero for next test case
  } 
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "fvt", "mksb", "OD92" );
  }

  ## FVT Test Case - OD93 - remove a sandbox
  # remove both sandboxes previously created
  $command = "mksb -rc $OdeTest::test_sb_rc -auto -undo "
             . $OdeTest::test_sb1_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status != 0)
  {
    $fvt_error = 1;
    $error_txt = "mksb returned a non-zero exit code";
  }
  # Sandbox should be deleted
  if ( !$fvt_error && (-d $OdeTest::test_sb1))
  {
    $fvt_error = 1;
    $error_txt = "sandbox directory $OdeTest::test_sb1 was not deleted";
  }
  # no reference to sb1 should exist in rc file
  if (!$fvt_error && ( OdeFile::findInFile( $OdeTest::test_sb1_name, 
                                            $OdeTest::test_sb_rc) ))
  {
    $fvt_error = 1;;
    $error_txt = "Reference to $OdeTest::test_sb1_name still ";
    $error_txt = $error_txt . "exists in $OdeTest::test_sb_rc";
  }
  # remove next sandbox
  if (!$fvt_error)
  {
    $command = "mksb -rc $OdeTest::test_sb_rc -auto -undo "
               . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command );
  }
  # Check command exit code
  if (!$fvt_error && $status)
  {
    $fvt_error = 1;;
    $error_txt = "mksb returned a non-zero exit code";
  }
  # Sandbox should be deleted
  if ( !$fvt_error && (-d $OdeTest::test_sb))
  {
    $fvt_error = 1;
    $error_txt = "sandbox directory $OdeTest::test_sb was not deleted";
  }
  # rc file should be deleted
  if ( !$fvt_error && (-f $OdeTest::test_sb_rc))
  {
    $fvt_error = 1;
    $error_txt = "rc file $OdeTest::test_sb_rc was not deleted";
  }
  if ($fvt_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "fvt", "mksb", "OD93" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $fvt_error = 0;   # Reset fvt_error to zero for next test case
  } 
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "fvt", "mksb", "OD93" );
  }

  OdeFile::deltree( \@delete_list );

  ## FVT Test Case - OD95 - create a sandbox with
  # some of the backing build's source
  # - use bin/server source for this example
  $command = "mksb -back $OdeTest::test_bbdir -dir $OdeTest::test_sbdir" 
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine "
             . "-src c /bin/server"
             . " -auto $OdeTest::test_sb_name >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status != 0)
  {
    $fvt_error = 1;
    $error_txt = "mksb returned a non-zero exit code";
  }
  # Validate Sandbox
  if (!$fvt_error)
  {
    $fvt_error = OdeTest::validateSandbox( $OdeTest::test_sb, 
                                           $OdeTest::test_sb_rc, \$error_txt );
  }
  # File src/bin/server/server.c should exist
  if ( !$fvt_error && !(-f $server_c))
  {
    $fvt_error = 1;
    $error_txt = "src file $server_c does not exist in sandbox";
  }
  # File src/bin/server/retserver.c should exist
  if ( !$fvt_error && !(-f $retserver_c))
  {
    $fvt_error = 1;
    $error_txt = "src file $retserver_c does not exist in sandbox";
  }
  # File src/bin/server/buildserver.c should exist
  if ( !$fvt_error && !(-f $buildserver_c))
  {
    $fvt_error = 1;
    $error_txt = "src file $buildserver_c does not exist in sandbox";
  }
  # File src/bin/server/makefile should exist
  if ( !$fvt_error && !(-f $server_makefile))
  {
    $fvt_error = 1;
    $error_txt = "makefile $server_makefile does not exist in sandbox";
  }
  if ($fvt_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "fvt", "mksb", "OD95" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $fvt_error = 0;   # Reset fvt_error to zero for next test case
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "fvt", "mksb", "OD95" );
  }

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  ## FVT Test Case - OD96 - create a sandbox with
  # all of the backing build's source
  # - should check number of files in sandbox's src
  # - directory, and the size (in bytes) src dir
  # - against that of the backing build's src directory,
  # - as well as the existence of a few known files, in 
  # - this case the bin/server files
  $command = "mksb -auto -back $OdeTest::test_bbdir -dir $OdeTest::test_sbdir" 
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine -src c /"
             . " $OdeTest::test_sb_name >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status != 0)
  {
    $fvt_error = 1;
    $error_txt = "mksb returned a non-zero exit code";
  }
  # Validate Sandbox
  if (!$fvt_error)
  {
    $fvt_error = OdeTest::validateSandbox( $OdeTest::test_sb, 
                                           $OdeTest::test_sb_rc, \$error_txt );
  }
  # File src/bin/server/server.c should exist
  if ( !$fvt_error && !(-f $server_c))
  {
    $fvt_error = 1;
    $error_txt = "src file $server_c does not exist in sandbox";
  }
  # File src/bin/server/retserver.c should exist
  if ( !$fvt_error && !(-f $retserver_c))
  {
    $fvt_error = 1;
    $error_txt = "src file $retserver_c does not exist in sandbox";
  }
  # File src/bin/server/buildserver.c should exist
  if ( !$fvt_error && !(-f $buildserver_c))
  {
    $fvt_error = 1;
    $error_txt = "src file $buildserver_c does not exist in sandbox";
  }
  # File src/bin/server/makefile should exist
  if ( !$fvt_error && !(-f $server_makefile))
  {
    $fvt_error = 1;
    $error_txt = "makefile $server_makefile does not exist in sandbox";
  }
  # get src dir file information
  if (!$fvt_error)
  {
    $num_bb_files = OdeFile::numDirEntries( 
                              $OdeTest::test_bbdir . $OdeEnv::dirsep . "src" );
    $num_sb_files = OdeFile::numDirEntries( 
                              $OdeTest::test_sb . $OdeEnv::dirsep . "src" );
    $bb_src_size = OdeFile::getDirSize( 
                              $OdeTest::test_bbdir . $OdeEnv::dirsep . "src" );
    $sb_src_size = OdeFile::getDirSize( 
                              $OdeTest::test_sb . $OdeEnv::dirsep . "src" );
  }
  # Number of files should match in backing build and sandbox src dirs
  if ( !$fvt_error && ($num_bb_files != $num_sb_files))
  {
    $fvt_error = 1;
    $error_txt = "Number of src files in backing build does not match";
    $error_txt = $error_txt . " number of src files in sandbox";
  }
  # Size of src dir should match in backing build and sandbox
  if ( !$fvt_error && ($bb_src_size != $sb_src_size))
  {
    $fvt_error = 1;
    $error_txt = "Size of src directory in backing build does not match";
    $error_txt = $error_txt . " size of src directory in sandbox";
  }
  if ($fvt_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "fvt", "mksb", "OD96" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $fvt_error = 0;   # Reset fvt_error to zero for next test case
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "fvt", "mksb", "OD96" );
  }

  #### FVT Test Case - OD97 - still to do
  #### FVT Test Case - OD98 - still to do
  #### FVT Test Case - OD99 - still to do
  #### FVT Test Case - OD100 - still to do

  OdeFile::deltree( \@delete_list );
 
  return( $error );
}


#########################################
#
# sub uncPathTest
#   Test UNC path to the backing build, of the form \\hostname\sharename
#
#   Currently Win32 only, because OS/2 LOGON requires more setup
#   than might have been done on the test machine.
#
#########################################
sub uncPathTest ( $ )
{
  my($tnum) = @_;
  my $command;
  my $status;
  my $error = 0;
  my $reg_error = 0;
  my $error_txt;
  my $tmpstr;
  my $hostnameline;
  my $sharename = substr( "TEST$$", 0, 8); # no more than 8 chars long
  my $uncpath;
  my $sbconf;
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb1, 
                     $OdeTest::test_sb_rc, $OdeTest::tmpfile);

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  # Run "net user" to get the host name, and to see if NET is runnable.
  $command = "NET USER > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "NET USER";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from tempfile to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "NET USER returned a non-zero exit code";
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", $tnum, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  # Get the $hostname we are running on
  open( FILEHND, "< $OdeTest::tmpfile" ) ||
    OdeInterface::logPrint( "Could not open file $OdeTest::tmpfile\n" );
  LINE: while (!eof( FILEHND ) )
  {
    $hostnameline = lc( readline( *FILEHND ) );
    chomp $hostnameline;
    $hostnameline =~ s/user accounts for\s+(\\\\\S+)\s*$/$1/;
    last LINE if $hostnameline;
  }
  close( FILEHND );
  if (! $hostnameline)
  {
    $reg_error = 1;
    $error_txt = "NET USER output had no hostname";
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", $tnum, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  # To make this REALLY bullet proof, we could do a NET SHARE here and
  # capture its output.  Then check if the $sharename already exists on
  # this host.  Complain and exit the test with failure if it already
  # exists.  This is so improbable, let's not bother.

  # Make sandbox $test_sb_name first, backed by bbexample.  bbexample might
  # be anywhere including on a file system where we cannot make UNC path
  # to it. $test_sb_name will be in the temporary test directory, so we
  # should be able to make a UNC path to it from a second sandbox
  # called $test_sb1_name.
  # Do "mksb sandbox1 -back bbexample -auto".
  # Check if the return code is okay, similarly to test 1.
  $command = "mksb -auto -back $OdeTest::test_bbdir -dir $OdeTest::test_sbdir"
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine -auto "
             . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mksb -back -dir -rc -m -auto, to be on UNC path";
  $status = OdeEnv::runOdeCommand( $command ); 
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mksb returned a non-zero exit code";
  }
  # Validate Sandbox
  if (!$reg_error)
  {
    $reg_error = OdeTest::validateSandbox( $OdeTest::test_sb, 
                                          $OdeTest::test_sb_rc, \$error_txt );
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", $tnum, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mksb", $tnum, $tmpstr );
  }

  # Create UNC pathname \\hostname\sharename where sharename is generated
  # from the process number, and the actual path is to the sandbox just made.
  # "NET SHARE $sharename=d:\temp\test_nnn\sandbox /UNLIMITED"
  # Check return code.  We could run NET SHARE and look at its output
  # for the new \\hostname\sharename as another check, but it suffices
  # to look for an error return code from mksb below.
  # Special Note:  NET SHARE would complain and want a yes/no verification
  # if the sharename is longer than 8 chars; we will keep it short.
  $command = "NET SHARE $sharename=$OdeTest::test_sbdir"
             . $OdeEnv::dirsep . $OdeTest::test_sb_name
             . " /UNLIMITED > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "NET SHARE sharename=sandbox /UNLIMITED";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from tempfile to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "NET SHARE returned a non-zero exit code";
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", $tnum, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  # remember the UNC path to the sandbox.
  $uncpath = "$hostnameline\\$sharename";

  # Do "mksb sandbox2 -back \\hostname\sharename -auto".
  # Check if the return code is okay, similarly to test 1.

  $command = "mksb -auto -back $uncpath -dir $OdeTest::test_sbdir"
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine -auto "
             . $OdeTest::test_sb1_name
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mksb -back \\\\hostname\\sharename -dir -rc -m -auto";
  $status = OdeEnv::runOdeCommand( $command ); 
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mksb returned a non-zero exit code";
  }
  # Validate Sandbox - checks if directories and sb.conf were created.
  if (!$reg_error)
  {
    $reg_error = OdeTest::validateSandbox( $OdeTest::test_sb1, 
                                          $OdeTest::test_sb_rc, \$error_txt );
  }

  # Check if sb.conf has the UNC path for the backing build.
  if (! $reg_error)
  {
    $sbconf = $OdeTest::test_sb1
              . $OdeEnv::dirsep . "rc_files" 
              . $OdeEnv::dirsep . "sb.conf";
    if (! OdeFile::findInFile( OdePath::unixize( lc ( $uncpath ) ),
                               $OdeTest::test_sb1_conf, 0 ))
    {
      $reg_error = 1;
      $error_txt = "Could not find $uncpath in $sbconf";
    }
  }

  # do "net share sharename /delete" here, otherwise you cannot delete
  # all of the test directory
  $command = "NET SHARE $sharename /DELETE"
             . " > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "NET SHARE sharename /DELETE";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from tempfile to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    if (! $error_txt) # if not a higher priority message
    {
      $error_txt = "NET SHARE sharename /DELETE returned a non-zero exit code";
    }
  }

  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mksb", $tnum, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    return $error;
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mksb", $tnum, $tmpstr );
  }


  return ( $error );
}

1;
