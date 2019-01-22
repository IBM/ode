#########################################
#
# Pkgtoolstest module
# - module to run Pkgtoolstest for different
#   test levels
#
#########################################

package Pkgtoolstest;

use File::Path;
use File::Copy;
use File::Basename;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdeUtil;
use OdeInterface;
use strict;

use PkgToolParserGeneratorTest;

#########################################
#
# sub platform_supported
# - returns 0 if platform is not supported
# - returns 1 if platform is supported
#
#########################################
sub platform_supported()
{
  # If a new platform is supported for packaging, then add it here.
  # When Monterey support is added, just change AIX_PPC to AIX.
  return (defined( $OdeEnv::AIX_PPC ) || defined( $OdeEnv::HP ) ||
          defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::WIN32 ) ||
          defined( $OdeEnv::MVS  ) || defined( $OdeEnv::SCO ) ||
          defined( $OdeEnv::IRIX ));
}

#########################################
#
# sub run
# - run test sequence based on testlevel
#
#########################################
sub run()
{
  my $status;
  
  OdeInterface::logPrint( "in PkgToolstest\n" );
  if (!platform_supported())
  {
    OdeInterface::logPrint(
                  "\nPackaging not supported on `$OdeEnv::machine'\n" );
    return 0;
  }
  if (( $OdeUtil::arghash{'testlevel'} eq "normal" )
      || ( $OdeUtil::arghash{'testlevel'} eq "regression" )
      || ( $OdeUtil::arghash{'testlevel'} eq "fvt" ))
  {
    $status = normalandregressionandfvt();
    return( $status );
  }
  else
  {
    OdeInterface::logPrint( 
      "ERROR: undefined testlevel $OdeUtil::arghash{'testlevel'}\n" );
    return( 1 );
  }

}


#########################################
#
# sub normalandregressionandfvt
# - execute Normal, Regression and FVT 
#   set of tests
#
#########################################
sub normalandregressionandfvt()
{
  my $status;
  my $pkgtools_error = 0;
  my $parsergenerator_error = 0;

  # execute parsergenerator test
  $status = PkgToolParserGeneratorTest::run();
  if ($status) 
  {
    $parsergenerator_error = $status;
    $pkgtools_error = 1;
  }  

  # check all error codes and report accordingly
  if ($pkgtools_error)
  {
    if ($parsergenerator_error) 
    { 
      OdeInterface::logPrint( 
                "  Pkgtools - ParserGeneratorInitiator test failed\n");
    }
  }
  else
  {
    OdeInterface::logPrint( "All Pkgtools tests passed!\n");
  }

  return( $pkgtools_error );
}

1;
