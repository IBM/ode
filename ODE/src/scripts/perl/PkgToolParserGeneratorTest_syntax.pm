####################################################
#
# PkgToolParserGeneratorTest_syntax module
# - module to test the syntax of cmf file 
#
####################################################

package PkgToolParserGeneratorTest_syntax;

use OdeEnv;
use OdeFile;
use OdeTest;
use OdeUtil;
use OdeInterface;
use strict;

use PkgToolParserGeneratorTest_syntax_semicolon;
use PkgToolParserGeneratorTest_syntax_brackets;
use PkgToolParserGeneratorTest_syntax_datatypes;
use PkgToolParserGeneratorTest_syntax_comments;
use PkgToolParserGeneratorTest_syntax_labels;
use PkgToolParserGeneratorTest_syntax_requiredfields;
#########################################
#
# sub run
#
#########################################
sub run ()
{
  my $syntax_error = 0;
  my $cmf_file;
  my $testfile;
  my $status;

  OdeInterface::logPrint( "in PkgToolParserGeneratorTest_syntax\n" );

  if (defined( $OdeEnv::AIX ) || defined( $OdeEnv::WIN32 ))
  {
    $cmf_file = "cmf_pp_aix";
  }
  elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
         defined( $OdeEnv::IRIX ))
  {
    $cmf_file = "cmf_pp_solaris";
  }
  elsif (defined( $OdeEnv::HP ))
  {
    $cmf_file = "cmf_pp_hp";
  }
  elsif (defined( $OdeEnv::MVS ))
  {
    $cmf_file = "cmf_pp_mvs";
  }

  $testfile = $OdeEnv::tempdir . $OdeEnv::dirsep . $cmf_file;
  if (! -f $testfile)
  {
    OdeInterface::printError(
                 "PkgToolParserGeneratorTest_syntax : syntax test" );
    OdeInterface::logPrint( "Could not locate cmf file - $testfile\n" );
    return( 1 );
  }

  ## Testing semicolons in a cmf file
  $status = PkgToolParserGeneratorTest_syntax_semicolon::run( $cmf_file );
  if (!$syntax_error) { $syntax_error = $status; }

  ## Testing brackets in a cmf file
  $status = PkgToolParserGeneratorTest_syntax_brackets::run( $cmf_file );
  if (!$syntax_error) { $syntax_error = $status; }

  ## Testing datatypes in a cmf file
  $status = PkgToolParserGeneratorTest_syntax_datatypes::run( $cmf_file );
  if (!$syntax_error) { $syntax_error = $status; }

  ## Testing comments in a cmf file
  $status = PkgToolParserGeneratorTest_syntax_comments::run( $cmf_file );
  if (!$syntax_error) { $syntax_error = $status; }

  ## Testing labels in a cmf file
  $status = PkgToolParserGeneratorTest_syntax_labels::run( $cmf_file );
  if (!$syntax_error) { $syntax_error = $status; }

  ## Testing required fields in a cmf file
  $status = PkgToolParserGeneratorTest_syntax_requiredfields::run( $cmf_file );
  if (!$syntax_error) { $syntax_error = $status; }

  return( $syntax_error );
}

1;
