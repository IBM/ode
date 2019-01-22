#########################################
#
# Sblstest module
# - module to run Sblstest for different
#   test levels
#
#########################################

package Sblstest;
use strict;
use OdeUtil;
use OdeEnv;
use OdeTest;
use OdePath;
use OdeInterface;
use Cwd;
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
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my @delete_list = ($OdeTest::test_sb_rc, $OdeTest::test_sb, 
                     $OdeTest::tmpfile, $cmd_file);
  OdeFile::deltree( \@delete_list );

  OdeInterface::logPrint( "in Sblstest - test level is normal\n");
  OdeEnv::runOdeCommand( "sbls -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");
  
  $command = "mksb -dir $OdeTest::test_sbdir -rc $OdeTest::test_sb_rc "
   . "-back $OdeTest::test_bbdir $OdeTest::test_sb_name "
   . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || !(-d $OdeTest::test_sb) || 
     !(-f join( $OdeEnv::dirsep, ($OdeTest::test_sb, "rc_files", "sb.conf") )) )
  {
    #Test case failed
    OdeInterface::printError( "sbls : mksb does not work" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "sbls > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n");
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb_name "
            . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: sbls; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if (($status == 0) && (OdeFile::findInFile(
                                "Buildconf.exp", $OdeTest::tmpfile )))
  {
    #Test 1 Passed
    OdeInterface::printResult( "pass", "normal", "sbls", 1, 
                          "workon -rc -sb; sbls; exit" );
  }
  else
  {
    #Test 1 Failed
    OdeInterface::printResult( "fail", "normal", "sbls", 1, 
                          "workon -rc -sb; sbls; exit" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "sbls -alR > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n");
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb_name "
            . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";

  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: sbls -alR; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  if (($status == 0) && (OdeFile::findInFile(
                                "retserver.c", $OdeTest::tmpfile )))
  {
    #Test 2 Passed
    OdeInterface::printResult( "pass", "normal", "sbls", 2, 
                          "workon -rc -sb; sbls -alR; exit" );
  }
  else
  {
    #Test 2 Failed
    OdeInterface::printResult( "fail", "normal", "sbls", 2, 
                          "workon -rc -sb; sbls -alR; exit" );
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
  my $lines = 0;
  my @fileline;
  my $dir = cwd();
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my $test_sb1_rc = $OdeTest::test_sb1 . $OdeEnv::dirsep . "odetestrc";
  my @delete_list = ($OdeTest::test_sb_rc, $OdeTest::test_sb, 
                     $OdeTest::test_sb1, $OdeTest::tmpfile, $cmd_file);

  OdeInterface::logPrint( "in sblstest - test level is regression\n");
  OdeEnv::runOdeCommand( "sbls -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

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
    OdeInterface::printError( "sbls : mksb does not work" );
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
    OdeInterface::printError( "sbls : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  ## Regression Test Case 1 - run sbls 
  ## Should print the directory contents in the current directory
  if ( OdePath::chgdir( $OdeTest::test_sb . $OdeEnv::dirsep . "src" ) == 0)
  {
    OdeInterface::logPrint( 
                "Can't cd to $OdeTest::test_sb . $OdeEnv::dirsep . src\n" );
  }
  $command = "sbls > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "sbls";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "sbls returned a non-zero exit code";
  }
  $lines = OdeFile::numLinesInFile( $OdeTest::tmpfile );
  if (!$reg_error && ($lines != 0))
  {
    $reg_error = 1;
    $error_txt = "sbls output has incorrect number of directory entries";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "sbls", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "sbls", 1, $tmpstr );
  }
  #get back to the original directory
  if (OdePath::chgdir( $dir ) == 0)
  {
    OdeInterface::logPrint( "Can't cd to $dir\n" );
  }

  ## Regression Test Case 2 - run sbls -Fa inside workon
  ## Should print the directory contents in the present directory
  ## and the backing build
  $filestr = join( $OdeEnv::dirsep, 
                   ($OdeTest::test_sb1, "src", "bin", "server", "temp") );
  OdeInterface::logPrint("creating path $filestr ...\n");
  mkpath( $filestr );
  if (!(-d $filestr))
  {
    OdeInterface::logPrint( "$filestr not created\n" );
  }
  $filestr .= $OdeEnv::dirsep . "server.c";
  $filestr1 = $OdeTest::test_bbdir . $OdeEnv::dirsep . "src" . $OdeEnv::dirsep  
        . "bin" . $OdeEnv::dirsep . "server" . $OdeEnv::dirsep . "server.c";
  copy( $filestr1, $filestr );
  if (!(-f $filestr)) 
  { 
    OdeInterface::logPrint( "$filestr not copied\n" );
  }
  $filestr = $OdeTest::test_sb1 . $OdeEnv::dirsep . "src" . $OdeEnv::dirsep
            . "bin" . $OdeEnv::dirsep . "server" . $OdeEnv::dirsep . ".sbrc"; 
  $filestr1 = $OdeTest::test_bbdir . $OdeEnv::dirsep . ".sbrc";  
  copy( $filestr1, $filestr );  
  if (!(-f $filestr))
  {
    OdeInterface::logPrint( "$filestr not copied\n" );
  }
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "cd bin\n");
  print( INPUT "cd server\n");
  print( INPUT "mklinks server.c -auto\n");
  print( INPUT "sbls -Fa > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n");
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
            . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -rc -sb; mklinks; sbls -Fa; exit;"; 
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: cd bin/server; mklinks server.c -auto; sbls -Fa; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "sbls returned a non-zero exit code";
  }
  $lines = OdeFile::numLinesInFile( $OdeTest::tmpfile );
  ##bbexample/src/bin/server has only 4 files. sbls output must have 8 lines
  ##any addition of files to this directory should change this figure.
  ##The extra lines account for the blank spaces in sbls output
  if (!$reg_error && ($lines != 8))
  {
    $reg_error = 1;
    $error_txt = "sbls output has incorrect number of directory entries";
  }
  ##Testing for the files in the backing build
  if (!$reg_error && !(OdeFile::findInFile( "buildserver.c^^", 
                                                 $OdeTest::tmpfile, 1 )))
  {
    $reg_error = 1;
    $error_txt = "sbls output does not contain buildserver.c^^";
  }
  if (!$reg_error && !(OdeFile::findInFile( ".sbrc", $OdeTest::tmpfile, 1 )))
  {
    $reg_error = 1;
    $error_txt = "sbls output does not contain .sbrc";
  }
  ##Testing for the directories
  if (!$reg_error && !(OdeFile::findInFile( "temp/", $OdeTest::tmpfile, 1)))
  {
    $reg_error = 1;
    $error_txt = "sbls output does not contain temp/";
  }
  ##Testing for the local files on non unix platforms and for the symbolic
  ##links on unix platforms
  if (!(defined( $OdeEnv::UNIX )))
  {
    if (!$reg_error && !(OdeFile::findInFile( "server.c", 
                                                 $OdeTest::tmpfile, 1 )))
    {
      $reg_error = 1;
      $error_txt = "sbls output does not contain server.c";
    }
  }
  else
  {
    if (!$reg_error && !(OdeFile::findInFile( "server.c@", 
                                                 $OdeTest::tmpfile, 1 )))
    {
      $reg_error = 1;
      $error_txt = "sbls output does not contain server.c@";
    }
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "sbls", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "sbls", 2, $tmpstr );
  }

  ## Regression Test Case 3 - run sbls -lp inside workon
  ## Should print the directory contents in the present directory
  ## and the backing build with absolute paths
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "cd bin\n");
  print( INPUT "cd server\n");
  print( INPUT "sbls -lp > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n");
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
            . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -rc -sb; sbls -lp; exit;";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: cd bin/server; sbls -lp; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "sbls returned a non-zero exit code";
  }
  $lines = OdeFile::numLinesInFile( $OdeTest::tmpfile );
  ##bbexample/src/bin/server has only 4 files. sbls output must have 6 lines
  ##any addition of files to this directory should change this figure.
  ##The two extra lines account for the blank spaces in sbls output.
  if (!$reg_error && ($lines != 6))
  {
    $reg_error = 1;
    $error_txt = "sbls output has incorrect number of directory entries";
  }
  open( FILE, $OdeTest::tmpfile ) ||
    OdeInterface::logPrint( "Could not open file $OdeTest::tmpfile\n" );
  @fileline = split ' ', readline( *FILE );
  $lines = scalar( @fileline );
  close FILE;
  if (!$reg_error && ($lines != 6))
  {
    $reg_error = 1;
    $error_txt = "sbls output has incorrect number of fields in a line";
  }
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_bbdir, "src", "bin", 
                                      "server", "buildserver.c") );
  if (!$reg_error && !($fileline[5] eq $filestr))
  {
    $reg_error = 1;
    $error_txt = "The last field in the sbls output is incorrect";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "sbls", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "sbls", 3, $tmpstr );
  }

  ## Regression Test Case 4 - run sbls -RF inside workon
  ## Should print the directory contents including the sub directories
  ## in the present directory and the backing build 
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "cd bin\n");
  print( INPUT "cd server\n");
  print( INPUT "sbls -RF > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n");
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb1_name "
            . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "workon -rc -sb; sbls -RF; exit;";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: cd bin/server; sbls -RF; exit;\n" );
  close( BLDLOG );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "sbls returned a non-zero exit code";
  }
  $lines = OdeFile::numLinesInFile( $OdeTest::tmpfile );
  ##bbexample/src/bin/server has only 4 files. sbls output must have 10 lines
  ##any addition of files to this directory should change this figure.
  ##The extra lines account for the blank spaces and directory paths in sbls
  ##output.
  if (!$reg_error && ($lines != 10))
  {
    $reg_error = 1;
    $error_txt = "sbls output has incorrect number of directory entries";
  }
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb1, "src", "bin", 
                                     "server") );
  if (!$reg_error && !(OdeFile::findInFile( $filestr, $OdeTest::tmpfile, 1 ))) 
  {
    $reg_error = 1;
    $error_txt = "sbls output does not contain $filestr";
  }
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb1, "src", "bin", "server",
                                     "temp") );
  if (!$reg_error && !(OdeFile::findInFile( $filestr, $OdeTest::tmpfile, 1 ))) 
  {
    $reg_error = 1;
    $error_txt = "sbls output does not contain $filestr";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "sbls", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "sbls", 4, $tmpstr );
  }

  ## Regression Test Case 5 - run sbls -badflag
  ## Should print an error message
  $command = "sbls -badflag > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "sbls -badflag";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status == 0) 
  {
    $reg_error = 1;
    $error_txt = "sbls returned a zero exit code";
  }
  if (!$reg_error && !(OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $reg_error = 1;
    $error_txt = "The command did not print an error message";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "sbls", 5, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "sbls", 5, $tmpstr );
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
