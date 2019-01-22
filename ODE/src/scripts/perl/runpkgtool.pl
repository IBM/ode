#!/usr/bin/perl

#BEGIN block and FindBin are used to be able to run this script from anywhere by
#specifying fullpath to it and to get along without specifying -I on OS2 and MVS

BEGIN
{
  if ($^O eq os2)
  {
    push( @INC, "O:\\tools\\i386_os2\\perl\\lib" );
    push( @INC, "O:\\tools\\i386_os2\\perl\\lib\\os2\\5.00305" );
  }
  elsif ($^O eq os390)
  {
    push( @INC, "/u/ode/bin/perl/lib/perl5");
   }
}
use FindBin;
use lib "$FindBin::Bin/lib";
use lib "$FindBin::Bin";

## wrapper for PkgToolsTest ##
use strict;
use Pkgtoolstest;
OdeEnv::setPlBase();

OdeInterface::logPrint( "\n\tRunning pkgtools Test Script...\n\n" );

if (!Pkgtoolstest::platform_supported()) 
{
  OdeInterface::logPrint( 
                "\nPackaging not supported on `$OdeEnv::machine'\n" );
  OdeFile::deltree( $OdeEnv::tempdir );
  exit( 0 );
}

# execute rules test
my $status = Pkgtoolstest::run();
if ($status) 
{ 
  OdeInterface::logPrint("PkgTools test failed!\n");
}
else
{
  OdeInterface::logPrint("PkgTools test passed!\n");
}
OdeInterface::logPrint( "See $OdeEnv::bldcurr for more details.\n" );

# Delete temp working directory
OdeFile::deltree( $OdeEnv::tempdir );

exit( $status );
