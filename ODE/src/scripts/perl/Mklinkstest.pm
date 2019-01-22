#########################################
#
# Mklinkstest module
# - module to run Mklinkstest for different
#   test levels
#
#########################################

package Mklinkstest;
use strict;
use OdeUtil;
use OdeEnv;
use OdeTest;
use OdeInterface;
use File::Copy;
use File::Path;

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
  my $filestr;
  my $filestr1;
  my $filestr2;
  my $filestr3;
  my @delete_list = ($OdeTest::test_sb_rc, $OdeTest::test_sb);
  OdeFile::deltree( \@delete_list );

  OdeInterface::logPrint( "in Mklinkstest - test level is normal\n");
  OdeEnv::runOdeCommand( "mklinks -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_bbdir, "src", "bin",
                                     "server", "server.c") );
  if (!(-f $filestr))
  {
    #Test case failed
    OdeInterface::printError( 
               "mklinks : $filestr does not exist. Can't test mklinks" );
    $error = 1;
    return $error;
  }
  
  $command = "mksb -dir $OdeTest::test_sbdir -rc $OdeTest::test_sb_rc "
   . "-back $OdeTest::test_bbdir $OdeTest::test_sb_name "
   . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || !(-d $OdeTest::test_sb) || 
     !(-f join( $OdeEnv::dirsep, ($OdeTest::test_sb, "rc_files", "sb.conf") )) )
  {
    #Test case failed
    OdeInterface::printError( "mklinks : mksb does not work" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  $filestr = $OdeEnv::dirsep . "src";
  $filestr = join( $OdeEnv::dirsep, ($filestr, "bin", "server", "server.c") );
  $command = "mklinks -copy -rc $OdeTest::test_sb_rc  $filestr -auto"
             ." >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code and the existence of file in the sandbox
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "bin", "server",
                                     "server.c") );
  if (($status == 0) && (-f $filestr))
  {
    #Test 1 Passed
    OdeInterface::printResult( "pass", "normal", "mklinks", 1, 
                               "mklinks -rc -copy -auto" );
  }
  else
  {
    #Test 1 Failed
    OdeInterface::printResult( "fail", "normal", "mklinks", 1, 
                               "mklinks -rc -copy -auto" );
    $error = 1;
  }

  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "bin") );
  OdeFile::deltree( $filestr ); 

  $filestr = $OdeEnv::dirsep . "src";
  $filestr = join( $OdeEnv::dirsep, ($filestr, "bin", "server", "server.c") );
  $command = "mklinks -copy -auto -rc $OdeTest::test_sb_rc $filestr "
             . $OdeEnv::dirsep . "src" . $OdeEnv::dirsep . "lib"
             ." >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "bin", "server",
                                     "server.c") );
  $filestr1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "lib") );

  if (($status == 0) && (-f $filestr) && (-d $filestr1))
  {
    #Test 2 Passed
    OdeInterface::printResult( "pass", "normal", "mklinks", 2, 
                               "mklinks -rc -copy -auto" );
  }
  else
  {
    #Test 2 Failed
    OdeInterface::printResult( "fail", "normal", "mklinks", 2, 
                               "mklinks -rc -copy -auto" );
    $error = 1;
  }

  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "bin") );
  OdeFile::deltree( $filestr ); 
  OdeFile::deltree( $filestr1 ); 

  $filestr = $OdeEnv::dirsep . "src";
  $filestr = join( $OdeEnv::dirsep, ($filestr, "bin", "server", "server.c") );
  $filestr1 = $OdeEnv::dirsep . "src";
  $filestr1 = join( $OdeEnv::dirsep, ($filestr1, "bin", "client", "p*") );
  $command = "mklinks -copy -auto -rc $OdeTest::test_sb_rc $filestr $filestr1 "
             . $OdeEnv::dirsep . "src" . $OdeEnv::dirsep . "lib"
             ." >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "bin", "server",
                                     "server.c") );
  $filestr1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "bin", 
                                     "client", "printmsg.c") );
  $filestr2 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "bin", 
                                     "client", "printmsg.h") );
  $filestr3 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "lib") );

  if (($status == 0) && (-f $filestr) && (-f $filestr1) && (-f $filestr2) && 
                                            (-d $filestr3))
  {
    #Test 3 Passed
    OdeInterface::printResult( "pass", "normal", "mklinks", 3, 
                               "mklinks -rc -copy -auto" );
  }
  else
  {
    #Test 3 Failed
    OdeInterface::printResult( "fail", "normal", "mklinks", 3, 
                               "mklinks -rc -copy -auto" );
    $error = 1;
  }

  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "bin") );
  OdeFile::deltree( $filestr ); 
  OdeFile::deltree( $filestr3 ); 

  $filestr = join( $OdeEnv::dirsep, 
                   ($OdeTest::test_bbdir, "src", "bin", "logger") );
  $filestr1 = join( $OdeEnv::dirsep, 
                   ($OdeTest::test_sb, "src", "temp") );
  $command = "mklinks -copy -auto -rc $OdeTest::test_sb_rc "
            . "-link_from $filestr -link_to $filestr1 >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (($status == 0) && (-d $filestr1))
  {
    #Test 4 Passed
    OdeInterface::printResult( "pass", "normal", "mklinks", 4, 
                               "mklinks -link_from -link_to" );
  }
  else
  {
    #Test 4 Failed
    OdeInterface::printResult( "fail", "normal", "mklinks", 4, 
                               "mklinks -link_from -link_to" );
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
  my $filestr1;
  my $filestr2;
  my $num_files = 0;
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my @delete_list = ($OdeTest::test_sb_rc, $OdeTest::test_sb, 
                     $OdeTest::test_sb1, $OdeTest::tmpfile, $cmd_file);

  OdeInterface::logPrint( "in mklinkstest - test level is regression\n");
  OdeEnv::runOdeCommand( "mklinks -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

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
    OdeInterface::printError( "mklinks : mksb does not work" );
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
    OdeInterface::printError( "mklinks : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  $filestr = join( $OdeEnv::dirsep, 
                   ($OdeTest::test_sb, "src", "bin", "server") );
  OdeInterface::logPrint("creating path $filestr..\n");
  mkpath( $filestr );
  $filestr1 = join( $OdeEnv::dirsep, 
                   ($OdeTest::test_bbdir, "src", "bin", "server", "server.c") );
  $filestr .= $OdeEnv::dirsep . "server.c";
  copy( $filestr1, $filestr );

  ## Regression Test Case 1 - run mklinks -rc -sb
  ## Should copy the target if non-unix otherwise link it

  $filestr1 = $OdeEnv::dirsep . "src";
  $filestr1 = join( $OdeEnv::dirsep, ($filestr1, "bin", "server", "server.c") );
  $command = "mklinks -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . " $filestr1 -auto >> $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "mklinks -rc -sb -auto";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "mklinks returned a non-zero exit code";
  }
  $filestr1 = join( $OdeEnv::dirsep, 
                   ($OdeTest::test_sb1, "src", "bin", "server", "server.c") ); 
  if (!(defined( $OdeEnv::UNIX )))
  {
    if (!$reg_error && !(-f $filestr1))
    {
      $reg_error = 1;
      $error_txt = "mklinks did not copy the file";
    }
  }
  else
  {
    if (!$reg_error && !(-l $filestr1))
    {
      $reg_error = 1;
      $error_txt = "mklinks did not link the file";
    }
  }
  #verify if server.c is copied from odetestsb and not bbexample
  if (!$reg_error && !(OdeFile::findInFile( $filestr, $OdeTest::tmpfile )))
  {
    $reg_error = 1;
    $error_txt = "file was not copied from $filestr";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mklinks", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mklinks", 1, $tmpstr );
  }

  #deleting bin directory in test_sb1
  $filestr1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb1, "src", "bin") );
  OdeFile::deltree( $filestr1 );
  if (-d $filestr1)
  {
    OdeInterface::logPrint( "$filestr1 not deleted\n" );
  }

  ## Regression Test Case 2 - run mklinks -copy -rc -sb target1 target2 
  ## Should copy the targets 
  $filestr = $OdeEnv::dirsep . "src" . $OdeEnv::dirsep . "lib";
  $filestr1 = $OdeEnv::dirsep . "src";
  $filestr2 = $filestr1;
  $filestr1 = join( $OdeEnv::dirsep, ($filestr1, "bin", "server", "*") );
  $filestr2 = join( $OdeEnv::dirsep, ($filestr2, "bin", "l*") );
  $command = "mklinks -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "-copy $filestr $filestr1 $filestr2 -auto "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mklinks -rc -sb -copy -auto";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "mklinks returned a non-zero exit code";
  }
  $filestr1 = $OdeTest::test_bbdir . $filestr;
  $num_files = OdeFile::numDirEntries( $filestr1 );
  $filestr1 = $OdeTest::test_sb1 . $filestr;
  if (!$reg_error && !(-d $filestr1))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 not created";
  }
  if (!$reg_error && (OdeFile::numDirEntries( $filestr1 ) != $num_files))
  {
    $reg_error = 1;
    $error_txt = "all the files in $filestr1 not copied";
  }
  $filestr1 = join( $OdeEnv::dirsep, 
                    ($OdeTest::test_bbdir, "src", "bin", "server") );
  $num_files = OdeFile::numDirEntries( $filestr1 );
  $filestr1 = join( $OdeEnv::dirsep, 
                    ($OdeTest::test_sb1, "src", "bin", "server") );
  if (!$reg_error && !(-d $filestr1))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 not created";
  }
  if (!$reg_error && (OdeFile::numDirEntries( $filestr1 ) != $num_files))
  {
    $reg_error = 1;
    $error_txt = "all the files in $filestr1 not copied";
  }
  $filestr1 = join( $OdeEnv::dirsep, 
                    ($OdeTest::test_bbdir, "src", "bin", "logger") );
  $num_files = OdeFile::numDirEntries( $filestr1 );
  $filestr1 = join( $OdeEnv::dirsep, 
                    ($OdeTest::test_sb1, "src", "bin", "logger") );
  if (!$reg_error && !(-d $filestr1))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 not created";
  }
  if (!$reg_error && (OdeFile::numDirEntries( $filestr1 ) != $num_files))
  {
    $reg_error = 1;
    $error_txt = "all the files in $filestr1 not copied";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mklinks", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mklinks", 2, $tmpstr );
  }

  ## Regression Test Case 3 - run mklinks -copy -rc -sb -over -norefresh
  ## Should not overwrite the file because of the -norefresh.
  $filestr = $OdeEnv::dirsep . "src";
  $filestr = join( $OdeEnv::dirsep, ($filestr, "bin", "logger", "logger.c") );
  $filestr1 .= $OdeEnv::dirsep . "logger.c";
  $filestr2 = (stat( $filestr1 ))[9];
  my $now;
  $now = time + 100; 
  utime $now, $now, $filestr1;  #changing access and modify times of logger.c
  $filestr2 = (stat( $filestr1 ))[9]; #storing the changed value
  $command = "mklinks -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "-copy $filestr -auto -over -norefresh"
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mklinks -rc -sb -copy -over -auto -norefresh";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "mklinks returned a non-zero exit code";
  }
  
  #time stamp of logger.c still should match the changed modification time
  if (!$reg_error && ($filestr2 != (stat( $filestr1 ))[9]))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 overwritten when -norefresh is used";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mklinks", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mklinks", 3, $tmpstr );
  }

  ## Regression Test Case 4 - run mklinks -copy -rc -sb -over 
  ## Should overwrite the file this time.
  $command = "mklinks -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "-copy $filestr -auto -over"
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mklinks -rc -sb -copy -over -auto";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "mklinks returned a non-zero exit code";
  }
  
  #time stamp of logger.c should match with that in the backing build
  #and should differ from $filestr2 which is the changed modification time
  if (!$reg_error && ($filestr2 == (stat( $filestr1 ))[9]))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 not overwritten when -over is used";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mklinks", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mklinks", 4, $tmpstr );
  }

  ## Regression Test Case 5 - run mklinks -copy -rc -sb -timecmp -norefresh
  ## Should not overwrite the file even though there is a later file to copy
  ## since -norefresh is specified
  $filestr2 = (stat( $filestr1 ))[9];
  my $oldtime;
  $oldtime = (stat( $filestr1 ))[9] - 1000;
  utime $now, $now, $filestr1;  #changing access and modify times of logger.c
  $filestr2 = (stat( $filestr1 ))[9]; #storing the changed value
  $command = "mklinks -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "-copy $filestr -auto -timecmp -norefresh"
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mklinks -rc -sb -copy -timecmp -auto -norefresh";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "mklinks returned a non-zero exit code";
  }
  
  #time stamp of logger.c still should match the changed modification time
  if (!$reg_error && ($filestr2 != (stat( $filestr1 ))[9]))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 overwritten when -norefresh is used";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mklinks", 5, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mklinks", 5, $tmpstr );
  }

  ## Regression Test Case 6 - run mklinks -copy -rc -sb -timecmp
  ## Should overwrite the file
  $command = "mklinks -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "-copy $filestr -auto -over"
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mklinks -rc -sb -copy -timecmp -auto";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "mklinks returned a non-zero exit code";
  }
  
  #time stamp of logger.c should match with that in the backing build
  #and should differ from $filestr2 which is the changed modification time
  if (!$reg_error && ($filestr2 == (stat( $filestr1 ))[9]))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 not overwritten when -over is used";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mklinks", 6, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mklinks", 6, $tmpstr );
  }

  #deleting bin directory in test_sb1
  $filestr1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb1, "src", "bin") );
  if (-d $filestr1)
  {
    OdeFile::deltree( $filestr1 );
  }

  ## Regression Test Case 7 - run mklinks -copy -link_from -link_to
  ## Should copy the target directory
  $filestr1 = join( $OdeEnv::dirsep, 
                    ($OdeTest::test_bbdir, "src", "bin", "logger") );
  $filestr2 = join( $OdeEnv::dirsep, 
                    ($OdeTest::test_sb1, "src", "bin", "logger") );
  $command = "mklinks -copy -auto -link_from $filestr1 -link_to $filestr2 "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mklinks -copy -auto -link_from -link_to";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "mklinks returned a non-zero exit code";
  }

  if (!$reg_error && !(-d $filestr2))
  {
    $reg_error = 1;
    $error_txt = "$filestr2 not created";
  }
  else
  {
    $num_files = OdeFile::numDirEntries( $filestr2 );
  }
  if (!$reg_error && ($num_files != OdeFile::numDirEntries( $filestr1 ))) 
  {
    $reg_error = 1;
    $error_txt = "all the files in $filestr1 not copied";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mklinks", 7, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mklinks", 7, $tmpstr );
  }

  $filestr1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb1, "src", "bin") );
  $filestr2 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "bin") );
  #deleting bin directory in test_sb and test_sb1 
  if (-d $filestr1)
  {
    OdeFile::deltree( $filestr1 );
  }
  if (-d $filestr2)
  {
    OdeFile::deltree( $filestr2 );
  }

  ## Regression Test Case 8 - run mklinks -copy inside workon
  ## Should copy all the target files
  $filestr = $OdeEnv::dirsep . "src";
  $filestr = join( $OdeEnv::dirsep, ($filestr, "bin", "*") );
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "mklinks -copy $filestr -auto\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -rc -sb; mklinks -copy -auto; exit;";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "mklinks returned a non-zero exit code";
  }
  if (!$reg_error && !(-d $filestr1))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 not created";
  }
  else
  {
    $num_files = OdeFile::numDirEntries( $filestr1 );
  }
  $filestr2 = join( $OdeEnv::dirsep, ($OdeTest::test_bbdir, "src", "bin") );
  if (!$reg_error && ($num_files != OdeFile::numDirEntries( $filestr2 )))
  {
    $reg_error = 1;
    $error_txt = "all the files in $filestr2 not copied";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mklinks", 8, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mklinks", 8, $tmpstr );
  }

  #deleting bin directory in test_sb1
  if (-d $filestr1)
  {
    OdeFile::deltree( $filestr1 );
  }

  ## Regression Test Case 9 - run mklinks -copy -norecurse inside workon
  ## Should copy only the immediate directory contents
  $filestr = $OdeEnv::dirsep . "src" . $OdeEnv::dirsep . "bin" . 
             $OdeEnv::dirsep;
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "mklinks -copy $filestr -auto -norecurse\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -rc -sb; mklinks -copy -auto -norecurse; exit;";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "mklinks returned a non-zero exit code";
  }
  $filestr = $filestr2 = $filestr1;
  $filestr1 .= $OdeEnv::dirsep . "logger";
  $filestr2 .= $OdeEnv::dirsep . "server";
  $filestr .= $OdeEnv::dirsep . "makefile";
  if (!$reg_error && !(-d $filestr1))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 not created";
  }
  if (!$reg_error && (OdeFile::numDirEntries( $filestr1 ) != 0))
  {
    $reg_error = 1;
    $error_txt = "files in subdirectories are copied";
  }
  if (!$reg_error && !(-d $filestr2))
  {
    $reg_error = 1;
    $error_txt = "$filestr2 not created";
  }
  if (!$reg_error && (OdeFile::numDirEntries( $filestr2 ) != 0))
  {
    $reg_error = 1;
    $error_txt = "files in subdirectories are copied";
  }
  if (!$reg_error && !(-f $filestr))
  {
    $reg_error = 1;
    $error_txt = "$filestr not copied";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mklinks", 9, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mklinks", 9, $tmpstr );
  }

  ## Regression Test Case 10 - run mklinks -copy -badflag
  ## Should return a non-zero exit code
  $filestr = $OdeEnv::dirsep . "src";
  $command = "mklinks -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name"
             . " -copy -auto -badflag $filestr >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mklinks -rc -sb -copy -auto -badflag";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status == 0)
  {
    $reg_error = 1;
    $error_txt = "mklinks returned a zero ezit code";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mklinks", 10, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mklinks", 10, $tmpstr );
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
