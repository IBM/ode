#########################################
#
# MkSuffixTest module
# - module to test mk suffix transformations
#  Also tests debug flag "s"
#
#########################################

package MkSuffixTest;

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
  my $makefile;
  my $command;
  my $error = 0;
  my $mk_error = 0;
  my $error_txt;
  my $makefile_name;
  my $makefile_dir = "suffix" . $OdeEnv::dirsep;
  my $test_desc;
  my @srch_strs;

  OdeInterface::logPrint( "in MkSuffixTest\n");
  OdeEnv::runOdeCommand( "mk -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  ## Test 1
  # normal usage .c->.o->.a
  $makefile = "suffix_test1.mk"; 
  $test_desc = "normal suffix transformation - .c->.o->.a and .c->";
  $srch_strs[0] = "Working with: test1.c";
  $srch_strs[1] = "Converting: test1.c to test1";
  $srch_strs[2] = "Converting: test1.c to test1.o";
  $srch_strs[3] = "Converting: test1.o to test1.a";
  $status = runMkTest( $makefile, 1, $test_desc, @srch_strs );
  if (!$error) { $error = $status }
  
  ## Test 2
  # multiple dot suffix .tar.Z->.tar
  $makefile = "suffix_test2.mk"; 
  $test_desc = "multiple dot suffix transformation - .tar->.tar.Z";
  $srch_strs[0] = "Working with: test2.tar";
  $srch_strs[1] = "Converting: test2.tar to test2.tar.Z";
  $srch_strs[2] = "Converting: test2.tar.Z to test2";
  $srch_strs[3] = "";
  $status = runMkTest( $makefile, 2, $test_desc, @srch_strs );
  if (!$error) { $error = $status }
 
  ## Test 3
  # circular suffix transformation rule - .c.c
  $makefile = "suffix_test3.mk"; 
  $test_desc = "circular suffix transformation rules - .c.c";
  $status = runMkErrorTest( $makefile, 3, $test_desc );  
  if (!$error) { $error = $status }

  ## Test 4
  # circular suffix transformation rules
  $makefile = "suffix_test4.mk"; 
  $test_desc = "circular suffix transformation rules - .c.o and .o.c";
  $status = runMkErrorTest( $makefile, 4, $test_desc );
  if (!$error) { $error = $status }

  ## Test 5
  # suffix transformation precedence
  $makefile = "suffix_test5.mk"; 
  $test_desc = "suffix transformation precedence";
  $srch_strs[0] = "Working with: f.1";
  $srch_strs[1] = "Converting: f.1 to f.2";
  $srch_strs[2] = "Converting: f.2 to f.4";
  $srch_strs[3] = "Working with: foo.2";
  $srch_strs[4] = "Converting: foo.2 to foo";
  $status = runMkTest( $makefile, 5, $test_desc, @srch_strs );
  if (!$error) { $error = $status }

  ## Test 6
  # overriding default suffix transformation precedence
  $makefile = "suffix_test6.mk"; 
  $test_desc = "overriding suffix transformation";
  $srch_strs[0] = "Working with: f.1";
  $srch_strs[1] = "Converting: f.1 to f.3";
  $srch_strs[2] = "Converting: f.3 to f.4";
  $srch_strs[3] = "";
  $srch_strs[4] = "";
  $status = runMkTest( $makefile, 6, $test_desc, @srch_strs );
  if (!$error) { $error = $status }

  ## Test 7
  # testing debug flag "s"
  $makefile = "suffix_test1.mk";
  $test_desc = "debug flag \"s\"";
  $status = runMkDebug_s( $makefile, $test_desc, 12, 4, 7 );
  if (!$error) { $error = $status }

  ## Test 8
  # testing debug flag "s"
  $makefile = "suffix_test5.mk";
  $test_desc = "debug flag \"s\"";
  $status = runMkDebug_s( $makefile, $test_desc, 29, 5, 8 );
  if (!$error) { $error = $status }

  ## Test 9
  # patterns and suffixes
  $makefile = "suffix_pattern_test1.mk"; 
  $test_desc = "patterns and suffixes using subdirectories";
  $srch_strs[0]  = "Working With dir1/6.a";
  $srch_strs[1]  = "two";
  $srch_strs[2]  = "Converting: dir1/6.a to dir1/6.b";
  $srch_strs[3]  = "Working With 88.a";
  $srch_strs[4]  = "two";
  $srch_strs[5]  = "Converting: 88.a to 88.b";
  $srch_strs[6]  = "Working With ../99.a";
  $srch_strs[7]  = "two";
  $srch_strs[8]  = "Converting: ../99.a to ../99.b";
  $srch_strs[9]  = "Working With ../dir2/1.a";
  $srch_strs[10] = "one";
  $srch_strs[11] = "Converting: ../dir2/1.a to ../dir2/1.c";
  $status = runMkTest( $makefile, 9, $test_desc, @srch_strs );
  if (!$error) { $error = $status }

  ## Test 10
  # patterns and suffixes
  $makefile = "suffix_pattern_test2.mk"; 
  $test_desc = "patterns and suffixes using different subdirectories";
  $srch_strs[0]  = "Working With dir1/6.a";
  $srch_strs[1]  = "two";
  $srch_strs[2]  = "Converting: dir1/6.a to dir2/6.b";
  $srch_strs[3]  = "Working With ../dir1/1.a";
  $srch_strs[4]  = "one";
  $srch_strs[5]  = "Converting: ../dir1/1.a to dir2/1.c";
  $srch_strs[6]  = "Working With dir1/18.x.y";
  $srch_strs[7]  = "three";
  $srch_strs[8]  = "Converting: dir1/18.x.y to 18.z";
  $srch_strs[9]  = "Working With dir1/dir2/20.q";
  $srch_strs[10] = "four";
  $srch_strs[11] = "Converting: dir1/dir2/20.q to 20.p.q";
  $srch_strs[12] = "Working With dir1/43.x.y";
  $srch_strs[13] = "six";
  $srch_strs[14] = "Converting: dir1/43.x.y to dir2/43";
  $status = runMkTest( $makefile, 10, $test_desc, @srch_strs );
  if (!$error) { $error = $status }


  ## Test 11
  # patterns and suffixes
  $makefile = "suffix_pattern_test3.mk"; 
  $test_desc = "patterns and suffixes internal ordering";
  $srch_strs[0]  = "Working With 6.x";
  $srch_strs[1]  = "four";
  $srch_strs[2]  = "Converting: 6.x to 6.b";
  $srch_strs[3]  = "Working With dir2/99.a";
  $srch_strs[4]  = "two";
  $srch_strs[5]  = "Converting: dir2/99.a to dir1/99.b";
  $srch_strs[6]  = "Working With dir3/17.a";
  $srch_strs[7]  = "three";
  $srch_strs[8]  = "Converting: dir3/17.a to dir1/17.b";
  $srch_strs[9]  = "Working With dir1/88.a";
  $srch_strs[10] = "five";
  $srch_strs[11] = "Converting: dir1/88.a to 88.b";
  $srch_strs[12] = "";
  $srch_strs[13] = "";
  $srch_strs[14] = "";
  $status = runMkTest( $makefile, 11, $test_desc, @srch_strs );
  if (!$error) { $error = $status }


  OdeFile::deltree( $OdeTest::tmpfile );
  return( $error );

}

