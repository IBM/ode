#########################################
#
# Genpathtest module
# - module to run Genpathtest for different
#   test levels
#
#########################################

package Genpathtest;
use strict;
use OdeUtil;
use OdeEnv;
use OdeTest;
use OdePath;
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
  my @delete_list = ($OdeTest::test_sb_rc, $OdeTest::test_sb, 
                                           $OdeTest::tmpfile);
  OdeFile::deltree( \@delete_list );

  OdeInterface::logPrint( "in Genpathtest - test level is normal\n");
  OdeEnv::runOdeCommand( "genpath -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");
  $command = "mksb -dir $OdeTest::test_sbdir -rc $OdeTest::test_sb_rc "
   . "-back $OdeTest::test_bbdir $OdeTest::test_sb_name "
   . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || !(-d $OdeTest::test_sb) || 
     !(-f join( $OdeEnv::dirsep, ($OdeTest::test_sb, "rc_files", "sb.conf") )) )
  {
    #Test case failed
    OdeInterface::printError( "genpath : mksb does not work" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  $command = "genpath -rc $OdeTest::test_sb_rc -I. >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code 
  if ($status == 0)
  {
    #Test 1 Passed
    OdeInterface::printResult( "pass", "normal", "genpath", 1,
                               "genpath -rc -I." );
  }
  else
  {
    #Test 1 Failed
    OdeInterface::printResult( "fail", "normal", "genpath", 1, 
                               "genpath -rc -I." );
    $error = 1;
  }

  $command = "genpath -rc $OdeTest::test_sb_rc -I. >> $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  $filestr = "-I";
  $filestr .= OdePath::unixize( $OdeTest::test_sb) . "/obj/$OdeEnv::machine";
  if (($status == 0) && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile )))
  {
    #Test2 passed
    OdeInterface::printResult( "pass", "normal", "genpath", 2, 
                               "genpath -rc -I." );
  }
  else
  {
    #Test2 failed
    OdeInterface::printResult( "fail", "normal", "genpath", 2, 
                               "genpath -rc -I." );
    $error = 1;
  }
  OdeFile::deltree( $filestr ); 
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
  my @list;
  my $num_files = 0;
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my @delete_list = ($OdeTest::test_sb_rc, $OdeTest::test_sb, 
                     $OdeTest::test_sb1, $OdeTest::tmpfile, $cmd_file);

  OdeInterface::logPrint( "in genpathtest - test level is regression\n");
  OdeEnv::runOdeCommand( "genpath -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

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
    OdeInterface::printError( "genpath : mksb does not work" );
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
    OdeInterface::printError( "genpath : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  ## Regression Test Case 1 - run genpath -rc -sb
  ## Should not give any output
  $command = "genpath -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . ">> $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "genpath -rc -sb";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "genpath returned a non-zero exit code";
  }
  #verify if the output of genpath is null
  if (!$reg_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != 0))
  {
    $reg_error = 1;
    $error_txt = "genpath output is not null";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "genpath", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "genpath", 1, $tmpstr );
  }

  ## Regression Test Case 2 - run genpath -I -I inside workon
  ## Should add the directories specified by -I to the search path
  $filestr = "." . $OdeEnv::dirsep . "gendir1" . $OdeEnv::dirsep . "gendir2";
  $filestr1 = $OdeTest::test_sb . $OdeEnv::dirsep . "gendir3" . 
              $OdeEnv::dirsep . "gendir4";
  open( INPUT, ">$cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "genpath -I$filestr -I$filestr1 > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -sb -rc; genpath -I -I;";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: genpath -I$filestr -I$filestr1; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "workon or genpath returned a non-zero exit code";
  }
  if (!$reg_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != 7))
  {
    $reg_error = 1;
    $error_txt = "genpath did not output the correct number of lines";
  }
  $filestr = "-I" . $OdeTest::test_sb1;
  $filestr = join( $OdeEnv::dirsep, 
                   ($filestr, "obj", $OdeEnv::machine, "gendir1", "gendir2") );
  $filestr = OdePath::unixize( $filestr );
  if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 1))
  {
    $reg_error = 1;
    $error_txt = "$filestr either not found or not the first line "
                 . "in $OdeTest::tmpfile";
  }
  $filestr = "-I" . $OdeTest::test_sb;
  $filestr = join( $OdeEnv::dirsep, 
                   ($filestr, "src", "gendir1", "gendir2") );
  $filestr = OdePath::unixize( $filestr );
  if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 4))
  {
    $reg_error = 1;
    $error_txt = "$filestr either not found or not in the correct position "
                 . "in $OdeTest::tmpfile";
  }
  $filestr = "-I" . $OdeTest::test_bbdir;
  $filestr = join( $OdeEnv::dirsep, 
                   ($filestr, "src", "gendir1", "gendir2") );
  $filestr = OdePath::unixize( $filestr );
  if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 6))
  {
    $reg_error = 1;
    $error_txt = "$filestr either not found or not in the correct position "
                 . "in $OdeTest::tmpfile";
  }
  $filestr = "-I" . OdePath::unixize( $filestr1 );
  if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 7))
  {
    $reg_error = 1;
    $error_txt = "$filestr either not found or not in the correct position "
                 . "in $OdeTest::tmpfile";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "genpath", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "genpath", 2, $tmpstr );
  }

  ## Regression Test Case 3 - run genpath -I -L -E inside workon
  ## Should add the directories specified by -I, -E and -L to the search path
  $filestr = "." . $OdeEnv::dirsep . "gendir1" . $OdeEnv::dirsep . "gendir2";
  $filestr1 = $OdeTest::test_sb . $OdeEnv::dirsep . "gendir3" . 
              $OdeEnv::dirsep . "gendir4";
  open( INPUT, ">$cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT 
      "genpath -I$filestr1 -L$filestr -E$filestr > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -sb -rc; genpath -I -L -E;";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: genpath -I$filestr1 -L$filestr -E$filestr; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "workon or genpath returned a non-zero exit code";
  }
  if (!$reg_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != 10))
  {
    $reg_error = 1;
    $error_txt = "genpath did not output the correct number of lines";
  }
  $filestr1 = OdePath::unixize( "-I" . $filestr1 );
  if (!$reg_error && (OdeFile::findInFile( $filestr1, $OdeTest::tmpfile ) != 1))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 either not found or not the first line "
                 . "in $OdeTest::tmpfile";
  }
  $filestr1 = join( $OdeEnv::dirsep, 
                    ("export", $OdeEnv::machine, "gendir1", "gendir2") );
  $filestr = "-I" . $OdeTest::test_sb1 . $OdeEnv::dirsep . $filestr1;
  $filestr = OdePath::unixize( $filestr );
  if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 8))
  {
    $reg_error = 1;
    $error_txt = "$filestr either not found or not in the correct position "
                 . "in $OdeTest::tmpfile";
  }
  $filestr = "-I" . $OdeTest::test_sb . $OdeEnv::dirsep . $filestr1;
  $filestr = OdePath::unixize( $filestr );
  if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 9))
  {
    $reg_error = 1;
    $error_txt = "$filestr either not found or not in the correct position "
                 . "in $OdeTest::tmpfile";
  }
  $filestr = "-I" . $OdeTest::test_bbdir . $OdeEnv::dirsep . $filestr1;
  $filestr = OdePath::unixize( $filestr );
  if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 10))
  {
    $reg_error = 1;
    $error_txt = "$filestr either not found or not in the correct position "
                 . "in $OdeTest::tmpfile";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "genpath", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "genpath", 3, $tmpstr );
  }

  ## Regression Test Case 4 - run genpath -a -l -I -I inside workon
  ## Should add only the existing directories specified by -I to 
  ## the search path with each line prepended by -L
  $filestr = "." . $OdeEnv::dirsep . "gendir1" . $OdeEnv::dirsep . "gendir2";
  $filestr1 = $OdeTest::test_sb . $OdeEnv::dirsep . "src"; 
  open( INPUT, ">$cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT 
         "genpath -I$filestr -I$filestr1 -a -l > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -sb -rc; genpath -I -I -a -l;";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: genpath -I$filestr -I$filestr1 -a -l; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "workon or genpath returned a non-zero exit code";
  }
  if (!$reg_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != 1))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::tmpfile has incorrect number of lines";
  }
  $filestr = "-L" . $OdeTest::test_sb . $OdeEnv::dirsep . "src";
  $filestr = OdePath::unixize( $filestr );
  if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 1))
  {
    $reg_error = 1;
    $error_txt = "$filestr either not found or not in the correct position "
                 . "in $OdeTest::tmpfile";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "genpath", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "genpath", 4, $tmpstr );
  }

  ## Regression Test Case 5 - run genpath -I -S -l -i inside workon
  ## Should output only source paths with each line prepended by -I  
  $filestr = "." . $OdeEnv::dirsep . "gendir1" . $OdeEnv::dirsep . "gendir2";
  open( INPUT, ">$cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT 
         "genpath -I$filestr -S -l -i > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -sb -rc; genpath -I -S -l -i;";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: genpath -I$filestr -S -l -i; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "workon or genpath returned a non-zero exit code";
  }
  if (!$reg_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != 3))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::tmpfile has incorrect number of lines";
  }
  $filestr = join( $OdeEnv::dirsep, ("src", "gendir1", "gendir2") );
  $filestr1 = "-I" . $OdeTest::test_sb1 . $OdeEnv::dirsep . $filestr;
  $filestr1 = OdePath::unixize( $filestr1 );
  if (!$reg_error && (OdeFile::findInFile( $filestr1, $OdeTest::tmpfile ) != 1))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 either not found or not the first line "
                 . "in $OdeTest::tmpfile";
  }
  $filestr1 = "-I" . $OdeTest::test_sb . $OdeEnv::dirsep . $filestr;
  $filestr1 = OdePath::unixize( $filestr1 );
  if (!$reg_error && (OdeFile::findInFile( $filestr1, $OdeTest::tmpfile ) != 2))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 either not found or not the second line "
                 . "in $OdeTest::tmpfile";
  }
  $filestr1 = "-I" . $OdeTest::test_bbdir . $OdeEnv::dirsep . $filestr;
  $filestr1 = OdePath::unixize( $filestr1 );
  if (!$reg_error && (OdeFile::findInFile( $filestr1, $OdeTest::tmpfile ) != 3))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 either not found or not the third line "
                 . "in $OdeTest::tmpfile";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "genpath", 5, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "genpath", 5, $tmpstr );
  }

  ## Regression Test Case 6 - run genpath -I -O -z -i -l inside workon
  ## Should output only obj paths with each line prepended by -L  
  $filestr = "." . $OdeEnv::dirsep . "gendir1" . $OdeEnv::dirsep . "gendir2";
  open( INPUT, ">$cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT 
         "genpath -I$filestr -O -z -i -l > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -sb -rc; genpath -I -O -i -l;";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: genpath -I$filestr -O -z -i -l; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "workon or genpath returned a non-zero exit code";
  }
  if (!$reg_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != 3))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::tmpfile has incorrect number of lines";
  }
  $filestr = join( $OdeEnv::dirsep, 
                   ("obj", $OdeEnv::machine, "gendir1", "gendir2") );
  $filestr1 = "-L" . $OdeTest::test_sb1 . $OdeEnv::dirsep . $filestr;
  $filestr1 = OdePath::unixize( $filestr1 );
  if (!$reg_error && (OdeFile::findInFile( $filestr1, $OdeTest::tmpfile ) != 1))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 either not found or not the first line "
                 . "in $OdeTest::tmpfile";
  }
  $filestr1 = "-L" . $OdeTest::test_sb . $OdeEnv::dirsep . $filestr;
  $filestr1 = OdePath::unixize( $filestr1 );
  if (!$reg_error && (OdeFile::findInFile( $filestr1, $OdeTest::tmpfile ) != 2))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 either not found or not the second line "
                 . "in $OdeTest::tmpfile";
  }
  $filestr1 = "-L" . $OdeTest::test_bbdir . $OdeEnv::dirsep . $filestr;
  $filestr1 = OdePath::unixize( $filestr1 );
  if (!$reg_error && (OdeFile::findInFile( $filestr1, $OdeTest::tmpfile ) != 3))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 either not found or not the third line "
                 . "in $OdeTest::tmpfile";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "genpath", 6, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "genpath", 6, $tmpstr );
  }

  ## Regression Test Case 7 - run genpath -I -l -O -S -z -R inside workon
  ## Should output only obj paths with -I and -L inhibited on each line 
  $filestr = "." . $OdeEnv::dirsep . "gendir1" . $OdeEnv::dirsep . "gendir2";
  $filestr1 = "." . $OdeEnv::dirsep . "gendir3" . $OdeEnv::dirsep . "gendir4";
  open( INPUT, ">$cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT 
     "genpath -I$filestr -l -z -O -S -R$filestr1 > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -sb -rc; genpath -I -l -O -S -z -R;";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: genpath -I$filestr -l -z -O -S -R$filestr1; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "workon or genpath returned a non-zero exit code";
  }
  if (!$reg_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != 6))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::tmpfile has incorrect number of lines";
  }
  $filestr = join( $OdeEnv::dirsep, ("src", "gendir1", "gendir2") );
  $filestr1 = $OdeTest::test_sb1 . $OdeEnv::dirsep . $filestr;
  $filestr1 = OdePath::unixize( $filestr1 );
  if (!$reg_error && (OdeFile::findInFile( $filestr1, $OdeTest::tmpfile ) != 1))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 either not found or not the first line "
                 . "in $OdeTest::tmpfile";
  }
  $filestr = join( $OdeEnv::dirsep, ("src", "gendir3", "gendir4") );
  $filestr1 = $OdeTest::test_sb1 . $OdeEnv::dirsep . $filestr;
  $filestr1 = OdePath::unixize( $filestr1 );
  if (!$reg_error && (OdeFile::findInFile( $filestr1, $OdeTest::tmpfile ) != 4))
  {
    $reg_error = 1;
    $error_txt = "$filestr1 either not found or not in the correct position "
                 . "in $OdeTest::tmpfile";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "genpath", 7, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "genpath", 7, $tmpstr );
  }

  ## Regression Test Case 8 - run genpath -I -l -V inside workon
  ## Should generate the output in VPATH format
  $filestr = "." . $OdeEnv::dirsep . "gendir1" . $OdeEnv::dirsep . "gendir2";
  open( INPUT, ">$cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT 
     "genpath -I$filestr -l -V > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
             . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -sb -rc; genpath -I -l -V;";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: genpath -I$filestr -l -V; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "workon or genpath returned a non-zero exit code";
  }
  #the tempfile has one big line (counted as zero on some OS's)
  my $num_tmpfile_lines = OdeFile::numLinesInFile( $OdeTest::tmpfile );
  if (!$reg_error && $num_tmpfile_lines != 0 && $num_tmpfile_lines != 1)
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::tmpfile has incorrect number of lines";
  }
  if (!$reg_error)
  {
    open( OUTPUT, "< $OdeTest::tmpfile" ) ||
      OdeInterface::logPrint( "Could not open file $OdeTest::tmpfile\n" );
    $filestr = readline( *OUTPUT );
    close( OUTPUT );
    @list = split $OdeEnv::pathsep, $filestr;
  }
  if (!$reg_error && scalar( @list ) != 7)
  {
    $reg_error = 1;
    $error_txt = "The path has incorrect number of entries";
  }
  if (!$reg_error && (@list[0] ne ""))
  {
    $reg_error = 1;
    $error_txt = "The first entry in the path is incorrect";
  }
  $filestr = join( $OdeEnv::dirsep, 
         ($OdeTest::test_sb1, "obj", $OdeEnv::machine, "gendir1", "gendir2") );
  $filestr = OdePath::unixize( $filestr );
  if (!$reg_error && (@list[1] ne $filestr))
  {
    $reg_error = 1;
    $error_txt = "The second entry in the path is incorrect";
  }
  $filestr = join( $OdeEnv::dirsep, 
                   ($OdeTest::test_sb, "src", "gendir1", "gendir2") );
  $filestr = OdePath::unixize( $filestr );
  if (!$reg_error && (@list[4] ne $filestr))
  {
    $reg_error = 1;
    $error_txt = "The fifth entry in the path is incorrect";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::logPrint( "\n" );
    OdeInterface::printResult( "fail", "regression", "genpath", 8, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::logPrint( "\n" );
    OdeInterface::printResult( "pass", "regression", "genpath", 8, $tmpstr );
  }

  ## Regression Test Case 9 - run genpath -badflag
  ## Should produce an error
  $command = "genpath -A >> $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "genpath -A;";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status == 0) 
  {
    $reg_error = 1;
    $error_txt = "genpath returned a zero exit code";
  }
  if (!$reg_error && !(OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $reg_error = 1;
    $error_txt = "The command did not produce an error";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "genpath", 9, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "genpath", 9, $tmpstr );
  }

  # Do NT-only test cases to check that genpath puts double quotes
  # around any directory in its output stream that has blanks in
  # the directory name.
  # Since ODE is still does not support filename blanks in backing
  # builds or sandboxes, not much testing is required yet.  However,
  # it is expected that genpath should work.
  if ( $OdeEnv::WIN32 )
  {
    ## Regression Test Case 10 - run genpath -I -I inside workon
    ## Should add the directories specified by -I to the search path
    ## Blanks are in some of the file names.
    $filestr = '".' . $OdeEnv::dirsep . "gen dir1" . $OdeEnv::dirsep . "gendir2"
                . '"';
    $filestr1 = $OdeTest::test_sb . $OdeEnv::dirsep . "gendir3" . 
                $OdeEnv::dirsep . "gen dir4";
    $filestr2 = '"' . $filestr1 . '"';
    open( INPUT, ">$cmd_file" ) ||
      OdeInterface::logPrint( "Could not open file $cmd_file\n" );
    print( INPUT "genpath -I$filestr -I$filestr2 > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
    print( INPUT "exit\n" );
    close( INPUT );
    $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
               . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $tmpstr = "workon -sb -rc; genpath -I -I;";
    $status = OdeEnv::runOdeCommand( $command );
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG "Executing: genpath -I$filestr -I$filestr2; exit;\n" );
    close( BLDLOG );
    # Copy contents of tempfile into build log file
    OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
    #check command exit code
    if ($status != 0) 
    {
      $reg_error = 1;
      $error_txt = "workon or genpath returned a non-zero exit code";
    }
    if (!$reg_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != 7))
    {
      $reg_error = 1;
      $error_txt = "genpath did not output the correct number of lines";
    }
    $filestr = $OdeTest::test_sb1;
    $filestr = join( $OdeEnv::dirsep, 
                     ($filestr, "obj", $OdeEnv::machine, "gen dir1", "gendir2") );
    $filestr = '-I"' . OdePath::unixize( $filestr ) . '"';
    if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 1))
    {
      $reg_error = 1;
      $error_txt = "$filestr either not found or not the first line "
                   . "in $OdeTest::tmpfile";
    }
    $filestr = $OdeTest::test_sb;
    $filestr = join( $OdeEnv::dirsep, 
                     ($filestr, "src", "gen dir1", "gendir2") );
    $filestr = '-I"' . OdePath::unixize( $filestr ) . '"';
    if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 4))
    {
      $reg_error = 1;
      $error_txt = "$filestr either not found or not in the correct position "
                   . "in $OdeTest::tmpfile";
    }
    $filestr = $OdeTest::test_bbdir;
    $filestr = join( $OdeEnv::dirsep, 
                     ($filestr, "src", "gen dir1", "gendir2") );
    $filestr = '-I"' . OdePath::unixize( $filestr ) . '"';
    if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 6))
    {
      $reg_error = 1;
      $error_txt = "$filestr either not found or not in the correct position "
                   . "in $OdeTest::tmpfile";
    }
    $filestr = '-I"' . OdePath::unixize( $filestr1 ) . '"';
    if (!$reg_error && (OdeFile::findInFile( $filestr, $OdeTest::tmpfile ) != 7))
    {
      $reg_error = 1;
      $error_txt = "$filestr either not found or not in the correct position "
                   . "in $OdeTest::tmpfile";
    }
    if ($reg_error)
    {
      #Test case failed
      OdeInterface::printResult( "fail", "regression", "genpath", 10, $tmpstr );
      OdeInterface::logPrint( "Test Failed: $error_txt\n" );
      $error = 1;
      $reg_error = 0;
    }
    else
    {
      #Test case passed
      OdeInterface::printResult( "pass", "regression", "genpath", 10, $tmpstr );
    }

    ## Regression Test Case 11 - run genpath -I -l -V inside workon
    ## Should generate the output in VPATH format with quotes around
    ## directory names.
    $filestr = '".' . $OdeEnv::dirsep . "gen dir1" . $OdeEnv::dirsep . 'gendir2"';
    open( INPUT, ">$cmd_file" ) ||
      OdeInterface::logPrint( "Could not open file $cmd_file\n" );
    print( INPUT 
       "genpath -I$filestr -l -V > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
    print( INPUT "exit\n" );
    close( INPUT );
    $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
               . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $tmpstr = "workon -sb -rc; genpath -I -l -V;";
    $status = OdeEnv::runOdeCommand( $command );
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG "Executing: genpath -I$filestr -l -V; exit;\n" );
    close( BLDLOG );
    # Copy contents of tempfile into build log file
    OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
    #check command exit code
    if ($status != 0) 
    {
      $reg_error = 1;
      $error_txt = "workon or genpath returned a non-zero exit code";
    }
    #the tempfile has one big line not ending with a \n
    #numLinesInFile would return 0 in that case with some operting systems.
    #NT just may be unpredictable, returning 0 or 1.
    if (!$reg_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) > 1))
    {
      $reg_error = 1;
      $error_txt = "$OdeTest::tmpfile has incorrect number of lines";
    }
    if (!$reg_error)
    {
      open( OUTPUT, "< $OdeTest::tmpfile" ) ||
        OdeInterface::logPrint( "Could not open file $OdeTest::tmpfile\n" );
      $filestr = readline( *OUTPUT );
      close( OUTPUT );
      @list = split $OdeEnv::pathsep, $filestr;
    }
    if (!$reg_error && scalar( @list ) != 7)
    {
      $reg_error = 1;
      $error_txt = "The path has incorrect number of entries";
    }
    if (!$reg_error && (@list[0] ne ""))
    {
      $reg_error = 1;
      $error_txt = "The first entry in the path is incorrect";
    }
    $filestr = join( $OdeEnv::dirsep, 
         ($OdeTest::test_sb1, "obj", $OdeEnv::machine, "gen dir1", "gendir2") );
    $filestr = '"' . OdePath::unixize( $filestr ) . '"';
    if (!$reg_error && (@list[1] ne $filestr))
    {
      $reg_error = 1;
      $error_txt = "The second entry in the path is incorrect";
    }
    $filestr = join( $OdeEnv::dirsep, 
                     ($OdeTest::test_sb, "src", "gen dir1", "gendir2") );
    $filestr = '"' . OdePath::unixize( $filestr ) . '"';
    if (!$reg_error && (@list[4] ne $filestr))
    {
      $reg_error = 1;
      $error_txt = "The fifth entry in the path is incorrect";
    }
    if ($reg_error)
    {
      #Test case failed
      OdeInterface::logPrint( "\n" );
      OdeInterface::printResult( "fail", "regression", "genpath", 11, $tmpstr );
      OdeInterface::logPrint( "Test Failed: $error_txt\n" );
      $error = 1;
      $reg_error = 0;
    }
    else
    {
      #Test case passed
      OdeInterface::logPrint( "\n" );
      OdeInterface::printResult( "pass", "regression", "genpath", 11, $tmpstr );
    }
  
  } # end NT-only test cases

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
