#########################################
#
# MkConditionalTest module
# - module to test mk conditionals logic
#   Also tests debug flag "c"
#
#########################################

package MkConditionalTest;

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
#
#########################################
sub run ()
{
  my $status;
  my $error = 0;

  OdeInterface::logPrint( "in MkConditionalTest\n");
  
  ## Testing conditional if
  $status = runMkTest( "cond_if.mk", "conditional if", 5, 1 );
  if (!$error) { $error = $status; }
  ## conditional .if with an extra .else
  $status = runMkErrorTest( "cond_if_error1.mk", "conditional if", 2 );
  if (!$error) { $error = $status; }
  ## conditional .if with a missing .endif
  $status = runMkErrorTest( "cond_if_error2.mk", "conditional if", 3 );
  if (!$error) { $error = $status; }

  ## Testing conditional ifdef
  $status = runMkTest( "cond_ifdef.mk", "conditional ifdef", 4, 4 );
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( "cond_ifdef_error1.mk", "conditional ifdef", 5 );
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( "cond_ifdef_error2.mk", "conditional ifdef", 6 );
  if (!$error) { $error = $status; }

  ## Testing conditional ifndef
  $status = runMkTest( "cond_ifndef.mk", "conditional ifndef", 5, 7 );
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( "cond_ifndef_error1.mk", "conditional ifndef", 8 );
  if (!$error) { $error = $status; }

  ## Testing conditional ifmake
  $status = runMkTest( 
           "cond_ifmake.mk", "conditional ifmake", 4, 9, "target1", "target2" );
  if (!$error) { $error = $status; }
  ## Testing with more targets
  $status = runMkTest( "cond_ifmake.mk", "conditional ifmake", 4, 10, 
                        "target1", "target2", "target2", "target1" );
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( 
             "cond_ifmake_error1.mk", "conditional ifmake", 11, "target1" );
  if (!$error) { $error = $status; }
  ## testing with an undefined source
  $status = runMkErrorTest( 
             "cond_ifmake_error2.mk", "conditional ifmake", 12, "target1" );
  if (!$error) { $error = $status; }

  ## Testing conditional ifnmake
  $status = runMkTest( 
        "cond_ifnmake.mk", "conditional ifnmake", 4, 13, "target1", "target2" );
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( 
        "cond_ifnmake_error1.mk", "conditional ifnmake", 14, "target1" );
  if (!$error) { $error = $status; }

  ## Testing conditional empty
  $status = runMkTest( "cond_empty.mk", "conditional empty", 5, 15 );
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( "cond_empty_error1.mk", "conditional empty", 16 );
  if (!$error) { $error = $status; }

  ## Testing conditional make
  $status = runMkTest( "cond_make.mk", "conditional make", 3, 17, "target1" );
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( "cond_make_error1.mk", "conditional make", 18 );
  if (!$error) { $error = $status; }

  ## Testing conditional target
  if (defined( $OdeEnv::UNIX ))
  {
    $status = runMkTest( 
                "cond_target.mk", "conditional target", 3, 19, "-DUNIX" );
  }
  else
  {
    $status = runMkTest( 
               "cond_target.mk", "conditional target", 3, 19 );
  }
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( "cond_target_error1.mk", "conditional target", 20 );
  if (!$error) { $error = $status; }

  ## Testing conditional defined
  $status = runMkTest( "cond_defined.mk", "conditional defined", 4, 21 );
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( 
                 "cond_defined_error1.mk", "conditional defined", 22 );
  if (!$error) { $error = $status; }

  ## Testing conditional exists
  $status = runMkTest( "cond_exists.mk", "conditional exists", 4, 23, 
                       "TMPFILE=$OdeEnv::tempdir", "LOGFILE=$OdeEnv::logdir",
                       "MACHINE=$OdeEnv::machine");
  if (!$error) { $error = $status; }

  ## Testing with malformed conditional
  $status = runMkErrorTest( "cond_exists_error1.mk", "conditional exists", 24, 
                       "TMPFILE=$OdeEnv::tempdir", "LOGFILE=$OdeEnv::logdir",
                       "MACHINE=$OdeEnv::machine");
  if (!$error) { $error = $status; }

  ## Testing debug flag "c"
  ## running mk -f cond_if.mk -dc
  $status = runMkDebug_c( "cond_if.mk", "debug flag \"c\"", 29, 5, 25 );
  if (!$error) { $error = $status; }
  ## running mk -f cond_empty.mk -dc
  $status = runMkDebug_c( "cond_empty.mk", "debug flag \"c\"", 16, 5, 26 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( $OdeTest::tmpfile );

  return $error; 
  
}

###########################################################
#
# sub runMkTest
# - to make and run the command with all the
#   arguments passed and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   args: a list of any other commandline args
############################################################
sub runMkTest( $@ )
{
  my ($filename, $tststr, $num_tests, $testno, @args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  $makefile_name = "conditionals" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
	{
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
	}
  $command = "mk -f $makefile @args > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  # Check if test passed
  # will catch both ERROR and ODEMKERROR messages in tmpfile
  if (!$mk_error && (OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "Error occured while testing $tststr";
  }
  # Check if only the specified targets are executed
  if (!$mk_error && 
  (OdeFile::numPatternsInFile( "ODEMKPASS", $OdeTest::tmpfile ) != $num_tests))
  {
    $mk_error = 1;
    $error_txt = "Incorrect number of targets executed while testing $tststr";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                                         $testno, $tststr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                                         $testno, $tststr );
  }
  return $mk_error;
}

###########################################################
#
# sub runMkErrorTest
# - to make and run the command with all the
#   arguments passed and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   testno: test number
#   args: a list of any other commandline args
############################################################
sub runMkErrorTest( $@ )
{
  my ($filename, $tststr, $testno, @args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  $makefile_name = "conditionals" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
	{
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
	}
  $command = "mk -f $makefile @args > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status == 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a zero exit code";
  }
  # Check if mk produced an error
  if (!$mk_error && !(OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "mk did not produce an error";
  }
  # Check if no targets are executed
  if (!$mk_error && 
  ((OdeFile::numPatternsInFile( "PASS", $OdeTest::tmpfile ) != 0) ||
   (OdeFile::numPatternsInFile( "FAIL", $OdeTest::tmpfile ) != 0)))
  {
    $mk_error = 1;
    $error_txt = "Some targets executed after an error occured";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                                         $testno, $tststr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                                         $testno, $tststr );
  }
  return $mk_error;
}

####################################################################
#
# sub runMkDebug_c
# - to make and run the command with all the
#   arguments passed and the debug flag "c" 
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   debug_lines: number of debug lines. These are prepended by Cond:
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   args: a list of any other commandline args
####################################################################
sub runMkDebug_c( $@ )
{
  my ($filename, $tststr, $debug_lines, $num_tests, $testno, @args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $cond_count = 0;
  my $bool_count = 0;
  my $status;
  my $error_txt;
  $makefile_name = "conditionals" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
	{
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
	}
  $command = "mk -f $makefile @args -dc > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  # Check the number of debug lines
  if (!$mk_error && 
     (OdeFile::numPatternsInFile( "Cond:", $OdeTest::tmpfile ) == 0))
  {
    $mk_error = 1;
    $error_txt = "Output has no debug lines";
  }
  $cond_count = OdeFile::numPatternsInFile( ".if", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( ".ifdef", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( ".ifndef", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( ".ifmake", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( ".ifnmake", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( ".else", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( ".elif", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( ".elifdef", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( ".elifndef", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( ".elifmake", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( ".elifnmake", $OdeTest::tmpfile );
  $bool_count = OdeFile::numPatternsInFile( "true", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( "false", $OdeTest::tmpfile )
              + OdeFile::numPatternsInFile( "skipped", $OdeTest::tmpfile );
  # Check the number of true and false statements
  if (!$mk_error && ($bool_count != $cond_count ))
  {
    $mk_error = 1;
    $error_txt = "Output has incorrect number of true and false statments"; 
  }
  # Check if test passed
  # will catch both ERROR and ODEMKERROR messages in tmpfile
  if (!$mk_error && (OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "Error occured while testing $tststr";
  }
  if (!$mk_error && 
  (OdeFile::numPatternsInFile( "ODEMKPASS", $OdeTest::tmpfile ) != $num_tests))
  {
    $mk_error = 1;
    $error_txt = "Incorrect number of targets executed while testing $tststr";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                                         $testno, $tststr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                                         $testno, $tststr );
  }
  return $mk_error;
}
  
1;
