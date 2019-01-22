#########################################
#
# MkRTConditionalTest module
# - module to test mk realtime conditionals logic
#
#########################################

package MkRTConditionalTest;

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
  my @arg_list;
  my @str_list;


  OdeInterface::logPrint( "in MkRTConditionalTest\n");
  
  ## Testing runtime conditional .rif
  $status = runMkTest( "rtcond_if.mk", "runtime conditional if", 5, 1 );
  if (!$error) { $error = $status; }
  ## conditional .rif with an extra .relse
  $status = runMkErrorTest( "rtcond_if_error1.mk",
                            "runtime conditional if", 2 );
  if (!$error) { $error = $status; }
  ## conditional .rif with a missing .rendif
  $status = runMkErrorTest( "rtcond_if_error2.mk",
                            "runtime conditional if", 3 );
  if (!$error) { $error = $status; }

  ## Testing if runtime conditional is really defered to runtime
  $status = runMkTest( "rtcond_defered.mk", "runtime conditional defered", 4, 4 );
  if (!$error) { $error = $status; }

  ## Testing runtime conditional empty
  $status = runMkTest( "rtcond_empty.mk", "runtime conditional empty", 4, 5 );
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( "rtcond_empty_error1.mk",
                            "runtime conditional empty", 6 );
  if (!$error) { $error = $status; }

  ## Testing runtime conditional make
  $status = runMkTest( "rtcond_make.mk",
                       "runtime conditional make", 3, 7, "target1" );
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( "rtcond_make_error1.mk",
                            "runtime conditional make", 8 );
  if (!$error) { $error = $status; }

  ## Testing runtime conditional target
  if (defined( $OdeEnv::UNIX ))
  {
    $status = runMkTest( 
                "rtcond_target.mk",
                "runtime conditional target", 3, 9, "-DUNIX" );
  }
  else
  {
    $status = runMkTest( 
               "rtcond_target.mk", "runtime conditional target", 3, 9 );
  }
  if (!$error) { $error = $status; }
  ## testing with malformed runtime conditional
  $status = runMkErrorTest( "rtcond_target_error1.mk",
                            "runtime conditional target", 10 );
  if (!$error) { $error = $status; }

  ## Testing runtime conditional defined
  $status = runMkTest( "rtcond_defined.mk",
                       "runtime conditional defined", 4, 11 );
  if (!$error) { $error = $status; }
  ## testing with malformed conditional
  $status = runMkErrorTest( 
                 "cond_defined_error1.mk", "conditional defined", 12 );
  if (!$error) { $error = $status; }

  ## Testing runtime conditional exists
  $status = runMkTest( "rtcond_exists.mk", "runtime conditional exists", 4, 13,
                       "TMPFILE=$OdeEnv::tempdir", "LOGFILE=$OdeEnv::logdir",
                       "MACHINE=$OdeEnv::machine");
  if (!$error) { $error = $status; }

  ## Testing with malformed runtime conditional exists
  $status = runMkErrorTest( "rtcond_exists_error1.mk",
                            "runtime conditional exists", 14, 
                       "TMPFILE=$OdeEnv::tempdir", "LOGFILE=$OdeEnv::logdir",
                       "MACHINE=$OdeEnv::machine");
  if (!$error) { $error = $status; }

  # Testing Runtime Conditionals .rfor and .rendfor
  @str_list=("xyz", "ABC", "Hound Dog", "DEF", "Hound Dog", "GHI",
             "Hound Dog", "xyz", "4", "5", "6", "7",
             "A1X", "A2X", "A3X", "B1X", "B2X", "B3X",
             "C1X", "C2X", "C3X", "5", "-", "2", "A", "-", "C", 
             "JailhouseRock", "Blue", "Suede", "Shoes");
  @arg_list = ("-DELVIS");
  $status = runMkTest2( "rtcond_for.mk", "Runtime Cond: .rfor", 31, 15,  
                        \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  ## Testing rfor errors
  $status = runMkErrorTest( "rtcond_for_error1.mk",
                            ".rfor: No equal sign", 16, 
                       "TMPFILE=$OdeEnv::tempdir", "LOGFILE=$OdeEnv::logdir",
                       "MACHINE=$OdeEnv::machine");
  if (!$error) { $error = $status; }

  $status = runMkErrorTest( "rtcond_for_error2.mk",
                          ".rfor: no matching .rendfor", 17, 
                     "TMPFILE=$OdeEnv::tempdir", "LOGFILE=$OdeEnv::logdir",
                     "MACHINE=$OdeEnv::machine");
  if (!$error) { $error = $status; }

  $status = runMkErrorTest( "rtcond_for_error3.mk",
                          ".rfor: No arguments given", 18, 
                     "TMPFILE=$OdeEnv::tempdir", "LOGFILE=$OdeEnv::logdir",
                     "MACHINE=$OdeEnv::machine");
  if (!$error) { $error = $status; }

  $status = runMkErrorTest( "rtcond_for_error4.mk",
                          ".rfor: No variable given", 19, 
                     "TMPFILE=$OdeEnv::tempdir", "LOGFILE=$OdeEnv::logdir",
                     "MACHINE=$OdeEnv::machine");
  if (!$error) { $error = $status; }

  $status = runMkErrorTest( "rtcond_for_error5.mk",
                          ".rfor: Too many .rendfor's", 20, 
                     "TMPFILE=$OdeEnv::tempdir", "LOGFILE=$OdeEnv::logdir",
                     "MACHINE=$OdeEnv::machine");
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


##############################################################
#
# sub runMkTest2
# - to make and run the command with all the
#   arguments passed and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   str_list: reference to a list of all the strings
#             to be looked for in the tempfile
#   args: a reference to a list of any other commandline args
#############################################################
sub runMkTest2( $$$$$$ )
{
  my ($filename, $tststr, $num_tests, $testno, $str_list, $args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  my $str_num = scalar( @$str_list );
  my $index;
  $makefile_name = "conditionals" . $OdeEnv::dirsep . $filename;

  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }

  OdeInterface::logPrint( $command );
  $command = "mk -f $makefile @$args > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  # Check if the output of mk is correct
  if (!$mk_error)
  {
    for( $index=0; $index<$str_num; $index++)
    {
      if (!(OdeFile::findInFile( @$str_list[$index], $OdeTest::tmpfile )))
      {
        $mk_error = 1;
        $error_txt = "Error occured while testing $tststr";
        last;
      }
    }
  }
  # Check if the correct number of targets are executed
  if (!$mk_error && 
     (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != $num_tests))
  {
    $mk_error = 1;
    $error_txt = "Incorrect number of targets executed while testing $tststr";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, 
                               "mk", $testno, $tststr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, 
                               "mk", $testno, $tststr );
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
  
1;
