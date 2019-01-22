####################################################
#
# PkgToolParserGeneratorTest_syntax_requiredfields module
# - module to test the requiredfields in a cmf file 
#
####################################################

package PkgToolParserGeneratorTest_syntax_requiredfields;

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
  my $error = 0;
  my $dir = cwd();
  my $changed_cmf;
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

  OdeInterface::logPrint( 
                "in PkgToolParserGeneratorTest_syntax_requiredfields\n" );
  ## cd'ing to the temp directory
  OdePath::chgdir( $OdeEnv::tempdir );

  ## Test 1: running parsergeneratorinitiator on a cmf file with 
  ## a missing entityName field
  $changed_cmf = "$cmf_file.tmp.$$";
  copy( $cmf_file, $changed_cmf );
  OdeFile::removeLinesFromFile( $changed_cmf, "entityName" );
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$OdeEnv::tempdir " .
           "-DPKG_CONTROL_DIR=$OdeEnv::tempdir -DPKG_CMF_FILE=$changed_cmf " .
           "-DPKG_CLASS=IPP -DPKG_TYPE=USER -DPKGFAMILY=$pkgFamily " .
           "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
           ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  if (!defined( $OdeEnv::WIN32 ))
  {
    $status = runErrorTest( $command, 1 );
  }
  else
  {
    $status = runTest( $command, 1 );
  }
  if (!$error) { $error = $status; }

  OdeFile::deltree( $changed_cmf );

  ## Test 2: running parsergeneratorinitiator on a cmf file with
  ## a missing parent field
  $changed_cmf = "$cmf_file.tmp.$$";
  copy( $cmf_file, $changed_cmf );
  OdeFile::removeLinesFromFile( $changed_cmf, "parent" );
  if (!defined( $OdeEnv::WIN32 ) && !defined( $OdeEnv::MVS ))
  {
    $status = runErrorTest( $command, 2 );
  }
  else
  {
    $status = runTest( $command, 2 );
  }
  if (!$error) { $error = $status; }

  OdeFile::deltree( $changed_cmf );

  ## Test 3: running parsergeneratorinitiator on a cmf file with
  ## a missing fileType field
  $changed_cmf = "$cmf_file.tmp.$$";
  copy( $cmf_file, $changed_cmf );
  OdeFile::removeLinesFromFile( $changed_cmf, "fileType" );
  if (!defined( $OdeEnv::WIN32 ))
  {
    $status = runErrorTest( $command, 3 );
  }
  else
  {
    $status = runTest( $command, 3 );
  }
  if (!$error) { $error = $status; }

  OdeFile::deltree( $changed_cmf );

  ## Test 4: running parsergeneratorinitiator on a cmf file with
  ## a missing targetDir field
  $changed_cmf = "$cmf_file.tmp.$$";
  copy( $cmf_file, $changed_cmf );
  OdeFile::removeLinesFromFile( $changed_cmf, "sourceFile" );
  if (!defined( $OdeEnv::WIN32 ))
  {
    $status = runErrorTest( $command, 4 );
  }
  else
  {
    $status = runTest( $command, 4 );
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
       "pkgtools", $testno, "parsergeneratorinitiator syntax-requiredfields" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'},
       "pkgtools", $testno, "parsergeneratorinitiator syntax-requiredfields" );
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
       "pkgtools", $testno, "parsergeneratorinitiator syntax-requiredfields" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'},
       "pkgtools", $testno, "parsergeneratorinitiator syntax-requiredfields" );
  }
  return $pkgtools_error;
}

1;
