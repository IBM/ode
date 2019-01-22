####################################################
#
# PkgToolParserGeneratorTest_syntax_semicolon module
# - module to test the semicolons in a cmf file 
#
####################################################

package PkgToolParserGeneratorTest_syntax_semicolon;

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
  my $orig_expr;
  my $chg_expr;
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

  OdeInterface::logPrint( "in PkgToolParserGeneratorTest_syntax_semicolon\n" );
  ## cd'ing to the temp directory
  OdePath::chgdir( $OdeEnv::tempdir );

  ## Test 1: running parsergeneratorinitiator on a cmf file with a missing
  ## semicolon in entityName field
  $orig_expr = "\\s+entityName\\s+=\\s+\"tdirpkg\"\;";
  $chg_expr = "entityName=\"tdirpkg\"";
  $changed_cmf = replaceLine( $cmf_file, $orig_expr, $chg_expr );
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

  ## Test 2: running parsergeneratorinitiator on a cmf file with an extra
  ## semicolon at the beginning of entityName field
  $orig_expr = "\\s+entityName\\s+=\\s+\"tdirpkg\"\;";
  $chg_expr = "\;entityName=\"tdirpkg\"\;";
  $changed_cmf = replaceLine( $cmf_file, $orig_expr, $chg_expr );
  if (!defined( $OdeEnv::WIN32 ))
  {
    $status = runErrorTest( $command, 2 );
  }
  else
  {
    $status = runTest( $command, 2 );
  }
  if (!$error) { $error = $status; }

  OdeFile::deltree( $changed_cmf );

  ## Test 3: running parsergeneratorinitiator on a cmf file with an extra
  ## semicolon at the end of entityName field
  $orig_expr = "\\s+entityName\\s+=\\s+\"tdirpkg\"\;";
  $chg_expr = "entityName=\"tdirpkg\"\;\;";
  $changed_cmf = replaceLine( $cmf_file, $orig_expr, $chg_expr );
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

  ## Test 4: running parsergeneratorinitiator on a cmf file with an = and extra
  ## semicolon at the end of entityName field
  $orig_expr = "\\s+entityName\\s+=\\s+\"tdirpkg\"\;";
  $chg_expr = "entityName=\"tdirpkg\"\;=\;";
  $changed_cmf = replaceLine( $cmf_file, $orig_expr, $chg_expr );
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

  ## Test 5: running parsergeneratorinitiator on a cmf file with an extra
  ## semicolon between InstallEntity and {
  $orig_expr = "InstallEntity\\s+\\\{";
  $chg_expr = "InstallEntity\;\{";
  $changed_cmf = replaceLine( $cmf_file, $orig_expr, $chg_expr );
  if (!defined( $OdeEnv::WIN32 ))
  {
    $status = runErrorTest( $command, 5 );
  }
  else
  {
    $status = runTest( $command, 5 );
  }
  if (!$error) { $error = $status; }

  OdeFile::deltree( $changed_cmf );

  ## Test 6: running parsergeneratorinitiator on a cmf file with an invalid 
  ## assignment at the end of entityName field
  $orig_expr = "\\s+entityName\\s+=\\s+\"tdirpkg\"\;";
  $chg_expr = "entityName=\"tdirpkg\"\; crap=crap\;";
  $changed_cmf = replaceLine( $cmf_file, $orig_expr, $chg_expr );
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

  ## Test 7: running parsergeneratorinitiator on a cmf file with a valid 
  ## assignment at the end of entityName field
  $orig_expr = "\\s+entityName\\s+=\\s+\"tdirpkg\"\;";
  $chg_expr = "entityName=\"tdirpkg\"\; description=\"test\"\;";
  $changed_cmf = replaceLine( $cmf_file, $orig_expr, $chg_expr );
  $status = runTest( $command, 7 );
  if (!$error) { $error = $status; }

  @delete_list = ("pcd.pd", "tfilepkg.il", "pcd.psf", "prototype", "pkginfo",
                  "pcd.mvs", $changed_cmf);
  OdeFile::deltree( \@delete_list );

  OdePath::chgdir( $dir );
  return( $error );
}


#################################################
#
# sub replaceLine
# - to create another file with a line replaced
#   returns the name of the new file
#
# - arguments
#   file: the file whose duplicate is to be created
#   orig_expr: the regular expression for the line 
#              to be replaced
#   chg_expr: the  regular expression for the
#             replacement line
#################################################
sub replaceLine( $$ )
{
  my ($file, $orig_expr, $chg_expr) = @_;
  my $old_cmf = $file;
  my $changed_cmf = "$file.tmp.$$";
  my $tmpstr;

  open( OLD, "< $old_cmf" ) ||
      OdeInterface::logPrint( "Could not open file $old_cmf\n" );
  open( NEW, "> $changed_cmf" ) ||
      OdeInterface::logPrint( "Could not open file $changed_cmf\n" );
  while (!eof( OLD )) 
  {
    $tmpstr = readline( *OLD );
    if ($tmpstr =~ /$orig_expr/)
    {
      $tmpstr =~ s/$orig_expr/$chg_expr/; 
    }
    print( NEW $tmpstr );
  }
  close( OLD );
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
           "pkgtools", $testno, "parsergeneratorinitiator syntax-semicolon" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'},
           "pkgtools", $testno, "parsergeneratorinitiator syntax-semicolon" );
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
           "pkgtools", $testno, "parsergeneratorinitiator syntax-semicolon" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'},
           "pkgtools", $testno, "parsergeneratorinitiator syntax-semicolon" );
  }
  return $pkgtools_error;
}

1;
