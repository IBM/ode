####################################################
#
# PkgToolParserGeneratorTest module
# - module to test pkgtool parsergeneratorinitiator
#
####################################################

package PkgToolParserGeneratorTest;

use OdeEnv;
use OdeFile;
use OdeTest;
use OdeUtil;
use OdeInterface;
use strict;
use Cwd;

use PkgToolParserGeneratorTest_parameters;
use PkgToolParserGeneratorTest_syntax;
#########################################
#
# sub run
#
#########################################
sub run ()
{
  my $parsergenerator_error = 0;
  my $parameters_error = 0;
  my $syntax_error = 0;
  my @delete_list;
  my $cmf_dir = join( $OdeEnv::dirsep, (".", "cmf") );
  my $cmf_file;
  my $tdir;
  my $status;

  OdeInterface::logPrint( "in PkgToolParserGeneratorTest\n" );

  ## locating the cmf directory
  if (! -d $cmf_dir )
  {
    $cmf_dir = join( $OdeEnv::dirsep, ($OdeEnv::bb_base, "src", "scripts",
                                       "perl", "cmf") );
    if (! -d $cmf_dir)
    {
      OdeInterface::printError( "Pkgtools: parsergeneratorinitiator" );
      OdeInterface::logPrint(
          "Could not locate the cmf directory - $cmf_dir\n" );
      return( 1 );
    }
  }

  $tdir = $OdeEnv::tempdir . $OdeEnv::dirsep;
  @delete_list = ($tdir . "cmf_pp_aix", $tdir . "cmf_pp_solaris",
                  $tdir . "cmf_pp_hp", $tdir . "cmf_pp_mvs");
  # remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  $cmf_file = $cmf_dir . $OdeEnv::dirsep . "*";
  ## copying all the cmf files into the temp directory
  OdeEnv::runSystemCommand( "$OdeEnv::copy $cmf_file $OdeEnv::tempdir" );

  #execute tests for parameters
  $status = PkgToolParserGeneratorTest_parameters::run();
  if ($status)
  {
    $parameters_error = $status;
    $parsergenerator_error = 1;
  }
  #execute tests for syntax
  $status = PkgToolParserGeneratorTest_syntax::run();
  if ($status)
  {
    $syntax_error = $status;
    $parsergenerator_error = 1;
  }

  if ($parsergenerator_error)
  {
    if ($parameters_error)
    {
      OdeInterface::logPrint( 
              " ParserGeneratorInitiator - parameters test failed\n");
    }
    if ($syntax_error)
    {
      OdeInterface::logPrint( 
              " ParserGeneratorInitiator - syntax test failed\n");
    }
  }
  else
  {
    OdeInterface::logPrint( "All ParserGeneratorInitiator tests passed\n");
  }

  OdeFile::deltree( \@delete_list );
  return( $parsergenerator_error );
}

1;
