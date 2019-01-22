###########################################
#
# MkVarAssgnTest module
# - module to test mk variable assignments
#
###########################################

package MkVarAssgnTest;

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
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $error_txt;

  OdeInterface::logPrint( "in MkVarAssgnTest\n");
 
  ## Testing variable assignments
  ## running mk -f var_assign.mk
  if (defined( $OdeEnv::UNIX ))
  {
    $status = runMkTest( "var_assign.mk", 
                     "variable assignments =,?=,+=,:=,!=,%=", 12, 1, "-DUNIX" );
  }
  else
  {
    $status = runMkTest( "var_assign.mk", 
                           "variable assignments =,?=,+=,:=,!=,%=", 12, 1 );
  }
  if (!$error) { $error = $status; }

  ## running mk -f var_assign_error1.mk  
  $status = runMkErrorTest( "var_assign_error1.mk", 
                            "variable assignments =", 2 );
  if (!$error) { $error = $status; }

  ## running mk -f var_assign_error2.mk  
  $status = runMkErrorTest( "var_assign_error2.mk", 
                            "variable assignments !=", 3 );
  if (!$error) { $error = $status; }

  ## running mk -f var_assign_error3.mk  
  $status = runMkErrorTest( "var_assign_error3.mk", 
                             "recursive variable assignments", 4 );
  if (!$error) { $error = $status; }

  ## running mk -f var_assign_error4.mk  
  $status = runMkErrorTest( "var_assign_error4.mk", 
                            "variable assignments =", 5 );
  if (!$error) { $error = $status; }

  ## Testing the environment variables inside makefile
  if (defined($OdeEnv::WINNT))
  {
    # @@@ Remove this WINNT condition whenever the BPS service provider
    # @@@ problem has been fixed.  Problem is that somehow the ENV is wrong.
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                               6, "@@@ Test not run due to BPS service provider problem" );
  }
  else
  {
  $makefile_name = "variables" . $OdeEnv::dirsep . "envvars.mk";
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : variable assignments" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  my $home = $ENV{'HOME'};
  my $path = $ENV{'PATH'};
  $command = "mk -f $makefile > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && (OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "Error occured while echoing environment variables" .
                 " in the makefile";
  }
  if (!$mk_error && !(OdeFile::findInFile( "$home", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "make did not echo environment variable HOME";
  }
  if (!$mk_error && !(OdeFile::findInFile( $path, $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "make did not echo environment variable PATH";
  }
  if ($mk_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                               6, "check environment variables" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                               6, "check environment variables" );
  }
  } # endif defined(WINNT)

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
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
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
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
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

1;
