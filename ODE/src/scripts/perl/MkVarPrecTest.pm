#####################################################
#
# MkVarPrecTest module
# - module to test variable precedence in makefiles
#
#####################################################

package MkVarPrecTest;

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
  my $testfile = $OdeEnv::tempdir . $OdeEnv::dirsep . "testfile.mk";
  my @delete_list = ($OdeTest::tmpfile, $testfile);

  OdeInterface::logPrint( "in MkVarPrecTest\n");
 
  $ENV{'PREC_VAR1'} = "env_var1";
  $ENV{'PREC_VAR4'} = "env_var4";
  $ENV{'PREC_VAR6'} = "env_var6";
  $ENV{'PREC_VAR7'} = "env_var7";
  $ENV{'PREC_VAR8'} = "env_var8";

  open( INPUT, "> $testfile" ) ||
    OdeInterface::logPrint( "Could not open file $testfile\n" );
  print( INPUT "PREC_VAR8=global_var8" );
  close( INPUT );

  ## Testing variable precedence
  ## running mk -f var_precedence.mk
  $status = runMkTest( "var_precedence.mk", "variable precedence", 8, 1,
                        "PREC_VAR3=cmd_var3", "PREC_VAR5=cmd_var5", 
                        "PREC_VAR6=cmd_var6", "PREC_VAR7=cmd_var7",
                        "FILEPATH=$OdeEnv::tempdir" );
  if (!$error) { $error = $status; }

  $status = runMkTest( "var_precedence.mk", "variable precedence", 8, 2,
                        "PREC_VAR3=cmd_var3", "PREC_VAR5=cmd_var5",
                      "PREC_VAR6=cmd_var6", "PREC_VAR7=cmd_var7", 
                      "-e FLAG=-E", "FILEPATH=$OdeEnv::tempdir" );
  if (!$error) { $error = $status; }

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
#   args: reference to a list of any other commandline args
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
