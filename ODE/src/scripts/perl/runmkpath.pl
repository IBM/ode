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

## wrapper for Mkpathtest ##
use strict;
use Mkpathtest;
OdeEnv::setPlBase();

OdeInterface::logPrint( "\n\tRunning mkpath Test Script...\n\n" );

# execute mkpath test
my $status = Mkpathtest::run();
if ($status) 
{ 
  OdeInterface::logPrint("mkpath test failed!\n");
}
else
{
  OdeInterface::logPrint("mkpath test passed!\n");
}
OdeInterface::logPrint("See $OdeEnv::bldcurr for more details.\n");

# Delete temp working directory
OdeFile::deltree( $OdeEnv::tempdir );
exit( $status );
