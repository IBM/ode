#!/usr/local/bin/perl
# odereadme.pl for ode build document   
#basic script to create a defect and update files for the next build

# Get the environment variable and set to a local variable
$SIMULATE = $ENV{'SIMULATE'};

$README = '/ode/test/odebld/doc/txts/fixes.txt';
$READMETMP = '/ode/test/odebld/doc/readme.tmp';
$DEFECTS = '/ode/test/odebld/doc/tracks.lst';

# Insert the new defects into the readme.txt
$INSERT_POINT = "*** FIXES PER BUILD ***\n";
open( README_FILE, "< $README" );
open( READMETMP_FILE, "> $READMETMP" );
while (!eof( README_FILE ))
{
  $line1=readline( *README_FILE );
  if ($line1 eq $INSERT_POINT)
  {
    printf READMETMP_FILE "%s\n", $INSERT_POINT; 
    open( DEFECTS_FILE, "< $DEFECTS" );
    while (!eof( DEFECTS_FILE ))
    {
      $line1 = readline( *DEFECTS_FILE );
      printf READMETMP_FILE "%s", $line1;
    }
    close( DEFECTS_FILE );
  }
  else
  {
    printf READMETMP_FILE "%s", $line1;
  }
}
close( README_FILE );
close( READMETMP_FILE );

rename $READMETMP, $README;