#########################################
#
# sub runMkTest
# - execute and validate a mk test
#
# - returns 1 is makefile not found, otherwise
#   returns test error condition
# - arguments
#   makefile - makefile to use
#   testno - test number
#   test_desc - test description
#   search_strs - array of strings to search for 
#                 in temp file to validate test
#
#########################################
sub runMkTest( $$$ )
{
  my($makefile, $testno, $test_desc, @search_strs) = @_;
  my $command;
  my $status;
  my $error = 0;
  my $mk_error = 0;
  my $error_txt;
  my $makefile_dir = "suffix" . $OdeEnv::dirsep;
  my $makefile_name = $makefile_dir . $makefile;
  my $curr_str;

  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
 	{
    OdeInterface::printError( "mk : " . $test_desc );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
	}
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
  # Check if any tests failed
  if (!$mk_error)
	{
    foreach $curr_str (@search_strs)
	  {
      if ( !OdeFile::findInFile( $curr_str, $OdeTest::tmpfile ))
	    {
        $mk_error = 1;
        $error_txt = "mk " . $curr_str . " error";
        last;
	    }
	  }
  }
  if ($mk_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                               $testno, $test_desc );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                               $testno, $test_desc );
  }

  return( $error );
}


#########################################
#
# sub runMkErrorTest
# - execute and validate a mk test that is 
#   expected to fail and generate an error
#   condtion
#
# - returns 1 is makefile not found, otherwise
#   returns test error condition
# - arguments
#   makefile - makefile to use
#   testno - test number
#   test_desc - test description
#
#########################################
sub runMkErrorTest( $$$ )
{
  my($makefile, $testno, $test_desc ) = @_;
  my $command;
  my $status;
  my $error = 0;
  my $makefile_dir = "suffix" . $OdeEnv::dirsep;
  my $makefile_name = $makefile_dir . $makefile;

  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
 	{
    OdeInterface::printError( "mk : " . $test_desc );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
	}
  $command = "mk -f $makefile > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status == 0)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                               $testno, $test_desc );
    OdeInterface::logPrint( "Test Failed: mk returned a zero exit code\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                               $testno, $test_desc );
  }

  return( $error );
}

####################################################################
#
# sub runMkDebug_s
# - to make and run the command with all the
#   arguments passed and the debug flag "s"
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   debug_lines: number of debug lines. These are prepended by Suff:
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   args: a list of any other commandline args
#
####################################################################
sub runMkDebug_s( $@ )
{
  my ($filename, $tststr, $debug_lines, $num_tests, $testno, @args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $suff_count = 0;
  my $status;
  my $error_txt;
  $makefile_name = "suffix" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  $command = "mk -f $makefile @args -ds > $OdeTest::tmpfile $OdeEnv::stderr_redir";
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
  $suff_count = OdeFile::numPatternsInFile( "Suff:", $OdeTest::tmpfile );
  if (!$mk_error && ($suff_count != $debug_lines))
  {
    $mk_error = 1;
    $error_txt = "Output has incorrect number of debug lines";
  }
  $suff_count = $debug_lines + $num_tests;
  if (!$mk_error &&
         (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != $suff_count))
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
