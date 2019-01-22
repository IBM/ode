####################################################
#
# PkgToolParserGeneratorTest_syntax_comments module
# - module to test the comments in a cmf file 
#
####################################################

package PkgToolParserGeneratorTest_syntax_comments;

use File::Copy;
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
sub run ($)
{
  my ($cmf_file) = @_;
  my $changed_cmf;
  my $error = 0;
  my $dir = cwd();
  my $command;
  my $status;
  my @delete_list;
  my $pkgFamily ;

  if (defined( $OdeEnv::AIX ))
  {
    $pkgFamily = "mkinstall" ;
  }
  elsif (defined( $OdeEnv::WIN32 ))
  {
    $pkgFamily = "ispe" ;
  }
  elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
         defined( $OdeEnv::IRIX ))
  {
    $pkgFamily = "pkgmk" ;
  }
  elsif (defined( $OdeEnv::HP ))
  {
    $pkgFamily = "swpackage" ;
  }
  elsif (defined( $OdeEnv::MVS ))
  {
    $pkgFamily = "mvs" ;
  }

  OdeInterface::logPrint( "in PkgToolParserGeneratorTest_syntax_comments\n" );
  ## cd'ing to the temp directory
  OdePath::chgdir( $OdeEnv::tempdir );

  ## Test 1: running parsergeneratorinitiator on a cmf file with a single line
  ## comment added to the file
  $changed_cmf = appendLine( $cmf_file, "//this is a single comment line\n" );
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$OdeEnv::tempdir " .
           "-DPKG_CONTROL_DIR=$OdeEnv::tempdir -DPKG_CMF_FILE=$changed_cmf " .
           "-DPKG_CLASS=IPP -DPKG_TYPE=USER -DPKGFAMILY=$pkgFamily " .
           "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
           ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = runTest( $command, 1 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( $changed_cmf );

  ## Test 2: running parsergeneratorinitiator on a cmf file with a multi line
  ## comment added to the file
  $changed_cmf = appendLine( $cmf_file, "/*this is a multi\n", 
                                        "comment line*/\n" );
  $status = runTest( $command, 2 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( $changed_cmf );

  ## Test 3: running parsergeneratorinitiator on a cmf file with a multi line
  ## comment added to the file in a single line
  $changed_cmf = appendLine( $cmf_file, "/*this is a multi comment line*/\n" );
  $status = runTest( $command, 3 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( $changed_cmf );

  ## Test 4: running parsergeneratorinitiator on a cmf file with a multi line
  ## comment enclosed in /* and */ added to the file
  $changed_cmf = appendLine( $cmf_file, 
                             "/*/*this is a multi comment line*/*/\n" );
  if (!defined( $OdeEnv::WIN32 ))
  {
    $status = runErrorTest( $command, 4 );
  }
  else
  {
    $status = runTest( $command, 4 );
  }
  if (!$error) { $error = $status; }

  OdeFile::deltree( $changed_cmf );

  ## Test 5: running parsergeneratorinitiator on a cmf file with a multi line
  ## comment containing // added at the end of the file
  $changed_cmf = appendLine( $cmf_file, 
                             "/*//this is a single comment line*/\n" );
  $status = runTest( $command, 5 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( $changed_cmf );

  ## Test 6: running parsergeneratorinitiator on a cmf file with a multi line
  ## comment ending with a /* added to the file
  $changed_cmf = appendLine( $cmf_file, "/*this is a multi comment line/*\n" );
  if (!defined( $OdeEnv::WIN32 ))
  {
    $status = runErrorTest( $command, 6 );
  }
  else
  {
    $status = runTest( $command, 6 );
  }
  if (!$error) { $error = $status; }

  OdeFile::deltree( $changed_cmf );

  ## Test 7: running parsergeneratorinitiator on a cmf file with a multi line
  ## comment with a missing */ added to the file
  $changed_cmf = appendLine( $cmf_file, "/*this is a multi comment line\n" );
  if (!defined( $OdeEnv::WIN32 ))
  {
    $status = runErrorTest( $command, 7 );
  }
  else
  {
    $status = runTest( $command, 7 );
  }
  if (!$error) { $error = $status; }

  @delete_list = ("pcd.pd", "tfilepkg.il", "pcd.psf", "prototype", "pkginfo",
                  "pcd.mvs", $changed_cmf);
  OdeFile::deltree( \@delete_list );
  OdePath::chgdir( $dir );
  return( $error );
}


#################################################
#
# sub appendLine
# - to create another file with the required lines
#   appended to it
#   returns the name of the new file
#
# - arguments
#   file: the file whose duplicate is to be created
#   newlines: a list of new lines to be appended
#################################################
sub appendLine( $$ )
{
  my ($old_cmf, @newlines) = @_;
  my $changed_cmf = "$old_cmf.tmp.$$";
  my $line;

  copy( $old_cmf, $changed_cmf );
  open( NEW, ">> $changed_cmf" ) ||
      OdeInterface::logPrint( "Could not open file $changed_cmf\n" );
  foreach $line (@newlines)
  {
    print( NEW $line );
  }
  close( NEW );
  return( $changed_cmf );
}
  
#################################################
#
# sub runTest
# - to run the command and check if an error
#   occured
#
# - arguments
#   command: the command to be run
#   testno: test number
#################################################
sub runTest( $$ )
{
  my ($command, $testno) = @_;
  my $status;
  my $error_txt;
  my $pkgtools_error = 0;

  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status)
  {
    $pkgtools_error = 1;
    $error_txt = "parsergeneratorinitiator returned a non-zero exit code";
  }
  if ($pkgtools_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'},
           "pkgtools", $testno, "parsergeneratorinitiator syntax-comments" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'},
           "pkgtools", $testno, "parsergeneratorinitiator syntax-comments" );
  }
  return $pkgtools_error;
}

#################################################
#
# sub runErrorTest
# - to run the command and check if an error
#   occured
#
# - arguments
#   command: the command to be run
#   testno: test number
#################################################
sub runErrorTest( $$ )
{
  my ($command, $testno) = @_;
  my $status;
  my $error_txt;
  my $pkgtools_error = 0;

  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if (!$status)
  {
    $pkgtools_error = 1;
    $error_txt = "parsergeneratorinitiator returned a zero exit code";
  }
  if ($pkgtools_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'},
           "pkgtools", $testno, "parsergeneratorinitiator syntax-comments" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'},
           "pkgtools", $testno, "parsergeneratorinitiator syntax-comments" );
  }
  return $pkgtools_error;
}

1;
