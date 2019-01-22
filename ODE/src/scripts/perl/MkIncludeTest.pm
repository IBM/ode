#########################################
#
# MkIncludeTest module
# - module to test mk include logic
#   Also tests debug flag "i"
#
#########################################

package MkIncludeTest;

use File::Path;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdeUtil;
use OdeInterface;
use strict;
use Cwd;

#########################################
#
# sub run
#
#########################################
sub run ()
{
  my $status;
  my $error = 0;
  my $tststr;
  my $dir = cwd();
  my @delete_list;
  my $testfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "testfile1.mk";
  my $testfile2 = $OdeEnv::tempdir . $OdeEnv::dirsep . "testfile2.mk";
  my $testfile3 = $OdeEnv::tempdir . $OdeEnv::dirsep . "testfile3.mk";
  my $testfile4 = $OdeEnv::tempdir . $OdeEnv::dirsep . "testfile4.mk";
  my $testfile5 = $OdeEnv::tempdir . $OdeEnv::dirsep . "testfile5.mk"; 
  my $testfile6 = $OdeEnv::tempdir . $OdeEnv::dirsep . "testdir" . 
                  $OdeEnv::dirsep . "testfile1.mk";
  my $testfile7 = $OdeEnv::tempdir . $OdeEnv::dirsep . "testdir" . 
                  $OdeEnv::dirsep . "testfile7.mk";

  OdeInterface::logPrint( "in MkIncludeTest\n");
 
  open( INPUT, "> $testfile1" ) ||
  	OdeInterface::logPrint( "Could not open file $testfile1\n" );
  print( INPUT "testvar1=100" );
  close( INPUT );
  open( INPUT, "> $testfile2" ) ||
  	OdeInterface::logPrint( "Could not open file $testfile2\n" );
  print( INPUT "testvar2=100" );
  close( INPUT );
  open( INPUT, "> $testfile3" ) ||
  	OdeInterface::logPrint( "Could not open file $testfile3\n" );
  print( INPUT ".PATH : \${FILEPATH}\n" );
  print( INPUT ".include \"testfile4.mk\"\n" );
  print( INPUT ".tryinclude \"non-existent-file\"" );
  close( INPUT );
  open( INPUT, "> $testfile4" ) ||
  	OdeInterface::logPrint( "Could not open file $testfile4\n" );
  print( INPUT "testvar4=100" );
  close( INPUT );
  open( INPUT, "> $testfile5" ) ||
  	OdeInterface::logPrint( "Could not open file $testfile5\n" );
  print( INPUT "testvar5=100" );
  close( INPUT );
  mkpath( $OdeEnv::tempdir . $OdeEnv::dirsep . "testdir" );
  open( INPUT, "> $testfile6" ) ||
  	OdeInterface::logPrint( "Could not open file $testfile6\n" );
  print( INPUT "testvar1=200\n" );
  print( INPUT "testvar6=300" );
  close( INPUT );
  open( INPUT, "> $testfile7" ) ||
  	OdeInterface::logPrint( "Could not open file $testfile7\n" );
  print( INPUT "testvar6=400" );
  close( INPUT );
  if (!(-f $testfile1) || !(-f $testfile2) || !(-f $testfile3) 
       || !(-f $testfile4) || !(-f $testfile5) || !(-f $testfile6)
       || !(-f $testfile7))
  {
    OdeInterface::printError( "mk : include" );
    OdeInterface::logPrint( "Could not locate test makefiles\n" );
    return( 1 );
  }

  ## Testing include
  ## running mk -f include.mk FILEPATH=<path> -I <path>
  $status = runMkTest( "include.mk", "include", 4, 1, 
                       "FILEPATH=$OdeEnv::tempdir", "-I $OdeEnv::tempdir" );
  if (!$error) { $error = $status; }

  ## running mk -f include.mk FILEPATH=<path> 
  ## should produce an error since -I is not specified
  ## should not be able to find testfile5.mk
  $status = runMkErrorTest( "include.mk", "include", 2, 
                             "FILEPATH=$OdeEnv::tempdir" );
  if (!$error) { $error = $status; }

  ## running mk -f include.mk -I <path> 
  ## should produce an error since FILEPATH is not specified
  ## should not be able to find testfile1.mk
  $status = runMkErrorTest( "include.mk", "include", 3, 
                             "-I $OdeEnv::tempdir" );
  if (!$error) { $error = $status; }

  ## running mk -f include.mk -I <path> 
  ## should not produce an error as VPATH is set in the environment
  $ENV{'VPATH'} = $OdeEnv::tempdir;
  $status = runMkTest( "include.mk", "include", 4, 4, 
                       "-I $OdeEnv::tempdir" );
  if (!$error) { $error = $status; }
  delete $ENV{'VPATH'};
  
  ## To test environment variable MAKEINCLUDECOMPAT
  ## running mk -f include.mk FILEPATH=<path> 
  ## should not produce an error eventhough -I is not specified
  ## as MAKEINCLUDECOMPAT is defined
  $ENV{'MAKEINCLUDECOMPAT'} = 1;
  $status = runMkTest( "include.mk", "include", 4, 5, 
                       "FILEPATH=$OdeEnv::tempdir" );
  if (!$error) { $error = $status; }

  ## Unsetting MAKEINCLUDECOMPAT 
  delete $ENV{'MAKEINCLUDECOMPAT'};

  ## running 
  ## mk -f include_searchorder.mk FILEPATH=<path> FILEPATH1=<path1> PASS=1
  $tststr = $OdeEnv::tempdir . $OdeEnv::dirsep . "testdir";
  $status = runMkTest( "include_searchorder.mk", "include", 2, 6, "PASS=1",
                       "FILEPATH=$OdeEnv::tempdir", "FILEPATH1=$tststr" );
  if (!$error) { $error = $status; }

  ## running 
  ## mk -f include_searchorder.mk FILEPATH=<path> FILEPATH1=<path1> PASS=2
  $status = runMkTest( "include_searchorder.mk", "include", 2, 7, "PASS=2",
                       "FILEPATH=$OdeEnv::tempdir", "FILEPATH1=$tststr" );
  if (!$error) { $error = $status; }

  ## Testing tryinclude
  ## running mk -f tryinclude.mk FILEPATH=<path>
  ## should not be able to find testfile2.mk, testfile3.mk as
  ## -I is not specified
  $status = runMkTest( "tryinclude.mk", "tryinclude", 3, 8, "PASS=1",
                       "FILEPATH=$OdeEnv::tempdir" );
  if (!$error) { $error = $status; }

  ## running mk -f tryinclude.mk FILEPATH=<path> -I <path>
  ## should find testfile2.mk, testfile3.mk as
  ## -I is specified
  $status = runMkTest( "tryinclude.mk", "tryinclude", 3, 9, "PASS=2",
                       "FILEPATH=$OdeEnv::tempdir -I $OdeEnv::tempdir" );
  if (!$error) { $error = $status; }

  ## running mk -f tryinclude.mk FILEPATH=<path> 
  ## should find testfile2.mk, testfile3.mk eventhough -I 
  ## is not specified as MAKEINCLUDECOMPAT is defined
  $ENV{'MAKEINCLUDECOMPAT'} = 1;
  $status = runMkTest( "tryinclude.mk", "tryinclude", 3, 10, "PASS=2",
                       "FILEPATH=$OdeEnv::tempdir" );
  if (!$error) { $error = $status; }

  ## Unsetting MAKEINCLUDECOMPAT 
  delete $ENV{'MAKEINCLUDECOMPAT'};

  ## running 
  ## mk -f include_searchorder.mk FILEPATH=<path> FILEPATH1=<path1> PASS=3
  $status = runMkTest( "include_searchorder.mk", "tryinclude", 2, 11, "PASS=3",
                       "FILEPATH=$OdeEnv::tempdir", "FILEPATH1=$tststr" );
  if (!$error) { $error = $status; }

  ## running 
  ## mk -f include_searchorder.mk FILEPATH=<path> FILEPATH1=<path1> PASS=4
  $status = runMkTest( "include_searchorder.mk", "tryinclude", 2, 12, "PASS=4",
                       "FILEPATH=$OdeEnv::tempdir", "FILEPATH1=$tststr" );
  if (!$error) { $error = $status; }
  
  ## running mk -f include_error1.mk FILEPATH=<path> 
  ## should produce an error since a non existent file
  ## is included
  $status = runMkErrorTest( "include_error1.mk", "include", 13, 
                             "FILEPATH=$OdeEnv::tempdir" );
  if (!$error) { $error = $status; }

  ## running mk -f include_error2.mk FILEPATH=<path> 
  ## should produce an error since .PATH is specified
  ## after including the file
  $status = runMkErrorTest( "include_error2.mk", "include", 14, 
                             "FILEPATH=$OdeEnv::tempdir" );
  if (!$error) { $error = $status; }

  ## Testing include with a relative path
  mkpath(
     $OdeEnv::tempdir . $OdeEnv::dirsep . "testdir" . $OdeEnv::dirsep . "tdir");
  OdePath::chgdir(
     $OdeEnv::tempdir . $OdeEnv::dirsep . "testdir" . $OdeEnv::dirsep . "tdir");
  ## running mk -f include.mk FILEPATH=<path> -I <path>
  $status = runMkTest( "include.mk", "include relative paths", 4, 15,
      "FILEPATH=..$OdeEnv::dirsep..", "-I ..$OdeEnv::dirsep..$OdeEnv::dirsep" );
  if (!$error) { $error = $status; }
  OdePath::chgdir( $dir );

  ## Testing debug flag "i"
  ## running mk -f include.mk FILEPATH=<path> -I <path> -di
  $status = runMkDebug_i( "include.mk", "debug flag \"i\"", 6, 4, 16,
                          "FILEPATH=$OdeEnv::tempdir", "-I $OdeEnv::tempdir");
  if (!$error) { $error = $status; }
  ## running mk -f tryinclude.mk FILEPATH=<path> -I <path> -di
  $status = runMkDebug_i( "tryinclude.mk", "debug flag \"i\"", 7, 3, 17,
                 "PASS=2", "FILEPATH=$OdeEnv::tempdir", "-I $OdeEnv::tempdir");
  if (!$error) { $error = $status; }

  ## deleting the temporary makefiles
  @delete_list = ($testfile1, $testfile2, $testfile3, $testfile4,
                  $testfile5, $OdeEnv::tempdir . $OdeEnv::dirsep . "testdir", 
                  $OdeTest::tmpfile);
  OdeFile::deltree( \@delete_list );
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
  $makefile_name = "include" . $OdeEnv::dirsep . $filename;
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
  $makefile_name = "include" . $OdeEnv::dirsep . $filename;
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
  ((OdeFile::numPatternsInFile( "ODEMKPASS", $OdeTest::tmpfile ) != 0) ||
   (OdeFile::numPatternsInFile( "ODEMKERROR", $OdeTest::tmpfile ) != 0)))
  {
    $mk_error = 1;
    $error_txt = "Some targets executed after an error occured";
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

####################################################################
#
# sub runMkDebug_i
# - to make and run the command with all the
#   arguments passed and the debug flag "i"
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   debug_lines: number of debug lines. These are prepended by Inc:
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   args: a list of any other commandline args
####################################################################
sub runMkDebug_i( $@ )
{
  my ($filename, $tststr, $debug_lines, $num_tests, $testno, @args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $inc_count = 0;
  my $status;
  my $error_txt;
  $makefile_name = "include" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  $command = "mk -f $makefile @args -di > $OdeTest::tmpfile $OdeEnv::stderr_redir";
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
  $inc_count = OdeFile::numPatternsInFile( "Inc:", $OdeTest::tmpfile );
  if (!$mk_error && ($inc_count != $debug_lines))
  {
    $mk_error = 1;
    $error_txt = "Output has incorrect number of debug lines";
  }
  $inc_count = $debug_lines + $num_tests;
  if (!$mk_error &&
      (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != $inc_count))
  {
    $mk_error = 1;
    $error_txt = "Output has incorrect number of lines";
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

1;
